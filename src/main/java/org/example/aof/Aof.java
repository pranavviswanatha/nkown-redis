package org.example.aof;

import org.example.handler.RequestHandler;
import org.example.resp.MarshalValue;
import org.example.resp.Resp;
import org.example.resp.Value;

import java.io.*;
import java.nio.file.Files;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Aof {

    private final File file;
    private final BufferedReader reader;
    private final OutputStream fos;
    private final Lock lock;

    public Aof(File file) throws IOException {
        this.file = file;
        if (!file.exists()) {
            Files.createFile(file.toPath());
        }

        this.fos = new FileOutputStream(file, true);
        this.reader = new BufferedReader(new FileReader(file));
        this.lock = new ReentrantLock();

        startSync();
    }

    private void startSync() {
        Thread thread = new Thread(() -> {
            while(true) {
                lock.lock();
                try {
                    ((FileOutputStream)fos).getFD().sync();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void write (Value value) throws IOException {
        lock.lock();
        try {
            fos.write(MarshalValue.marshal(value));
        } finally {
            lock.unlock();
        }
    }

    public void close() throws IOException {
        lock.lock();
        try {
            if (reader != null) {
                reader.close();
            }
        } finally {
            lock.unlock();
        }
    }

    public void load() throws IOException {
        Resp resp = new Resp(new FileInputStream(file));
        Value value = null;
        while (true) {
            try {
                value = resp.read();
                RequestHandler.loadAof(value);
            } catch (IOException e) {
                break;
            }
        }
    }
}

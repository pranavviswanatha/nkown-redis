package org.example;

import org.example.aof.Aof;
import org.example.handler.ClientHandler;
import org.example.handler.RequestHandler;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Listening on port :6379");
        ServerSocket serverSocket =null;
        try {
            serverSocket = new ServerSocket(6379);
            serverSocket.setReuseAddress(true);
            Aof aof = null;
            try {
                aof = new Aof(new File("database.aof"));
                RequestHandler.setAof(aof);
                aof.load();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (aof != null) {
                    aof.close();
                }
            }
            Socket clientSocket = null;
            try {
                while (true) {
                    clientSocket = serverSocket.accept();
                    new Thread(new ClientHandler(clientSocket)).start();
                    System.out.println("Client connected "+clientSocket.getInetAddress());
                }
            } catch (IOException e) {
                System.out.println("Server connection failed!!");
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("Server creation failed!!");
            e.printStackTrace();
            return;
        }



    }
}
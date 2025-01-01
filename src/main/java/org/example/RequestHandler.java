package org.example;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class RequestHandler {
    private static Map<String, Function<Value[],Value>> handlers;
    private static final ReadWriteLock lock;

    private static Map<String, String> set;
    private static Map<String, ConcurrentHashMap<String, String>> hset;

    static {
        //different redis commands
        handlers = new HashMap<>();
        handlers.put("PING", RequestHandler::ping);
        handlers.put("SET", RequestHandler::set);
        handlers.put("GET", RequestHandler::get);
        handlers.put("HSET", RequestHandler::hset);
        handlers.put("HGET", RequestHandler::hget);

        //used for storage
        set = new ConcurrentHashMap<>();
        hset = new ConcurrentHashMap<>();
        lock = new ReentrantReadWriteLock();
    }

    public static Value handleRequest(Value request) throws IOException {
        if (request.type != Value.ARR || request.array.length == 0)
            throw new IOException("Invalid Request, no command provided!!");
        String cmd = request.array[0].bulk;
        Value[] args = Arrays.copyOfRange(request.array, 1, request.array.length);
        Function<Value[], Value> handler = handlers.get(cmd);
        if (handler == null)
            throw new IOException("Invalid Request, unknown command!!");
        return handler.apply(args);
    }

    private static Value okMessage() {
        return new ValueBuilder()
                .setType(Value.STR)
                .setStr("OK")
                .build();
    }

    public static Value errorMessage(IOException e) {
        return new ValueBuilder()
                .setType(Value.ERR)
                .setStr(e.getMessage())
                .build();
    }

    private static Value nullMessage() {
        return new ValueBuilder()
                .build();
    }

    private static Value ping(Value[] args) {
        if (args.length == 0) {
            return okMessage();
        }
        return new ValueBuilder()
                .setType(Value.STR)
                .setStr(args[0].bulk)
                .build();
    }

    private static Value set (Value[] args) {
        try {
            if (args.length != 2)
                throw new IOException("ERR wrong number of arguments for 'set' command");
            String k = args[0].bulk;
            String v = args[1].bulk;

            lock.writeLock().lock();
            set.put(k, v);
            lock.writeLock().unlock();

            return okMessage();
        } catch (IOException e) {
            return errorMessage(e);
        }
    }

    private static Value get(Value[] args) {
        try {
            if (args.length != 1)
                throw new IOException("ERR wrong number of arguments for 'get' command");
            String key = args[0].bulk;
            lock.readLock().lock();
            if (!set.containsKey(key))
                return nullMessage();
            String bulk = set.get(key);
            lock.readLock().unlock();
            return new ValueBuilder()
                    .setType(Value.BULK)
                    .setBulk(bulk)
                    .build();
        } catch (IOException e) {
            return errorMessage(e);
        }
    }

    private static Value hset (Value[] args) {
        try {
            if (args.length != 3)
                throw new IOException("ERR wrong number of arguments for 'hset' command");
            String k1 = args[0].bulk;
            String k2 = args[1].bulk;
            String v = args[2].bulk;

            lock.writeLock().lock();
            ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
            if (hset.containsKey(k1)) {
                map = hset.get(k1);
            }
            map.put(k2, v);
            hset.put(k1, map);
            lock.writeLock().unlock();

            return okMessage();
        } catch (IOException e) {
            return errorMessage(e);
        }
    }

    private static Value hget(Value[] args) {
        try {
            if (args.length != 2)
                throw new IOException("ERR wrong number of arguments for 'hget' command");
            String k1 = args[0].bulk;
            String k2 = args[1].bulk;

            lock.readLock().lock();
            String bulk = null;
            if (hset.containsKey(k1)) {
                ConcurrentHashMap<String,String> map = hset.get(k1);
                bulk = map.getOrDefault(k2, null);
            }
            lock.readLock().unlock();

            if (bulk == null)
                return nullMessage();
            return new ValueBuilder()
                    .setType(Value.BULK)
                    .setBulk(bulk)
                    .build();
        } catch (IOException e) {
            return errorMessage(e);
        }
    }

}
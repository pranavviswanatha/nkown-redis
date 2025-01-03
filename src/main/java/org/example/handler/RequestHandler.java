package org.example.handler;

import org.example.aof.Aof;
import org.example.resp.Value;
import org.example.resp.ValueBuilder;

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

    public static Aof aof;

    private static Map<String, String> set;
    private static Map<String, Map<String, String>> hset;

    static {
        //different redis commands
        handlers = new HashMap<>();
        handlers.put("PING", RequestHandler::ping);
        handlers.put("SET", RequestHandler::set);
        handlers.put("GET", RequestHandler::get);
        handlers.put("HSET", RequestHandler::hset);
        handlers.put("HGET", RequestHandler::hget);
        handlers.put("HGETALL", RequestHandler::hgetall);

        //used for storage
        set = new ConcurrentHashMap<>();
        hset = new ConcurrentHashMap<>();
        lock = new ReentrantReadWriteLock();
    }

    public static void loadAof(Value value) {
        String cmd = value.array[0].bulk;
        Value[] args = Arrays.copyOfRange(value.array, 1, value.array.length);
        Function<Value[], Value> handler = handlers.get(cmd);
        handler.apply(args);
    }

    public static Value handleRequest(Value request) throws IOException {
        if (request.type != ValueBuilder.ARR || request.array.length == 0)
            throw new IOException("Invalid Request, no command provided!!");
        String cmd = request.array[0].bulk;
        Value[] args = Arrays.copyOfRange(request.array, 1, request.array.length);
        Function<Value[], Value> handler = handlers.get(cmd);
        if (handler == null)
            throw new IOException("Invalid Request, unknown command!!");
        Value value = handler.apply(args);
        if (value.type!=ValueBuilder.NIL && cmd.matches("SET")) {
            aof.write(request);
        }
        return value;
    }

    private static Value okMessage() {
        return new ValueBuilder()
                .setType(ValueBuilder.STR)
                .setStr("OK")
                .build();
    }

    public static Value errorMessage(IOException e) {
        return new ValueBuilder()
                .setType(ValueBuilder.ERR)
                .setStr(e.getMessage())
                .build();
    }

    private static Value nilMessage() {
        return new ValueBuilder()
                .setType(ValueBuilder.NIL)
                .build();
    }

    private static Value ping(Value[] args) {
        if (args.length == 0) {
            return new ValueBuilder()
                    .setType(ValueBuilder.STR)
                    .setStr("PONG")
                    .build();
        }
        return new ValueBuilder()
                .setType(ValueBuilder.STR)
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
                return nilMessage();
            String bulk = set.get(key);
            lock.readLock().unlock();
            return new ValueBuilder()
                    .setType(ValueBuilder.BULK)
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
            Map<String, String> map = new ConcurrentHashMap<>();
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
                Map<String,String> map = hset.get(k1);
                bulk = map.getOrDefault(k2, null);
            }
            lock.readLock().unlock();

            if (bulk == null)
                return nilMessage();
            return new ValueBuilder()
                    .setType(ValueBuilder.BULK)
                    .setBulk(bulk)
                    .build();
        } catch (IOException e) {
            return errorMessage(e);
        }
    }

    private static Value hgetall(Value[] args) {
        try {
            if (args.length != 1)
                throw new IOException("ERR wrong number of arguments for 'hgetall' command");
            String key = args[0].bulk;
            lock.readLock().lock();
            Map<String, String> map = hset.getOrDefault(key, new ConcurrentHashMap<>());
            int i = 0, sz = map.size() * 2;
            Value[] values = new Value[sz];
            for (String s: map.keySet()) {
                values[i++] = new ValueBuilder()
                        .setType(ValueBuilder.BULK)
                        .setBulk(s)
                        .build();
            }
            lock.readLock().unlock();
            return new ValueBuilder()
                    .setType(ValueBuilder.ARR)
                    .setArray(values)
                    .build();
        } catch (IOException e) {
            return errorMessage(e);
        }
    }

    public static void setAof(Aof aof) {
        RequestHandler.aof = aof;
    }
}

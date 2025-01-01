package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Resp {

    private BufferedReader reader;
    public Resp(InputStream inputStream) {
        this.reader = new BufferedReader(new InputStreamReader(inputStream));
    }

    private String readLine() throws IOException {
        String line = reader.readLine();
        if (line == null)
            throw new IOException("End of stream!!");
        return line;
    }

    private Value readInt() throws IOException {
        Value value = new Value();
        value.type = Value.INT;
        value.num = readInteger();
        return value;
    }

    private Value readString() throws IOException {
        Value value = new Value();
        value.type = Value.STR;
        value.str = readLine();
        return value;
    }

    private int readInteger() throws IOException {
        String line =readLine();
        return Integer.parseInt(line);
    }

    private Value readArray() throws IOException {
        Value value = new Value();
        value.type = Value.ARR;
        int length = readInteger();
        value.array = new Value[length];
        for (int i=0;i<length;i++)
            value.array[i] = read();
        return value;
    }

    private Value readBulk() throws IOException {
        Value value = new Value();
        value.type = Value.BULK;
        int length = readInteger();
        if (length == -1) {
            value.bulk = null;
            return value;
        }

        char[] bulkData = new char[length];
        int byteRead = reader.read(bulkData);
        if (byteRead != length)
            throw new IOException("Incomplete bulk string data!!");

        value.bulk = new String(bulkData);
        //go to end of line for line.
        readLine();
        return value;
    }

    public Value read() throws IOException {
        int type = reader.read();
        if (type == -1)
            throw new IOException("End of stream.");
        switch (type) {
            case Value.ARR:   return readArray();
            case Value.BULK:  return readBulk();
            case Value.INT:   return readInt();
            case Value.STR:   return readString();
            case Value.ERR:
            default:    throw new IOException("Unknown Exception of Datatype!!");
        }
    }

}

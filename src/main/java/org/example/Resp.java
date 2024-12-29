package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Resp {

    final static char STR = '+';
    final static char ERR = '-';
    final static char INT = ':';
    final static char BULK = '$';
    final static char ARR = '*';

    static class Value {
        char type;
        String str;
        int num;
        String bulk;
        List<Value> array;

        Value() {
            this.array = new ArrayList<>();
        }
    }

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
        value.type = INT;
        value.num = readInteger();
        return value;
    }

    private Value readString() throws IOException {
        Value value = new Value();
        value.type = STR;
        value.str = readLine();
        return value;
    }

    private int readInteger() throws IOException {
        String line =readLine();
        return Integer.parseInt(line);
    }

    private Value readArray() throws IOException {
        Value value = new Value();
        value.type = ARR;
        int length = readInteger();
        for (int i=0;i<length;i++)
            value.array.add(read());
        return value;
    }

    private Value readBulk() throws IOException {
        Value value = new Value();
        value.type = BULK;
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
            case ARR:   return readArray();
            case BULK:  return readBulk();
            case INT:   return readInt();
            case STR:   return readString();
            case ERR:
            default:    throw new IOException("Unknown Exception of Datatype!!");
        }
    }

}

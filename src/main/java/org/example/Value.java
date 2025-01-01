package org.example;

import java.nio.charset.StandardCharsets;

public class Value {
    public final static char STR = '+';
    public final static char ERR = '-';
    public final static char INT = ':';
    public final static char BULK = '$';
    public final static char ARR = '*';
    public final static char NIL = '_';

    char type;
    String str;
    int num;
    String bulk;
    Value[] array;

    Value() {
        this.type = NIL;
        this.array = new Value[0];
    }

    public byte[] marshall() {
        switch (type) {
            case STR :  return marshallString();
            case BULK:  return marshallBulk();
            case ARR:   return marshallArray();
            case INT:   return marshallInt();
            case ERR:   return marshallError();
            case NIL:  return marshallNull();
            default:    return new byte[0];
        }
    }

    private byte[] marshallNull() {
        return "$-1\r\n".getBytes(StandardCharsets.UTF_8);
    }

    private byte[] marshallError() {
        StringBuilder sb = new StringBuilder();
        sb.append(ERR)
                .append(str)
                .append("\r\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] marshallString() {
        StringBuilder sb = new StringBuilder();
        sb.append(STR)
                .append(str)
                .append("\r\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] marshallBulk() {
        StringBuilder sb = new StringBuilder();
        sb.append(BULK)
                .append(bulk == null ? "-1" : bulk.length());
        sb.append("\r\n");
        if (bulk != null) {
            sb.append(bulk)
                    .append("\r\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] marshallArray() {
        StringBuilder sb = new StringBuilder();
        sb.append(ARR)
                .append(array.length)
                .append("\r\n");
        for (Value v : array) {
            sb.append(new String(v.marshall(),StandardCharsets.UTF_8));
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] marshallInt() {
        StringBuilder sb = new StringBuilder();
        sb.append(INT)
                .append(num)
                .append("\r\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }



}

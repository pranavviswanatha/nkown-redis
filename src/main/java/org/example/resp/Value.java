package org.example.resp;

import java.nio.charset.StandardCharsets;

public class Value {

    public char type;
    public String str;
    public int num;
    public String bulk;
    public Value[] array;

    Value() {
        this.array = new Value[0];
    }

    private byte[] marshallNull() {
        return "$-1\r\n".getBytes(StandardCharsets.UTF_8);
    }

    private byte[] marshalError() {
        StringBuilder sb = new StringBuilder();
        sb.append(ValueBuilder.ERR)
                .append(str)
                .append("\r\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }



}

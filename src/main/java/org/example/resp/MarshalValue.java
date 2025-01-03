package org.example.resp;

import java.nio.charset.StandardCharsets;

public class MarshalValue {
    public static byte[] marshal(Value value){
        if (value == null)
            return new byte[0];
        switch (value.type) {
            case ValueBuilder.STR :  return marshalString(value);
            case ValueBuilder.BULK:  return marshalBulk(value);
            case ValueBuilder.ARR:   return marshalArray(value);
            case ValueBuilder.INT:   return marshalInt(value);
            case ValueBuilder.ERR:   return marshalError(value);
            case ValueBuilder.NIL:  return marshalNull(value);
            default:    return new byte[0];
        }
    }

    private static byte[] marshalNull(Value value) {
        return "$-1\r\n".getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] marshalError(Value value) {
        StringBuilder sb = new StringBuilder();
        sb.append(ValueBuilder.ERR)
                .append(value.str)
                .append("\r\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] marshalInt(Value value) {
        StringBuilder sb = new StringBuilder();
        sb.append(ValueBuilder.INT)
                .append(value.num)
                .append("\r\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] marshalArray(Value value) {
        StringBuilder sb = new StringBuilder();
        sb.append(ValueBuilder.ARR)
                .append(value.array.length)
                .append("\r\n");
        for (Value v : value.array) {
            sb.append(new String(marshal(v),StandardCharsets.UTF_8));
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }


    private static byte[] marshalBulk(Value value) {
        StringBuilder sb = new StringBuilder();
        sb.append(ValueBuilder.BULK)
                .append(value.bulk == null ? "-1" : value.bulk.length());
        sb.append("\r\n");
        if (value.bulk != null) {
            sb.append(value.bulk)
                    .append("\r\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] marshalString(Value value) {
        StringBuilder sb = new StringBuilder();
        sb.append(ValueBuilder.STR)
                .append(value.str)
                .append("\r\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }



}

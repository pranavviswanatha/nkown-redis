package org.example;

import java.util.List;

public class ValueBuilder {
    private char type;
    private String str;
    private int num;
    private String bulk;
    private Value[] array;

    public ValueBuilder() {
        this.array = new Value[0];
    }

    public ValueBuilder setType(char type) {
        this.type = type;
        return this;
    }

    public ValueBuilder setStr(String str) {
        this.str = str;
        return this;
    }

    public ValueBuilder setNum(int num) {
        this.num = num;
        return this;
    }

    public ValueBuilder setBulk(String bulk) {
        this.bulk = bulk;
        return this;
    }

    public ValueBuilder setArray(Value[] array) {
        this.array = array;
        return this;
    }

    public Value build() {
        Value value = new Value();
        value.type = this.type;
        value.str = this.str;
        value.num = this.num;
        value.bulk = this.bulk;
        value.array = this.array;
        return value;
    }
}

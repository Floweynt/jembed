package com.floweytf.jembed.lang.sema;

public class Type {
    private final String name;

    public Type(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "Type('" + name + ")";
    }
}

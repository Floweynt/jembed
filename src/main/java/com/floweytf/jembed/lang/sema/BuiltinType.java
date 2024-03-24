package com.floweytf.jembed.lang.sema;

public class BuiltinType<T> extends Type {
    private final Class<T> underlyingClass;
    private final String name;

    public BuiltinType(Class<T> underlyingClass, String name) {
        super(name);
        this.underlyingClass = underlyingClass;
        this.name = name;
    }

    public Class<T> underlyingClass() {
        return underlyingClass;
    }

    @Override
    public String toString() {
        return "Type('" + name + "', " + underlyingClass.getName() + ")";
    }
}

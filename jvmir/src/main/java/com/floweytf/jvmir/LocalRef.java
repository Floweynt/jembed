package com.floweytf.jvmir;

import org.objectweb.asm.Type;

public record LocalRef(CGMethod method, int index) {
    public LocalRef {
        if (index > method.locals.size())
            throw new IllegalArgumentException("index >= locals");
    }

    public Type type() {
        return method.locals.get(index);
    }
}

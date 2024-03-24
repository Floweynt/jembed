package com.floweytf.jembed.lang.sema.sym;

import com.floweytf.jembed.lang.sema.OverloadedMethod;
import com.floweytf.jembed.lang.sema.results.TypeSemResult;

public class SymbolType<T, U> {
    public static final SymbolType<VariableSymbolInfo, VariableSymbolInfo> VALUE = new SymbolType<>(0);
    public static final SymbolType<VariableSymbolInfo, OverloadedMethod.Overload> OVERLOAD = new SymbolType<>(0);
    public static final SymbolType<Object, Object> VALUE_OR_OVERLOAD = new SymbolType<>(0);
    public static final SymbolType<TypeSemResult, TypeSemResult> TYPE = new SymbolType<>(0);

    private final int ord;

    private SymbolType(int ord) {
        this.ord = ord;
    }

    public int ord() {
        return ord;
    }
}

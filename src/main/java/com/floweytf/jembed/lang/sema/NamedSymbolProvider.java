package com.floweytf.jembed.lang.sema;

// This is something that provides named (order indep) symbols
// currently, this is only the module.
public interface NamedSymbolProvider {
    SymbolContainer getSymbolTable();
}

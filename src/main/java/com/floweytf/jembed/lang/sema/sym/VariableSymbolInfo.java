package com.floweytf.jembed.lang.sema.sym;

import com.floweytf.jembed.lang.ast.stmt.def.VarDefAST;
import com.floweytf.jembed.lang.sema.Type;
import com.floweytf.jembed.lang.sema.results.ExprSemResult;

public record VariableSymbolInfo(Type type, ExprSemResult.ValueCategory category, VarDefAST definition) {
}

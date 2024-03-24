package com.floweytf.jembed.lang.ast.type;

import com.floweytf.jembed.lang.ast.SemanticAST;
import com.floweytf.jembed.lang.sema.results.TypeSemResult;
import com.floweytf.jembed.lang.source.CodeLocation;
import com.floweytf.jembed.lang.source.CodeRange;

public abstract class TypeAST extends SemanticAST<TypeSemResult> {
    public TypeAST(CodeRange range) {
        super(range);
    }

    public TypeAST(CodeLocation left, CodeLocation right) {
        super(left, right);
    }
}

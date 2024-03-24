package com.floweytf.jembed.lang.ast.expr;

import com.floweytf.jembed.lang.ast.SemanticAST;
import com.floweytf.jembed.lang.sema.results.ExprSemResult;
import com.floweytf.jembed.lang.source.CodeLocation;
import com.floweytf.jembed.lang.source.CodeRange;

/**
 * An AST that has a value and a type.
 */
public abstract class ExprAST extends SemanticAST<ExprSemResult> {
    /**
     * @param range The entire code range for this AST
     */
    public ExprAST(CodeRange range) {
        super(range);
    }

    /**
     * @param left  The code location of the starting character of this AST
     * @param right The code location of the ending character of this AST
     */
    public ExprAST(CodeLocation left, CodeLocation right) {
        super(left, right);
    }
}

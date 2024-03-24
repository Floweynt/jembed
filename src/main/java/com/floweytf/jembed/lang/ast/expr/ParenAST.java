package com.floweytf.jembed.lang.ast.expr;

import com.floweytf.jembed.lang.lexer.Token;
import com.floweytf.jembed.lang.sema.AnalysisFlags;
import com.floweytf.jembed.lang.sema.ctx.SemanticContext;
import com.floweytf.jembed.lang.sema.results.ExprSemResult;

public class ParenAST extends ExprAST {
    public final ExprAST child;

    public ParenAST(Token left, ExprAST child, Token right) {
        super(left.range().start(), right.range().start());
        this.child = child;
    }

    public ExprAST child() {
        return child;
    }

    @Override
    public ExprSemResult semanticAnalysis(SemanticContext context, AnalysisFlags flags) {
        return child.semanticAnalysis(context, flags);
    }
}

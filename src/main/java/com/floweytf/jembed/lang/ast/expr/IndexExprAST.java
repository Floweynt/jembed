package com.floweytf.jembed.lang.ast.expr;

import com.floweytf.jembed.lang.ast.ASTBase;
import com.floweytf.jembed.lang.lexer.Token;
import com.floweytf.jembed.lang.sema.AnalysisFlags;
import com.floweytf.jembed.lang.sema.ctx.SemanticContext;
import com.floweytf.jembed.lang.sema.results.ExprSemResult;

import java.util.function.Consumer;

public class IndexExprAST extends ExprAST {
    private final ExprAST array;
    private final ExprAST index;

    public IndexExprAST(ExprAST array, ExprAST index, Token closing) {
        super(array.start(), closing.range().end());
        this.array = array;
        this.index = index;
    }

    public ExprAST array() {
        return array;
    }

    public ExprAST index() {
        return index;
    }

    @Override
    public void visit(Consumer<ASTBase> visitor) {
        visitor.accept(array);
        visitor.accept(index);

    }

    @Override
    public ExprSemResult semanticAnalysis(SemanticContext context, AnalysisFlags flags) {
        throw new UnsupportedOperationException("no impl");
    }
}

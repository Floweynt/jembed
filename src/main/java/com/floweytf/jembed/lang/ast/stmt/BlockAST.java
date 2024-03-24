package com.floweytf.jembed.lang.ast.stmt;

import com.floweytf.jembed.lang.ast.ASTBase;
import com.floweytf.jembed.lang.ast.SemanticAST;
import com.floweytf.jembed.lang.lexer.Token;
import com.floweytf.jembed.lang.sema.AnalysisFlags;
import com.floweytf.jembed.lang.sema.ctx.SemanticContext;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class BlockAST extends SemanticAST<Void> {
    private final List<SemanticAST<?>> statements;
    private final boolean shouldIntroduceFrame;

    public BlockAST(Token open, List<SemanticAST<?>> statements, Token close, boolean shouldIntroduceFrame) {
        super(open.range().start(), close.range().end());
        this.statements = Collections.unmodifiableList(statements);
        this.shouldIntroduceFrame = shouldIntroduceFrame;
    }

    public BlockAST(Token open, List<SemanticAST<?>> statements, Token close) {
        this(open, statements, close, true);
    }

    public List<SemanticAST<?>> statements() {
        return statements;
    }

    public boolean shouldIntroduceFrame() {
        return shouldIntroduceFrame;
    }

    @Override
    public void visit(Consumer<ASTBase> visitor) {
        statements.forEach(visitor);
    }

    @Override
    public Void semanticAnalysis(SemanticContext context, AnalysisFlags flags) {
        if (shouldIntroduceFrame) {
            context.symbolFrameManager().defineFrame();
        }

        statements.forEach(context::runSemantic);

        if (shouldIntroduceFrame) {
            context.symbolFrameManager().exitFrame();
        }

        return null;
    }
}

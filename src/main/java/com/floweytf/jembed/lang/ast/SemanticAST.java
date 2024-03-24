package com.floweytf.jembed.lang.ast;

import com.floweytf.jembed.lang.sema.AnalysisFlags;
import com.floweytf.jembed.lang.sema.ctx.SemanticContext;
import com.floweytf.jembed.lang.source.CodeLocation;
import com.floweytf.jembed.lang.source.CodeRange;

/**
 * An AST node that can be reasonably semantically analyzed
 *
 * @param <T> The result of semantic analysis
 */
public abstract class SemanticAST<T> extends ASTBase {
    public SemanticAST(CodeRange range) {
        super(range);
    }

    public SemanticAST(CodeLocation left, CodeLocation right) {
        super(left, right);
    }

    public abstract T semanticAnalysis(SemanticContext context, AnalysisFlags flags);
}

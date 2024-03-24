package com.floweytf.jembed.lang.ast.expr.literal;

import com.floweytf.jembed.lang.ast.expr.ExprAST;
import com.floweytf.jembed.lang.sema.AnalysisFlags;
import com.floweytf.jembed.lang.sema.ctx.SemanticContext;
import com.floweytf.jembed.lang.sema.results.ExprSemResult;
import com.floweytf.jembed.lang.source.CodeRange;

/**
 * A base class that represents any constant literal within the source.
 * Examples are {@code 123}, {@code 134.5}, {@code "str"}, and {@code "str${with_interpolation}"}.
 * <p>
 * String template literals technically do not extend this class, see {@link StringTemplateLiteralAST}, although
 * individual components will (see {@link StringLiteralAST}).
 *
 * @param <T> The type of the literal. This may be {@link Long}, {@link Double}, or {@link String}.
 */
public class LiteralAST<T> extends ExprAST {
    private final T value;

    /**
     * @param range The code range of the literal
     * @param value The value
     */
    public LiteralAST(CodeRange range, T value) {
        super(range);
        this.value = value;
    }

    /**
     * @return The value of the literal
     */
    public T value() {
        return value;
    }

    @Override
    public ExprSemResult semanticAnalysis(SemanticContext context, AnalysisFlags flags) {
        return new ExprSemResult(context.typeManager().typeByClass(value.getClass()), ExprSemResult.ValueCategory.RVALUE, range());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[value=" + value + "]";
    }
}

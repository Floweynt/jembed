package com.floweytf.jembed.lang.ast.expr.literal;

import com.floweytf.jembed.lang.ast.ASTBase;
import com.floweytf.jembed.lang.ast.expr.ExprAST;
import com.floweytf.jembed.lang.lexer.Token;
import com.floweytf.jembed.lang.sema.AnalysisFlags;
import com.floweytf.jembed.lang.sema.ctx.SemanticContext;
import com.floweytf.jembed.lang.sema.results.ExprSemResult;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * An AST node that represents any string literal (regardless of whether it has templating)
 */
public class StringTemplateLiteralAST extends ExprAST {
    private final List<ExprAST> components;
    private final boolean hasTemplate;

    /**
     * @param startQt    The starting quote token in a string that defines the code range of this literal
     * @param components The various components that make up this string.
     *                   Regular character sequences will generate a {@link StringLiteralAST}, while template
     *                   expressions will be any other {@link ExprAST}.
     * @param endQt      The ending quote token in a string that defines the code range of this literal
     */
    public StringTemplateLiteralAST(Token startQt, List<ExprAST> components, Token endQt) {
        super(startQt.range().start(), endQt.range().end());
        this.components = Collections.unmodifiableList(components);
        this.hasTemplate = components.stream().anyMatch(x -> !(x instanceof StringLiteralAST));
    }

    /**
     * @return The various components that make up this string.
     * Regular character sequences will generate a {@link StringLiteralAST}, while template expressions will be any other {@link ExprAST}.
     * This list is immutable.
     */
    public List<ExprAST> components() {
        return components;
    }

    /**
     * @return Whether this literal contains any string templating.
     */
    public boolean hasTemplate() {
        return hasTemplate;
    }

    @Override
    public void visit(Consumer<ASTBase> visitor) {
        components.forEach(visitor);
    }

    @Override
    public ExprSemResult semanticAnalysis(SemanticContext context, AnalysisFlags flags) {
        components.forEach(context::runSemantic);
        return new ExprSemResult(context.typeManager().builtinString(), ExprSemResult.ValueCategory.RVALUE, range());
    }
}
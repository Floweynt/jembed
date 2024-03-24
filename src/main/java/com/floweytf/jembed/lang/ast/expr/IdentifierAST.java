package com.floweytf.jembed.lang.ast.expr;

import com.floweytf.jembed.lang.lexer.Token;
import com.floweytf.jembed.lang.sema.AnalysisFlags;
import com.floweytf.jembed.lang.sema.OverloadedMethod;
import com.floweytf.jembed.lang.sema.ctx.SemanticContext;
import com.floweytf.jembed.lang.sema.results.ExprSemResult;
import com.floweytf.jembed.lang.sema.sym.SymbolType;
import com.floweytf.jembed.lang.sema.sym.VariableSymbolInfo;
import com.floweytf.jembed.lang.source.diagnostic.Severity;
import com.floweytf.jembed.util.StringPool;

/**
 * An AST node that represents a variable reference via an identifier.
 */
public class IdentifierAST extends ExprAST {
    private final StringPool.StrRef name;

    /**
     * @param token The identifier token to construct this AST from
     */
    public IdentifierAST(Token token) {
        super(token.range());
        this.name = token.identifier();
    }

    public StringPool.StrRef name() {
        return name;
    }

    @Override
    public ExprSemResult semanticAnalysis(SemanticContext context, AnalysisFlags flags) {
        var res = context.symbolFrameManager().lookupSymbol(SymbolType.VALUE_OR_OVERLOAD, name);

        if (res == null) {
            context.tu().report(Severity.ERROR, "unknown symbol", range());
            // we should pretend to be lvalue in order to ensure that future assignment won't generate more errors
            return new ExprSemResult(null, ExprSemResult.ValueCategory.LVALUE, range());
        }

        if (res instanceof OverloadedMethod && !flags.allowOverload()) {
            context.tu().report()
                .body(Severity.ERROR, "function name reference cannot be used in this context", range())
                .hint("function names can only be used:")
                .hint("- as a function call parameter, like `thread.run(Foo.method)`")
                .hint("- as an initializer for a variable, like `var x: () -> void = Foo.method`")
                .hint("- as a cast expression, like `Foo.method as () -> void`")
                .done();
        }

        if (res instanceof OverloadedMethod overloadedMethod) {
            return new ExprSemResult(overloadedMethod, ExprSemResult.ValueCategory.RVALUE, range());
        } else if (res instanceof VariableSymbolInfo varInfo) {
            return new ExprSemResult(varInfo.type(), varInfo.category(), range());
        }

        throw new IllegalStateException();
    }

    @Override
    public String toString() {
        return "IdentifierAST[" + name + "]";
    }
}
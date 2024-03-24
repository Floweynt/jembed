package com.floweytf.jembed.lang.ast.expr;

import com.floweytf.jembed.lang.ast.ASTBase;
import com.floweytf.jembed.lang.lexer.Token;
import com.floweytf.jembed.lang.sema.AnalysisFlags;
import com.floweytf.jembed.lang.sema.OverloadedMethod;
import com.floweytf.jembed.lang.sema.Type;
import com.floweytf.jembed.lang.sema.ctx.SemanticContext;
import com.floweytf.jembed.lang.sema.results.ExprSemResult;
import com.floweytf.jembed.lang.source.TranslationUnit;
import com.floweytf.jembed.lang.source.diagnostic.Severity;
import com.floweytf.jembed.util.Utils;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class InvokeExprAST extends ExprAST {
    private final ExprAST invokee;
    private final List<ExprAST> args;

    public InvokeExprAST(ExprAST invokee, List<ExprAST> args, Token closing) {
        super(invokee.start(), closing.range().end());
        this.args = args;
        this.invokee = invokee;
    }

    public ExprAST invokee() {
        return invokee;
    }

    public List<ExprAST> args() {
        return args;
    }

    @Override
    public void visit(Consumer<ASTBase> visitor) {
        visitor.accept(invokee);
        args.forEach(visitor);
    }

    @Override
    public ExprSemResult semanticAnalysis(SemanticContext context, AnalysisFlags flags) {
        var invokeeRes = context.runSemantic(invokee, new AnalysisFlags(true));
        final var errorResult = new ExprSemResult(null, ExprSemResult.ValueCategory.RVALUE, range());

        var result = args.stream().map(context::runSemantic).toList();

        // if we fail figuring out type, give up
        if (result.stream().anyMatch(argRes -> argRes.type() == null) || invokeeRes == null)
            return errorResult;

        if (invokeeRes.type() instanceof OverloadedMethod overload) {
            var argTypeList = result.stream().map(ExprSemResult::type).toList();

            var selectedOverload = overload.getOverload(argTypeList, context);

            if (selectedOverload == null) {
                final var diagnostic = context.tu().report()
                    .body(Severity.ERROR, "failed to resolve overload", range())
                    .hint("invoking method with arguments (" + argTypeList.stream().map(Type::name).collect(Collectors.joining(", ")) + ")");

                overload.iterateSimilar(argTypeList, 1, (similarOverload) -> {
                    final var child = diagnostic.child()
                        .body(Severity.NOTE, "overload defined here", similarOverload.definitionRange())
                        .source(Utils.mapNull(similarOverload.tu(), TranslationUnit::source));

                    if (similarOverload.args().size() != args.size()) {
                        child.hint("overload takes " + similarOverload.args().size() + ", but you supplied " + args.size());
                    }

                    for (int i = 0; i < Math.min(argTypeList.size(), similarOverload.args().size()); i++) {
                        if (!context.typeManager().canCoerce(argTypeList.get(i), similarOverload.args().get(i))) {
                            child.hint("argument mismatch at parameter #" + (i + 1) + ": cannot coerce `" + argTypeList.get(i).name() + "` to `" + similarOverload.args().get(i).name());
                        }
                    }

                    child.done();
                }, context);

                diagnostic.done();
                return errorResult;
            }

            return new ExprSemResult(selectedOverload.returnType(), ExprSemResult.ValueCategory.RVALUE, range());
        } else {
            context.tu().report()
                .body(Severity.ERROR, "cannot invoke non-invocable type", range())
                .hint("you may only invoke methods or method references, such as `(int, int) -> int`")
                .child(Severity.NOTE, "invokee has type `" + invokeeRes.type().name() + "`", invokee.range());
        }

        return errorResult;
    }
}

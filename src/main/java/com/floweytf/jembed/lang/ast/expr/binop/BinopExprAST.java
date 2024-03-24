package com.floweytf.jembed.lang.ast.expr.binop;

import com.floweytf.jembed.lang.ast.ASTBase;
import com.floweytf.jembed.lang.ast.expr.ExprAST;
import com.floweytf.jembed.lang.sema.AnalysisFlags;
import com.floweytf.jembed.lang.sema.Type;
import com.floweytf.jembed.lang.sema.ctx.SemanticContext;
import com.floweytf.jembed.lang.sema.results.ExprSemResult;
import com.floweytf.jembed.lang.source.CodeRange;
import com.floweytf.jembed.lang.source.TranslationUnit;
import com.floweytf.jembed.lang.source.diagnostic.Severity;
import com.floweytf.jembed.util.Utils;

import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a binary operator (like x + b).
 * The operator is represented with {@link BinopExprType}.
 */
public class BinopExprAST extends ExprAST {
    private final ExprAST left;
    private final ExprAST right;
    private final CodeRange operatorRange;
    private final BinopExprType type;

    /**
     * @param left          The expression on the left-hand side
     * @param operatorRange The code range the operator spans, used for error reporting
     * @param right         The expression on the right-hand side
     * @param type          The type of the operator, such as {@link BinopExprType#ADD} for '+'
     */
    public BinopExprAST(ExprAST left, CodeRange operatorRange, ExprAST right, BinopExprType type) {
        super(left.start(), right.end());
        this.left = left;
        this.operatorRange = operatorRange;
        this.right = right;
        this.type = type;
    }

    /**
     * @return The left-hand side expression
     */
    public ExprAST left() {
        return left;
    }

    /**
     * @return The code range the operator spans, used for error reporting
     */
    public CodeRange operatorRange() {
        return operatorRange;
    }

    /**
     * @return The right-hand side expression
     */
    public ExprAST right() {
        return right;
    }

    /**
     * @return The type of the operator, such as {@link BinopExprType#ADD} for '+'
     */
    public BinopExprType type() {
        return type;
    }

    @Override
    public void visit(Consumer<ASTBase> visitor) {
        visitor.accept(left);
        visitor.accept(right);
    }

    /**
     * Computes the resulting type for a particular operator
     *
     * @param context The semantic analysis context
     * @param lhs     The type of the left hand side expression
     * @param type    The type of the binary operator. This may differ from {@link BinopExprAST#type}, since this method may be called for handling compound assignment operators
     * @param rhs     The type of the right hand side expression
     * @return The resulting type, or null if it cannot be computed
     */
    private Type doTypeCalc(SemanticContext context, Type lhs, BinopExprType type, Type rhs) {
        final var opMan = context.operatorManager();
        final var res = opMan.getBinaryOpOverload(lhs, type, rhs);
        if (res == null) {
            var diagnostic = context.tu().report()
                .body(Severity.ERROR, "unknown overload for operator", operatorRange)
                .child().body(Severity.NOTE, "left-hand side has type '" + lhs.name() + "'", left.range()).done()
                .child().body(Severity.NOTE, "right-hand side has type '" + rhs.name() + "'", right.range()).done();

            // lets look for related overloads...
            opMan.getBinaryOpResolver(type).iterateSimilar(List.of(lhs, rhs), 1, overload -> {
                var child = diagnostic
                    .child()
                    .body(
                        Severity.NOTE,
                        "possible overload: " + (overload.definitionRange() == null ? "operator defined intrinsically" : "operator defined here"),
                        overload.definitionRange()
                    )
                    .source(Utils.mapNull(overload.tu(), TranslationUnit::source));

                child.hint("the operator has signature `" + overload.toSignature() + "`");

                if (lhs != overload.args().get(0)) {
                    child.hint("left-hand side has type `" + lhs.name() + "`, which does not match `" + overload.args().get(0).name() + "`");
                }
                if (rhs != overload.args().get(1)) {
                    child.hint("right-hand side has type `" + rhs.name() + "`, which does not match `" + overload.args().get(1).name() + "`");
                }
                child.done();

            }, context);
            diagnostic.done();

            return null;
        }
        return res.returnType();
    }

    @Override
    public ExprSemResult semanticAnalysis(SemanticContext context, AnalysisFlags flags) {
        final var lhs = context.runSemantic(left);
        final var rhs = context.runSemantic(right);

        // we can still run value category checks even if lhs/rhs failed
        if (type.isAssign()) {
            // we expect lhs to be mutable lvalues
            switch (lhs.category()) {
            case LVALUE -> {
            }
            case RVALUE -> context.tu()
                .report()
                .body(Severity.ERROR, "expected variable on the left hand side of an assignment", lhs.valueCategoryReportRange())
                .hint("an assignment should look like a `variable = 1 + 2 * foo` or `object.member = 1 * 3`")
                .hint("assignment to constants or expressions is not allowed, such as `1 = foo` or `1 + 2 = foo`")
                .child()
                .body(Severity.NOTE, "did you mean to type '=='?", operatorRange)
                .done()
                .done();
            case CONST_LVALUE -> context.tu().report()
                .body(Severity.ERROR, "expected mutable variable on the left hand side of an assignment", lhs.valueCategoryReportRange())
                .hint("you cannot assign to a `const` variable")
                .done();
            }
        }

        if (lhs.type() == null || rhs.type() == null) {
            return new ExprSemResult(null, ExprSemResult.ValueCategory.RVALUE, range());
        }

        // okay, assignment operators should be handled differently, since they are just sugar
        if (type.isAssign()) {
            // assignment always returns T(lhs), so additional logic only needs to be done in 'fused' operators
            if (type.underlyingOp() != null) {
                final var resType = doTypeCalc(context, lhs.type(), type.underlyingOp(), rhs.type());

                if (resType != null && context.typeManager().canCoerce(resType, lhs.type())) {
                    context.tu().report()
                        .body(Severity.ERROR, "cannot utilize compound assignment operator because the result of the operation cannot be assigned to the left hand side", operatorRange)
                        .hint(String.format("'%s' (operator result type) cannot be converted to '%s' (assignee type)", resType.name(), lhs.type().name()))
                        .child().body(Severity.NOTE, "left-hand side has type '" + lhs.type().name() + "'", left.range()).done()
                        .child().body(Severity.NOTE, "right-hand side has type '" + rhs.type().name() + "'", right.range()).done()
                        .done();
                }
            }

            return new ExprSemResult(
                lhs.type(),
                ExprSemResult.ValueCategory.RVALUE,
                range()
            );
        }

        return new ExprSemResult(
            doTypeCalc(context, lhs.type(), type, rhs.type()),
            ExprSemResult.ValueCategory.RVALUE,
            range()
        );
    }

    @Override
    public String toString() {
        return "BinopExprAST" + "[type = " + type.name() + ", op = @(" + operatorRange() + ")]";
    }
}
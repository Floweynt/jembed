package com.floweytf.jembed.lang.ast.expr.unary;

import com.floweytf.jembed.lang.ast.ASTBase;
import com.floweytf.jembed.lang.ast.expr.ExprAST;
import com.floweytf.jembed.lang.sema.AnalysisFlags;
import com.floweytf.jembed.lang.sema.ctx.SemanticContext;
import com.floweytf.jembed.lang.sema.results.ExprSemResult;
import com.floweytf.jembed.lang.source.CodeRange;
import com.floweytf.jembed.lang.source.diagnostic.Severity;

import java.util.function.Consumer;

/**
 * Represents a pre or postfix unary operator, such as {@code x++} or @{@code x--}.
 * The operator is represented with {@link UnaryExprType}.
 */
public class UnaryExprAST extends ExprAST {
    private final ExprAST operand;
    private final CodeRange operatorRange;
    private final UnaryExprType type;

    /**
     * Creates a postfix unary operator.
     *
     * @param operatorRange The code range for the operator, used for error reporting
     * @param operand       The operand that this unary operator acts on
     * @param type          The type of the unary operator, such as {@link UnaryExprType#POST_INC} for {@code x++}
     */
    public UnaryExprAST(CodeRange operatorRange, ExprAST operand, UnaryExprType type) {
        super(operatorRange.start(), operand.end());
        this.operand = operand;
        this.operatorRange = operatorRange;
        this.type = type;
    }

    /**
     * Creates a prefix unary operator.
     *
     * @param operand       The operand that this unary operator acts on
     * @param operatorRange The code range for the operator, used for error reporting
     * @param type          The type of the unary operator, such as {@link UnaryExprType#POST_INC} for {@code x++}
     */
    public UnaryExprAST(ExprAST operand, CodeRange operatorRange, UnaryExprType type) {
        super(operand.start(), operatorRange.end());
        this.operand = operand;
        this.operatorRange = operatorRange;
        this.type = type;
    }

    /**
     * @return The code range of the operator.
     */
    public CodeRange operatorRange() {
        return operatorRange;
    }

    /**
     * @return The operand that this unary operator acts on.
     */
    public ExprAST operand() {
        return operand;
    }

    /**
     * @return The type of operator this is, such as {@link UnaryExprType#POST_INC} for {@code x++}.
     */
    public UnaryExprType type() {
        return type;
    }

    @Override
    public void visit(Consumer<ASTBase> visitor) {
        visitor.accept(operand);
    }

    @Override
    public ExprSemResult semanticAnalysis(SemanticContext context, AnalysisFlags flags) {
        final var result = context.runSemantic(operand);

        if (type.needsLvalue()) {
            switch (result.category()) {
            case LVALUE -> {
            }
            case RVALUE -> context.tu()
                .report()
                .body(Severity.ERROR, "expected variable as the operand of a increment/decrement", result.valueCategoryReportRange())
                .hint("increment/decrement should look like a `variable++` or `object.member--`")
                .hint("mutating constants or expressions is not allowed, such as `1++` or `(1 + 2)++`")
                .done();
            case CONST_LVALUE -> context.tu().report()
                .body(Severity.ERROR, "expected mutable variable as the operand of an increment/decrement", result.valueCategoryReportRange())
                .hint("you cannot increment or decrement a `const` variable")
                .done();
            }
        }

        if (result.type() == null) {
            return new ExprSemResult(null, ExprSemResult.ValueCategory.RVALUE, range());
        }

        final var opRes = context.operatorManager().unaryOpResult(result.type(), type);

        if (opRes == null) {
            context.tu().report()
                .body(Severity.ERROR, "unknown overload for operator", operatorRange)
                .child().body(Severity.NOTE, "operand '" + result.type().name() + "'", operand.range()).done()
                .done();
        }

        return new ExprSemResult(context.operatorManager().unaryOpResult(result.type(), type), ExprSemResult.ValueCategory.RVALUE, range());
    }

    @Override
    public String toString() {
        return "UnaryExprAST[type = " + type.name() + ", op = " + operatorRange + "]";
    }
}
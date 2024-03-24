package com.floweytf.jembed.lang.ast.expr.unary;

/**
 * An enumerator representing the different unary operators that a {@link UnaryExprAST} may be.
 */
public enum UnaryExprType {
    // true true
    POST_INC(true, true),
    POST_DEC(true, true),
    INC(true, false),
    DEC(true, false),

    PLUS,
    NEG,

    BIT_NOT,
    LOGICAL_NOT;

    private final boolean isPost;
    private final boolean needsLvalue;

    /**
     * @param needsLvalue Whether this unary operator requires a mutable value. This only applies for {@code ++} and {@code --}
     * @param isPost      Whether this unary operator is postfix (goes after expression)
     */
    UnaryExprType(boolean needsLvalue, boolean isPost) {
        this.needsLvalue = needsLvalue;
        this.isPost = isPost;
    }

    /**
     * Constructs a unary expression that neither needs an lvalue nor is postfix.
     */
    UnaryExprType() {
        this(false, false);
    }

    /**
     * @return Whether this operator is postfix.
     */
    public boolean isPost() {
        return isPost;
    }

    /**
     * @return Whether this operator requires a mutable value as its operand.
     */
    public boolean needsLvalue() {
        return needsLvalue;
    }
}

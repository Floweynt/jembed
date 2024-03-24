package com.floweytf.jembed.lang.ast.expr.binop;

import org.jetbrains.annotations.Nullable;

/**
 * An enumerator representing the different binary operators that a {@link BinopExprAST} may be.
 */
public enum BinopExprType {
    MUL,
    DIV,
    MOD,
    ADD,
    SUB,
    SHR,
    USHR,
    SHL,
    GT,
    GE,
    LT,
    LE,
    EQ,
    NEQ,
    BIT_AND,
    BIT_XOR,
    BIT_OR,
    LOGICAL_AND,
    LOGICAL_OR,

    ASSIGN(null, true),
    ADD_ASSIGN(ADD),
    SUB_ASSIGN(SUB),
    MUL_ASSIGN(MUL),
    DIV_ASSIGN(DIV),
    MOD_ASSIGN(MOD),
    AND_ASSIGN(BIT_AND),
    XOR_ASSIGN(BIT_XOR),
    OR_ASSIGN(BIT_OR),
    SHR_ASSIGN(SHR),
    USHR_ASSIGN(USHR),
    SHL_ASSIGN(SHL);

    private final boolean isAssign;
    @Nullable
    private final BinopExprType underlyingOp;

    /**
     * @param type     The underlying operator for compound assignment
     * @param isAssign Whether this is an assignment operator. Must be true if {@code type} is not null.
     */
    BinopExprType(@Nullable BinopExprType type, boolean isAssign) {
        underlyingOp = type;
        this.isAssign = isAssign;
    }

    /**
     * Constructs a binary operator that is not an assignment
     */
    BinopExprType() {
        this(null, false);
    }

    /**
     * Constructs a compound assignment operator.
     *
     * @param type The actual operator (such as {@code +} for {@code +=})
     */
    BinopExprType(BinopExprType type) {
        this(type, true);
    }

    /**
     * @return Whether this operator is an assignment.
     */
    public boolean isAssign() {
        return isAssign;
    }

    /**
     * @return The underlying binary operator. Null if this is not a compound assignment
     */
    public @Nullable BinopExprType underlyingOp() {
        return underlyingOp;
    }
}

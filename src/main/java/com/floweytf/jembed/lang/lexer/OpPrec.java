package com.floweytf.jembed.lang.lexer;

public enum OpPrec {
    MUL,
    ADD,
    SHIFT,
    RELATIONAL,
    EQUALITY,
    BIT_AND,
    BIT_XOR,
    BIT_OR,
    LOGICAL_AND,
    LOGICAL_OR,
    ASSIGNMENT(true);

    public final boolean isRtoL;

    OpPrec() {
        this(false);
    }

    OpPrec(boolean isRtoL) {
        this.isRtoL = isRtoL;
    }

    public OpPrec next() {
        if (ordinal() == 0)
            return null;
        return values()[ordinal() - 1];
    }
}

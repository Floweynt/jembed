package com.floweytf.jembed.lang.ast.expr.literal;

import com.floweytf.jembed.lang.lexer.Token;

/**
 * AST Node that represents a constant floating point literal.
 * This is interpreted as a {@link Double}.
 */
public class FloatLiteralAST extends LiteralAST<Double> {
    /**
     * @param token The floating point literal token
     */
    public FloatLiteralAST(Token token) {
        super(token.range(), token.floatLit());
    }
}

package com.floweytf.jembed.lang.ast.expr.literal;

import com.floweytf.jembed.lang.lexer.Token;

/**
 * AST Node that represents a constant integer literal.
 * This is interpreted as a {@link Long}.
 */
public class IntLiteralAST extends LiteralAST<Long> {
    /**
     * @param token The integer literal token
     */
    public IntLiteralAST(Token token) {
        super(token.range(), token.intLit());
    }
}
package com.floweytf.jembed.lang.ast.expr.literal;

import com.floweytf.jembed.lang.lexer.Token;

/**
 * AST Node that represents a fragment of a string literal in a {@link StringTemplateLiteralAST}.
 */
public class StringLiteralAST extends LiteralAST<String> {
    /**
     * @param token The string literal token. This must be generated from the {@link com.floweytf.jembed.lang.lexer.Lexer.Mode#STRING} lexer mode.
     */
    public StringLiteralAST(Token token) {
        super(token.range(), token.stringLit());
    }
}

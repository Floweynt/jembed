package com.floweytf.jembed.lang.lexer;

import com.floweytf.jembed.lang.source.CodeRange;

public class LexerError extends RuntimeException {
    private final CodeRange range;

    public LexerError(CodeRange range) {
        super();
        this.range = range;
    }

    public CodeRange range() {
        return range;
    }
}

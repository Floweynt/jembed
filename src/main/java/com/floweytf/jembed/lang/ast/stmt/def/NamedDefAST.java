package com.floweytf.jembed.lang.ast.stmt.def;

import com.floweytf.jembed.lang.ast.SemanticAST;
import com.floweytf.jembed.lang.lexer.Token;
import com.floweytf.jembed.lang.source.CodeLocation;
import com.floweytf.jembed.lang.source.CodeRange;
import com.floweytf.jembed.util.StringPool;

public abstract class NamedDefAST<T> extends SemanticAST<T> {
    private final Token name;

    public NamedDefAST(CodeRange range, Token name) {
        super(range);
        this.name = name;
    }

    public NamedDefAST(CodeLocation left, CodeLocation right, Token name) {
        super(left, right);
        this.name = name;
    }

    public StringPool.StrRef name() {
        return name.identifier();
    }

    public CodeRange nameRange() {
        return name.range();
    }

    @Override
    public String toString() {
        return "NamedDefAST[" + name() + "]";
    }
}

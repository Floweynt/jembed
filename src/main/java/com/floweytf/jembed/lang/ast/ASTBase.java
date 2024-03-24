package com.floweytf.jembed.lang.ast;

import com.floweytf.jembed.lang.source.CodeLocation;
import com.floweytf.jembed.lang.source.CodeRange;

import java.util.function.Consumer;

public abstract class ASTBase {
    private final CodeRange range;

    public ASTBase(CodeRange range) {
        this.range = range;
    }

    public ASTBase(CodeLocation left, CodeLocation right) {
        this(new CodeRange(left, right));
    }

    public final CodeRange range() {
        return range;
    }

    public final CodeLocation start() {
        return range.start();
    }

    public final CodeLocation end() {
        return range.end();
    }

    private void doSerialize(StringBuilder builder, int tab) {
        builder.append(" ".repeat(tab)).append(this).append("@(").append(range).append(")").append("\n");
        visit((child) -> child.doSerialize(builder, tab + 2));
    }

    // things to extend
    public final String serialize() {
        StringBuilder builder = new StringBuilder();
        doSerialize(builder, 0);
        return builder.toString();
    }

    public void visit(Consumer<ASTBase> visitor) {

    }

    // misc
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}

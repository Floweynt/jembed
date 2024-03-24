package com.floweytf.jembed.lang.source;

public record CodeRange(CodeLocation start, CodeLocation end) {
    public static final CodeRange ZERO = new CodeRange(CodeLocation.ZERO, CodeLocation.ZERO);

    public CodeRange(int startLine, int startCol, int startIdx, int endLine, int endCol, int endIdx) {
        this(
            new CodeLocation(startLine, startCol, startIdx),
            new CodeLocation(endLine, endCol, endIdx)
        );
    }

    @Override
    public String toString() {
        return start.toString() + "-" + end.toString();
    }
}

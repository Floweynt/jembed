package com.floweytf.jembed.lang.source;

public record CodeLocation(int line, int col, int idx) {
    public static final CodeLocation ZERO = new CodeLocation(0, 0, 0);

    @Override
    public String toString() {
        return line + ":" + col;
    }

    public CodeLocation next(byte ch) {
        var col = this.col;
        var line = this.line;

        if (ch == '\n') {
            col = 1;
            line++;
        } else {
            col++;
        }

        return new CodeLocation(line, col, idx + 1);
    }
}

package com.floweytf.jembed.lang.source.diagnostic;

import com.floweytf.jembed.lang.source.CodeRange;
import com.floweytf.jembed.lang.source.Source;
import com.floweytf.jembed.util.Ansi;

public class DiagnosticPrinter {
    private static final Ansi NUM_FORMAT = Ansi.GREEN;
    private static final Ansi FILE_FORMAT = Ansi.ITALIC;
    private static final String SEP = Ansi.CYAN.colorize("|");
    private static final String HINT = Severity.NOTE.formatting().colorize("hint");
    private final StringBuilder builder = new StringBuilder();
    private int indent;

    private void println(String str) {
        builder.append("  ".repeat(indent)).append(str).append("\n");
    }

    private void printf(String str, Object... args) {
        builder.append("  ".repeat(indent)).append(String.format(str, args)).append("\n");
    }

    private String formatNum(int num) {
        return NUM_FORMAT.colorize(Integer.toString(num));
    }

    private String formatRange(CodeRange range) {
        return String.format(
            "%s:%s - %s:%s",
            formatNum(range.start().line()), formatNum(range.start().col()),
            formatNum(range.end().line()), formatNum(range.end().col())
        );
    }

    private void printSource(Source src, CodeRange range, int startContext, int endContext, Ansi format) {
        final var startRange = src.locationAt(Math.max(1, range.start().line() - startContext), 1);
        final var endRange = src.locationAt(Math.min(src.lineCount(), range.end().line() + endContext), -1);

        final var startPart = src.textAt(startRange, range.start());
        final var coloredPart = src.textAt(range);
        final var endPart = src.textAt(range.end(), endRange);
        final var combinedText = startPart + format.colorize(coloredPart) + endPart;

        var lineNo = startRange.line();
        var comp = combinedText.split("\n", -1);

        // two passes for alignment
        int maxLen = 0;
        for (int i = 0; i < comp.length; i++) {
            maxLen = Math.max(maxLen, String.valueOf(lineNo + i).length());
        }

        for (var line : comp) {
            var padding = maxLen - String.valueOf(lineNo).length();
            if (lineNo > range.start().line() && lineNo <= range.end().line())
                line = format.colorize(line);
            printf("%s %s %s", formatNum(lineNo++) + " ".repeat(padding), SEP, line);
        }
    }

    public DiagnosticPrinter print(Diagnostic d) {
        println(Ansi.BOLD.colorize("==== Diagnostic ===="));
        doPrint(d);
        if (indent != 0)
            throw new IllegalStateException();
        return this;
    }

    private void doPrint(Diagnostic d) {
        var msgParts = d.message().split("\n");
        printf("%s: %s", d.severity().formattedName(), msgParts[0]);
        for (int i = 1; i < msgParts.length; i++) {
            printf(msgParts[i]);
        }

        for (var note : d.notes()) {
            printf("%s: %s", HINT, note);
        }

        if (d.source() != null) {
            printf("%s %s @ (%s)", Ansi.BOLD.colorize("[src]"), FILE_FORMAT.colorize(d.source().fileName()), formatRange(d.range()));
            printSource(
                d.source(), d.range(), 1, 1,
                d.severity().formatting().and(Ansi.UNDERLINE)
            );
        }

        printf("");
        d.children().forEach(this::doPrint);
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}

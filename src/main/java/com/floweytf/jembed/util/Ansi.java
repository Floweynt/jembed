package com.floweytf.jembed.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Ansi {
    public static final Ansi BOLD = new Ansi("\u001B[1m");
    public static final Ansi ITALIC = new Ansi("\u001B[3m");
    public static final Ansi UNDERLINE = new Ansi("\u001B[4m");
    public static final Ansi BLACK = new Ansi("\u001B[30m");
    public static final Ansi RED = new Ansi("\u001B[31m");
    public static final Ansi GREEN = new Ansi("\u001B[32m");
    public static final Ansi YELLOW = new Ansi("\u001B[33m");
    public static final Ansi BLUE = new Ansi("\u001B[34m");
    public static final Ansi MAGENTA = new Ansi("\u001B[35m");
    public static final Ansi CYAN = new Ansi("\u001B[36m");
    public static final Ansi WHITE = new Ansi("\u001B[37m");
    public static final Ansi BG_BLACK = new Ansi("\u001B[40m");
    public static final Ansi BG_RED = new Ansi("\u001B[41m");
    public static final Ansi BG_GREEN = new Ansi("\u001B[42m");
    public static final Ansi BG_YELLOW = new Ansi("\u001B[43m");
    public static final Ansi BG_BLUE = new Ansi("\u001B[44m");
    public static final Ansi BG_MAGENTA = new Ansi("\u001B[45m");
    public static final Ansi BG_CYAN = new Ansi("\u001B[46m");
    public static final Ansi BG_WHITE = new Ansi("\u001B[47m");
    private static final String RESET = "\u001B[0m";
    private static final String REVERSE_VIDEO = "\u001B[7m";
    private static final String INVISIBLE_TEXT = "\u001B[8m";
    private final String[] codes;
    private final String codeSerialized;

    public Ansi(String... codes) {
        this.codes = codes;
        StringBuilder res = new StringBuilder();
        for (String code : codes) {
            res.append(code);
        }
        codeSerialized = res.toString();
    }

    public static Ansi of(Ansi first, Ansi... args) {
        for (var arg : args) {
            first = first.and(arg);
        }

        return first;
    }

    public Ansi and(Ansi other) {
        List<String> both = new ArrayList<>();
        Collections.addAll(both, codes);
        Collections.addAll(both, other.codes);
        return new Ansi(both.toArray(new String[]{}));
    }

    public String colorize(String original) {
        return codeSerialized + original + RESET;
    }

    public String format(String template, Object... args) {
        return colorize(String.format(template, args));
    }
}
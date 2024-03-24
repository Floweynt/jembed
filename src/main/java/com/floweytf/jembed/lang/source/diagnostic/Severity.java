package com.floweytf.jembed.lang.source.diagnostic;

import com.floweytf.jembed.util.Ansi;

public enum Severity {
    ERROR(Ansi.of(Ansi.RED, Ansi.BOLD)),
    WARNING(Ansi.of(Ansi.YELLOW, Ansi.BOLD)),
    NOTE(Ansi.BLUE);

    private final String formattedName;
    private final String pluralName;
    private final Ansi formatting;

    Severity(Ansi formatting) {
        this.formatting = formatting;
        this.formattedName = formatting.colorize(name().toLowerCase());
        this.pluralName = formatting.colorize(name().toLowerCase() + "s");
    }

    public String formattedName() {
        return formattedName;
    }

    public String pluralName() {
        return pluralName;
    }

    public Ansi formatting() {
        return formatting;
    }

    public String grammaticallyCorrectName(int count) {
        return count == 1 ? formattedName : pluralName;
    }
}

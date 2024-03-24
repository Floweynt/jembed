package com.floweytf.jembed.lang.source.diagnostic;

import com.floweytf.jembed.lang.source.CodeRange;
import com.floweytf.jembed.lang.source.Source;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Diagnostic {
    private final Severity severity;
    private final String message;
    @Nullable
    private final CodeRange range;
    private final List<Diagnostic> children;
    private final List<String> notes;
    private final Source source;

    public Diagnostic(Source source, Severity severity, String message, @Nullable CodeRange range, List<String> notes) {
        this(source, severity, message, range, notes, new ArrayList<>());
    }

    public Diagnostic(Source source, Severity severity, String message, @Nullable CodeRange range) {
        this(source, severity, message, range, List.of(), new ArrayList<>());
    }

    public Diagnostic(Source source, Severity severity, String message, @Nullable CodeRange range, List<String> notes, List<Diagnostic> children) {
        this.severity = severity;
        this.message = message;
        this.range = range;
        this.source = source;
        this.notes = notes;
        this.children = children;
    }

    public Diagnostic child(Diagnostic diagnostic) {
        children.add(diagnostic);
        return this;
    }

    public Severity severity() {
        return severity;
    }

    public String message() {
        return message;
    }

    public CodeRange range() {
        return range;
    }

    public Source source() {
        return source;
    }

    public List<Diagnostic> children() {
        return Collections.unmodifiableList(children);
    }

    public List<String> notes() {
        return notes;
    }
}
package com.floweytf.jembed.lang.source.diagnostic;

import com.floweytf.jembed.lang.source.CodeRange;
import com.floweytf.jembed.lang.source.Source;

import java.util.ArrayList;
import java.util.List;

public class DiagnosticBuilder<T extends IDiagnosticSink> implements IDiagnosticSink {
    private final T link;
    private final List<Diagnostic> children = new ArrayList<>();
    private final List<String> notes = new ArrayList<>();
    private Severity severity;
    private String message;
    private CodeRange range;
    private Source source;

    public DiagnosticBuilder(T link) {
        this.link = link;
    }

    public DiagnosticBuilder<T> body(Severity severity, String message, CodeRange range) {
        this.severity = severity;
        this.message = message;
        this.range = range;
        return this;
    }

    public DiagnosticBuilder<T> source(Source source) {
        this.source = source;
        return this;
    }

    public DiagnosticBuilder<DiagnosticBuilder<T>> child() {
        return new DiagnosticBuilder<>(this)
            .source(source);
    }

    public DiagnosticBuilder<T> hint(String str) {
        notes.add(str);
        return this;
    }

    public DiagnosticBuilder<T> child(Severity severity, String message, CodeRange range) {
        children.add(new Diagnostic(source, severity, message, range));
        return this;
    }

    public T done() {
        if (severity == null)
            throw new IllegalStateException("done called without body");

        link.addDiagnostic(new Diagnostic(source, severity, message, range, notes, children));
        return link;
    }

    @Override
    public void addDiagnostic(Diagnostic diagnostic) {
        children.add(diagnostic);
    }
}

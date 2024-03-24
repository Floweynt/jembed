package com.floweytf.jembed.lang.source;

import com.floweytf.jembed.lang.CompilerFrontend;
import com.floweytf.jembed.lang.ast.stmt.def.NamedDefAST;
import com.floweytf.jembed.lang.parser.Parser;
import com.floweytf.jembed.lang.source.diagnostic.*;
import com.floweytf.jembed.util.StringPool;

import java.io.PrintStream;
import java.util.*;

public class TranslationUnit implements IDiagnosticSink {
    private final Source source;
    private final Parser parser;
    private final CompilerFrontend compilerFrontend;
    private final List<Diagnostic> diagnostics = new ArrayList<>();
    private final Map<StringPool.StrRef, NamedDefAST> symTab = new HashMap<>();

    public TranslationUnit(CompilerFrontend compilerFrontend, Source source) {
        this.source = source;
        this.parser = new Parser(compilerFrontend, this);
        this.compilerFrontend = compilerFrontend;
    }

    public CompilerFrontend compilerFrontend() {
        return compilerFrontend;
    }

    public Parser parser() {
        return parser;
    }

    public Source source() {
        return source;
    }

    @Override
    public void addDiagnostic(Diagnostic diagnostic) {
        diagnostics.add(diagnostic);
    }

    public void dumpErrors(PrintStream out) {
        final EnumMap<Severity, Integer> diagnosticCounts = new EnumMap<>(Severity.class);
        var printer = new DiagnosticPrinter();

        for (var diagnostic : diagnostics) {
            printer.print(diagnostic);
            diagnosticCounts.put(diagnostic.severity(), 1 + diagnosticCounts.getOrDefault(diagnostic.severity(), 0));
        }

        out.println(printer);
        StringBuilder finalMsg = new StringBuilder()
            .append("Compilation ")
            .append(diagnosticCounts.containsKey(Severity.ERROR) ? "failed" : "succeeded")
            .append(" with");

        if (diagnostics.isEmpty()) {
            finalMsg.append(" no diagnostics");
        } else {
            finalMsg.append(" ");

            diagnosticCounts.forEach(((severity, count) ->
                finalMsg.append(count).append(" ").append(severity.grammaticallyCorrectName(count)).append(", ")));
            finalMsg.setLength(finalMsg.length() - 2);
        }

        out.println(finalMsg);
    }

    public boolean hasErrors() {
        return diagnostics.stream().anyMatch(p -> p.severity() == Severity.ERROR);
    }

    public DiagnosticBuilder<TranslationUnit> report() {
        return new DiagnosticBuilder<>(this).source(source);
    }

    public void report(Severity severity, String message, CodeRange range, String... notes) {
        report(severity, message, range, List.of(notes));
    }

    public void report(Severity severity, String message, CodeRange range, List<String> notes) {
        addDiagnostic(new Diagnostic(source, severity, message, range, notes));
    }

    public Map<StringPool.StrRef, NamedDefAST> symTab() {
        return symTab;
    }
}

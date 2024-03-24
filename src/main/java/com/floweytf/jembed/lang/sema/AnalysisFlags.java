package com.floweytf.jembed.lang.sema;

public record AnalysisFlags(boolean allowOverload) {
    public AnalysisFlags() {
        this(false);
    }
}

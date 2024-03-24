package com.floweytf.jembed.lang.sema.results;

import com.floweytf.jembed.lang.sema.Type;
import com.floweytf.jembed.lang.source.CodeRange;

public record ExprSemResult(Type type, ValueCategory category, CodeRange valueCategoryReportRange) {
    public enum ValueCategory {
        LVALUE,
        CONST_LVALUE,
        RVALUE
    }
}

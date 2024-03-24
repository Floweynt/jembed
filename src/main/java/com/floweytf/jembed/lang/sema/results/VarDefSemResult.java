package com.floweytf.jembed.lang.sema.results;

import com.floweytf.jembed.lang.sema.Type;
import com.floweytf.jembed.lang.source.CodeRange;

public record VarDefSemResult(Type type, CodeRange definitionRange) {
}

package com.floweytf.jembed.lang.ast.type;

import com.floweytf.jembed.lang.lexer.Token;
import com.floweytf.jembed.lang.sema.AnalysisFlags;
import com.floweytf.jembed.lang.sema.ctx.SemanticContext;
import com.floweytf.jembed.lang.sema.results.TypeSemResult;
import com.floweytf.jembed.lang.source.diagnostic.Severity;
import com.floweytf.jembed.util.StringPool;

public class TypeRefAST extends TypeAST {
    private final StringPool.StrRef name;

    public TypeRefAST(Token token) {
        super(token.range());
        name = token.identifier();
    }

    public StringPool.StrRef name() {
        return name;
    }

    @Override
    public TypeSemResult semanticAnalysis(SemanticContext context, AnalysisFlags flags) {
        var type = context.typeManager().typeByName(name);

        if (type == null) {
            context.tu().report()
                .body(Severity.ERROR, "unknown type named `" + name + "`", range());
        }

        return new TypeSemResult(type);
    }
}

package com.floweytf.jembed.lang.sema.ctx;

import com.floweytf.jembed.lang.CompilerFrontend;
import com.floweytf.jembed.lang.ast.stmt.def.FuncDefAST;
import com.floweytf.jembed.lang.source.Source;
import com.floweytf.jembed.lang.source.TranslationUnit;

public class BuiltinDefManager {
    private final FuncDefAST func;
    private final TranslationUnit tu;

    public BuiltinDefManager(CompilerFrontend frontend) {
        tu = frontend.makeTU(
            new Source(
                "#builtin",
                """
                    func temp() { }
                    """
            )
        );

        func = tu.parser().parseFuncDef();
    }

    public FuncDefAST func() {
        return func;
    }

    public TranslationUnit tu() {
        return tu;
    }
}

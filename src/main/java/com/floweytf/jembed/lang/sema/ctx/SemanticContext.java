package com.floweytf.jembed.lang.sema.ctx;

import com.floweytf.jembed.lang.CompilerFrontend;
import com.floweytf.jembed.lang.ast.ModuleAST;
import com.floweytf.jembed.lang.ast.SemanticAST;
import com.floweytf.jembed.lang.sema.AnalysisFlags;
import com.floweytf.jembed.lang.sema.results.ExprSemResult;
import com.floweytf.jembed.lang.source.TranslationUnit;
import com.floweytf.jembed.util.StringPool;

import java.util.*;

public class SemanticContext {
    private final Map<SemanticAST<?>, Object> semCache = new HashMap<>();
    private final Set<SemanticAST<?>> currentlyRunningAnalysis = new HashSet<>();

    private final CompilerFrontend frontend;
    private final TypeManager typeManager;
    private final OperatorManager operatorManager;
    private final SymbolFrameManager symbolFrameManager;

    // symbol stack
    // TODO: handle NamedSymbolContainer
    private final List<Map<StringPool.StrRef, ExprSemResult>> symbols = new ArrayList<>();

    private TranslationUnit tu;

    public SemanticContext(CompilerFrontend frontend) {
        this.frontend = frontend;
        typeManager = new TypeManager(frontend);
        operatorManager = new OperatorManager(this, typeManager);
        symbolFrameManager = new SymbolFrameManager(this);
    }

    @SuppressWarnings("unchecked")
    public <T> T runSemantic(SemanticAST<T> ast, AnalysisFlags flags) {
        if (ast == null)
            return null;

        var res = (T) semCache.getOrDefault(ast, null);
        if (res == null) {
            if (currentlyRunningAnalysis.contains(ast)) {
                throw new IllegalStateException("cyclic AST analysis pattern found, this is a bug!");
            }
            // setup state
            currentlyRunningAnalysis.add(ast);
            final var oldTu = tu;
            if (ast instanceof ModuleAST module) {
                tu = module.tu();
            }

            res = ast.semanticAnalysis(this, flags);
            semCache.put(ast, res);

            // end state
            currentlyRunningAnalysis.remove(ast);
            tu = oldTu;
        }

        return res;
    }

    public <T> T runSemantic(SemanticAST<T> ast) {
        return runSemantic(ast, new AnalysisFlags());
    }

    // getters
    public TranslationUnit tu() {
        return tu;
    }

    public TypeManager typeManager() {
        return typeManager;
    }

    public SymbolFrameManager symbolFrameManager() {
        return symbolFrameManager;
    }

    public OperatorManager operatorManager() {
        return operatorManager;
    }
}
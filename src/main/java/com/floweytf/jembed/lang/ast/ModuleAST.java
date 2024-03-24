package com.floweytf.jembed.lang.ast;

import com.floweytf.jembed.lang.ast.expr.literal.StringTemplateLiteralAST;
import com.floweytf.jembed.lang.sema.AnalysisFlags;
import com.floweytf.jembed.lang.sema.NamedSymbolProvider;
import com.floweytf.jembed.lang.sema.SymbolContainer;
import com.floweytf.jembed.lang.sema.ctx.SemanticContext;
import com.floweytf.jembed.lang.source.CodeRange;
import com.floweytf.jembed.lang.source.TranslationUnit;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ModuleAST extends SemanticAST<Void> implements NamedSymbolProvider {
    private final List<StringTemplateLiteralAST> imports;
    private final List<SemanticAST<?>> statements;
    private final SymbolContainer symbolTable = new SymbolContainer();
    private final TranslationUnit tu;

    public ModuleAST(List<StringTemplateLiteralAST> imports, List<SemanticAST<?>> statements, CodeRange range, TranslationUnit tu) {
        super(range);
        this.imports = Collections.unmodifiableList(imports);
        this.statements = Collections.unmodifiableList(statements);
        this.tu = tu;
    }

    public List<StringTemplateLiteralAST> imports() {
        return imports;
    }

    public List<SemanticAST<?>> statements() {
        return statements;
    }

    public TranslationUnit tu() {
        return tu;
    }

    @Override
    public SymbolContainer getSymbolTable() {
        return symbolTable;
    }

    @Override
    public void visit(Consumer<ASTBase> visitor) {
        imports.forEach(visitor);
        statements.forEach(visitor);
    }

    @Override
    public Void semanticAnalysis(SemanticContext context, AnalysisFlags flags) {
        context.symbolFrameManager().withProvider(this, () -> {
            statements.forEach(context::runSemantic);
        });

        return null;
    }
}

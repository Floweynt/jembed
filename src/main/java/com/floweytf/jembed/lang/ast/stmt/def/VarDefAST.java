package com.floweytf.jembed.lang.ast.stmt.def;

import com.floweytf.jembed.lang.ast.ASTBase;
import com.floweytf.jembed.lang.ast.expr.ExprAST;
import com.floweytf.jembed.lang.ast.type.TypeAST;
import com.floweytf.jembed.lang.lexer.Token;
import com.floweytf.jembed.lang.sema.AnalysisFlags;
import com.floweytf.jembed.lang.sema.ctx.SemanticContext;
import com.floweytf.jembed.lang.sema.results.ExprSemResult;
import com.floweytf.jembed.lang.sema.results.TypeSemResult;
import com.floweytf.jembed.lang.sema.results.VarDefSemResult;
import com.floweytf.jembed.lang.sema.sym.SymbolType;
import com.floweytf.jembed.lang.sema.sym.VariableSymbolInfo;
import com.floweytf.jembed.lang.source.CodeLocation;
import com.floweytf.jembed.lang.source.diagnostic.Severity;
import com.floweytf.jembed.util.Utils;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class VarDefAST extends NamedDefAST<VarDefSemResult> {
    @Nullable
    private final TypeAST type;
    @Nullable
    private final ExprAST init;
    private final boolean isMutable;

    public VarDefAST(Token declKw, Token name, boolean isMutable, @Nullable TypeAST type, @Nullable ExprAST init) {
        super(declKw.range().start(), buildEnding(name, type, init), name);
        this.type = type;
        this.init = init;
        this.isMutable = isMutable;
    }

    private static CodeLocation buildEnding(Token name, TypeAST type, ExprAST init) {
        if (init != null)
            return init.end();
        if (type != null)
            return type.end();
        return name.range().end();
    }

    public TypeAST type() {
        return type;
    }

    public ExprAST init() {
        return init;
    }

    public boolean isMutable() {
        return isMutable;
    }

    @Override
    public void visit(Consumer<ASTBase> visitor) {
        if (type != null)
            visitor.accept(type);
        if (init != null)
            visitor.accept(init);
    }

    @Override
    public String toString() {
        return "VarDefAST[" + name() + "]";
    }

    @Override
    public VarDefSemResult semanticAnalysis(SemanticContext context, AnalysisFlags flags) {
        if (type == null && init == null) {
            context.tu().report()
                .body(Severity.ERROR, "cannot declare type with deduced type without initializer", range())
                .hint("A variable with explicit type looks like: `var x: Type`")
                .hint("A variable with deduced type looks like: `var x = <initializer>`");
        }

        var explicitRes = context.runSemantic(type);
        var initializerRes = context.runSemantic(init);

        var deducedType = Utils.mapNull(explicitRes, TypeSemResult::type);
        var initType = Utils.mapNull(initializerRes, ExprSemResult::type);

        if (deducedType != null && initType != null && !context.typeManager().canCoerce(deducedType, initType)) {
            context.tu().report()
                .body(Severity.ERROR, "cannot convert initializer type to variable type", init.range())
                .hint("initializer has type `" + initType.name() + "`")
                .done();
            return new VarDefSemResult(null, range());
        }

        deducedType = Utils.coalesce(deducedType, () -> initType);

        // TODO: report "previously defined here" messages
        context.symbolFrameManager().defineSymbol(
            SymbolType.VALUE,
            name(),
            new VariableSymbolInfo(deducedType, isMutable ? ExprSemResult.ValueCategory.LVALUE : ExprSemResult.ValueCategory.CONST_LVALUE, this)
        );

        return new VarDefSemResult(deducedType, range());
    }
}
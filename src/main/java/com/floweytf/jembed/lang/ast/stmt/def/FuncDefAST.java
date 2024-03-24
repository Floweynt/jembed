package com.floweytf.jembed.lang.ast.stmt.def;

import com.floweytf.jembed.lang.ast.ASTBase;
import com.floweytf.jembed.lang.ast.stmt.BlockAST;
import com.floweytf.jembed.lang.ast.type.TypeAST;
import com.floweytf.jembed.lang.lexer.Token;
import com.floweytf.jembed.lang.sema.AnalysisFlags;
import com.floweytf.jembed.lang.sema.OverloadedMethod;
import com.floweytf.jembed.lang.sema.ctx.SemanticContext;
import com.floweytf.jembed.lang.sema.results.VarDefSemResult;
import com.floweytf.jembed.lang.sema.sym.SymbolType;
import com.floweytf.jembed.lang.source.CodeRange;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class FuncDefAST extends NamedDefAST<Void> {
    private final List<VarDefAST> parameters;
    private final BlockAST body;
    @Nullable
    private final TypeAST returnType;
    private final CodeRange signatureRange;

    public FuncDefAST(Token start, Token name, List<VarDefAST> params, Token closingParen, @Nullable TypeAST returnType, BlockAST body) {
        super(start.range().start(), body.end(), name);
        parameters = Collections.unmodifiableList(params);
        this.returnType = returnType;
        this.body = body;
        signatureRange = new CodeRange(start.start(), returnType == null ? closingParen.range().end() : returnType.range().end());
    }

    public BlockAST body() {
        return body;
    }

    public List<VarDefAST> parameters() {
        return parameters;
    }

    public TypeAST returnType() {
        return returnType;
    }

    @Override
    public void visit(Consumer<ASTBase> visitor) {
        parameters.forEach(visitor);
        visitor.accept(body);
        if (returnType != null)
            visitor.accept(returnType);
    }

    @Override
    public Void semanticAnalysis(SemanticContext context, AnalysisFlags flags) {
        context.symbolFrameManager().defineFrame();

        var parameterResults = parameters.stream().map(context::runSemantic).toList();
        // if any of them are null, we give up and don't declare anything...
        if (parameterResults.stream().noneMatch(x -> x.type() == null)) {
            context.symbolFrameManager().defineSymbolGlobally(
                SymbolType.OVERLOAD,
                name(),
                // TODO: figure out how to handle overload conflicts
                new OverloadedMethod.Overload(
                    parameterResults.stream().map(VarDefSemResult::type).toList(),
                    context.typeManager().builtinInt(),
                    context.tu(),
                    signatureRange
                )
            );
        }

        context.runSemantic(body);
        context.symbolFrameManager().exitFrame();
        return null;
    }

    @Override
    public String toString() {
        return "FuncDefAST[" + name() + "]";
    }
}
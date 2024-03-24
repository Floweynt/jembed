package com.floweytf.jembed.lang.ast.expr;

import com.floweytf.jembed.lang.ast.ASTBase;
import com.floweytf.jembed.lang.lexer.Token;
import com.floweytf.jembed.lang.sema.AnalysisFlags;
import com.floweytf.jembed.lang.sema.ctx.SemanticContext;
import com.floweytf.jembed.lang.sema.results.ExprSemResult;
import com.floweytf.jembed.lang.source.CodeRange;
import com.floweytf.jembed.util.StringPool;

import java.util.function.Consumer;

public class MemberAccessAST extends ExprAST {
    private final Token memberName;
    private final CodeRange opRange;
    private final ExprAST object;

    public MemberAccessAST(ExprAST object, Token operator, Token memberName) {
        super(object.start(), memberName.range().end());
        this.object = object;
        this.opRange = operator.range();
        this.memberName = memberName;
    }

    public StringPool.StrRef memberName() {
        return memberName.identifier();
    }

    public CodeRange opRange() {
        return opRange;
    }

    public CodeRange memberRange() {
        return memberName.range();
    }

    public ExprAST object() {
        return object;
    }

    @Override
    public void visit(Consumer<ASTBase> visitor) {
        visitor.accept(object);
    }

    @Override
    public String toString() {
        return "MemberAccessAST[" + memberName() + "]";
    }

    @Override
    public ExprSemResult semanticAnalysis(SemanticContext context, AnalysisFlags flags) {
        throw new UnsupportedOperationException("no impl");
    }
}
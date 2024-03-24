package com.floweytf.jembed.lang.sema.ctx;

import com.floweytf.jembed.lang.ast.expr.binop.BinopExprType;
import com.floweytf.jembed.lang.ast.expr.unary.UnaryExprType;
import com.floweytf.jembed.lang.sema.OverloadedMethod;
import com.floweytf.jembed.lang.sema.Type;
import com.floweytf.jembed.lang.source.CodeRange;
import com.floweytf.jembed.lang.source.TranslationUnit;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OperatorManager {
    private final Map<BinopExprType, OverloadedMethod> binops = new HashMap<>();
    private final SemanticContext context;

    public OperatorManager(SemanticContext context, TypeManager types) {
        this.context = context;

        final var i = types.builtinInt();
        final var n = types.builtinNumber();
        final var s = types.builtinString();

        // Addition
        addBinaryOpOverload(i, BinopExprType.ADD, i, i, null, null);
        addBinaryOpOverload(n, BinopExprType.ADD, i, n, null, null);
        addBinaryOpOverload(n, BinopExprType.ADD, n, i, null, null);
        addBinaryOpOverload(n, BinopExprType.ADD, n, n, null, null);

        // string concatenation
        addBinaryOpOverload(s, BinopExprType.ADD, s, s, null, null);

        addBinaryOpOverload(s, BinopExprType.ADD, i, s, null, null);
        addBinaryOpOverload(s, BinopExprType.ADD, s, i, null, null);

        addBinaryOpOverload(s, BinopExprType.ADD, n, s, null, null);
        addBinaryOpOverload(s, BinopExprType.ADD, s, n, null, null);

        // Multiplication
        addBinaryOpOverload(i, BinopExprType.MUL, i, i, null, null);
        addBinaryOpOverload(n, BinopExprType.MUL, i, n, null, null);
        addBinaryOpOverload(n, BinopExprType.MUL, n, i, null, null);
        addBinaryOpOverload(n, BinopExprType.MUL, n, n, null, null);
    }

    public void addBinaryOpOverload(Type lhs, BinopExprType binop, Type rhs, Type result, @Nullable TranslationUnit source, @Nullable CodeRange definition) {
        getBinaryOpResolver(binop).addOverload(new OverloadedMethod.Overload(List.of(lhs, rhs), result, source, definition));
    }

    public OverloadedMethod.Overload getBinaryOpOverload(Type lhs, BinopExprType binop, Type rhs) {
        return getBinaryOpResolver(binop).getOverload(List.of(lhs, rhs), context);
    }

    public OverloadedMethod getBinaryOpResolver(BinopExprType binop) {
        return binops.computeIfAbsent(binop, x -> new OverloadedMethod());
    }

    public Type unaryOpResult(Type lhs, UnaryExprType binop) {
        return null;
    }
}

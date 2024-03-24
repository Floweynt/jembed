package com.floweytf.jembed.lang.sema;

import com.floweytf.jembed.lang.ast.stmt.def.NamedDefAST;
import com.floweytf.jembed.util.StringPool;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SymbolContainer {
    private final Map<StringPool.StrRef, List<NamedDefAST>> astMap = new HashMap<>();
    // private final Map<StringPool.StrRef, Type> typeMap = new HashMap<>();

    public void add(NamedDefAST ast) {
        astMap.computeIfAbsent(ast.name(), x -> new ArrayList<>()).add(ast);
    }

    public @Nullable List<NamedDefAST> get(StringPool.StrRef ref) {
        var ast = astMap.get(ref);
        if (ast == null)
            return null;
        return Collections.unmodifiableList(ast);
    }
}

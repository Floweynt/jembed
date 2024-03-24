package com.floweytf.jembed.lang.sema.ctx;

import com.floweytf.jembed.lang.CompilerFrontend;
import com.floweytf.jembed.lang.ast.SemanticAST;
import com.floweytf.jembed.lang.sema.BuiltinType;
import com.floweytf.jembed.lang.sema.Type;
import com.floweytf.jembed.util.StringPool;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// this represents *global* type information, since types may persist outside their frames (even though they may not be referred to)
// SymbolFrameManager handles actual name lookup
public class TypeManager {
    private final Set<SemanticAST<?>> currentlyRunningAnalysis = new HashSet<>();
    private final Map<Class<?>, Type> typeLookup = new HashMap<>();
    private final Map<StringPool.StrRef, Type> builtinTypeByName = new HashMap<>();

    private final Type builtinInt;
    private final Type builtinString;
    private final Type builtinNumber;

    private final CompilerFrontend frontend;

    public TypeManager(CompilerFrontend frontend) {
        this.frontend = frontend;
        builtinInt = registerBuiltin(Long.class, "int");
        builtinNumber = registerBuiltin(Double.class, "number");
        builtinString = registerBuiltin(String.class, "string");
    }

    private <T> Type registerBuiltin(Class<T> clazz, String name) {
        var ty = new BuiltinType<T>(clazz, name);
        typeLookup.put(clazz, ty);
        builtinTypeByName.put(frontend.intern(name), ty);
        return ty;
    }

    public boolean canCoerce(Type from, Type to) {
        return from == to;
    }

    public Type typeByClass(Class<?> aClass) {
        var res = typeLookup.get(aClass);
        if (res == null)
            throw new IllegalArgumentException("type not found for, has it been registered? " + aClass.getName());
        return res;
    }

    public Type typeByName(StringPool.StrRef ref) {
        return builtinTypeByName.get(ref);
    }

    public Type builtinInt() {
        return builtinInt;
    }

    public Type builtinNumber() {
        return builtinNumber;
    }

    public Type builtinString() {
        return builtinString;
    }
}

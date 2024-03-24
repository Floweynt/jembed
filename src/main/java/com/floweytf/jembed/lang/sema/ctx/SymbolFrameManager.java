package com.floweytf.jembed.lang.sema.ctx;

import com.floweytf.jembed.lang.ast.stmt.def.NamedDefAST;
import com.floweytf.jembed.lang.sema.NamedSymbolProvider;
import com.floweytf.jembed.lang.sema.OverloadedMethod;
import com.floweytf.jembed.lang.sema.SymbolContainer;
import com.floweytf.jembed.lang.sema.Type;
import com.floweytf.jembed.lang.sema.sym.SymbolType;
import com.floweytf.jembed.util.StringPool;

import java.util.*;

public class SymbolFrameManager {
    // thoughts:
    // when looking up a type, one of three things may happen:
    // 1. the name is locally scoped, which means you iterate the stack and look for it
    // 2. the name is "globally" scoped (outside functions in this context), but type is already known
    //


    private final SemanticContext context;
    private final Deque<OrderedFrame> frameStack = new ArrayDeque<>();
    private final Deque<TypeDeductionFrame> deductionFrames = new ArrayDeque<>();
    private GlobalFrame currentProvider = null;
    public SymbolFrameManager(SemanticContext context) {
        this.context = context;
    }

    private void beginTypeDeduction(NamedDefAST<?> ast, GlobalFrame container) {
        // starts doing type deduction, which is pretty cursed
        for (var entry : deductionFrames) {
            // TODO: properly handle exception
            if (entry.currentDeduction == ast) {
                throw new UnsupportedOperationException();
            }
        }

        // store this
        deductionFrames.push(new TypeDeductionFrame(ast, container));
        var oldProvider = currentProvider;

        // okay, begin semantic analysis
        context.runSemantic(ast);

        currentProvider = oldProvider;
        deductionFrames.pop();
    }

    public void defineFrame() {
        frameStack.add(new OrderedFrame());
    }

    public void exitFrame() {
        frameStack.pop();
    }

    public <T, U> boolean defineSymbol(SymbolType<T, U> type, StringPool.StrRef name, U info) {
        if (frameStack.isEmpty()) {
            defineSymbolGlobally(type, name, info);
        }

        return frameStack.getLast().define(type, name, info);
    }

    public <T, U> boolean defineSymbolGlobally(SymbolType<T, U> type, StringPool.StrRef name, U info) {
        Objects.requireNonNull(currentProvider);
        return currentProvider.define(type, name, info);
    }

    public <T, U> T lookupSymbol(SymbolType<T, U> type, StringPool.StrRef name) {
        for (var it = frameStack.descendingIterator(); it.hasNext(); ) {
            var x = it.next();
            var res = x.read(type, name);
            if (res != null)
                return res;
        }

        for (var it = currentProvider; it != null; it = it.parent) {
            var res = it.read(type, name);
            if (res != null)
                return res;
        }

        return null;
    }

    public void lookupType() {

    }

    public void withProvider(NamedSymbolProvider newProvider, Runnable callback) {
        var old = currentProvider;
        currentProvider = new GlobalFrame(currentProvider, newProvider.getSymbolTable());
        callback.run();
        currentProvider = old;
    }

    /*
    public void typeSymbol() {

    }*/

    // basically, when attempting to deduce types, it may trigger the deduction of another variable/function
    // this keep tracks of that info
    // this also implies you **must** report type information *as early as possible*
    private record TypeDeductionFrame(
        NamedDefAST<?> currentDeduction,
        GlobalFrame ast
    ) {
    }

    private static class OrderedFrame {
        private final Map<StringPool.StrRef, Object> symbolLookup = new HashMap<>();

        public <T, U> boolean define(SymbolType<T, U> type, StringPool.StrRef name, U info) {
            if (type == SymbolType.TYPE)
                throw new IllegalStateException();

            if (type == SymbolType.OVERLOAD) {
                ((OverloadedMethod) symbolLookup.computeIfAbsent(name, x -> new OverloadedMethod())).addOverload((OverloadedMethod.Overload) info);
            } else {
                symbolLookup.put(name, info);
            }

            return true;
        }

        @SuppressWarnings("unchecked")
        public <T, U> T read(SymbolType<T, U> type, StringPool.StrRef name) {
            return (T) symbolLookup.get(name);
        }
    }

    // This is something that provides named (order indep) symbols
    // currently, this is only the module.
    private class GlobalFrame {
        private final SymbolContainer container;
        private final GlobalFrame parent;
        private final Map<StringPool.StrRef, Type> symbolLookup = new HashMap<>();

        // since doing semantic analysis on one thing may trigger other things, we need to prepare a stack

        public GlobalFrame(GlobalFrame parent, SymbolContainer container) {
            this.parent = parent;
            this.container = container;
        }

        public <T, U> boolean define(SymbolType<T, U> type, StringPool.StrRef name, U info) {
            if (type == SymbolType.TYPE)
                throw new IllegalStateException();

            if (type == SymbolType.OVERLOAD) {
                ((OverloadedMethod) symbolLookup.computeIfAbsent(name, x -> new OverloadedMethod())).addOverload((OverloadedMethod.Overload) info);
            } else {
                symbolLookup.put(name, (Type) info);
            }

            return true;
        }

        @SuppressWarnings("unchecked")
        public <T, U> T read(SymbolType<T, U> type, StringPool.StrRef name) {
            if (symbolLookup.containsKey(name)) {
                return (T) symbolLookup.get(name);
            }

            beginTypeDeduction(container.get(name).get(0), this);

            if (symbolLookup.containsKey(name)) {
                return (T) symbolLookup.get(name);
            }

            throw new IllegalStateException();
        }
    }
}

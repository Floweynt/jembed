package com.floweytf.jvmir.pass;

import com.floweytf.jvmir.BlockDAG;
import com.floweytf.jvmir.CGMethod;
import com.floweytf.jvmir.util.DAGUtils;
import com.floweytf.jvmir.util.GraphEmitter;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class Pass<A, B> implements Function<A, B> {
    @SafeVarargs
    public static <T> Pass<T, T> of(Pass<T, T>... passes) {
        return new Pass<>() {
            @Override
            public T apply(T value) {
                for (var pass : passes) {
                    value = pass.apply(value);
                }
                return value;
            }

            @Override
            public List<String> getPassNames() {
                return Arrays.stream(passes).flatMap(x -> x.getPassNames().stream()).toList();
            }
        };
    }

    public static Pass<BlockDAG, BlockDAG> validatePass() {
        return new Pass<>() {
            @Override
            public BlockDAG apply(BlockDAG value) {
                DAGUtils.validateDAG(value);
                return value;
            }

            @Override
            public List<String> getPassNames() {
                return List.of("misc:DAGValidate");
            }
        };
    }

    public static Pass<BlockDAG, BlockDAG> printPass(GraphEmitter out, boolean refCounts) {
        return new Pass<>() {
            @Override
            public BlockDAG apply(BlockDAG value) {
                DAGUtils.printDagDot(value, out, refCounts);
                return value;
            }

            @Override
            public List<String> getPassNames() {
                return List.of("print:DAGPrintDot");
            }
        };
    }

    @SafeVarargs
    public static Pass<CGMethod, CGMethod> forEachBlock(Pass<BlockDAG, BlockDAG>... dagPasses) {
        final var dagPass = of(dagPasses);
        return new Pass<>() {
            @Override
            public CGMethod apply(CGMethod value) {
                value.mapBlocks(dagPass);
                return value;
            }

            @Override
            public List<String> getPassNames() {
                return List.of("ForEachDAG(" + String.join(", ", dagPass.getPassNames()) + ")");
            }
        };
    }

    public <C> Pass<A, C> and(Pass<B, C> next) {
        final var that = this;
        return new Pass<>() {
            @Override
            public C apply(A value) {
                return next.apply(that.apply(value));
            }

            @Override
            public List<String> getPassNames() {
                return Stream.of(that.getPassNames(), next.getPassNames()).flatMap(Collection::stream).toList();
            }
        };
    }

    @Override
    public abstract B apply(A value);

    public abstract List<String> getPassNames();
}
package com.floweytf.jvmir.pass;

import com.floweytf.jvmir.InstructionDAG;
import com.floweytf.jvmir.util.GraphEmitter;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public abstract class Pass<A, B> {
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

    public abstract B apply(A value);

    public abstract List<String> getPassNames();

    public static Pass<InstructionDAG, InstructionDAG> validatePass() {
        return new Pass<>() {
            @Override
            public InstructionDAG apply(InstructionDAG value) {
                value.validateDag();
                value.getRoot().typeCheck();
                return value;
            }

            @Override
            public List<String> getPassNames() {
                return List.of("misc:DAGValidate");
            }
        };
    }

    public static Pass<InstructionDAG, InstructionDAG> printPass(GraphEmitter out) {
        return new Pass<>() {
            @Override
            public InstructionDAG apply(InstructionDAG value) {
                value.printDagDot(out);
                return value;
            }

            @Override
            public List<String> getPassNames() {
                return List.of("print:DAGPrintDot");
            }
        };
    }
}

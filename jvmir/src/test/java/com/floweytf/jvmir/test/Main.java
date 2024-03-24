package com.floweytf.jvmir.test;

import com.floweytf.jvmir.InstructionDAG;
import com.floweytf.jvmir.pass.Pass;
import com.floweytf.jvmir.pass.legalize.InsertDiscardPass;
import com.floweytf.jvmir.util.DotEmitter;

public class Main {
    public static void main(String... args) {
        var dag = new InstructionDAG();

        var add = dag.makeIAdd(
            dag.makeConst(1),
            dag.makeConst(2)
        );

        dag.getRoot().addOperand(add);

        dag.getRoot().addOperand(dag.makeBranch(add));


        final var pipeline = Pass.printPass(new DotEmitter(System.out))
            .and(InsertDiscardPass.PASS)
            .and(Pass.validatePass());

        pipeline.apply(dag);
    }
}

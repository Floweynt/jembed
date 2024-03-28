package com.floweytf.jvmir.test;

import com.floweytf.jvmir.CGMethod;
import com.floweytf.jvmir.pass.Pass;
import com.floweytf.jvmir.pass.legalize.TreeifyPass;
import com.floweytf.jvmir.util.DotEmitter;
import org.objectweb.asm.Type;

public class Main {
    public static void main(String... args) {
        var method = new CGMethod();
        var dag = method.defineBlock();

        dag.invokeMember(
            "charAt",
            Type.CHAR_TYPE,
            dag.immediate("foo"),
            dag.immediate(2)
        );

        final var pipeline =
            Pass.of(
                Pass.forEachBlock(
                    Pass.validatePass(),
                    new TreeifyPass(),
                    Pass.validatePass(),
                    Pass.printPass(new DotEmitter(System.out), false)
                )
            );

        pipeline.apply(method);
    }
}
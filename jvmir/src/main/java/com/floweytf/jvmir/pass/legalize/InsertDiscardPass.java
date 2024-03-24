package com.floweytf.jvmir.pass.legalize;

import com.floweytf.jvmir.InstructionDAG;
import com.floweytf.jvmir.pass.Pass;

import java.util.List;

public class InsertDiscardPass extends Pass<InstructionDAG, InstructionDAG> {
    public static final InsertDiscardPass PASS = new InsertDiscardPass();

    private InsertDiscardPass() {

    }

    @Override
    public InstructionDAG apply(InstructionDAG value) {
        value.iterateNodes(i -> {
            if(i instanceof ISideEffectChain instr) {
                instr.forEach(operand -> {
                    if(operand.type() != null) {
                        var discard = value.makeDiscard(operand);
                        operand.replace(discard);
                    }
                });
            }
        });

        return value;
    }

    @Override
    public List<String> getPassNames() {
        return List.of("legalize:InsertDiscard");
    }
}

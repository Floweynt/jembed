package com.floweytf.jvmir.pass;

import com.floweytf.jvmir.InstructionDAG;

import java.util.List;

public class TreeifyPass extends Pass<InstructionDAG, InstructionDAG> {
    @Override
    public InstructionDAG apply(InstructionDAG value) {
        value.iterateNodes(instr -> {
            // TODO: skip dup instructions
            if(instr.refCount() > 1) {


            }
        });

        return value;
    }

    @Override
    public List<String> getPassNames() {
        return List.of("legalize:EnforceStackMachineTree");
    }
}

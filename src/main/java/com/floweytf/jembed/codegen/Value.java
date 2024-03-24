package com.floweytf.jembed.codegen;

import com.floweytf.jvmir.InstructionDAG;

import java.util.Optional;

public abstract class Value {
    public abstract void lowerToInstr(InstructionDAG dag);

    public abstract Optional<Object> tryEval();
}

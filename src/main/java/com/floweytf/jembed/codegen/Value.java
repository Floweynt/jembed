package com.floweytf.jembed.codegen;

import com.floweytf.jvmir.BlockDAG;

import java.util.Optional;

public abstract class Value {
    public abstract void lowerToInstr(BlockDAG dag);

    public abstract Optional<Object> tryEval();
}

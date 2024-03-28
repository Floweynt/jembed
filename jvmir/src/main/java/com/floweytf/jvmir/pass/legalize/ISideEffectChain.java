package com.floweytf.jvmir.pass.legalize;

import com.floweytf.jvmir.InstrNode;

import java.util.function.BiConsumer;

/**
 * Represents an instruction that chains side effects
 */
public interface ISideEffectChain {
    void forEach(BiConsumer<Integer, InstrNode> consumer);
}

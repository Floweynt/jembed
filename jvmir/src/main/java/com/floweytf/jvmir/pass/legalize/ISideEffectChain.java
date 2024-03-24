package com.floweytf.jvmir.pass.legalize;

import com.floweytf.jvmir.Instr;

import java.util.function.Consumer;

/**
 * Represents an instruction that chains side effects
 */
public interface ISideEffectChain {
    void forEach(Consumer<Instr> consumer);
}

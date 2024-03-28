package com.floweytf.jvmir.instrs.pseduo;

import com.floweytf.jvmir.BlockInstrWriter;
import com.floweytf.jvmir.Instr;

public final class NopInstr extends Instr {
    public NopInstr() {
        super(null);
    }//

    @Override
    public void emit(BlockInstrWriter writer) {
    }
}
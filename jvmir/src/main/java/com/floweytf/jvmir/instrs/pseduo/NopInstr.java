
package com.floweytf.jvmir.instrs.pseduo;

import com.floweytf.jvmir.InstructionDAG;
import com.floweytf.jvmir.Instr;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.MethodVisitor;

public final class NopInstr extends Instr {
    @ApiStatus.Internal
    public NopInstr(InstructionDAG dag) {
        super(dag, null);
    }

    @Override
    public void emit(MethodVisitor writer) {
    }

    @Override
    public void typeCheck() {
    }
}

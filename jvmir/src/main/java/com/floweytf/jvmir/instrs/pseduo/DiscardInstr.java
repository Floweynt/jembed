package com.floweytf.jvmir.instrs.pseduo;

import com.floweytf.jvmir.InstructionDAG;
import com.floweytf.jvmir.Instr;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class DiscardInstr extends Instr {
    @ApiStatus.Internal
    public DiscardInstr(InstructionDAG dag, Instr value) {
        super(dag, null, value);
    }

    @Override
    public void emit(MethodVisitor writer) {
        value().emit(writer);
        writer.visitInsn(Opcodes.POP);
    }

    @Override
    public void typeCheck() {
        super.typeCheck();

        typeAssert(value().type() != null, "expected %o0 to have a type");
    }

    public Instr value() {
        return getOperand(0);
    }

    @Override
    public String getOperandName(int i) {
        return "value";
    }
}
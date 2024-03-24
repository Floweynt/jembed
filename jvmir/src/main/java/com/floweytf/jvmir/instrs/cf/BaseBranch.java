package com.floweytf.jvmir.instrs.cf;

import com.floweytf.jvmir.InstructionDAG;
import com.floweytf.jvmir.Instr;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;

/**
 * Represents a branch instruction.
 */
public abstract class BaseBranch extends Instr {
    protected BaseBranch(InstructionDAG dag, Type type, Instr... operands) {
        super(dag, type, operands);
    }

    protected BaseBranch(InstructionDAG dag, Type type, List<Instr> operands) {
        super(dag, type, operands);
    }

    public abstract void emit(MethodVisitor writer);
}

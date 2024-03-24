package com.floweytf.jvmir.instrs.cf;

import com.floweytf.jvmir.InstructionDAG;
import com.floweytf.jvmir.Instr;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

public class Branch extends BaseBranch {
    private static final List<String> IMM_NAMES = List.of("target");
    private final Instr target;

    @ApiStatus.Internal
    public Branch(InstructionDAG dag, Instr target) {
        super(dag, null);
        this.target = target;
    }

    @Override
    public List<Object> getImmOperands() {
        return List.of(target);
    }

    @Override
    public List<String> getImmNames() {
        return IMM_NAMES;
    }

    public void emit(MethodVisitor writer) {

    }

    public Instr target() {
        return target;
    }
}

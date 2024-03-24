package com.floweytf.jvmir.instrs.pseduo;

import com.floweytf.jvmir.InstructionDAG;
import com.floweytf.jvmir.Instr;
import com.floweytf.jvmir.pass.legalize.ISideEffectChain;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.MethodVisitor;

import java.util.Objects;
import java.util.function.Consumer;

public final class ChainEffectInstr extends Instr implements ISideEffectChain {
    private static final String[] OPERAND_NAMES = {"leftEffect", "value", "rightEffect"};

    @ApiStatus.Internal
    public ChainEffectInstr(InstructionDAG dag, Instr left, Instr value, Instr right) {
        super(dag, Objects.requireNonNull(value.type(), "value must have a type"), left, value, right);
    }

    @Override
    public void emit(MethodVisitor writer) {
        leftEffect().emit(writer);
        rightEffect().emit(writer);
        rightEffect().emit(writer);
    }

    @Override
    public void typeCheck() {
        super.typeCheck();

        final var vT = value().type();
        typeAssert(leftEffect().type() == null, "expected %o0 to have no type, but got %t0");
        typeAssert(rightEffect().type() == null, "expected %o2 to have no type, but got %t2");
        typeAssert(vT == null, "expected %o1 to have a type");
        typeAssert(Objects.equals(vT, type()), "expected %o1 to have type %t but got %t1");
    }

    public Instr leftEffect() {
        return getOperand(0);
    }

    public Instr value() {
        return getOperand(1);
    }

    public Instr rightEffect() {
        return getOperand(2);
    }

    @Override
    public String getOperandName(int i) {
        return OPERAND_NAMES[i];
    }

    @Override
    public void forEach(Consumer<Instr> consumer) {
        consumer.accept(leftEffect());
        consumer.accept(rightEffect());
    }
}
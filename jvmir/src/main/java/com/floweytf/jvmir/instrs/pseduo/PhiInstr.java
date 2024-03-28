package com.floweytf.jvmir.instrs.pseduo;

import com.floweytf.jvmir.BlockInstrWriter;
import com.floweytf.jvmir.Instr;
import com.floweytf.jvmir.InstrNode;
import org.objectweb.asm.Type;

import java.util.Objects;

/**
 * Selects values and performs side effects based on predicate.
 */
public final class PhiInstr extends Instr {
    private static final String[] OPERAND_NAMES = {"condition", "bTrue", "bFalse"};

    public PhiInstr(Type type) {
        super(type);
    }

    @Override
    public void emit(BlockInstrWriter writer) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void check() {
        assertOperandCount(3);
        typeAssert(Type.BOOLEAN_TYPE.equals(condition().type()), "expected %o0 to have type boolean, but got %t0");
        typeAssert(Objects.equals(type(), trueBranch().type()), "expected %o1 to have type %t, but got %t1");
        typeAssert(Objects.equals(type(), falseBranch().type()), "expected %o2 to have type %t, but got %t2");
    }

    @Override
    public String getOperandName(int i) {
        return OPERAND_NAMES[i];
    }

    public InstrNode condition() {
        return getOperand(0);
    }

    public InstrNode trueBranch() {
        return getOperand(1);
    }

    public InstrNode falseBranch() {
        return getOperand(2);
    }
}
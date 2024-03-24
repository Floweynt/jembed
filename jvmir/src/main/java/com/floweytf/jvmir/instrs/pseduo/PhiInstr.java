package com.floweytf.jvmir.instrs.pseduo;

import com.floweytf.jvmir.InstructionDAG;
import com.floweytf.jvmir.Instr;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.Objects;

/**
 * Selects values and performs side effects based on predicate.
 *
 * @see InstructionDAG#makePhi(Instr, Type, Instr, Instr)
 */
public final class PhiInstr extends Instr {
    private static final String[] OPERAND_NAMES = {"condition", "bTrue", "bFalse"};

    /**
     * Constructs an add instruction.
     *
     * @apiNote Internal
     * @see InstructionDAG#makePhi(Instr, Type, Instr, Instr)
     */
    @ApiStatus.Internal
    public PhiInstr(InstructionDAG dag, Type type, Instr cond, Instr brT, Instr brF) {
        super(dag, type, cond, brT, brF);
    }

    @Override
    public void emit(MethodVisitor writer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void typeCheck() {
        super.typeCheck();

        typeAssert(Type.BOOLEAN_TYPE.equals(condition().type()), "expected %o0 to have type boolean, but got %t0");
        typeAssert(Objects.equals(type(), trueBranch().type()), "expected %o1 to have type %t, but got %t1");
        typeAssert(Objects.equals(type(), falseBranch().type()), "expected %o2 to have type %t, but got %t2");
    }

    @Override
    public String getOperandName(int i) {
        return OPERAND_NAMES[i];
    }

    /**
     * Obtains the left-hand side operand of this instruction
     *
     * @return The LHS instruction
     */
    public Instr condition() {
        return getOperand(0);
    }

    /**
     * Obtains the right-hand side operand of this instruction
     *
     * @return The RHS instruction
     */
    public Instr trueBranch() {
        return getOperand(1);
    }

    /**
     * Obtains the right-hand side operand of this instruction
     *
     * @return The RHS instruction
     */
    public Instr falseBranch() {
        return getOperand(2);
    }
}
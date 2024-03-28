package com.floweytf.jvmir.instrs;

import com.floweytf.jvmir.BlockInstrWriter;
import com.floweytf.jvmir.Instr;
import com.floweytf.jvmir.InstrNode;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnNode;

import java.util.Objects;

/**
 * Represents arithmetic addition operation between two values of the same type.
 */
public final class AddInstr extends Instr {
    private static final String[] OPERAND_NAMES = {"lhs", "rhs"};
    private final int opc;

    public AddInstr(Type type, int opc) {
        super(type);
        this.opc = opc;
    }

    @Override
    public void emit(BlockInstrWriter writer) {
        lhs().emit(writer);
        rhs().emit(writer);
        writer.write(new InsnNode(opc));
    }

    @Override
    protected void check() {
        assertOperandCount(2);
        typeAssert(Objects.equals(type(), rhs().type()), "expected %o0 to have type %t, but got %t0");
        typeAssert(Objects.equals(type(), rhs().type()), "expected %o1 to have type %t, but got %t1");
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
    public InstrNode lhs() {
        return getOperand(0);
    }

    /**
     * Obtains the right-hand side operand of this instruction
     *
     * @return The RHS instruction
     */
    public InstrNode rhs() {
        return getOperand(1);
    }
}
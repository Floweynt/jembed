package com.floweytf.jvmir.instrs;

import com.floweytf.jvmir.Instr;
import com.floweytf.jvmir.InstructionDAG;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.Objects;

/**
 * Represents arithmetic addition operation between two values of the same type.
 *
 * @see InstructionDAG#makeIAdd(Instr, Instr)
 * @see InstructionDAG#makeLAdd(Instr, Instr)
 * @see InstructionDAG#makeFAdd(Instr, Instr)
 * @see InstructionDAG#makeDAdd(Instr, Instr)
 */
public final class StLoc extends Instr {
    private final int opc;
    private static final String[] OPERAND_NAMES = {"lhs", "rhs"};

    /**
     * Constructs an add instruction.
     *
     * @apiNote Internal
     * @see InstructionDAG#makeIAdd(Instr, Instr)
     * @see InstructionDAG#makeLAdd(Instr, Instr)
     * @see InstructionDAG#makeFAdd(Instr, Instr)
     * @see InstructionDAG#makeDAdd(Instr, Instr)
     */
    @ApiStatus.Internal
    public StLoc(InstructionDAG dag, int opc, Type type, Instr lhs, Instr rhs) {
        super(dag, type, lhs, rhs);
        this.opc = opc;
    }

    @Override
    public void emit(MethodVisitor writer) {
        lhs().emit(writer);
        rhs().emit(writer);
        writer.visitInsn(opc);
    }

    @Override
    public void typeCheck() {
        super.typeCheck();
        var lhsT = lhs().type();
        var rhsT = rhs().type();
        if (lhsT == null || rhsT == null)
            throw new IllegalStateException("Add instruction only takes typed operands");

        if (!Objects.equals(type(), lhsT) || !Objects.equals(type(), rhsT)) {
            final var typename = type().toString();

            throw new IllegalStateException(
                String.format(
                    "Add instruction expected (%1$s, %1$s), but got (%2$s, %3$s)",
                    typename,
                    lhsT,
                    rhsT
                )
            );
        }
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
    public Instr lhs() {
        return getOperand(0);
    }

    /**
     * Obtains the right-hand side operand of this instruction
     *
     * @return The RHS instruction
     */
    public Instr rhs() {
        return getOperand(1);
    }
}
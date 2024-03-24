package com.floweytf.jvmir.instrs;

import com.floweytf.jvmir.Instr;
import com.floweytf.jvmir.InstructionDAG;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;

/**
 * Represents arithmetic addition operation between two values of the same type.
 *
 * @see InstructionDAG#makeConst(int)
 */
public final class ImmI32Instr extends Instr {
    private static final List<String> IMM_NAMES = List.of("value");
    private final int value;

    /**
     * Constructs an 32-bit integer (int) constant load instruction.
     *
     * @apiNote Internal
     * @see InstructionDAG#makeConst(int)
     */
    @ApiStatus.Internal
    public ImmI32Instr(InstructionDAG dag, int value) {
        super(dag, Type.INT_TYPE);
        this.value = value;
    }

    @Override
    public void emit(MethodVisitor writer) {
        // writer.visitIntInsn(Opcodes.LDC);
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Object> getImmOperands() {
        return List.of(value);
    }

    @Override
    public List<String> getImmNames() {
        return IMM_NAMES;
    }

    /**
     * @return The value to load.
     */
    public int value() {
        return value;
    }
}

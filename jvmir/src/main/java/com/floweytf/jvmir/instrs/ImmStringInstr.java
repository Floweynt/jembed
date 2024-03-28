package com.floweytf.jvmir.instrs;

import com.floweytf.jvmir.BlockInstrWriter;
import com.floweytf.jvmir.Instr;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.LdcInsnNode;

import java.util.List;

/**
 * Represents arithmetic addition operation between two values of the same type.
 */
public final class ImmStringInstr extends Instr {
    private static final List<String> IMM_NAMES = List.of("value");
    private final String value;

    public ImmStringInstr(String value) {
        super(Type.getType(String.class));
        this.value = value;
    }

    @Override
    public void emit(BlockInstrWriter writer) {
        writer.write(new LdcInsnNode(value));
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
    public String value() {
        return value;
    }
}
package com.floweytf.jvmir.instrs;

import com.floweytf.jvmir.BlockInstrWriter;
import com.floweytf.jvmir.Instr;
import com.floweytf.jvmir.InstrNode;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class InvokeMemberInstr extends Instr {
    private final static List<String> IMM_NAMES = List.of("method");

    private final Type calleeType;
    private final List<Type> argTypes;
    private final String signature;
    private final String name;

    public InvokeMemberInstr(Type calleeType, Type returnType, String name, List<Type> args) {
        super(returnType);
        this.calleeType = calleeType;
        this.argTypes = Collections.unmodifiableList(args);
        this.name = name;
        this.signature = calleeType.getDescriptor() + name + Type.getMethodDescriptor(returnType, argTypes.toArray(new Type[]{}));
    }

    @Override
    public List<String> getImmNames() {
        return IMM_NAMES;
    }

    @Override
    public List<Object> getImmOperands() {
        return List.of(signature);
    }

    @Override
    public void emit(BlockInstrWriter writer) {
        final var operands = Objects.requireNonNull(node).operands();
        operands.forEach(x -> x.emit(writer));
        writer.write(
            new MethodInsnNode(
                Opcodes.INVOKEVIRTUAL,
                calleeType.getInternalName(),
                name,
                Type.getMethodDescriptor(type(), argTypes.toArray(new Type[]{})),
                false
            )
        );
    }

    @Override
    public String getOperandName(int i) {
        if (i == 0)
            return "invokee";
        return Integer.toString(i - 1);
    }

    @Override
    protected void check() {
        assertOperandCount(1 + argTypes.size());
    }

    /**
     * Obtains the left-hand side operand of this instruction
     *
     * @return The LHS instruction
     */
    public InstrNode invokee() {
        return getOperand(0);
    }

    public Type calleeType() {
        return calleeType;
    }

    public List<Type> argTypes() {
        return argTypes;
    }

    public String signature() {
        return signature;
    }
}
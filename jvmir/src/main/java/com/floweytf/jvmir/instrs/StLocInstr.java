package com.floweytf.jvmir.instrs;

import com.floweytf.jvmir.BlockInstrWriter;
import com.floweytf.jvmir.Instr;
import com.floweytf.jvmir.InstrNode;
import com.floweytf.jvmir.LocalRef;

import java.util.List;
import java.util.Objects;

public final class StLocInstr extends Instr {
    private static final List<String> IMM_NAMES = List.of("local");

    private final LocalRef local;

    public StLocInstr(LocalRef local) {
        super(local.type());
        this.local = local;
    }

    public static InstrNode store(LocalRef local, InstrNode value) {
        return new InstrNode(List.of(value), new StLocInstr(local));
    }

    @Override
    public List<Object> getImmOperands() {
        return List.of(local);
    }

    @Override
    public List<String> getImmNames() {
        return IMM_NAMES;
    }

    @Override
    public String getOperandName(int i) {
        return "value";
    }

    @Override
    public void emit(BlockInstrWriter writer) {

    }

    @Override
    protected void check() {
        assertOperandCount(1);
        typeAssert(Objects.equals(local.type(), value().type()), "expected %o0 to have type %t, but got %t0");
    }

    public InstrNode value() {
        return getOperand(0);
    }

    public LocalRef local() {
        return local;
    }
}
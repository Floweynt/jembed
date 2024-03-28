package com.floweytf.jvmir.instrs;

import com.floweytf.jvmir.BlockInstrWriter;
import com.floweytf.jvmir.Instr;
import com.floweytf.jvmir.LocalRef;

import java.util.List;

public class LdLocInstr extends Instr {
    private static final List<String> IMM_NAMES = List.of("local");

    private final LocalRef local;

    public LdLocInstr(LocalRef local) {
        super(local.type());
        this.local = local;
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
    public void emit(BlockInstrWriter writer) {

    }

    @Override
    protected void check() {
        assertOperandCount(0);
    }

    public LocalRef local() {
        return local;
    }
}
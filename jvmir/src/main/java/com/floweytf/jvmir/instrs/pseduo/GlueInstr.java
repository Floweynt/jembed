package com.floweytf.jvmir.instrs.pseduo;

import com.floweytf.jvmir.InstructionDAG;
import com.floweytf.jvmir.Instr;
import com.floweytf.jvmir.pass.legalize.ISideEffectChain;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.MethodVisitor;

import java.util.function.Consumer;

/**
 * Represents a group of sequentially executed instructions
 *
 * @see InstructionDAG#makeGlue(Instr...)
 */
public final class GlueInstr extends Instr implements ISideEffectChain {

    /**
     * Constructs a glue instruction.
     *
     * @apiNote Internal
     * @see InstructionDAG#makeGlue(Instr...)
     */
    @ApiStatus.Internal
    public GlueInstr(InstructionDAG dag) {
        super(dag, null);
    }

    @Override
    public void emit(MethodVisitor writer) {
        operandsStream().forEach(child -> child.emit(writer));
    }

    @Override
    public void typeCheck() {
        super.typeCheck();

        for (int i = 0; i < operandsCount(); i++) {
            typeAssert(getOperand(i).type() == null, "expected %o" + i + " to have no type");
        }
    }

    @Override
    public void addOperand(Instr node) {
        super.addOperand(node);
    }

    @Override
    public void addOperand(int i, Instr node) {
        super.addOperand(i, node);
    }

    @Override
    public void forEach(Consumer<Instr> consumer) {
        operandsStream().forEach(consumer);
    }
}
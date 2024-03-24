package com.floweytf.jvmir;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class InstrNode<T extends Instr> {
    @Nullable
    private T instr;
    protected final List<InstrNode<Instr>> operands;
    private boolean isFrozen = false;

    private void ensureMutable() {
        if(isFrozen)
            throw new UnsupportedOperationException("cannot modify frozen instruction!");
    }

    InstrNode(List<InstrNode<Instr>> operands, @Nullable T instr) {
        this.operands = new ArrayList<>(operands);
        this.instr = instr;
    }

    public Instr replaceInstr(Instr other) {
        ensureMutable();
    }

    /**
     * Freezes the node (and all children), preventing all mutations.
     */
    public final void freeze() {
        isFrozen = true;
    }

    // getters and stuff

    /**
     * Obtains the amount of nodes that refer to this one.
     *
     * @return The amount of nodes that refer to this one.
     */
    public final int refCount() {
        return ref != null ? ref.refCount : 0;
    }

    /**
     * Obtains the i-th operand (zero index).
     *
     * @param i The index of the operand. This index matches the constructor parameter.
     * @return The operand node.
     */
    public final T getOperand(int i) {
        return operands.get(i).node();
    }

    /**
     * Sets the i-th operand (zero index).
     *
     * @param i    The index of the operand. This index matches the constructor parameter.
     * @param node The new node
     */
    public final void setOperand(int i, T node) {
        ensureMutable();

        operands.get(i).unbindRef();
        var ref = ((InstrNode<T>) node).getOrCreateRef();
        ref.bindRef();
        operands.set(i, ref);
    }

    /**
     * Adds an operand to the list of operands at a specified index.
     *
     * @param i    The index to insert this operand
     * @param node The node to insert. See {@link List#add(int, Object)}.
     */
    protected void addOperand(int i, T node) {
        var ref = ((InstrNode<T>) node).getOrCreateRef();
        ref.bindRef();
        operands.add(i, ref);
    }

    /**
     * Adds an operand to the end of the list of operands.
     *
     * @param node The node to insert. See {@link List#add(Object)}.
     */
    protected void addOperand(T node) {
        var ref = ((InstrNode<T>) node).getOrCreateRef();
        ref.bindRef();
        operands.add(ref);
    }

    /**
     * Obtains the amount of operand/child nodes this node has.
     *
     * @return The amount of operand nodes.
     */
    public final int operandsCount() {
        return operands.size();
    }

    /**
     * Obtains a stream (in sequence) of all the operands of this node.
     *
     * @return The stream of operands/nodes.
     */
    public final Stream<T> operandsStream() {
        return operands.stream().map(u -> u.node);
    }
}
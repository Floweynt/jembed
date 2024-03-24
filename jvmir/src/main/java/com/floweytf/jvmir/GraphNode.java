package com.floweytf.jvmir;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a node in a graph
 * @param <T> The subclass type, for CRTP
 */
@SuppressWarnings("unchecked") // Java CRTP is fun!
public class GraphNode<T extends GraphNode<T>> {
    protected class Ref {
        private int refCount;
        private T node;

        private Ref(T node) {
            this.refCount = 0;
            this.node = node;
        }

        private void bindRef() {
            refCount++;
        }

        private void unbindRef() {
            refCount--;
            if (refCount < 0) {
                throw new IllegalStateException();
            }
        }

        public T node() {
            return node;
        }

    }

    @Nullable
    private Ref ref;
    protected final List<Ref> operands;
    private boolean isFrozen = false;

    // random helper methods
    private Ref getOrCreateRef() {
        if (ref == null) {
            this.ref = new Ref((T) this);
        }

        return this.ref;
    }

    protected void ensureMutable() {
        if (isFrozen) {
            throw new IllegalStateException("cannot mutate frozen AST");
        }
    }

    protected GraphNode(Stream<T> operands) {
        this.operands = operands.map(op -> {
            var ref = ((GraphNode<T>) op).getOrCreateRef();
            ref.bindRef();
            return ref;
        }).collect(Collectors.toList());
    }

    /**
     * Replaces the current graph node with a different one.
     * This method allows the mutation of the current node to a completely different node, while
     * preserving references. Since this method mutates the node, it must be mutable.
     *
     * @param other Another node
     * @return The new node.
     */
    public T replace(T other) {
        ensureMutable();

        if (other == this) {
            return (T) this;
        }

        final var o = ((GraphNode<T>) other);

        if(o.ref != null) {
            throw new IllegalArgumentException("replaced node must be fresh");
        }

        // fix ref pointers
        if (this.ref != null) {
            this.ref.node = other;
        }

        o.ref = this.ref;

        // unbind children ref
        this.operands.forEach(Ref::unbindRef);

        // delete our ref
        this.ref = null;

        return other;
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
        var ref = ((GraphNode<T>) node).getOrCreateRef();
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
        var ref = ((GraphNode<T>) node).getOrCreateRef();
        ref.bindRef();
        operands.add(i, ref);
    }

    /**
     * Adds an operand to the end of the list of operands.
     *
     * @param node The node to insert. See {@link List#add(Object)}.
     */
    protected void addOperand(T node) {
        var ref = ((GraphNode<T>) node).getOrCreateRef();
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
package com.floweytf.jvmir;

import com.floweytf.jvmir.instrs.*;
import com.floweytf.jvmir.instrs.cf.Branch;
import com.floweytf.jvmir.instrs.pseduo.DiscardInstr;
import com.floweytf.jvmir.instrs.pseduo.GlueInstr;
import com.floweytf.jvmir.instrs.pseduo.PhiInstr;
import com.floweytf.jvmir.util.GraphEmitter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.*;
import java.util.function.Consumer;

/**
 * A direct acyclic graph that represents a method/function.
 * Overcomplicated LLVM inspire backend setup? It's more likely than you think!
 */
public final class InstructionDAG {
    private final GlueInstr root = new GlueInstr(this);
    private final List<Type> locals = new ArrayList<>();

    public InstructionDAG() {

    }

    // Instruction creation

    /**
     * Provides a constant 32-bit integer (int).
     *
     * @param value The integer to load.
     * @return The instruction that represents the constant.
     */
    public ImmI32Instr makeConst(int value) {
        return new ImmI32Instr(this, value);
    }

    /**
     * Constructs a glue instruction for serializing side effects.
     *
     * @param values The instructions to execute, in order.
     * @return The glue instruction.
     */
    public GlueInstr makeGlue(Instr... values) {
        var glue = new GlueInstr(this);
        for (var operand : values)
            glue.addOperand(operand);
        return glue;
    }

    /**
     * Creates a phi instruction that selects a branch to execute, and returns that value.
     *
     * @param predicate  The predicate, selects {@code trueValue} if true and {@code falseValue} if false. Must be boolean.
     * @param type       The return type of the predicate.
     * @param trueValue  The value to return if the {@code predicate} is true
     * @param falseValue The value to return if the {@code predicate} is false
     * @return The value selected by {@code predicate}.
     */
    public PhiInstr makePhi(Instr predicate, Type type, Instr trueValue, Instr falseValue) {
        return new PhiInstr(this, type, predicate, trueValue, falseValue);
    }

    public DiscardInstr makeDiscard(Instr value) {
        return new DiscardInstr(this, value);
    }

    /**
     * Constructs 32-bit integer (int) addition between two int operands.
     *
     * @param lhs The left-hand side of the addition. Must be an int.
     * @param rhs The right-hand side of the addition. Must be an int.
     * @return The instruction that adds the left and right hand sides. Is an int.
     * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-6.html#jvms-6.5.iadd">JVM iadd instruction</a>
     */
    public AddInstr makeIAdd(Instr lhs, Instr rhs) {
        return new AddInstr(this, Opcodes.IADD, Type.INT_TYPE, lhs, rhs);
    }

    /**
     * Constructs 64-bit integer (long) addition between two long operands.
     *
     * @param lhs The left-hand side of the addition. Must be a long.
     * @param rhs The right-hand side of the addition. Must be a long.
     * @return The instruction that adds the left and right hand sides. Is a long.
     * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-6.html#jvms-6.5.ladd">JVM ladd instruction</a>
     */
    public AddInstr makeLAdd(Instr lhs, Instr rhs) {
        return new AddInstr(this, Opcodes.LADD, Type.LONG_TYPE, lhs, rhs);
    }

    /**
     * Constructs 32-bit floating point (float) addition between two float operands.
     *
     * @param lhs The left-hand side of the addition. Must be a float.
     * @param rhs The right-hand side of the addition. Must be a float.
     * @return The instruction that adds the left and right hand sides. Is a float.
     * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-6.html#jvms-6.5.fadd">JVM fadd instruction</a>
     */
    public AddInstr makeFAdd(Instr lhs, Instr rhs) {
        return new AddInstr(this, Opcodes.FADD, Type.FLOAT_TYPE, lhs, rhs);
    }

    /**
     * Constructs 64-bit floating point (double) addition between two double operands.
     *
     * @param lhs The left-hand side of the addition. Must be a double.
     * @param rhs The right-hand side of the addition. Must be a double.
     * @return The instruction that adds the left and right hand sides. Is a double.
     * @see <a href="https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-6.html#jvms-6.5.dadd">JVM dadd instruction</a>
     */
    public AddInstr makeDAdd(Instr lhs, Instr rhs) {
        return new AddInstr(this, Opcodes.DADD, Type.DOUBLE_TYPE, lhs, rhs);
    }

    public Branch makeBranch(Instr target) {
        return new Branch(this, target);

    }

    /**
     * Runs a BFS on all the nodes of the DAG.
     *
     * @param instrConsumer The action to run for each instruction node
     */
    public void iterateNodes(Consumer<Instr> instrConsumer) {
        final var nodeQueue = new ArrayDeque<Instr>();
        nodeQueue.add(root);

        while (!nodeQueue.isEmpty()) {
            final var size = nodeQueue.size();
            for (int i = 0; i < size; i++) {
                final var node = nodeQueue.pollFirst();
                if (node == null)
                    throw new IllegalStateException();

                // accept node
                instrConsumer.accept(node);

                node.operandsStream().forEach(nodeQueue::push);
            }
        }
    }

    /**
     * Emits the current DAG as a dot graph.
     *
     * @param output The stream to emit dot source to
     */
    public void printDagDot(GraphEmitter output) {
        iterateNodes(instr -> {
            output.emitNode(instr);

            for (int i = 0; i < instr.operandsCount(); i++) {
                output.emitEdge(instr, i + ":n", instr.getOperand(i), GraphEmitter.EdgeKind.SOLID);
            }

            final var imms = instr.getImmOperands();
            for (int i = 0; i < imms.size(); i++) {
                final var imm = imms.get(i);
                if (imm instanceof Instr target) {
                    output.emitEdge(instr, "i" + i, target, GraphEmitter.EdgeKind.DASHED);
                }
            }
        });
        output.done(root);
    }

    /**
     * Ensures the DAG is actually a DAG.
     * If not, throws {@link IllegalStateException}.
     */
    public void validateDag() {
        final var v = new HashSet<Instr>();
        iterateNodes(x -> {
            if (v.contains(x))
                throw new IllegalStateException("cycle not permitted in dag");
            v.add(x);
        });
    }

    // Getters and setters

    /**
     * Obtains the root node.
     *
     * @return The root glue node representing this function.
     */
    public GlueInstr getRoot() {
        return root;
    }

    public Local defineLocal(Type type) {
        final int id = locals.size();
        locals.add(type);
        return new Local(this, id, type);
    }

    public Local getLocal(int i) {
        return new Local(this, i, locals.get(i));
    }
}
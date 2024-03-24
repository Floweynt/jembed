package com.floweytf.jvmir;

import com.floweytf.jvmir.util.Utils;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// instruction node base class

/**
 * Represents a node in an instruction DAG that represents an individual instruction.
 * Note: this may not necessary compile down to one JVM instruction
 *
 * @see InstructionDAG
 */
public abstract class Instr extends GraphNode<Instr> {
    private static final Pattern OPERAND_TYPE_PATTERN = Pattern.compile("%([to])(\\d*)");
    private final InstructionDAG dag;
    private final Type type;

    /**
     * Emit instructions to a method.
     *
     * @param writer The body of the method this instruction belongs to
     */
    public abstract void emit(MethodVisitor writer);

    /**
     * Check to see if the type is well-formed for this instruction.
     */
    public void typeCheck() {
        operandsStream().forEach(Instr::typeCheck);
    }

    protected void typeAssert(boolean expr, String fmt) {
        if (!expr) {
            StringBuilder builder = new StringBuilder();
            builder.append(name()).append(": ");
            Matcher matcher = OPERAND_TYPE_PATTERN.matcher(fmt);

            while (matcher.find()) {
                final var kind = matcher.group(1);
                final var str = matcher.group(2);
                switch (kind) {
                case "t" -> {
                    final var fmtType = str.isEmpty() ? type : getOperand(Integer.parseInt(str)).type();

                    final var typeName = Utils.coalesce(
                        Utils.mapNull(fmtType, Type::toString),
                        () -> "[none]"
                    );

                    matcher.appendReplacement(builder, "`" + typeName + "`");
                }
                case "o" -> {
                    final var operandName = Utils.coalesce(
                        getOperandName(Integer.parseInt(str)),
                        () -> str
                    );

                    matcher.appendReplacement(builder, "`op:" + operandName + "`");
                }
                }
            }
            matcher.appendTail(builder);

            throw new IllegalStateException(builder.toString());
        }
    }

    protected Instr(InstructionDAG dag, Type type, Instr... operands) {
        super(Arrays.stream(operands));
        this.type = type;
        this.dag = dag;
    }

    protected Instr(InstructionDAG dag, Type type, List<Instr> operands) {
        super(operands.stream());
        this.type = type;
        this.dag = dag;
    }

    /**
     * Replaces the current instruction node with a different one.
     * This method allows the mutation of the current instruction to a completely different instruction, while
     * preserving references. Since this method mutates the DAG, it must be mutable.
     *
     * @param other Another instruction that belongs to the same DAG
     * @return The new node.
     */
    public final Instr replace(Instr other) {
        if (other.dag != this.dag) {
            throw new IllegalArgumentException("other must belong to the same dag");
        }

        return super.replace(other);
    }

    /**
     * Obtains the name (for printing/debug purpose) of the ith operand.
     *
     * @param i The index of the operand
     * @return The name of the operand, or null if it is unnamed.
     */
    public @Nullable String getOperandName(int i) {
        return null;
    }

    /**
     * Gets the DAG this instruction node belongs to.
     *
     * @return The DAG.
     */
    public final InstructionDAG dag() {
        return dag;
    }

    /**
     * Obtains the type (integer, object, etc.) annotation belonging to this instruction.
     *
     * @return The type of this instruction.
     */
    public final @Nullable Type type() {
        return type;
    }

    /**
     * Returns a string that describes the name of this instruction.
     *
     * @return The name of this instruction.
     */
    public String name() {
        return getClass().getSimpleName();
    }

    /**
     * Returns the immediate arguments of this instruction.
     *
     * @return An immutable list of the immediate operands.
     */
    public List<Object> getImmOperands() {
        return List.of();
    }

    /**
     * Returns a list of the names of each immediate.
     *
     * @return An immutable list of the immediate operand names.
     */
    public List<String> getImmNames() {
        return List.of();
    }
}
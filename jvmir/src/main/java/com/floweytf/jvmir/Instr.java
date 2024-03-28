package com.floweytf.jvmir;

import com.floweytf.jvmir.util.Utils;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Instr {
    private static final Pattern OPERAND_TYPE_PATTERN = Pattern.compile("%([to])(\\d*)");
    private final Type type;
    @Nullable
    protected InstrNode node;
    private final boolean isRoot;

    protected Instr(Type type) {
        this.type = type;
        isRoot = false;
    }

    protected Instr(Type type, boolean isRoot) {
        this.type = type;
        this.isRoot = isRoot;
    }

    protected final void typeAssert(boolean expr, String fmt) {
        if (!expr) {
            StringBuilder builder = new StringBuilder();
            builder.append(name()).append(": ");
            Matcher matcher = OPERAND_TYPE_PATTERN.matcher(fmt);

            while (matcher.find()) {
                final var kind = matcher.group(1);
                final var str = matcher.group(2);
                switch (kind) {
                case "t" -> {
                    final var fmtType = str.isEmpty() ?
                        type : Objects.requireNonNull(getOperand(Integer.parseInt(str)).instr()).type();

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

    protected final void assertOperandCount(int count) {
        final var operandCount = Objects.requireNonNull(node).operands().size();
        if (operandCount != count) {
            throw new IllegalStateException(String.format("%s: expected %d operands but got %d", name(), count, operandCount));
        }
    }

    protected final InstrNode getOperand(int i) {
        return Objects.requireNonNull(node).getOperand(i);
    }

    public final @Nullable Type type() {
        return type;
    }

    public final void runChecks() {
        if (node.instr() != this) {
            throw new IllegalStateException("Instr <-> InstrNode link failure!");
        }

        check();
    }

    public boolean isRoot() {
        return isRoot;
    }

    // points of extension
    public @Nullable String getOperandName(int i) {
        return null;
    }

    public String name() {
        return getClass().getSimpleName();
    }

    public List<Object> getImmOperands() {
        return List.of();
    }

    public List<String> getImmNames() {
        return List.of();
    }

    public abstract void emit(BlockInstrWriter writer);

    protected void check() {
    }
}
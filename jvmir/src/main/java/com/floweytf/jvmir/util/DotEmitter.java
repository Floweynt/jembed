package com.floweytf.jvmir.util;

import com.floweytf.jvmir.InstrNode;
import com.floweytf.jvmir.LocalRef;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class DotEmitter implements GraphEmitter {
    private final StringBuilder builder = new StringBuilder();
    private final OutputStream output;

    public DotEmitter(OutputStream out) {
        output = out;
    }

    @Override
    public void emitEdge(InstrNode from, String port, InstrNode to, EdgeKind kind) {
        builder.append("node").append(to.hashCode()).append(":opc:s")
            .append(" -> ")
            .append("node").append(from.hashCode()).append(":").append(port);

        switch (kind) {
        case SOLID -> {
        }
        case DASHED -> builder.append(" [style=dotted]");
        case CHAIN -> builder.append(" [style=dotted,color=blue]");
        }

        builder.append("\n");
    }

    @Override
    public void emitNode(InstrNode instr, @Nullable String extraLabel) {
        final var name = "node" + instr.hashCode();
        builder.append(name).append(" [label=\"{");

        final var opCount = instr.operands().size();

        if (opCount != 0 || instr.chain() != null) {
            builder.append("{");

            if (instr.chain() != null) {
                builder.append("<chain>ch|");
            }

            for (int i = 0; i < opCount; i++) {
                var operandName = instr.getOperandName(i);
                if (operandName == null)
                    operandName = String.valueOf(i);

                builder.append("<").append(i).append(">").append(operandName);
                builder.append("|");
            }
            builder.setLength(builder.length() - 1);

            builder.append("}|");
        }

        // emit immediate operands
        final var imms = instr.getImmOperands();
        if (!imms.isEmpty()) {
            final var immNames = instr.getImmNames();

            for (int i = 0; i < imms.size(); i++) {
                final var imm = imms.get(i);
                final var immName = immNames.get(i);
                builder.append("<i").append(i).append(">[I]");
                if (imm instanceof InstrNode) {
                    builder.append(immName).append("|");
                } else if (imm instanceof LocalRef local) {
                    builder.append(immName).append(": #").append(local.index()).append("|");
                } else {
                    builder.append(immName).append(": ").append(imm).append("|");
                }
            }
        }

        if (extraLabel != null)
            builder.append(extraLabel).append("|");

        final var typeName = Utils.coalesce(
            Utils.mapNull(instr.type(), Type::toString),
            () -> "[none]"
        );

        builder.append("T:").append(typeName).append("|<opc>").append(instr.name()).append("}\"]\n");
    }

    @Override
    public void done(InstrNode root) {
        try {
            var writer = new OutputStreamWriter(output);

            writer.write("digraph G {\n");
            writer.write("node [shape=record,style=rounded]\nedge [dir=\"back\"]\n");
            writer.write(builder.toString());
            writer.write("node" + root.hashCode() + " -> InstrRoot [style=dotted]\n");
            writer.write("}\n");
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

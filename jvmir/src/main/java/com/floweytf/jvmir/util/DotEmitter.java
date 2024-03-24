package com.floweytf.jvmir.util;

import com.floweytf.jvmir.Instr;
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
    public void emitEdge(Instr from, String port, Instr to, EdgeKind kind) {
        builder.append("node").append(to.hashCode()).append(":opc:s")
            .append(" -> ")
            .append("node").append(from.hashCode()).append(":").append(port);

        switch (kind) {
        case SOLID -> {
        }
        case DASHED -> builder.append(" [style=dotted]");
        }

        builder.append("\n");
    }

    @Override
    public void emitNode(Instr instr) {
        final var name = "node" + instr.hashCode();
        builder.append(name).append(" [label=\"{");

        final var opCount = instr.operandsCount();

        if (opCount != 0) {
            builder.append("{");

            for (int i = 0; i < opCount; i++) {
                var operandName = instr.getOperandName(i);
                if (operandName == null)
                    operandName = String.valueOf(i);

                builder.append("<").append(i).append(">").append(operandName);

                if (i + 1 != opCount)
                    builder.append("|");
            }

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
                if (imm instanceof Instr) {
                    builder.append(immName).append("|");
                } else {
                    builder.append(immName).append(": ").append(imm).append("|");
                }
            }
        }

        final var typeName = Utils.coalesce(
            Utils.mapNull(instr.type(), Type::toString),
            () -> "[none]"
        );

        builder.append("T:").append(typeName).append("|<opc>").append(instr.name()).append("}\"]\n");
    }

    @Override
    public void done(Instr root) {
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

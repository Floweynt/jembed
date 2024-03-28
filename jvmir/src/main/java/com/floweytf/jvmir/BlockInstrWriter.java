package com.floweytf.jvmir;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class BlockInstrWriter {
    private final List<AbstractInsnNode> nodes = new ArrayList<>();

    public BlockInstrWriter() {
        nodes.add(new LabelNode());
    }

    public void write(AbstractInsnNode node) {
        nodes.add(node);
    }

    public Stream<AbstractInsnNode> getNodes() {
        return nodes.stream();
    }

    public LabelNode getLabel() {
        return (LabelNode) nodes.get(0);
    }
}

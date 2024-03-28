package com.floweytf.jvmir.util;

import com.floweytf.jvmir.InstrNode;
import org.jetbrains.annotations.Nullable;

public interface GraphEmitter {
    void emitEdge(InstrNode from, String port, InstrNode to, EdgeKind kind);

    void emitNode(InstrNode instr, @Nullable String extraLabel);

    void done(InstrNode root);

    enum EdgeKind {
        SOLID,
        DASHED,
        CHAIN
    }
}

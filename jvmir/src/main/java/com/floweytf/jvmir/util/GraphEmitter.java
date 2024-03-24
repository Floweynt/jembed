package com.floweytf.jvmir.util;

import com.floweytf.jvmir.Instr;

public interface GraphEmitter {
    enum EdgeKind {
        SOLID,
        DASHED
    }

    void emitEdge(Instr from, String port, Instr to, EdgeKind kind);
    void emitNode(Instr instr);
    void done(Instr root);
}

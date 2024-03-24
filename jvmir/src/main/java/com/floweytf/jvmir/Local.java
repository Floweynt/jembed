package com.floweytf.jvmir;

import org.objectweb.asm.Type;

public record Local(InstructionDAG dag, int index, Type type) {
}

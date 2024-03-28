package com.floweytf.jvmir;

import com.floweytf.jvmir.util.DAGUtils;
import com.floweytf.jvmir.util.Pair;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class CGMethod {
    final List<Type> locals = new ArrayList<>();
    final List<BlockDAG> dags = new ArrayList<>();

    public BlockDAG defineBlock() {
        var block = new BlockDAG(this);
        dags.add(block);
        return block;
    }

    public LocalRef defineLocal(Type type) {
        locals.add(type);
        return new LocalRef(this, locals.size() - 1);
    }

    public LocalRef getLocal(int index) {
        return new LocalRef(this, index);
    }

    public Stream<BlockDAG> getBlocks() {
        return dags.stream();
    }

    public void mapBlocks(Function<BlockDAG, BlockDAG> mapper) {
        dags.replaceAll(mapper::apply);
    }

    public void codeGen(MethodVisitor writer) {
    }
}

package com.floweytf.jvmir.util;

import com.floweytf.jvmir.InstrNode;
import com.floweytf.jvmir.BlockDAG;

import java.util.*;
import java.util.function.Consumer;

public class DAGUtils {
    public static Map<InstrNode, Integer> instrRefCountInfo(BlockDAG dag) {
        final var ret = new HashMap<InstrNode, Integer>();
        dag.visitNodes(node -> {
            if (Objects.requireNonNull(node.instr()).isRoot())
                return;

            node.operands().forEach(dep -> ret.put(dep, ret.getOrDefault(dep, 0) + 1));
        });
        return Collections.unmodifiableMap(ret);
    }

    /**
     * Emits the current DAG as a dot graph.
     *
     * @param output The stream to emit dot source to
     */
    public static void printDagDot(BlockDAG dag, GraphEmitter output, boolean refCounts) {
        final var ref = refCounts ? instrRefCountInfo(dag) : null;
        dag.visitNodes(instr -> {
            if (refCounts) {
                output.emitNode(instr, "rc: " + ref.getOrDefault(instr, 0));
            } else {
                output.emitNode(instr, null);
            }

            if (instr.chain() != null) {
                output.emitEdge(instr, "chain", instr.chain(), GraphEmitter.EdgeKind.CHAIN);
            }

            for (int i = 0; i < instr.operands().size(); i++) {
                output.emitEdge(
                    instr, i + ":n", instr.getOperand(i),
                    Objects.requireNonNull(instr.instr()).isRoot() ? GraphEmitter.EdgeKind.CHAIN : GraphEmitter.EdgeKind.SOLID
                );
            }

            final var imms = instr.getImmOperands();
            for (int i = 0; i < imms.size(); i++) {
                final var imm = imms.get(i);
                if (imm instanceof InstrNode target) {
                    output.emitEdge(instr, "i" + i, target, GraphEmitter.EdgeKind.DASHED);
                }
            }
        });

        output.done(dag.root());
    }

    public static void validateDAG(BlockDAG dag) {
        dag.visitNodes(InstrNode::runChecks);
    }

    public static void visitNodes(InstrNode root, Consumer<InstrNode> instrConsumer) {
        final var nodeQueue = new ArrayDeque<InstrNode>();
        nodeQueue.add(root);
        final var visited = new HashSet<>();

        while (!nodeQueue.isEmpty()) {
            final var size = nodeQueue.size();
            for (int i = 0; i < size; i++) {
                final var node = nodeQueue.pollFirst();
                if (node == null)
                    throw new IllegalStateException();
                if (visited.contains(node))
                    continue;
                visited.add(node);

                // accept node
                instrConsumer.accept(node);

                if (node.chain() != null) {
                    nodeQueue.push(node.chain());
                }
                node.operands().forEach(nodeQueue::push);
            }
        }
    }

}
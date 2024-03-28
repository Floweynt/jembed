package com.floweytf.jvmir.pass.legalize;

import com.floweytf.jvmir.InstrNode;
import com.floweytf.jvmir.BlockDAG;
import com.floweytf.jvmir.LocalRef;
import com.floweytf.jvmir.instrs.StLocInstr;
import com.floweytf.jvmir.pass.Pass;
import com.floweytf.jvmir.util.DAGUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeifyPass extends Pass<BlockDAG, BlockDAG> {
    @Override
    public BlockDAG apply(BlockDAG value) {
        class Applicator {
            private final Map<InstrNode, Integer> refCounts;
            private final Map<InstrNode, LocalRef> nodeToLocal = new HashMap<>();
            private final BlockDAG dag;

            private Applicator(BlockDAG dag) {
                refCounts = DAGUtils.instrRefCountInfo(dag);
                this.dag = dag;
            }

            private void visit(InstrNode parent, int index, InstrNode instr) {
                if (refCounts.getOrDefault(instr, 0) > 1) {
                    final var node = nodeToLocal.computeIfAbsent(instr, (ignored) -> {
                        var local = dag.method().defineLocal(instr.type());
                        StLocInstr.store(local, instr);
                        return local;
                    });

                    parent.setOperand(index, dag.loadLocal(node));
                }
            }
        }

        final var util = new Applicator(value);

        value.visitNodesOrdered(instr -> {
            for (int i = 0; i < instr.operands().size(); i++) {
                util.visit(instr, i, instr.operands().get(i));
            }
        });

        return value;
    }

    @Override
    public List<String> getPassNames() {
        return List.of("legalize:EnforceStackMachineTree");
    }
}

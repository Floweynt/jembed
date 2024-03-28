package com.floweytf.jvmir.pass.legalize;

import com.floweytf.jvmir.BlockDAG;
import com.floweytf.jvmir.CGMethod;
import com.floweytf.jvmir.InstrNode;
import com.floweytf.jvmir.pass.Pass;
import com.floweytf.jvmir.util.DAGUtils;

import java.util.*;

/**
 * Removes all chains from a CGMethod, opting to replace with multiple DAGs that are executed in order
 */
public class DagSplitPass extends Pass<CGMethod, CGMethod> {
    private static List<InstrNode> topoSort(List<InstrNode> nodes, HashMap<InstrNode, List<InstrNode>> adj) {
        var refCount = new HashMap<InstrNode, Integer>();

        for (var i : nodes) {
            for (var it : adj.get(i)) {
                refCount.put(it, refCount.getOrDefault(it, 0) + 1);
            }
        }

        List<InstrNode> ans = new ArrayList<>();
        Queue<InstrNode> qrr = new LinkedList<>();

        for (var i : nodes) {
            if (refCount.get(i) == 0) {
                qrr.add(i);
            }
        }

        while (!qrr.isEmpty()) {
            var node = qrr.poll();
            ans.add(node);

            for (var it : adj.get(node)) {
                refCount.put(it, refCount.get(it) - 1);

                if (refCount.get(it) == 0) {
                    qrr.add(it);
                }
            }
        }

        return ans;
    }

    @Override
    public CGMethod apply(CGMethod value) {
        final var newMethod = new CGMethod();
        value.getBlocks().flatMap(block -> {
            // Obtain all root nodes (nodes that only perform an effect and don't have value (nodes that are only referenced via chain)
            final var roots = DAGUtils.instrRefCountInfo(block).entrySet().stream()
                .filter(p -> p.getValue() == 0)
                .map(Map.Entry::getKey)
                .toList();

            // For each of such nodes, we construct a mapping from child -> root
            final var subtreeNodeToRoot = new HashMap<InstrNode, InstrNode>();
            roots.forEach(root -> DAGUtils.visitNodes(root, child -> subtreeNodeToRoot.put(child, root)));

            // Construct tree<->tree dependencies with
            final var chains = new HashMap<InstrNode, List<InstrNode>>();

            block.visitNodes(node -> {
                if (node.chain() != null) {
                    chains.computeIfAbsent(subtreeNodeToRoot.get(node), x -> new ArrayList<>()).add(subtreeNodeToRoot.get(node.chain()));
                }
            });

            // clear all chains, we don't need them anymore
            block.visitNodes(x -> {
                x.chain(null);
            });

            // topologically sort stuff
            var rootsSorted = topoSort(roots, chains);

            // We no longer have any chains (except for root chains)
            // The
            newMethod.


        });
    }

    @Override
    public List<String> getPassNames() {
        return List.of("legalize:SplitChainDAG");
    }
}

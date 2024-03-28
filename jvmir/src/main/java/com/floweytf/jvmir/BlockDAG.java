package com.floweytf.jvmir;

import com.floweytf.jvmir.instrs.*;
import com.floweytf.jvmir.instrs.pseduo.NopInstr;
import com.floweytf.jvmir.instrs.pseduo.PhiInstr;
import com.floweytf.jvmir.util.Cache;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A direct acyclic graph that represents a method/function.
 * Overcomplicated LLVM inspire backend setup? It's more likely than you think!
 */
public final class BlockDAG {
    private final Map<Integer, InstrNode> localChains = new HashMap<>();
    private final CGMethod method;
    @Nullable
    private InstrNode memoryChain = null;
    private final Cache<InstrNode> root = new Cache<>(
        () -> new InstrNode(Stream.concat(
            Stream.of(memoryChain),
            localChains.values().stream()
        ).filter(Objects::nonNull).toList(),
            new Instr(null, true) {
                @Override
                public void emit(BlockInstrWriter writer) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public @Nullable String getOperandName(int i) {
                    if (i == 0)
                        return "M";
                    return null;
                }

                @Override
                public String name() {
                    return "DAGChainRoot";
                }
            }
        )
    );

    BlockDAG(CGMethod method) {
        this.method = method;
    }

    private InstrNode addToMChain(InstrNode instr) {
        instr.chain(memoryChain);
        memoryChain = instr;
        root.markDirty();
        return instr;
    }

    private InstrNode addToLChain(LocalRef local, InstrNode instr) {
        if (local.method() != method()) {
            throw new IllegalArgumentException("local must belong to the same method as the dag");
        }

        instr.chain(localChain(local));
        localChains.put(local.index(), instr);
        root.markDirty();
        return instr;
    }

    // Iteration

    /**
     * Runs a BFS on all the nodes of the DAG.
     *
     * @param instrConsumer The action to run for each instruction node
     */
    public void visitNodes(Consumer<InstrNode> instrConsumer) {
        final var nodeQueue = new ArrayDeque<InstrNode>();
        nodeQueue.add(root());
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

    public void visitNodesOrdered(Consumer<InstrNode> instrConsumer) {
        class TopoSortHelper {
            private final Set<InstrNode> visited = new HashSet<>();
            private final List<InstrNode> res = new ArrayList<>();

            private void dfs(InstrNode node) {
                visited.add(node);

                node.dependants().forEach(x -> {
                    if (!visited.contains(x))
                        dfs(x);
                });

                res.add(node);
            }

            private List<InstrNode> run(InstrNode node) {
                dfs(node);
                return res;
            }
        }

        new TopoSortHelper().run(root()).forEach(instrConsumer);
    }

    // Getters and setters

    /**
     * Obtains the root node.
     *
     * @return The root glue node representing this function.
     */
    public InstrNode root() {
        return root.get();
    }

    public InstrNode memoryChain() {
        return memoryChain;
    }

    public InstrNode localChain(LocalRef local) {
        return localChains.get(local.index());
    }

    public CGMethod method() {
        return method;
    }

    // Builders

    public InstrNode nop() {
        return new InstrNode(List.of(), new NopInstr());
    }

    public InstrNode phi(Type type, InstrNode cond, InstrNode brT, InstrNode brF) {
        return new InstrNode(List.of(cond, brT, brF), new PhiInstr(type));
    }

    public InstrNode intAdd(InstrNode left, InstrNode right) {
        return new InstrNode(List.of(left, right), new AddInstr(Type.INT_TYPE, Opcodes.IADD));
    }

    public InstrNode longAdd(InstrNode left, InstrNode right) {
        return new InstrNode(List.of(left, right), new AddInstr(Type.LONG_TYPE, Opcodes.LADD));
    }

    public InstrNode floatAdd(InstrNode left, InstrNode right) {
        return new InstrNode(List.of(left, right), new AddInstr(Type.FLOAT_TYPE, Opcodes.FADD));
    }

    public InstrNode doubleAdd(InstrNode left, InstrNode right) {
        return new InstrNode(List.of(left, right), new AddInstr(Type.DOUBLE_TYPE, Opcodes.DADD));
    }

    public InstrNode immediate(int value) {
        return new InstrNode(List.of(), new ImmI32Instr(value));
    }

    public InstrNode immediate(String value) {
        return new InstrNode(List.of(), new ImmStringInstr(value));
    }

    public InstrNode invokeMember(String name, Type returnType, InstrNode invokee, InstrNode... args) {
        return addToMChain(
            new InstrNode(
                Stream.concat(Stream.of(invokee), Arrays.stream(args)).toList(),
                new InvokeMemberInstr(
                    Objects.requireNonNull(invokee.type()),
                    returnType,
                    name,
                    Arrays.stream(args).map(arg -> Objects.requireNonNull(arg.type())).toList()
                )
            )
        );
    }

    public InstrNode loadLocal(LocalRef local) {
        return addToLChain(local, new InstrNode(List.of(), new LdLocInstr(local)));
    }

    public InstrNode storeLocal(LocalRef local, InstrNode value) {
        return addToLChain(local, new InstrNode(List.of(value), new StLocInstr(local)));
    }
}
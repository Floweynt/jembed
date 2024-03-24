package com.floweytf.jembed.lang.sema;

import com.floweytf.jembed.lang.sema.ctx.SemanticContext;
import com.floweytf.jembed.lang.source.CodeRange;
import com.floweytf.jembed.lang.source.TranslationUnit;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// blazingly fast overload resolution!11!!!11
public class OverloadedMethod extends Type {
    private final List<Overload> overloads = new ArrayList<>();

    public OverloadedMethod() {
        super(null);
    }

    @Override
    public String name() {
        throw new UnsupportedOperationException("pseudo-type `#overload-method` has no name");
    }

    // overload adding
    public void addOverload(Overload overload) {
        overloads.add(overload);
    }

    public @Nullable Overload getOverload(List<Type> args, SemanticContext context) {
        for (var overload : overloads) {
            if (overload.args().size() != args.size())
                continue;

            boolean flag = false;
            for (int i = 0; i < args.size(); i++) {
                if (!context.typeManager().canCoerce(args.get(i), overload.args.get(i))) {
                    flag = true;
                    break;
                }
            }

            if (flag) {
                continue;
            }

            return overload;
        }

        return null;
    }

    // misc
    public void iterateSimilar(List<Type> args, int minMatchArgs, Consumer<Overload> handler, SemanticContext context) {
        for (var overload : overloads) {
            int matchingArgs = 0;
            for (int i = 0; i < Math.min(args.size(), overload.args().size()); i++) {
                if (context.typeManager().canCoerce(args.get(i), overload.args.get(i)))
                    matchingArgs++;
            }

            if (matchingArgs >= minMatchArgs) {
                handler.accept(overload);
            }
        }
    }

    public record Overload(List<Type> args, Type returnType, @Nullable TranslationUnit tu,
                           @Nullable CodeRange definitionRange) {
        public String toSignature() {
            StringBuilder builder = new StringBuilder();
            builder.append("(");
            for (var arg : args) {
                builder.append(arg.name()).append(", ");
            }

            builder.setLength(builder.length() - 2);
            builder.append(") -> ").append(returnType.name());
            return builder.toString();
        }
    }
}
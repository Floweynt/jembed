package com.floweytf.jembed.util;

import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public class Utils {
    // flatMap for null
    public static <T, U> U mapNull(@Nullable T value, Function<T, @Nullable U> mapper) {
        if (value == null)
            return null;
        return mapper.apply(value);
    }

    @SafeVarargs
    public static <T> T coalesce(@Nullable T value, Supplier<@Nullable T>... fallbacks) {
        int index = 0;
        while (value == null) {
            if (index == fallbacks.length)
                break;
            value = fallbacks[index++].get();
        }

        return value;
    }
}

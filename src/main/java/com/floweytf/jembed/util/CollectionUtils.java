package com.floweytf.jembed.util;

import java.util.Arrays;
import java.util.List;

public class CollectionUtils {
    @SafeVarargs
    public static <T> List<T> listFromArrayFlat(T[]... args) {
        return Arrays.stream(args).flatMap(Arrays::stream).toList();
    }
}

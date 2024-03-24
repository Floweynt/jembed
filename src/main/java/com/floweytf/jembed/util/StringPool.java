package com.floweytf.jembed.util;

import java.util.HashMap;

public class StringPool {
    private final HashMap<String, StrRef> pool = new HashMap<>();

    public StrRef intern(String str) {
        // okay I promise this is smart
        return pool.computeIfAbsent(str, (s) -> new StrRef(str));
    }

    public TaggedStrRef internTag(String str, int tag) {
        // okay I promise this is smart
        return (TaggedStrRef) pool.computeIfAbsent(str, (s) -> new TaggedStrRef(str, tag));
    }

    public static class StrRef {
        private final String str;

        private StrRef(String init) {
            this.str = init;
        }

        public String get() {
            return str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    public static class TaggedStrRef extends StrRef {
        private final int tag;

        private TaggedStrRef(String init, int tag) {
            super(init);
            this.tag = tag;
        }

        public int tag() {
            return tag;
        }
    }
}

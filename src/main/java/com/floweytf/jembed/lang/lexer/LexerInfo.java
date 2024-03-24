package com.floweytf.jembed.lang.lexer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;

public record LexerInfo(
    int[] transition,
    int start,
    boolean[] endMask,
    int[] classifier,
    int classCount
) {
    private static int readSignedLeb128(InputStream in) throws IOException {
        int result = 0;
        int cur;
        int count = 0;
        int signBits = -1;

        do {
            cur = in.read() & 0xff;
            result |= (cur & 0x7f) << (count * 7);
            signBits <<= 7;
            count++;
        } while (((cur & 0x80) == 0x80) && count < 5);

        if ((cur & 0x80) == 0x80) {
            throw new IllegalStateException("invalid LEB128 sequence");
        }

        // Sign extend if appropriate
        if (((signBits >> 1) & result) != 0) {
            result |= signBits;
        }

        return result;
    }

    public static LexerInfo from(String lexerData) {
        try {
            lexerData = lexerData.replaceAll("[^a-zA-Z0-9/+]", "");
            final var lexerBytes = Base64.getDecoder().decode(lexerData);
            final var in = new ByteArrayInputStream(lexerBytes);
            final var type = in.read();
            if (type != 1)
                throw new UnsupportedOperationException("cannot use non classifying lexer");

            var stateCount = readSignedLeb128(in);
            var startState = readSignedLeb128(in);

            final var stateBitset = new byte[(stateCount + 7) / 8];
            final var endBitmask = new boolean[stateCount];
            in.read(stateBitset);

            for (int i = 0; i < endBitmask.length; i++) {
                endBitmask[i] = (stateBitset[i / 8] & (1 << (i % 8))) != 0;
            }

            final var classCount = readSignedLeb128(in);
            final var classifier = new int[256];
            var classifierIdx = 0;
            while (classifierIdx < classifier.length) {
                final var value = readSignedLeb128(in);
                final var count = readSignedLeb128(in);
                for (int i = 0; i < count; i++) {
                    classifier[classifierIdx++] = value;
                }
            }

            final var transition = new int[classCount * stateCount];
            var transitionIdx = 0;
            while (transitionIdx < transition.length) {
                final var value = readSignedLeb128(in);
                final var count = readSignedLeb128(in);
                for (int i = 0; i < count; i++) {
                    transition[transitionIdx++] = value;
                }
            }

            return new LexerInfo(transition, startState, endBitmask, classifier, classCount);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "Lexer[\n" +
            "    transition = " + Arrays.toString(transition) + "\n" +
            "    start = " + start + "\n" +
            "    endMask = " + Arrays.toString(endMask) + "\n" +
            "    classCount = " + classCount + "\n" +
            "    classifier = " + Arrays.toString(classifier) + "\n" +
            "]";
    }
}

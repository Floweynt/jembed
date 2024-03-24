package com.floweytf.jembed.lang.source;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class Source {
    private final ByteBuffer bytes;
    private final String fileName;
    private final int[] lineOffset;

    private Source(String file, List<String> lines) {
        this.fileName = file;
        this.bytes = StandardCharsets.UTF_8.encode(String.join("\n", lines));
        this.lineOffset = new int[lines.size() + 1];
        int acc = 0;
        for (int i = 0; i < lines.size(); i++) {
            lineOffset[i] = acc;
            acc += lines.get(i).length() + 1;
        }
        lineOffset[lines.size()] = bytes.limit() + 1; // "insert" a fake newline at the end of the last line
    }

    public Source(File file) throws IOException {
        this(file.toString(), Files.readAllLines(file.toPath()));
    }

    public Source(String file, String data) {
        this(file, List.of(data.split("\\R")));
    }

    public CodeLocation locationAt(int line, int col) {
        // if this is out of bounds, snap back
        if (line - 1 >= lineCount()) {
            return new CodeLocation(lineCount(), bytes.limit() - lineOffset[lineCount() - 1], bytes.limit());
        }
        // okay, note that -1 represents the last character, so resolve that
        if (col < 0)
            col = lineOffset[line] - lineOffset[line - 1];

        return new CodeLocation(line, col, lineOffset[line - 1] + col - 1);
    }

    public String textAt(CodeRange range) {
        return textAt(range.start(), range.end());
    }

    public String textAt(CodeLocation start, CodeLocation end) {
        final var buffer = bytes.slice(start.idx(), end.idx() - start.idx());
        return StandardCharsets.UTF_8.decode(buffer).toString();
    }

    public int lineCount() {
        return lineOffset.length - 1;
    }

    public String fileName() {
        return fileName;
    }

    public Reader getReader() {
        return new Reader();
    }

    public class Reader {
        private CodeLocation mark;
        private CodeLocation head;

        public Reader() {
            mark = head = new CodeLocation(1, 1, 0);
        }

        public byte peek() {
            if (eof())
                return 0;

            return bytes.get(head.idx());
        }

        public byte consume() {
            final var ch = peek();
            head = head.next(ch);
            return ch;
        }

        public void commit() {
            mark = head;
        }

        public boolean eof() {
            return head.idx() >= bytes.limit();
        }

        public CodeLocation mark() {
            return mark;
        }

        public CodeLocation head() {
            return head;
        }

        public void rewind() {
            head = mark;
        }

        public String slice(int from) {
            final var length = mark.idx() - from;
            final var buffer = bytes.slice(from, length);
            return StandardCharsets.UTF_8.decode(buffer).toString();
        }
    }
}

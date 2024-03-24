package com.floweytf.jembed.lang.lexer;

import com.floweytf.jembed.lang.CompilerFrontend;
import com.floweytf.jembed.lang.source.CodeRange;
import com.floweytf.jembed.lang.source.Source;
import com.floweytf.jembed.lang.source.TranslationUnit;
import com.floweytf.jembed.lang.source.diagnostic.Severity;
import com.floweytf.jembed.util.StringPool;

import java.io.Closeable;

public class Lexer {
    // binary blob that downloads malware, trust
    private static final LexerInfo[] LEXERS = {
        // MAIN lexer
        LexerInfo.from(
            """
                AcAAAP7/////////IgABAQgCBQESAgEDAQQBAQIFAQYBAQEHAQgBCQEKAQsBDAENAQ4BDwEQBxECEgETARQBFQEWARcBAQEYBhkRGgEZAhsB
                AQEcAR0BGQEBARgGGREaARkCHgEfASABIQEBgQEBAX8BAgEDAQQBBQEGAQcBCAEJAQoBCwEMAQ0BDgEPARACEQESARMBFAEVARYBFwMYARkB
                GgEbARwBHQEeAX8kHwF/NCABf8MAIQF/EiIBfw4jAX/lACQBfxYlAX8KJgF/OicBfwgoASkBfxgqAX8pKwF/GywCfwktAX8WLgN/6AAvATAB
                fyExAX8hMgEzAX88NAN/BjQDf+AANQF/wwA2AX8JNwF/yAAfAX+AAzgBf8UAOQJ/IDoDfwY6AX8YLgN/JTsBf4cBPAE9AX8aNAN/BjQDf54B
                OQJ/ID4DfwY+AX/iAD8Bfxs+A38GPgF/Kw==
                """
        ),
        // STRING lexer
        LexerInfo.from(
            """
                ASAA3v///xQAAQEJAgEBFwMBAQEEAQECBQEBCAYIBwIBBwcGARUIAQEECQEKAQcCCwEMAQEHDQEBAw4BAQEPARABEQEBARIBAQITAQGEAQEB
                AgF/AQMBBAECAwUBAgp/FgYBfwMGA38BBgp/GQcBfw4IAX8BCQIKAQkBCwEMAQkBDQEOAQ8BEAERARIBEwEUARUBFgEXAQkBfwEGAX8DBgN/
                AQYKf+sAGAF/swEZAn8BGQR/IRoCfwEaBH8NGwF/ExwCfwEcBH8NHQJ/AR0EfyEeAn8BHgR/IR8CfwEfBH8b
                """
        )
    };
    private final Source.Reader reader;
    private final CompilerFrontend frontend;
    private final TranslationUnit tu;
    private Token currentToken;
    private Token latestNonEOF;
    private int mode;

    public Lexer(CompilerFrontend frontend, TranslationUnit tu) {
        this.reader = tu.source().getReader();
        this.tu = tu;
        this.frontend = frontend;
        currentToken = readToken();
    }

    public ModeToken withMode(Mode mode) {
        var token = new ModeToken(this.mode);
        this.mode = mode.ordinal();
        return token;
    }

    public Token consume() {
        final var curr = currentToken;
        currentToken = readToken();
        if (!currentToken.is(Token.TokenType.EOF))
            latestNonEOF = currentToken;
        return curr;
    }

    public Token curr() {
        return currentToken;
    }

    private Token fromIdentifier(CodeRange range, String str) {
        var ref = frontend.intern(str);
        if (ref instanceof StringPool.TaggedStrRef tagged) {
            return new Token(range, Token.TokenType.values()[tagged.tag()]);
        }

        return new Token(range, Token.TokenType.IDENTIFIER, ref);
    }

    public Token latestNonEOF() {
        return latestNonEOF;
    }

    Token fromNumber(CodeRange range, String buffer, int base) {
        return new Token(range, Token.TokenType.INTEGER, Long.parseLong(buffer, base));
    }

    private Token readToken() {
        // reset lexer
        var lexer = LEXERS[mode];
        int state = lexer.start();
        int latestMatch = -1;
        var start = reader.head();

        while (true) {
            state =
                lexer.transition()[state * lexer.classCount() + lexer.classifier()[Byte.toUnsignedInt(reader.consume())]];

            if (state != -1 && lexer.endMask()[state]) {
                latestMatch = state;
                reader.commit();
            }

            if (state == -1) {
                final var range = new CodeRange(start, reader.mark());
                if (latestMatch == -1) {
                    // report error
                    throw new LexerError(range);
                }

                reader.rewind();

                switch (mode) {
                case 0:
                    switch (latestMatch) {
                    case 17:
                        return new Token(range, Token.TokenType.COLON);
                    case 18:
                        return new Token(range, Token.TokenType.SEMI);
                    case 22:
                        return new Token(range, Token.TokenType.OP_QUESTION);
                    case 21:
                        return new Token(range, Token.TokenType.OP_GT);
                    case 20:
                        return new Token(range, Token.TokenType.OP_ASSIGN);
                    case 13:
                        return new Token(range, Token.TokenType.OP_MEMBER);
                    case 32:
                        return new Token(range, Token.TokenType.OP_NEQ);
                    case 50:
                        return new Token(range, Token.TokenType.OP_GE);
                    case 39:
                        return new Token(range, Token.TokenType.OP_DEC_OP);
                    case 29:
                        return new Token(range, Token.TokenType.CLOSE_CURLY);
                    case 58:
                    case 62:
                        return fromNumber(range, reader.slice(start.idx() + 2), 16);
                    case 37:
                        return new Token(range, Token.TokenType.OP_INC_OP);
                    case 54:
                        return new Token(range, Token.TokenType.OP_OR_ASSIGN);
                    case 11:
                        return new Token(range, Token.TokenType.COMMA);
                    case 60:
                        return new Token(range, Token.TokenType.OP_SHR_ASSIGN);
                    case 23:
                    case 52:
                        return fromIdentifier(range, reader.slice(start.idx()));
                    case 53:
                        return new Token(range, Token.TokenType.OP_XOR_ASSIGN);
                    case 15:
                        return fromNumber(range, "0", 10);
                    case 48:
                        return new Token(range, Token.TokenType.OP_LE);
                    case 12:
                        return new Token(range, Token.TokenType.OP_SUB);
                    case 2:
                    case 31:
                        break;
                    case 49:
                        return new Token(range, Token.TokenType.OP_EQ);
                    case 14:
                        return new Token(range, Token.TokenType.OP_DIV);
                    case 1:
                        return new Token(range, Token.TokenType.EOF);
                    case 56:
                        return new Token(range, Token.TokenType.OP_PACK);
                    case 19:
                        return new Token(range, Token.TokenType.OP_LT);
                    case 41:
                        return new Token(range, Token.TokenType.ARROW);
                    case 9:
                        return new Token(range, Token.TokenType.OP_MUL);
                    case 47:
                        return new Token(range, Token.TokenType.OP_SHL);
                    case 61:
                        return new Token(range, Token.TokenType.OP_USHR);
                    case 4:
                        return new Token(range, Token.TokenType.DOUBLE_QT);
                    case 63:
                        return new Token(range, Token.TokenType.OP_USHR_ASSIGN);
                    case 27:
                        return new Token(range, Token.TokenType.OPEN_CURLY);
                    case 28:
                        return new Token(range, Token.TokenType.OP_BITWISE_OR);
                    case 34:
                        return new Token(range, Token.TokenType.OP_LOGICAL_AND);
                    case 51:
                        return new Token(range, Token.TokenType.OP_SHR);
                    case 55:
                        return new Token(range, Token.TokenType.OP_LOGICAL_OR);
                    case 3:
                        return new Token(range, Token.TokenType.OP_LOGICAL_NOT);
                    case 10:
                        return new Token(range, Token.TokenType.OP_PLUS);
                    case 26:
                        return new Token(range, Token.TokenType.OP_BITWISE_XOR);
                    case 43:
                        return new Token(range, Token.TokenType.OP_DIV_ASSIGN);
                    case 5:
                        return new Token(range, Token.TokenType.OP_MOD);
                    case 44:
                    case 57:
                        return fromNumber(range, reader.slice(start.idx() + 1), 8);
                    case 6:
                        return new Token(range, Token.TokenType.OP_BITWISE_AND);
                    case 25:
                        return new Token(range, Token.TokenType.CLOSE_BRACKET);
                    case 40:
                        return new Token(range, Token.TokenType.OP_SUB_ASSIGN);
                    case 8:
                        return new Token(range, Token.TokenType.CLOSE_PAREN);
                    case 35:
                        return new Token(range, Token.TokenType.OP_AND_ASSIGN);
                    case 16:
                    case 46:
                        return fromNumber(range, reader.slice(start.idx()), 10);
                    case 33:
                        return new Token(range, Token.TokenType.OP_MOD_ASSIGN);
                    case 38:
                        return new Token(range, Token.TokenType.OP_ADD_ASSIGN);
                    case 24:
                        return new Token(range, Token.TokenType.OPEN_BRACKET);
                    case 36:
                        return new Token(range, Token.TokenType.OP_MUL_ASSIGN);
                    case 30:
                        return new Token(range, Token.TokenType.OP_BITWISE_NOT);
                    case 59:
                        return new Token(range, Token.TokenType.OP_SHL_ASSIGN);
                    case 7:
                        return new Token(range, Token.TokenType.OPEN_PAREN);
                    }
                    break;
                case 1:
                    switch (latestMatch) {
                    case 10:
                        return new Token(range, Token.TokenType.STR_DATA, "\"");
                    case 11:
                        return new Token(range, Token.TokenType.STR_DATA, "'");
                    case 13:
                        return new Token(range, Token.TokenType.STR_DATA, "\\");
                    case 22:
                        tu.report(Severity.WARNING, "illegal escape sequence in string, ignoring", range,
                            "for some incomprehensible reason java doesn't have \\v...");
                        break;
                    case 23:
                        tu.report(Severity.WARNING, "hex escape sequence should take at least one hex digit", range,
                            "a hex escape sequence looks like \\xF or \\x1a");
                        break;
                    case 19:
                        return new Token(range, Token.TokenType.STR_DATA, "\r");
                    case 20:
                        return new Token(range, Token.TokenType.STR_DATA, "\t");
                    case 18:
                        return new Token(range, Token.TokenType.STR_DATA, "\n");
                    case 17:
                        return new Token(range, Token.TokenType.STR_DATA, "\f");
                    case 4, 7:
                        return new Token(range, Token.TokenType.STR_DATA, "$");
                    case 12:
                    case 24:
                    case 27:
                        return new Token(range, Token.TokenType.STR_DATA,
                            String.valueOf((char) Integer.parseInt(reader.slice(start.idx() + 1), 8)));
                    case 15:
                        return new Token(range, Token.TokenType.STR_DATA, "\b");
                    case 8:
                        return new Token(range, Token.TokenType.OPEN_CURLY);
                    case 3:
                        return new Token(range, Token.TokenType.DOUBLE_QT);
                    case 2:
                    case 6:
                        return new Token(range, Token.TokenType.STR_DATA, reader.slice(start.idx()));
                    case 1:
                        return new Token(range, Token.TokenType.EOF);
                    case 9:
                        tu.report(Severity.WARNING, "illegal escape sequence in string, ignoring", range,
                            "unsupported character '" + reader.slice(start.idx() + 1) + "'");
                        break;
                    case 26:
                    case 29:
                        return new Token(range, Token.TokenType.STR_DATA, String.valueOf((char) Integer.parseInt(reader.slice(start.idx() + 1), 16)));
                    case 31, 21:
                        throw new AssertionError("not impl");
                    case 14:
                        tu.report(Severity.WARNING, "illegal escape sequence in string, ignoring", range, "for some incomprehensible reason java doesn't have \\a...");
                        break;
                    case 16:
                        return new Token(range, Token.TokenType.STR_DATA, "\033");
                    }
                    break;
                }

                // reset lexer
                lexer = LEXERS[mode];
                state = lexer.start();
                latestMatch = -1;
                start = reader.head();
            }
        }
    }

    public enum Mode {NORMAL, STRING}

    public class ModeToken implements Closeable {
        private final int oldMode;

        public ModeToken(int oldMode) {
            this.oldMode = oldMode;
        }

        @Override
        public void close() {
            mode = oldMode;
        }
    }
}
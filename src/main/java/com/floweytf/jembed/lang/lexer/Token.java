package com.floweytf.jembed.lang.lexer;

import com.floweytf.jembed.lang.ast.expr.ExprAST;
import com.floweytf.jembed.lang.ast.expr.binop.BinopExprAST;
import com.floweytf.jembed.lang.ast.expr.binop.BinopExprType;
import com.floweytf.jembed.lang.ast.expr.unary.UnaryExprAST;
import com.floweytf.jembed.lang.ast.expr.unary.UnaryExprType;
import com.floweytf.jembed.lang.source.CodeLocation;
import com.floweytf.jembed.lang.source.CodeRange;
import com.floweytf.jembed.util.StringPool;

import java.util.Arrays;
import java.util.EnumSet;

public record Token(CodeRange range, TokenType type, Object parameter) {
    public Token {
        boolean isValid = parameter == null ||
            parameter instanceof StringPool.StrRef ||
            parameter instanceof String ||
            parameter instanceof Double ||
            parameter instanceof Long;

        if (!isValid)
            throw new IllegalArgumentException("Illegal type " + parameter.getClass().getCanonicalName());
    }

    public Token(CodeRange range, TokenType type) {
        this(range, type, null);
    }

    public boolean is(TokenType... types) {
        return Arrays.stream(types).anyMatch(type -> type == this.type);
    }

    public boolean isKeyword(KeywordCategory... types) {
        return Arrays.stream(types).anyMatch(this.type.keywordCategories::contains);
    }

    // build stuff
    public ExprAST buildUnaryPrefix(ExprAST ast) {
        return new UnaryExprAST(range, ast, type().unaryPrefix);
    }

    public ExprAST buildUnaryPostfix(ExprAST ast) {
        return new UnaryExprAST(ast, range, type().unaryPostfix);
    }

    public ExprAST buildBinaryOperator(ExprAST lhs, ExprAST rhs) {
        return new BinopExprAST(lhs, range, rhs, type().binop);
    }

    public OpPrec precedence() {
        return type.precedence;
    }

    public String name() {
        return type.name;
    }

    public boolean isUnaryPrefix() {
        return type.unaryPrefix != null;
    }

    public boolean isUnaryPostfix() {
        return type.unaryPostfix != null;
    }

    public CodeLocation start() {
        return range.start();
    }

    public CodeLocation end() {
        return range.end();
    }

    // get token parameters
    public String stringLit() {
        return (String) parameter;
    }

    public Long intLit() {
        return (Long) parameter;
    }

    public Double floatLit() {
        return (Double) parameter;
    }

    public StringPool.StrRef identifier() {
        return (StringPool.StrRef) parameter;
    }

    @Override
    public String toString() {
        String res = "Token[" + range.toString() + "](" + type.name();
        if (parameter != null)
            res += ", " + parameter;

        return res + ")";
    }

    public enum TokenType {
        // regular tokens
        EOF("end-of-file"),
        IDENTIFIER("identifier"),
        INTEGER("int-literal"),
        FLOAT("float-literal"),
        CHAR("char-literal"),

        // operators
        OP_PACK("..."),

        // assign
        OP_ASSIGN("=", OpPrec.ASSIGNMENT, BinopExprType.ASSIGN),
        OP_SHR_ASSIGN(">>=", OpPrec.ASSIGNMENT, BinopExprType.SHR_ASSIGN),
        OP_USHR_ASSIGN(">>>=", OpPrec.ASSIGNMENT, BinopExprType.USHR_ASSIGN),
        OP_SHL_ASSIGN("<<=", OpPrec.ASSIGNMENT, BinopExprType.SHL_ASSIGN),
        OP_ADD_ASSIGN("+=", OpPrec.ASSIGNMENT, BinopExprType.ADD_ASSIGN),
        OP_SUB_ASSIGN("-=", OpPrec.ASSIGNMENT, BinopExprType.SUB_ASSIGN),
        OP_MUL_ASSIGN("*=", OpPrec.ASSIGNMENT, BinopExprType.MUL_ASSIGN),
        OP_DIV_ASSIGN("/=", OpPrec.ASSIGNMENT, BinopExprType.DIV_ASSIGN),
        OP_MOD_ASSIGN("%=", OpPrec.ASSIGNMENT, BinopExprType.MOD_ASSIGN),
        OP_AND_ASSIGN("&=", OpPrec.ASSIGNMENT, BinopExprType.AND_ASSIGN),
        OP_XOR_ASSIGN("^=", OpPrec.ASSIGNMENT, BinopExprType.XOR_ASSIGN),
        OP_OR_ASSIGN("|=", OpPrec.ASSIGNMENT, BinopExprType.OR_ASSIGN),

        // increment
        // ++
        OP_INC_OP("++", UnaryExprType.INC, UnaryExprType.POST_INC),
        // --
        OP_DEC_OP("--", UnaryExprType.DEC, UnaryExprType.POST_DEC),

        // comparison
        OP_LE("<=", OpPrec.RELATIONAL, BinopExprType.LE),
        OP_GE(">=", OpPrec.RELATIONAL, BinopExprType.GE),
        OP_LT("<", OpPrec.RELATIONAL, BinopExprType.LT),
        OP_GT(">", OpPrec.RELATIONAL, BinopExprType.GT),

        OP_EQ("==", OpPrec.EQUALITY, BinopExprType.EQ),
        OP_NEQ("!=", OpPrec.EQUALITY, BinopExprType.NEQ),

        // bitwise
        OP_BITWISE_AND("&", OpPrec.BIT_AND, BinopExprType.BIT_AND),
        OP_BITWISE_XOR("^", OpPrec.BIT_XOR, BinopExprType.BIT_XOR),
        OP_BITWISE_OR("|", OpPrec.BIT_OR, BinopExprType.BIT_OR),
        OP_BITWISE_NOT("~", UnaryExprType.BIT_NOT, null),
        OP_SHR(">>", OpPrec.SHIFT, BinopExprType.SHR),
        OP_USHR(">>>", OpPrec.SHIFT, BinopExprType.USHR),
        OP_SHL("<<", OpPrec.SHIFT, BinopExprType.SHL),

        // logical
        OP_LOGICAL_AND("&&", OpPrec.LOGICAL_AND, BinopExprType.LOGICAL_AND),
        OP_LOGICAL_OR("||", OpPrec.LOGICAL_OR, BinopExprType.LOGICAL_OR),
        OP_LOGICAL_NOT("!", UnaryExprType.LOGICAL_NOT, null),
        // arithmetic
        OP_SUB("-", UnaryExprType.NEG, null, OpPrec.ADD, BinopExprType.SUB),
        OP_PLUS("+", UnaryExprType.PLUS, null, OpPrec.ADD, BinopExprType.ADD),
        OP_MUL("*", OpPrec.MUL, BinopExprType.MUL),
        OP_DIV("/", OpPrec.MUL, BinopExprType.DIV),
        OP_MOD("%", OpPrec.MUL, BinopExprType.MOD),

        // punctuation
        SEMI(";"), // ;
        COMMA(","), // ,
        COLON(":"), // :

        // paren
        OPEN_CURLY("{"), // {
        CLOSE_CURLY("}"), // }
        OPEN_PAREN("("), // (
        CLOSE_PAREN(")"), // )
        OPEN_BRACKET("["), // [
        CLOSE_BRACKET("]"), // ]

        // misc
        ARROW("->"), // ->
        OP_MEMBER("."), // .
        OP_QUESTION("?"), // ?

        // keywords
        KW_VAR("var-keyword", "var", KeywordCategory.VARIABLE_DECL, KeywordCategory.DECL),
        KW_CONST("const-keyword", "const", KeywordCategory.VARIABLE_DECL, KeywordCategory.DECL),
        KW_FUNC("func-keyword", "func", KeywordCategory.DECL),
        KW_IMPORT("import-keyword", "import"),

        DOUBLE_QT("\""),
        STR_DATA("<string-data>"),
        ;

        private final UnaryExprType unaryPrefix;
        private final UnaryExprType unaryPostfix;
        private final BinopExprType binop;
        private final OpPrec precedence;
        private final String name;
        private final EnumSet<KeywordCategory> keywordCategories = EnumSet.noneOf(KeywordCategory.class);
        private boolean isKeyword = false;
        private String kwId = null;

        TokenType(String name) {
            this(name, null, null, null, null);
        }

        TokenType(
            String name,
            UnaryExprType unaryPrefix,
            UnaryExprType unaryPostfix,
            OpPrec precedence,
            BinopExprType binopType
        ) {
            this.unaryPrefix = unaryPrefix;
            this.unaryPostfix = unaryPostfix;
            this.precedence = precedence;
            this.binop = binopType;
            this.name = name;
        }

        TokenType(String name, UnaryExprType prefixBuilder, UnaryExprType postfixBuilder) {
            this(name, prefixBuilder, postfixBuilder, null, null);
        }

        TokenType(String name, OpPrec precedence, BinopExprType type) {
            this(name, null, null, precedence, type);
        }

        TokenType(String name, String kwId, KeywordCategory... categories) {
            this(name, null, null, null, null);
            this.kwId = kwId;
            keywordCategories.addAll(Arrays.asList(categories));
            isKeyword = true;
        }

        public boolean isKeyword() {
            return isKeyword;
        }

        public String kwId() {
            return kwId;
        }
    }
}

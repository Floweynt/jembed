package com.floweytf.jembed.lang.parser;

import com.floweytf.jembed.lang.CompilerFrontend;
import com.floweytf.jembed.lang.ast.ModuleAST;
import com.floweytf.jembed.lang.ast.SemanticAST;
import com.floweytf.jembed.lang.ast.expr.*;
import com.floweytf.jembed.lang.ast.expr.literal.FloatLiteralAST;
import com.floweytf.jembed.lang.ast.expr.literal.IntLiteralAST;
import com.floweytf.jembed.lang.ast.expr.literal.StringLiteralAST;
import com.floweytf.jembed.lang.ast.expr.literal.StringTemplateLiteralAST;
import com.floweytf.jembed.lang.ast.stmt.BlockAST;
import com.floweytf.jembed.lang.ast.stmt.def.FuncDefAST;
import com.floweytf.jembed.lang.ast.stmt.def.NamedDefAST;
import com.floweytf.jembed.lang.ast.stmt.def.VarDefAST;
import com.floweytf.jembed.lang.ast.type.TypeAST;
import com.floweytf.jembed.lang.ast.type.TypeRefAST;
import com.floweytf.jembed.lang.lexer.KeywordCategory;
import com.floweytf.jembed.lang.lexer.Lexer;
import com.floweytf.jembed.lang.lexer.OpPrec;
import com.floweytf.jembed.lang.lexer.Token;
import com.floweytf.jembed.lang.source.CodeRange;
import com.floweytf.jembed.lang.source.TranslationUnit;
import com.floweytf.jembed.lang.source.diagnostic.Severity;
import com.floweytf.jembed.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final Lexer lexer;
    private final TranslationUnit tu;
    private final CompilerFrontend frontend;

    public Parser(CompilerFrontend frontend, TranslationUnit tu) {
        this.lexer = new Lexer(frontend, tu);
        this.tu = tu;
        this.frontend = frontend;
    }

    private <T> T reportError(Token token, String message, String... notes) {
        tu.report(Severity.ERROR, message, token.range(), notes);
        throw new ParseError();
    }

    private <T> T reportError(Token token, String message, List<String> notes) {
        tu.report(Severity.ERROR, message, token.range(), notes);
        throw new ParseError();
    }

    private Token validate(Token tok, Token.TokenType type, String message, String... notes) {
        if (!tok.is(type))
            reportError(tok, message, CollectionUtils.listFromArrayFlat(notes, new String[]{"got token '" + tok.name() + "'"}));
        return tok;
    }

    private Token eat(Token.TokenType type, String message, String... notes) {
        return validate(lexer.consume(), type, message, notes);
    }

    private Token expect(Token.TokenType type, String message, String... notes) {
        return validate(lexer.curr(), type, message, notes);
    }

    // expression parser

    public StringTemplateLiteralAST parseString() {
        Token start;
        List<ExprAST> components;

        try (var ignored = lexer.withMode(Lexer.Mode.STRING)) {
            start = lexer.consume();
            components = new ArrayList<>();

            while (!lexer.curr().is(Token.TokenType.DOUBLE_QT)) {
                components.add(switch (lexer.curr().type()) {
                    case STR_DATA -> new StringLiteralAST(lexer.consume());
                    case OPEN_CURLY -> {
                        ExprAST res;
                        try (var ignored1 = lexer.withMode(Lexer.Mode.NORMAL)) {
                            lexer.consume();
                            res = parseExpr();
                            eat(Token.TokenType.CLOSE_CURLY, "expected '}' at the end of a string template expression");
                        }

                        yield res;
                    }
                    default -> throw new IllegalStateException(lexer.curr().toString());
                });
            }
        }

        var end = lexer.consume();
        return new StringTemplateLiteralAST(start, components, end);

    }

    public ExprAST parseAtom() {
        return switch (lexer.curr().type()) {
            case OPEN_PAREN -> {
                lexer.consume();
                var res = parseExpr();
                eat(Token.TokenType.CLOSE_PAREN, "expected closing parenthesis");
                yield res;
            }
            case INTEGER -> new IntLiteralAST(lexer.consume());
            case FLOAT -> new FloatLiteralAST(lexer.consume());
            case EOF -> reportError(
                lexer.latestNonEOF(),
                "unexpected end-of-file while parsing expression"
            );
            case IDENTIFIER -> new IdentifierAST(lexer.consume());
            case DOUBLE_QT -> parseString();
            default -> reportError(
                lexer.curr(),
                String.format("illegal token '%s' in primary expression",
                    lexer.curr().name()
                )
            );
        };
    }

    public ExprAST parsePrimary() {
        var atom = parseAtom();
        boolean exit = false;

        while (!exit) {
            var op = lexer.curr();

            switch (op.type()) {
            case OP_MEMBER -> {
                lexer.consume();
                var identifier = eat(Token.TokenType.IDENTIFIER, "expected identifier in member access");
                atom = new MemberAccessAST(atom, op, identifier);
            }
            case OPEN_PAREN -> {
                lexer.consume();
                // parse function call
                final ArrayList<ExprAST> args = new ArrayList<>();

                while (!lexer.curr().is(Token.TokenType.CLOSE_PAREN)) {
                    // error recovery: eat all commas
                    while (lexer.curr().is(Token.TokenType.COMMA)) {
                        tu.report(Severity.ERROR, "extraneous comma in function call", lexer.consume().range());
                    }

                    if (lexer.curr().is(Token.TokenType.CLOSE_PAREN)) {
                        break;
                    }

                    args.add(parseExpr());

                    // if we see a comma, the next token is technically allowed to be a ')'
                    if (lexer.curr().is(Token.TokenType.COMMA)) {
                        lexer.consume();
                        continue;
                    }

                    // if we don't see a comma, this is terrible, break and hope we get a ')' next
                    break;
                }

                var closing = eat(Token.TokenType.CLOSE_PAREN, "expected ')' (did you forget a comma?)");

                atom = new InvokeExprAST(atom, args, closing);
            }
            case OPEN_BRACKET -> {
                lexer.consume();
                var index = parseExpr();
                var closing = eat(Token.TokenType.CLOSE_BRACKET, "expected ] in array index");

                atom = new IndexExprAST(atom, index, closing);
            }
            default -> exit = true;
            }
        }

        return atom;
    }

    public ExprAST parseUnary() {
        ArrayList<Token> prefixTokens = new ArrayList<>(16);
        while (lexer.curr().isUnaryPrefix()) {
            prefixTokens.add(lexer.curr());
            lexer.consume();
        }

        ExprAST current = parsePrimary();

        // okay we need to right-to-left associate
        for (int i = prefixTokens.size() - 1; i >= 0; i--) {
            current = prefixTokens.get(i).buildUnaryPrefix(current);
        }

        // parse postfix
        // this is left-to-right
        while (lexer.curr().isUnaryPostfix()) {
            current = lexer.consume().buildUnaryPostfix(current);
        }

        return current;
    }

    public ExprAST parseBinop(OpPrec prec) {
        if (prec == null) {
            return parseUnary();
        }

        final var next = prec.next();

        if (!prec.isRtoL) {
            ExprAST lhs = parseBinop(next);

            while (lexer.curr().precedence() == prec) {
                final var tok = lexer.consume();
                final var rhs = parseBinop(next);
                lhs = tok.buildBinaryOperator(lhs, rhs);
            }

            return lhs;
        } else {
            ArrayList<ExprAST> expr = new ArrayList<>();
            ArrayList<Token> tokens = new ArrayList<>();

            expr.add(parseBinop(next));

            while (lexer.curr().precedence() == prec) {
                tokens.add(lexer.consume());
                expr.add(parseBinop(next));
            }

            var rhs = expr.get(expr.size() - 1);

            for (int i = expr.size() - 2; i >= 0; i--) {
                rhs = tokens.get(i).buildBinaryOperator(expr.get(i), rhs);
            }

            return rhs;
        }
    }

    public ExprAST parseExpr() {
        return parseBinop(OpPrec.ASSIGNMENT);
    }

    // misc
    public TypeAST parseType() {
        return new TypeRefAST(eat(Token.TokenType.IDENTIFIER, "expected identifier (name of type) in type reference "));
    }

    // definitionRange parser
    public VarDefAST parseVariableDef() {
        var decl = lexer.consume();
        boolean isMutable = switch (decl.type()) {
            case KW_VAR -> true;
            case KW_CONST -> false;
            default -> throw new IllegalStateException();
        };

        final var name = eat(Token.TokenType.IDENTIFIER, "expected name of variable after var keyword");
        TypeAST type = null;
        ExprAST init = null;

        if (lexer.curr().is(Token.TokenType.COLON)) {
            lexer.consume();
            type = parseType();
        }

        if (lexer.curr().is(Token.TokenType.OP_ASSIGN)) {
            lexer.consume();
            init = parseExpr();
        }

        return new VarDefAST(decl, name, isMutable, type, init);
    }

    public FuncDefAST parseFuncDef() {
        final var start = lexer.consume();
        final var name = eat(
            Token.TokenType.IDENTIFIER,
            "expected name of function after func keyword",
            "a function definitionRange looks like `func name(...params) { body }`"
        );

        final var params = new ArrayList<VarDefAST>();
        eat(
            Token.TokenType.OPEN_PAREN,
            "expected '(' to open parameter list in function definitionRange",
            "a function definitionRange looks like `func name(...params) { body }`"
        );

        while (!lexer.curr().is(Token.TokenType.CLOSE_PAREN)) {
            // error recovery: eat all commas
            while (lexer.curr().is(Token.TokenType.COMMA)) {
                tu.report(Severity.ERROR, "extraneous comma in function definitionRange", lexer.consume().range());
            }

            if (lexer.curr().is(Token.TokenType.CLOSE_PAREN)) {
                break;
            }

            params.add(parseVariableDef());

            // if we see a comma, the next token is technically allowed to be a ')'
            if (lexer.curr().is(Token.TokenType.COMMA)) {
                lexer.consume();
                continue;
            }

            // if we don't see a comma, this is terrible, break and hope we get a ')' next
            break;
        }

        var closingParen = eat(Token.TokenType.CLOSE_PAREN, "expected ')' (did you forget a comma?)");

        TypeAST returnType = null;

        if (lexer.curr().is(Token.TokenType.ARROW)) {
            lexer.consume();
            returnType = parseType();
        }

        final var block = parseBlock(false);

        return new FuncDefAST(start, name, params, closingParen, returnType, block);
    }

    public SemanticAST<?> parseDecl() {
        if (lexer.curr().isKeyword(KeywordCategory.VARIABLE_DECL)) {
            return parseVariableDef();
        } else if (lexer.curr().is(Token.TokenType.KW_FUNC)) {
            return parseFuncDef();
        }

        throw new IllegalStateException();
    }

    // block parser
    public BlockAST parseBlock(boolean shouldIntroFrame) {
        var start = lexer.consume();
        List<SemanticAST<?>> ast = new ArrayList<>();
        while (!lexer.curr().is(Token.TokenType.CLOSE_CURLY)) {
            boolean needSemi = true;

            if (lexer.curr().isKeyword(KeywordCategory.DECL)) {
                ast.add(parseDecl());
            } else if (lexer.curr().is(Token.TokenType.SEMI)) {
                tu.report(Severity.WARNING, "lone semicolon does not declare anything, consider removing", lexer.curr().range());
            } else if (lexer.curr().is(Token.TokenType.OPEN_CURLY)) {
                needSemi = false;
                ast.add(parseBlock(shouldIntroFrame));
            } else {
                ast.add(parseExpr());
            }

            if (needSemi)
                eat(Token.TokenType.SEMI, "expected semicolon at end of statement");
        }

        var end = lexer.consume();

        return new BlockAST(start, ast, end, shouldIntroFrame);
    }

    public ModuleAST parseModule() {
        final var first = lexer.curr().start();

        final var imports = new ArrayList<StringTemplateLiteralAST>();
        while (lexer.curr().is(Token.TokenType.KW_IMPORT)) {
            var kw = lexer.consume();
            var moduleName = parseString();
            imports.add(moduleName);

            if (lexer.curr().is(Token.TokenType.SEMI)) {
                lexer.consume();
                continue;
            }

            // we should be smart on where errors are reported
            tu.report(
                Severity.ERROR,
                "expected semicolon at end of import",
                new CodeRange(kw.start(), moduleName.end()),
                "an import statement looks like `import \"path/to/module\";` or `import \"@sys_module\";`"
            );
        }

        var declerations = new ArrayList<SemanticAST<?>>();

        while (!lexer.curr().is(Token.TokenType.EOF)) {
            declerations.add(parseDecl());
        }

        final var last = lexer.latestNonEOF().range().end();

        final var module = new ModuleAST(imports, declerations, new CodeRange(first, last), tu);

        declerations.forEach(ast -> {
            if (ast instanceof NamedDefAST namedAst) {
                module.getSymbolTable().add(namedAst);
            }
        });

        return module;
    }
}
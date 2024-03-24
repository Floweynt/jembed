package com.floweytf.jembed.lang;

import com.floweytf.jembed.lang.ast.SemanticAST;
import com.floweytf.jembed.lang.lexer.LexerError;
import com.floweytf.jembed.lang.lexer.Token;
import com.floweytf.jembed.lang.parser.ParseError;
import com.floweytf.jembed.lang.sema.ctx.SemanticContext;
import com.floweytf.jembed.lang.source.Source;
import com.floweytf.jembed.lang.source.TranslationUnit;
import com.floweytf.jembed.lang.source.diagnostic.Severity;
import com.floweytf.jembed.util.StringPool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompilerFrontend {
    private final StringPool pool = new StringPool();
    private final List<TranslationUnit> sources = new ArrayList<>();
    private final SemanticContext semanticContext;

    public CompilerFrontend() {
        Arrays.stream(Token.TokenType.values())
            .filter(Token.TokenType::isKeyword)
            .forEach(tokenType -> pool.internTag(tokenType.kwId(), tokenType.ordinal()));
        semanticContext = new SemanticContext(this);
    }

    public StringPool.StrRef intern(String str) {
        return pool.intern(str);
    }

    public boolean parse(Source source) {
        var tu = makeTU(source);
        SemanticAST<?> ast = null;

        try {
            ast = tu.parser().parseModule();
        } catch (ParseError ignored) {
        } catch (LexerError e) {
            tu.report(Severity.ERROR, "unrecognized token", e.range());
        }

        if (tu.hasErrors()) {
            tu.dumpErrors(System.out);
            return false;
        }

        semanticContext.runSemantic(ast);
        if (tu.hasErrors()) {
            tu.dumpErrors(System.out);
            return false;
        }

        return true;
    }

    public void registerTU(TranslationUnit unit) {
        sources.add(unit);
    }

    public TranslationUnit makeTU(Source source) {
        var tu = new TranslationUnit(this, source);
        registerTU(tu);
        return tu;
    }
}

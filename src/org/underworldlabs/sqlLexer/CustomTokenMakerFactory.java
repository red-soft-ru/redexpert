package org.underworldlabs.sqlLexer;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.TokenMaker;

public class CustomTokenMakerFactory extends AbstractTokenMakerFactory {
    private TokenMaker tokenMaker;
    @Override
    protected void initTokenMakerMap() {
        this.putMapping("antlr/sql", SqlLexerTokenMaker.class.getName());

    }

    protected TokenMaker getTokenMakerImpl(String key) {
        if(tokenMaker==null)
            tokenMaker = new SqlLexerTokenMaker();
        return tokenMaker;
    }

    public TokenMaker getTokenMaker() {
        return tokenMaker;
    }
}

package org.underworldlabs.sqlLexer;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.Pair;

public class CustomSqlLexer extends SqlLexer {
    public CustomSqlLexer(CharStream input) {
        super(input);
    }
    public Pair<TokenSource, CharStream> getTokenFactorySourcePair()
    {
        return _tokenFactorySourcePair;
    }
}

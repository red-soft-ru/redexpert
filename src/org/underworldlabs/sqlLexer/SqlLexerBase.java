package org.underworldlabs.sqlLexer;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;

public abstract class SqlLexerBase extends Lexer
{
    public SqlLexerBase(CharStream input)
    {
        super(input);
    }

    protected boolean IsNewlineAtPos(int pos)
    {
        int la = _input.LA(pos);
        return la == -1 || la == '\n';
    }
}

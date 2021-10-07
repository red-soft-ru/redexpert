package org.underworldlabs.sqlLexer;


import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenSource;
import org.fife.ui.rsyntaxtextarea.Token;
import org.underworldlabs.antrlExtentionRsyntxtextarea.AntlrTokenMaker;
import org.underworldlabs.antrlExtentionRsyntxtextarea.MultiLineTokenInfo;

import java.util.TreeSet;


public class SqlLexerTokenMaker extends AntlrTokenMaker {

    public final static int DB_OBJECT = 999;

    public SqlLexerTokenMaker() {
        super(new MultiLineTokenInfo(0, Token.COMMENT_MULTILINE, "/*", "*/"),
                new MultiLineTokenInfo(0, Token.LITERAL_STRING_DOUBLE_QUOTE, "'", "'"));
    }

    TreeSet<String> dbobjects;

    @Override
    protected int convertType(int i) {
        switch (i) {
            case SqlLexer.KEYWORD:
                return Token.RESERVED_WORD;
            case SqlLexer.DATATYPE_SQL:
                return Token.DATA_TYPE;
            case SqlLexer.MULTILINE_COMMENT:
                return Token.COMMENT_MULTILINE;
            case SqlLexer.SINGLE_LINE_COMMENT:
                return Token.COMMENT_EOL;
            case SqlLexer.OPERATOR:
            case SqlLexer.UNARY_OPERATOR:
                return Token.OPERATOR;
            case SqlLexer.STRING_LITERAL:
                return Token.LITERAL_STRING_DOUBLE_QUOTE;
            case SqlLexer.PART_OBJECT:
                return Token.VARIABLE;
            case SqlLexer.LINTERAL_VALUE:
                return Token.LITERAL_BOOLEAN;
            case DB_OBJECT:
                return Token.PREPROCESSOR;
            case SqlLexer.NUMERIC_LITERAL:
                return Token.LITERAL_NUMBER_DECIMAL_INT;
            case SqlLexer.ERROR_CHAR:
                return Token.ERROR_IDENTIFIER;
            default:
                return Token.IDENTIFIER;
        }
    }

    @Override
    protected org.antlr.v4.runtime.Token convertToken(org.antlr.v4.runtime.Token token) {
        if(token.getType()==SqlLexer.IDENTIFIER)
        {
            if (dbobjects != null&&currentToken!=null) {
                String x = token.getText();
                if (x.length() > 0 && x.charAt(0) > 'A' && x.charAt(0) < 'z')
                    x = x.toUpperCase();
                if (x.startsWith("\"") && x.endsWith("\"") && x.length() > 1)
                    x = x.substring(1, x.length() - 1);
                if (dbobjects.contains(x))
                {
                    CustomToken customToken = new CustomToken(token);
                    customToken.setType(DB_OBJECT);
                    return customToken;
                }

            }
        }
        return token;

    }

    public TreeSet<String> getDbobjects() {
        return dbobjects;
    }

    public void setDbobjects(TreeSet<String> dbobjects) {
        this.dbobjects = dbobjects;
    }

    @Override
    protected Lexer createLexer(String s) {
        return new SqlLexer(CharStreams.fromString(s));
    }
}

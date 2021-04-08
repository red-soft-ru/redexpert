package org.underworldlabs.sqlLexer;


import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Lexer;
import org.fife.ui.rsyntaxtextarea.Token;
import org.underworldlabs.antrlExtentionRsyntxtextarea.AntlrTokenMaker;
import org.underworldlabs.antrlExtentionRsyntxtextarea.MultiLineTokenInfo;

import java.util.TreeSet;


public class SqlLexerTokenMaker extends AntlrTokenMaker {
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
            default:
                if (dbobjects != null) {
                    String x = currentToken.toString();
                    if (dbobjects.contains(x)) ;
                    return Token.FUNCTION;
                }
                return Token.IDENTIFIER;
        }
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

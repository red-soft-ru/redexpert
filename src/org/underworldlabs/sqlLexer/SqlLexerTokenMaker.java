package org.underworldlabs.sqlLexer;


import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Lexer;
import org.fife.ui.rsyntaxtextarea.Token;
import org.underworldlabs.antrlExtentionRsyntxtextarea.AntlrTokenMaker;
import org.underworldlabs.antrlExtentionRsyntxtextarea.MultiLineTokenInfo;

import java.util.TreeSet;


public class SqlLexerTokenMaker extends AntlrTokenMaker {

    public final static int DB_OBJECT = 999;
    public final static int VARIABLE = DB_OBJECT-1;
    public final static int PARAMETER = VARIABLE-1;
    public final static int ALIAS =PARAMETER - 1;


    public SqlLexerTokenMaker() {
        super(new MultiLineTokenInfo(0, Token.COMMENT_MULTILINE, "/*", "*/"),
                new MultiLineTokenInfo(0, Token.LITERAL_STRING_DOUBLE_QUOTE, "'", "'"),
                new MultiLineTokenInfo(0, Token.RESERVED_WORD_2, "\"", "\""));
    }

    TreeSet<String> dbobjects;
    TreeSet<String> variables;
    TreeSet<String> parameters;


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
            case SqlLexer.SEPARATOR:
                return Token.SEPARATOR;
            case SqlLexer.STRING_LITERAL:
                return Token.LITERAL_STRING_DOUBLE_QUOTE;
            case SqlLexer.PART_OBJECT:
            case PARAMETER:
            case VARIABLE:
                return Token.VARIABLE;
            case SqlLexer.LINTERAL_VALUE:
                return Token.LITERAL_BOOLEAN;
            case DB_OBJECT:
                return Token.PREPROCESSOR;
            case ALIAS:
                return Token.ANNOTATION;
            case SqlLexer.NUMERIC_LITERAL:
                return Token.LITERAL_NUMBER_DECIMAL_INT;
            case SqlLexer.ERROR_CHAR:
                return Token.ERROR_IDENTIFIER;
            case SqlLexer.QUOTE_IDENTIFIER:
                return Token.RESERVED_WORD_2;
            case SqlLexer.SPACES:
                return Token.WHITESPACE;
            default:
                return Token.IDENTIFIER;
        }
    }

    String lastDBObject = null;

    public String getLastDBObject() {
        return lastDBObject;
    }

    public void setLastDBObject(String lastDBObject) {
        this.lastDBObject = lastDBObject;
    }

    @Override
    protected org.antlr.v4.runtime.Token convertToken(org.antlr.v4.runtime.Token token) {
        if (token.getType() != SqlLexer.SPACES
                && token.getType() != SqlLexer.SINGLE_LINE_COMMENT
                && token.getType() != SqlLexer.MULTILINE_COMMENT
                && !token.getText().equalsIgnoreCase("as")
        )
            lastDBObject = null;
        if (token.getType() == SqlLexer.IDENTIFIER || token.getType() == SqlLexer.QUOTE_IDENTIFIER) {
            if (dbobjects != null) {
                String x = token.getText();
                if (x.length() > 0 && x.charAt(0) >= 'A' && x.charAt(0) <= 'z')
                    x = x.toUpperCase();
                if (x.startsWith("\"") && x.endsWith("\"") && x.length() > 1)
                    x = x.substring(1, x.length() - 1);
                if (lastDBObject != null) {
                    CustomToken customToken = new CustomToken(token);
                    customToken.setType(ALIAS);
                    customToken.setTableNameForAlias(lastDBObject);
                    lastDBObject = null;
                    return customToken;
                }
                if (dbobjects.contains(x)) {
                    CustomToken customToken = new CustomToken(token);
                    customToken.setType(DB_OBJECT);
                    lastDBObject = x;
                    return customToken;
                }
            }

            if (parameters != null) {
                String x = token.getText();
                if (x.length() > 0 && x.charAt(0) >= 'A' && x.charAt(0) <= 'z')
                    x = x.toUpperCase();
                if (x.startsWith("\"") && x.endsWith("\"") && x.length() > 1)
                    x = x.substring(1, x.length() - 1);
                if (parameters.contains(x)) {
                    CustomToken customToken = new CustomToken(token);
                    customToken.setType(PARAMETER);
                    return customToken;
                }
            }
            if (variables != null) {
                String x = token.getText();
                if (x.length() > 0 && x.charAt(0) >= 'A' && x.charAt(0) <= 'z')
                    x = x.toUpperCase();
                if (x.startsWith("\"") && x.endsWith("\"") && x.length() > 1)
                    x = x.substring(1, x.length() - 1);
                if (variables.contains(x)) {
                    CustomToken customToken = new CustomToken(token);
                    customToken.setType(VARIABLE);
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


    public TreeSet<String> getVariables() {
        return variables;
    }

    public void setVariables(TreeSet<String> variables) {
        this.variables = variables;
    }

    public TreeSet<String> getParameters() {
        return parameters;
    }

    public void setParameters(TreeSet<String> parameters) {
        this.parameters = parameters;
    }

    @Override
    protected Lexer createLexer(String s) {
        return new SqlLexer(CharStreams.fromString(s));
    }
}

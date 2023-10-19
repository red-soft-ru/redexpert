package org.underworldlabs.sqlLexer;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;

public class CustomToken implements Token {
    private String text;
    private int type;
    private int line;
    private int charPositionInLine;
    private int channel;
    private int tokenIndex;
    private int startIndex;
    private int stopIndex;
    private TokenSource tokenSource;
    private CharStream inputStream;

    private String tableNameForAlias;

    public CustomToken(Token token) {
        setText(token.getText());
        setType(token.getType());
        setLine(token.getLine());
        setCharPositionInLine(token.getCharPositionInLine());
        setChannel(token.getChannel());
        setTokenIndex(token.getTokenIndex());
        setStartIndex(token.getStartIndex());
        setStopIndex(token.getStopIndex());
        setTokenSource(token.getTokenSource());
        setInputStream(token.getInputStream());
        if (token instanceof CustomToken) {
            setTableNameForAlias(((CustomToken) token).getTableNameForAlias());
        }
    }

    @Override
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    @Override
    public int getCharPositionInLine() {
        return charPositionInLine;
    }

    public void setCharPositionInLine(int charPositionInLine) {
        this.charPositionInLine = charPositionInLine;
    }

    @Override
    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    @Override
    public int getTokenIndex() {
        return tokenIndex;
    }

    public void setTokenIndex(int tokenIndex) {
        this.tokenIndex = tokenIndex;
    }

    @Override
    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    @Override
    public int getStopIndex() {
        return stopIndex;
    }

    public void setStopIndex(int stopIndex) {
        this.stopIndex = stopIndex;
    }

    @Override
    public TokenSource getTokenSource() {
        return tokenSource;
    }

    public void setTokenSource(TokenSource tokenSource) {
        this.tokenSource = tokenSource;
    }

    @Override
    public CharStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(CharStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getTableNameForAlias() {
        return tableNameForAlias;
    }

    public void setTableNameForAlias(String tableNameForAlias) {
        this.tableNameForAlias = tableNameForAlias;
    }
}
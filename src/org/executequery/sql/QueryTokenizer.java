/*
 * QueryTokenizer.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.sql;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonToken;
import org.executequery.gui.text.syntax.Token;
import org.executequery.gui.text.syntax.TokenTypes;
import org.underworldlabs.sqlLexer.SqlLexer;
import org.underworldlabs.util.InterruptedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Takis Diakoumis
 */
public class QueryTokenizer {

    private String queryDelimiter = ";";

    private final List<Token> stringTokens;
    private final List<Token> singleLineCommentTokens;
    private final List<Token> multiLineCommentTokens;
    private final List<Token> declareBlockTokens;
    private static final int NORMAL = 0;
    private static final int AS = NORMAL + 1;
    private static final int DECLARE = AS + 1;
    private static final int BEGIN_END = DECLARE + 1;

    public QueryTokenizer() {
        stringTokens = new ArrayList<>();
        singleLineCommentTokens = new ArrayList<>();
        multiLineCommentTokens = new ArrayList<>();
        declareBlockTokens = new ArrayList<>();
        beginEndBlockTokens = new ArrayList<>();
    }

    public String removeComments(String query) {

        return removeAllCommentsFromQuery(query);
    }

    public List<DerivedQuery> tokenize(String query) {

        extractNotDelimiterTokens(query);

        List<DerivedQuery> derivedQueries = deriveQueries(query);
        for (DerivedQuery derivedQuery : derivedQueries) {

            if (Thread.interrupted())
                throw new InterruptedException();

            String noCommentsQuery = removeAllCommentsFromQuery(derivedQuery.getOriginalQuery());
            derivedQuery.setQueryWithoutComments(noCommentsQuery.trim());
        }

        return derivedQueries;
    }

    public QueryTokenized tokenizeFirstQuery(String query, String lowQuery, int startQueryIndex, String delimiter) {

        QueryTokenized firstQuery = firstQuery(query, delimiter, startQueryIndex, lowQuery);
        if (firstQuery.query != null) {

            String noCommentsQuery = removeAllCommentsFromQuery(firstQuery.query.getOriginalQuery()).trim();
            if (noCommentsQuery.isEmpty())
                firstQuery.query.setDerivedQuery("");
            firstQuery.query.setQueryWithoutComments(noCommentsQuery);
        }

        return firstQuery;
    }

    private String removeAllCommentsFromQuery(String query) {

        return removeSingleLineComments(removeMultiLineComments(query));
    }

    private String removeMultiLineComments(String query) {

        return removeTokensForType(SqlLexer.MULTILINE_COMMENT, query);
    }

    private String removeSingleLineComments(String query) {

        return removeTokensForType(SqlLexer.SINGLE_LINE_COMMENT, query);
    }

    private List<DerivedQuery> deriveQueries(String query) {

        if (!query.endsWith(queryDelimiter))
            query += queryDelimiter;

        int index = 0;
        int lastIndex = 0;
        boolean setTermToSet = false;

        List<DerivedQuery> queries = new ArrayList<>();
        while ((index = query.indexOf(queryDelimiter, index + 1)) != -1) {

            if (Thread.interrupted())
                throw new InterruptedException();

            if (notInAnyToken(index)) {

                String substring = query.substring(lastIndex, index);

                // if substring includes a set term command
                Pattern setTermPattern = Pattern.compile("Set(\\s+)term(\\s+)", Pattern.CASE_INSENSITIVE);

                Matcher setTermMatcher = setTermPattern.matcher(substring);
                boolean setTerm = setTermMatcher.find();

                while (setTerm) {

                    if (!notInAnyToken(setTermMatcher.end() + lastIndex - 1))
                        setTerm = setTermMatcher.find(setTermMatcher.end());
                    else
                        break;
                }

                if (setTerm) {

                    String oldDelimiter = queryDelimiter;
                    queryDelimiter = substring.substring(setTermMatcher.end()).trim();
                    queryDelimiter = removeAllCommentsFromQuery(queryDelimiter).trim();

                    if (queryDelimiter.isEmpty())
                        throw new RuntimeException("Delimiter cannot be empty:\n" + substring);

                    lastIndex = index + (oldDelimiter.length());
                    setTermToSet = !setTermToSet;
                    continue;
                }

                queries.add(new DerivedQuery(substring, null, queryDelimiter, setTermToSet));
                lastIndex = index + queryDelimiter.length();
            }
        }

        if (queries.isEmpty())
            queries.add(new DerivedQuery(query, null, queryDelimiter, setTermToSet));

        return queries;
    }

    private final List<Token> beginEndBlockTokens;


    private boolean notInAnyToken(int index) {

        boolean single = !(withinSingleLineComment(index, index));
        boolean multi = !(withinMultiLineComment(index, index));
        boolean quote = !(withinQuotedString(index, index));
        boolean declare = !(withinDeclareBlock(index, index));
        boolean beginEnd = !(withinBeginEndBlock(index, index));

        return single && multi && quote && declare && beginEnd;
    }

    private QueryTokenized firstQuery(String query, String delimiter, int startIndexQuery, String lowQuery) {

        int index = query.indexOf(delimiter);
        int lastIndex = 0;

        while (index != -1) {

            if (Thread.interrupted())
                throw new InterruptedException();

            if (notInAllTokens(index + startIndexQuery)) {

                String substring = query.substring(lastIndex, index);

                // if substring includes a set term command
                Pattern setTermPattern = Pattern.compile("Set(\\s+)term(\\s+)", Pattern.CASE_INSENSITIVE);

                Matcher setTermMatcher = setTermPattern.matcher(substring);
                boolean setTerm = setTermMatcher.find();
                while (setTerm) {

                    if (!notInAnyToken(setTermMatcher.end() - 1 + startIndexQuery))
                        setTerm = setTermMatcher.find(setTermMatcher.end());
                    else
                        break;
                }

                if (setTerm) {

                    String oldDelimiter = delimiter;
                    delimiter = substring.substring(setTermMatcher.end()).trim();
                    delimiter = removeAllCommentsFromQuery(delimiter).trim();

                    if (delimiter.isEmpty())
                        throw new RuntimeException("Delimiter cannot be empty:\n" + substring);

                    lastIndex = index + (oldDelimiter.length());
                    return new QueryTokenized(null, query.substring(lastIndex), lowQuery.substring(lastIndex), startIndexQuery + lastIndex, delimiter);
                }

                lastIndex = index + delimiter.length();
                return new QueryTokenized(new DerivedQuery(substring), query.substring(lastIndex), lowQuery.substring(lastIndex), startIndexQuery + lastIndex, delimiter);

            } else
                index = query.indexOf(delimiter, index + 1);
        }

        return new QueryTokenized(new DerivedQuery(query), "", "", lastIndex + startIndexQuery, delimiter);
    }

    private boolean notInAllTokens(int index) {

        return notInAnyToken(index);
    }

    public void extractNotDelimiterTokens(String query) {

        SqlLexer lexer = new SqlLexer(CharStreams.fromString(query));

        stringTokens.clear();
        singleLineCommentTokens.clear();
        multiLineCommentTokens.clear();
        beginEndBlockTokens.clear();
        declareBlockTokens.clear();
        int state = NORMAL;
        int beginCount = 0;
        int startIndex = 0;
        while (true) {

            org.antlr.v4.runtime.Token antlrToken = lexer.nextToken();
            if (antlrToken.getType() == SqlLexer.STRING_LITERAL)
                stringTokens.add(new Token(TokenTypes.STRING, antlrToken.getStartIndex(), antlrToken.getStopIndex()));
            if (antlrToken.getType() == SqlLexer.SINGLE_LINE_COMMENT)
                singleLineCommentTokens.add(new Token(TokenTypes.SINGLE_LINE_COMMENT, antlrToken.getStartIndex(), antlrToken.getStopIndex()));
            if (antlrToken.getType() == SqlLexer.MULTILINE_COMMENT)
                multiLineCommentTokens.add(new Token(TokenTypes.COMMENT, antlrToken.getStartIndex(), antlrToken.getStopIndex()));
            if (state == AS) {
                if (antlrToken.getType() != SqlLexer.SPACES && antlrToken.getType() != SqlLexer.MULTILINE_COMMENT && antlrToken.getType() != SqlLexer.SINGLE_LINE_COMMENT) {
                    if (antlrToken.getType() == SqlLexer.KEYWORD && antlrToken.getText().equalsIgnoreCase("declare")) {
                        state = DECLARE;
                        startIndex = antlrToken.getStartIndex();
                    } else if (antlrToken.getType() == SqlLexer.KEYWORD && antlrToken.getText().equalsIgnoreCase("begin")) {
                        state = BEGIN_END;
                        beginCount++;
                        startIndex = antlrToken.getStartIndex();
                    } else state = NORMAL;

                }
            } else if (state == DECLARE) {
                if (antlrToken.getType() == SqlLexer.OPERATOR && antlrToken.getText().equalsIgnoreCase(";")) {
                    declareBlockTokens.add(new Token(TokenTypes.DECLARE_BLOCK, startIndex, antlrToken.getStopIndex()));
                    state = AS;
                }
                if (antlrToken.getType() == SqlLexer.KEYWORD && antlrToken.getText().equalsIgnoreCase("as")) {
                    declareBlockTokens.add(new Token(TokenTypes.DECLARE_BLOCK, startIndex, antlrToken.getStopIndex()));
                    state = AS;
                }
            } else if (state == BEGIN_END) {
                if (antlrToken.getType() == SqlLexer.KEYWORD) {
                    if (antlrToken.getText().equalsIgnoreCase("begin") || antlrToken.getText().equalsIgnoreCase("case")) {
                        beginCount++;
                    } else if (antlrToken.getText().equalsIgnoreCase("end")) {
                        beginCount--;
                        if (beginCount <= 0) {
                            beginEndBlockTokens.add(new Token(TokenTypes.BEGIN_END_BLOCK, startIndex, antlrToken.getStopIndex()));
                            state = AS;
                        }
                    }
                }
            } else {
                if (antlrToken.getType() == SqlLexer.KEYWORD && antlrToken.getText().equalsIgnoreCase("as"))
                    state = AS;
                if (antlrToken.getType() == SqlLexer.KEYWORD && antlrToken.getText().equalsIgnoreCase("begin")) {
                    state = BEGIN_END;
                    beginCount++;
                    startIndex = antlrToken.getStartIndex();
                }
            }
            if (antlrToken.getType() == CommonToken.EOF)
                break;
        }

    }

    public void extractTokens(String query) {

        extractNotDelimiterTokens(query);
    }

    private String removeTokensForType(int typeAntlrToken, String query) {

        StringBuilder sb = new StringBuilder(query);

        List<org.antlr.v4.runtime.Token> tokenList = new ArrayList<>();
        SqlLexer lexer = new SqlLexer(CharStreams.fromString(query));

        while (true) {

            org.antlr.v4.runtime.Token antlrToken = lexer.nextToken();

            if (antlrToken.getType() == typeAntlrToken)
                tokenList.add(antlrToken);
            if (antlrToken.getType() == SqlLexer.EOF)
                break;
        }

        Collections.reverse(tokenList);
        for (org.antlr.v4.runtime.Token token : tokenList)
            sb.delete(token.getStartIndex(), token.getStopIndex() + 1);

        return sb.toString();
    }


    private boolean withinMultiLineComment(int start, int end) {

        return contains(multiLineCommentTokens, start, end);
    }

    private boolean withinSingleLineComment(int start, int end) {

        return contains(singleLineCommentTokens, start, end);
    }

    private boolean withinQuotedString(int start, int end) {

        return contains(stringTokens, start, end);
    }

    private boolean withinDeclareBlock(int start, int end) {

        return contains(declareBlockTokens, start, end);
    }

    private boolean withinBeginEndBlock(int start, int end) {

        return contains(beginEndBlockTokens, start, end);
    }

    private boolean contains(List<Token> tokens, int start, int end) {

        for (Token token : tokens)
            if (token.contains(start, end))
                return true;

        return false;
    }

    private List<Token> getTokensBetween(List<Token> tokens, int start, int end) {

        ArrayList<Token> result = new ArrayList<>();
        if (tokens != null) {
            for (Token token : tokens) {
                if (token.getStartIndex() >= start && token.getEndIndex() <= end)
                    result.add(token);
            }
        }

        return result;
    }


    public static class QueryTokenized {

        public DerivedQuery query;
        public String script;
        public String lowScript;
        public int startIndex;
        public String delimiter;

        public QueryTokenized(DerivedQuery query, String script, String lowScript, int startIndex, String delimiter) {
            this.query = query;
            this.script = script;
            this.lowScript = lowScript;
            this.startIndex = startIndex;
            this.delimiter = delimiter;
        }

    }

}







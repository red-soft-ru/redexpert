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
import org.executequery.Constants;
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
    private final String beginPattern = "begin";
    private final String endPattern = "end";

    private final List<Token> stringTokens;
    private final List<Token> singleLineCommentTokens;
    private final List<Token> multiLineCommentTokens;
    private final List<Token> declareBlockTokens;
    private final Matcher declareBlockMatcher;

    private List<String> beginEndBlocks;

    public QueryTokenizer() {
        stringTokens = new ArrayList<>();
        singleLineCommentTokens = new ArrayList<>();
        multiLineCommentTokens = new ArrayList<>();
        declareBlockTokens = new ArrayList<>();
        beginEndBlockTokens = new ArrayList<>();
        declareBlockMatcher = Pattern.compile(TokenTypes.DECLARE_BLOCK_REGEX, Pattern.DOTALL).matcher(Constants.EMPTY);
    }

    public String removeComments(String query) {

        return removeAllCommentsFromQuery(query);
    }

    public List<DerivedQuery> tokenize(String query) {

        extractStringAndCommentsTokens(query);

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

        return single && multi && quote;
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

    private void extractDeclareBlockTokens(String query) {

        addTokensForMatcherWhenNotInString(declareBlockMatcher, query, declareBlockTokens);
    }

    private int indexOfStringWithSpaces(String query, String word) {
        return indexOfStringWithSpaces(query, word, 0);
    }

    private int indexOfStringWithSpaces(String query, String word, int startIndex) {

        String[] spaces = {" ", "\n", ";"};
        int index = query.length();

        for (String iSpace : spaces) {
            for (String jSpace : spaces) {

                int ind = query.indexOf(iSpace + word + jSpace, startIndex);
                if (ind != -1 && ind < index)
                    index = ind;
            }
        }

        return (index == query.length()) ? -1 : index + 1;
    }

    private String replace(String query, int start, String replacement) {

        String startQuery = query.substring(0, start);
        startQuery += replacement;
        startQuery += query.substring(start + replacement.length());

        return startQuery;
    }

    private String addBeginEndBlock(String query) {

        int start = indexOfStringWithSpaces(query, beginPattern);
        int end = -1;

        if (start > 0 && notInAnyToken(start)) {

            end = indexOfStringWithSpaces(query, endPattern);
            if (end > 0 && notInAnyToken(end)) {

                query = replace(query, start, "nigeb");

                int ind = indexOfStringWithSpaces(query, beginPattern);
                while (ind > 0 && ind < end) {

                    query = addBeginEndBlock(query);
                    query = replace(query, ind, "nigeb");
                    ind = indexOfStringWithSpaces(query, beginPattern);
                    end = indexOfStringWithSpaces(query, endPattern);

                    while (!notInAnyToken(end))
                        end = query.indexOf(end);
                }
            }
        }

        List<Token> tokens = beginEndBlockTokens;
        if (start >= 0 && end >= 0) {
            tokens.add(new Token(TokenTypes.BEGIN_END_BLOCK, start, end));
            query = replace(query, end, "dne");
            beginEndBlocks.add(query.substring(start, end + 3));
        }

        return query;
    }

    private void extractBeginEndBlockTokens(String query) {

        beginEndBlocks = new ArrayList<>();
        for (int index = indexOfStringWithSpaces(query, beginPattern); index >= 0; index = indexOfStringWithSpaces(query, beginPattern, index + 1)) {
            if (notInAnyToken(index))
                query = addBeginEndBlock(query);
        }

    }

    public void extractStringAndCommentsTokens(String query) {

        SqlLexer lexer = new SqlLexer(CharStreams.fromString(query));

        stringTokens.clear();
        singleLineCommentTokens.clear();
        multiLineCommentTokens.clear();

        while (true) {

            org.antlr.v4.runtime.Token antlrToken = lexer.nextToken();

            if (antlrToken.getType() == SqlLexer.STRING_LITERAL)
                stringTokens.add(new Token(TokenTypes.STRING, antlrToken.getStartIndex(), antlrToken.getStopIndex()));
            if (antlrToken.getType() == SqlLexer.SINGLE_LINE_COMMENT)
                singleLineCommentTokens.add(new Token(TokenTypes.SINGLE_LINE_COMMENT, antlrToken.getStartIndex(), antlrToken.getStopIndex()));
            if (antlrToken.getType() == SqlLexer.MULTILINE_COMMENT)
                multiLineCommentTokens.add(new Token(TokenTypes.COMMENT, antlrToken.getStartIndex(), antlrToken.getStopIndex()));
            if (antlrToken.getType() == CommonToken.EOF)
                break;
        }

    }

    public void extractTokens(String query) {

        extractStringAndCommentsTokens(query);
    }

    private void addTokensForMatcherWhenNotInString(Matcher matcher, String query, List<Token> tokens) {

        tokens.clear();
        matcher.reset(query);
        int startIndex = 0;

        while (matcher.find()) {

            int start = matcher.start() + startIndex;
            int end = matcher.end();

            query = query.substring(end);
            end = end + startIndex;
            startIndex = end;

            int endOffset = end;

            if (!withinQuotedString(start, endOffset)) {
                int tokenStyle = (matcher == declareBlockMatcher) ? TokenTypes.DECLARE_BLOCK : TokenTypes.COMMENT;
                tokens.add(new Token(tokenStyle, start, end));
            }

            matcher.reset(query);
        }

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







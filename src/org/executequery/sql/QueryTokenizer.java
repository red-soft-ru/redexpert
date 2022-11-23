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

    private /*static final*/ String queryDelimiter = ";";

    private final List<Token> stringTokens;

    private final List<Token> singleLineCommentTokens;

    private final List<Token> multiLineCommentTokens;

    String beginPattern = "begin";
    String endPattern = "end";

    List<String> begin_end_blocks;
    private final List<Token> declareBlockTokens;

    public String removeComments(String query) {

        return removeAllCommentsFromQuery(query);
    }

    public List<DerivedQuery> tokenize(String query) {
        extractStringAndCommentsTokens(query);
        List<DerivedQuery> derivedQueries = deriveQueries(query);
        for (DerivedQuery derivedQuery : derivedQueries) {

            if (Thread.interrupted()) {

                throw new InterruptedException();
            }

            String noCommentsQuery = removeAllCommentsFromQuery(derivedQuery.getOriginalQuery());
            derivedQuery.setQueryWithoutComments(noCommentsQuery.trim());
        }
        return derivedQueries;
    }

    public QueryTokenized tokenizeFirstQuery(String query,String lowQuery,int startQueryIndex, String delimiter) {

        QueryTokenized fquery = firstQuery(query, delimiter, startQueryIndex, lowQuery);
        if (fquery.query != null) {
            String noCommentsQuery = removeAllCommentsFromQuery(fquery.query.getOriginalQuery()).trim();
            if (noCommentsQuery.isEmpty())
                fquery.query.setDerivedQuery("");
            fquery.query.setQueryWithoutComments(noCommentsQuery);
        }
        return fquery;
    }

    private String removeAllCommentsFromQuery(String query) {

        String newQuery = removeMultiLineComments(query);

        return removeSingleLineComments(newQuery);
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

        List<DerivedQuery> queries = new ArrayList<DerivedQuery>();

        while ((index = query.indexOf(queryDelimiter, index + 1)) != -1) {

            if (Thread.interrupted()) {

                throw new InterruptedException();
            }

            if (notInAnyToken(index)) {

                String substring = query.substring(lastIndex, index);

                // if substring includes a set term command
                Pattern p = Pattern.compile("Set(\\s+)term(\\s+)", Pattern.CASE_INSENSITIVE);

                Matcher m = p.matcher(substring);
                boolean setTerm = m.find();
                while (setTerm) {
                    if (notInAnyToken(m.end() - 1))
                        break;
                    else {
                        setTerm = m.find(m.end());
                    }
                }
                if (setTerm) {
                    String oldDelimiter = queryDelimiter;
                    queryDelimiter = substring.substring(m.end()).trim();
                    queryDelimiter = removeAllCommentsFromQuery(queryDelimiter).trim();
                    if (queryDelimiter.isEmpty())
                        throw new RuntimeException("Delimiter cannot be empty:\n" +
                                substring);
                    lastIndex = index + (oldDelimiter.length());
                    continue;
                }

                queries.add(new DerivedQuery(substring));
                lastIndex = index + queryDelimiter.length();/*1;*/
            }

        }

        if (queries.isEmpty()) {

            queries.add(new DerivedQuery(query));
        }

        return queries;
    }

    private final List<Token> beginEndBlockTokens;


    private boolean notInAnyToken(int index) {

        return !(withinMultiLineComment(index, index))
                && !(withinSingleLineComment(index, index))
                && !(withinQuotedString(index, index));
    }

    private final Matcher declareBlockMatcher;

    public QueryTokenizer() {

        stringTokens = new ArrayList<Token>();

        singleLineCommentTokens = new ArrayList<Token>();

        multiLineCommentTokens = new ArrayList<Token>();
        declareBlockTokens = new ArrayList<>();
        declareBlockMatcher = Pattern.compile(TokenTypes.DECLARE_BLOCK_REGEX, Pattern.DOTALL).matcher(Constants.EMPTY);
        beginEndBlockTokens = new ArrayList<>();
    }

    private QueryTokenized firstQuery(String query, String delimiter, int startIndexQuery, String lowQuery) {

        int index = 0;
        int lastIndex = 0;

        index = query.indexOf(delimiter);
        boolean cycleContinue = true;

        while ((index != -1) && cycleContinue) {
            cycleContinue = false;

            if (Thread.interrupted()) {

                throw new InterruptedException();
            }

            if (notInAllTokens(index + startIndexQuery)) {

                String substring = query.substring(lastIndex, index);

                // if substring includes a set term command
                Pattern p = Pattern.compile("Set(\\s+)term(\\s+)", Pattern.CASE_INSENSITIVE);

                Matcher m = p.matcher(substring);
                boolean setTerm = m.find();
                while (setTerm) {
                    if (notInAnyToken(m.end() - 1 + startIndexQuery))
                        break;
                    else {
                        setTerm = m.find(m.end());
                    }
                }
                if (setTerm) {
                    String oldDelimiter = delimiter;
                    delimiter = substring.substring(m.end()).trim();
                    delimiter = removeAllCommentsFromQuery(delimiter).trim();
                    if (delimiter.isEmpty())
                        throw new RuntimeException("Delimiter cannot be empty:\n" +
                                substring);
                    lastIndex = index + (oldDelimiter.length());
                    return new QueryTokenized(null, query.substring(lastIndex), lowQuery.substring(lastIndex), startIndexQuery + lastIndex, delimiter);
                }
                lastIndex = index + delimiter.length();/*1;*/
                return new QueryTokenized(new DerivedQuery(substring), query.substring(lastIndex), lowQuery.substring(lastIndex), startIndexQuery + lastIndex, delimiter);
            } else {
                cycleContinue = true;
                index = query.indexOf(delimiter, index + 1);
            }

        }
        return new QueryTokenized(new DerivedQuery(query), "", "", lastIndex + startIndexQuery, delimiter);
    }

    private boolean notInAllTokens(int index) {
        return notInAnyToken(index);
        /*boolean wdb = withinDeclareBlock(index, index);
        boolean wbeb = withinBeginEndBlock(index, index);
        return notAny && !wdb && !wbeb;*/
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
        for (int i = 0; i < spaces.length; i++) {
            for (int g = 0; g < spaces.length; g++) {
                int ind = query.indexOf(spaces[i] + word + spaces[g], startIndex);
                if (ind != -1 && ind < index)
                    index = ind;
            }
        }
        if (index == query.length())
            return -1;
        else return index + 1;
    }

    private String replace(String query, int start, String replacement) {
        String startQuery = query.substring(0, start);
        startQuery += replacement;
        startQuery += query.substring(start + replacement.length());
        return startQuery;
    }

    private String addBeginEndBlock(String query) {
        List<Token> tokens = beginEndBlockTokens;
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
                    while (!notInAnyToken(end)) {
                        end = query.indexOf(end);
                    }
                }
            }
        }
        if (start >= 0 && end >= 0) {
            tokens.add(new Token(TokenTypes.BEGIN_END_BLOCK, start, end));
            query = replace(query, end, "dne");
            begin_end_blocks.add(query.substring(start, end + 3));
        }
        return query;
    }

    private void extractBeginEndBlockTokens(String query) {

        begin_end_blocks = new ArrayList<>();
        for (int index = indexOfStringWithSpaces(query, beginPattern); index >= 0; index = indexOfStringWithSpaces(query, beginPattern, index + 1)) {
            if (notInAnyToken(index)) {
                query = addBeginEndBlock(query);
            }

        }

    }

    public void extractStringAndCommentsTokens(String query) {
        SqlLexer lexer = new SqlLexer(CharStreams.fromString(query));
        stringTokens.clear();
        singleLineCommentTokens.clear();
        multiLineCommentTokens.clear();
        while (true) {
            org.antlr.v4.runtime.Token at = lexer.nextToken();
            if (at.getType() == SqlLexer.STRING_LITERAL)
                stringTokens.add(new Token(TokenTypes.STRING, at.getStartIndex(), at.getStopIndex()));
            if (at.getType() == SqlLexer.SINGLE_LINE_COMMENT)
                singleLineCommentTokens.add(new Token(TokenTypes.SINGLE_LINE_COMMENT, at.getStartIndex(), at.getStopIndex()));
            if (at.getType() == SqlLexer.MULTILINE_COMMENT)
                multiLineCommentTokens.add(new Token(TokenTypes.COMMENT, at.getStartIndex(), at.getStopIndex()));
            if (at.getType() == CommonToken.EOF)
                break;
        }
    }

    public void extractTokens(String query) {
        extractStringAndCommentsTokens(query);
        /*extractDeclareBlockTokens(query);
        extractBeginEndBlockTokens(query);*/
    }

    private void addTokensForMatcherWhenNotInString(Matcher matcher, String query, List<Token> tokens) {

        tokens.clear();
        matcher.reset(query);
        int startIndex=0;

        while (matcher.find()) {
            int start = matcher.start()+startIndex;
            int end = matcher.end();
            query = query.substring(end);
            end = end+startIndex;
            startIndex = end;

            int endOffset = end;

            if (!withinQuotedString(start, endOffset)) {

                if (matcher == declareBlockMatcher)
                    tokens.add(new Token(TokenTypes.DECLARE_BLOCK, start, end));
                else tokens.add(new Token(TokenTypes.COMMENT, start, end));
            }
            matcher.reset(query);

        }

    }

    private String removeTokensForType(int typeAntlrToken, String query) {


        StringBuilder sb = new StringBuilder(query);

        List<org.antlr.v4.runtime.Token> tokenList = new ArrayList<>();
        SqlLexer lexer = new SqlLexer(CharStreams.fromString(query));
        while (true) {
            org.antlr.v4.runtime.Token at = lexer.nextToken();
            if (at.getType() == typeAntlrToken)
                tokenList.add(at);
            if (at.getType() == SqlLexer.EOF)
                break;
        }
        Collections.reverse(tokenList);
        for (org.antlr.v4.runtime.Token token : tokenList) {
            sb.delete(token.getStartIndex(), token.getStopIndex() + 1);
        }
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

        for (Token token : tokens) {

            if (token.contains(start, end)) {

                return true;
            }
        }

        return false;

    }

    private List<Token> getTokensBetween(List<Token> tokens, int start, int end) {
        ArrayList<Token> result = new ArrayList<>();
        if (tokens != null)
            for (Token token : tokens) {
                if (token.getStartIndex() >= start && token.getEndIndex() <= end)
                    result.add(token);
            }
        return result;
    }


    public class QueryTokenized {
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







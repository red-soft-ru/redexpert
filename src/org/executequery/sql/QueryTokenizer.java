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

import org.executequery.Constants;
import org.executequery.gui.text.syntax.Token;
import org.executequery.gui.text.syntax.TokenTypes;
import org.underworldlabs.util.InterruptedException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Takis Diakoumis
 */
public class QueryTokenizer {

    private /*static final*/ String QUERY_DELIMITER = ";";

    private List<Token> stringTokens;

    private List<Token> singleLineCommentTokens;

    private List<Token> multiLineCommentTokens;

    String beginPattern = "begin";
    String endPattern = "end";

    private Matcher stringMatcher;

    private Matcher singleLineCommentMatcher;

    private Matcher multiLineCommentMatcher;
    List<String> begin_end_blocks;

    private static final String QUOTE_REGEX = "'((\\?>[^']*\\+)(\\?>'{2}[^']*\\+)*\\+)'|'.*'";//"'((?>[^']*+)(?>'{2}[^']*+)*+)'|'.*";

    private static final String MULTILINE_COMMENT_REGEX = "/\\*.*?\\*/";
    //                                                    "/\\*(?:.|[\\n\\r])*?\\*/|/\\*.*";
//                                                        "/\\*((?>[^\\*/]*+)*+)\\*/|/\\*.*";
    private List<Token> declareBlockTokens;

    public String removeComments(String query) {

        return removeAllCommentsFromQuery(query);
    }

    public List<DerivedQuery> tokenize(String query) {

        extractQuotedStringTokens(query);
        extractSingleLineCommentTokens(query);
        extractMultiLineCommentTokens(query);

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
        String noCommentsQuery = removeAllCommentsFromQuery(fquery.query.getOriginalQuery());
        fquery.query.setQueryWithoutComments(noCommentsQuery.trim());
        return fquery;
    }

    private String removeAllCommentsFromQuery(String query) {

        String newQuery = removeMultiLineComments(query);

        return removeSingleLineComments(newQuery);
    }

    private String removeMultiLineComments(String query) {

        return removeTokensForMatcherWhenNotInString(multiLineCommentMatcher, query);
    }

    private String removeSingleLineComments(String query) {

        return removeTokensForMatcherWhenNotInString(singleLineCommentMatcher, query);
    }

    private List<DerivedQuery> deriveQueries(String query) {

        int index = 0;
        int lastIndex = 0;

        List<DerivedQuery> queries = new ArrayList<DerivedQuery>();

        while ((index = query.indexOf(QUERY_DELIMITER, index + 1)) != -1) {

            if (Thread.interrupted()) {

                throw new InterruptedException();
            }

            if (notInAnyToken(index)) {

                String substring = query.substring(lastIndex, index);

                // if substring includes a set term command
                Pattern p = Pattern.compile("Set(\\s+)term(\\s+)", Pattern.CASE_INSENSITIVE);

                Matcher m = p.matcher(substring);

                if (m.find()) {
                    QUERY_DELIMITER = substring.substring(m.end(), substring.length()).trim();
                    lastIndex = index + (substring.length() - m.end());
                    continue;
                }

                queries.add(new DerivedQuery(substring));
                lastIndex = index + QUERY_DELIMITER.length();/*1;*/
            }

        }

        if (queries.isEmpty()) {

            queries.add(new DerivedQuery(query));
        }

        return queries;
    }

    private List<Token> beginEndBlockTokens;


    private boolean notInAnyToken(int index) {

        return !(withinMultiLineComment(index, index))
                && !(withinSingleLineComment(index, index))
                && !(withinQuotedString(index, index));
    }

    private Matcher declareBlockMatcher;

    private void extractSingleLineCommentTokens(String query) {

        addTokensForMatcherWhenNotInString(singleLineCommentMatcher, query, singleLineCommentTokens);
    }

    private void extractMultiLineCommentTokens(String query) {

        addTokensForMatcherWhenNotInString(multiLineCommentMatcher, query, multiLineCommentTokens);
    }

    public QueryTokenizer() {

        stringTokens = new ArrayList<Token>();
        stringMatcher = Pattern.compile(QUOTE_REGEX).matcher(Constants.EMPTY);

        singleLineCommentTokens = new ArrayList<Token>();
        singleLineCommentMatcher = Pattern.compile(
                TokenTypes.SINGLE_LINE_COMMENT_REGEX, Pattern.MULTILINE).
                matcher(Constants.EMPTY);

        multiLineCommentTokens = new ArrayList<Token>();
        multiLineCommentMatcher = Pattern.compile(
                MULTILINE_COMMENT_REGEX, Pattern.DOTALL).
                matcher(Constants.EMPTY);
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

                if (m.find()) {
                    delimiter = substring.substring(m.end(), substring.length()).trim();
                    lastIndex = index + (substring.length() - m.end());
                    return new QueryTokenized(null, query.substring(lastIndex),lowQuery.substring(lastIndex),startIndexQuery+lastIndex, delimiter);
                }
                lastIndex = index + delimiter.length();/*1;*/
                return new QueryTokenized(new DerivedQuery(substring), query.substring(lastIndex),lowQuery.substring(lastIndex), startIndexQuery+lastIndex, delimiter);
            } else {
                cycleContinue = true;
                index = query.indexOf(delimiter, index + 1);
            }

        }
        return new QueryTokenized(new DerivedQuery(query), "", "", lastIndex + startIndexQuery, delimiter);
    }

    private boolean notInAllTokens(int index) {
        boolean notAny = notInAnyToken(index);
        boolean wdb = withinDeclareBlock(index, index);
        boolean wbeb = withinBeginEndBlock(index, index);
        return notAny && !wdb && !wbeb;
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

    public void extractTokens(String query) {
        extractQuotedStringTokens(query);
        extractSingleLineCommentTokens(query);
        extractMultiLineCommentTokens(query);
        extractDeclareBlockTokens(query);
        extractBeginEndBlockTokens(query);
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

            if (isSingleLineMatcher(matcher)) {

                endOffset = start + 2;
            }

            if (!withinQuotedString(start, endOffset)) {

                if (matcher == declareBlockMatcher)
                    tokens.add(new Token(TokenTypes.DECLARE_BLOCK, start, end));
                else tokens.add(new Token(TokenTypes.COMMENT, start, end));
            }
            matcher.reset(query);

        }

    }

    private String removeTokensForMatcherWhenNotInString(Matcher matcher, String query) {

        int start = 0, end = 0, endOffset = 0;

        StringBuilder sb = new StringBuilder(query);
        matcher.reset(query);

        while (matcher.find(start)) {

            start = matcher.start();
            end = matcher.end();

            extractQuotedStringTokens(sb.toString());

            endOffset = end;

            if (isSingleLineMatcher(matcher)) {

                endOffset = start + 2;
            }

            if (!withinQuotedString(start, endOffset)) {

                sb.delete(start, end);
                matcher.reset(sb);

            } else {

                start = end;
            }

        }

        return sb.toString();
    }

    private boolean isSingleLineMatcher(Matcher matcher) {

        return (matcher == singleLineCommentMatcher);
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

    private void extractQuotedStringTokens(String query) {

        stringTokens.clear();
        stringMatcher.reset(query);

        while (stringMatcher.find()) {

            stringTokens.add(new Token(TokenTypes.STRING,
                    stringMatcher.start(), stringMatcher.end()));
        }

    }
    public class QueryTokenized
    {
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







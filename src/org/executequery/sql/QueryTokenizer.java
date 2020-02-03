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

    private Matcher stringMatcher;

    private Matcher singleLineCommentMatcher;

    private Matcher multiLineCommentMatcher;

    private static final String QUOTE_REGEX = "'((\\?>[^']*\\+)(\\?>'{2}[^']*\\+)*\\+)'|'.*'";//"'((?>[^']*+)(?>'{2}[^']*+)*+)'|'.*";

    private static final String MULTILINE_COMMENT_REGEX = "/\\*.*?\\*/";
    //                                                    "/\\*(?:.|[\\n\\r])*?\\*/|/\\*.*";
//                                                        "/\\*((?>[^\\*/]*+)*+)\\*/|/\\*.*";

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
    }

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
            derivedQuery.setDerivedQuery(noCommentsQuery.trim());
        }

        return derivedQueries;
    }

    public QueryTokenized tokenizeFirstQuery(String query,String lowQuery,int startQueryIndex, String delimiter) {

        QueryTokenized fquery = firstQuery(query,delimiter,startQueryIndex,lowQuery);
        String noCommentsQuery = removeAllCommentsFromQuery(fquery.query.getOriginalQuery());
        fquery.query.setDerivedQuery(noCommentsQuery.trim());

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

        if (query.toLowerCase().startsWith("create")) {
            queries.add(new DerivedQuery(query));
            return queries;
        }

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

    private QueryTokenized firstQuery(String query, String delimiter, int startIndexQuery, String lowQuery) {

        int index = 0;
        int lastIndex = 0;


        if (lowQuery.startsWith("create")) {
            return new QueryTokenized(new DerivedQuery(query), "", "", startIndexQuery + query.length(), delimiter);
        }
        index = query.indexOf(delimiter);
        boolean cycleContinue = true;

        while ((index != -1)&&cycleContinue) {
            cycleContinue=false;

            if (Thread.interrupted()) {

                throw new InterruptedException();
            }

            if (notInAnyToken(index + startIndexQuery)) {

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
                index = query.indexOf(delimiter,index+1);
            }

        }
        return new QueryTokenized(new DerivedQuery(query), "","",lastIndex+startIndexQuery, delimiter);
    }




    private boolean notInAnyToken(int index) {

        return !(withinMultiLineComment(index, index))
                && !(withinSingleLineComment(index, index))
                && !(withinQuotedString(index, index));
    }

    private void extractSingleLineCommentTokens(String query) {

        addTokensForMatcherWhenNotInString(singleLineCommentMatcher, query, singleLineCommentTokens);
    }

    private void extractMultiLineCommentTokens(String query) {

        addTokensForMatcherWhenNotInString(multiLineCommentMatcher, query, multiLineCommentTokens);
    }

    public void extractTokens(String query)
    {
        extractQuotedStringTokens(query);
        extractSingleLineCommentTokens(query);
        extractMultiLineCommentTokens(query);
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

                tokens.add(new Token(TokenTypes.COMMENT, start, end));
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







/*
 * QueryTokenizer.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.executequery.Constants;
import org.executequery.gui.text.syntax.Token;
import org.executequery.gui.text.syntax.TokenTypes;
import org.underworldlabs.util.InterruptedException;

/** 
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class QueryTokenizer {

    private static final String QUERY_DELIMITER = ";";

    private List<Token> stringTokens;
    
    private List<Token> singleLineCommentTokens;
    
    private List<Token> multiLineCommentTokens;

    private Matcher stringMatcher;
    
    private Matcher singleLineCommentMatcher;
    
    private Matcher multiLineCommentMatcher;
    
    private static final String QUOTE_REGEX = "'((?>[^']*+)(?>'{2}[^']*+)*+)'|'.*";
    
    private static final String MULTILINE_COMMENT_REGEX = "/\\*(?:.|[\\n\\r])*?\\*/|/\\*.*"; 
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

                queries.add(new DerivedQuery(substring));
                lastIndex = index + 1;
            }

        }

        if (queries.isEmpty()) {
            
            queries.add(new DerivedQuery(query));
        }
        
        return queries;
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

    private void addTokensForMatcherWhenNotInString(Matcher matcher, String query, List<Token> tokens) {
        
        tokens.clear();
        matcher.reset(query);

        while (matcher.find()) {

            int start = matcher.start();
            int end = matcher.end();

            int endOffset = end; 
            
            if (isSingleLineMatcher(matcher)) {
                
                endOffset = start + 2;
            }

            if (!withinQuotedString(start, endOffset)) {

                tokens.add(new Token(TokenTypes.COMMENT, start, end));
            }

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

}






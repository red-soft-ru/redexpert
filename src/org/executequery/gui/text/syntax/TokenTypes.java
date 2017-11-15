/*
 * TokenTypes.java
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

package org.executequery.gui.text.syntax;

/**
 * The TokenTypes interface defines the integer constants representing
 * different types of tokens, for use with any languages.  The constants are
 * used in symbols to represent the types of similar tokens, and in scanners as
 * scanner states, and in highlighters to determine the colour or style of
 * tokens.  There is also an array typeNames of textual names, indexed by type,
 * for descriptive purposes.
 *
 * <p>The UNRECOGNIZED constant (zero) is for tokens which are completely
 * unrecognized, usually consisting of a single illegal character.  Other error
 * tokens are represented by negative types, where -t represents an incomplete
 * or malformed token of type t.  An error token usually consists of the
 * maximal legal substring of the source text.
 *
 */
/**
 *
 * @author   Takis Diakoumis
 */
public interface TokenTypes {

    public static final String OPEN_COMMENT = "/*";
    public static final String CLOSE_COMMENT = "*/";
    public static final String SINGLE_LINE_COMMENT_STRING = "--";

    public static final String SINGLE_LINE_COMMENT_REGEX = "--.*$";

//    public static final String QUOTE_REGEX = "'([^'\r\n])+'|'.*";
    
    public static final String QUOTE_REGEX = "\'((?>[^\']*+)(?>\'{2}[^\']*+)*+)\'|\'.*";

    public static final String NUMBER_REGEX = "\\b(([0-9]+)\\.?[0-9]*)\\b";
    //public static final String NUMBER_REGEX = "\\b(([0-9]+)\\.?[0-9]+[^\\.])\\b";

    public static final String BRACES_REGEX = "\\(|\\{|\\[|\\)|\\]|\\}";

    public static final String OPERATOR_REGEX = "(\\;|\\.|\\,|~|\\?|\\:|" +
                                                "\\+|\\-|\\&|\\||\\\\|\\!" + 
                                                "|\\=|\\*|\\^|%|\\$|/|\\<|\\>)+";

    public static final String[] MATCHERS = {
        "keyword",
        "operator",
        "number",
        "literals",
        "braces",
        "string",
        "single-line-comment"
    };

    public static final int KEYWORD_MATCH = 0,
                            OPERATOR_MATCH = 1,
                            NUMBER_MATCH = 2,
                            LITERALS_MATCH = 3,
                            BRACES_MATCH = 4,
                            SINGLE_LINE_COMMENT_MATCH = 6,
                            STRING_MATCH = 5;
    
    public static final int UNRECOGNIZED = 0,
                            WORD = 1,
                            NUMBER = 2,
                            COMMENT = 3,
                            KEYWORD = 4,
                            KEYWORD2 = 5,
                            LITERAL = 6,
                            STRING = 7,
                            OPERATOR = 8,
                            BRACKET = 9,
                            SINGLE_LINE_COMMENT = 10,
                            BRACKET_HIGHLIGHT = 11,
                            BRACKET_HIGHLIGHT_ERR = 12;
        

    /**
     * The names of the token types, indexed by type, are provided for
     * descriptive purposes.
     */
    public static final String[] typeNames = {
        "bad token",
        "normal",
        "number",
        "comment",
        "keyword",
        "keyword 2",
        "literal",
        "string",
        "operator",
        "bracket",
        "single line comment",
        "bracket highlight at cursor",
        "bracket highlight at cursor error"
    };
}
















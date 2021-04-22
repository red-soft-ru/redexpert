/*
 * SQLSyntaxDocument.java
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

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.executequery.Constants;
import org.executequery.gui.editor.QueryEditorSettings;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.underworldlabs.sqlLexer.SqlLexer;

import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

/**
 * @author Takis Diakoumis
 */
public class SQLSyntaxDocument extends RSyntaxDocument
         {

    /**
     * The document root element
     */
    private Element rootElement;

    /**
     * The text component owner of this document
     */
    private JTextComponent textComponent;

    /**
     * Convert tabs to spaces
     */
    private boolean tabsToSpaces;

    /**
     * tracks brace positions
     */
    private Vector<Token> braceTokens;

    /**
     * the current text insert mode
     */
    private int insertMode;

    /**
     * tracks string literal entries (quotes)
     */
    private List<Token> stringTokens;

    /* syntax matchers */
    private TokenMatcher[] matchers;

    //public SQLSyntaxDocument() {
       // this(null, null);
    //}

    /**
     * Sets the SQL keywords to be applied to this document.
     *
     * @param keywords - the keywords list
     * @param reset
     */
    private TreeSet keywords;
    private TreeSet dbobjects;

    public void setTextComponent(JTextComponent textComponent) {
        this.textComponent = textComponent;
    }



    public void setTabsToSpaces(boolean tabsToSpaces) {
        this.tabsToSpaces = tabsToSpaces;
    }

    private Token getAvailableBraceToken() {
        for (int i = 0, k = braceTokens.size(); i < k; i++) {
            Token token = braceTokens.get(i);
            if (!token.isValid()) {
                return token;
            }
        }
        Token token = new Token(-1, -1, -1);
        braceTokens.add(token);
        return token;
    }

    private boolean hasValidBraceTokens() {
        int size = braceTokens.size();
        if (size == 0) {
            return false;
        } else {
            for (int i = 0; i < size; i++) {
                Token token = braceTokens.get(i);
                if (token.isValid()) {
                    return true;
                }
            }
        }
        return false;
    }








    private int getMatchingBraceOffset(int offset, char brace, String text) {
        int thisBraceCount = 0;
        int matchingBraceCount = 0;
        char braceMatch = getMatchingBrace(brace);
        char[] chars = text.toCharArray();

        if (isOpenBrace(brace)) {

            for (int i = offset; i < chars.length; i++) {
                if (chars[i] == brace) {
                    thisBraceCount++;
                } else if (chars[i] == braceMatch) {
                    matchingBraceCount++;
                }

                if (thisBraceCount == matchingBraceCount) {
                    return i;
                }
            }

        } else {

            for (int i = offset; i >= 0; i--) {
                if (chars[i] == brace) {
                    thisBraceCount++;
                } else if (chars[i] == braceMatch) {
                    matchingBraceCount++;
                }

                if (thisBraceCount == matchingBraceCount) {
                    return i;
                }
            }

        }

        return -1;
    }

    private char getMatchingBrace(char brace) {
        switch (brace) {
            case '(':
                return ')';
            case ')':
                return '(';
            case '[':
                return ']';
            case ']':
                return '[';
            case '{':
                return '}';
            case '}':
                return '{';
            default:
                return 0;
        }
    }

    private boolean isOpenBrace(char brace) {
        switch (brace) {
            case '(':
            case '[':
            case '{':
                return true;
        }
        return false;
    }

    private boolean isBrace(char charAt) {
        for (int i = 0; i < Constants.BRACES.length; i++) {
            if (charAt == Constants.BRACES[i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * temp string buffer for insertion text
     */
    private StringBuffer buffer = new StringBuffer();



    /* NOTE:
     * method process for text entry into the document:
     *
     *    1. replace(...)
     *    2. insertString(...)
     *
     * remove called once only on text/character removal
     */

    public SQLSyntaxDocument(TreeSet<String> keys, TokenMakerFactory tokenMakerFactory,String syntaxStyle) {
        super(tokenMakerFactory,syntaxStyle);

        rootElement = getDefaultRootElement();
        putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");

        braceTokens = new Vector<Token>();
        stringTokens = new ArrayList<Token>();

        this.textComponent = textComponent;
        dbobjects=new TreeSet();
        keywords = new TreeSet();

        //initMatchers();
        if (keys != null) {
            setSQLKeywords(keys);
        }

    }

    /**
     * Mulit-line comment tokens from the last scan
     */
    private List<Token> multiLineComments = new ArrayList<Token>();

    /*
     *  Override to apply syntax highlighting after
     *  the document has been updated
     */
    public void insertString(int offset, String text, AttributeSet attrs)
            throws BadLocationException {

        //Log.debug("insert");

       /* int length = text.length();

        // check overwrite mode
        if (insertMode == SqlMessages.OVERWRITE_MODE &&
                length == 1 && offset != getLength()) {
            remove(offset, 1);
        }

        if (length == 1) {

            char firstChar = text.charAt(0);

            /* check if we convert tabs to spaces
            if ((firstChar == Constants.TAB_CHAR) && tabsToSpaces) {

                text = QueryEditorSettings.getTabs();
                length = text.length();
            }

            /* auto-indent the next line
            else if (firstChar == Constants.NEW_LINE_CHAR) {

                int index = rootElement.getElementIndex(offset);
                Element line = rootElement.getElement(index);

                char SPACE = ' ';
                buffer.append(text);

                int start = line.getStartOffset();
                int end = line.getEndOffset();

                String _text = getText(start, end - start);
                char[] chars = _text.toCharArray();

                for (int i = 0; i < chars.length; i++) {

                    if ((Character.isWhitespace(chars[i]))
                            && (chars[i] != Constants.NEW_LINE_CHAR)) {
                        buffer.append(SPACE);
                    } else {
                        break;
                    }

                }
                text = buffer.toString();
                length = text.length();
            }

        }

        resetBracePosition();

        /* call super method and default to normal style */
        //super.insertString(offset, text, styles[WORD]);

        //processChangedLines();
        //updateBraces(offset + 1);
        //buffer.setLength(0);
        super.insertString(offset,text,attrs);
    }

    /*
     *  Override to apply syntax highlighting after
     *  the document has been updated
     */
    public void remove(int offset, int length) throws BadLocationException {
        super.remove(offset,length);
        //Log.debug("remove");



    }

           /*}

                }
            }*/


        // reassign the multi-line comments list
        //multiLineComments = tokens;




    /**
     * Shifts the text at start to end left one TAB character. The
     * specified region will be selected/reselected if specified.
     *
     * @param selectionStart - the start offset
     * @param selectionEnd   - the end offset
     */
    public void shiftTabEvent(int selectionStart, int selectionEnd) {
        shiftTabEvent(selectionStart, selectionEnd, true);
    }

    /**
     * Shifts the text at start to end left one TAB character. The
     * specified region will be selected/reselected if specified.
     *
     * @param selectionStart - the start offset
     * @param selectionEnd   - the end offset
     * @param reselect       - whether to select the region when done
     */
    public void shiftTabEvent(int selectionStart, int selectionEnd, boolean reselect) {

        if (textComponent == null) {
            return;
        }

        int minusOffset = tabsToSpaces ? QueryEditorSettings.getTabSize() : 1;

        int start = rootElement.getElementIndex(selectionStart);
        int end = rootElement.getElementIndex(selectionEnd - 1);

        for (int i = start; i <= end; i++) {
            Element line = rootElement.getElement(i);
            int startOffset = line.getStartOffset();
            int endOffset = line.getEndOffset();
            int removeCharCount = 0;

            if (startOffset == endOffset - 1) {
                continue;
            }

            try {

                char[] chars = getText(startOffset, minusOffset).toCharArray();

                for (int j = 0; j < chars.length; j++) {

                    if ((Character.isWhitespace(chars[j])) &&
                            (chars[j] != Constants.NEW_LINE_CHAR)) {
                        removeCharCount++;
                    } else if (j == 0) {
                        break;
                    }

                }
                super.remove(startOffset, removeCharCount);

            } catch (BadLocationException badLocExc) {
            }

        }

        if (reselect) {
            textComponent.setSelectionStart(
                    rootElement.getElement(start).getStartOffset());
            textComponent.setSelectionEnd(
                    rootElement.getElement(end).getEndOffset());
        }

    }

    private Style[] styles;




    /**
     * Change the style of a particular type of token.
     */


    /**
     * Change the style of a particular type of token, including adding bold or
     * italic using a third argument of <code>Font.BOLD</code> or
     * <code>Font.ITALIC</code> or the bitwise union
     * <code>Font.BOLD|Font.ITALIC</code>.
     */


    private void scanLines(int offset, int length,
                           String content, int documentLength, List<Token> tokens) {

        // The lines affected by the latest document update
        int startLine = rootElement.getElementIndex(offset);
        int endLine = rootElement.getElementIndex(offset + length);

        boolean applyStyle = true;
        int tokenCount = tokens.size();

        for (int i = startLine; i <= endLine; i++) {
            Element element = rootElement.getElement(i);
            int startOffset = element.getStartOffset();
            int endOffset = element.getEndOffset() - 1;

            if (endOffset < 0) {
                endOffset = 0;
            }

            applyStyle = true;
            for (int j = 0; j < tokenCount; j++) {
                Token token = tokens.get(j);
                if (token.contains(startOffset, endOffset)) {
                    applyStyle = false;
                    break;
                }
            }

            if (applyStyle) {
                String textSnippet = content.substring(startOffset, endOffset);
                /*applySyntaxColours(textSnippet,
                        startOffset,
                        endOffset,
                        documentLength);*/
            }
        }
    }

    public String getNameDBObjectFromPosition(int position, String text) {

        /*TokenMatcher tokenMatcher = matchers[TokenTypes.DBOBJECTS_MATCH];
        Matcher matcher = tokenMatcher.getMatcher();

        int start = 0;
        int end = 0;

        boolean applyStyle = true;
        matcher.reset(text);

        // the string token count for when we are not
        // processing string tokens
        int stringTokenCount = stringTokens.size();

        int length = text.length();
        int matcherStart = 0;
        while (matcher.find(matcherStart)) {
            start = matcher.start();
            end = matcher.end();

            if (position >= start && position <= end) {
                return text.substring(start, end);
            }

            // if this is a string mather add to the cache
            // compare against string cache to apply


            matcherStart = end + 1;
            if (matcherStart > length) {
                break;
            }

        }
        matcher.reset(Constants.EMPTY);*/
        return null;
    }

    public void setSQLKeywords(TreeSet<String> keywords) {
        this.keywords=keywords;
    }

    public void setDBObjects(TreeSet<String> dbobjects) {
        this.dbobjects = dbobjects;
    }

    public int getInsertMode() {
        return insertMode;
    }

    public void setInsertMode(int insertMode) {
        this.insertMode = insertMode;
    }

}












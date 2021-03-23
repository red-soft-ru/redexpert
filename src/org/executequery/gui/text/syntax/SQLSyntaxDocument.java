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
public class SQLSyntaxDocument extends DefaultStyledDocument
        implements TokenTypes {

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

    public SQLSyntaxDocument() {
        this(null, null);
    }

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

    public void resetAttributeSets() {
        initStyles(true);
        // update the stored tokens
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

    public void resetBracePosition() {
        if (!hasValidBraceTokens()) {
            return;
        }

        for (int i = 0, k = braceTokens.size(); i < k; i++) {
            Token token = braceTokens.get(i);
            applyBraceHiglight(false, token);
            token.reset();
        }
    }

    public void applyBraceHiglight(boolean apply, Token token) {
        Style style = apply ? styles[token.getStyle()] : styles[BRACKET];
        if (token.getStartIndex() != -1) {
            setCharacterAttributes(token.getStartIndex(), 1, style, !apply);
        }
        if (token.getEndIndex() != -1) {
            setCharacterAttributes(token.getEndIndex(), 1, style, !apply);
        }
    }

    private void applyErrorBrace(int offset, char brace) {
        if (!isOpenBrace(brace)) {
            Token token = getAvailableBraceToken();
            token.reset(BRACKET_HIGHLIGHT_ERR, offset, -1);
            applyBraceHiglight(true, token);
        }
    }

    public void updateBraces(int offset) {
        try {
            int length = getLength();
            if (length == 0) {
                return;
            }

            String text = getText(0, length);
            char charAtOffset = 0;
            char charBeforeOffset = 0;

            if (offset > 0) {
                charBeforeOffset = text.charAt(offset - 1);
            }

            if (offset < length) {
                charAtOffset = text.charAt(offset);
            }

            int matchOffset = -1;
            if (isBrace(charAtOffset)) {
                matchOffset = getMatchingBraceOffset(offset,
                        charAtOffset,
                        text);
                if (matchOffset == -1) {
                    applyErrorBrace(offset, charAtOffset);
                    return;
                }
            } else if (isBrace(charBeforeOffset)) {
                offset--;
                matchOffset = getMatchingBraceOffset(offset,
                        charBeforeOffset,
                        text);
                if (matchOffset == -1) {
                    applyErrorBrace(offset, charBeforeOffset);
                    return;
                }
            }

            if (matchOffset == -1) {
                return;
            }

            Token token = getAvailableBraceToken();
            token.reset(BRACKET_HIGHLIGHT, offset, matchOffset);
            applyBraceHiglight(true, token);

        } catch (BadLocationException e) {

            throw new Error(e);
        }

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

    public SQLSyntaxDocument(TreeSet<String> keys) {
        this(keys, null);
    }

    /* NOTE:
     * method process for text entry into the document:
     *
     *    1. replace(...)
     *    2. insertString(...)
     *
     * remove called once only on text/character removal
     */

    public SQLSyntaxDocument(TreeSet<String> keys, JTextComponent textComponent) {

        rootElement = getDefaultRootElement();
        putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
        initStyles(false);

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
        super.insertString(offset, text, styles[WORD]);

        processChangedLines();
        updateBraces(offset + 1);
        buffer.setLength(0);
    }

    /*
     *  Override to apply syntax highlighting after
     *  the document has been updated
     */
    public void remove(int offset, int length) throws BadLocationException {

        //Log.debug("remove");

        resetBracePosition();
        super.remove(offset, length);
        processChangedLines();

        if (offset > 0) {

            updateBraces(offset);
        }

    }

    private void processChangedLines()
            throws BadLocationException {

        int documentLength = getLength();
        if (documentLength == 0) {
            return;
        }

        String content = getText(0, documentLength);
        CommonTokenStream cts = new CommonTokenStream(new SqlLexer(CharStreams.fromString(content)));
        try {
            cts.fill();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        List<org.antlr.v4.runtime.Token> toks = cts.getTokens();
        for(org.antlr.v4.runtime.Token token:toks) {
            Style style = styles[WORD];
            switch (token.getType())
            {
                case SqlLexer.KEYWORD:style = styles[KEYWORD];
                break;
                case SqlLexer.DATATYPE_SQL:style = styles[NUMBER];
                break;
                case SqlLexer.STRING_LITERAL:style = styles[STRING];
                    break;
                case SqlLexer.IDENTIFIER:
                    String tokenText = token.getText();
                    if(!tokenText.startsWith("\""))
                        tokenText = tokenText.toUpperCase();
                    if (dbobjects.contains(tokenText))
                    style = styles[DBOBJECT];
                    break;
                case SqlLexer.MULTILINE_COMMENT:
                    case SqlLexer.SINGLE_LINE_COMMENT:
                        style = styles[COMMENT];
                        break;
                default:break;
            }
            if(style!=styles[WORD])
            setCharacterAttributes(token.getStartIndex(),
                    token.getStopIndex()-token.getStartIndex()+1,
                    style,
                    false);
        }
                    /*}

                }
            }*/


        // reassign the multi-line comments list
        //multiLineComments = tokens;
    }

    public void replace(int offset, int length,
                        String text, AttributeSet attrs)
            throws BadLocationException {

        if (text == null) {
            return;
        }

//        Log.debug("replace");

        int textLength = text.length();
        if ((length == 0) && (textLength == 0)) {
            return;
        }

        // if text is selected - ie. length > 0
        // and it is a TAB and we have a text component 
        if ((length > 0) && (textLength > 0) &&
//                (text.charAt(0) == Constants.TAB_CHAR) && 
                ("\t".equals(text)) &&
                (textComponent != null)) {

            int selectionStart = textComponent.getSelectionStart();
            int selectionEnd = textComponent.getSelectionEnd();

            int start = rootElement.getElementIndex(selectionStart);
            int end = rootElement.getElementIndex(selectionEnd - 1);

            for (int i = start; i <= end; i++) {
                Element line = rootElement.getElement(i);
                int startOffset = line.getStartOffset();

                try {
                    insertString(startOffset, text, attrs);
                } catch (BadLocationException badLocExc) {
                    badLocExc.printStackTrace();
                }

            }

            textComponent.setSelectionStart(
                    rootElement.getElement(start).getStartOffset());
            textComponent.setSelectionEnd(
                    rootElement.getElement(end).getEndOffset());
            return;

        }

        if (attrs == null) {
            attrs = styles[WORD];
        }

        super.replace(offset, length, text, attrs);

    }

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

    private void initStyles(boolean reset) {
        styles = new Style[typeNames.length];

        Font font = QueryEditorSettings.getEditorFont();
        int fontSize = font.getSize();
        String fontFamily = font.getName();

        SyntaxStyle[] syntaxStyles = QueryEditorSettings.getSyntaxStyles();

        for (int i = 0; i < syntaxStyles.length; i++) {
            changeStyle(syntaxStyles[i].getType(),
                    syntaxStyles[i].getForeground(),
                    syntaxStyles[i].getFontStyle(),
                    syntaxStyles[i].getBackground(),
                    fontFamily,
                    fontSize);
        }

    }

    public void changeStyle(int type, Color fcolor,
                            int fontStyle, Color bcolor,
                            String fontFamily, int fontSize) {

        Style style = addStyle(typeNames[type], null);

        if (fcolor != null) {
            StyleConstants.setForeground(style, fcolor);
        }

        if (bcolor != null) {
            StyleConstants.setBackground(style, bcolor);
        }

        StyleConstants.setFontSize(style, fontSize);
        StyleConstants.setFontFamily(style, fontFamily);

        switch (fontStyle) {
            case 0:
                StyleConstants.setItalic(style, false);
                StyleConstants.setBold(style, false);
                break;
            case 1:
                StyleConstants.setBold(style, true);
                break;
            case 2:
                StyleConstants.setItalic(style, true);
                break;
            default:
                StyleConstants.setItalic(style, false);
                StyleConstants.setBold(style, false);
        }

        styles[type] = style;

    }

    /**
     * Change the style of a particular type of token.
     */
    public void changeStyle(int type, Color color) {
        Style style = addStyle(typeNames[type], null);
        if (color != null) {
            StyleConstants.setForeground(style, color);
        }
        styles[type] = style;
    }

    /**
     * Change the style of a particular type of token, including adding bold or
     * italic using a third argument of <code>Font.BOLD</code> or
     * <code>Font.ITALIC</code> or the bitwise union
     * <code>Font.BOLD|Font.ITALIC</code>.
     */
    public void changeStyle(int type, Color color,
                            int fontStyle, Color bcolor) {

        Style style = addStyle(typeNames[type], null);
        StyleConstants.setForeground(style, color);

        if (bcolor != null) {
            StyleConstants.setBackground(style, bcolor);
        }

        switch (fontStyle) {
            case 0:
                StyleConstants.setItalic(style, false);
                StyleConstants.setBold(style, false);
                break;
            case 1:
                StyleConstants.setBold(style, true);
                break;
            case 2:
                StyleConstants.setItalic(style, true);
                break;
            default:
                StyleConstants.setItalic(style, false);
                StyleConstants.setBold(style, false);
        }

        styles[type] = style;

    }

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












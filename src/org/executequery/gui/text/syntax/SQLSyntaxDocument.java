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

import org.executequery.Constants;
import org.executequery.gui.editor.QueryEditorSettings;
import org.executequery.sql.SqlMessages;

import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public SQLSyntaxDocument(List<String> keys) {
        this(keys, null);
    }

    public SQLSyntaxDocument(List<String> keys, JTextComponent textComponent) {

        rootElement = getDefaultRootElement();
        putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
        initStyles(false);

        braceTokens = new Vector<Token>();
        stringTokens = new ArrayList<Token>();

        this.textComponent = textComponent;

        initMatchers();
        if (keys != null) {
            setSQLKeywords(keys);
        }

    }

    protected void initMatchers() {
        matchers = new TokenMatcher[MATCHERS.length];

        matchers[NUMBER_MATCH] =
                new TokenMatcher(NUMBER,
                        styles[NUMBER],
                        Pattern.compile(NUMBER_REGEX).
                                matcher(Constants.EMPTY));

        matchers[BRACES_MATCH] =
                new TokenMatcher(BRACKET,
                        styles[BRACKET],
                        Pattern.compile(BRACES_REGEX).
                                matcher(Constants.EMPTY));

        matchers[OPERATOR_MATCH] =
                new TokenMatcher(OPERATOR,
                        styles[OPERATOR],
                        Pattern.compile(OPERATOR_REGEX).
                                matcher(Constants.EMPTY));

        matchers[STRING_MATCH] =
                new TokenMatcher(STRING,
                        styles[STRING],
                        Pattern.compile(QUOTE_REGEX).
                                matcher(Constants.EMPTY));

        matchers[SINGLE_LINE_COMMENT_MATCH] =
                new TokenMatcher(SINGLE_LINE_COMMENT,
                        styles[SINGLE_LINE_COMMENT],
                        Pattern.compile(SINGLE_LINE_COMMENT_REGEX).
                                matcher(Constants.EMPTY));

        char PIPE = '|';
        StringBuilder sb = new StringBuilder("\\b(");
        String[] literals = {Constants.TRUE_LITERAL,
                Constants.FALSE_LITERAL,
                Constants.NULL_LITERAL};

        for (int i = 0, n = literals.length - 1; i < literals.length; i++) {
            sb.append(literals[i]);
            if (i < n) {
                sb.append(PIPE);
            }
        }

        sb.append(")\\b");
        matchers[LITERALS_MATCH] =
                new TokenMatcher(LITERAL,
                        styles[LITERAL],
                        Pattern.compile(
                                sb.toString(), Pattern.CASE_INSENSITIVE).
                                matcher(Constants.EMPTY));

    }

    public void setTextComponent(JTextComponent textComponent) {
        this.textComponent = textComponent;
    }

    public void resetAttributeSets() {
        initStyles(true);
        // update the stored tokens
        for (int i = 0; i < matchers.length; i++) {
            TokenMatcher matcher = matchers[i];
            if (matcher != null) {
                int type = matcher.getType();
                matcher.setStyle(styles[type]);
            }
        }
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

    /*
     *  Override to apply syntax highlighting after
     *  the document has been updated
     */
    public void insertString(int offset, String text, AttributeSet attrs)
            throws BadLocationException {

        //Log.debug("insert");

        int length = text.length();

        // check overwrite mode
        if (insertMode == SqlMessages.OVERWRITE_MODE &&
                length == 1 && offset != getLength()) {
            remove(offset, 1);
        }

        if (length == 1) {

            char firstChar = text.charAt(0);

            /* check if we convert tabs to spaces */
            if ((firstChar == Constants.TAB_CHAR) && tabsToSpaces) {

                text = QueryEditorSettings.getTabs();
                length = text.length();
            }

            /* auto-indent the next line */
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

        processChangedLines(offset, length);
        updateBraces(offset + 1);
        buffer.setLength(0);
    }

    /* NOTE:
     * method process for text entry into the document:
     *
     *    1. replace(...)
     *    2. insertString(...)
     *
     * remove called once only on text/character removal
     */

    /*
     *  Override to apply syntax highlighting after
     *  the document has been updated
     */
    public void remove(int offset, int length) throws BadLocationException {

        //Log.debug("remove");

        resetBracePosition();
        super.remove(offset, length);
        processChangedLines(offset, 0);

        if (offset > 0) {

            updateBraces(offset);
        }

    }

    /**
     * Mulit-line comment tokens from the last scan
     */
    private List<Token> multiLineComments = new ArrayList<Token>();

    private void processChangedLines(int offset, int length)
            throws BadLocationException {

        int documentLength = getLength();
        if (documentLength == 0) {
            return;
        }

        int tokenStart = -1;
        int tokenEnd = 0;
        int endOffset = offset + length;
        String content = getText(0, documentLength);

        // scan for multi-line comments
        List<Token> tokens = new ArrayList<Token>();
        while ((tokenStart = content.indexOf(OPEN_COMMENT, tokenEnd)) != -1) {

            tokenEnd = content.indexOf(CLOSE_COMMENT, tokenStart);

            if (tokenEnd != -1) {

                tokenEnd += 2;
            }

            tokens.add(new Token(tokenStart, tokenEnd));

            if (tokenEnd == -1) {

                break;
            }

        }

        // scan the lines for highlighting
        scanLines(offset, length, content, documentLength, tokens);

        // scan multi comment tokens for apply/reapply
        boolean applyStyle = true;
        int tokenCount = tokens.size();
        int lastTokenCount = multiLineComments.size();

        // check for multi-line comments that do not exist anymore
        for (int j = 0; j < lastTokenCount; j++) {
            Token lastToken = multiLineComments.get(j);
            tokenStart = lastToken.getStartIndex();
            tokenEnd = lastToken.getEndIndex();

            applyStyle = true;

            for (int i = 0; i < tokenCount; i++) {
                Token token = tokens.get(i);
                if (token.equals(lastToken)) {
                    applyStyle = false;
                    break;
                }
            }

            // reapply the styles to the portion from the last
            // scan that no longer exists
            if (applyStyle) {

                // if end was -1 set to the end of the document
                if (tokenEnd == -1) {
                    tokenEnd = documentLength - 1;
                }

                scanLines(tokenStart, tokenEnd - tokenStart,
                        content, documentLength, tokens);
            }
        }

        // apply multi-line comment style where it did not exist before
        for (int i = 0; i < tokenCount; i++) {
            Token token = tokens.get(i);
            tokenStart = token.getStartIndex();
            tokenEnd = token.getEndIndex();

            // if we have a dangling open comment
            // apply to the rest of the document
            if (token.getEndIndex() == -1) {
                setCharacterAttributes(tokenStart,
                        content.length() - tokenStart,
                        styles[COMMENT],
                        false);
                break;
            }

            applyStyle = true;

            // check the last multiline comments
            for (int j = 0; j < lastTokenCount; j++) {
                Token lastToken = multiLineComments.get(j);

                // check if the current token existed in the last scan
                if (lastToken.equals(token)) {
                    // style not applied if it did
                    applyStyle = false;
                    break;
                }
                // where previously there was no close comment tag
                // reapply to the rest of the text from the current end
                else if (lastToken.getEndIndex() == -1) {
                    applyStyle = false;
                    // rescan that section that was previously still open
                    scanLines(tokenEnd, documentLength - tokenEnd,
                            content, documentLength, tokens);
                    break;
                }

            }

            // check for the tokens intersecting the current offset
            // and reapply to cover the comment correctly regardless 
            // of the current state of the apply flag
            if (token.intersects(offset, endOffset)) {
                setCharacterAttributes(tokenStart,
                        token.getLength(),
                        styles[COMMENT],
                        false);
            } else {
                if (applyStyle) {

                    // if we have a close tag
                    if (tokenEnd > 0) {
                        setCharacterAttributes(tokenStart,
                                token.getLength(),
                                styles[COMMENT],
                                false);
                    }
                    // otherwise set style to the 
                    // remainder of the document
                    else {
                        setCharacterAttributes(tokenStart,
                                content.length() - tokenStart,
                                styles[COMMENT],
                                false);
                    }

                }
            }

        }

        // reassign the multi-line comments list
        multiLineComments = tokens;
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
                applySyntaxColours(textSnippet,
                        startOffset,
                        endOffset,
                        documentLength);
            }
        }
    }

    private void applySyntaxColours(String text,
                                    int startOffset,
                                    int endOffset,
                                    int contentLength) {

        int lineLength = endOffset - startOffset;
//        if (endOffset >= contentLength) {
//            endOffset = contentLength - 1;
//        }

        // set the plain style
        setCharacterAttributes(startOffset, lineLength, styles[WORD], false);

        for (int i = 0; i < matchers.length; i++) {
            if (matchers[i] != null) { // check case keywords not initialised
                applyHighlights(i,
                        text,
                        startOffset,
                        matchers[i].getMatcher(),
                        matchers[i].getStyle(),
                        (i == SINGLE_LINE_COMMENT_MATCH));
            }
        }

    }

    private void applyHighlights(int matcherType,
                                 String text,
                                 int startOffset,
                                 Matcher matcher,
                                 Style style,
                                 boolean replace) {

        if (matcherType == STRING_MATCH) {
            stringTokens.clear();
        }

        int start = 0;
        int end = 0;
        int realStart = 0;
        int realEnd = 0;

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

            realStart = start + startOffset;
            realEnd = end + startOffset;

            applyStyle = true;

            // if this is a string mather add to the cache
            if (matcherType == STRING_MATCH) {
                stringTokens.add(new Token(realStart, realEnd));
            }
            // compare against string cache to apply
            else if (matcherType == SINGLE_LINE_COMMENT_MATCH) {
                if (stringTokenCount > 0) {
                    /*
                    Log.debug("text: " +text);
                    Log.debug("length: " +text.length());
                    Log.debug("string tokens: "+stringTokenCount);
                    Log.debug("start: " +startOffset);
                    Log.debug("realStart: " + realStart + 
                            " realEnd: "+realEnd);
                    */

                    // check we are not within a string literal
                    for (int i = 0; i < stringTokenCount; i++) {
                        Token token = stringTokens.get(i);
                        int tokenStart = token.getStartIndex();
                        int tokenEnd = token.getEndIndex();

                        if (realStart > tokenEnd) {
                            continue;
                        }

                        //Log.debug(token);

                        if (realStart < tokenStart) {
                            applyStyle = true;
                            break;
                        }

                        if (realStart > tokenStart && realStart < tokenEnd) {
                            // set the end to the end of the string
                            // token for the matcher reset
                            end = token.getEndIndex() - startOffset;
                            applyStyle = false;
                            break;
                        }

                    }
                }
            }

            if (applyStyle) {
                setCharacterAttributes(realStart,
                        end - start,
                        style,
                        replace);
            }

            matcherStart = end + 1;
            if (matcherStart > length) {
                break;
            }

        }
        matcher.reset(Constants.EMPTY);
    }

    public String getNameDBObjectFromPosition(int position, String text) {

        TokenMatcher tokenMatcher = matchers[TokenTypes.DBOBJECTS_MATCH];
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
        matcher.reset(Constants.EMPTY);
        return null;
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

    /**
     * Sets the SQL keywords to be applied to this document.
     *
     * @param keywords - the keywords list
     * @param reset
     */
    public void setSQLKeywords(List<String> keywords) {
        StringBuffer sb = new StringBuffer("\\b(?:");
        // the last start char
        char lastFirstChar = 0;

        // we are trying to achieve the following regex
        // where the first char of each group is the same char 
        // as in: t(?:his|hat) as opposed to (?:this|that)

        for (int i = 0, k = keywords.size(); i < k; i++) {
            String _keyword = keywords.get(i).trim();

            char firstChar = _keyword.charAt(0);
            if (firstChar == lastFirstChar) {
                sb.append("|");
                sb.append(_keyword.substring(1));
            } else {
                if (i > 0) {
                    sb.append(")|");
                }
                sb.append(firstChar);
                sb.append("(?:");
                sb.append(_keyword.substring(1));
                lastFirstChar = firstChar;
            }

        }
        sb.append("))\\b");

        Matcher matcher = Pattern.compile(
                sb.toString(),
                Pattern.CASE_INSENSITIVE).matcher(Constants.EMPTY);
        matchers[KEYWORD_MATCH] = new TokenMatcher(KEYWORD, styles[KEYWORD], matcher);
    }

    public void setDBObjects(List<String> dbobjects) {
        StringBuffer sb = new StringBuffer("\\b(?:");
        // the last start char
        char lastFirstChar = 0;

        // we are trying to achieve the following regex
        // where the first char of each group is the same char
        // as in: t(?:his|hat) as opposed to (?:this|that)

        for (int i = 0, k = dbobjects.size(); i < k; i++) {
            String _dbobject = dbobjects.get(i).trim();

            char firstChar = _dbobject.charAt(0);
            if (firstChar == lastFirstChar) {
                sb.append("|");
                sb.append(_dbobject.substring(1));
            } else {
                if (i > 0) {
                    sb.append(")|");
                }
                sb.append(firstChar);
                sb.append("(?:");
                sb.append(_dbobject.substring(1));
                lastFirstChar = firstChar;
            }

        }
        sb.append("))\\b");
        if (dbobjects.size() == 0) {
            sb = new StringBuffer();
        }

        Matcher matcher = Pattern.compile(
                sb.toString(),
                Pattern.CASE_INSENSITIVE).matcher(Constants.EMPTY);
        matchers[DBOBJECTS_MATCH] = new TokenMatcher(DBOBJECT, styles[DBOBJECT], matcher);
    }

    public int getInsertMode() {
        return insertMode;
    }

    public void setInsertMode(int insertMode) {
        this.insertMode = insertMode;
    }

}












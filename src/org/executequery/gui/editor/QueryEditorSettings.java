/*
 * QueryEditorSettings.java
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

package org.executequery.gui.editor;

import java.awt.Color;
import java.awt.Font;

import org.underworldlabs.util.SystemProperties;
import org.executequery.gui.text.syntax.TokenTypes;
import org.executequery.gui.text.syntax.SyntaxStyle;

/**
 * Query editor settings.
 *
 * @author   Takis Diakoumis
 */
public class QueryEditorSettings {

    /** Whether to display the line higlight */
    private static boolean displayLineHighlight;
    /** Whether to display the right margin */
    private static boolean displayRightMargin;
    /** The right margin size */
    private static int rightMarginSize;
    /** The right margin colour */
    private static Color rightMarginColour;
    /** Selection colour */
    private static Color selectionColour;
    /** Selected text colour */
    private static Color selectedTextColour;
    /** The line highlight colour */
    private static Color lineHighlightColour;
    /** Editor background */
    private static Color editorBackground;
    /** the caret colour */
    private static Color caretColour;
    /** The currently installed font - in plain - no styles */
    private static Font editorFont;
    /** The characters for a TAB */
    private static int tabSize;
    /** converting tabs to spaces */
    private static boolean tabsToSpaces;
    /** tab text when converting to spaces */
    private static String tabs;
    /** maximum values held in history */
    private static int historyMax;
    /** the syntax styles */
    private static SyntaxStyle[] syntaxStyles;
    
    private QueryEditorSettings() {}

    static {
        initialise();
    }
    
    public static void initialise() {
        selectionColour = SystemProperties.getColourProperty(
"user",                                          "editor.text.selection.background");
        selectedTextColour = SystemProperties.getColourProperty(
"user",                                          "editor.text.selection.foreground");
        
        editorBackground = SystemProperties.getColourProperty(
"user",                                          "editor.text.background.colour");
        
        lineHighlightColour = SystemProperties.getColourProperty(
"user",                                          "editor.display.linehighlight.colour");
        
        displayRightMargin = SystemProperties.getBooleanProperty("user", "editor.display.margin");
        rightMarginSize = SystemProperties.getIntProperty("user", "editor.margin.size");
        rightMarginColour = SystemProperties.getColourProperty("user", "editor.margin.colour");
        
        displayLineHighlight = SystemProperties.getBooleanProperty(
"user",                                          "editor.display.linehighlight");
        
        int fontSize = SystemProperties.getIntProperty("user", "sqlsyntax.font.size");
        String fontName = SystemProperties.getProperty("user", "sqlsyntax.font.name");
        editorFont = new Font(fontName, Font.PLAIN, fontSize);

        tabsToSpaces = SystemProperties.getBooleanProperty("user", "editor.tabs.tospaces");
        tabSize = SystemProperties.getIntProperty("user", "editor.tab.spaces");

        if (tabsToSpaces) {
            char space = ' ';
            StringBuffer sb = new StringBuffer(tabSize);
            for (int i = 0; i < tabSize; i++) {
                sb.append(space);
            }
            tabs = sb.toString();
        }

        caretColour = SystemProperties.getColourProperty("user", "editor.caret.colour");
        
        historyMax = SystemProperties.getIntProperty("user", "editor.history.count");
        
        initialiseStyles();
    }

    private static void initialiseStyles() {
        syntaxStyles = new SyntaxStyle[TokenTypes.typeNames.length];

        createStyle(TokenTypes.UNRECOGNIZED, Color.red, Font.PLAIN, null);
        createStyle(TokenTypes.KEYWORD2, Color.blue, Font.PLAIN, null);

        // -----------------------------
        // user defined styles
        int fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.multicomment");
        Color color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.multicomment"); 
        createStyle(TokenTypes.COMMENT, color, fontStyle, null);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.normal");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.normal");
        createStyle(TokenTypes.WORD, color, fontStyle, null);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.singlecomment");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.singlecomment");
        createStyle(TokenTypes.SINGLE_LINE_COMMENT, color, fontStyle, null);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.keyword");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.keyword");
        createStyle(TokenTypes.KEYWORD, color, fontStyle, null);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.quote");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.quote");
        createStyle(TokenTypes.STRING, color, fontStyle, null);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.number");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.number");
        createStyle(TokenTypes.NUMBER, color, fontStyle, null);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.literal");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.literal");
        createStyle(TokenTypes.LITERAL, color, fontStyle, null);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.operator");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.operator");
        createStyle(TokenTypes.OPERATOR, color, fontStyle, null);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.braces");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.braces");
        createStyle(TokenTypes.BRACKET, color, fontStyle, null);

        /* bracket highlights */
        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.braces.match1");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.braces.match1");
        createStyle(TokenTypes.BRACKET_HIGHLIGHT, Color.BLACK, fontStyle, color);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.braces.error");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.braces.error");
        createStyle(TokenTypes.BRACKET_HIGHLIGHT_ERR, Color.BLACK, fontStyle, color);
    }
    
    private static void createStyle(int type, Color fcolor, 
                                    int fontStyle, Color bcolor) {
        syntaxStyles[type] = new SyntaxStyle(type, fontStyle, fcolor, bcolor);
    }
    
    public static SyntaxStyle[] getSyntaxStyles() {
        return syntaxStyles;
    }
    
    public static boolean isDisplayLineHighlight() {
        return displayLineHighlight;
    }

    public static void setDisplayLineHighlight(boolean aDisplayLineHighlight) {
        displayLineHighlight = aDisplayLineHighlight;
    }

    public static boolean isDisplayRightMargin() {
        return displayRightMargin;
    }

    public static void setDisplayRightMargin(boolean aDisplayRightMargin) {
        displayRightMargin = aDisplayRightMargin;
    }

    public static int getRightMarginSize() {
        return rightMarginSize;
    }

    public static void setRightMarginSize(int aRightMarginSize) {
        rightMarginSize = aRightMarginSize;
    }

    public static int getHistoryMax() {
        return historyMax;
    }

    public static void setHistoryMax(int aHistoryMax) {
        historyMax = aHistoryMax;
    }

    public static Color getRightMarginColour() {
        return rightMarginColour;
    }

    public static void setRightMarginColour(Color aRightMarginColour) {
        rightMarginColour = aRightMarginColour;
    }

    public static Color getSelectionColour() {
        return selectionColour;
    }

    public static void setSelectionColour(Color aSelectionColour) {
        selectionColour = aSelectionColour;
    }

    public static Color getSelectedTextColour() {
        return selectedTextColour;
    }

    public static void setSelectedTextColour(Color aSelectedTextColour) {
        selectedTextColour = aSelectedTextColour;
    }

    public static Color getLineHighlightColour() {
        return lineHighlightColour;
    }

    public static void setLineHighlightColour(Color aLineHighlightColour) {
        lineHighlightColour = aLineHighlightColour;
    }

    public static Color getEditorBackground() {
        return editorBackground;
    }

    public static void setEditorBackground(Color aEditorBackground) {
        editorBackground = aEditorBackground;
    }

    public static Color getCaretColour() {
        return caretColour;
    }

    public static void setCaretColour(Color aCaretColour) {
        caretColour = aCaretColour;
    }

    public static Font getEditorFont() {
        return editorFont;
    }

    public static void setEditorFont(Font aEditorFont) {
        editorFont = aEditorFont;
    }

    public static int getTabSize() {
        return tabSize;
    }

    public static void setTabSize(int aTabSize) {
        tabSize = aTabSize;
    }

    public static boolean isTabsToSpaces() {
        return tabsToSpaces;
    }

    public static void setTabsToSpaces(boolean aTabsToSpaces) {
        tabsToSpaces = aTabsToSpaces;
    }

    public static String getTabs() {
        return tabs;
    }

    public static void setTabs(String aTabs) {
        tabs = aTabs;
    }

}
















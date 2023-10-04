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

import org.executequery.gui.text.syntax.SyntaxStyle;
import org.executequery.gui.text.syntax.TokenTypes;
import org.underworldlabs.util.SystemProperties;

import java.awt.*;
import java.util.Collections;

/**
 * Query editor settings.
 *
 * @author Takis Diakoumis
 */
public class QueryEditorSettings {

    /**
     * Whether to display the line highlight
     */
    private static boolean displayLineHighlight;

    /**
     * Selection colour
     */
    private static Color selectionColour;

    /**
     * The line highlight colour
     */
    private static Color lineHighlightColour;

    /**
     * Editor background
     */
    private static Color editorBackground;

    /**
     * The caret colour
     */
    private static Color caretColour;

    /**
     * The currently installed font - in plain - no styles
     */
    private static Font editorFont;

    /**
     * The characters for a TAB
     */
    private static int tabSize;

    /**
     * Converting tabs to spaces
     */
    private static boolean tabsToSpaces;

    /**
     * Tab text when converting to spaces
     */
    private static String tabs;

    /**
     * Maximum values held in history
     */
    private static int historyMax;

    /**
     * The syntax styles
     */
    private static SyntaxStyle[] syntaxStyles;

    static {
        initialise();
    }

    public static void initialise() {

        selectionColour = SystemProperties.getColourProperty(
                "user", "editor.text.selection.background");
        editorBackground = SystemProperties.getColourProperty(
                "user", "editor.text.background.colour");
        lineHighlightColour = SystemProperties.getColourProperty(
                "user", "editor.display.linehighlight.colour");
        displayLineHighlight = SystemProperties.getBooleanProperty(
                "user", "editor.display.linehighlight");
        tabsToSpaces = SystemProperties.getBooleanProperty(
                "user", "editor.tabs.tospaces");
        tabSize = SystemProperties.getIntProperty(
                "user", "editor.tab.spaces");
        caretColour = SystemProperties.getColourProperty(
                "user", "editor.caret.colour");
        historyMax = SystemProperties.getIntProperty(
                "user", "editor.history.count");

        int fontSize = SystemProperties.getIntProperty("user", "sqlsyntax.font.size");
        String fontName = SystemProperties.getProperty("user", "sqlsyntax.font.name");
        editorFont = new Font(fontName, Font.PLAIN, fontSize);

        if (tabsToSpaces)
            tabs = String.join("", Collections.nCopies(tabSize, " "));

        initialiseStyles();
    }

    private static void initialiseStyles() {

        syntaxStyles = new SyntaxStyle[TokenTypes.typeNames.length];

        createStyle(TokenTypes.UNRECOGNIZED, Color.red, Font.PLAIN);
        createStyle(TokenTypes.KEYWORD2, Color.blue, Font.PLAIN);

        int fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.multicomment");
        Color color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.multicomment");
        createStyle(TokenTypes.COMMENT, color, fontStyle);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.normal");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.normal");
        createStyle(TokenTypes.WORD, color, fontStyle);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.singlecomment");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.singlecomment");
        createStyle(TokenTypes.SINGLE_LINE_COMMENT, color, fontStyle);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.keyword");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.keyword");
        createStyle(TokenTypes.KEYWORD, color, fontStyle);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.quote");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.quote");
        createStyle(TokenTypes.STRING, color, fontStyle);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.number");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.number");
        createStyle(TokenTypes.NUMBER, color, fontStyle);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.literal");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.literal");
        createStyle(TokenTypes.LITERAL, color, fontStyle);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.operator");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.operator");
        createStyle(TokenTypes.OPERATOR, color, fontStyle);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.dbobjects");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.dbobjects");
        createStyle(TokenTypes.DBOBJECT, color, fontStyle);

        color = SystemProperties.getColourProperty("user", "sqlsyntax.colour.datatype");
        fontStyle = SystemProperties.getIntProperty("user", "sqlsyntax.style.datatype");
        createStyle(TokenTypes.DATATYPE, color, fontStyle);

    }

    private static void createStyle(int type, Color fcolor, int fontStyle) {
        syntaxStyles[type] = new SyntaxStyle(type, fontStyle, fcolor, null);
    }

    public static boolean isDisplayLineHighlight() {
        return displayLineHighlight;
    }

    public static int getHistoryMax() {
        return historyMax;
    }

    public static Color getSelectionColour() {
        return selectionColour;
    }

    public static Color getLineHighlightColour() {
        return lineHighlightColour;
    }

    public static Color getEditorBackground() {
        return editorBackground;
    }

    public static Color getCaretColour() {
        return caretColour;
    }

    public static Font getEditorFont() {
        return editorFont;
    }

    public static int getTabSize() {
        return tabSize;
    }

    public static boolean isTabsToSpaces() {
        return tabsToSpaces;
    }

    public static String getTabs() {
        return tabs;
    }

    public static void setTabs(String aTabs) {
        tabs = aTabs;
    }

}

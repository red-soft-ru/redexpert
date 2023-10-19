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

import org.underworldlabs.util.SystemProperties;

import java.awt.*;

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

        int fontSize = SystemProperties.getIntProperty("user", "sqlsyntax.font.size");
        String fontName = SystemProperties.getProperty("user", "sqlsyntax.font.name");
        editorFont = new Font(fontName, Font.PLAIN, fontSize);

    }

    public static boolean isDisplayLineHighlight() {
        return displayLineHighlight;
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

}

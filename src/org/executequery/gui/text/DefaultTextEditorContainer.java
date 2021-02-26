/*
 * DefaultTextEditorContainer.java
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

package org.executequery.gui.text;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.print.TextPrinter;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.print.Printable;

/**
 * Default TextEditor combined with TextEditorContainer implementation.
 *
 * @author Takis Diakoumis
 */
public class DefaultTextEditorContainer extends JPanel
        implements TextEditor,
        TextEditorContainer {

    /**
     * the text component contained within this panel
     */
    protected JTextComponent textComponent;

    /**
     * Creates a new instance of DefaultTextEditorContainer.
     */
    public DefaultTextEditorContainer() {
    }

    /**
     * Creates a new instance of DefaultTextEditorContainer with
     * the specified layout manager for this panel.
     */
    public DefaultTextEditorContainer(LayoutManager layout) {
        super(layout);
    }

    /**
     * Returns the TextEditor component that this container holds.
     */
    public TextEditor getTextEditor() {
        return this;
    }

    /**
     * Returns the default focus component for this panel.
     *
     * @return the text component
     */
    public Component getDefaultFocusComponent() {
        return textComponent;
    }

    /**
     * Pastes text from the system clipboard into the text component.
     */
    public void paste() {
        textComponent.paste();
    }

    /**
     * Copies text to the system clipboard from the text component.
     */
    public void copy() {
        textComponent.copy();
    }

    /**
     * Cuts selected text from the text component.
     */
    public void cut() {
        textComponent.cut();
    }

    /**
     * Kicks off a save or save as process for the text components
     * contents as specified.
     *
     * @param saveAs - whether this is a save as call. When this is false
     *               the contents are saved to some already known file name
     *               and path. Otherwise the standard save dialog is shown.
     */
    public int save(boolean saveAs) {
        return TextUtilities.save(textComponent);
    }

    /**
     * Returns the actual text component encapsulated by this panel.
     */
    public JTextComponent getEditorTextComponent() {
        return textComponent;
    }

    /**
     * Sets the text component text to that specified.
     *
     * @param s - the text to be set in the editor text component
     */
    public void setEditorText(String s) {
        try {
            textComponent.setText(s);
        } catch (OutOfMemoryError e) {
            System.gc();
            GUIUtilities.displayErrorMessage("Out of Memory.\nThe file is " +
                    "too large to\nopen for viewing.");
            textComponent.setText(Constants.EMPTY);
            return;
        }

    }

    public String getEditorText() {
        return textComponent.getText();
    }

    public void changeSelectionToUnderscore() {
        TextUtilities.changeSelectionToUnderscore(textComponent);
    }

    public void changeSelectionToCamelCase() {
        TextUtilities.changeSelectionToCamelCase(textComponent);
    }

    public void changeSelectionCase(boolean upper) {
        TextUtilities.changeSelectionCase(textComponent, upper);
    }

    public void deleteLine() {
        TextUtilities.deleteLine(textComponent);
    }

    public void deleteWord() {
        TextUtilities.deleteWord(textComponent);
    }

    public void deleteSelection() {
        TextUtilities.deleteSelection(textComponent);
    }

    public void insertFromFile() {
        TextUtilities.insertFromFile(textComponent);
    }

    public void selectAll() {
        TextUtilities.selectAll(textComponent);
    }

    public void selectNone() {
        TextUtilities.selectNone(textComponent);
    }

    public void insertLineAfter() {
        TextUtilities.insertLineAfter(textComponent);
    }

    public void insertLineBefore() {
        TextUtilities.insertLineBefore(textComponent);
    }

    public boolean contentCanBeSaved() {
        return false;
    }

    public void disableUpdates(boolean disable) {
    }

    public boolean canSearch() {
        return true;
    }

    public boolean canPrint() {
        return true;
    }

    public Printable getPrintable() {
        return new TextPrinter(textComponent.getText());
    }

    public String getPrintJobName() {
        return "RedXpert";
    }

    public String getDisplayName() {
        return Constants.EMPTY;
    }

}







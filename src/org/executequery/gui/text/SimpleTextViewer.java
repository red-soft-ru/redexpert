/*
 * SimpleTextViewer.java
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

import org.executequery.print.TextPrinter;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.print.Printable;

/**
 * <p>A simple text file viewer. Implementing
 * <code>TextEditor</code>, it provides indirect
 * support for all file/print tasks on the
 * displayed text. It provides no special formatting
 * support however and uses a <code>JTextArea</code>
 * as the primary text component.
 *
 * @author Takis Diakoumis
 */
public class SimpleTextViewer extends JPanel implements TextEditor {

    /**
     * The text area displaying the text
     */
    private JTextArea textArea;

    /**
     * Creates a new instance with no text
     */
    public SimpleTextViewer() {
        super(new GridBagLayout());

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * <p>Creates a new instance with the text to display
     * passed as an argument to this constructor.
     *
     * @param the text to be displayed within the text area.
     */
    public SimpleTextViewer(String text) {
        this();
        textArea.setText(text);
        textArea.setCaretPosition(0);
    }

    /**
     * Initializes the state of this instance.
     */
    private void jbInit() throws Exception {
        textArea = new JTextArea();
        JScrollPane scroller = new JScrollPane(textArea);
        textArea.setFont(new Font("monospaced", 0, 12));
        textArea.setMargin(new Insets(3, 3, 3, 3));
        textArea.setCaretPosition(0);

        this.setPreferredSize(new Dimension(600, 450));

        GridBagConstraints gbc = new GridBagConstraints();
        Insets ins = new Insets(7, 7, 7, 7);
        gbc.insets = ins;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        this.add(scroller, gbc);

        this.setFocusable(true);
    }

    /**
     * <p>Sets the text within the text area as the
     * passed argument.
     *
     * @param the text to be displayed.
     */
    public void setEditorText(String text) {
        textArea.setText(text);
        textArea.setCaretPosition(0);
    }

    /**
     * <p>Retrieves the text component.
     *
     * @return the text area component
     */
    public JTextComponent getEditorTextComponent() {
        return textArea;
    }

    /**
     * <p>Retrieves the text within the text area.
     *
     * @return the text within the text area
     */
    public String getEditorText() {
        return textArea.getText();
    }

    public boolean canPrint() {
        return true;
    }

    public Printable getPrintable() {
        return new TextPrinter(textArea.getText());
    }

    public String getPrintJobName() {
        return "RedXpert";
    }

    public void paste() {
        textArea.paste();
    }

    public void copy() {
        textArea.copy();
    }

    public void cut() {
        textArea.cut();
    }

    public int save(boolean saveAs) {
        return TextUtilities.save(textArea);
    }

    public void changeSelectionToUnderscore() {
        TextUtilities.changeSelectionToUnderscore(textArea);
    }

    public void changeSelectionToCamelCase() {
        TextUtilities.changeSelectionToCamelCase(textArea);
    }

    public void changeSelectionCase(boolean upper) {
        TextUtilities.changeSelectionCase(textArea, upper);
    }

    public void deleteLine() {
        TextUtilities.deleteLine(textArea);
    }

    public void deleteWord() {
        TextUtilities.deleteWord(textArea);
    }

    public void deleteSelection() {
        TextUtilities.deleteSelection(textArea);
    }

    public void insertFromFile() {
        TextUtilities.insertFromFile(textArea);
    }

    public void selectAll() {
        TextUtilities.selectAll(textArea);
    }

    public void selectNone() {
        TextUtilities.selectNone(textArea);
    }

    public void insertLineAfter() {
        TextUtilities.insertLineAfter(textArea);
    }

    public void insertLineBefore() {
        TextUtilities.insertLineBefore(textArea);
    }

    public void disableUpdates(boolean disable) {
    }

    public boolean canSearch() {
        return false;
    }

    public boolean contentCanBeSaved() {
        return false;
    }

    public String getDisplayName() {
        return "";
    }

}







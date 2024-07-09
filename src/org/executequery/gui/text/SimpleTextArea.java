/*
 * SimpleTextArea.java
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

import org.executequery.gui.editor.QueryEditorSettings;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.underworldlabs.swing.menu.SimpleTextComponentPopUpMenu;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.awt.*;

/**
 * @author Takis Diakoumis
 */
public class SimpleTextArea extends JPanel {

    private RSyntaxTextArea textArea;
    private RTextScrollPane scrollPane;

    public SimpleTextArea(String label) {
        this();
        setBorder(BorderFactory.createTitledBorder(label));
    }

    public SimpleTextArea() {
        super(new BorderLayout());
        init();
    }

    private void init() {
        textArea = new RSyntaxTextArea();
        scrollPane = new RTextScrollPane(textArea);
        new SimpleTextComponentPopUpMenu(textArea);

        applyUserProperties();
        this.add(scrollPane, BorderLayout.CENTER);
    }

    private void applyUserProperties() {
        Color foreground = SystemProperties.getColourProperty("user", "editor.text.foreground.colour");
        Color background = SystemProperties.getColourProperty("user", "editor.text.background.colour");
        Color selection = SystemProperties.getColourProperty("user", "editor.text.selection.background");

        scrollPane.setLineNumbersEnabled(true);
        scrollPane.setFoldIndicatorEnabled(true);
        scrollPane.getGutter().setBackground(background);
        scrollPane.getGutter().setLineNumberColor(foreground);
        scrollPane.getGutter().setCurrentLineNumberColor(selection);
        scrollPane.getGutter().setLineNumberFont(QueryEditorSettings.getEditorFont());

        textArea.setCaretPosition(0);
        textArea.setDragEnabled(true);
        textArea.setForeground(foreground);
        textArea.setBackground(background);
        textArea.setSelectionColor(selection);
        textArea.setUseSelectedTextColor(true);
        textArea.setBracketMatchingEnabled(false);
        textArea.setFont(QueryEditorSettings.getEditorFont());
        textArea.setSelectedTextColor(SystemProperties.getColourProperty("user", "editor.text.selection.foreground"));
        textArea.setCurrentLineHighlightColor(SystemProperties.getColourProperty("user", "editor.display.linehighlight.colour"));
    }

    public RSyntaxTextArea getTextAreaComponent() {
        return textArea;
    }

}

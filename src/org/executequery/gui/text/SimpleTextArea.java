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

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.underworldlabs.swing.menu.SimpleTextComponentPopUpMenu;

import javax.swing.*;
import java.awt.*;

/**
 * @author Takis Diakoumis
 */
public class SimpleTextArea extends JPanel {

    private RSyntaxTextArea textArea;

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
        RTextScrollPane scrollPane = new RTextScrollPane(textArea);
        scrollPane.setLineNumbersEnabled(true);
        scrollPane.setFoldIndicatorEnabled(true);
        new SimpleTextComponentPopUpMenu(textArea);

        //textArea.setFont(new Font("monospaced", 0, 12));
        textArea.setMargin(new Insets(3, 3, 3, 3));
        textArea.setCaretPosition(0);
        textArea.setDragEnabled(true);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    public RSyntaxTextArea getTextAreaComponent() {
        return textArea;
    }

}

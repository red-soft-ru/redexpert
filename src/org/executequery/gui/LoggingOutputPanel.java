/*
 * LoggingOutputPanel.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

package org.executequery.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.executequery.UserPreferencesManager;
import org.executequery.components.BasicPopupMenuListener;
import org.executequery.components.LoggingOutputPane;
import org.underworldlabs.swing.plaf.UIUtils;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
*/
public class LoggingOutputPanel extends JPanel
                                implements DocumentListener, ReadOnlyTextPane {

    private LoggingOutputPane outputPane;

    public LoggingOutputPanel() {

        super(new BorderLayout());

        outputPane = new LoggingOutputPane();
        outputPane.setMargin(new Insets(5, 5, 5, 5));
        outputPane.setDisabledTextColor(Color.black);

        Color bg = UserPreferencesManager.getOutputPaneBackground();
        outputPane.setBackground(bg);

        JScrollPane textOutputScroller = new JScrollPane(outputPane);
        textOutputScroller.setBackground(bg);
        textOutputScroller.setBorder(null);
        textOutputScroller.getViewport().setBackground(bg);

        setBorder(BorderFactory.createLineBorder(UIUtils.getDefaultBorderColour()));

        add(textOutputScroller, BorderLayout.CENTER);
        addDocumentListener(this);

        ReadOnlyTextPanePopUpMenu readOnlyTextPanePopUpMenu = new ReadOnlyTextPanePopUpMenu(this);
        outputPane.addMouseListener(new BasicPopupMenuListener(readOnlyTextPanePopUpMenu));
    }

    @Override
    public void setBackground(Color bg) {

        super.setBackground(bg);
        if (outputPane != null) {

            outputPane.setBackground(bg);
        }
    }

    public void append(String text) {

        outputPane.append(text);
    }

    public void append(int type, String text) {

        outputPane.append(type, text);
    }

    public void appendError(String text) {

        outputPane.appendError(text);
    }

    public void appendWarning(String text) {

        outputPane.appendWarning(text);
    }

    public void appendPlain(String text) {

        outputPane.appendPlain(text);
    }

    public void appendAction(String text) {

        outputPane.appendAction(text);
    }

    public void appendErrorFixedWidth(String text) {

        outputPane.appendErrorFixedWidth(text);
    }

    public void appendWarningFixedWidth(String text) {

        outputPane.appendWarningFixedWidth(text);
    }

    public void appendPlainFixedWidth(String text) {

        outputPane.appendPlainFixedWidth(text);
    }

    public void appendActionFixedWidth(String text) {

        outputPane.appendActionFixedWidth(text);
    }

    public Document getDocument() {

        return outputPane.getDocument();
    }

    public void addDocumentListener(DocumentListener listener) {

        outputPane.getDocument().addDocumentListener(listener);
    }

    public JTextPane getTextPane() {

        return outputPane;
    }

    public void changedUpdate(DocumentEvent e) {

        documentChanged();
    }

    public void insertUpdate(DocumentEvent e) {

        documentChanged();
    }

    public void removeUpdate(DocumentEvent e) {

        documentChanged();
    }

    private void documentChanged() {

        outputPane.setCaretPosition(getDocument().getLength());
    }

    public JTextComponent getTextComponent() {

        return outputPane;
    }
    
    public void clear() {

        outputPane.setText("");
        outputPane.setCaretPosition(0);
    }

    public void selectAll() {

        outputPane.selectAll();
    }

    public String getText() {

        return outputPane.getText();
    }

    public void copy() {

        outputPane.copy();
    }

}






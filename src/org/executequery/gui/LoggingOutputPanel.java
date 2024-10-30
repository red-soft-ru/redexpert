/*
 * LoggingOutputPanel.java
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

package org.executequery.gui;

import org.executequery.UserPreferencesManager;
import org.executequery.components.BasicPopupMenuListener;
import org.executequery.gui.logging.output.LoggingOutputPane;
import org.executequery.gui.logging.output.LoggingStream;
import org.underworldlabs.swing.plaf.UIUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;

/// @author Takis Diakoumis
public class LoggingOutputPanel extends JPanel
        implements DocumentListener,
        ReadOnlyTextPane {

    private final LoggingOutputPane outputPane;

    public LoggingOutputPanel() {
        super(new BorderLayout());

        Color bg = UserPreferencesManager.getOutputPaneBackground();

        outputPane = new LoggingOutputPane();
        outputPane.setMargin(new Insets(5, 5, 5, 5));
        outputPane.setDisabledTextColor(Color.black);
        outputPane.setBackground(bg);

        JScrollPane scrollPane = new JScrollPane(outputPane);
        scrollPane.getViewport().setBackground(bg);
        scrollPane.setBackground(bg);
        scrollPane.setBorder(null);

        setBorder(BorderFactory.createLineBorder(UIUtils.getDefaultBorderColour()));
        add(scrollPane, BorderLayout.CENTER);
        addDocumentListener(this);

        ReadOnlyTextPanePopUpMenu popUpMenu = new ReadOnlyTextPanePopUpMenu(this);
        outputPane.addMouseListener(new BasicPopupMenuListener(popUpMenu));
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

    public void appendActionFixedWidth(String text) {
        outputPane.appendActionFixedWidth(text);
    }

    public Document getDocument() {
        return outputPane.getDocument();
    }

    public void addDocumentListener(DocumentListener listener) {
        outputPane.getDocument().addDocumentListener(listener);
    }

    public LoggingStream getLoggingStream(int bufferSize, boolean trimByLine) {
        return new LoggingStream(outputPane, bufferSize, trimByLine);
    }

    // --- JComponent impl ---

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (outputPane != null)
            outputPane.setBackground(bg);
    }

    // --- DocumentListener impl ---

    @Override
    public void changedUpdate(DocumentEvent e) {
        documentChanged();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        documentChanged();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        documentChanged();
    }

    private void documentChanged() {
        outputPane.setCaretPosition(getDocument().getLength());
    }

    // --- ReadOnlyTextPane impl ---

    @Override
    public JTextComponent getTextComponent() {
        return outputPane;
    }

    @Override
    public void clear() {
        outputPane.setText("");
        outputPane.setCaretPosition(0);
    }

    @Override
    public void selectAll() {
        outputPane.selectAll();
    }

    @Override
    public String getText() {
        return outputPane.getText();
    }

    @Override
    public void copy() {
        outputPane.copy();
    }

}

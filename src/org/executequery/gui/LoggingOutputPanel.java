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

import org.executequery.GUIUtilities;
import org.executequery.UserPreferencesManager;
import org.executequery.components.BasicPopupMenuListener;
import org.executequery.components.LoggingOutputPane;
import org.executequery.log.Log;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author Takis Diakoumis
 */
public class LoggingOutputPanel extends JPanel
        implements DocumentListener, ReadOnlyTextPane {

    private LoggingStream loggingStream;
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

    public LoggingStream getLoggingStream() {
        if (loggingStream == null)
            loggingStream = new LoggingStream();
        return loggingStream;
    }

    /// Class that allows to work with the <code>LoggingOutputStream</code> as a <code>OutputStream</code>
    public class LoggingStream extends ByteArrayOutputStream {
        private Path logFilePath;

        public void setLogFilePath(String filePath) {
            if (MiscUtils.isNull(filePath)) {
                logFilePath = null;
                return;
            }

            try {
                logFilePath = Paths.get(filePath);
                if (Files.notExists(logFilePath.getParent()))
                    Files.createDirectories(logFilePath.getParent());

            } catch (IOException e) {
                Log.error(e.getMessage(), e);
                GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e, getClass());
                this.logFilePath = null;
            }
        }

        private void printToPanel(String val) {
            GUIUtils.invokeLater(() -> append(val));
        }

        private void printToFile(String val) {
            GUIUtils.invokeLater(() -> {
                try {
                    if (logFilePath != null) {
                        Files.write(
                                logFilePath,
                                val.getBytes(),
                                StandardOpenOption.CREATE,
                                StandardOpenOption.APPEND
                        );
                    }
                } catch (IOException e) {
                    Log.error(e.getMessage(), e);
                }
            });
        }

        @Override
        public void write(int b) {
            printToPanel(String.valueOf((char) b));
            printToFile(String.valueOf((char) b));
        }

        @Override
        public void write(byte[] b, int off, int len) {
            printToPanel(new String(b, off, len));
            printToFile(new String(b, off, len));
        }

    } // LoggingStream class

}

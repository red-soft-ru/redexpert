/*
 * LoggingOutputPane.java
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

package org.executequery.gui.logging.output;

import org.executequery.Constants;
import org.executequery.log.Log;
import org.executequery.sql.SqlMessages;
import org.executequery.util.UserProperties;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/// @author Takis Diakoumis
public class LoggingOutputPane extends JTextPane {
    private final OutputPaneDocument document;

    public LoggingOutputPane() {
        this.document = new OutputPaneDocument();

        setDocument(document);
        setBackground(SystemProperties.getColourProperty("user", "editor.output.background"));
    }

    public void append(String text) {
        appendPlain(text);
    }

    public void append(int type, String text) {
        switch (type) {

            case SqlMessages.ACTION_MESSAGE:
                appendAction(text);
                break;

            case SqlMessages.ERROR_MESSAGE:
                appendError(text);
                break;

            case SqlMessages.WARNING_MESSAGE:
                appendWarning(text);
                break;

            case SqlMessages.ACTION_MESSAGE_PREFORMAT:
                appendActionFixedWidth(text);
                break;

            case SqlMessages.ERROR_MESSAGE_PREFORMAT:
                appendErrorFixedWidth(text);
                break;

            case SqlMessages.WARNING_MESSAGE_PREFORMAT:
                appendWarningFixedWidth(text);
                break;

            case SqlMessages.PLAIN_MESSAGE_PREFORMAT:
                appendPlainFixedWidth(text);
                break;

            case SqlMessages.PLAIN_MESSAGE:
            default:
                appendPlain(text);
                break;
        }
    }

    public void appendError(String text) {
        document.appendError(text);
    }

    public void appendWarning(String text) {
        document.appendWarning(text);
    }

    public void appendPlain(String text) {
        document.appendPlain(text);
    }

    public void appendAction(String text) {
        document.appendAction(text);
    }

    public void appendErrorFixedWidth(String text) {
        document.appendErrorFixedWidth(text);
    }

    public void appendWarningFixedWidth(String text) {
        document.appendWarningFixedWidth(text);
    }

    public void appendPlainFixedWidth(String text) {
        document.appendPlainFixedWidth(text);
    }

    public void appendActionFixedWidth(String text) {
        document.appendActionFixedWidth(text);
    }

    // --- JTextComponent impl ---

    @Override
    public boolean isEditable() {
        return false;
    }

    // ---

    private static class OutputPaneDocument extends DefaultStyledDocument {
        private final StringBuilder textBuffer;

        // --- colors ---
        private final Color plainColor = UserProperties.getInstance().getColourProperty("editor.output.plain.color");
        private final Color errorColor = UserProperties.getInstance().getColourProperty("editor.output.error.color");
        private final Color actionColor = UserProperties.getInstance().getColourProperty("editor.output.action.color");
        private final Color warningColor = UserProperties.getInstance().getColourProperty("editor.output.warning.color");

        // --- normal font ---
        private final MutableAttributeSet plain = new SimpleAttributeSet();
        private final MutableAttributeSet error = new SimpleAttributeSet();
        private final MutableAttributeSet action = new SimpleAttributeSet();
        private final MutableAttributeSet warning = new SimpleAttributeSet();

        // --- fixed width font ---
        private final MutableAttributeSet plainFixedWidth = new SimpleAttributeSet();
        private final MutableAttributeSet errorFixedWidth = new SimpleAttributeSet();
        private final MutableAttributeSet actionFixedWidth = new SimpleAttributeSet();
        private final MutableAttributeSet warningFixedWidth = new SimpleAttributeSet();

        public OutputPaneDocument() {
            this.textBuffer = new StringBuilder();
            initPlainStyles();
            initFixedStyles();
        }

        private void initPlainStyles() {
            StyleConstants.setForeground(plain, plainColor);
            StyleConstants.setForeground(error, errorColor);
            StyleConstants.setForeground(action, actionColor);
            StyleConstants.setForeground(warning, warningColor);
        }

        protected void initFixedStyles() {
            String fontName = "monospaced";
            StyleConstants.setFontFamily(plainFixedWidth, fontName);
            StyleConstants.setFontFamily(errorFixedWidth, fontName);
            StyleConstants.setFontFamily(warningFixedWidth, fontName);
            StyleConstants.setFontFamily(actionFixedWidth, fontName);

            StyleConstants.setForeground(plainFixedWidth, plainColor);
            StyleConstants.setForeground(errorFixedWidth, errorColor);
            StyleConstants.setForeground(actionFixedWidth, actionColor);
            StyleConstants.setForeground(warningFixedWidth, warningColor);
        }

        protected void appendErrorFixedWidth(String text) {
            append(text, errorFixedWidth);
        }

        protected void appendWarningFixedWidth(String text) {
            append(text, warningFixedWidth);
        }

        protected void appendPlainFixedWidth(String text) {
            append(text, plainFixedWidth);
        }

        protected void appendActionFixedWidth(String text) {
            append(text, actionFixedWidth);
        }

        protected void appendError(String text) {
            append(text, error);
        }

        protected void appendWarning(String text) {
            append(text, warning);
        }

        protected void appendPlain(String text) {
            append(text, plain);
        }

        protected void appendAction(String text) {
            append(text, action);
        }

        protected void append(final String text, final AttributeSet attrs) {
            GUIUtils.invokeLater(() -> print(text, attrs));
        }

        private void print(String text, AttributeSet attrs) {
            try {

                int length = getLength();
                if (length > 0)
                    textBuffer.append(Constants.NEW_LINE_CHAR);

                textBuffer.append(text).append(Constants.NEW_LINE_CHAR);
                insertString(length, textBuffer.toString(), attrs);

            } catch (BadLocationException e) {
                Log.debug(e.getMessage(), e);

            } finally {
                textBuffer.setLength(0);
            }
        }

    } // class OutputPaneDocument

}

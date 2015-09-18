/*
 * LoggingOutputPane.java
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

package org.executequery.components;

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.executequery.Constants;
import org.executequery.sql.SqlMessages;
import org.underworldlabs.swing.GUIUtils;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class LoggingOutputPane extends JTextPane {
        
    private OutputPaneDocument document;

    public LoggingOutputPane() {
        document = new OutputPaneDocument();
        setDocument(document);
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
            case SqlMessages.PLAIN_MESSAGE:
                appendPlain(text);
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

    public boolean isEditable() {
        return false;
    }
    
    class OutputPaneDocument extends DefaultStyledDocument {
        
        private StringBuffer textBuffer;
        
        // normal font
        protected MutableAttributeSet plain;
        protected MutableAttributeSet error;
        protected MutableAttributeSet warning;
        protected MutableAttributeSet action;

        // fixed width font
        protected MutableAttributeSet plainFixedWidth;
        protected MutableAttributeSet errorFixedWidth;
        protected MutableAttributeSet warningFixedWidth;
        protected MutableAttributeSet actionFixedWidth;
        
        public OutputPaneDocument() {
            initStyles();
            textBuffer = new StringBuffer();
        }
        
        protected void initStyles() {
            // normal font styles
            plain = new SimpleAttributeSet();
            StyleConstants.setForeground(plain, Color.BLACK);

            error = new SimpleAttributeSet();
            StyleConstants.setForeground(error, Color.RED.darker());

            warning = new SimpleAttributeSet();
            StyleConstants.setForeground(warning, new Color(222,136,8));

            action = new SimpleAttributeSet();
            StyleConstants.setForeground(action, Color.BLUE.darker());
            
            // fixed width font styles
            String fixedWidthFontName = "monospaced";
            plainFixedWidth = new SimpleAttributeSet(plain);
            StyleConstants.setFontFamily(plainFixedWidth, fixedWidthFontName);

            errorFixedWidth = new SimpleAttributeSet(error);
            StyleConstants.setFontFamily(errorFixedWidth, fixedWidthFontName);

            warningFixedWidth = new SimpleAttributeSet(warning);
            StyleConstants.setFontFamily(warningFixedWidth, fixedWidthFontName);

            actionFixedWidth = new SimpleAttributeSet(action);
            StyleConstants.setFontFamily(actionFixedWidth, fixedWidthFontName);

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
            
            GUIUtils.invokeLater(new Runnable() {
                
                public void run() {
                
                    int length = getLength();
                    if (length > 0) {
        
                        textBuffer.append(Constants.NEW_LINE_CHAR);
                    }
                    
                    textBuffer.append(text).append(Constants.NEW_LINE_CHAR);
        
                    try {

                        insertString(length, textBuffer.toString(), attrs);
                    
                    } catch (BadLocationException	e) {}
        
                    textBuffer.setLength(0);
                    
                }
            
            });

        }

    } // class OutputPaneDocument
    
}


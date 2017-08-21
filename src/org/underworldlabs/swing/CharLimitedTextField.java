/*
 * CharLimitedTextField.java
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

package org.underworldlabs.swing;

import java.awt.Toolkit;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1766 $
 * @date     $Date: 2017-08-14 23:34:37 +1000 (Mon, 14 Aug 2017) $
 */
public class CharLimitedTextField extends JTextField {

    private int maxLength;
    private CharLimitedDocument charLimitedDocument;
    
    public CharLimitedTextField(int maxLength) {
        this.maxLength = maxLength;
        this.charLimitedDocument = new CharLimitedDocument();
    }
    
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
    
    public int getMaxLength() {
        return maxLength;
    }

    protected Document createDefaultModel() {
        if (charLimitedDocument == null) {
            charLimitedDocument = new CharLimitedDocument();
        }
        return charLimitedDocument;
    }
 
    class CharLimitedDocument extends PlainDocument {

        private Toolkit toolkit;

        public CharLimitedDocument() {
            toolkit = Toolkit.getDefaultToolkit();
        }

        public void insertString(int offs, String str, AttributeSet a)
            throws BadLocationException {
            if (getLength() >= maxLength) {
                toolkit.beep();
                return;
            }
            super.insertString(offs, str, a);
        }
    }

}















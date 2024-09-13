/*
 * NumberTextField.java
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

package org.underworldlabs.swing;

import org.executequery.log.Log;
import org.underworldlabs.swing.menu.SimpleTextComponentPopUpMenu;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * @author Takis Diakoumis
 */
public class NumberTextField extends JTextField {

    private final int digits;
    private final boolean negativeNumbers;
    private final NumberFormat integerFormatter;

    private NumberDocument numberDocument;

    public NumberTextField() {
        this(-1, true);
    }

    public NumberTextField(int digits) {
        this(digits, true);
    }

    public NumberTextField(int digits, boolean negativeNumbers) {
        super();
        this.digits = digits;
        this.negativeNumbers = negativeNumbers;

        integerFormatter = NumberFormat.getNumberInstance();
        integerFormatter.setParseIntegerOnly(true);

        initModel();
        initPopup();
    }

    private void initModel() {
        if (numberDocument == null) {
            numberDocument = new NumberDocument();
            numberDocument.setDigits(digits);
            numberDocument.setEnableNegativeNumbers(negativeNumbers);
        }
    }

    private void initPopup() {
        new SimpleTextComponentPopUpMenu(this);
    }

    public void setEnableNegativeNumbers(boolean enableNegativeNumbers) {
        numberDocument.setEnableNegativeNumbers(enableNegativeNumbers);
    }

    public void setDigits(int digits) {
        this.numberDocument.setDigits(digits);
    }

    public int getValue() {
        try {

            String value = getText();
            if (MiscUtils.isNull(value))
                value = "0";

            return integerFormatter.parse(value).intValue();

        } catch (ParseException e) {
            Log.error(e.getMessage(), e);
            return 0;
        }
    }

    public long getLongValue() {
        try {

            String value = getText();
            if (MiscUtils.isNull(value))
                value = "0";

            return integerFormatter.parse(value).longValue();

        } catch (ParseException e) {
            Log.error(e.getMessage(), e);
            return 0;
        }
    }

    public String getStringValue() {
        return String.valueOf(getLongValue());
    }

    public void setValue(int value) {
        setText(integerFormatter.format(value));
    }

    public void setLongValue(long value) {
        setText(integerFormatter.format(value));
    }

    @Override
    protected Document createDefaultModel() {
        initModel();
        return numberDocument;
    }

    private static class NumberDocument extends PlainDocument {

        private boolean enableNegativeNumbers;
        private final Toolkit toolkit;
        private int digits;

        public NumberDocument() {
            toolkit = Toolkit.getDefaultToolkit();
            enableNegativeNumbers = true;
        }

        public void setEnableNegativeNumbers(boolean enableNegativeNumbers) {
            this.enableNegativeNumbers = enableNegativeNumbers;
        }

        public void setDigits(int digits) {
            this.digits = digits;
        }

        @Override
        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {

            if (digits > 0 && getLength() >= digits) {
                toolkit.beep();
                return;
            }

            int j = 0;
            char[] source = str.toCharArray();
            char[] result = new char[source.length];

            for (int i = 0; i < result.length; i++) {

                boolean isNegative = offs == 0 && i == 0 && source[i] == '-';
                if (Character.isDigit(source[i]) || enableNegativeNumbers && isNegative) {
                    result[j++] = source[i];
                } else
                    toolkit.beep();
            }

            super.insertString(offs, new String(result, 0, j), a);
        }

    } // NumberDocument class

}

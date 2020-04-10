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

    private NumberFormat integerFormatter;
    private WholeNumberDocument numberDocument;
    private int digits;
    private boolean negativeNumbers;

    public NumberTextField() {
        this(true);
    }

    public NumberTextField(boolean negativeNumbers) {

        super();

        this.negativeNumbers = negativeNumbers;
        this.digits = -1;
        if (numberDocument == null)
            this.numberDocument = new WholeNumberDocument();
        numberDocument.setDigits(digits);
        numberDocument.setEnableNegativeNumbers(this.negativeNumbers);

        this.integerFormatter = NumberFormat.getNumberInstance();
        integerFormatter.setParseIntegerOnly(true);
    }

    public NumberTextField(int digits) {
        this();
        numberDocument.setDigits(digits);
        this.digits = digits;
    }

    public boolean isEnableNegativeNumbers() {
        return numberDocument.isEnableNegativeNumbers();
    }

    public void setEnableNegativeNumbers(boolean enableNegativeNumbers) {
        numberDocument.setEnableNegativeNumbers(enableNegativeNumbers);
    }

    public void setDigits(int digits) {
        this.digits = digits;
    }

    public int getDigits() {
        return digits;
    }

    public int getValue() {
        int retVal = 0;
        try {
            String value = getText();
            if (MiscUtils.isNull(value)) {
                value = "0";
            }
            retVal = integerFormatter.parse(value).intValue();
        } catch (ParseException e) {
            //toolkit.beep();
        }
        return retVal;
    }

    public long getLongValue() {
        long retVal = 0;
        try {
            String value = getText();
            if (MiscUtils.isNull(value)) {
                value = "0";
            }
            retVal = integerFormatter.parse(value).longValue();
        } catch (ParseException e) {
            //toolkit.beep();
        }
        return retVal;
    }

    public String getStringValue() {
        return String.valueOf(getLongValue());
    }

    public boolean isZero() {
        return getValue() == 0;
    }

    public void setValue(int value) {
        setText(integerFormatter.format(value));
    }

    public void setLongValue(long value) {
        setText(integerFormatter.format(value));
    }

    protected Document createDefaultModel() {

        if (numberDocument == null)
            numberDocument = new WholeNumberDocument();
        numberDocument.setEnableNegativeNumbers(negativeNumbers);
        return numberDocument;

    }

}


class WholeNumberDocument extends PlainDocument {

    private Toolkit toolkit;
    private int digits;

    private boolean enableNegativeNumbers;

    public WholeNumberDocument() {
        toolkit = Toolkit.getDefaultToolkit();
        enableNegativeNumbers = true;
    }

    public boolean isEnableNegativeNumbers() {
        return enableNegativeNumbers;
    }

    public void setEnableNegativeNumbers(boolean enableNegativeNumbers) {
        this.enableNegativeNumbers = enableNegativeNumbers;
    }

    public int getDigits() {
        return digits;
    }

    public void setDigits(int digits) {
        this.digits = digits;
    }

    public void insertString(int offs, String str, AttributeSet a)
            throws BadLocationException {

        if (digits > 0) {

            if (getLength() >= digits) {
                toolkit.beep();
                return;
            }

        }

        int j = 0;
        char[] source = str.toCharArray();
        char[] result = new char[source.length];

        for (int i = 0; i < result.length; i++) {

            if (Character.isDigit(source[i]) || enableNegativeNumbers &&
                    (offs == 0 && i == 0 && source[i] == '-')) {
                result[j++] = source[i];
            } else {
                toolkit.beep();
            }

        }

        super.insertString(offs, new String(result, 0, j), a);
    }

} // class WholeNumberDocument







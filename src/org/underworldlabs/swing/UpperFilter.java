package org.underworldlabs.swing;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class UpperFilter extends DocumentFilter {
    public void insertString(FilterBypass fb, int offset, String string,
                             AttributeSet attr) throws BadLocationException {
        super.insertString(fb, offset, string.toUpperCase(), attr);
    }

    public void replace(FilterBypass fb, int offset, int length, String text,
                        AttributeSet attrs) throws BadLocationException {
        super.replace(fb, offset, length, text.toUpperCase(), attrs);
    }
}
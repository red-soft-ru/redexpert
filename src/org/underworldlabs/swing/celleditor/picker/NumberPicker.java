package org.underworldlabs.swing.celleditor.picker;

import org.executequery.databaseobjects.Types;
import org.underworldlabs.Constants;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.math.BigInteger;

/**
 * Simple number value table column cell editor
 * with values formatter.
 *
 * @author Alexey Kozlov
 */
public class NumberPicker extends JTextField
        implements DefaultPicker {

    private static final BigInteger INT128_MAX_VALUE =
            new BigInteger("7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
    private static final BigInteger INT128_MIN_VALUE =
            new BigInteger("-80000000000000000000000000000000", 16);

    private final int numberType;

    public NumberPicker(int numberType) {
        super();
        this.numberType = numberType;

        setHorizontalAlignment(JTextField.LEFT);
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));

        /*addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                processKeyTyping(e);
            }
        });*/
        PlainDocument doc = (PlainDocument) getDocument();
        doc.setDocumentFilter(new NumberFilter());
    }

    @Override
    public String getValue() {
        return getText();
    }

    @Override
    public void setValue(Object value) {
        setText(value != null ? value.toString() : Constants.EMPTY);
    }

    @Override
    public JTextField getEditorComponent() {
        return this;
    }

    protected boolean checkValue(String value) {

        try {


            switch (numberType) {

                case Types.BIGINT:
                    Long.parseLong(value);
                    break;

                case Types.INTEGER:
                    Integer.parseInt(value);
                    break;

                case Types.SMALLINT:
                    Short.parseShort(value);
                    break;

                case Types.INT128:
                    BigInteger int128 = new BigInteger(value);
                    if (int128.compareTo(INT128_MAX_VALUE) > 0 || int128.compareTo(INT128_MIN_VALUE) < 0)
                        throw new NumberFormatException();
                    break;
            }
            return true;

        } catch (NumberFormatException ex) {
            return false;
        }
    }

    class NumberFilter extends DocumentFilter {
        public void insertString(FilterBypass fb, int offset, String string,
                                 AttributeSet attr) throws BadLocationException {
            StringBuilder sb = new StringBuilder(getText());
            sb.insert(offset, string);
            if (checkValue(sb.toString()))
                super.insertString(fb, offset, string, attr);
        }

        public void replace(FilterBypass fb, int offset, int length, String string,
                            AttributeSet attrs) throws BadLocationException {
            StringBuilder sb = new StringBuilder(getText());
            sb.replace(offset, offset + length, string);
            if (checkValue(sb.toString()))
                super.replace(fb, offset, length, string, attrs);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            StringBuilder sb = new StringBuilder(getText());
            sb.delete(offset, offset + length);
            if (checkValue(sb.toString()))
                super.remove(fb, offset, length);
        }
    }


}

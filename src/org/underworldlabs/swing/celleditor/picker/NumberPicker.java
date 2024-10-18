package org.underworldlabs.swing.celleditor.picker;

import org.executequery.databaseobjects.Types;
import org.executequery.log.Log;
import org.underworldlabs.Constants;
import org.underworldlabs.util.MiscUtils;

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

        setHorizontalAlignment(SwingConstants.LEFT);
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        getDocument().setDocumentFilter(new NumberFilter());
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

    @Override
    public PlainDocument getDocument() {
        return (PlainDocument) super.getDocument();
    }

    protected boolean validate(String value) {

        if (MiscUtils.isNull(value))
            return true;

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
                case Types.DECIMAL:
                case Types.DOUBLE:
                case Types.NUMERIC:
                case Types.FLOAT:
                    Double.parseDouble(value);
                    break;
                default:
                    throw new IllegalArgumentException(
                            String.format("Type [%d] is not supported by NumberPicker", numberType)
                    );
            }

        } catch (NumberFormatException ex) {
            Log.debug(ex.getMessage(), ex);
            return false;

        } catch (IllegalArgumentException ex) {
            Log.error(ex.getMessage(), ex);
            return false;
        }

        return true;
    }

    private class NumberFilter extends DocumentFilter {

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {

            StringBuilder sb = getStringBuilder();
            sb.insert(offset, string);
            if (validate(sb.toString()))
                super.insertString(fb, offset, string, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs)
                throws BadLocationException {

            StringBuilder sb = getStringBuilder();
            sb.replace(offset, offset + length, string);
            if (validate(sb.toString()))
                super.replace(fb, offset, length, string, attrs);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {

            StringBuilder sb = getStringBuilder();
            sb.delete(offset, offset + length);
            if (validate(sb.toString()))
                super.remove(fb, offset, length);
        }

        private StringBuilder getStringBuilder() {
            return new StringBuilder(getText());
        }

    } // NumberFilter class

}

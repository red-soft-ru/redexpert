package org.underworldlabs.swing.table;

import org.executequery.databaseobjects.Types;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.math.BigInteger;

/**
 * Simple number value table column cell editor
 * with values formatter.
 *
 * @author Alexey Kozlov
 */
public class ValidatedNumberCellEditor extends JTextField
        implements TableCellEditorValue {

    private static final BigInteger INT128_MAX_VALUE =
            new BigInteger("7FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
    private static final BigInteger INT128_MIN_VALUE =
            new BigInteger("-800000000000000FFFFFFFFFFFFFFFFF", 16);

    private final int numberType;

    public ValidatedNumberCellEditor(int numberType) {
        super();
        setBorder(null);
        setHorizontalAlignment(JTextField.LEFT);

        this.numberType = numberType;
        setKeyListener();
    }

    /**
     * Returns the current editor value from the component
     * defining this object.
     *
     * @return the editor's value
     */
    @Override
    public String getEditorValue() {
        return getText();
    }

    /**
     * Returns the current editor value string.
     */
    public String getValue() {
        return getEditorValue();
    }

    /**
     * Adding key listener on the editor
     * that prevents the input oj incorrect values.
     */
    private void setKeyListener() {
        this.addKeyListener(new java.awt.event.KeyAdapter() {

            @Override
            public void keyTyped(java.awt.event.KeyEvent evt) {

                if (evt.getKeyChar() == KeyEvent.VK_BACK_SPACE || evt.getKeyChar() == KeyEvent.VK_DELETE)
                    return;
                if (String.valueOf(evt.getKeyChar()).equals("-") && getCaretPosition() == 0)
                    return;

                try {
                    String value = String.valueOf(new StringBuilder(getText().trim())
                            .insert(getCaretPosition(), evt.getKeyChar()));

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

//                        case Types.INT128:
//                            BigInteger int128 = new BigInteger(value);
//                            if (int128.compareTo(INT128_MAX_VALUE) > 0 || int128.compareTo(INT128_MIN_VALUE) < 0)
//                                throw new NumberFormatException();
//                            break;
                    }

                } catch (NumberFormatException e) {
                    evt.consume();
                }

            }
        });
    }

}

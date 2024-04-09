package org.underworldlabs.swing.celleditor.picker;

import org.executequery.databaseobjects.Types;
import org.underworldlabs.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                processKeyTyping(e);
            }
        });
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

    private void processKeyTyping(KeyEvent evt) {

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

                case Types.INT128:
                    BigInteger int128 = new BigInteger(value);
                    if (int128.compareTo(INT128_MAX_VALUE) > 0 || int128.compareTo(INT128_MIN_VALUE) < 0)
                        throw new NumberFormatException();
                    break;
            }

        } catch (NumberFormatException ex) {
            evt.consume();
        }
    }

}

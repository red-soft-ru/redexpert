package org.executequery.gui.browser.generatortestdata.methodspanels;

import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.gui.text.SimpleTextArea;
import org.underworldlabs.swing.EQTimePicker;
import org.underworldlabs.swing.NumberTextField;

import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;
import java.time.LocalTime;
import java.util.Random;

public class RandomMethodPanel extends AbstractMethodPanel {
    private JPanel settingsPanel;
    private JTextField maxField;
    private JTextField minField;
    private EQTimePicker minTime;
    private EQTimePicker maxTime;
    private NumberTextField countSymbolsAfterComma;
    private JCheckBox useOnlyThisSymbolsBox;
    private SimpleTextArea useOnlyThisSymbolsField;
    //private JScrollPane scrollSymbols;
    private JTextField maxByteField;
    private JTextField minByteField;

    public RandomMethodPanel(DatabaseColumn col) {
        super(col);
        init();
    }

    private void init() {
        countSymbolsAfterComma = new NumberTextField();
        settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridBagLayout());
        if (col.getFormattedDataType().contentEquals("BIGINT")
                || col.getFormattedDataType().contentEquals("INTEGER")
                || col.getFormattedDataType().contentEquals("SMALLINT")
                || col.getFormattedDataType().contentEquals("DOUBLE PRECISION")
                || col.getFormattedDataType().contentEquals("FLOAT")
                || col.getFormattedDataType().startsWith("DECIMAL")
                || col.getFormattedDataType().startsWith("NUMERIC")) {

            if (col.getFormattedDataType().contentEquals("BIGINT")
                    || col.getFormattedDataType().contentEquals("DOUBLE PRECISION")
                    || col.getFormattedDataType().contentEquals("FLOAT")
                    || col.getFormattedDataType().startsWith("DECIMAL")
                    || col.getFormattedDataType().startsWith("NUMERIC")) {
                maxField = new JTextField();
                minField = new JTextField();
                if (col.getFormattedDataType().contentEquals("BIGINT")) {
                    maxField.setText("9223372036854775807");
                    minField.setText("-9223372036854775808");
                } else {
                    maxField.setText("0");
                    minField.setText("0");
                }
            } else {
                maxField = new NumberTextField();
                minField = new NumberTextField();
                if (col.getFormattedDataType().contentEquals("INTEGER")) {
                    maxField.setText("" + Integer.MAX_VALUE);
                    minField.setText("" + Integer.MIN_VALUE);
                } else {
                    maxField.setText("" + 32767);
                    minField.setText("" + (-32768));
                }
            }


            JLabel label = new JLabel("Min");
            settingsPanel.add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            settingsPanel.add(minField, new GridBagConstraints(1, 0, 1, 1, 1, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            label = new JLabel("Max");
            settingsPanel.add(label, new GridBagConstraints(2, 0, 1, 1, 0, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 20, 5, 5), 0, 0));
            settingsPanel.add(maxField, new GridBagConstraints(3, 0, 1, 1, 1, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            if (col.getFormattedDataType().contentEquals("DOUBLE PRECISION")
                    || col.getFormattedDataType().contentEquals("FLOAT")
                    || col.getFormattedDataType().startsWith("DECIMAL")
                    || col.getFormattedDataType().startsWith("NUMERIC")) {
                label = new JLabel("Count Digits After Comma");
                settingsPanel.add(label, new GridBagConstraints(0, 1, 1, 1, 0, 0,
                        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
                settingsPanel.add(countSymbolsAfterComma, new GridBagConstraints(1, 1, 3, 1, 1, 0,
                        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            }

        }
        if (col.getFormattedDataType().contentEquals("TIME")) {
            minTime = new EQTimePicker();
            minTime.setTime(LocalTime.MIN);
            maxTime = new EQTimePicker();
            maxTime.setTime(LocalTime.MAX);
            JLabel label = new JLabel("Min");
            settingsPanel.add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            settingsPanel.add(minTime, new GridBagConstraints(1, 0, 1, 1, 1, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            label = new JLabel("Max");
            settingsPanel.add(label, new GridBagConstraints(2, 0, 1, 1, 0, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 20, 5, 5), 0, 0));
            settingsPanel.add(maxTime, new GridBagConstraints(3, 0, 1, 1, 1, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        }
        if (col.getFormattedDataType().contains("CHAR")) {
            maxField = new NumberTextField();
            minField = new NumberTextField();
            maxField.setText("" + col.getColumnSize());
            minField.setText("0");
            useOnlyThisSymbolsBox = new JCheckBox("Use only this symbols");
            useOnlyThisSymbolsField = new SimpleTextArea();

            //scrollSymbols = new JScrollPane(useOnlyThisSymbolsField);
            //scrollSymbols.setVerticalScrollBar(scrollSymbols.createVerticalScrollBar());

            JLabel label = new JLabel("Min length");

            settingsPanel.add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            settingsPanel.add(minField, new GridBagConstraints(1, 0, 1, 1, 1, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            label = new JLabel("Max length");
            settingsPanel.add(label, new GridBagConstraints(2, 0, 1, 1, 0, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 20, 5, 5), 0, 0));
            settingsPanel.add(maxField, new GridBagConstraints(3, 0, 1, 1, 1, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            settingsPanel.add(useOnlyThisSymbolsBox, new GridBagConstraints(0, 1, 2, 1, 0, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 20, 5, 5), 0, 0));
            settingsPanel.add(useOnlyThisSymbolsField, new GridBagConstraints(0, 2, 4, 1, 1, 1,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));


        }
        if (col.getFormattedDataType().contains("BLOB")) {
            maxField = new NumberTextField();
            minField = new NumberTextField();
            maxField.setText("" + col.getColumnSize());
            minField.setText("0");
            maxByteField = new NumberTextField();
            minByteField = new NumberTextField();
            maxByteField.setText("255");
            minByteField.setText("0");


            JLabel label = new JLabel("Min Length");

            settingsPanel.add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            settingsPanel.add(minField, new GridBagConstraints(1, 0, 1, 1, 1, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            label = new JLabel("Max Length");
            settingsPanel.add(label, new GridBagConstraints(2, 0, 1, 1, 0, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 20, 5, 5), 0, 0));
            settingsPanel.add(maxField, new GridBagConstraints(3, 0, 1, 1, 1, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            label = new JLabel("Min Byte");

            settingsPanel.add(label, new GridBagConstraints(0, 1, 1, 1, 0, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            settingsPanel.add(minByteField, new GridBagConstraints(1, 1, 1, 1, 1, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            label = new JLabel("Max Byte");
            settingsPanel.add(label, new GridBagConstraints(2, 1, 1, 1, 0, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 20, 5, 5), 0, 0));
            settingsPanel.add(maxByteField, new GridBagConstraints(3, 1, 1, 1, 1, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));


        }
        setLayout(new GridBagLayout());
        add(settingsPanel, new GridBagConstraints(0, 0, 1, 1, 1, 1,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    }

    public Object getTestDataObject() {
        if (col.getFormattedDataType().contentEquals("BIGINT")) {
            BigInteger bigint = new BigInteger(62, new Random());
            BigInteger max = new BigInteger(maxField.getText());
            BigInteger min = new BigInteger(minField.getText());
            BigInteger zero = new BigInteger("0");
            BigInteger diapason;
            if (min.compareTo(zero) < 0 && max.compareTo(zero) > 0) {
                Random random = new Random();
                int x = random.nextInt();
                x = x % 2;
                if (x == 0) {
                    diapason = min.multiply(new BigInteger("-1"));
                    bigint = bigint.mod(diapason).multiply(new BigInteger("-1"));
                } else {
                    diapason = max;
                    bigint = bigint.mod(diapason);
                }
            } else {
                diapason = max.subtract(min);
                bigint = min.add(bigint.mod(diapason));
            }
            return bigint;
        }
        if (col.getFormattedDataType().contentEquals("TIME")) {
            minTime = new EQTimePicker();
            minTime.setTime(LocalTime.MIN);
            maxTime = new EQTimePicker();
            maxTime.setTime(LocalTime.MAX);
            JLabel label = new JLabel("Min");
            settingsPanel.add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            settingsPanel.add(minTime, new GridBagConstraints(1, 0, 1, 1, 1, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            label = new JLabel("Max");
            settingsPanel.add(label, new GridBagConstraints(2, 0, 1, 1, 0, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 20, 5, 5), 0, 0));
            settingsPanel.add(maxTime, new GridBagConstraints(3, 0, 1, 1, 1, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        }
        if (col.getFormattedDataType().contentEquals("INTEGER") || col.getFormattedDataType().contentEquals("SMALLINT")) {
            int value = new Random().nextInt();
            if (value < 0)
                value *= -1;
            int max = ((NumberTextField) maxField).getValue();
            int min = ((NumberTextField) minField).getValue();
            long diapason = max - min;
            if (diapason == 0) {
                value = max;
            } else
                value = (int) (min + (value % diapason));
            if (col.getFormattedDataType().contentEquals("SMALLINT"))
                return (short) value;
            return value;
        }
        if (col.getFormattedDataType().contentEquals("DOUBLE PRECISION")
                || col.getFormattedDataType().contentEquals("FLOAT")
                || col.getFormattedDataType().startsWith("DECIMAL")
                || col.getFormattedDataType().startsWith("NUMERIC")) {
            long value = new Random().nextLong();
            if (value < 0)
                value *= -1;
            long power = (long) Math.pow(10, countSymbolsAfterComma.getLongValue());
            long max = Long.parseLong(maxField.getText()) * power;
            long min = Long.parseLong(minField.getText()) * power;
            long diapason = max - min;
            if (diapason == 0) {
                value = max;
            } else
                value = (min + (value % diapason));
            return ((double) value) / ((double) power);
        }
        if (col.getFormattedDataType().contains("CHAR")) {
            long n = new Random().nextLong();
            if (n < 0)
                n *= -1;
            long max = ((NumberTextField) maxField).getLongValue() + 1;
            long min = ((NumberTextField) minField).getLongValue();
            long diapason = max - min;
            if (diapason == 0) {
                n = max;
            } else
                n = (min + (n % diapason));
            StringBuilder result = new StringBuilder();
            if (useOnlyThisSymbolsBox.isSelected()) {
                String charset = useOnlyThisSymbolsField.getTextAreaComponent().getText();
                int length = charset.length();
                for (int i = 0; i < n; i++) {
                    int x = new Random().nextInt(length);
                    result.append(charset.charAt(x));
                }
            } else {
                for (int i = 0; i < n; i++) {
                    int x = new Random().nextInt(127);
                    result.append((char) x);
                }
            }
            return result.toString();
        }
        if (col.getFormattedDataType().contains("BLOB")) {
            int n = new Random().nextInt();
            if (n < 0)
                n *= -1;
            int max = ((NumberTextField) maxField).getValue() + 1;
            int min = ((NumberTextField) minField).getValue();
            int diapason = max - min;
            if (diapason == 0) {
                n = max;
            } else
                n = (min + (n % diapason));
            max = ((NumberTextField) maxByteField).getValue() + 1;
            min = ((NumberTextField) minByteField).getValue();
            diapason = max - min;
            byte[] bytes = new byte[n];
            for (int i = 0; i < n; i++) {
                if (diapason == 0)
                    bytes[i] = (byte) max;
                int x = new Random().nextInt();
                if (x < 0)
                    x *= -1;
                x = min + (x % diapason);
                bytes[i] = (byte) x;
            }
            return bytes;
        }
        return null;
    }

}

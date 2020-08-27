package org.executequery.gui.browser.generatortestdata.methodspanels;

import com.github.lgooddatepicker.components.DatePicker;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.gui.text.SimpleTextArea;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.EQDateTimePicker;
import org.underworldlabs.swing.EQTimePicker;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;

public class RandomMethodPanel extends AbstractMethodPanel {
    private JPanel settingsPanel;
    private JTextField maxField;
    private JTextField minField;
    private EQTimePicker minTime;
    private EQTimePicker maxTime;
    private EQDateTimePicker minDateTime;
    private EQDateTimePicker maxDateTime;
    private DatePicker maxDate;
    private DatePicker minDate;
    private NumberTextField countSymbolsAfterComma;
    private JCheckBox useOnlyThisSymbolsBox;
    private SimpleTextArea useOnlyThisSymbolsField;
    //private JScrollPane scrollSymbols;
    private JTextField maxByteField;
    private JTextField minByteField;
    private JCheckBox nullBox;

    public RandomMethodPanel(DatabaseColumn col) {
        super(col);
        init();
    }

    private void init() {
        countSymbolsAfterComma = new NumberTextField();
        countSymbolsAfterComma.setText("1");
        nullBox = new JCheckBox(bundles("UseNull"));
        settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridBagLayout());
        GridBagHelper gbh = new GridBagHelper();
        GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0);
        gbh.setDefaults(gbc);

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
                    maxField.setText("1");
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


            JLabel label = new JLabel(bundles("Min"));
            settingsPanel.add(label, gbh.defaults().setLabelDefault().get());
            settingsPanel.add(minField, gbh.defaults().nextCol().setMaxWeightX().get());

            label = new JLabel(bundles("Max"));
            settingsPanel.add(label, gbh.defaults().nextCol().setLabelDefault().get());
            settingsPanel.add(maxField, gbh.defaults().nextCol().setMaxWeightX().get());
            if (col.getFormattedDataType().contentEquals("DOUBLE PRECISION")
                    || col.getFormattedDataType().contentEquals("FLOAT")
                    || col.getFormattedDataType().startsWith("DECIMAL")
                    || col.getFormattedDataType().startsWith("NUMERIC")) {
                label = new JLabel(bundles("CountDigitsAfterComma"));
                settingsPanel.add(label, gbh.defaults().nextRowFirstCol().setLabelDefault().get());
                settingsPanel.add(countSymbolsAfterComma, gbh.defaults().nextCol().spanX().get());
            }

        }
        if (col.getFormattedDataType().contentEquals("TIME")) {
            minTime = new EQTimePicker();
            minTime.setVisibleNullBox(false);
            minTime.setTime(LocalTime.MIN);
            maxTime = new EQTimePicker();
            maxTime.setVisibleNullBox(false);
            maxTime.setTime(LocalTime.MAX);
            JLabel label = new JLabel(bundles("Min"));
            settingsPanel.add(label, gbh.defaults().setLabelDefault().get());
            settingsPanel.add(minTime, gbh.defaults().nextCol().setMaxWeightX().get());
            label = new JLabel(bundles("Max"));
            settingsPanel.add(label, gbh.defaults().nextCol().setLabelDefault().get());
            settingsPanel.add(maxTime, gbh.defaults().nextCol().setMaxWeightX().get());
        }
        if (col.getFormattedDataType().contentEquals("DATE")) {
            minDate = new DatePicker();
            minDate.setDate(LocalDate.of(0, 1, 1));
            maxDate = new DatePicker();
            maxDate.setDate(LocalDate.of(9999, 1, 1));
            JLabel label = new JLabel(bundles("Min"));
            settingsPanel.add(label, gbh.defaults().setLabelDefault().get());
            settingsPanel.add(minDate, gbh.defaults().nextCol().setMaxWeightX().get());
            label = new JLabel(bundles("Max"));
            settingsPanel.add(label, gbh.defaults().nextCol().setLabelDefault().get());
            settingsPanel.add(maxDate, gbh.defaults().nextCol().setMaxWeightX().get());
        }
        if (col.getFormattedDataType().contentEquals("TIMESTAMP")) {
            minDateTime = new EQDateTimePicker();
            minDateTime.setVisibleNullBox(false);
            minDateTime.setDateTimePermissive(LocalDateTime.of(LocalDate.of(0, 1, 1), LocalTime.of(0, 0, 0)));
            maxDateTime = new EQDateTimePicker();
            maxDateTime.setVisibleNullBox(false);
            maxDateTime.setDateTimePermissive(LocalDateTime.of(LocalDate.of(9999, 12, 31), LocalTime.of(23, 59, 59)));
            JLabel label = new JLabel(bundles("Min"));
            settingsPanel.add(label, gbh.defaults().setLabelDefault().get());
            settingsPanel.add(minDateTime, gbh.defaults().nextCol().spanX().get());
            label = new JLabel(bundles("Max"));
            settingsPanel.add(label, gbh.defaults().nextRowFirstCol().setLabelDefault().get());
            settingsPanel.add(maxDateTime, gbh.defaults().nextCol().spanX().get());
        }
        if (col.getFormattedDataType().contains("CHAR")) {
            maxField = new NumberTextField(false);
            minField = new NumberTextField(false);
            maxField.setText("" + col.getColumnSize());
            minField.setText("0");
            useOnlyThisSymbolsBox = new JCheckBox(bundles("UseOnlyThisSymbols"));
            useOnlyThisSymbolsField = new SimpleTextArea();

            //scrollSymbols = new JScrollPane(useOnlyThisSymbolsField);
            //scrollSymbols.setVerticalScrollBar(scrollSymbols.createVerticalScrollBar());

            JLabel label = new JLabel(bundles("MinLength"));

            settingsPanel.add(label, gbh.defaults().setLabelDefault().get());
            settingsPanel.add(minField, gbh.defaults().nextCol().setMaxWeightX().get());
            label = new JLabel(bundles("MaxLength"));
            settingsPanel.add(label, gbh.defaults().nextCol().setLabelDefault().get());
            settingsPanel.add(maxField, gbh.defaults().nextCol().setMaxWeightX().get());
            settingsPanel.add(useOnlyThisSymbolsBox, gbh.defaults().nextRowFirstCol().spanX().get());
            settingsPanel.add(useOnlyThisSymbolsField, gbh.defaults().nextRowFirstCol().fillBoth().spanX().spanY().get());
        }
        if (col.getFormattedDataType().contains("BLOB")) {
            maxField = new NumberTextField(false);
            minField = new NumberTextField(false);
            maxField.setText("" + col.getColumnSize());
            minField.setText("0");
            maxByteField = new NumberTextField();
            minByteField = new NumberTextField();
            maxByteField.setText("255");
            minByteField.setText("0");


            JLabel label = new JLabel(bundles("MinLength"));

            settingsPanel.add(label, gbh.defaults().setLabelDefault().get());
            settingsPanel.add(minField, gbh.defaults().nextCol().setMaxWeightX().get());
            label = new JLabel(bundles("MaxLength"));
            settingsPanel.add(label, gbh.defaults().nextCol().setLabelDefault().get());
            settingsPanel.add(maxField, gbh.defaults().nextCol().setMaxWeightX().get());
            label = new JLabel(bundles("MinByte"));

            settingsPanel.add(label, gbh.defaults().nextRowFirstCol().setLabelDefault().get());
            settingsPanel.add(minByteField, gbh.defaults().nextCol().setMaxWeightX().get());
            label = new JLabel(bundles("MaxByte"));
            settingsPanel.add(label, gbh.defaults().nextCol().setLabelDefault().get());
            settingsPanel.add(maxByteField, gbh.defaults().nextCol().setMaxWeightX().get());


        }
        setLayout(new GridBagLayout());
        gbh.setXY(0, 0);
        add(nullBox, gbh.defaults().spanX().get());
        if (col.getFormattedDataType().contains("CHAR")) {
            add(settingsPanel, gbh.defaults().nextRowFirstCol().fillBoth().spanX().spanY().get());
        } else {
            add(settingsPanel, gbh.defaults().nextRowFirstCol().spanX().get());
            add(new JPanel(), gbh.defaults().nextRowFirstCol().spanX().spanY().get());
        }

    }

    public Object getTestDataObject() {
        if (nullBox.isSelected()) {
            if (new Random().nextInt(10) == 0)
                return null;
        }
        if (col.getFormattedDataType().contentEquals("BIGINT")) {
            BigInteger bigint = new BigInteger(62, new Random());
            BigInteger max = new BigInteger(maxField.getText());
            BigInteger min = new BigInteger(minField.getText());
            if (max.compareTo(min) == -1)
                throw new DataSourceException("minimum greater than maximum for column \"" + col.getName() + "\"");
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
            long value = new Random().nextLong();
            if (value < 0)
                value *= -1;
            long max = Timestamp.valueOf(maxTime.getTime()).getTime();
            long min = Timestamp.valueOf(minTime.getTime()).getTime();
            if (min > max)
                throw new DataSourceException("minimum greater than maximum for column \"" + col.getName() + "\"");
            long diapason = max - min;
            if (diapason == 0) {
                value = max;
            } else
                value = (min + (value % diapason));
            Timestamp v = new Timestamp(value);
            Time t = new Time(v.getTime());
            return t;
        }
        if (col.getFormattedDataType().contentEquals("DATE")) {
            long value = new Random().nextLong();
            if (value < 0)
                value *= -1;
            long max = maxDate.getDate().toEpochDay();
            long min = minDate.getDate().toEpochDay();
            if (min > max)
                throw new DataSourceException("minimum greater than maximum for column \"" + col.getName() + "\"");
            long diapason = max - min;
            if (diapason == 0) {
                value = max;
            } else
                value = (min + (value % diapason));
            LocalDate temp = LocalDate.of(1970, 1, 1);
            temp = temp.plusDays(value);
            Date date = Date.valueOf(temp);
            return date;
        }
        if (col.getFormattedDataType().contentEquals("TIMESTAMP")) {
            long value = new Random().nextLong();
            if (value < 0)
                value *= -1;
            long max = Timestamp.valueOf(maxDateTime.getStringValue()).getTime();
            long min = Timestamp.valueOf(minDateTime.getStringValue()).getTime();
            if (min > max)
                throw new DataSourceException("minimum greater than maximum for column \"" + col.getName() + "\"");
            long diapason = max - min;
            if (diapason == 0) {
                value = max;
            } else
                value = (min + (value % diapason));
            Timestamp v = new Timestamp(value);
            Time t = new Time(v.getTime());
            return t;
        }
        if (col.getFormattedDataType().contentEquals("INTEGER") || col.getFormattedDataType().contentEquals("SMALLINT")) {
            long value = new Random().nextLong();
            if (value < 0)
                value *= -1;
            long max = ((NumberTextField) maxField).getValue();
            long min = ((NumberTextField) minField).getValue();
            if (min > max)
                throw new DataSourceException("minimum greater than maximum for column \"" + col.getName() + "\"");
            long diapason = max - min;
            if (diapason == 0) {
                value = max;
            } else
                value = (min + (value % diapason));
            if (col.getFormattedDataType().contentEquals("SMALLINT"))
                return (short) value;
            return (int) value;
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
            if (min > max)
                throw new DataSourceException("minimum greater than maximum for column \"" + col.getName() + "\"");
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
            if (min > max)
                throw new DataSourceException("minimum greater than maximum for column \"" + col.getName() + "\"");
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
            if (min > max)
                throw new DataSourceException("minimum greater than maximum for column \"" + col.getName() + "\"");
            int diapason = max - min;
            if (diapason == 0) {
                n = max;
            } else
                n = (min + (n % diapason));
            max = ((NumberTextField) maxByteField).getValue() + 1;
            min = ((NumberTextField) minByteField).getValue();
            if (min > max)
                throw new DataSourceException("minimum greater than maximum for column \"" + col.getName() + "\"");
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
        if (col.getFormattedDataType().contains("BOOLEAN")) {
            return new Random().nextInt(2) == 1;
        }
        return null;
    }

}

package org.executequery.gui.browser.generatortestdata.methodspanels;

import com.github.lgooddatepicker.components.DatePicker;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.T;
import org.executequery.gui.text.SimpleTextArea;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.*;
import org.underworldlabs.swing.celleditor.picker.DefaultDateTimePicker;
import org.underworldlabs.swing.celleditor.picker.DefaultDateTimezonePicker;
import org.underworldlabs.swing.celleditor.picker.DefaultTimePicker;
import org.underworldlabs.swing.celleditor.picker.DefaultTimezonePicker;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;
import java.time.*;
import java.util.Random;

public class RandomMethodPanel extends AbstractMethodPanel {
    private JPanel settingsPanel;
    private JTextField maxField;
    private JTextField minField;
    private DefaultTimePicker minTime;
    private DefaultTimePicker maxTime;
    private DefaultTimezonePicker minTimezone;
    private DefaultTimezonePicker maxTimezone;
    private DefaultDateTimePicker minDateTime;
    private DefaultDateTimePicker maxDateTime;
    private DefaultDateTimezonePicker minDateTimezone;
    private DefaultDateTimezonePicker maxDateTimezone;
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

        if (col.getFormattedDataType().contentEquals(T.BIGINT)
                || col.getFormattedDataType().contentEquals(T.INT128)
                || col.getFormattedDataType().contentEquals(T.INTEGER)
                || col.getFormattedDataType().contentEquals(T.SMALLINT)
                || col.getFormattedDataType().contentEquals(T.DOUBLE_PRECISION)
                || col.getFormattedDataType().contentEquals(T.FLOAT)
                || col.getFormattedDataType().startsWith(T.DECIMAL)
                || col.getFormattedDataType().startsWith(T.NUMERIC)
                || col.getFormattedDataType().startsWith(T.DECFLOAT)
        ) {

            if (col.getFormattedDataType().contentEquals(T.BIGINT)
                    || col.getFormattedDataType().contentEquals(T.INT128)
                    || col.getFormattedDataType().contentEquals(T.DOUBLE_PRECISION)
                    || col.getFormattedDataType().contentEquals(T.FLOAT)
                    || col.getFormattedDataType().startsWith(T.DECIMAL)
                    || col.getFormattedDataType().startsWith(T.NUMERIC)
                    || col.getFormattedDataType().startsWith(T.DECFLOAT)
            ) {
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
                if (col.getFormattedDataType().contentEquals(T.INTEGER)) {
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
            if (col.getFormattedDataType().contentEquals(T.DOUBLE_PRECISION)
                    || col.getFormattedDataType().contentEquals(T.FLOAT)
                    || col.getFormattedDataType().startsWith(T.DECIMAL)
                    || col.getFormattedDataType().startsWith(T.NUMERIC)
                    || col.getFormattedDataType().startsWith(T.DECFLOAT)
            ) {
                label = new JLabel(bundles("CountDigitsAfterComma"));
                settingsPanel.add(label, gbh.defaults().nextRowFirstCol().setLabelDefault().get());
                settingsPanel.add(countSymbolsAfterComma, gbh.defaults().nextCol().spanX().get());
            }

        }

        if (col.getFormattedDataType().contentEquals(T.TIME)) {

            minTime = new DefaultTimePicker();
            minTime.setVisibleNullCheck(false);
            minTime.setTime(LocalTime.MIN);

            maxTime = new DefaultTimePicker();
            maxTime.setVisibleNullCheck(false);
            maxTime.setTime(LocalTime.MAX);

            settingsPanel.add(new JLabel(bundles("Min")), gbh.defaults().setLabelDefault().get());
            settingsPanel.add(minTime, gbh.defaults().nextCol().setMaxWeightX().get());
            settingsPanel.add(new JLabel(bundles("Max")), gbh.defaults().nextCol().setLabelDefault().get());
            settingsPanel.add(maxTime, gbh.defaults().nextCol().setMaxWeightX().get());
        }

        if (col.getFormattedDataType().contentEquals(T.TIME_WITH_TIMEZONE)) {

            minTimezone = new DefaultTimezonePicker();
            minTimezone.setVisibleNullCheck(false);
            minTimezone.setTime(LocalTime.MIN);

            maxTimezone = new DefaultTimezonePicker();
            maxTimezone.setVisibleNullCheck(false);
            maxTimezone.setTime(LocalTime.MAX);

            settingsPanel.add(new JLabel(bundles("Min")), gbh.defaults().setLabelDefault().get());
            settingsPanel.add(minTimezone, gbh.defaults().nextCol().setMaxWeightX().get());
            settingsPanel.add(new JLabel(bundles("Max")), gbh.defaults().nextCol().setLabelDefault().get());
            settingsPanel.add(maxTimezone, gbh.defaults().nextCol().setMaxWeightX().get());
        }

        if (col.getFormattedDataType().contentEquals(T.DATE)) {
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

        if (col.getFormattedDataType().contentEquals(T.TIMESTAMP)) {

            minDateTime = new DefaultDateTimePicker();
            minDateTime.setVisibleNullBox(false);
            minDateTime.setDateTime(LocalDateTime.of(LocalDate.of(0, 1, 1), LocalTime.of(0, 0, 0)));

            maxDateTime = new DefaultDateTimePicker();
            maxDateTime.setVisibleNullBox(false);
            maxDateTime.setDateTime(LocalDateTime.of(LocalDate.of(9999, 12, 31), LocalTime.of(23, 59, 59)));

            settingsPanel.add(new JLabel(bundles("Min")), gbh.defaults().setLabelDefault().get());
            settingsPanel.add(minDateTime, gbh.defaults().nextCol().spanX().get());
            settingsPanel.add(new JLabel(bundles("Max")), gbh.defaults().nextRowFirstCol().setLabelDefault().get());
            settingsPanel.add(maxDateTime, gbh.defaults().nextCol().spanX().get());
        }

        if (col.getFormattedDataType().contentEquals(T.TIMESTAMP_WITH_TIMEZONE)) {

            minDateTimezone = new DefaultDateTimezonePicker();
            minDateTimezone.setVisibleNullBox(false);
            minDateTimezone.setDateTime(LocalDateTime.of(LocalDate.of(0, 1, 1), LocalTime.of(0, 0, 0)));

            maxDateTimezone = new DefaultDateTimezonePicker();
            maxDateTimezone.setVisibleNullBox(false);
            maxDateTimezone.setDateTime(LocalDateTime.of(LocalDate.of(9999, 12, 31), LocalTime.of(23, 59, 59)));

            settingsPanel.add(new JLabel(bundles("Min")), gbh.defaults().setLabelDefault().get());
            settingsPanel.add(minDateTimezone, gbh.defaults().nextCol().spanX().get());
            settingsPanel.add(new JLabel(bundles("Max")), gbh.defaults().nextRowFirstCol().setLabelDefault().get());
            settingsPanel.add(maxDateTimezone, gbh.defaults().nextCol().spanX().get());
        }

        if (col.getFormattedDataType().contains(T.CHAR)) {
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
        if (col.getFormattedDataType().contains(T.BLOB)) {
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
        if (col.getFormattedDataType().contains(T.CHAR)) {
            add(settingsPanel, gbh.defaults().nextRowFirstCol().fillBoth().spanX().spanY().get());
        } else {
            add(settingsPanel, gbh.defaults().nextRowFirstCol().spanX().get());
            add(new JPanel(), gbh.defaults().nextRowFirstCol().spanX().spanY().get());
        }

    }

    private BigInteger getBigint() {
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


    public Object getTestDataObject() {
        if (nullBox.isSelected()) {
            if (new Random().nextInt(10) == 0)
                return null;
        }
        if (col.getFormattedDataType().contentEquals(T.BIGINT) || col.getFormattedDataType().contentEquals(T.INT128)) {
            return getBigint();
        }
        if (col.getFormattedDataType().contentEquals(T.TIME)) {
            long value = new Random().nextLong();
            if (value < 0)
                value *= -1;

            long max = maxTime.getLocalTime().atDate(LocalDate.of(1970, 1, 1))
                    .toInstant(ZoneId.of(ZoneId.systemDefault().getId())
                            .getRules().getOffset(Instant.now()))
                    .toEpochMilli();
            long min = minTime.getLocalTime().atDate(LocalDate.of(1970, 1, 1))
                    .toInstant(ZoneId.of(ZoneId.systemDefault().getId())
                            .getRules().getOffset(Instant.now()))
                    .toEpochMilli();

            if (min > max)
                throw new DataSourceException("minimum greater than maximum for column \"" + col.getName() + "\"");
            long diapason = max - min;
            if (diapason == 0) {
                value = max;
            } else
                value = (min + (value % diapason));
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault());
            return dateTime.toLocalTime();
        }

        if (col.getFormattedDataType().contentEquals(T.TIME_WITH_TIMEZONE)) {
            long value = new Random().nextLong();
            if (value < 0)
                value *= -1;

            long max = maxTimezone.getOffsetTime().atDate(LocalDate.of(1970, 1, 1)).toInstant().toEpochMilli();
            long min = minTimezone.getOffsetTime().atDate(LocalDate.of(1970, 1, 1)).toInstant().toEpochMilli();
            if (min > max)
                throw new DataSourceException("minimum greater than maximum for column \"" + col.getName() + "\"");
            long diapason = max - min;
            if (diapason == 0) {
                value = max;
            } else
                value = (min + (value % diapason));
            OffsetDateTime dateTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault());
            return dateTime.toOffsetTime();
        }

        if (col.getFormattedDataType().contentEquals(T.DATE)) {
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
            LocalDate temp = LocalDate.ofEpochDay(value);
            return temp;
        }
        if (col.getFormattedDataType().contentEquals(T.TIMESTAMP)) {
            long value = new Random().nextLong();
            if (value < 0)
                value *= -1;
            ZoneId z_id = ZoneId.systemDefault();
            ZoneOffset offset = z_id.getRules().getOffset(Instant.now());
            long max = maxDateTime.getDateTime().toInstant(offset).toEpochMilli();
            long min = minDateTime.getDateTime().toInstant(offset).toEpochMilli();
            if (min > max)
                throw new DataSourceException("minimum greater than maximum for column \"" + col.getName() + "\"");
            long diapason = max - min;
            if (diapason == 0) {
                value = max;
            } else
                value = (min + (value % diapason));
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault());
            return dateTime;
        }
        if (col.getFormattedDataType().contentEquals(T.TIMESTAMP_WITH_TIMEZONE)) {
            long value = new Random().nextLong();
            if (value < 0)
                value *= -1;
            long max = maxDateTimezone.getOffsetDateTime().toInstant().toEpochMilli();
            long min = minDateTimezone.getOffsetDateTime().toInstant().toEpochMilli();
            if (min > max)
                throw new DataSourceException("minimum greater than maximum for column \"" + col.getName() + "\"");
            long diapason = max - min;
            if (diapason == 0) {
                value = max;
            } else
                value = (min + (value % diapason));
            OffsetDateTime dateTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault());
            return dateTime;
        }
        if (col.getFormattedDataType().contentEquals(T.INTEGER)
                || col.getFormattedDataType().contentEquals(T.SMALLINT)) {
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
            if (col.getFormattedDataType().contentEquals(T.SMALLINT))
                return (short) value;
            return (int) value;
        }
        if (col.getFormattedDataType().contentEquals(T.DOUBLE_PRECISION)
                || col.getFormattedDataType().contentEquals(T.FLOAT)
                || col.getFormattedDataType().startsWith(T.DECIMAL)
                || col.getFormattedDataType().startsWith(T.NUMERIC)
                || col.getFormattedDataType().startsWith(T.DECFLOAT)
        ) {
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
        if (col.getFormattedDataType().contains(T.CHAR)) {
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
        if (col.getFormattedDataType().contains(T.BLOB)) {
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
        if (col.getFormattedDataType().contains(T.BOOLEAN)) {
            return new Random().nextInt(2) == 1;
        }
        return null;
    }

}

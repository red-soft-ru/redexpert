package org.executequery.gui.browser.generatortestdata.methodspanels;

import com.github.lgooddatepicker.components.DatePicker;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.T;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.text.SimpleTextArea;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.*;
import org.underworldlabs.swing.celleditor.picker.TimestampPicker;
import org.underworldlabs.swing.celleditor.picker.ZonedTimestampPicker;
import org.underworldlabs.swing.celleditor.picker.TimePicker;
import org.underworldlabs.swing.celleditor.picker.ZonedTimePicker;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;
import java.time.*;
import java.util.Random;

public class RandomMethodPanel extends AbstractMethodPanel {

    private JCheckBox useNullCheck;
    private JPanel settingsPanel;

    private NumberTextField maxField;
    private NumberTextField minField;
    private NumberTextField symbolsAfterComma;

    private DatePicker maxDate;
    private DatePicker minDate;

    private TimePicker minTime;
    private TimePicker maxTime;

    private ZonedTimePicker minTimezone;
    private ZonedTimePicker maxTimezone;

    private TimestampPicker minDateTime;
    private TimestampPicker maxDateTime;

    private ZonedTimestampPicker minDateTimezone;
    private ZonedTimestampPicker maxDateTimezone;

    private JCheckBox useSelectedCharsetCheck;
    private SimpleTextArea useSelectedCharsetField;

    private JTextField maxByteField;
    private JTextField minByteField;

    public RandomMethodPanel(DatabaseColumn col) {
        super(col);
        init();
    }

    private void init() {

        settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridBagLayout());
        useNullCheck = WidgetFactory.createCheckBox("useNullCheck", bundleString("UseNull"));

        // --- init settings panel ---

        String dataType = col.getFormattedDataType();
        if (isNumeric(dataType)) {
            initNumericPanel(dataType);

        } else if (isChar(dataType)) {
            initCharPanel();

        } else if (isDate(dataType)) {
            initDatePanel();

        } else if (isTime(dataType)) {
            initTimePanel();

        } else if (isTimestamp(dataType)) {
            initTimestampPanel();

        } else if (isZonedTime(dataType)) {
            initZonedTimePanel();

        } else if (isZonedTimestamp(dataType)) {
            initZonedTimestampPanel();

        } else if (isBlob(dataType))
            initBlobPanel();

        // --- base ---

        setLayout(new GridBagLayout());

        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        add(useNullCheck, gbh.spanX().get());
        add(settingsPanel, gbh.nextRowFirstCol().topGap(5).fillBoth().spanY().get());
    }

    private void initNumericPanel(String dataType) {

        maxField = WidgetFactory.createNumberTextField("maxField");
        minField = WidgetFactory.createNumberTextField("minField");

        if (dataType.contentEquals(T.INTEGER)) {
            maxField.setText("" + Integer.MAX_VALUE);
            minField.setText("" + Integer.MIN_VALUE);

        } else if (dataType.contentEquals(T.SMALLINT)) {
            maxField.setText("" + 32767);
            minField.setText("" + (-32768));

        } else if (dataType.contentEquals("BIGINT")) {
            maxField.setText("9223372036854775807");
            minField.setText("-9223372036854775808");

        } else {
            maxField.setText("1");
            minField.setText("0");
        }

        if (isDecimal(dataType)) {
            symbolsAfterComma = WidgetFactory.createNumberTextField("symbolsAfterComma");
            symbolsAfterComma.setText("1");
        }

        // --- arrange ---

        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        settingsPanel.add(new JLabel(bundleString("Min")), gbh.leftGap(3).topGap(3).get());
        settingsPanel.add(minField, gbh.nextCol().topGap(0).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JLabel(bundleString("Max")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        settingsPanel.add(maxField, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        if (isDecimal(dataType)) {
            settingsPanel.add(new JLabel(bundleString("CountDigitsAfterComma")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
            settingsPanel.add(symbolsAfterComma, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        }
        settingsPanel.add(new JPanel(), gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
    }

    private void initCharPanel() {

        maxField = WidgetFactory.createNumberTextField("maxField");
        maxField.setText("" + col.getColumnSize());
        maxField.setEnableNegativeNumbers(false);

        minField = WidgetFactory.createNumberTextField("minField");
        minField.setEnableNegativeNumbers(false);
        minField.setText("0");

        useSelectedCharsetField = new SimpleTextArea();
        useSelectedCharsetCheck = WidgetFactory.createCheckBox("useSelectedCharsetCheck", bundleString("UseOnlyThisSymbols"));

        // --- arrange ---

        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        settingsPanel.add(new JLabel(bundleString("MinLength")), gbh.leftGap(3).topGap(3).get());
        settingsPanel.add(minField, gbh.nextCol().topGap(0).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JLabel(bundleString("MaxLength")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        settingsPanel.add(maxField, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(useSelectedCharsetCheck, gbh.nextRowFirstCol().leftGap(0).spanX().get());
        settingsPanel.add(useSelectedCharsetField, gbh.nextRowFirstCol().setMaxWeightY().fillBoth().spanY().get());
    }

    private void initDatePanel() {

        minDate = new DatePicker();
        minDate.setDate(LocalDate.of(0, 1, 1));

        maxDate = new DatePicker();
        maxDate.setDate(LocalDate.of(9999, 1, 1));

        // --- arrange ---

        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        settingsPanel.add(new JLabel(bundleString("Min")), gbh.leftGap(3).topGap(3).get());
        settingsPanel.add(minDate, gbh.nextCol().topGap(0).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JLabel(bundleString("Max")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        settingsPanel.add(maxDate, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JPanel(), gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
    }

    private void initTimePanel() {

        minTime = new TimePicker();
        minTime.setTime(LocalTime.MIN);
        minTime.setVisibleNullCheck(false);

        maxTime = new TimePicker();
        maxTime.setTime(LocalTime.MAX);
        maxTime.setVisibleNullCheck(false);

        // --- arrange ---

        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        settingsPanel.add(new JLabel(bundleString("Min")), gbh.leftGap(3).topGap(3).get());
        settingsPanel.add(minTime, gbh.nextCol().topGap(0).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JLabel(bundleString("Max")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        settingsPanel.add(maxTime, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JPanel(), gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
    }

    private void initTimestampPanel() {

        LocalDateTime minValue = LocalDateTime.of(LocalDate.of(0, 1, 1), LocalTime.of(0, 0, 0));
        LocalDateTime maxValue = LocalDateTime.of(LocalDate.of(9999, 12, 31), LocalTime.of(23, 59, 59));

        minDateTime = new TimestampPicker();
        minDateTime.setDateTime(minValue);
        minDateTime.setVisibleNullCheck(false);

        maxDateTime = new TimestampPicker();
        maxDateTime.setDateTime(maxValue);
        maxDateTime.setVisibleNullCheck(false);

        // --- arrange ---

        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        settingsPanel.add(new JLabel(bundleString("Min")), gbh.leftGap(3).topGap(3).get());
        settingsPanel.add(minDateTime, gbh.nextCol().topGap(0).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JLabel(bundleString("Max")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        settingsPanel.add(maxDateTime, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JPanel(), gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
    }

    private void initZonedTimePanel() {

        minTimezone = new ZonedTimePicker();
        minTimezone.setTime(LocalTime.MIN);
        minTimezone.setVisibleNullCheck(false);

        maxTimezone = new ZonedTimePicker();
        maxTimezone.setTime(LocalTime.MAX);
        maxTimezone.setVisibleNullCheck(false);

        // --- arrange ---

        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        settingsPanel.add(new JLabel(bundleString("Min")), gbh.leftGap(3).topGap(3).get());
        settingsPanel.add(minTimezone, gbh.nextCol().topGap(0).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JLabel(bundleString("Max")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        settingsPanel.add(maxTimezone, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JPanel(), gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
    }

    private void initZonedTimestampPanel() {

        LocalDateTime minValue = LocalDateTime.of(LocalDate.of(0, 1, 1), LocalTime.of(0, 0, 0));
        LocalDateTime maxValue = LocalDateTime.of(LocalDate.of(9999, 12, 31), LocalTime.of(23, 59, 59));

        minDateTimezone = new ZonedTimestampPicker();
        minDateTimezone.setVisibleNullCheck(false);
        minDateTimezone.setDateTime(minValue);

        maxDateTimezone = new ZonedTimestampPicker();
        maxDateTimezone.setVisibleNullCheck(false);
        maxDateTimezone.setDateTime(maxValue);

        // --- arrange ---

        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        settingsPanel.add(new JLabel(bundleString("Min")), gbh.leftGap(3).topGap(3).get());
        settingsPanel.add(minDateTimezone, gbh.nextCol().topGap(0).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JLabel(bundleString("Max")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        settingsPanel.add(maxDateTimezone, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JPanel(), gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
    }

    private void initBlobPanel() {

        maxField = WidgetFactory.createNumberTextField("maxField");
        maxField.setText("" + col.getColumnSize());
        maxField.setEnableNegativeNumbers(false);

        minField = WidgetFactory.createNumberTextField("minField");
        minField.setEnableNegativeNumbers(false);
        minField.setText("0");

        maxByteField = WidgetFactory.createNumberTextField("maxByteField");
        maxByteField.setText("255");

        minByteField = WidgetFactory.createNumberTextField("minByteField");
        minByteField.setText("0");

        // --- arrange ---

        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        settingsPanel.add(new JLabel(bundleString("MinLength")), gbh.leftGap(3).topGap(3).get());
        settingsPanel.add(minField, gbh.nextCol().topGap(0).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JLabel(bundleString("MaxLength")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        settingsPanel.add(maxField, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JLabel(bundleString("MinByte")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        settingsPanel.add(minByteField, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JLabel(bundleString("MaxByte")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        settingsPanel.add(maxByteField, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JPanel(), gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
    }

    // ---

    private BigInteger getBigintValue() {
        BigInteger diapason;
        BigInteger zero = new BigInteger("0");

        BigInteger min = new BigInteger(minField.getText());
        BigInteger max = new BigInteger(maxField.getText());
        checkDiapason(max.compareTo(min) < 0);

        BigInteger value = new BigInteger(62, new Random());
        if (min.compareTo(zero) < 0 && max.compareTo(zero) > 0) {

            int x = new Random().nextInt() % 2;
            if (x == 0) {
                diapason = min.multiply(new BigInteger("-1"));
                value = value.mod(diapason).multiply(new BigInteger("-1"));
            } else {
                diapason = max;
                value = value.mod(diapason);
            }

        } else {
            diapason = max.subtract(min);
            value = min.add(value.mod(diapason));
        }

        return value;
    }

    private int getIntegerValue(String dataType) {

        long max = maxField.getValue();
        long min = minField.getValue();
        checkDiapason(min, max);

        long value = getRandomValue(max, min);
        return dataType.contentEquals(T.SMALLINT) ? (short) value : (int) value;
    }

    private Double getDecimalValue() {
        long power = (long) Math.pow(10, symbolsAfterComma.getLongValue());

        long max = Long.parseLong(maxField.getText()) * power;
        long min = Long.parseLong(minField.getText()) * power;
        checkDiapason(min, max);

        long value = getRandomValue(max, min);
        return ((double) value) / ((double) power);
    }

    private String getCharValue() {

        long max = maxField.getLongValue() + 1;
        long min = minField.getLongValue();
        checkDiapason(min, max);

        StringBuilder value = new StringBuilder();
        long valueLength = getRandomValue(max, min);
        if (useSelectedCharsetCheck.isSelected()) {

            String charset = useSelectedCharsetField.getTextAreaComponent().getText();
            int length = charset.length();

            for (int i = 0; i < valueLength; i++) {
                int x = new Random().nextInt(length);
                value.append(charset.charAt(x));
            }

        } else {
            for (int i = 0; i < valueLength; i++) {
                int x = new Random().nextInt(127);
                value.append((char) x);
            }
        }

        return value.toString();
    }

    private LocalDate getDateValue() {

        long max = maxDate.getDate().toEpochDay();
        long min = minDate.getDate().toEpochDay();
        checkDiapason(min, max);

        long value = getRandomValue(max, min);
        return LocalDate.ofEpochDay(value);
    }

    private LocalTime getTimeValue() {

        long max = maxTime.getLocalTime().atDate(LocalDate.of(1970, 1, 1))
                .toInstant(ZoneId.of(ZoneId.systemDefault().getId()).getRules().getOffset(Instant.now()))
                .toEpochMilli();

        long min = minTime.getLocalTime().atDate(LocalDate.of(1970, 1, 1))
                .toInstant(ZoneId.of(ZoneId.systemDefault().getId()).getRules().getOffset(Instant.now()))
                .toEpochMilli();

        checkDiapason(min, max);

        long value = getRandomValue(max, min);
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault());
        return dateTime.toLocalTime();
    }

    private LocalDateTime getTimestampValue() {
        ZoneOffset offset = ZoneId.systemDefault().getRules().getOffset(Instant.now());

        long max = maxDateTime.getDateTime().toInstant(offset).toEpochMilli();
        long min = minDateTime.getDateTime().toInstant(offset).toEpochMilli();
        checkDiapason(min, max);

        long value = getRandomValue(max, min);
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault());
    }

    private OffsetTime getZonedTimeValue() {

        long max = maxTimezone.getOffsetTime().atDate(LocalDate.of(1970, 1, 1)).toInstant().toEpochMilli();
        long min = minTimezone.getOffsetTime().atDate(LocalDate.of(1970, 1, 1)).toInstant().toEpochMilli();
        checkDiapason(min, max);

        long value = getRandomValue(max, min);
        OffsetDateTime dateTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault());
        return dateTime.toOffsetTime();
    }

    private OffsetDateTime getZonedTimestamp() {

        long max = maxDateTimezone.getOffsetDateTime().toInstant().toEpochMilli();
        long min = minDateTimezone.getOffsetDateTime().toInstant().toEpochMilli();
        checkDiapason(min, max);

        long value = getRandomValue(max, min);
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault());
    }

    private byte[] getBlobValue() {

        int maxLength = maxField.getValue() + 1;
        int minLength = minField.getValue();
        checkDiapason(minLength, maxLength);

        int max = ((NumberTextField) maxByteField).getValue() + 1;
        int min = ((NumberTextField) minByteField).getValue();
        checkDiapason(min, max);

        int valueLength = (int) getRandomValue(maxLength, minLength);
        byte[] bytes = new byte[valueLength];
        for (int i = 0; i < valueLength; i++)
            bytes[i] = (byte) getRandomValue(max, min);

        return bytes;
    }

    private static boolean getBooleanValue() {
        return new Random().nextInt(2) == 1;
    }

    // ---

    private static long getRandomValue(long max, long min) {

        long diapason = max - min;
        if (diapason == 0)
            return max;

        return min + Math.abs(new Random().nextLong()) % diapason;
    }

    private void checkDiapason(long min, long max) throws DataSourceException {
        checkDiapason(min > max);
    }

    private void checkDiapason(boolean isInvalid) throws DataSourceException {
        if (isInvalid)
            throw new DataSourceException("minimum greater than maximum for column \"" + col.getName() + "\"");
    }

    // --- AbstractMethodPanel impl ---

    @Override
    public Object getTestDataObject() {

        if (useNullCheck.isSelected() && new Random().nextInt(10) == 0)
            return null;

        String dataType = col.getFormattedDataType();
        if (isBigint(dataType)) {
            return getBigintValue();

        } else if (isSmallint(dataType) || isInteger(dataType)) {
            return getIntegerValue(dataType);

        } else if (isDecimal(dataType)) {
            return getDecimalValue();

        } else if (isChar(dataType)) {
            return getCharValue();

        } else if (isDate(dataType)) {
            return getDateValue();

        } else if (isTime(dataType)) {
            return getTimeValue();

        } else if (isTimestamp(dataType)) {
            return getTimestampValue();

        } else if (isZonedTime(dataType)) {
            return getZonedTimeValue();

        } else if (isZonedTimestamp(dataType)) {
            return getZonedTimestamp();

        } else if (isBlob(dataType)) {
            return getBlobValue();

        } else if (isBoolean(dataType))
            return getBooleanValue();

        return null;
    }

}

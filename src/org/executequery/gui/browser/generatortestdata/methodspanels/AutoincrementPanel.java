package org.executequery.gui.browser.generatortestdata.methodspanels;

import com.github.lgooddatepicker.components.DatePicker;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.gui.WidgetFactory;
import org.underworldlabs.swing.*;
import org.underworldlabs.swing.celleditor.picker.TimestampPicker;
import org.underworldlabs.swing.celleditor.picker.ZonedTimestampPicker;
import org.underworldlabs.swing.celleditor.picker.TimePicker;
import org.underworldlabs.swing.celleditor.picker.ZonedTimePicker;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.*;

public class AutoincrementPanel extends AbstractMethodPanel {

    private JPanel settingsPanel;
    private JTextField iterationField;
    private JTextField startValueField;
    private JComboBox<?> incrementsCombo;

    private DatePicker startDate;
    private TimePicker startTime;
    private TimestampPicker startTimestamp;
    private ZonedTimePicker startZonedTime;
    private ZonedTimestampPicker startZonedTimestamp;

    private NumberTextField iterationYears;
    private NumberTextField iterationMouths;
    private NumberTextField iterationDays;
    private TimePicker iterationTime;

    private long currentInteger;
    private double currentDouble;
    private BigInteger currentBigint;
    private LocalDateTime currentLocalDateTime;
    private OffsetDateTime currentOffsetDateTime;

    public AutoincrementPanel(DatabaseColumn col) {
        super(col);
        init();
    }

    private void init() {

        settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridBagLayout());
        incrementsCombo = WidgetFactory.createComboBox("incrementsCombo", new String[]{"+", "-"});

        // --- init settings panel ---

        String dataType = col.getFormattedDataType();
        if (isNumeric(dataType)) {
            initNumericPanel(dataType);

        } else if (isDate(dataType)) {
            initDatePanel();

        } else if (isTime(dataType)) {
            initTimePanel();

        } else if (isTimestamp(dataType)) {
            initTimestampPanel();

        } else if (isZonedTime(dataType)) {
            initZonedTimePanel();

        } else if (isZonedTimestamp(dataType))
            initZonedTimestampPanel();

        // --- base ---

        setLayout(new GridBagLayout());

        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        add(incrementsCombo, gbh.spanX().get());
        add(settingsPanel, gbh.nextRowFirstCol().topGap(5).fillBoth().spanY().get());
    }

    private void initNumericPanel(String dataType) {

        iterationField = WidgetFactory.createNumberTextField("iterationField");
        iterationField.setText("1");

        startValueField = WidgetFactory.createNumberTextField("startValueField");
        if (isInteger(dataType)) {
            startValueField.setText("" + Integer.MIN_VALUE);
        } else if (isSmallint(dataType)) {
            startValueField.setText("" + (-32768));
        } else if (isBigint(dataType)) {
            startValueField.setText("-9223372036854775808");
        } else
            startValueField.setText("0");

        // --- arrange ---

        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        settingsPanel.add(new JLabel(bundleString("StartValue")), gbh.leftGap(3).topGap(3).get());
        settingsPanel.add(startValueField, gbh.nextCol().topGap(0).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JLabel(bundleString("Iteration")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        settingsPanel.add(iterationField, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JPanel(), gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
    }

    private void initDatePanel() {

        startDate = new DatePicker();
        startDate.setDate(LocalDate.now());

        iterationYears = WidgetFactory.createNumberTextField("yearsField");
        iterationYears.setValue(0);

        iterationMouths = WidgetFactory.createNumberTextField("mouthsField");
        iterationMouths.setValue(0);

        iterationDays = WidgetFactory.createNumberTextField("daysField");
        iterationDays.setValue(1);

        // --- arrange ---

        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        settingsPanel.add(new JLabel(bundleString("StartValue")), gbh.leftGap(3).topGap(3).get());
        settingsPanel.add(startDate, gbh.nextCol().topGap(0).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JLabel(bundleString("Years")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        settingsPanel.add(iterationYears, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JLabel(bundleString("Mouths")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        settingsPanel.add(iterationMouths, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JLabel(bundleString("Days")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        settingsPanel.add(iterationDays, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JPanel(), gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
    }

    private void initTimePanel() {

        startTime = new TimePicker();
        startTime.setVisibleNullCheck(false);
        startTime.setTime(LocalTime.MIN);

        iterationTime = new TimePicker();
        iterationTime.setVisibleNullCheck(false);
        iterationTime.setTime(LocalTime.of(1, 0, 0));

        // --- arrange ---

        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        settingsPanel.add(new JLabel(bundleString("StartValue")), gbh.leftGap(3).topGap(3).get());
        settingsPanel.add(startTime, gbh.nextCol().topGap(0).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JLabel(bundleString("Iteration")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        settingsPanel.add(iterationTime, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JPanel(), gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
    }

    private void initTimestampPanel() {

        startTimestamp = new TimestampPicker();
        startTimestamp.setVisibleNullCheck(false);
        startTimestamp.setDateTime(LocalDateTime.now());

        iterationTime = new TimePicker();
        iterationTime.setVisibleNullCheck(false);
        iterationTime.setTime(LocalTime.of(0, 0, 0));

        iterationYears = WidgetFactory.createNumberTextField("yearsField");
        iterationYears.setValue(0);

        iterationMouths = WidgetFactory.createNumberTextField("mouthsField");
        iterationMouths.setValue(0);

        iterationDays = WidgetFactory.createNumberTextField("daysField");
        iterationDays.setValue(1);

        // --- arrange ---

        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        settingsPanel.add(new JLabel(bundleString("StartValue")), gbh.leftGap(3).topGap(3).get());
        settingsPanel.add(startTimestamp, gbh.nextCol().topGap(0).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JLabel(bundleString("Years")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        settingsPanel.add(iterationYears, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JLabel(bundleString("Mouths")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        settingsPanel.add(iterationMouths, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JLabel(bundleString("Days")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        settingsPanel.add(iterationDays, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JLabel(bundleString("Time")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        settingsPanel.add(iterationTime, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JPanel(), gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
    }

    private void initZonedTimePanel() {

        startZonedTime = new ZonedTimePicker();
        startZonedTime.setVisibleNullCheck(false);
        startZonedTime.setTime(LocalTime.MIN);

        iterationTime = new TimePicker();
        iterationTime.setVisibleNullCheck(false);
        iterationTime.setTime(LocalTime.of(1, 0, 0));

        // --- arrange ---

        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        settingsPanel.add(new JLabel(bundleString("StartValue")), gbh.leftGap(3).topGap(3).get());
        settingsPanel.add(startZonedTime, gbh.nextCol().topGap(0).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JLabel(bundleString("Iteration")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        settingsPanel.add(iterationTime, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JPanel(), gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
    }

    private void initZonedTimestampPanel() {

        startZonedTimestamp = new ZonedTimestampPicker();
        startZonedTimestamp.setVisibleNullCheck(false);
        startZonedTimestamp.setDateTime(LocalDateTime.now());

        iterationTime = new TimePicker();
        iterationTime.setVisibleNullCheck(false);
        iterationTime.setTime(LocalTime.of(0, 0, 0));

        iterationYears = WidgetFactory.createNumberTextField("yearsField");
        iterationYears.setValue(0);

        iterationMouths = WidgetFactory.createNumberTextField("mouthsField");
        iterationMouths.setValue(0);

        iterationDays = WidgetFactory.createNumberTextField("daysField");
        iterationDays.setValue(1);

        // --- arrange ---

        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        settingsPanel.add(new JLabel(bundleString("StartValue")), gbh.leftGap(3).topGap(3).get());
        settingsPanel.add(startZonedTimestamp, gbh.nextCol().topGap(0).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JLabel(bundleString("Years")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        settingsPanel.add(iterationYears, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JLabel(bundleString("Mouths")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        settingsPanel.add(iterationMouths, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JLabel(bundleString("Days")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        settingsPanel.add(iterationDays, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JLabel(bundleString("Time")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        settingsPanel.add(iterationTime, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        settingsPanel.add(new JPanel(), gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
    }

    // ---

    private BigInteger getBigintValue(boolean increment) {

        if (first) {
            first = false;
            currentBigint = new BigInteger(startValueField.getText());
            return currentBigint;
        }

        BigInteger iterationBig = new BigInteger(iterationField.getText());
        currentBigint = increment ? currentBigint.add(iterationBig) : currentBigint.subtract(iterationBig);
        return currentBigint;
    }

    private int getIntegerValue(String dataType, boolean increment) {

        if (first) {
            first = false;
            currentInteger = Long.parseLong(startValueField.getText());
            return isSmallint(dataType) ? (short) currentInteger : (int) currentInteger;
        }

        currentInteger += Long.parseLong(iterationField.getText()) * (increment ? 1 : -1);
        return isSmallint(dataType) ? (short) currentInteger : (int) currentInteger;
    }

    private double getDecimalValue(boolean increment) {

        if (first) {
            first = false;
            currentDouble = Double.parseDouble(startValueField.getText());
            return currentDouble;
        }

        currentDouble += Double.parseDouble(iterationField.getText()) * (increment ? 1 : -1);
        return currentDouble;
    }

    private LocalDate getDateValue(boolean increment) {

        if (first) {
            first = false;
            currentLocalDateTime = LocalDateTime.of(startDate.getDate(), LocalTime.of(0, 0, 0));
            return currentLocalDateTime.toLocalDate();
        }

        if (increment) {
            currentLocalDateTime = currentLocalDateTime.plusYears(iterationYears.getValue());
            currentLocalDateTime = currentLocalDateTime.plusMonths(iterationMouths.getValue());
            currentLocalDateTime = currentLocalDateTime.plusDays(iterationDays.getValue());
        } else {
            currentLocalDateTime = currentLocalDateTime.minusYears(iterationYears.getValue());
            currentLocalDateTime = currentLocalDateTime.minusMonths(iterationMouths.getValue());
            currentLocalDateTime = currentLocalDateTime.minusDays(iterationDays.getValue());
        }

        return new Date(Timestamp.valueOf(currentLocalDateTime).getTime()).toLocalDate();
    }

    private LocalTime getTimeValue(boolean increment) {

        if (first) {
            first = false;
            currentLocalDateTime = startTime.getLocalTime().atDate(LocalDate.of(1970, 1, 1));
            return currentLocalDateTime.toLocalTime();
        }

        LocalTime iteration = iterationTime.getLocalTime();
        if (increment) {
            currentLocalDateTime = currentLocalDateTime.plusHours(iteration.getHour());
            currentLocalDateTime = currentLocalDateTime.plusMinutes(iteration.getMinute());
            currentLocalDateTime = currentLocalDateTime.plusSeconds(iteration.getSecond());
            currentLocalDateTime = currentLocalDateTime.plusNanos(iteration.getNano());
        } else {
            currentLocalDateTime = currentLocalDateTime.minusHours(iteration.getHour());
            currentLocalDateTime = currentLocalDateTime.minusMinutes(iteration.getMinute());
            currentLocalDateTime = currentLocalDateTime.minusSeconds(iteration.getSecond());
            currentLocalDateTime = currentLocalDateTime.minusNanos(iteration.getNano());
        }

        return currentLocalDateTime.toLocalTime();
    }

    private LocalDateTime getTimestampValue(boolean increment) {

        if (first) {
            first = false;
            currentLocalDateTime = startTimestamp.getDateTime();
            return currentLocalDateTime;
        }

        LocalTime iteration = iterationTime.getLocalTime();
        if (increment) {
            currentLocalDateTime = currentLocalDateTime.plusYears(iterationYears.getValue());
            currentLocalDateTime = currentLocalDateTime.plusMonths(iterationMouths.getValue());
            currentLocalDateTime = currentLocalDateTime.plusDays(iterationDays.getValue());
            currentLocalDateTime = currentLocalDateTime.plusHours(iteration.getHour());
            currentLocalDateTime = currentLocalDateTime.plusMinutes(iteration.getMinute());
            currentLocalDateTime = currentLocalDateTime.plusSeconds(iteration.getSecond());
            currentLocalDateTime = currentLocalDateTime.plusNanos(iteration.getNano());
        } else {
            currentLocalDateTime = currentLocalDateTime.minusYears(iterationYears.getValue());
            currentLocalDateTime = currentLocalDateTime.minusMonths(iterationMouths.getValue());
            currentLocalDateTime = currentLocalDateTime.minusDays(iterationDays.getValue());
            currentLocalDateTime = currentLocalDateTime.minusHours(iteration.getHour());
            currentLocalDateTime = currentLocalDateTime.minusMinutes(iteration.getMinute());
            currentLocalDateTime = currentLocalDateTime.minusSeconds(iteration.getSecond());
            currentLocalDateTime = currentLocalDateTime.minusNanos(iteration.getNano());
        }

        return currentLocalDateTime;
    }

    private OffsetTime getZonedTimeValue(boolean increment) {

        if (first) {
            first = false;
            currentOffsetDateTime = startZonedTime.getOffsetTime().atDate(LocalDate.of(1970, 1, 1));
            return currentOffsetDateTime.toOffsetTime();
        }

        LocalTime iteration = iterationTime.getLocalTime();
        if (increment) {
            currentOffsetDateTime = currentOffsetDateTime.plusHours(iteration.getHour());
            currentOffsetDateTime = currentOffsetDateTime.plusMinutes(iteration.getMinute());
            currentOffsetDateTime = currentOffsetDateTime.plusSeconds(iteration.getSecond());
            currentOffsetDateTime = currentOffsetDateTime.plusNanos(iteration.getNano());
        } else {
            currentOffsetDateTime = currentOffsetDateTime.minusHours(iteration.getHour());
            currentOffsetDateTime = currentOffsetDateTime.minusMinutes(iteration.getMinute());
            currentOffsetDateTime = currentOffsetDateTime.minusSeconds(iteration.getSecond());
            currentOffsetDateTime = currentOffsetDateTime.minusNanos(iteration.getNano());
        }

        return currentOffsetDateTime.toOffsetTime();
    }

    private OffsetDateTime getZonedTimestampValue(boolean increment) {

        if (first) {
            first = false;
            currentOffsetDateTime = startZonedTimestamp.getOffsetDateTime();
            return currentOffsetDateTime;
        }

        LocalTime iteration = iterationTime.getLocalTime();
        if (increment) {
            currentLocalDateTime = currentLocalDateTime.plusYears(iterationYears.getValue());
            currentLocalDateTime = currentLocalDateTime.plusMonths(iterationMouths.getValue());
            currentLocalDateTime = currentLocalDateTime.plusDays(iterationDays.getValue());
            currentLocalDateTime = currentLocalDateTime.plusHours(iteration.getHour());
            currentLocalDateTime = currentLocalDateTime.plusMinutes(iteration.getMinute());
            currentLocalDateTime = currentLocalDateTime.plusSeconds(iteration.getSecond());
            currentLocalDateTime = currentLocalDateTime.plusNanos(iteration.getNano());
        } else {
            currentLocalDateTime = currentLocalDateTime.minusYears(iterationYears.getValue());
            currentLocalDateTime = currentLocalDateTime.minusMonths(iterationMouths.getValue());
            currentLocalDateTime = currentLocalDateTime.minusDays(iterationDays.getValue());
            currentLocalDateTime = currentLocalDateTime.minusHours(iteration.getHour());
            currentLocalDateTime = currentLocalDateTime.minusMinutes(iteration.getMinute());
            currentLocalDateTime = currentLocalDateTime.minusSeconds(iteration.getSecond());
            currentLocalDateTime = currentLocalDateTime.minusNanos(iteration.getNano());
        }

        return currentOffsetDateTime;
    }

    // --- AbstractMethodPanel impl ---

    @Override
    public Object getTestDataObject() {

        String dataType = col.getFormattedDataType();
        boolean increment = incrementsCombo.getSelectedIndex() == 0;

        if (isBigint(dataType)) {
            return getBigintValue(increment);

        } else if (isSmallint(dataType) || isInteger(dataType)) {
            return getIntegerValue(dataType, increment);

        } else if (isDecimal(dataType) || isDecFloat(dataType)) {
            return getDecimalValue(increment);

        } else if (isDate(dataType)) {
            return getDateValue(increment);

        } else if (isTime(dataType)) {
            return getTimeValue(increment);

        } else if (isTimestamp(dataType)) {
            return getTimestampValue(increment);

        } else if (isZonedTime(dataType)) {
            return getZonedTimeValue(increment);

        } else if (isZonedTimestamp(dataType))
            return getZonedTimestampValue(increment);

        return null;
    }

}

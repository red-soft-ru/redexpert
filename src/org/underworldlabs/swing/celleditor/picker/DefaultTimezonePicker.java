package org.underworldlabs.swing.celleditor.picker;

import org.executequery.gui.WidgetFactory;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DefaultTimezonePicker extends JPanel {

    private JSpinner timeSpinner;
    private JCheckBox isNullCheck;
    private JSpinner timezoneSpinner;
    private JComboBox<?> plusMinusCombox;

    public DefaultTimezonePicker() {
        init();
        arrange();
        setUpdateNull();
        setCurrentTime();
    }

    void init() {

        timeSpinner = WidgetFactory.createSpinner("timeSpinner", new SpinnerDateModel());
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm:ss.SSS"));

        timezoneSpinner = WidgetFactory.createSpinner("timezoneSpinner", new SpinnerDateModel());
        timezoneSpinner.setEditor(new JSpinner.DateEditor(timezoneSpinner, "HH:mm"));

        plusMinusCombox = WidgetFactory.createComboBox("plusMinusCombox", new String[]{"+", "-"});
        plusMinusCombox.setPreferredSize(new Dimension(plusMinusCombox.getPreferredSize().width + 5, plusMinusCombox.getPreferredSize().height));

        isNullCheck = WidgetFactory.createCheckBox("isNullCheck", "NULL");
        isNullCheck.addActionListener(e -> setUpdateNull());
    }

    private void arrange() {
        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillBoth().spanY();

        setLayout(new GridBagLayout());
        add(timeSpinner, gbh.setMaxWeightX().get());
        add(plusMinusCombox, gbh.setMinWeightX().nextCol().get());
        add(timezoneSpinner, gbh.setMaxWeightX().nextCol().get());
        add(isNullCheck, gbh.anchorNorthEast().setMinWeightX().nextCol().get());

        setPreferredSize(new Dimension(300, timezoneSpinner.getPreferredSize().height));
    }

    public String getStringValue() {

        if (isNull())
            return "";

        Instant instant = Instant.ofEpochMilli(((Date) (timeSpinner).getValue()).getTime());
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());
        String time = localDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));

        instant = Instant.ofEpochMilli(((Date) (timezoneSpinner).getValue()).getTime());
        localDateTime = LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());
        time += plusMinusCombox.getSelectedItem() + localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"));

        return time;
    }

    public void setTime(LocalTime time) {

        if (time == null) {
            setNull(true);
            return;
        }

        Instant instant = time
                .atDate(LocalDate.of(1970, 1, 1))
                .atZone(ZoneId.of(ZoneId.systemDefault().getId()))
                .toInstant();

        setNull(false);
        timeSpinner.setValue(Date.from(instant));
    }

    public void setTime(OffsetTime time) {

        if (time == null) {
            setNull(true);
            return;
        }

        Instant instant = LocalTime
                .of(time.getHour(), time.getMinute(), time.getSecond(), time.getNano())
                .atDate(LocalDate.of(1970, 1, 1))
                .atZone(ZoneId.of(ZoneId.systemDefault().getId()))
                .toInstant();

        setNull(false);
        timeSpinner.setValue(Date.from(instant));
        setZoneOffset(time.getOffset());
    }

    public void setZoneOffset(ZoneOffset offset) {

        Instant instant = LocalTime
                .ofSecondOfDay(Math.abs(offset.getTotalSeconds()))
                .atDate(LocalDate.of(1970, 1, 1))
                .atZone(ZoneId.of(ZoneId.systemDefault().getId()))
                .toInstant();

        plusMinusCombox.setSelectedIndex(offset.getTotalSeconds() < 0 ? 1 : 0);
        timezoneSpinner.setValue(Date.from(instant));
    }

    private void setUpdateNull() {
        timeSpinner.setEnabled(!isNull());
        timezoneSpinner.setEnabled(!isNull());
        plusMinusCombox.setEnabled(!isNull());
    }

    public void setNull(boolean isNull) {
        isNullCheck.setSelected(isNull);
        setUpdateNull();
    }

    public void setVisibleNullCheck(boolean visible) {
        isNullCheck.setVisible(visible);
    }

    public boolean isNull() {
        return isNullCheck.isSelected();
    }

    public void setCurrentTime() {
        setTime(OffsetTime.now());
    }

    public OffsetTime getOffsetTime() {
        return OffsetTime.parse(getStringValue());
    }

    public LocalTime getLocalTime() {
        Instant instant = Instant.ofEpochMilli(((Date) (timeSpinner).getValue()).getTime());
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime();
    }

    public void addNullCheckActionListener(ActionListener l) {
        isNullCheck.addActionListener(l);
    }

}

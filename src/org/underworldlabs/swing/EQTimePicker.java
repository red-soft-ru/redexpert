package org.underworldlabs.swing;


import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class EQTimePicker extends JPanel {
    JSpinner timeSpinner;
    JSpinner.DateEditor timeEditor;
    JCheckBox nullBox;
    JComboBox plusminusCombox;
    JSpinner timezoneSpinner;
    JSpinner.DateEditor timezoneEditor;


    public EQTimePicker() {
        init();
    }

    public EQTimePicker(boolean enabled) {
        init();
        nullBox.setSelected(!enabled);
        setEnable(enabled);
    }

    void init() {
        timeSpinner = new JSpinner(new SpinnerDateModel());
        timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm:ss.SSS");
        timeSpinner.setEditor(timeEditor);
        timezoneSpinner = new JSpinner(new SpinnerDateModel());
        timezoneEditor = new JSpinner.DateEditor(timezoneSpinner, "HH:mm");
        timezoneSpinner.setEditor(timezoneEditor);
        timezoneSpinner.setVisible(false);
        plusminusCombox = new JComboBox(new String[]{"+", "-"});
        plusminusCombox.setVisible(false);
        nullBox = new JCheckBox("NULL");
        nullBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                timeSpinner.setEnabled(!nullBox.isSelected());
            }
        });
        this.setLayout(new GridBagLayout());
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaults(new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        gbh.defaults();
        add(timeSpinner, gbh.setMaxWeightX().get());
        add(plusminusCombox, gbh.setMinWeightX().nextCol().get());
        add(timezoneSpinner, gbh.setMaxWeightX().nextCol().get());
        add(nullBox, gbh.setMinWeightX().nextCol().get());
        clear();
    }

    public String getStringValue() {
        if (nullBox.isSelected())
            return "";
        Instant instant = Instant.ofEpochMilli(((Date) (timeSpinner).getValue()).getTime());
        LocalDateTime temp = LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());
        String time = temp.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));//temp.getHour() + ":" + temp.getMinute() + ":" + temp.getSecond() + "." + temp.getNano();
        if (timezoneSpinner.isVisible()) {
            instant = Instant.ofEpochMilli(((Date) (timezoneSpinner).getValue()).getTime());
            temp = LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());
            time += plusminusCombox.getSelectedItem() + temp.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        return time;
    }

    public OffsetTime getOffsetTime() {
        return OffsetTime.parse(getStringValue());
    }

    public LocalTime getLocalTime() {
        Instant instant = Instant.ofEpochMilli(((Date) (timeSpinner).getValue()).getTime());
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime();
    }


    /*public OffsetDateTime getTime() {
        Instant instant = Instant.ofEpochMilli(((Date) (timeSpinner).getValue()).getTime());
        return OffsetDateTime.ofInstant(instant, ZoneOffset.of(getTimeZone()));
    }*/

    public String getTimeZone() {
        if (!timezoneSpinner.isVisible())
            return ZoneId.systemDefault().getId();
        Instant instant = Instant.ofEpochMilli(((Date) (timezoneSpinner).getValue()).getTime());
        LocalDateTime temp = LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());
        return "" + plusminusCombox.getSelectedItem() + temp.getHour() + ":" + temp.getMinute();
    }

    public void setTime(LocalTime time) {
        if (time != null) {
            Instant instant = time.atDate(LocalDate.of(1970, 1, 1)).
                    atZone(ZoneId.of(ZoneId.systemDefault().getId())).toInstant();
            Date date = Date.from(instant);
            timeSpinner.setValue(date);
        } else {
            nullBox.setSelected(true);
        }
    }

    public void setTime(OffsetTime time) {
        if (time != null) {
            nullBox.setSelected(false);
            LocalTime localTime = LocalTime.of(time.getHour(), time.getMinute(), time.getSecond(), time.getNano());
            Instant instant = localTime.atDate(LocalDate.of(1970, 1, 1)).
                    atZone(ZoneId.of(ZoneId.systemDefault().getId())).toInstant();
            Date date = Date.from(instant);
            timeSpinner.setValue(date);
            ZoneOffset offset = time.getOffset();
            setZoneOffset(offset);
        } else nullBox.setSelected(true);
    }

    public void setZoneOffset(ZoneOffset offset) {
        plusminusCombox.setSelectedIndex(offset.getTotalSeconds() < 0 ? 1 : 0);
        Instant instant = LocalTime.ofSecondOfDay(Math.abs(offset.getTotalSeconds())).atDate(LocalDate.of(1970, 1, 1)).
                atZone(ZoneId.of(ZoneId.systemDefault().getId())).toInstant();
        Date date = Date.from(instant);
        timezoneSpinner.setValue(date);
    }

    public void setEnable(boolean enable) {
        setEnabled(enable);
        nullBox.setSelected(!enable);
        nullBox.setEnabled(enable);
        timeSpinner.setEnabled(enable);
        timezoneSpinner.setEnabled(enable);
        plusminusCombox.setEnabled(enable);
    }

    public void setVisibleNullBox(boolean flag) {
        nullBox.setVisible(flag);
    }

    public void setVisibleTimeZone(boolean flag) {
        timezoneSpinner.setVisible(flag);
        plusminusCombox.setVisible(flag);
    }

    public void clear() {
        setTime(OffsetTime.now());
    }
}
package org.underworldlabs.swing;

import com.github.lgooddatepicker.optionalusertools.DateChangeListener;
import com.github.lgooddatepicker.zinternaltools.DateChangeEvent;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

public class EQDateTimePicker extends JPanel {
    public EQDatePicker datePicker;
    public EQTimePicker timePicker;

    public EQDateTimePicker() {
        datePicker = new EQDatePicker();
        timePicker = new EQTimePicker(false);
        init();
    }

    void init() {
        datePicker.addDateChangeListener(new DateChangeListener() {
            @Override
            public void dateChanged(DateChangeEvent dateChangeEvent) {
                timePicker.setEnable(!datePicker.getDateStringOrEmptyString().equals(""));

            }
        });
        setLayout(new GridBagLayout());
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaults(new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        gbh.defaults();
        add(datePicker, gbh.setMaxWeightX().get());
        add(timePicker, gbh.nextCol().get());
    }

    public String getStringValue() {
        String date = datePicker.getDateStringOrEmptyString();
        String time = timePicker.getStringValue().trim();
        if (time.equals("") && !date.equals(""))
            time = "0:00:00";
        return (date + " " + time).trim();
    }

    public OffsetDateTime getOffsetDateTime() {
        return OffsetDateTime.of(getDateTime(), timePicker.getOffsetTime().getOffset());
    }

    public void setDateTimePermissive(LocalDateTime time) {
        if (time != null) {
            datePicker.setDate(time.toLocalDate());
            timePicker.setTime(time.toLocalTime());
        } else {
            datePicker.setDate(null);
            timePicker.setTime((OffsetTime) null);
        }
    }

    public void setDateTimePermissive(OffsetDateTime time) {
        if (time != null) {
            datePicker.setDate(time.toLocalDate());
            timePicker.setTime(time.toOffsetTime());
        } else {
            datePicker.setDate(null);
            timePicker.setTime((OffsetTime) null);
        }
    }

    public LocalDateTime getDateTime() {
        return LocalDateTime.of(datePicker.getDate(), timePicker.getLocalTime());
    }

    public void clear() {
        datePicker.clear();
        timePicker.clear();
        timePicker.setEnable(false);
    }

    public void setVisibleNullBox(boolean flag) {
        timePicker.setVisibleNullBox(flag);
    }

    public void setVisibleTimeZone(boolean flag) {
        timePicker.setVisibleTimeZone(flag);
    }

    public boolean isNull()
    {
        return datePicker.getDate() == null;
    }
}

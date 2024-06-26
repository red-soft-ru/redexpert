package org.underworldlabs.swing.celleditor.picker;

import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public class ZonedTimestampPicker extends JPanel {

    private DatePicker datePicker;
    private ZonedTimePicker timezonePicker;

    public ZonedTimestampPicker() {
        init();
        arrange();
        update();
    }

    private void init() {
        datePicker = new DatePicker();

        timezonePicker = new ZonedTimePicker();
        timezonePicker.addNullCheckActionListener(e -> update());
    }

    private void arrange() {
        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillBoth().spanY();

        setLayout(new GridBagLayout());
        add(datePicker, gbh.setWeightX(0.4).get());
        add(timezonePicker, gbh.nextCol().setWeightX(0.6).spanX().get());

        setPreferredSize(new Dimension(480, timezonePicker.getPreferredSize().height));
    }

    public String getStringValue() {

        if (isNull())
            return null;

        String date = datePicker.getDateStringOrEmptyString().trim();
        String time = timezonePicker.getStringValue().trim();

        if (time.isEmpty() && !date.isEmpty())
            time = "0:00:00";

        return (date + " " + time).trim();
    }

    public void setDateTime(LocalDateTime dateTime) {

        if (dateTime == null) {
            setNull(true);
            return;
        }

        setNull(false);
        datePicker.setDate(dateTime.toLocalDate());
        timezonePicker.setTime(dateTime.toLocalTime());
    }

    public void setDateTime(OffsetDateTime dateTime) {

        if (dateTime == null) {
            setNull(true);
            return;
        }

        setNull(false);
        datePicker.setDate(dateTime.toLocalDate());
        timezonePicker.setTime(dateTime.toOffsetTime());
    }

    public LocalDateTime getDateTime() {

        if (isNull())
            return null;

        LocalDate date = datePicker.getDate();
        if (date == null) {
            date = LocalDate.now();
            datePicker.setDate(date);
        }

        LocalTime time = timezonePicker.getLocalTime();
        if (time == null) {
            time = LocalTime.now();
            timezonePicker.setTime(time);
        }

        return LocalDateTime.of(date, time);
    }

    public OffsetDateTime getOffsetDateTime() {
        return isNull() ? null : OffsetDateTime.of(getDateTime(), timezonePicker.getOffsetTime().getOffset());
    }

    private void update() {
        setDateTime(isNull() ? null : OffsetDateTime.now());
        setEnabled(!isNull());
    }

    public void setNull(boolean isNull) {
        timezonePicker.setNull(isNull);
        setEnabled(!isNull());
    }

    @Override
    public void setEnabled(boolean enabled) {
        datePicker.setEnabled(enabled);
        timezonePicker.setEnabled(enabled);
    }

    public void setVisibleNullCheck(boolean flag) {
        timezonePicker.setVisibleNullCheck(flag);
    }

    public boolean isNull() {
        return timezonePicker.isNull();
    }

    public DatePicker getDatePicker() {
        return datePicker;
    }

    public ZonedTimePicker getTimezonePicker() {
        return timezonePicker;
    }

}

package org.underworldlabs.swing.celleditor.picker;

import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class DefaultDateTimezonePicker extends JPanel {

    private DefaultDatePicker datePicker;
    private DefaultTimezonePicker timezonePicker;

    public DefaultDateTimezonePicker() {
        init();
        arrange();
    }

    private void init() {
        datePicker = new DefaultDatePicker();
        timezonePicker = new DefaultTimezonePicker();

        datePicker.addDateChangeListener(e -> setCurrentDate());
        timezonePicker.addNullCheckActionListener(e -> setCurrentDate());
        timezonePicker.addNullCheckActionListener(e -> datePicker.setEnabled(!timezonePicker.isNull()));
    }

    private void arrange() {
        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillBoth().spanY();

        setLayout(new GridBagLayout());
        add(datePicker, gbh.get());
        add(timezonePicker, gbh.nextCol().spanX().get());

        setPreferredSize(new Dimension(450, timezonePicker.getPreferredSize().height));
    }

    public String getStringValue() {

        if (timezonePicker.isNull())
            return "";

        String date = datePicker.getDateStringOrEmptyString();
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
        return LocalDateTime.of(datePicker.getDate(), timezonePicker.getLocalTime());
    }

    public OffsetDateTime getOffsetDateTime() {
        return OffsetDateTime.of(getDateTime(), timezonePicker.getOffsetTime().getOffset());
    }

    private void setCurrentDate() {
        if (datePicker.getDateStringOrEmptyString().isEmpty())
            datePicker.setDate(LocalDate.now());
    }

    public void setVisibleNullBox(boolean flag) {
        timezonePicker.setVisibleNullCheck(flag);
    }

    public boolean isNull() {
        return datePicker.getDate() == null;
    }

    public DefaultDatePicker getDatePicker() {
        return datePicker;
    }

    public DefaultTimezonePicker getTimezonePicker() {
        return timezonePicker;
    }

    public void setNull(boolean isNull) {
        timezonePicker.setNull(isNull);
        datePicker.setEnabled(!isNull);
    }

}

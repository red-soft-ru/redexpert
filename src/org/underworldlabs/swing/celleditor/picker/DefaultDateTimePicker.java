package org.underworldlabs.swing.celleditor.picker;

import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DefaultDateTimePicker extends JPanel {

    private DefaultDatePicker datePicker;
    private DefaultTimePicker timePicker;

    public DefaultDateTimePicker() {
        init();
        arrange();
    }

    void init() {
        timePicker = new DefaultTimePicker();
        datePicker = new DefaultDatePicker();

        timePicker.addNullCheckActionListener(e -> datePicker.setEnabled(!timePicker.isNull()));
        timePicker.addNullCheckActionListener(e -> setCurrentDate());
        datePicker.addDateChangeListener(e -> setCurrentDate());
    }

    private void arrange() {
        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillBoth().spanY();

        setLayout(new GridBagLayout());
        add(datePicker, gbh.get());
        add(timePicker, gbh.nextCol().spanX().get());

        setPreferredSize(new Dimension(320, timePicker.getPreferredSize().height));
    }

    public String getStringValue() {

        if (timePicker.isNull())
            return "";

        String date = datePicker.getDateStringOrEmptyString();
        String time = timePicker.getStringValue().trim();

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
        timePicker.setTime(dateTime.toLocalTime());
    }

    public LocalDateTime getDateTime() {
        return LocalDateTime.of(datePicker.getDate(), timePicker.getLocalTime());
    }

    private void setCurrentDate() {
        if (datePicker.getDateStringOrEmptyString().isEmpty())
            datePicker.setDate(LocalDate.now());
    }

    public void setVisibleNullBox(boolean flag) {
        timePicker.setVisibleNullCheck(flag);
    }

    public boolean isNull() {
        return datePicker.getDate() == null;
    }

    public DefaultDatePicker getDatePicker() {
        return datePicker;
    }

    public DefaultTimePicker getTimePicker() {
        return timePicker;
    }

    public void setNull(boolean isNull) {
        timePicker.setNull(isNull);
        datePicker.setEnabled(!isNull);
    }

}

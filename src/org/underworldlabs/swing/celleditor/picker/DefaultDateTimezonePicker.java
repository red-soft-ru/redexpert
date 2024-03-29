package org.underworldlabs.swing.celleditor.picker;

import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class DefaultDateTimezonePicker extends JPanel {

    private DefaultDatePicker datePicker;
    private DefaultTimezonePicker timezonePicker;

    public DefaultDateTimezonePicker() {
        init();
        arrange();
        update();
    }

    private void init() {
        datePicker = new DefaultDatePicker();

        timezonePicker = new DefaultTimezonePicker();
        timezonePicker.addNullCheckActionListener(e -> update());
    }

    private void arrange() {
        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillBoth().spanY();

        setLayout(new GridBagLayout());
        add(datePicker, gbh.get());
        add(timezonePicker, gbh.nextCol().spanX().get());

        setPreferredSize(new Dimension(450, timezonePicker.getPreferredSize().height));
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
        return isNull() ? null : LocalDateTime.of(datePicker.getDate(), timezonePicker.getLocalTime());
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

    public DefaultDatePicker getDatePicker() {
        return datePicker;
    }

    public DefaultTimezonePicker getTimezonePicker() {
        return timezonePicker;
    }

}

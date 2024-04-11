package org.underworldlabs.swing.celleditor.picker;

import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

public class TimestampPicker extends JPanel {

    private DatePicker datePicker;
    private TimePicker timePicker;

    public TimestampPicker() {
        init();
        arrange();
        update();
    }

    void init() {
        datePicker = new DatePicker();

        timePicker = new TimePicker();
        timePicker.addNullCheckActionListener(e -> update());
    }

    private void arrange() {
        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillBoth().spanY();

        setLayout(new GridBagLayout());
        add(datePicker, gbh.setWeightX(0.4).get());
        add(timePicker, gbh.nextCol().setWeightX(0.6).spanX().get());

        setPreferredSize(new Dimension(350, timePicker.getPreferredSize().height));
    }

    public String getStringValue() {

        if (isNull())
            return null;

        String date = datePicker.getDateStringOrEmptyString().trim();
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
        return isNull() ? null : LocalDateTime.of(datePicker.getDate(), timePicker.getLocalTime());
    }

    private void update() {
        setDateTime(isNull() ? null : LocalDateTime.now());
        setEnabled(!isNull());
    }

    public void setNull(boolean isNull) {
        timePicker.setNull(isNull);
        setEnabled(!isNull());
    }

    @Override
    public void setEnabled(boolean enabled) {
        datePicker.setEnabled(enabled);
        timePicker.setEnabled(enabled);
    }

    public void setVisibleNullCheck(boolean flag) {
        timePicker.setVisibleNullCheck(flag);
    }

    public boolean isNull() {
        return timePicker.isNull();
    }

    public DatePicker getDatePicker() {
        return datePicker;
    }

    public TimePicker getTimePicker() {
        return timePicker;
    }

}

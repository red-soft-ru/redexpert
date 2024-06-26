package org.underworldlabs.swing.celleditor.picker;


import org.executequery.gui.WidgetFactory;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TimePicker extends JPanel {

    protected JSpinner timeSpinner;
    protected JCheckBox isNullCheck;

    public TimePicker() {
        init();
        arrange();
        update();
    }

    void init() {

        timeSpinner = WidgetFactory.createSpinner("timeSpinner", new SpinnerDateModel());
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm:ss.SSS"));

        isNullCheck = WidgetFactory.createCheckBox("isNullCheck", "NULL");
        isNullCheck.addActionListener(e -> update());
    }

    private void arrange() {
        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillBoth().spanY();

        setLayout(new GridBagLayout());
        add(timeSpinner, gbh.setMaxWeightX().get());
        add(isNullCheck, gbh.anchorNorthEast().setMinWeightX().nextCol().get());

        setPreferredSize(new Dimension(200, timeSpinner.getPreferredSize().height));
    }

    public String getStringValue() {

        if (isNull())
            return null;

        Instant instant = Instant.ofEpochMilli(((Date) (timeSpinner).getValue()).getTime());
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());

        return localDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
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

    private void update() {
        setTime(isNull() ? null : LocalTime.now());
        setEnabled(!isNull());
    }

    public void setNull(boolean isNull) {
        isNullCheck.setSelected(isNull);
        setEnabled(!isNull());
    }

    @Override
    public void setEnabled(boolean enabled) {
        timeSpinner.setEnabled(enabled);
    }

    public void setVisibleNullCheck(boolean visible) {
        isNullCheck.setVisible(visible);
    }

    public boolean isNull() {
        return isNullCheck.isSelected();
    }

    public LocalTime getLocalTime() {

        if (isNull())
            return null;

        Instant instant = Instant.ofEpochMilli(((Date) (timeSpinner).getValue()).getTime());
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime();
    }

    public void addNullCheckActionListener(ActionListener l) {
        isNullCheck.addActionListener(l);
    }

    public void setDisplayPattern(String pattern) {
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, pattern));
    }

    public void addChangeListener(ChangeListener l) {
        timeSpinner.addChangeListener(l);
    }

}

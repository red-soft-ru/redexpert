package org.underworldlabs.swing;


import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.*;
import java.util.Date;

public class EQTimePicker extends JPanel {
    JSpinner timeSpinner;
    JSpinner.DateEditor timeEditor;
    JCheckBox nullBox;

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
        timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm:ss");
        timeSpinner.setEditor(timeEditor);
        nullBox = new JCheckBox("NULL");
        nullBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                timeSpinner.setEnabled(!nullBox.isSelected());
            }
        });
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addComponent(timeSpinner)
                        .addGap(10)
                        .addComponent(nullBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)

        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(nullBox)
                        .addComponent(timeSpinner)
        );
    }

    public String getStringValue() {
        if (nullBox.isSelected())
            return "";
        Instant instant = Instant.ofEpochMilli(((Date) (timeSpinner).getValue()).getTime());
        LocalDateTime temp = LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());
        return temp.getHour() + ":" + temp.getMinute() + ":" + temp.getSecond();
    }

    public LocalDateTime getTime() {
        Instant instant = Instant.ofEpochMilli(((Date) (timeSpinner).getValue()).getTime());
        LocalDateTime temp = LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());
        return temp;
    }

    public void setTime(LocalTime time) {
        if (time != null) {
            Instant instant = time.atDate(LocalDate.of(1970, 1, 1)).
                    atZone(ZoneId.systemDefault()).toInstant();
            Date date = Date.from(instant);
            timeSpinner.setValue(date);
        } else {
            nullBox.setSelected(true);
        }
    }

    public void setEnable(boolean enable) {
        setEnabled(enable);
        nullBox.setSelected(!enable);
        nullBox.setEnabled(enable);
        timeSpinner.setEnabled(enable);
    }

    public void setVisibleNullBox(boolean flag) {
        nullBox.setVisible(flag);
    }
}
package org.underworldlabs.swing;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

public class CronPanel extends JPanel {
    private JCheckBox everyMinuteCheckBox;
    private JSpinner minutesSpinner;
    private JCheckBox everyHourCheckBox;
    private JSpinner hoursSpinner;
    private JCheckBox everyDayOfMonthCheckBox;
    private JSpinner daysOfMonthSpinner;
    private JTextField daysOfMonthTextField;
    private JCheckBox everyMonthCheckBox;
    private JCheckBox[] monthsCheckBoxes;
    private JCheckBox everyDayOfWeekCheckBox;
    private JCheckBox[] daysOfWeekCheckBoxes;

    public CronPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Minutes
        JPanel minutesPanel = new JPanel();
        minutesPanel.setLayout(new BoxLayout(minutesPanel, BoxLayout.X_AXIS));
        everyMinuteCheckBox = new JCheckBox("Every minute");
        everyMinuteCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                minutesSpinner.setEnabled(!everyMinuteCheckBox.isSelected());
            }
        });
        minutesPanel.add(everyMinuteCheckBox);
        SpinnerNumberModel minutesModel = new SpinnerNumberModel(0, 0, 59, 1);
        minutesSpinner = new JSpinner(minutesModel);
        minutesSpinner.setEnabled(false);
        minutesPanel.add(minutesSpinner);
        add(minutesPanel);

        // Hours
        JPanel hoursPanel = new JPanel();
        hoursPanel.setLayout(new BoxLayout(hoursPanel, BoxLayout.X_AXIS));
        everyHourCheckBox = new JCheckBox("Every hour");
        everyHourCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hoursSpinner.setEnabled(!everyHourCheckBox.isSelected());
            }
        });
        hoursPanel.add(everyHourCheckBox);
        SpinnerNumberModel hoursModel = new SpinnerNumberModel(0, 0, 23, 1);
        hoursSpinner = new JSpinner(hoursModel);
        hoursSpinner.setEnabled(false);
        hoursPanel.add(hoursSpinner);
        add(hoursPanel);

        // Days of the month
        JPanel daysOfMonthPanel = new JPanel();
        daysOfMonthPanel.setLayout(new BoxLayout(daysOfMonthPanel, BoxLayout.X_AXIS));
        everyDayOfMonthCheckBox = new JCheckBox("Every day of the month");
        everyDayOfMonthCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                daysOfMonthSpinner.setEnabled(!everyDayOfMonthCheckBox.isSelected());
                daysOfMonthTextField.setEnabled(!everyDayOfMonthCheckBox.isSelected());
            }
        });
        daysOfMonthPanel.add(everyDayOfMonthCheckBox);
        SpinnerNumberModel daysOfMonthModel = new SpinnerNumberModel(1, 1, 31, 1);
        daysOfMonthSpinner = new JSpinner(daysOfMonthModel);
        daysOfMonthSpinner.setEnabled(false);
        daysOfMonthPanel.add(daysOfMonthSpinner);
        daysOfMonthTextField = new JTextField(10);
        daysOfMonthTextField.setInputVerifier(new InputVerifier() {
            public boolean verify(JComponent input) {
                JTextField textField = (JTextField) input;
                String text = textField.getText();
                return text.matches("^\\*|([0-9]+(,[0-9]+)*)$") && text.length() <= 100;
            }
        });
        daysOfMonthTextField.setEnabled(false);
        daysOfMonthPanel.add(daysOfMonthTextField);
        add(daysOfMonthPanel);

        // Months
        JPanel monthsPanel = new JPanel();
        monthsPanel.setLayout(new BoxLayout(monthsPanel, BoxLayout.X_AXIS));
        everyMonthCheckBox = new JCheckBox("Every month");
        everyMonthCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < monthsCheckBoxes.length; i++) {
                    monthsCheckBoxes[i].setEnabled(!everyMonthCheckBox.isSelected());
                }
            }
        });
        monthsPanel.add(everyMonthCheckBox);
        monthsCheckBoxes = new JCheckBox[12];
        for (int i = 0; i < monthsCheckBoxes.length; i++) {
            monthsCheckBoxes[i] = new JCheckBox(new DateFormatSymbols().getMonths()[i]);
            monthsPanel.add(monthsCheckBoxes[i]);
        }
        add(monthsPanel);
        // Days of the week
        JPanel daysOfWeekPanel = new JPanel();
        daysOfWeekPanel.setLayout(new BoxLayout(daysOfWeekPanel, BoxLayout.X_AXIS));
        everyDayOfWeekCheckBox = new JCheckBox("Every day of the week");
        everyDayOfWeekCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (JCheckBox checkBox : daysOfWeekCheckBoxes) {
                    checkBox.setEnabled(!everyDayOfWeekCheckBox.isSelected());
                }
            }
        });
        daysOfWeekPanel.add(everyDayOfWeekCheckBox);
        daysOfWeekCheckBoxes = new JCheckBox[7];
        String[] weekdays = new DateFormatSymbols().getWeekdays();
        for (int i = 1; i <= 7; i++) {
            daysOfWeekCheckBoxes[i - 1] = new JCheckBox(weekdays[i]);
            daysOfWeekCheckBoxes[i - 1].setEnabled(false);
            daysOfWeekPanel.add(daysOfWeekCheckBoxes[i - 1]);
        }
        add(daysOfWeekPanel);
    }

    public void setCronString(String cronString) {
        String[] cronParts = cronString.split("\\s+");
        if (cronParts.length != 5) {
            throw new IllegalArgumentException("Invalid cron format string: " + cronString);
        }

        // Minutes
        if (cronParts[0].equals("*")) {
            everyMinuteCheckBox.setSelected(true);
            minutesSpinner.setEnabled(false);
        } else {
            everyMinuteCheckBox.setSelected(false);
            minutesSpinner.setValue(Integer.parseInt(cronParts[0]));
            minutesSpinner.setEnabled(true);
        }

        // Hours
        if (cronParts[1].equals("*")) {
            everyHourCheckBox.setSelected(true);
            hoursSpinner.setEnabled(false);
        } else {
            everyHourCheckBox.setSelected(false);
            hoursSpinner.setValue(Integer.parseInt(cronParts[1]));
            hoursSpinner.setEnabled(true);
        }

        // Days of the month
        if (cronParts[2].equals("*")) {
            everyDayOfMonthCheckBox.setSelected(true);
            daysOfMonthSpinner.setEnabled(false);
            daysOfMonthTextField.setEnabled(false);
        } else if (cronParts[2].contains(",")) {
            everyDayOfMonthCheckBox.setSelected(false);
            daysOfMonthSpinner.setEnabled(false);
            daysOfMonthTextField.setText(cronParts[2]);
            daysOfMonthTextField.setEnabled(true);
        } else {
            everyDayOfMonthCheckBox.setSelected(false);
            daysOfMonthSpinner.setValue(Integer.parseInt(cronParts[2]));
            daysOfMonthSpinner.setEnabled(true);
            daysOfMonthTextField.setEnabled(false);
        }

        // Months
        if (cronParts[3].equals("*")) {
            everyMonthCheckBox.setSelected(true);
            for (JCheckBox checkBox : monthsCheckBoxes) {
                checkBox.setEnabled(false);
            }
        } else {
            everyMonthCheckBox.setSelected(false);
            String[] monthList = cronParts[3].split(",");
            for (int i = 0; i < monthsCheckBoxes.length; i++) {
                JCheckBox checkBox = monthsCheckBoxes[i];
                checkBox.setSelected(false);
                for (String month : monthList) {
                    if (month.matches("[a-zA-Z]+")) {
                        if (checkBox.getText().startsWith(month.substring(0, 3))) {
                            checkBox.setSelected(true);
                        }
                    } else {
                        if (Integer.parseInt(month) == i + 1) {
                            checkBox.setSelected(true);
                        }
                    }
                }
                checkBox.setEnabled(true);
            }
        }

        // Days of the week
        if (cronParts[4].equals("*")) {
            everyDayOfWeekCheckBox.setSelected(true);
            for (JCheckBox checkBox : daysOfWeekCheckBoxes) {
                checkBox.setEnabled(false);
            }
        } else {
            everyDayOfWeekCheckBox.setSelected(false);
            String[] dowList = cronParts[4].split(",");
            for (int i = 0; i < daysOfWeekCheckBoxes.length; i++) {
                JCheckBox checkBox = daysOfWeekCheckBoxes[i];
                checkBox.setSelected(false);
                for (String dow : dowList) {
                    if (dow.matches("[a-zA-Z]+")) {
                        if (checkBox.getText().startsWith(dow.substring(0, 3))) {
                            checkBox.setSelected(true);
                        }
                    } else {
                        if (Integer.parseInt(dow) == i) {
                            checkBox.setSelected(true);
                        }
                    }
                }
                checkBox.setEnabled(true);
            }
        }
    }

    public String getCronString() {
        StringBuilder sb = new StringBuilder();
        if (everyMinuteCheckBox.isSelected()) {
            sb.append("*");
        } else {
            sb.append((Integer) minutesSpinner.getValue());
        }
        sb.append(" ");

        if (everyHourCheckBox.isSelected()) {
            sb.append("*");
        } else {
            sb.append((Integer) hoursSpinner.getValue());
        }
        sb.append(" ");

        if (everyDayOfMonthCheckBox.isSelected()) {
            sb.append("*");
        } else if (daysOfMonthTextField.isEnabled()) {
            sb.append(daysOfMonthTextField.getText());
        } else {
            sb.append((Integer) daysOfMonthSpinner.getValue());
        }
        sb.append(" ");

        if (everyMonthCheckBox.isSelected()) {
            sb.append("*");
        } else {
            List<String> selectedMonths = new ArrayList<>();
            for (JCheckBox checkBox : monthsCheckBoxes) {
                if (checkBox.isSelected()) {
                    String month = checkBox.getText().substring(0, 3);
                    selectedMonths.add(month);
                }
            }
            sb.append(String.join(",", selectedMonths));
        }
        sb.append(" ");

        if (everyDayOfWeekCheckBox.isSelected()) {
            sb.append("*");
        } else {
            List<String> selectedDOWs = new ArrayList<>();
            for (int i = 0; i < daysOfWeekCheckBoxes.length; i++) {
                JCheckBox checkBox = daysOfWeekCheckBoxes[i];
                if (checkBox.isSelected()) {
                    selectedDOWs.add(Integer.toString(i));
                }
            }
            sb.append(String.join(",", selectedDOWs));
        }

        return sb.toString();
    }
}


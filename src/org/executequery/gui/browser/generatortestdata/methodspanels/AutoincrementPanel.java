package org.executequery.gui.browser.generatortestdata.methodspanels;

import com.github.lgooddatepicker.components.DatePicker;
import org.executequery.databaseobjects.DatabaseColumn;
import org.underworldlabs.swing.DateDifferenceSetter;
import org.underworldlabs.swing.EQDateTimePicker;
import org.underworldlabs.swing.EQTimePicker;
import org.underworldlabs.swing.NumberTextField;

import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AutoincrementPanel extends AbstractMethodPanel {
    private JPanel settingsPanel;
    private JTextField iterationField;
    private JTextField startValueField;
    private EQTimePicker startValueTime;
    private EQTimePicker iterationTime;
    private EQDateTimePicker startValueDateTime;
    private EQDateTimePicker iterationDateTime;
    private DateDifferenceSetter iterationDate;
    private DatePicker startValueDate;
    private JComboBox plusMinusBox;
    private long current_value;
    private LocalDateTime current_date_time;
    private BigInteger cur_bigint;
    private double current_double;

    public AutoincrementPanel(DatabaseColumn col) {
        super(col);
        init();
    }

    private void init() {
        settingsPanel = new JPanel();
        plusMinusBox = new JComboBox(new String[]{
                "+", "-"
        });

        settingsPanel.setLayout(new GridBagLayout());
        if (col.getFormattedDataType().contentEquals("BIGINT")
                || col.getFormattedDataType().contentEquals("INTEGER")
                || col.getFormattedDataType().contentEquals("SMALLINT")
                || col.getFormattedDataType().contentEquals("DOUBLE PRECISION")
                || col.getFormattedDataType().contentEquals("FLOAT")
                || col.getFormattedDataType().startsWith("DECIMAL")
                || col.getFormattedDataType().startsWith("NUMERIC")) {

            if (col.getFormattedDataType().contentEquals("BIGINT")
                    || col.getFormattedDataType().contentEquals("DOUBLE PRECISION")
                    || col.getFormattedDataType().contentEquals("FLOAT")
                    || col.getFormattedDataType().startsWith("DECIMAL")
                    || col.getFormattedDataType().startsWith("NUMERIC")) {
                iterationField = new JTextField();
                startValueField = new JTextField();
                if (col.getFormattedDataType().contentEquals("BIGINT")) {
                    iterationField.setText("1");
                    startValueField.setText("-9223372036854775808");
                } else {
                    iterationField.setText("1");
                    startValueField.setText("0");
                }
            } else {
                iterationField = new NumberTextField();
                startValueField = new NumberTextField();
                if (col.getFormattedDataType().contentEquals("INTEGER")) {
                    iterationField.setText("" + Integer.MAX_VALUE);
                    startValueField.setText("" + Integer.MIN_VALUE);
                } else {
                    iterationField.setText("" + 1);
                    startValueField.setText("" + (-32768));
                }
            }


            JLabel label = new JLabel("Start value");
            settingsPanel.add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            settingsPanel.add(startValueField, new GridBagConstraints(1, 0, 1, 1, 1, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            label = new JLabel("Iteration");
            settingsPanel.add(label, new GridBagConstraints(2, 0, 1, 1, 0, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 20, 5, 5), 0, 0));
            settingsPanel.add(iterationField, new GridBagConstraints(3, 0, 1, 1, 1, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

        }
        if (col.getFormattedDataType().contentEquals("TIME")) {
            startValueTime = new EQTimePicker();
            startValueTime.setTime(LocalTime.MIN);
            iterationTime = new EQTimePicker();
            iterationTime.setTime(LocalTime.of(1, 1, 1));
            JLabel label = new JLabel("Start value");
            settingsPanel.add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            settingsPanel.add(startValueTime, new GridBagConstraints(1, 0, 1, 1, 1, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            label = new JLabel("Iteration");
            settingsPanel.add(label, new GridBagConstraints(2, 0, 1, 1, 0, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 20, 5, 5), 0, 0));
            settingsPanel.add(iterationTime, new GridBagConstraints(3, 0, 1, 1, 1, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        }
        if (col.getFormattedDataType().contentEquals("DATE")) {
            startValueDate = new DatePicker();
            startValueDate.setDate(LocalDate.of(0, 1, 1));
            iterationDate = new DateDifferenceSetter();
            JLabel label = new JLabel("Start value");
            settingsPanel.add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            settingsPanel.add(startValueDate, new GridBagConstraints(1, 0, 1, 1, 1, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            label = new JLabel("Iteration");
            settingsPanel.add(label, new GridBagConstraints(2, 0, 1, 1, 0, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 20, 5, 5), 0, 0));
            settingsPanel.add(iterationDate, new GridBagConstraints(3, 0, 1, 1, 1, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        }
        if (col.getFormattedDataType().contentEquals("TIMESTAMP")) {
            startValueDateTime = new EQDateTimePicker();
            startValueDateTime.setDateTimePermissive(LocalDateTime.MIN);
            iterationDate = new DateDifferenceSetter();
            iterationTime = new EQTimePicker();
            iterationTime.setTime(LocalTime.of(0, 0, 0));
            JLabel label = new JLabel("Start value");
            settingsPanel.add(label, new GridBagConstraints(0, 0, 1, 1, 0, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            settingsPanel.add(startValueDateTime, new GridBagConstraints(1, 0, 1, 1, 1, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            label = new JLabel("Iteration");
            settingsPanel.add(label, new GridBagConstraints(2, 0, 1, 1, 0, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 20, 5, 5), 0, 0));
            settingsPanel.add(iterationDate, new GridBagConstraints(3, 0, 1, 1, 1, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
            settingsPanel.add(iterationTime, new GridBagConstraints(3, 1, 1, 1, 1, 0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        }
        setLayout(new GridBagLayout());
        add(plusMinusBox, new GridBagConstraints(0, 0, 1, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        add(settingsPanel, new GridBagConstraints(0, 1, 1, 1, 1, 1,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    }

    public Object getTestDataObject() {
        if (col.getFormattedDataType().contentEquals("BIGINT")) {
            if (first) {
                cur_bigint = new BigInteger(startValueField.getText());
                first = false;
            }
            if (plusMinusBox.getSelectedIndex() == 0)
                cur_bigint.add(new BigInteger(iterationField.getText()));
            else cur_bigint.subtract(new BigInteger(iterationField.getText()));
            return cur_bigint;
        }
        if (col.getFormattedDataType().contentEquals("TIME")) {
            if (first) {
                current_date_time = startValueTime.getTime();
                first = false;
            }
            LocalTime iteration = iterationTime.getTime().toLocalTime();
            if (plusMinusBox.getSelectedIndex() == 0) {
                current_date_time = current_date_time.plusHours(iteration.getHour());
                current_date_time = current_date_time.plusMinutes(iteration.getMinute());
                current_date_time = current_date_time.plusSeconds(iteration.getSecond());
                current_date_time = current_date_time.plusNanos(iteration.getNano());
            } else {
                current_date_time = current_date_time.minusHours(iteration.getHour());
                current_date_time = current_date_time.minusMinutes(iteration.getMinute());
                current_date_time = current_date_time.minusSeconds(iteration.getSecond());
                current_date_time = current_date_time.minusNanos(iteration.getNano());
            }
            return new Time(Timestamp.valueOf(current_date_time).getTime());
        }
        if (col.getFormattedDataType().contentEquals("DATE")) {
            if (first) {
                current_date_time = LocalDateTime.of(startValueDate.getDate(), LocalTime.of(0, 0, 0));
                first = false;
            }
            if (plusMinusBox.getSelectedIndex() == 0) {
                current_date_time = current_date_time.plusYears(iterationDate.getYears());
                current_date_time = current_date_time.plusDays(iterationDate.getDays());
                current_date_time = current_date_time.plusMonths(iterationDate.getMouths());
            } else {
                current_date_time = current_date_time.minusYears(iterationDate.getYears());
                current_date_time = current_date_time.minusDays(iterationDate.getDays());
                current_date_time = current_date_time.minusMonths(iterationDate.getMouths());
            }
            return new Date(Timestamp.valueOf(current_date_time).getTime());
        }
        if (col.getFormattedDataType().contentEquals("TIMESTAMP")) {
            if (first) {
                current_date_time = startValueDateTime.getDateTime();
                first = false;
            }
            LocalTime iteration = iterationTime.getTime().toLocalTime();
            if (plusMinusBox.getSelectedIndex() == 0) {
                current_date_time = current_date_time.plusYears(iterationDate.getYears());
                current_date_time = current_date_time.plusDays(iterationDate.getDays());
                current_date_time = current_date_time.plusMonths(iterationDate.getMouths());
                current_date_time = current_date_time.plusHours(iteration.getHour());
                current_date_time = current_date_time.plusMinutes(iteration.getMinute());
                current_date_time = current_date_time.plusSeconds(iteration.getSecond());
                current_date_time = current_date_time.plusNanos(iteration.getNano());
            } else {
                current_date_time = current_date_time.minusYears(iterationDate.getYears());
                current_date_time = current_date_time.minusDays(iterationDate.getDays());
                current_date_time = current_date_time.minusMonths(iterationDate.getMouths());
                current_date_time = current_date_time.minusHours(iteration.getHour());
                current_date_time = current_date_time.minusMinutes(iteration.getMinute());
                current_date_time = current_date_time.minusSeconds(iteration.getSecond());
                current_date_time = current_date_time.minusNanos(iteration.getNano());
            }
            return Timestamp.valueOf(current_date_time);

        }
        if (col.getFormattedDataType().contentEquals("INTEGER") || col.getFormattedDataType().contentEquals("SMALLINT")) {
            if (first) {
                current_value = Long.parseLong(startValueField.getText());
                first = false;
            }
            if (plusMinusBox.getSelectedIndex() == 0)
                current_value += Long.parseLong(iterationField.getText());
            else current_value -= Long.parseLong(iterationField.getText());
            if (col.getFormattedDataType().contentEquals("SMALLINT"))
                return (short) current_value;
            return (int) current_value;
        }
        if (col.getFormattedDataType().contentEquals("DOUBLE PRECISION")
                || col.getFormattedDataType().contentEquals("FLOAT")
                || col.getFormattedDataType().startsWith("DECIMAL")
                || col.getFormattedDataType().startsWith("NUMERIC")) {
            if (first) {
                current_double = Double.parseDouble(startValueField.getText());
                first = false;
            }
            if (plusMinusBox.getSelectedIndex() == 0)
                current_double += Double.parseDouble(iterationField.getText());
            else current_double -= Double.parseDouble(iterationField.getText());
            return current_double;
        }

        return null;
    }
}

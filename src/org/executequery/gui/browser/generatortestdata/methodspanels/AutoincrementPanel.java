package org.executequery.gui.browser.generatortestdata.methodspanels;

import com.github.lgooddatepicker.components.DatePicker;
import org.executequery.databaseobjects.DatabaseColumn;
import org.underworldlabs.swing.DateDifferenceSetter;
import org.underworldlabs.swing.EQDateTimePicker;
import org.underworldlabs.swing.EQTimePicker;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.layouts.GridBagHelper;

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
        GridBagHelper gbh = new GridBagHelper();
        GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0);
        gbh.setDefaults(gbc);
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
                    iterationField.setText("" + 1);
                    startValueField.setText("" + Integer.MIN_VALUE);
                } else {
                    iterationField.setText("" + 1);
                    startValueField.setText("" + (-32768));
                }
            }

            gbh.setXY(0, 0);
            JLabel label = new JLabel(bundles("StartValue"));
            settingsPanel.add(label, gbh.defaults().setLabelDefault().get());
            settingsPanel.add(startValueField, gbh.defaults().nextCol().setMaxWeightX().get());
            label = new JLabel(bundles("Iteration"));
            settingsPanel.add(label, gbh.defaults().nextCol().setLabelDefault().get());
            settingsPanel.add(iterationField, gbh.defaults().nextCol().setMaxWeightX().get());

        }
        if (col.getFormattedDataType().contentEquals("TIME")) {
            startValueTime = new EQTimePicker();
            startValueTime.setVisibleNullBox(false);
            startValueTime.setTime(LocalTime.MIN);
            iterationTime = new EQTimePicker();
            iterationTime.setVisibleNullBox(false);
            iterationTime.setTime(LocalTime.of(1, 1, 1));

            gbh.setXY(0, 0);

            JLabel label = new JLabel(bundles("StartValue"));
            settingsPanel.add(label, gbh.defaults().setLabelDefault().get());
            settingsPanel.add(startValueTime, gbh.defaults().nextCol().setMaxWeightX().get());
            label = new JLabel(bundles("Iteration"));
            settingsPanel.add(label, gbh.defaults().nextCol().setLabelDefault().get());
            settingsPanel.add(iterationTime, gbh.defaults().nextCol().setMaxWeightX().get());
        }
        if (col.getFormattedDataType().contentEquals("DATE")) {
            startValueDate = new DatePicker();
            startValueDate.setDate(LocalDate.now());
            iterationDate = new DateDifferenceSetter();


            JLabel label = new JLabel(bundles("StartValue"));
            settingsPanel.add(label, gbh.defaults().setLabelDefault().get());
            settingsPanel.add(startValueDate, gbh.defaults().nextCol().setMaxWeightX().get());
            label = new JLabel(bundles("Iteration"));
            settingsPanel.add(label, gbh.defaults().nextRowFirstCol().setLabelDefault().get());
            settingsPanel.add(iterationDate, gbh.defaults().nextCol().setMaxWeightX().get());
        }
        if (col.getFormattedDataType().contentEquals("TIMESTAMP")) {
            startValueDateTime = new EQDateTimePicker();
            startValueDateTime.setVisibleNullBox(false);
            startValueDateTime.setDateTimePermissive(LocalDateTime.now());
            iterationDate = new DateDifferenceSetter();
            iterationTime = new EQTimePicker();
            iterationTime.setVisibleNullBox(false);
            iterationTime.setTime(LocalTime.of(0, 0, 0));

            gbh.setXY(0, 0);
            JLabel label = new JLabel(bundles("StartValue"));
            settingsPanel.add(label, gbh.defaults().setLabelDefault().get());
            settingsPanel.add(startValueDateTime, gbh.defaults().nextCol().setMaxWeightX().get());
            label = new JLabel(bundles("Iteration"));
            settingsPanel.add(label, gbh.defaults().nextRowFirstCol().setLabelDefault().get());
            settingsPanel.add(iterationDate, gbh.defaults().nextCol().setMaxWeightX().get());
            settingsPanel.add(iterationTime, gbh.defaults().nextRow().setMaxWeightX().get());
        }
        setLayout(new GridBagLayout());
        gbh.setXY(0, 0);
        add(plusMinusBox, gbh.defaults().spanX().get());
        add(settingsPanel, gbh.defaults().nextRowFirstCol().spanX().fillBoth().get());
        add(new JPanel(), gbh.defaults().nextRowFirstCol().spanX().spanY().fillBoth().get());
    }

    public Object getTestDataObject() {
        if (col.getFormattedDataType().contentEquals("BIGINT")) {
            if (first) {
                cur_bigint = new BigInteger(startValueField.getText());
                first = false;
                return cur_bigint;
            }
            BigInteger iterationBig = new BigInteger(iterationField.getText());
            if (plusMinusBox.getSelectedIndex() == 0)
                cur_bigint = cur_bigint.add(iterationBig);
            else cur_bigint = cur_bigint.subtract(iterationBig);
            return cur_bigint;
        }
        if (col.getFormattedDataType().contentEquals("TIME")) {
            if (first) {
                current_date_time = startValueTime.getTime();
                first = false;
                return new Time(Timestamp.valueOf(current_date_time).getTime());
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
                return new Date(Timestamp.valueOf(current_date_time).getTime());
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
                return Timestamp.valueOf(current_date_time);
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
                if (col.getFormattedDataType().contentEquals("SMALLINT"))
                    return (short) current_value;
                return (int) current_value;
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
                return current_double;
            }
            if (plusMinusBox.getSelectedIndex() == 0)
                current_double += Double.parseDouble(iterationField.getText());
            else current_double -= Double.parseDouble(iterationField.getText());
            return current_double;
        }

        return null;
    }
}

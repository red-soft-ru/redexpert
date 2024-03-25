package org.executequery.gui.browser.generatortestdata.methodspanels;

import com.github.lgooddatepicker.components.DatePicker;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.T;
import org.executequery.log.Log;
import org.underworldlabs.swing.*;
import org.underworldlabs.swing.celleditor.picker.DefaultDateTimePicker;
import org.underworldlabs.swing.celleditor.picker.DefaultDateTimezonePicker;
import org.underworldlabs.swing.celleditor.picker.DefaultTimePicker;
import org.underworldlabs.swing.celleditor.picker.DefaultTimezonePicker;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public class AutoincrementPanel extends AbstractMethodPanel {
    private JPanel settingsPanel;
    private JTextField iterationField;
    private JTextField startValueField;
    private DefaultTimezonePicker startValueTimezone;
    private DefaultTimePicker startValueTime;
    private DefaultTimePicker iterationTime;
    private DefaultDateTimePicker startValueDateTime;
    private DefaultDateTimezonePicker startValueDateTimezone;
    private DateDifferenceSetter iterationDate;
    private DatePicker startValueDate;
    private JComboBox plusMinusBox;
    private long current_value;
    private LocalDateTime current_local_date_time;
    private OffsetDateTime current_offset_date_time;
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
        Log.info("\"" + col.getFormattedDataType() + "\"");
        if (col.getFormattedDataType().contentEquals(T.BIGINT)
                || col.getFormattedDataType().contentEquals(T.INTEGER)
                || col.getFormattedDataType().contentEquals(T.SMALLINT)
                || col.getFormattedDataType().contentEquals(T.DOUBLE_PRECISION)
                || col.getFormattedDataType().contentEquals(T.FLOAT)
                || col.getFormattedDataType().startsWith(T.DECIMAL)
                || col.getFormattedDataType().startsWith(T.NUMERIC)
                || col.getFormattedDataType().startsWith(T.INT128)) {

            if (col.getFormattedDataType().contentEquals(T.BIGINT)
                    || col.getFormattedDataType().contentEquals(T.DOUBLE_PRECISION)
                    || col.getFormattedDataType().contentEquals(T.FLOAT)
                    || col.getFormattedDataType().startsWith(T.DECIMAL)
                    || col.getFormattedDataType().startsWith(T.NUMERIC)
                    || col.getFormattedDataType().startsWith(T.DECFLOAT)
                    || col.getFormattedDataType().startsWith(T.INT128)
            ) {
                iterationField = new JTextField();
                startValueField = new JTextField();
                if (col.getFormattedDataType().contentEquals(T.BIGINT)) {
                    iterationField.setText("1");
                    startValueField.setText("-9223372036854775808");
                } else {
                    iterationField.setText("1");
                    startValueField.setText("0");
                }
            } else {
                iterationField = new NumberTextField();
                startValueField = new NumberTextField();
                if (col.getFormattedDataType().contentEquals(T.INTEGER)) {
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

        if (col.getFormattedDataType().contentEquals(T.TIME)) {

            startValueTime = new DefaultTimePicker();
            startValueTime.setVisibleNullCheck(false);
            startValueTime.setTime(LocalTime.MIN);

            iterationTime = new DefaultTimePicker();
            iterationTime.setVisibleNullCheck(false);
            iterationTime.setTime(LocalTime.of(1, 1, 1));

            gbh.setXY(0, 0);
            settingsPanel.add(new JLabel(bundles("StartValue")), gbh.defaults().setLabelDefault().get());
            settingsPanel.add(startValueTime, gbh.defaults().nextCol().setMaxWeightX().get());
            settingsPanel.add(new JLabel(bundles("Iteration")), gbh.defaults().nextCol().setLabelDefault().get());
            settingsPanel.add(iterationTime, gbh.defaults().nextCol().setMaxWeightX().get());
        }

        if (col.getFormattedDataType().contentEquals(T.TIME_WITH_TIMEZONE)) {

            startValueTimezone = new DefaultTimezonePicker();
            startValueTimezone.setVisibleNullCheck(false);
            startValueTimezone.setTime(LocalTime.MIN);

            iterationTime = new DefaultTimePicker();
            iterationTime.setVisibleNullCheck(false);
            iterationTime.setTime(LocalTime.of(1, 1, 1));

            gbh.setXY(0, 0);
            settingsPanel.add(new JLabel(bundles("StartValue")), gbh.defaults().setLabelDefault().get());
            settingsPanel.add(startValueTimezone, gbh.defaults().nextCol().setMaxWeightX().get());
            settingsPanel.add(new JLabel(bundles("Iteration")), gbh.defaults().nextCol().setLabelDefault().get());
            settingsPanel.add(iterationTime, gbh.defaults().nextCol().setMaxWeightX().get());
        }

        if (col.getFormattedDataType().contentEquals(T.DATE)) {

            startValueDate = new DatePicker();
            startValueDate.setDate(LocalDate.now());

            iterationDate = new DateDifferenceSetter();

            settingsPanel.add(new JLabel(bundles("StartValue")), gbh.defaults().setLabelDefault().get());
            settingsPanel.add(startValueDate, gbh.defaults().nextCol().setMaxWeightX().get());
            settingsPanel.add(new JLabel(bundles("Iteration")), gbh.defaults().nextRowFirstCol().setLabelDefault().get());
            settingsPanel.add(iterationDate, gbh.defaults().nextCol().setMaxWeightX().get());
        }

        if (col.getFormattedDataType().contentEquals(T.TIMESTAMP)) {

            startValueDateTime = new DefaultDateTimePicker();
            startValueDateTime.setVisibleNullBox(false);
            startValueDateTime.setDateTime(LocalDateTime.now());

            iterationDate = new DateDifferenceSetter();

            iterationTime = new DefaultTimePicker();
            iterationTime.setVisibleNullCheck(false);
            iterationTime.setTime(LocalTime.of(0, 0, 0));

            gbh.setXY(0, 0);
            settingsPanel.add(new JLabel(bundles("StartValue")), gbh.defaults().setLabelDefault().get());
            settingsPanel.add(startValueDateTime, gbh.defaults().nextCol().setMaxWeightX().get());
            settingsPanel.add(new JLabel(bundles("Iteration")), gbh.defaults().nextRowFirstCol().setLabelDefault().get());
            settingsPanel.add(iterationDate, gbh.defaults().nextCol().setMaxWeightX().get());
            settingsPanel.add(iterationTime, gbh.defaults().nextRow().setMaxWeightX().get());
        }

        if (col.getFormattedDataType().contentEquals(T.TIMESTAMP_WITH_TIMEZONE)) {

            startValueDateTimezone = new DefaultDateTimezonePicker();
            startValueDateTimezone.setVisibleNullBox(false);
            startValueDateTimezone.setDateTime(LocalDateTime.now());

            iterationDate = new DateDifferenceSetter();

            iterationTime = new DefaultTimePicker();
            iterationTime.setVisibleNullCheck(false);
            iterationTime.setTime(LocalTime.of(0, 0, 0));

            gbh.setXY(0, 0);
            settingsPanel.add(new JLabel(bundles("StartValue")), gbh.defaults().setLabelDefault().get());
            settingsPanel.add(startValueDateTimezone, gbh.defaults().nextCol().setMaxWeightX().get());
            settingsPanel.add(new JLabel(bundles("Iteration")), gbh.defaults().nextRowFirstCol().setLabelDefault().get());
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
        if (col.getFormattedDataType().contentEquals(T.BIGINT) || col.getFormattedDataType().contentEquals(T.INT128)) {
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
        if (col.getFormattedDataType().contentEquals(T.TIME)) {
            if (first) {
                current_local_date_time = startValueTime.getLocalTime().atDate(LocalDate.of(1970, 1, 1));
                first = false;
                return current_local_date_time.toLocalTime();
            }
            LocalTime iteration = iterationTime.getLocalTime();
            if (plusMinusBox.getSelectedIndex() == 0) {
                current_local_date_time = current_local_date_time.plusHours(iteration.getHour());
                current_local_date_time = current_local_date_time.plusMinutes(iteration.getMinute());
                current_local_date_time = current_local_date_time.plusSeconds(iteration.getSecond());
                current_local_date_time = current_local_date_time.plusNanos(iteration.getNano());
            } else {
                current_local_date_time = current_local_date_time.minusHours(iteration.getHour());
                current_local_date_time = current_local_date_time.minusMinutes(iteration.getMinute());
                current_local_date_time = current_local_date_time.minusSeconds(iteration.getSecond());
                current_local_date_time = current_local_date_time.minusNanos(iteration.getNano());
            }
            return current_local_date_time.toLocalTime();
        }
        if (col.getFormattedDataType().contentEquals(T.TIME_WITH_TIMEZONE)) {
            if (first) {
                current_offset_date_time = startValueTimezone.getOffsetTime().atDate(LocalDate.of(1970, 1, 1));
                first = false;
                return current_offset_date_time.toOffsetTime();
            }
            LocalTime iteration = iterationTime.getLocalTime();
            if (plusMinusBox.getSelectedIndex() == 0) {
                current_offset_date_time = current_offset_date_time.plusHours(iteration.getHour());
                current_offset_date_time = current_offset_date_time.plusMinutes(iteration.getMinute());
                current_offset_date_time = current_offset_date_time.plusSeconds(iteration.getSecond());
                current_offset_date_time = current_offset_date_time.plusNanos(iteration.getNano());
            } else {
                current_offset_date_time = current_offset_date_time.minusHours(iteration.getHour());
                current_offset_date_time = current_offset_date_time.minusMinutes(iteration.getMinute());
                current_offset_date_time = current_offset_date_time.minusSeconds(iteration.getSecond());
                current_offset_date_time = current_offset_date_time.minusNanos(iteration.getNano());
            }
            return current_offset_date_time.toOffsetTime();
        }
        if (col.getFormattedDataType().contentEquals(T.DATE)) {
            if (first) {
                current_local_date_time = LocalDateTime.of(startValueDate.getDate(), LocalTime.of(0, 0, 0));
                first = false;
                return current_local_date_time.toLocalDate();
            }
            if (plusMinusBox.getSelectedIndex() == 0) {
                current_local_date_time = current_local_date_time.plusYears(iterationDate.getYears());
                current_local_date_time = current_local_date_time.plusDays(iterationDate.getDays());
                current_local_date_time = current_local_date_time.plusMonths(iterationDate.getMouths());
            } else {
                current_local_date_time = current_local_date_time.minusYears(iterationDate.getYears());
                current_local_date_time = current_local_date_time.minusDays(iterationDate.getDays());
                current_local_date_time = current_local_date_time.minusMonths(iterationDate.getMouths());
            }
            return new Date(Timestamp.valueOf(current_local_date_time).getTime());
        }
        if (col.getFormattedDataType().contentEquals(T.TIMESTAMP)) {
            if (first) {
                current_local_date_time = startValueDateTime.getDateTime();
                first = false;
                return current_local_date_time;
            }
            LocalTime iteration = iterationTime.getLocalTime();
            if (plusMinusBox.getSelectedIndex() == 0) {
                current_local_date_time = current_local_date_time.plusYears(iterationDate.getYears());
                current_local_date_time = current_local_date_time.plusDays(iterationDate.getDays());
                current_local_date_time = current_local_date_time.plusMonths(iterationDate.getMouths());
                current_local_date_time = current_local_date_time.plusHours(iteration.getHour());
                current_local_date_time = current_local_date_time.plusMinutes(iteration.getMinute());
                current_local_date_time = current_local_date_time.plusSeconds(iteration.getSecond());
                current_local_date_time = current_local_date_time.plusNanos(iteration.getNano());
            } else {
                current_local_date_time = current_local_date_time.minusYears(iterationDate.getYears());
                current_local_date_time = current_local_date_time.minusDays(iterationDate.getDays());
                current_local_date_time = current_local_date_time.minusMonths(iterationDate.getMouths());
                current_local_date_time = current_local_date_time.minusHours(iteration.getHour());
                current_local_date_time = current_local_date_time.minusMinutes(iteration.getMinute());
                current_local_date_time = current_local_date_time.minusSeconds(iteration.getSecond());
                current_local_date_time = current_local_date_time.minusNanos(iteration.getNano());
            }
            return current_local_date_time;

        }
        if (col.getFormattedDataType().contentEquals(T.TIMESTAMP_WITH_TIMEZONE)) {
            if (first) {
                current_offset_date_time = startValueDateTimezone.getOffsetDateTime();
                first = false;
                return current_offset_date_time;
            }
            LocalTime iteration = iterationTime.getLocalTime();
            if (plusMinusBox.getSelectedIndex() == 0) {
                current_offset_date_time = current_offset_date_time.plusYears(iterationDate.getYears());
                current_offset_date_time = current_offset_date_time.plusDays(iterationDate.getDays());
                current_offset_date_time = current_offset_date_time.plusMonths(iterationDate.getMouths());
                current_offset_date_time = current_offset_date_time.plusHours(iteration.getHour());
                current_offset_date_time = current_offset_date_time.plusMinutes(iteration.getMinute());
                current_offset_date_time = current_offset_date_time.plusSeconds(iteration.getSecond());
                current_offset_date_time = current_offset_date_time.plusNanos(iteration.getNano());
            } else {
                current_offset_date_time = current_offset_date_time.minusYears(iterationDate.getYears());
                current_offset_date_time = current_offset_date_time.minusDays(iterationDate.getDays());
                current_offset_date_time = current_offset_date_time.minusMonths(iterationDate.getMouths());
                current_offset_date_time = current_offset_date_time.minusHours(iteration.getHour());
                current_offset_date_time = current_offset_date_time.minusMinutes(iteration.getMinute());
                current_offset_date_time = current_offset_date_time.minusSeconds(iteration.getSecond());
                current_offset_date_time = current_offset_date_time.minusNanos(iteration.getNano());
            }
            return current_offset_date_time;

        }
        if (col.getFormattedDataType().contentEquals(T.INTEGER) || col.getFormattedDataType().contentEquals(T.SMALLINT)) {
            if (first) {
                current_value = Long.parseLong(startValueField.getText());
                first = false;
                if (col.getFormattedDataType().contentEquals(T.SMALLINT))
                    return (short) current_value;
                return (int) current_value;
            }
            if (plusMinusBox.getSelectedIndex() == 0)
                current_value += Long.parseLong(iterationField.getText());
            else current_value -= Long.parseLong(iterationField.getText());
            if (col.getFormattedDataType().contentEquals(T.SMALLINT))
                return (short) current_value;
            return (int) current_value;
        }
        if (col.getFormattedDataType().contentEquals(T.DOUBLE_PRECISION)
                || col.getFormattedDataType().contentEquals(T.FLOAT)
                || col.getFormattedDataType().startsWith(T.DECIMAL)
                || col.getFormattedDataType().startsWith(T.NUMERIC)
                || col.getFormattedDataType().startsWith(T.DECFLOAT)
        ) {
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

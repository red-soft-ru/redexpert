package org.underworldlabs.swing.cron;

import com.github.lgooddatepicker.zinternaltools.ExtraDateStrings;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.japura.gui.event.ListCheckListener;
import org.japura.gui.event.ListEvent;
import org.underworldlabs.swing.EQCheckCombox;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.celleditor.picker.TimePicker;
import org.underworldlabs.swing.celleditor.picker.TimestampPicker;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.text.DateFormatSymbols;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.*;
import java.util.stream.IntStream;

public class WindowsSchedulerPanel extends JPanel
        implements DocumentListener,
        ListCheckListener {

    private final JTextField cronField;

    private JRadioButton everyDayRadio;
    private JRadioButton everyWeekdayRadio;
    private JRadioButton everyMonthRadio;
    private JRadioButton everyYearRadio;

    private TimePicker timePicker;
    private TimestampPicker timestampPicker;

    private JCheckBox repeatCheck;
    private EQCheckCombox daysCheckCombo;
    private EQCheckCombox monthCheckCombo;
    private JComboBox<String> intervalCombo;
    private EQCheckCombox weekdaysCheckCombo;

    private NumberTextField intervalField;
    private JPanel propertiessPanel;
    private JLabel everyLabel;
    private JLabel unitLabel;

    private List<String> months;
    private List<String> weekdays;

    public WindowsSchedulerPanel(JTextField cronField) {
        this.cronField = cronField;

        init();
        arrange();
        updateVisibile();
    }

    private void init() {

        months = new ArrayList<>();
        months.addAll(Arrays.asList(ExtraDateStrings.getDefaultStandaloneLongMonthNamesForLocale(Locale.getDefault())));

        weekdays = new ArrayList<>();
        weekdays.addAll(Arrays.asList(new DateFormatSymbols(Locale.getDefault()).getWeekdays()));
        weekdays.remove(0);

        List<String> intervalItems = Arrays.asList(
                bundleString("min", 5),
                bundleString("min", 10),
                bundleString("min", 15),
                bundleString("min", 30),
                bundleString("h", 1)
        );

        // --- radio buttons ---

        everyDayRadio = WidgetFactory.createRadioButton("everyDayRadio", bundleString("EveryDay"), true);
        everyDayRadio.addItemListener(e -> update());

        everyWeekdayRadio = WidgetFactory.createRadioButton("everyWeekdayRadio", bundleString("EveryWeek"));
        everyWeekdayRadio.addItemListener(e -> update());

        everyMonthRadio = WidgetFactory.createRadioButton("everyMonthRadio", bundleString("EveryMonth"));
        everyMonthRadio.addItemListener(e -> update());

        everyYearRadio = WidgetFactory.createRadioButton("everyYearRadio", bundleString("once"));
        everyYearRadio.addItemListener(e -> update());

        ButtonGroup radioButtons = new ButtonGroup();
        radioButtons.add(everyDayRadio);
        radioButtons.add(everyWeekdayRadio);
        radioButtons.add(everyMonthRadio);
        radioButtons.add(everyYearRadio);

        // --- date/time pickers ---

        timePicker = new TimePicker();
        timePicker.setVisibleNullCheck(false);
        timePicker.setDisplayPattern("HH:mm");
        timePicker.addChangeListener(e -> generateCron());

        timestampPicker = new TimestampPicker();
        timestampPicker.setVisibleNullCheck(false);
        timestampPicker.getTimePicker().setDisplayPattern("HH:mm");
        timestampPicker.getTimePicker().addChangeListener(e -> generateCron());
        timestampPicker.getDatePicker().addDocumentListener(this);

        // --- check-combo boxes ---

        daysCheckCombo = WidgetFactory.createCheckComboBox("daysCheckCombo", IntStream.range(1, 32).mapToObj(Integer::toString).toArray());
        daysCheckCombo.getModel().addListCheckListener(this);

        weekdaysCheckCombo = WidgetFactory.createCheckComboBox("weekdaysCheckCombo", weekdays.toArray());
        weekdaysCheckCombo.getModel().addListCheckListener(this);

        monthCheckCombo = WidgetFactory.createCheckComboBox("monthCheckCombo", months.toArray());
        monthCheckCombo.getModel().addListCheckListener(this);

        // --- interval components  ---

        //noinspection unchecked
        intervalCombo = WidgetFactory.createComboBox("intervalCombo", new Vector<>(intervalItems));
        intervalCombo.setPreferredSize(new Dimension(70, intervalCombo.getPreferredSize().height));
        intervalCombo.addItemListener(e -> update());

        intervalField = WidgetFactory.createNumberTextField("intervalField", "1", 2);
        intervalField.getDocument().addDocumentListener(this);

        // --- repeat checkBox ---

        repeatCheck = WidgetFactory.createCheckBox("repeatCheck", bundleString("repeat"));
        repeatCheck.addItemListener(e -> update());

        // ---

        everyLabel = new JLabel(bundleString("Every"));
        unitLabel = new JLabel(bundleString("Day/s"));
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- buttons panel ---

        JPanel buttonsPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(5, 5, 5, 5).anchorNorthWest().fillHorizontally();
        buttonsPanel.add(everyDayRadio, gbh.get());
        buttonsPanel.add(everyWeekdayRadio, gbh.nextRowFirstCol().get());
        buttonsPanel.add(everyMonthRadio, gbh.nextRowFirstCol().get());
        buttonsPanel.add(everyYearRadio, gbh.nextRowFirstCol().get());

        // --- repeat panel ---

        JPanel repeatPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest();
        repeatPanel.add(repeatCheck, gbh.setMinWeightX().get());
        repeatPanel.add(intervalCombo, gbh.nextCol().leftGap(5).setMaxWeightX().get());

        // --- properties panel ---

        propertiessPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(5, 5, 5, 5).anchorNorthWest().fillHorizontally();
        propertiessPanel.add(timePicker, gbh.spanX().get());
        propertiessPanel.add(timestampPicker, gbh.get());
        propertiessPanel.add(everyLabel, gbh.nextRowFirstCol().setWidth(1).topGap(9).setMinWeightX().get());
        propertiessPanel.add(intervalField, gbh.nextCol().topGap(5).get());
        propertiessPanel.add(unitLabel, gbh.nextCol().topGap(9).get());
        propertiessPanel.add(new JLabel(bundleString("Weekdays")), gbh.nextRowFirstCol().setMinWeightX().get());
        propertiessPanel.add(weekdaysCheckCombo, gbh.nextCol().topGap(5).setMaxWeightX().spanX().get());
        propertiessPanel.add(new JLabel(bundleString("Months")), gbh.nextRowFirstCol().topGap(9).setWidth(1).setMinWeightX().get());
        propertiessPanel.add(monthCheckCombo, gbh.nextCol().topGap(5).setMaxWeightX().spanX().get());
        propertiessPanel.add(new JLabel(bundleString("Days")), gbh.nextRowFirstCol().topGap(9).setWidth(1).setMinWeightX().get());
        propertiessPanel.add(daysCheckCombo, gbh.nextCol().setMaxWeightX().topGap(5).spanX().get());
        propertiessPanel.add(repeatPanel, gbh.nextRowFirstCol().get());

        // --- base ---

        setLayout(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().fillBoth();
        add(buttonsPanel, gbh.setMinWeightX().get());
        add(new JSeparator(SwingConstants.VERTICAL), gbh.nextCol().get());
        add(propertiessPanel, gbh.nextCol().setMaxWeightX().spanX().get());
    }

    private void updateVisibile() {

        timePicker.setVisible(!everyYearRadio.isSelected());
        timestampPicker.setVisible(everyYearRadio.isSelected());

        unitLabel.setVisible(everyDayRadio.isSelected() || everyWeekdayRadio.isSelected());
        everyLabel.setVisible(everyDayRadio.isSelected() || everyWeekdayRadio.isSelected());
        intervalField.setVisible(everyDayRadio.isSelected() || everyWeekdayRadio.isSelected());

        daysCheckCombo.setVisible(everyMonthRadio.isSelected());
        monthCheckCombo.setVisible(everyMonthRadio.isSelected());
        weekdaysCheckCombo.setVisible(everyWeekdayRadio.isSelected());

        intervalCombo.setEnabled(repeatCheck.isSelected());
        unitLabel.setText(bundleString(everyDayRadio.isSelected() ? "Day/s" : "Week/s"));

        propertiessPanel.getComponent(propertiessPanel.getComponentZOrder(weekdaysCheckCombo) - 1).setVisible(everyWeekdayRadio.isSelected());
        propertiessPanel.getComponent(propertiessPanel.getComponentZOrder(monthCheckCombo) - 1).setVisible(everyMonthRadio.isSelected());
        propertiessPanel.getComponent(propertiessPanel.getComponentZOrder(daysCheckCombo) - 1).setVisible(everyMonthRadio.isSelected());
    }

    private void generateCron() {
        String[] crons = new String[5];

        if (everyYearRadio.isSelected()) {
            LocalDateTime localDate = timestampPicker.getDateTime();
            if (localDate != null) {
                crons[0] = localDate.getMinute() + "";
                crons[1] = localDate.getHour() + "";
                crons[2] = localDate.getDayOfMonth() + "";
                crons[3] = localDate.getMonthValue() + "";
            }
            crons[4] = "*";

        } else {
            LocalTime localTime = timePicker.getLocalTime();
            if (localTime != null) {
                crons[0] = localTime.getMinute() + "";
                crons[1] = localTime.getHour() + "";
            }
        }

        if (everyDayRadio.isSelected()) {
            crons[2] = "*/" + intervalField.getStringValue();
            crons[3] = "*";
            crons[4] = "*";
        }

        if (everyWeekdayRadio.isSelected()) {

            boolean first = true;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < weekdays.size(); i++) {
                if (weekdaysCheckCombo.getModel().isChecked(weekdays.get(i))) {
                    if (!first)
                        sb.append(",");
                    first = false;
                    sb.append(i);
                }
            }

            crons[2] = "*";
            crons[3] = "*";
            crons[4] = sb.toString();
            if (MiscUtils.isNull(crons[4]))
                crons[4] = "*";
            crons[4] = crons[4] + "/" + intervalField.getStringValue();
        }

        if (everyMonthRadio.isSelected()) {

            boolean first = true;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < months.size(); i++) {
                if (monthCheckCombo.getModel().isChecked(months.get(i))) {
                    if (!first)
                        sb.append(",");
                    first = false;
                    sb.append(i + 1);
                }
            }

            crons[3] = sb.toString();
            if (MiscUtils.isNull(crons[3]))
                crons[3] = "*";

            first = true;
            sb = new StringBuilder();
            for (int i = 1; i <= 31; i++) {
                if (daysCheckCombo.getModel().isChecked(i)) {
                    if (!first)
                        sb.append(",");
                    first = false;
                    sb.append(i);
                }
            }

            crons[2] = sb.toString();
            if (MiscUtils.isNull(crons[2]))
                crons[2] = "*";
        }

        if (repeatCheck.isSelected()) {
            switch (intervalCombo.getSelectedIndex()) {
                case 0:
                    crons[0] += "/5";
                    break;
                case 1:
                    crons[0] += "/10";
                    break;
                case 2:
                    crons[0] += "/15";
                    break;
                case 3:
                    crons[0] += "/30";
                    break;
                case 4:
                    crons[1] += "/1";
                    break;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < crons.length; i++) {
            if (crons[i] == null)
                crons[i] = "*";
            sb.append(crons[i]).append(" ");
        }

        cronField.setText(sb.toString());
    }

    public void update() {
        updateVisibile();
        generateCron();
    }

    private String bundleString(String key, Object... args) {
        return Bundles.get(WindowsSchedulerPanel.class, key, args);
    }

    // --- DocumentListener impl ---

    @Override
    public void insertUpdate(DocumentEvent e) {
        generateCron();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        generateCron();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        generateCron();
    }

    // --- ListCheckListener impl ---

    @Override
    public void removeCheck(ListEvent listEvent) {
        generateCron();
    }

    @Override
    public void addCheck(ListEvent listEvent) {
        generateCron();
    }

}

package org.underworldlabs.swing;

import com.github.lgooddatepicker.zinternaltools.ExtraDateStrings;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DateFormatSymbols;
import java.util.Locale;
import java.util.Vector;

public class CronTab extends JPanel {
    public static final int MINUTES = 0;
    public static final int HOURS = MINUTES + 1;
    public static final int DAYS = HOURS + 1;
    public static final int MONTHS = DAYS + 1;
    public static final int WEEKDAYS = MONTHS + 1;
    public static final String[] CRON_NAMES = {"minute", "hour", "day", "month", "weekday"};
    ButtonGroup radioButtons;
    JRadioButton eachUnitButton;
    JRadioButton intervalUnitsButton;
    JRadioButton specificUnitsButton;
    JRadioButton eachUnitBetweenButton;
    JRadioButton useTextFieldButton;
    JComboBox intervalCombo;
    JCheckBox beginCheck;
    JComboBox beginCombo;
    JCheckBox endCheck;
    JComboBox endCombo;
    CheckBoxPanel checkBoxPanel;
    JComboBox betweenBeginCombo;
    JComboBox betweenEndCombo;
    private boolean fillValues = false;
    private boolean generatingCron = false;
    private Vector<String> units;
    private Vector<Integer> intervalUnits;
    private int typeCronTab;
    private JTextField cronField;
    private ItemListener itemListener;


    public CronTab(int typeCronTab, JTextField cronField) {
        this.typeCronTab = typeCronTab;
        this.cronField = cronField;
        init();
    }

    private void init() {
        radioButtons = new ButtonGroup();
        eachUnitButton = new JRadioButton(bundleString("each_" + CRON_NAMES[typeCronTab]));
        radioButtons.add(eachUnitButton);
        intervalUnitsButton = new JRadioButton(bundleString("interval"));
        radioButtons.add(intervalUnitsButton);
        eachUnitBetweenButton = new JRadioButton(bundleString("each_between_" + CRON_NAMES[typeCronTab]));
        radioButtons.add(eachUnitBetweenButton);
        specificUnitsButton = new JRadioButton(bundleString("specific", bundleString(CRON_NAMES[typeCronTab] + "s")));
        radioButtons.add(specificUnitsButton);
        useTextFieldButton = new JRadioButton(bundleString("use_textField"));
        radioButtons.add(useTextFieldButton);

        units = new Vector<>();
        intervalUnits = new Vector<>();
        DateFormatSymbols dfs = new DateFormatSymbols(Locale.getDefault());
        int countOnRow = 12;

        switch (typeCronTab) {
            case MINUTES:
                for (int i = 0; i < 60; i++) {
                    String unit;
                    if (i < 10)
                        unit = "0" + i;
                    else unit = i + "";
                    units.add(unit);
                    intervalUnits.add(i + 1);
                }
                break;
            case HOURS:
                for (int i = 0; i < 24; i++) {
                    String unit;
                    if (i < 10)
                        unit = "0" + i;
                    else unit = i + "";
                    units.add(unit);
                    intervalUnits.add(i + 1);
                    countOnRow = 6;
                }
                break;
            case DAYS:
                for (int i = 1; i <= 31; i++) {
                    String unit = i + "";
                    units.add(unit);
                    intervalUnits.add(i);
                }
                break;
            case MONTHS:
                String[] months = ExtraDateStrings.getDefaultStandaloneLongMonthNamesForLocale(Locale.getDefault());
                for (int i = 0; i < months.length; i++) {
                    String unit = months[i];
                    units.add(unit);
                    intervalUnits.add(i + 1);
                    countOnRow = 6;
                }
                break;
            case WEEKDAYS:
                String[] weekdays = dfs.getWeekdays();
                for (int i = 1; i < weekdays.length; i++) {
                    String unit = weekdays[i];
                    units.add(unit);
                    intervalUnits.add(1);
                }
                break;
            default:
                break;
        }
        intervalCombo = new JComboBox(intervalUnits);
        intervalCombo.setEditable(true);
        beginCheck = new JCheckBox(bundleString("begin_with"));
        beginCombo = new JComboBox<>(units);
        endCheck = new JCheckBox(bundleString("end_with"));
        endCombo = new JComboBox<>(units);
        betweenBeginCombo = new JComboBox<>(units);
        betweenEndCombo = new JComboBox<>(units);
        checkBoxPanel = new CheckBoxPanel(units.toArray(new String[0]), countOnRow, false);
        checkBoxPanel.setVisibleAllCheckBox(false);


        setLayout(new GridBagLayout());

        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaultsStatic();
        gbh.defaults();

        add(useTextFieldButton, gbh.fillHorizontally().spanX().get());
        add(eachUnitButton, gbh.nextRowFirstCol().setLabelDefault().get());
        add(intervalUnitsButton, gbh.nextRowFirstCol().setLabelDefault().get());
        add(intervalCombo, gbh.nextCol().setLabelDefault().get());
        add(new JLabel(bundleString(CRON_NAMES[typeCronTab] + "/s")), gbh.nextCol().setLabelDefault().get());
        add(beginCheck, gbh.nextCol().setLabelDefault().get());
        add(beginCombo, gbh.nextCol().setLabelDefault().get());
        add(endCheck, gbh.nextCol().setLabelDefault().get());
        add(endCombo, gbh.nextCol().setLabelDefault().get());
        add(eachUnitBetweenButton, gbh.nextRowFirstCol().setLabelDefault().get());
        add(betweenBeginCombo, gbh.nextCol().setLabelDefault().get());
        add(new JLabel(bundleString("and")), gbh.nextCol().setLabelDefault().get());
        add(betweenEndCombo, gbh.nextCol().setLabelDefault().get());
        add(specificUnitsButton, gbh.nextRowFirstCol().fillHorizontally().spanX().get());
        add(checkBoxPanel, gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());

        itemListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getSource() == endCheck && endCheck.isSelected())
                    beginCheck.setSelected(true);
                if (e.getSource() == beginCheck && !beginCheck.isSelected())
                    endCheck.setSelected(false);
                checkEnabled();
                generateCron();
            }
        };
        eachUnitButton.addItemListener(itemListener);
        intervalUnitsButton.addItemListener(itemListener);
        eachUnitBetweenButton.addItemListener(itemListener);
        specificUnitsButton.addItemListener(itemListener);
        useTextFieldButton.addItemListener(itemListener);
        intervalCombo.addItemListener(itemListener);
        beginCheck.addItemListener(itemListener);
        beginCombo.addItemListener(itemListener);
        endCheck.addItemListener(itemListener);
        endCombo.addItemListener(itemListener);
        betweenBeginCombo.addItemListener(itemListener);
        betweenEndCombo.addItemListener(itemListener);
        for (JCheckBox checkBox : checkBoxPanel.getCheckBoxMap().values()) {
            checkBox.addItemListener(itemListener);
        }
        cronField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                fillValuesFromField();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fillValuesFromField();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                fillValuesFromField();
            }
        });
        fillValuesFromField();
    }

    private void checkEnabled() {
        intervalCombo.setEnabled(intervalUnitsButton.isSelected());
        beginCheck.setEnabled(intervalUnitsButton.isSelected());
        endCheck.setEnabled(intervalUnitsButton.isSelected());
        beginCombo.setEnabled(beginCheck.isSelected());
        endCombo.setEnabled(endCheck.isSelected());
        betweenBeginCombo.setEnabled(eachUnitBetweenButton.isSelected());
        betweenEndCombo.setEnabled(eachUnitBetweenButton.isSelected());
        checkBoxPanel.setEnabled(specificUnitsButton.isSelected());
    }

    private int getValueFromIndex(int index) {
        if (typeCronTab == MINUTES || typeCronTab == HOURS || typeCronTab == WEEKDAYS)
            return index;
        else return index + 1;
    }

    private int getIndexFromValue(int value) {
        if (typeCronTab == MINUTES || typeCronTab == HOURS || typeCronTab == WEEKDAYS)
            return value;
        else return value - 1;
    }

    private void generateCron() {
        if (!fillValues) {
            generatingCron = true;
            if (eachUnitButton.isSelected()) {
                cronField.setText("*");
            } else if (intervalUnitsButton.isSelected()) {
                StringBuilder cron = new StringBuilder();
                if (beginCheck.isSelected())
                    cron.append(getValueFromIndex(beginCombo.getSelectedIndex()));
                if (endCheck.isSelected())
                    cron.append("-").append(getValueFromIndex(endCombo.getSelectedIndex()));
                if (!beginCheck.isSelected() && !endCheck.isSelected())
                    cron.append("*");
                cron.append("/").append(intervalCombo.getSelectedItem());
                cronField.setText(cron.toString());

            } else if (eachUnitBetweenButton.isSelected()) {
                cronField.setText(getValueFromIndex(betweenBeginCombo.getSelectedIndex()) + "-" + getValueFromIndex(betweenEndCombo.getSelectedIndex()));
            } else if (specificUnitsButton.isSelected()) {
                StringBuilder cron = new StringBuilder();
                boolean first = true;
                for (int i = 0; i < units.size(); i++) {
                    if (checkBoxPanel.getCheckBoxMap().get(units.get(i)).isSelected()) {
                        if (!first)
                            cron.append(",");
                        first = false;
                        cron.append(getValueFromIndex(i));
                    }

                }
                cronField.setText(cron.toString());
            }
            generatingCron = false;
        }
    }

    private void fillValuesFromField() {
        if (!generatingCron) {
            boolean validValue = true;
            fillValues = true;
            try {
                String cron = cronField.getText();
                if (cron.trim().equalsIgnoreCase("*"))
                    eachUnitButton.setSelected(true);
                else if (cron.contains(",") && !(cron.contains("-") || cron.contains("/")) || (!cron.contains(",") && !cron.contains("/") && !cron.contains("-"))) {
                    specificUnitsButton.setSelected(true);
                    String[] qs = cron.split(",");
                    for (String q : checkBoxPanel.getCheckBoxMap().keySet()) {
                        checkBoxPanel.getCheckBoxMap().get(q).setSelected(false);
                    }
                    for (String q : qs) {
                        int index = Integer.parseInt(q);
                        checkBoxPanel.getCheckBoxMap().get(units.get(index)).setSelected(true);
                    }

                } else if (cron.contains("/") && !cron.contains(",")) {
                    intervalUnitsButton.setSelected(true);
                    String interval = cron.substring(cron.indexOf("/") + 1);
                    intervalCombo.setSelectedIndex(Integer.parseInt(interval) - 1);
                    interval = cron.substring(0, cron.indexOf("/"));
                    if (interval.equalsIgnoreCase("*")) {
                        beginCheck.setSelected(false);
                        endCheck.setSelected(false);
                    } else if (interval.contains("-")) {
                        beginCheck.setSelected(true);
                        endCheck.setSelected(true);
                        String beginValue = interval.substring(0, cron.indexOf("-"));
                        String endValue = interval.substring(cron.indexOf("-") + 1);
                        int ind1 = Integer.parseInt(beginValue);
                        int ind2 = Integer.parseInt(endValue);
                        beginCombo.setSelectedIndex(getIndexFromValue(ind1));
                        endCombo.setSelectedIndex(getIndexFromValue(ind2));
                    } else {
                        beginCheck.setSelected(true);
                        int ind = Integer.parseInt(interval);
                        beginCombo.setSelectedItem(getIndexFromValue(ind));
                    }
                } else if (cron.contains("-") && !cron.contains(",")) {
                    eachUnitBetweenButton.setSelected(true);
                    String[] qs = cron.split("-");
                    if (qs.length > 2)
                        validValue = false;
                    betweenBeginCombo.setSelectedIndex(getIndexFromValue(Integer.parseInt(qs[0])));
                    betweenEndCombo.setSelectedIndex(getIndexFromValue(Integer.parseInt(qs[1])));
                } else {
                    useTextFieldButton.setSelected(true);
                }


            } catch (Exception e) {
                if (Log.isDebugEnabled())
                    e.printStackTrace();
                validValue = false;
            }
            if (!validValue)
                cronField.setBorder(BorderFactory.createLineBorder(Color.RED));
            else cronField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            fillValues = false;
        }
    }

    private String bundleString(String key) {
        return Bundles.get(getClass(), key);
    }

    private String bundleString(String key, Object... args) {
        return Bundles.get(getClass(), key, args);
    }

}

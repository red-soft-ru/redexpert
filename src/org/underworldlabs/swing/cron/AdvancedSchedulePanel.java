package org.underworldlabs.swing.cron;

import com.github.lgooddatepicker.zinternaltools.ExtraDateStrings;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.japura.gui.event.ListCheckListener;
import org.japura.gui.event.ListEvent;
import org.underworldlabs.swing.EQCheckCombox;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.text.DateFormatSymbols;
import java.util.Locale;
import java.util.Vector;
import java.util.stream.IntStream;

public class AdvancedSchedulePanel extends JTabbedPane {

    public static final int MINUTES = 0;
    public static final int HOURS = MINUTES + 1;
    public static final int DAYS = HOURS + 1;
    public static final int MONTHS = DAYS + 1;
    public static final int WEEKDAYS = MONTHS + 1;

    public static final String[] CRON_NAMES = {"minute", "hour", "day", "month", "weekday"};

    public AdvancedSchedulePanel(JTextField cronField) {
        super();

        for (int i = 0; i < CRON_NAMES.length; i++)
            addTab(bundleString(CRON_NAMES[i] + "s"), new CronTab(i, cronField));
    }

    private static class CronTab extends JPanel
            implements ListCheckListener,
            DocumentListener {

        private final int typeCronTab;
        private final JTextField cronField;

        private boolean fillValues;
        private boolean generatingCron;

        private JRadioButton eachUnitRadio;
        private JRadioButton intervalUnitRadio;
        private JRadioButton specificUnitRadio;
        private JRadioButton betweenUnitRadio;

        private JCheckBox endCheck;
        private JCheckBox beginCheck;

        private JComboBox<?> endCombo;
        private JComboBox<?> beginCombo;
        private JComboBox<?> intervalCombo;
        private JComboBox<?> betweenEndCombo;
        private JComboBox<?> betweenBeginCombo;
        private EQCheckCombox specificUnitComboCheck;

        private Vector<String> baseUnits;
        private Vector<Integer> intervalUnits;

        public CronTab(int typeCronTab, JTextField cronField) {

            this.typeCronTab = typeCronTab;
            this.cronField = cronField;
            this.fillValues = false;
            this.generatingCron = false;

            updateUnits();
            init();
            arrange();
            update();
        }

        private void init() {

            // --- radio buttons ---

            eachUnitRadio = WidgetFactory.createRadioButton("eachUnitRadio", bundleStringF("each_"), true);
            eachUnitRadio.addItemListener(this::update);

            betweenUnitRadio = WidgetFactory.createRadioButton("betweenUnitRadio", bundleStringF("each_between_"));
            betweenUnitRadio.addItemListener(this::update);

            String buttonText = String.format(bundleString("specific"), bundleString(CRON_NAMES[typeCronTab] + "s").split(" ")[0]);
            specificUnitRadio = WidgetFactory.createRadioButton("specificUnitRadio", buttonText);
            specificUnitRadio.addItemListener(this::update);

            intervalUnitRadio = WidgetFactory.createRadioButton("intervalUnitRadio", bundleString("interval"));
            intervalUnitRadio.addItemListener(this::update);

            ButtonGroup radioButtons = new ButtonGroup();
            radioButtons.add(eachUnitRadio);
            radioButtons.add(betweenUnitRadio);
            radioButtons.add(intervalUnitRadio);
            radioButtons.add(specificUnitRadio);

            // --- check boxes ---

            beginCheck = WidgetFactory.createCheckBox("beginCheck", bundleString("begin_with"));
            beginCheck.addItemListener(this::update);

            endCheck = WidgetFactory.createCheckBox("endCheck", bundleString("end_with"));
            endCheck.addItemListener(this::update);

            // --- combo boxes ---

            int width = typeCronTab < MONTHS ? 50 : 110;

            endCombo = WidgetFactory.createComboBox("endCombo", baseUnits, this::update);
            endCombo.setPreferredSize(new Dimension(width, endCombo.getPreferredSize().height));

            beginCombo = WidgetFactory.createComboBox("beginCombo", baseUnits, this::update);
            beginCombo.setPreferredSize(new Dimension(width, beginCombo.getPreferredSize().height));

            intervalCombo = WidgetFactory.createComboBox("intervalCombo", intervalUnits, this::update);
            intervalCombo.setPreferredSize(new Dimension(50, intervalCombo.getPreferredSize().height));

            betweenEndCombo = WidgetFactory.createComboBox("betweenEndCombo", baseUnits, this::update);
            betweenEndCombo.setPreferredSize(new Dimension(width, betweenEndCombo.getPreferredSize().height));

            betweenBeginCombo = WidgetFactory.createComboBox("betweenBeginCombo", baseUnits, this::update);
            betweenBeginCombo.setPreferredSize(new Dimension(width, betweenBeginCombo.getPreferredSize().height));

            // --- check-combo box ---

            specificUnitComboCheck = WidgetFactory.createCheckComboBox("specificUnitComboCheck", baseUnits.toArray());
            specificUnitComboCheck.setPreferredSize(new Dimension(width + 30, specificUnitComboCheck.getPreferredSize().height));
            specificUnitComboCheck.getModel().addListCheckListener(this);

            // ---

            cronField.getDocument().addDocumentListener(this);
        }

        private void arrange() {
            GridBagHelper gbh;

            // --- interval selector panel ---

            JPanel intervalSelectorPanel = new JPanel(new GridBagLayout());
            JLabel label = new JLabel(bundleString(CRON_NAMES[typeCronTab] + "/s"));

            gbh = new GridBagHelper().anchorNorthWest().rightGap(5);
            intervalSelectorPanel.add(intervalUnitRadio, gbh.get());
            intervalSelectorPanel.add(intervalCombo, gbh.nextCol().get());
            intervalSelectorPanel.add(label, gbh.nextCol().topGap(4).get());

            // --- base ---

            setLayout(new GridBagLayout());

            gbh = new GridBagHelper().setInsets(5, 5, 5, 5).anchorNorthWest().fillHorizontally();
            add(eachUnitRadio, gbh.get());

            add(intervalSelectorPanel, gbh.nextRowFirstCol().setMinWeightX().get());
            add(beginCheck, gbh.nextCol().get());
            add(beginCombo, gbh.nextCol().setWeightX(0.3).get());
            add(endCheck, gbh.nextCol().setMinWeightX().get());
            add(endCombo, gbh.nextCol().setWeightX(0.3).get());

            add(betweenUnitRadio, gbh.nextRowFirstCol().setMinWeightX().get());
            add(betweenBeginCombo, gbh.nextCol().setWidth(2).setWeightX(0.3).get());
            add(new JLabel(bundleString("and"), SwingConstants.CENTER), gbh.nextCol().setWidth(1).setMinWeightX().topGap(9).get());
            add(betweenEndCombo, gbh.nextCol().setWeightX(0.3).topGap(5).get());

            add(specificUnitRadio, gbh.nextRowFirstCol().setMinWeightX().spanY().get());
            add(specificUnitComboCheck, gbh.nextCol().setWidth(4).setWeightX(0.3).get());
        }

        private void generateCron() {
            if (!fillValues) {
                generatingCron = true;
                String[] crons = cronField.getText().split(" ");
                if (crons.length != 5)
                    crons = new String[]{"*", "*", "*", "*", "*"};
                String cronRes = crons[typeCronTab];
                if (eachUnitRadio.isSelected()) {
                    cronRes = "*";
                } else if (intervalUnitRadio.isSelected()) {
                    StringBuilder cron = new StringBuilder();
                    if (beginCheck.isSelected())
                        cron.append(getValueFromIndex(beginCombo.getSelectedIndex()));
                    if (endCheck.isSelected())
                        cron.append("-").append(getValueFromIndex(endCombo.getSelectedIndex()));
                    if (!beginCheck.isSelected() && !endCheck.isSelected())
                        cron.append("*");
                    cron.append("/").append(intervalCombo.getSelectedItem());
                    cronRes = cron.toString();

                } else if (betweenUnitRadio.isSelected()) {
                    cronRes = getValueFromIndex(betweenBeginCombo.getSelectedIndex()) + "-" + getValueFromIndex(betweenEndCombo.getSelectedIndex());
                } else if (specificUnitRadio.isSelected()) {
                    StringBuilder cron = new StringBuilder();
                    boolean first = true;
                    for (int i = 0; i < baseUnits.size(); i++) {
                        if (specificUnitComboCheck.getModel().isChecked(baseUnits.get(i))) {
                            if (!first)
                                cron.append(",");
                            first = false;
                            cron.append(getValueFromIndex(i));
                        }

                    }
                    cronRes = cron.toString();
                    if (MiscUtils.isNull(cronRes))
                        cronRes = "?";
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < crons.length; i++) {
                    if (i != 0)
                        sb.append(" ");
                    if (i == typeCronTab)
                        sb.append(cronRes);
                    else sb.append(crons[i]);
                }
                cronField.setText(sb.toString());
                generatingCron = false;
            }
        }

        private void fillValuesFromField() {

            if (generatingCron)
                return;

            fillValues = true;
            boolean validValue = true;
            try {

                String[] crons = cronField.getText().split(" ");
                if (crons.length != 5)
                    throw new Exception("crons.length != 5");

                String cron = crons[typeCronTab];
                if (cron.trim().equalsIgnoreCase("*")) {
                    eachUnitRadio.setSelected(true);

                } else if (cron.contains(",") && !(cron.contains("-")
                        || cron.contains("/"))
                        || (!cron.contains(",") && !cron.contains("/") && !cron.contains("-"))
                ) {
                    specificUnitRadio.setSelected(true);
                    specificUnitComboCheck.getModel().removeChecks();

                    String[] values = cron.split(",");
                    for (String q : values) {
                        int index = getIndexFromValue(Integer.parseInt(q));
                        specificUnitComboCheck.getModel().addCheck(baseUnits.get(index));
                    }

                } else if (cron.contains("/") && !cron.contains(",")) {
                    String interval = cron.substring(cron.indexOf("/") + 1);

                    intervalUnitRadio.setSelected(true);
                    intervalCombo.setSelectedIndex(Integer.parseInt(interval) - 1);

                    interval = cron.substring(0, cron.indexOf("/"));
                    if (interval.equalsIgnoreCase("*")) {
                        beginCheck.setSelected(false);
                        endCheck.setSelected(false);

                    } else if (interval.contains("-")) {
                        String beginValue = interval.substring(0, cron.indexOf("-"));
                        String endValue = interval.substring(cron.indexOf("-") + 1);

                        beginCheck.setSelected(true);
                        beginCombo.setSelectedIndex(getIndexFromValue(Integer.parseInt(beginValue)));
                        endCheck.setSelected(true);
                        endCombo.setSelectedIndex(getIndexFromValue(Integer.parseInt(endValue)));

                    } else {
                        beginCheck.setSelected(true);
                        beginCombo.setSelectedItem(getIndexFromValue(Integer.parseInt(interval)));
                    }

                } else if (cron.contains("-") && !cron.contains(",")) {
                    String[] range = cron.split("-");
                    if (range.length > 2)
                        validValue = false;

                    betweenUnitRadio.setSelected(true);
                    betweenBeginCombo.setSelectedIndex(getIndexFromValue(Integer.parseInt(range[0])));
                    betweenEndCombo.setSelectedIndex(getIndexFromValue(Integer.parseInt(range[1])));
                }

            } catch (Exception e) {
                Log.debug(e.getMessage(), e);
                validValue = false;
            }

            cronField.setBorder(BorderFactory.createLineBorder(validValue ? Color.BLACK : Color.RED));
            fillValues = false;
        }

        protected void update() {
            checkEnabled();
            generateCron();
        }

        private void updateUnits() {

            baseUnits = new Vector<>();
            intervalUnits = new Vector<>();

            Object[] units = new Object[0];
            switch (typeCronTab) {
                case MINUTES:
                    units = IntStream.range(0, 60).mapToObj(Integer::toString).toArray();
                    break;
                case HOURS:
                    units = IntStream.range(0, 24).mapToObj(Integer::toString).toArray();
                    break;
                case DAYS:
                    units = IntStream.range(1, 32).mapToObj(Integer::toString).toArray();
                    break;
                case MONTHS:
                    units = ExtraDateStrings.getDefaultStandaloneLongMonthNamesForLocale(Locale.getDefault());
                    break;
                case WEEKDAYS:
                    units = new Object[7];
                    System.arraycopy(new DateFormatSymbols(Locale.getDefault()).getWeekdays(), 1, units, 0, 7);
            }

            for (int i = 0; i < units.length; i++) {
                baseUnits.add(units[i].toString());
                intervalUnits.add(i + 1);
            }
        }

        private void update(ItemEvent e) {

            if (e.getSource() == endCheck && endCheck.isSelected())
                beginCheck.setSelected(true);

            if (e.getSource() == beginCheck && !beginCheck.isSelected())
                endCheck.setSelected(false);

            update();
        }

        private void checkEnabled() {

            endCheck.setEnabled(intervalUnitRadio.isSelected());
            beginCheck.setEnabled(intervalUnitRadio.isSelected());
            intervalCombo.setEnabled(intervalUnitRadio.isSelected());

            endCombo.setEnabled(endCheck.isSelected() && intervalUnitRadio.isSelected());
            beginCombo.setEnabled(beginCheck.isSelected() && intervalUnitRadio.isSelected());

            betweenEndCombo.setEnabled(betweenUnitRadio.isSelected());
            betweenBeginCombo.setEnabled(betweenUnitRadio.isSelected());

            specificUnitComboCheck.setEnabled(specificUnitRadio.isSelected());
        }

        private int getValueFromIndex(int index) {
            if (typeCronTab == MINUTES || typeCronTab == HOURS || typeCronTab == WEEKDAYS)
                return index;
            return index + 1;
        }

        private int getIndexFromValue(int value) {
            if (typeCronTab == MINUTES || typeCronTab == HOURS || typeCronTab == WEEKDAYS)
                return value;
            return value - 1;
        }

        private String bundleStringF(String key) {
            return bundleString(key + CRON_NAMES[typeCronTab]);
        }

        // --- ListCheckListener impl ---

        @Override
        public void removeCheck(ListEvent listEvent) {
            update();
        }

        @Override
        public void addCheck(ListEvent listEvent) {
            update();
        }

        // --- DocumentListener impl --

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

    } // CronTab class

    public void update() {
        ((CronTab) getSelectedComponent()).update();
    }

    private static String bundleString(String key) {
        return Bundles.get(CronTab.class, key);
    }

}

package org.underworldlabs.swing.cron;

import com.github.lgooddatepicker.zinternaltools.ExtraDateStrings;
import org.executequery.localization.Bundles;
import org.japura.gui.event.ListCheckListener;
import org.japura.gui.event.ListEvent;
import org.underworldlabs.swing.CheckBoxPanel;
import org.underworldlabs.swing.EQCheckCombox;
import org.underworldlabs.swing.celleditor.picker.DefaultDatePicker;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.util.List;
import java.util.*;

public class WindowsSchedulerPanel extends JPanel {
    ButtonGroup radioButtons;

    JRadioButton oneTimeButton;
    JRadioButton everyDayButton;
    JRadioButton everyWeekDayButton;
    JRadioButton everyMonthButton;
    DefaultDatePicker datePicker;
    JSpinner timeSpinner;
    JSpinner.DateEditor timeEditor;
    EQCheckCombox monthCheckBox;
    EQCheckCombox daysCheckBox;
    CheckBoxPanel weekDaysPanel;

    NumberTextField intervalTextField;
    JLabel everyLabel;
    JLabel unitLabel;

    JCheckBox repeatCheckBox;
    ItemListener itemListener;
    JComboBox intervalBox;
    List<String> weekdays;
    private JTextField cronField;
    public WindowsSchedulerPanel(JTextField cronField)
    {
        this.cronField=cronField;
        init();
    }
    private void init()
    {
        itemListener=new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                checkVisible();
                generateCron();
            }
        };
        radioButtons = new ButtonGroup();
        oneTimeButton = new JRadioButton(bundleString("once"));
        oneTimeButton.addItemListener(itemListener);
        radioButtons.add(oneTimeButton);
        everyDayButton = new JRadioButton(bundleString("EveryDay"));
        everyDayButton.addItemListener(itemListener);
        radioButtons.add(everyDayButton);
        everyWeekDayButton = new JRadioButton(bundleString("EveryWeek"));
        everyWeekDayButton.addItemListener(itemListener);
        radioButtons.add(everyWeekDayButton);
        everyMonthButton = new JRadioButton(bundleString("EveryMonth"));
        everyMonthButton.addItemListener(itemListener);
        radioButtons.add(everyMonthButton);

        intervalTextField = new NumberTextField();
        intervalTextField.setText("1");
        intervalTextField.setColumns(2);
        intervalTextField.getDocument().addDocumentListener(new DocumentListener() {
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
        });

        timeSpinner = new JSpinner(new SpinnerDateModel());
        timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                generateCron();
            }
        });
        datePicker = new DefaultDatePicker();
        datePicker.getComponentDateTextField().getDocument().addDocumentListener(new DocumentListener() {
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
        });
        weekdays=new ArrayList<>();
        Collections.addAll(weekdays, new DateFormatSymbols(Locale.getDefault()).getWeekdays());
        weekdays.remove(0);
        weekDaysPanel = new CheckBoxPanel(weekdays.toArray(new String[0]), 4, false);
        for(JCheckBox checkBox:weekDaysPanel.getCheckBoxMap().values())
        {
            checkBox.addItemListener(itemListener);
        }
        weekDaysPanel.setVisibleAllCheckBox(false);
        monthCheckBox = new EQCheckCombox();
        for(String month: ExtraDateStrings.getDefaultStandaloneLongMonthNamesForLocale(Locale.getDefault()))
            monthCheckBox.getModel().addElement(month);
        monthCheckBox.getModel().addListCheckListener(new ListCheckListener() {
            @Override
            public void removeCheck(ListEvent listEvent) {
                generateCron();
            }

            @Override
            public void addCheck(ListEvent listEvent) {
                generateCron();
            }
        });
        daysCheckBox = new EQCheckCombox();
        for(int i=1;i<=31;i++)
            daysCheckBox.getModel().addElement(i);
        daysCheckBox.getModel().addListCheckListener(new ListCheckListener() {
            @Override
            public void removeCheck(ListEvent listEvent) {
                generateCron();
            }

            @Override
            public void addCheck(ListEvent listEvent) {
                generateCron();
            }
        });
        everyLabel=new JLabel(bundleString("Every"));
        unitLabel=new JLabel(bundleString("Day/s"));
        repeatCheckBox = new JCheckBox(bundleString("repeat"));
        repeatCheckBox.addItemListener(itemListener);
        intervalBox = new JComboBox();
        intervalBox.addItem(bundleString("min",5));
        intervalBox.addItem(bundleString("min",10));
        intervalBox.addItem(bundleString("min",15));
        intervalBox.addItem(bundleString("min",30));
        intervalBox.addItem(bundleString("h",1));
        intervalBox.addItemListener(itemListener);

        setLayout(new GridBagLayout());
        GridBagHelper gbh = new GridBagHelper().setDefaultsStatic();
        gbh.defaults();

        add(oneTimeButton, gbh.fillHorizontally().setLabelDefault().get());
        add(everyDayButton, gbh.nextRowFirstCol().setLabelDefault().get());
        add(everyWeekDayButton, gbh.nextRowFirstCol().setLabelDefault().get());
        add(everyMonthButton, gbh.nextRowFirstCol().setLabelDefault().get());
        add(new JSeparator(SwingConstants.VERTICAL),gbh.nextCol().setY(0).setHeight(4).setWidth(1).fillVertical().get());
        add(timeSpinner,gbh.nextCol().setLabelDefault().fillHorizontally().setWidth(3).setMaxWeightX().makeCurrentXTheDefaultForNewline().get());
        add(datePicker,gbh.nextCol().setWidth(1).spanX().get());
        add(everyLabel,gbh.nextRowFirstCol().setLabelDefault().get());
        add(intervalTextField,gbh.nextCol().setLabelDefault().get());
        add(unitLabel,gbh.nextCol().setLabelDefault().get());
        add(weekDaysPanel,gbh.nextRowFirstCol().fillBoth().spanX().setHeight(2).get());
        gbh.setY(0);
        gbh.addLabelFieldPair(this,bundleString("Months"),monthCheckBox,null);
        gbh.addLabelFieldPair(this,bundleString("Days"),daysCheckBox,null);
        gbh.setXY(0,4).makeCurrentXTheDefaultForNewline();
        add(repeatCheckBox,gbh.setLabelDefault().setWidth(2).get());
        add(intervalBox,gbh.nextCol().setLabelDefault().get());
        add(new JPanel(),gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
        oneTimeButton.setSelected(true);
    }

    private void checkVisible(){
        datePicker.setVisible(oneTimeButton.isSelected());
        everyLabel.setVisible(everyDayButton.isSelected()||everyWeekDayButton.isSelected());
        intervalTextField.setVisible(everyDayButton.isSelected()||everyWeekDayButton.isSelected());
        unitLabel.setVisible(everyDayButton.isSelected()||everyWeekDayButton.isSelected());
        if(everyDayButton.isSelected())
            unitLabel.setText(bundleString("Day/s"));
        if(everyWeekDayButton.isSelected())
            unitLabel.setText(bundleString("Week/s"));
        weekDaysPanel.setVisible(everyWeekDayButton.isSelected());
        monthCheckBox.setVisible(everyMonthButton.isSelected());
        this.getComponent(this.getComponentZOrder(monthCheckBox)-1).setVisible(everyMonthButton.isSelected());
        daysCheckBox.setVisible(everyMonthButton.isSelected());
        this.getComponent(this.getComponentZOrder(daysCheckBox)-1).setVisible(everyMonthButton.isSelected());
        intervalBox.setEnabled(repeatCheckBox.isSelected());
    }
    private void generateCron(){
        String[] crons = new String[5];
        crons[0]=((Date)timeSpinner.getModel().getValue()).getMinutes()+"";
        crons[1]=((Date)timeSpinner.getModel().getValue()).getHours()+"";
        if(oneTimeButton.isSelected())
        {
            LocalDate localDate =datePicker.getDate();
            if(localDate!=null) {
                crons[2] = localDate.getDayOfMonth() + "";
                crons[3] = localDate.getMonthValue() + "";
            }
            crons[4]="*";
        }
        if(everyDayButton.isSelected())
        {
            crons[2]="*/"+intervalTextField.getStringValue();
            crons[3]="*";
            crons[4]="*";
        }
        if(everyWeekDayButton.isSelected())
        {
            crons[2]="*";
            crons[3]="*";
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for(int i=0;i<weekDaysPanel.getCheckBoxMap().size();i++)
            {
                if(weekDaysPanel.getCheckBoxMap().get(weekdays.get(i)).isSelected()) {
                    if (!first)
                        sb.append(",");
                    first=false;
                    sb.append(i);
                }
            }
            crons[4]=sb.toString();
            if(MiscUtils.isNull(crons[4]))
                crons[4]="*";
            crons[4]=crons[4]+"/"+intervalTextField.getStringValue();
        }
        if(everyMonthButton.isSelected())
        {
            String[] months = ExtraDateStrings.getDefaultStandaloneLongMonthNamesForLocale(Locale.getDefault());
            StringBuilder cron = new StringBuilder();
            boolean first = true;
            for(int i=0;i<months.length;i++)
            {

                if(monthCheckBox.getModel().isChecked(months[i]))
                {
                        if (!first)
                            cron.append(",");
                        first = false;
                        cron.append(i+1);
                }
            }
            crons[3]=cron.toString();

            if(MiscUtils.isNull(crons[3]))
            {
                crons[3]="*";
            }
            cron=new StringBuilder();
            first=true;
            for(int i=1;i<=31;i++)
            {

                if(daysCheckBox.getModel().isChecked(i))
                {
                    if (!first)
                        cron.append(",");
                    first = false;
                    cron.append(i);
                }
            }
            crons[2]=cron.toString();
            if(MiscUtils.isNull(crons[2]))
            {
                crons[2]="*";
            }
        }
        if(repeatCheckBox.isSelected())
        {
            switch (intervalBox.getSelectedIndex())
            {
                case 0:
                    crons[0]+="/5";
                    break;
                case 1:
                    crons[0]+="/10";
                    break;
                case 2:
                    crons[0]+="/15";
                    break;
                case 3:
                    crons[0]+="/30";
                    break;
                case 4:
                    crons[1]+="/1";
                    break;
            }

        }
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<crons.length;i++) {
            if (crons[i] == null)
                crons[i] = "*";
            sb.append(crons[i]).append(" ");
        }
        cronField.setText(sb.toString());
    }

    private String bundleString(String key,Object... args)
    {
        return Bundles.get(WindowsSchedulerPanel.class,key,args);
    }
}

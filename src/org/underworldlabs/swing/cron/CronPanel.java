package org.underworldlabs.swing.cron;

import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;

public class CronPanel extends JPanel {

    private JTextField cronField;
    private JCheckBox advancedCheck;
    private WindowsSchedulerPanel windowsSchedulerPanel;
    private AdvancedSchedulePanel advancedSchedulePanel;

    public CronPanel(boolean advanced) {
        init(advanced);
        arrange();
    }

    private void init(boolean advanced) {

        // --- crone textField ---

        cronField = WidgetFactory.createTextField("cronField", "* * * * *");
        cronField.setMinimumSize(new Dimension(cronField.getMinimumSize().width, cronField.getPreferredSize().height));

        // --- schedule panels ---

        windowsSchedulerPanel = new WindowsSchedulerPanel(cronField);
        windowsSchedulerPanel.setVisible(!advanced);

        advancedSchedulePanel = new AdvancedSchedulePanel(cronField);
        advancedSchedulePanel.setVisible(advanced);

        // --- advanced checkBox ---

        advancedCheck = WidgetFactory.createCheckBox("advancedCheck", Bundles.getCommon("advanced"));
        advancedCheck.addItemListener(e -> toggleShedulePanels());
        advancedCheck.setSelected(advanced);
        advancedCheck.setEnabled(!advanced);
    }

    private void arrange() {
        setLayout(new GridBagLayout());

        GridBagHelper gbh = new GridBagHelper().setInsets(10, 14, 0, 5).fillHorizontally().anchorNorthWest();
        add(new JLabel("Cron"), gbh.setMinWeightX().rightGap(0).get());
        add(cronField, gbh.nextCol().setMaxWeightX().leftGap(5).topGap(10).rightGap(5).get());
        add(advancedCheck, gbh.nextCol().rightGap(10).setMinWeightX().get());
        add(advancedSchedulePanel, gbh.nextRowFirstCol().topGap(5).bottomGap(10).fillBoth().spanX().spanY().get());
        add(windowsSchedulerPanel, gbh.get());
    }

    private void toggleShedulePanels() {
        advancedSchedulePanel.setVisible(advancedCheck.isSelected());
        windowsSchedulerPanel.setVisible(!advancedCheck.isSelected());
    }

    public String getCronString() {
        return cronField.getText();
    }

    public void setCronString(String value) {
        cronField.setText(value);
    }

}

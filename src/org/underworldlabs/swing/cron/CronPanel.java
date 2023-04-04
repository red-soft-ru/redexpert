package org.underworldlabs.swing.cron;

import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class CronPanel extends JPanel {
   private JTextField cronField;
   private JTabbedPane tabbedPane;
   private WindowsSchedulerPanel windowsSchedulerPanel;
   private JCheckBox advancedCheckBox;
   private boolean advanced;

   public CronPanel(boolean advanced) {
      this.advanced=advanced;
      init();
   }

   private void init() {
      tabbedPane = new JTabbedPane();
      tabbedPane.setTabPlacement(SwingConstants.LEFT);
      cronField = new JTextField("* * * * *");
      cronField.setColumns(15);
      windowsSchedulerPanel = new WindowsSchedulerPanel(cronField);
      advancedCheckBox = new JCheckBox(Bundles.getCommon("Advanced"));
      advancedCheckBox.addItemListener(new ItemListener() {
         @Override
         public void itemStateChanged(ItemEvent e) {
            tabbedPane.setVisible(advancedCheckBox.isSelected());
            windowsSchedulerPanel.setVisible(!advancedCheckBox.isSelected());
         }
      });
      setLayout(new GridBagLayout());
      GridBagHelper gbh = new GridBagHelper();
      gbh.setDefaultsStatic();
      gbh.defaults();
      gbh.addLabelFieldPair(this, "Cron", cronField, null, true, false);
      if(!advanced)
      add(advancedCheckBox,gbh.setLabelDefault().nextCol().get());

      for (int i = 0; i < CronTab.CRON_NAMES.length; i++) {
         tabbedPane.addTab(Bundles.get(CronTab.class, CronTab.CRON_NAMES[i] + "s"), new CronTab(i, cronField));
      }

      add(tabbedPane, gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
      if(!advanced)
      add(windowsSchedulerPanel, gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
      tabbedPane.setVisible(advanced);
   }

   public String getCron() {
      return cronField.getText();
   }

   public void setCron(String cron) {
      cronField.setText(cron);
   }
}


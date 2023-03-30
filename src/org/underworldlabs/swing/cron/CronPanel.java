package org.underworldlabs.swing.cron;

import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;

public class CronPanel extends JPanel {
   private JTextField cronField;
   private JTabbedPane tabbedPane;

   public CronPanel() {
      init();
   }

   private void init() {
      tabbedPane = new JTabbedPane();
      tabbedPane.setTabPlacement(SwingConstants.LEFT);
      cronField = new JTextField("* * * * *");
      setLayout(new GridBagLayout());
      GridBagHelper gbh = new GridBagHelper();
      gbh.setDefaultsStatic();
      gbh.defaults();
      gbh.addLabelFieldPair(this, "Cron", cronField, null, true, false);
      for (int i = 0; i < CronTab.CRON_NAMES.length; i++) {
         tabbedPane.addTab(Bundles.get(CronTab.class, CronTab.CRON_NAMES[i] + "s"), new CronTab(i, cronField));
      }
      add(tabbedPane, gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
   }

   public String getCron() {
      return cronField.getText();
   }

   public void setCron(String cron) {
      cronField.setText(cron);
   }
}


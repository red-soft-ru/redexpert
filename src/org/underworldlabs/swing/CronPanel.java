package org.underworldlabs.swing;

import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CronPanel extends JPanel {
   private List<JTextField> cronFields;
   private JTabbedPane tabbedPane;

   public CronPanel() {
      init();
   }

   private void init() {
      tabbedPane = new JTabbedPane();
      tabbedPane.setTabPlacement(SwingConstants.LEFT);
      cronFields = new ArrayList<>();
      setLayout(new GridBagLayout());
      GridBagHelper gbh = new GridBagHelper();
      gbh.setDefaultsStatic();
      gbh.defaults();

      for (int i = 0; i < CronTab.CRON_NAMES.length; i++) {
         JTextField cronField = new JTextField();
         cronField.setText("*");
         cronFields.add(cronField);
         tabbedPane.addTab(Bundles.get(CronTab.class, CronTab.CRON_NAMES[i] + "s"), new CronTab(i, cronField));
         gbh.addLabelFieldPair(this, Bundles.get(CronTab.class, CronTab.CRON_NAMES[i] + "s"), cronField, null, i == 0, false);
      }
      add(tabbedPane, gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
   }

   public String getCron() {
      StringBuilder sb = new StringBuilder();
      for (JTextField cronField : cronFields) {
         sb.append(cronField.getText()).append(" ");
      }
      return sb.toString().trim();
   }

   public void setCron(String cron) {
      String[] crons = cron.split(" ");
      for (int i = 0; i < cronFields.size(); i++) {
         crons[i] = crons[i].trim();
         cronFields.get(i).setText(crons[i]);
      }
   }
}


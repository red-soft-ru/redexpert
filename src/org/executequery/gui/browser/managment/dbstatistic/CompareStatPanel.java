package org.executequery.gui.browser.managment.dbstatistic;

import org.executequery.gui.browser.managment.AbstractServiceManagerPanel;
import org.executequery.gui.text.DifferenceTextPanel;
import org.underworldlabs.statParser.StatDatabase;
import org.underworldlabs.swing.AbstractPanel;
import org.underworldlabs.swing.util.SwingWorker;

import javax.swing.*;

public class CompareStatPanel extends AbstractPanel {
    CompareStatisticTablePanel tablesPanel;
    CompareStatisticTablePanel indexesPanel;
    CompareStatisticTablePanel tablespacesPanel;
    DifferenceTextPanel textPanel;
    StatDatabase db;
    JTabbedPane tabPane = new JTabbedPane();

    public CompareStatPanel(StatDatabase db, StatDatabase db1, StatDatabase db2) {
        super();
        this.db = db;
        SwingWorker sw = new SwingWorker("compareStatText") {
            @Override
            public Object construct() {
                textPanel.setTexts(db2.sb.toString(), db1.sb.toString());
                return null;
            }
        };
        sw.start();
        tablesPanel.setRows(db.tables);
        indexesPanel.setRows(db.indices);
        tablespacesPanel.setRows(db.tablespaces);
    }

    @Override
    protected void initComponents() {
        textPanel = new DifferenceTextPanel("new", "old");
        tablesPanel = new CompareStatisticTablePanel();
        tablesPanel.initModel(StatisticTablePanel.TABLE);
        indexesPanel = new CompareStatisticTablePanel();
        indexesPanel.initModel(StatisticTablePanel.INDEX);
        tablespacesPanel = new CompareStatisticTablePanel();
        tablespacesPanel.initModel(StatisticTablePanel.TABLESPACE);
        tabPane = new JTabbedPane();
        tabPane.add(AbstractServiceManagerPanel.bundleString("tabText"), textPanel);
        tabPane.add(AbstractServiceManagerPanel.bundleString("tables"), tablesPanel);
        tabPane.add(AbstractServiceManagerPanel.bundleString("indices"), indexesPanel);
        tabPane.add(AbstractServiceManagerPanel.bundleString("tablespaces"), tablespacesPanel);
    }

    @Override
    protected void arrangeComponents() {
        add(tabPane, gbh.fillBoth().spanX().spanY().get());
    }

    @Override
    protected void postInitActions() {

    }
}

package org.executequery.gui.browser.managment.dbstatistic;

import org.executequery.gui.browser.managment.AbstractServiceManagerPanel;
import org.executequery.gui.text.DifferenceTextPanel;
import org.underworldlabs.statParser.StatDatabase;
import org.underworldlabs.swing.AbstractPanel;

import javax.swing.*;

public class CompareStatPanel extends AbstractPanel {
    CompareStatisticTablePanel tablesPanel;
    CompareStatisticTablePanel indexesPanel;
    DifferenceTextPanel textPanel;
    StatDatabase db;
    JTabbedPane tabPane = new JTabbedPane();

    public CompareStatPanel(StatDatabase db, StatDatabase db1, StatDatabase db2) {
        super();
        this.db = db;
        textPanel.setTexts(db2.sb.toString(), db1.sb.toString());
        tablesPanel.setRows(db.tables);
        indexesPanel.setRows(db.indices);
    }

    @Override
    protected void initComponents() {
        textPanel = new DifferenceTextPanel("new", "old");
        tablesPanel = new CompareStatisticTablePanel();
        tablesPanel.initModel(StatisticTablePanel.TABLE);
        indexesPanel = new CompareStatisticTablePanel();
        indexesPanel.initModel(StatisticTablePanel.INDEX);
        tabPane = new JTabbedPane();
        tabPane.add(AbstractServiceManagerPanel.bundleString("tabText"), textPanel);
        tabPane.add(AbstractServiceManagerPanel.bundleString("tables"), tablesPanel);
        tabPane.add(AbstractServiceManagerPanel.bundleString("indices"), indexesPanel);
    }

    @Override
    protected void arrangeComponents() {
        add(tabPane, gbh.fillBoth().spanX().spanY().get());
    }

    @Override
    protected void postInitActions() {

    }
}

package org.executequery.gui.browser.managment.dbstatistic;

import org.executequery.gui.browser.managment.AbstractServiceManagerPanel;
import org.executequery.gui.text.SimpleTextArea;
import org.underworldlabs.statParser.StatDatabase;
import org.underworldlabs.swing.AbstractPanel;

import javax.swing.*;

public class DbStatPanel extends AbstractPanel {
    StatisticTablePanel tablesPanel;
    StatisticTablePanel indexesPanel;
    SimpleTextArea textPanel;
    StatDatabase db;
    JTabbedPane tabPane = new JTabbedPane();

    public DbStatPanel(StatDatabase db) {
        super();
        this.db = db;
        textPanel.getTextAreaComponent().setText(db.sb.toString());
        tablesPanel.setRows(db.tables);
        indexesPanel.setRows(db.indices);
    }

    @Override
    protected void initComponents() {
        textPanel = new SimpleTextArea();
        tablesPanel = new StatisticTablePanel();
        tablesPanel.initModel(StatisticTablePanel.TABLE);
        indexesPanel = new StatisticTablePanel();
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

package org.executequery.gui.browser;

import org.executequery.databaseobjects.impl.DefaultDatabaseTablespace;
import org.executequery.gui.browser.depend.DependPanel;
import org.executequery.gui.browser.tree.TreePanel;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.DisabledField;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.Printable;

public class BrowserTablespacePanel extends AbstractFormObjectViewPanel {

    public static final String NAME = "BrowserTablespacePanel";
    private final BrowserController controller;

    public BrowserTablespacePanel(BrowserController controller) {
        super();
        this.controller = controller;

        try {
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void init() {
        setLayout(new GridBagLayout());
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaults(new GridBagConstraints(
                0, 0, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(2, 2, 2, 2), 0, 0));
        gbh.defaults();
        gbh.insertEmptyRow(this, 5);
        add(editButton, gbh.setLabelDefault().get());
        gbh.nextRowFirstCol();
        gbh.addLabelFieldPair(this, Bundles.getCommon("name"), new DisabledField(tablespace.getName()), null);
        gbh.addLabelFieldPair(this, Bundles.getCommon("file"), new DisabledField(tablespace.getFileName()), null);
        gbh.nextRowFirstCol();
        JCheckBox offlineBox = new JCheckBox("Offline");
        offlineBox.setSelected(Boolean.parseBoolean(tablespace.getAttribute(DefaultDatabaseTablespace.OFFLINE).trim()));
        offlineBox.setEnabled(false);
        add(offlineBox, gbh.setLabelDefault().get());
        JCheckBox readOnlyBox = new JCheckBox("Read-only");
        readOnlyBox.setSelected(Boolean.parseBoolean(tablespace.getAttribute(DefaultDatabaseTablespace.READ_ONLY).trim()));
        readOnlyBox.setEnabled(false);
        add(readOnlyBox, gbh.nextCol().setLabelDefault().get());
        JTabbedPane tabPane = new JTabbedPane();
        DependPanel tablesIndexesPanel = new DependPanel(TreePanel.TABLESPACE);
        tablesIndexesPanel.setDatabaseObject(tablespace);
        tablesIndexesPanel.setDatabaseConnection(tablespace.getHost().getDatabaseConnection());
        tabPane.add(Bundles.getCommon("contents"), new JScrollPane(tablesIndexesPanel));
        SimpleSqlTextPanel descPanel = new SimpleSqlTextPanel();
        descPanel.setSQLText(tablespace.getRemarks());
        tabPane.add(Bundles.getCommon("description"), descPanel);
        SimpleSqlTextPanel sqlPanel = new SimpleSqlTextPanel();
        sqlPanel.setSQLText(tablespace.getCreateFullSQLText());
        tabPane.add(Bundles.getCommon("SQL"), sqlPanel);
        add(tabPane, gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());

    }

    private DefaultDatabaseTablespace tablespace;

    public void setValues(DefaultDatabaseTablespace tablespace) {
        this.tablespace = tablespace;
        init();
    }

    @Override
    public void cleanup() {

    }

    @Override
    public Printable getPrintable() {
        return null;
    }

    @Override
    public String getLayoutName() {
        return null;
    }

    class TablespaceModel extends DefaultTableModel {
        @Override
        public String getColumnName(int column) {
            return DefaultDatabaseTablespace.COLUMNS[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            return tablespace.getAttribute(column);
        }

        @Override
        public int getColumnCount() {
            return DefaultDatabaseTablespace.COLUMNS.length;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public int getRowCount() {
            return 2;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }
    }
}

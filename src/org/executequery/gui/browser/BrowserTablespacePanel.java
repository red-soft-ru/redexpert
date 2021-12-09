package org.executequery.gui.browser;

import org.executequery.databaseobjects.impl.DefaultDatabaseTablespace;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.browser.depend.DependPanel;
import org.executequery.gui.browser.tree.TreePanel;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
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
        gbh.insertEmptyRow(this, 5);
        gbh.addLabelFieldPair(this, Bundles.getCommon("name"), new DisabledField(tablespace.getName()), null);
        gbh.insertEmptyRow(this, 20);
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(DefaultDatabaseTablespace.COLUMNS);
        DefaultTable table = new DefaultTable(model);
        model.addRow(tablespace.getAttributes());
        //JScrollPane scroll = new JScrollPane(table);
        add(table.getTableHeader(), gbh.nextRowFirstCol().fillHorizontally().setMaxWeightX().spanX().setMinWeightY().get());
        add(table, gbh.nextRowFirstCol().fillHorizontally().setMaxWeightX().spanX().setMinWeightY().get());
        gbh.insertEmptyRow(this, 20);
        DependPanel tablesIndexesPanel = new DependPanel(TreePanel.TABLESPACE);
        tablesIndexesPanel.setDatabaseObject(tablespace);
        tablesIndexesPanel.setDatabaseConnection(tablespace.getHost().getDatabaseConnection());
        add(new JScrollPane(tablesIndexesPanel), gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());

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

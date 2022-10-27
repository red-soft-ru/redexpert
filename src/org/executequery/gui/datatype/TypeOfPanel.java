package org.executequery.gui.datatype;

import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.table.TableDefinitionPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.DynamicComboBoxModel;

import javax.swing.*;
import java.awt.*;

public class TypeOfPanel extends JPanel {
    private final ColumnData columnData;
    private JComboBox tablesBox;
    private JComboBox columnBox;
    private DynamicComboBoxModel tableModel;
    private DynamicComboBoxModel columnsModel;
    private JCheckBox typeOfBox;

    public TypeOfPanel(ColumnData cd) {
        columnData = new ColumnData(cd.getDatabaseConnection());
        init(cd);
    }

    private void init(ColumnData cd) {
        tableModel = new DynamicComboBoxModel();
        tableModel.setElements(columnData.getTableNames());
        tablesBox = new JComboBox(tableModel);
        tablesBox.setEnabled(false);
        tablesBox.addActionListener(actionEvent -> {
            columnData.setTable(getTable());
            columnsModel.setElements(columnData.getColumns());
            columnBox.setSelectedIndex(0);
        });
        columnsModel = new DynamicComboBoxModel();
        columnBox = new JComboBox(columnsModel);
        columnBox.setEnabled(false);
        columnBox.addActionListener(actionEvent ->
                columnData.setColumnTable(getColumn()));
        if (tableModel.getSize() > 0)
            tablesBox.setSelectedIndex(0);
        typeOfBox = new JCheckBox(bundleString("TypeOf"));
        typeOfBox.addActionListener(actionEvent -> {
            columnData.setTypeOf(typeOfBox.isSelected());
            tablesBox.setEnabled(columnData.isTypeOf());
            columnBox.setEnabled(columnData.isTypeOf());
            if (columnData.isTypeOf())
                columnData.setTypeOfFrom(ColumnData.TYPE_OF_FROM_COLUMN);
        });

        this.setLayout(new GridBagLayout());
        this.add(typeOfBox, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        this.add(new JLabel(bundleString("Table")), new GridBagConstraints(0, 1,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        this.add(tablesBox, new GridBagConstraints(1, 1,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        this.add(new JLabel(bundleString("Column")), new GridBagConstraints(0, 2,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        this.add(columnBox, new GridBagConstraints(1, 2,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        typeOfBox.setSelected(cd.isTypeOf());
        columnData.setTypeOfFrom(cd.getTypeOfFrom());
        if (cd.isTypeOf()) {
            tablesBox.setSelectedItem(cd.getTable());
            columnBox.setSelectedItem(cd.getColumnTable());
            tablesBox.setEnabled(cd.isTypeOf());
            columnBox.setEnabled(cd.isTypeOf());
        }

    }

    private String bundleString(String key) {
        return Bundles.get(TableDefinitionPanel.class, key);
    }

    public String getTable() {
        return (String) tablesBox.getSelectedItem();
    }

    public String getColumn() {
        return (String) columnBox.getSelectedItem();
    }

    public String getTableColumn() {
        return getTable() + "." + getColumn();
    }
}

package org.executequery.gui.datatype;

import org.executequery.gui.browser.ColumnData;
import org.underworldlabs.swing.DynamicComboBoxModel;

import javax.swing.*;
import java.awt.*;

public class TypeOfPanel extends JPanel {
    private ColumnData columnData;
    private JComboBox tablesBox;
    private JComboBox columnBox;
    private DynamicComboBoxModel tableModel;
    private DynamicComboBoxModel columnsModel;
    private JCheckBox typeOfBox;

    public TypeOfPanel(ColumnData cd) {
        columnData = cd;
        init();
    }

    private void init() {
        tableModel = new DynamicComboBoxModel();
        tableModel.setElements(columnData.getTables());
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
        typeOfBox = new JCheckBox("Type Of");
        typeOfBox.addActionListener(actionEvent -> {
            columnData.setType_of(typeOfBox.isSelected());
            tablesBox.setEnabled(columnData.isType_of());
            columnBox.setEnabled(columnData.isType_of());
            if (columnData.isType_of())
                columnData.setTypeOfFrom(ColumnData.TYPE_OF_FROM_COLUMN);
        });

        this.setLayout(new GridBagLayout());
        this.add(typeOfBox, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        this.add(new JLabel("Table"), new GridBagConstraints(0, 1,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        this.add(tablesBox, new GridBagConstraints(1, 1,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        this.add(new JLabel("Column"), new GridBagConstraints(0, 2,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        this.add(columnBox, new GridBagConstraints(1, 2,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        typeOfBox.setSelected(columnData.isType_of());
        columnData.setTypeOfFrom(ColumnData.TYPE_OF_FROM_COLUMN);
        if (columnData.isType_of()) {
            tablesBox.setSelectedItem(columnData.getTable());
            columnBox.setSelectedItem(columnData.getColumnTable());
            tablesBox.setEnabled(columnData.isType_of());
            columnBox.setEnabled(columnData.isType_of());
        }

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

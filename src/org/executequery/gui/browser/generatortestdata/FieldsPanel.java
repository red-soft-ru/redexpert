package org.executequery.gui.browser.generatortestdata;

import org.executequery.gui.browser.GeneratorTestDataPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FieldsPanel extends JPanel {

    public final static int SELECTED_FIELD = 0;
    public final static int NAME_FIELD = SELECTED_FIELD + 1;
    public final static int TYPE_FIELD = NAME_FIELD + 1;
    public final static int REQUIRED_FIELD = TYPE_FIELD + 1;

    private JPanel rightPanel;
    private JTable tableFields;
    private FieldGeneratorModel model;
    private List<FieldGenerator> fieldGenerators;

    public FieldsPanel() {
        fieldGenerators = new ArrayList<>();
        init();
    }

    private void init() {

        rightPanel = new JPanel();
        rightPanel.setLayout(new GridBagLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder(Bundles.get("GeneratorTestDataPanel.GeneratorMethod")));

        model = new FieldGeneratorModel();
        tableFields = new JTable(model);
        tableFields.getSelectionModel().addListSelectionListener(e -> reloadRightPanel());

        // ---

        JScrollPane rightScrollPane = new JScrollPane(rightPanel);
        rightScrollPane.setPreferredSize(new Dimension(675, 400));
        rightScrollPane.setMinimumSize(rightScrollPane.getPreferredSize());

        JScrollPane leftScrollPane = new JScrollPane(tableFields);
        leftScrollPane.setPreferredSize(new Dimension(400, 400));
        leftScrollPane.setMinimumSize(leftScrollPane.getPreferredSize());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setTopComponent(leftScrollPane);
        splitPane.setBottomComponent(rightScrollPane);
        splitPane.setResizeWeight(1);

        setLayout(new GridBagLayout());
        add(splitPane, new GridBagHelper().fillBoth().spanX().spanY().get());
    }

    private void reloadRightPanel() {
        rightPanel.removeAll();
        if (tableFields.getSelectedRow() >= 0) {
            rightPanel.add(
                    fieldGenerators.get(tableFields.getSelectedRow()).getMethodGeneratorPanel(),
                    new GridBagHelper().fillBoth().spanX().spanY().get()
            );
        }

        rightPanel.updateUI();
    }

    public List<FieldGenerator> getFieldGenerators() {
        return fieldGenerators;
    }

    public void setFieldGenerators(List<FieldGenerator> fieldGenerators) {
        this.fieldGenerators = fieldGenerators;
        this.model.fireTableDataChanged();
    }

    private class FieldGeneratorModel extends AbstractTableModel {

        private final String[] COLUMN_NAMES = Bundles.get(
                GeneratorTestDataPanel.class,
                new String[]{"Selected", "Name", "Type", "Required"}
        );

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case SELECTED_FIELD:
                    return fieldGenerators.get(rowIndex).isSelectedField();
                case NAME_FIELD:
                    return fieldGenerators.get(rowIndex).getColumn().getName();
                case TYPE_FIELD:
                    return fieldGenerators.get(rowIndex).getColumn().getFormattedDataType();
                case REQUIRED_FIELD:
                    return fieldGenerators.get(rowIndex).getColumn().isRequired();
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == SELECTED_FIELD)
                fieldGenerators.get(rowIndex).setSelectedField((boolean) aValue);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case SELECTED_FIELD:
                case REQUIRED_FIELD:
                    return Boolean.class;
                default:
                    return String.class;
            }
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == SELECTED_FIELD;
        }

        @Override
        public int getRowCount() {
            return fieldGenerators.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

    } // FieldGeneratorModel class

}

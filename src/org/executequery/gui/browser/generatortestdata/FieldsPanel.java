package org.executequery.gui.browser.generatortestdata;

import org.executequery.gui.browser.GeneratorTestDataPanel;
import org.executequery.localization.Bundles;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

public class FieldsPanel extends JPanel {
    public final static int SELECTED_FIELD = 0;
    public final static int NAME_FIELD = 1;
    public final static int TYPE_FIELD = 2;
    private JTable tableFields;
    private List<FieldGenerator> fieldGenerators;
    private String[] colNames = Bundles.get(GeneratorTestDataPanel.class, new String[]{"Selected", "Name", "Type"});
    private FieldGeneratorModel model;
    private JPanel rightPanel;

    public FieldsPanel(List<FieldGenerator> fieldGenerators) {
        this.fieldGenerators = fieldGenerators;
        init();
    }

    private void init() {
        rightPanel = new JPanel();
        rightPanel.setBorder(BorderFactory.createTitledBorder(bundledString("GeneratorMethod")));
        rightPanel.setLayout(new GridBagLayout());
        model = new FieldGeneratorModel();
        tableFields = new JTable(model);
        tableFields.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                rightPanel.removeAll();
                if (tableFields.getSelectedRow() >= 0)
                    rightPanel.add(fieldGenerators.get(tableFields.getSelectedRow()).getMethodGeneratorPanel(), new GridBagConstraints(0, 0, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 1, 1,
                            GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
                rightPanel.updateUI();
            }
        });
        JScrollPane scroll = new JScrollPane(tableFields);


        setLayout(new GridBagLayout());


        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll, rightPanel);
        add(splitPane, new GridBagConstraints(0, 0, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        splitPane.setResizeWeight(0.6);
    }

    public List<FieldGenerator> getFieldGenerators() {
        return fieldGenerators;
    }

    public void setFieldGenerators(List<FieldGenerator> fieldGenerators) {
        this.fieldGenerators = fieldGenerators;
        model.fireTableDataChanged();
    }

    private String bundledString(String key) {
        return GeneratorTestDataPanel.bundles(key);
    }

    public class FieldGeneratorModel extends AbstractTableModel {


        @Override
        public int getRowCount() {
            return fieldGenerators.size();
        }

        @Override
        public int getColumnCount() {
            return colNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case SELECTED_FIELD:
                    return fieldGenerators.get(rowIndex).isSelectedField();
                case NAME_FIELD:
                    return fieldGenerators.get(rowIndex).getColumn().getName();
                case TYPE_FIELD:
                    return fieldGenerators.get(rowIndex).getColumn().getFormattedDataType();
                default:
                    return null;
            }
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == SELECTED_FIELD) {
                fieldGenerators.get(rowIndex).setSelectedField((boolean) aValue);
            }
        }

        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case SELECTED_FIELD:
                    return Boolean.class;
                default:
                    return String.class;
            }
        }

        public String getColumnName(int column) {
            return colNames[column];
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == SELECTED_FIELD;
        }


    }
}

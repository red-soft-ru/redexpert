package org.executequery.gui.procedure;

import org.executequery.Constants;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.table.CreateTableToolBar;
import org.executequery.gui.table.TableDefinitionPanel;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.print.AbstractPrintableTableModel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Vector;

public class SubProgramPanel extends JPanel implements DefinitionPanel {
    private final boolean editing;
    private static final int NAME_COLUMN = 0;
    private static final int TYPE_COLUMN = 1;
    private CreateTableToolBar toolBar;
    private SimpleSqlTextPanel sqlTextPanel;
    private ProcedureParameterModel parameterModel;
    private int selectedRow;
    private DatabaseConnection connection;
    private Vector<ColumnData> subProgsVector;
    private DatabaseTable cursorsTable;

    public SubProgramPanel() {
        this(true);
    }

    public SubProgramPanel(boolean editing) {
        super(new GridBagLayout());
        this.editing = editing;
        this.selectedRow = -1;

        init();
        arrange();
    }

    private void init() {
        toolBar = new CreateTableToolBar(this);

        // --- table ---

        parameterModel = new ProcedureParameterModel();

        cursorsTable = new DatabaseTable(parameterModel);
        cursorsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        cursorsTable.getSelectionModel().addListSelectionListener(e -> selectionChanged());

        TableColumnModel tcm = cursorsTable.getColumnModel();
        tcm.getColumn(NAME_COLUMN).setPreferredWidth(200);
        tcm.getColumn(TYPE_COLUMN).setPreferredWidth(120);

        // --- sql text panel ---

        sqlTextPanel = new SimpleSqlTextPanel();
        sqlTextPanel.setMinimumSize(new Dimension(100, 200));
        sqlTextPanel.getTextPane().getSQLSyntaxDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (selectedRow >= 0) {
                    subProgsVector.get(selectedRow).setSelectOperator(sqlTextPanel.getSQLText());
                    parameterModel.fireTableRowsUpdated(selectedRow, selectedRow);
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

        });

        // --- cell editors ---

        if (!editing) {
        }
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- table panel ---

        JPanel tablePanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().fillVertical();
        tablePanel.add(toolBar, gbh.setMinWeightX().rightGap(2).spanY().get());
        tablePanel.add(new JScrollPane(cursorsTable), gbh.nextCol().bottomGap(10).setMaxWeightX().fillBoth().spanX().get());

        // ---  split pane ---

        JSplitPane splitPane = new JSplitPane();
        splitPane.setTopComponent(tablePanel);
        splitPane.setBottomComponent(sqlTextPanel);
        splitPane.setDividerLocation(0.5);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

        // --- base ---

        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(300, 200));
        setMinimumSize(getPreferredSize());

        gbh = new GridBagHelper().setInsets(0, 0, 0, 0).fillBoth().spanX().spanY();
        add(splitPane, gbh.get());
    }

    public void addRow(ColumnData cd) {
        cd.setCursor(true);
        subProgsVector.add(cd);
    }

    private void selectionChanged() {
        if (cursorsTable.getSelectedRow() >= 0) {
            selectedRow = cursorsTable.getSelectedRow();
            sqlTextPanel.setSQLText(subProgsVector.get(selectedRow).getSelectOperator());
        }
    }

    public void setDatabaseConnection(DatabaseConnection databaseConnection) {
        connection = databaseConnection;
        subProgsVector.forEach(columnData -> columnData.setConnection(connection));
    }

    public int getSelectedRow() {
        return cursorsTable.getSelectedRow();
    }

    public boolean isEditing() {
        return cursorsTable.isEditing();
    }

    public void insertRow(ColumnData cd, int position) {
        subProgsVector.insertElementAt(cd, position);
    }

    public void removeRow(int position) {
        subProgsVector.remove(position);
    }

    public void clearRows() {
        subProgsVector.clear();
    }

    public void fireEditingStopped() {
        cursorsTable.editingStopped(null);
        if (cursorsTable.isEditing())
            cursorsTable.removeEditor();
    }

    public void deleteEmptyRow() {

        cursorsTable.editingStopped(null);
        if (cursorsTable.isEditing())
            cursorsTable.removeEditor();

        if (subProgsVector.isEmpty())
            return;

        removeRow(0);
        parameterModel.fireTableRowsDeleted(0, 0);
    }

    public void setRowSelectionInterval(int row) {
        cursorsTable.setRowSelectionInterval(row, row);
    }

    public String getSQLText() {
        return sqlTextPanel.getSQLText();
    }

    public Vector<ColumnData> getSubProgsVector() {
        return subProgsVector;
    }

    public ProcedureParameterModel getProcedureParameterModel() {
        return parameterModel;
    }

    // --- DefinitionPanel impl ---

    /**
     * <p>Inserts a new column after the selected
     * column moving the selected column up one row.
     */
    @Override
    public void addRow() {
        fireEditingStopped();

        int lastRow = subProgsVector.size() - 1;
        int newRow = lastRow + 1;

        ColumnData columnData = new ColumnData(connection);
        columnData.setCursor(true);
        addRow(columnData);

        parameterModel.fireTableRowsInserted(lastRow, newRow);
        cursorsTable.setRowSelectionInterval(newRow, newRow);
        cursorsTable.setColumnSelectionInterval(1, 1);

        cursorsTable.setEditingRow(newRow);
        cursorsTable.setEditingColumn(NAME_COLUMN);
        ((DefaultCellEditor) cursorsTable.getCellEditor(newRow, NAME_COLUMN)).getComponent().requestFocus();
    }

    /**
     * <p>Deletes the selected row from the table.
     * This will also modify the SQL generated text.
     */
    @Override
    public void deleteRow() {

        cursorsTable.editingStopped(null);
        if (cursorsTable.isEditing())
            cursorsTable.removeEditor();

        int selection = cursorsTable.getSelectedRow();
        if (selection == -1 || subProgsVector.isEmpty())
            return;

        removeRow(selection);
        parameterModel.fireTableRowsDeleted(selection, selection);

        if (subProgsVector.isEmpty()) {
            addRow(new ColumnData(true, connection));
            parameterModel.fireTableRowsInserted(0, 0);
        }
    }

    /**
     * <p>Moves the selected column up one row within
     * the table moving the column above the selection
     * below the selection.
     */
    @Override
    public void moveRowUp() {

        int selection = cursorsTable.getSelectedRow();
        if (selection == -1 || selection == 0)
            return;

        cursorsTable.editingStopped(null);
        if (cursorsTable.isEditing())
            cursorsTable.removeEditor();

        int newPosition = selection - 1;
        ColumnData columnDataToMove = subProgsVector.elementAt(selection);

        removeRow(selection);
        insertRow(columnDataToMove, newPosition);
        cursorsTable.setRowSelectionInterval(newPosition, newPosition);
        parameterModel.fireTableRowsUpdated(newPosition, selection);
    }

    /**
     * <p>Moves the selected column down one row within
     * the table moving the column below the selection
     * above the selection.
     */
    @Override
    public void moveRowDown() {

        int selectedRow = cursorsTable.getSelectedRow();
        if (selectedRow == -1 || selectedRow == subProgsVector.size() - 1)
            return;

        cursorsTable.editingStopped(null);
        if (cursorsTable.isEditing())
            cursorsTable.removeEditor();

        int newPosition = selectedRow + 1;
        ColumnData columnDataToMove = subProgsVector.elementAt(selectedRow);

        removeRow(selectedRow);
        insertRow(columnDataToMove, newPosition);
        cursorsTable.setRowSelectionInterval(newPosition, newPosition);
        parameterModel.fireTableRowsUpdated(selectedRow, newPosition);
    }

    public void addChangesListener(ActionListener listener) {
        parameterModel.addTableModelListener(e -> listener.actionPerformed(null));
    }

    private static class DatabaseTable extends DefaultTable {

        public DatabaseTable(TableModel _model) {
            super(_model);
            getTableHeader().setReorderingAllowed(false);
            setCellSelectionEnabled(true);
            setColumnSelectionAllowed(false);
            setRowSelectionAllowed(false);
            setSurrendersFocusOnKeystroke(true);
        }

        @Override
        public TableCellEditor getCellEditor(int row, int col) {
            return super.getCellEditor(row, col);
        }

    }

    private class ProcedureParameterModel extends AbstractPrintableTableModel {

        protected String[] header = Bundles.get(TableDefinitionPanel.class, new String[]{
                "Name", "Datatype"
        });

        public ProcedureParameterModel() {
            subProgsVector = new Vector<>();
            ColumnData cd = new ColumnData(connection);
            cd.setCursor(true);
            addRow(cd);
        }

        @Override
        public int getColumnCount() {
            return header.length;
        }

        @Override
        public int getRowCount() {
            return subProgsVector.size();
        }

        /**
         * Returns the printable value at the specified row and column.
         *
         * @param row the row index
         * @param col the column index
         * @return the value to print
         */
        @Override
        public String getPrintValueAt(int row, int col) {

            if (col < 0)
                return Constants.EMPTY;

            Object value = getValueAt(row, col);
            if (value != null)
                return value.toString();

            return Constants.EMPTY;
        }

        @Override
        public Object getValueAt(int row, int col) {

            if (row >= subProgsVector.size())
                return null;

            ColumnData cd = subProgsVector.elementAt(row);
            switch (col) {

                case NAME_COLUMN:
                    return cd.getColumnName();

                case TYPE_COLUMN:
                    return cd.getTypeName();

                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {

            ColumnData cd = subProgsVector.elementAt(row);
            switch (col) {

                case NAME_COLUMN:
                    cd.setColumnName((String) value);
                    break;

                case TYPE_COLUMN:
                    cd.setTypeName((String) value);
                    break;
            }

            fireTableRowsUpdated(row, row);
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        @Override
        public String getColumnName(int col) {
            return header[col];
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return String.class;
        }

    } // CreateTableModel class

}

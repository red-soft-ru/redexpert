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
import org.underworldlabs.swing.celleditor.picker.StringPicker;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.Vector;

public class CursorsPanel extends JPanel
        implements DefinitionPanel {

    private static final int NAME_COLUMN = 0;
    private static final int SCROLL_COLUMN = NAME_COLUMN + 1;
    private static final int SELECT_OPERATOR_COLUMN = SCROLL_COLUMN + 1;
    private static final int DESCRIPTION_COLUMN = SELECT_OPERATOR_COLUMN + 1;

    private final boolean editing;

    private CreateTableToolBar toolBar;
    private DatabaseTable cursorsTable;
    private SimpleSqlTextPanel sqlTextPanel;
    private ProcedureParameterModel parameterModel;

    private int selectedRow;
    private DatabaseConnection connection;
    private Vector<ColumnData> cursorsVector;

    private StringPicker descEditor;
    private StringPicker colNameEditor;

    public CursorsPanel() {
        this(true);
    }

    public CursorsPanel(boolean editing) {
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
        tcm.getColumn(SCROLL_COLUMN).setPreferredWidth(120);
        tcm.getColumn(SELECT_OPERATOR_COLUMN).setPreferredWidth(200);
        tcm.getColumn(DESCRIPTION_COLUMN).setPreferredWidth(200);

        // --- sql text panel ---

        sqlTextPanel = new SimpleSqlTextPanel();
        sqlTextPanel.setMinimumSize(new Dimension(100, 200));
        sqlTextPanel.getTextPane().getSQLSyntaxDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (selectedRow >= 0)
                    parameterModel.setValueAt(sqlTextPanel.getSQLText(), selectedRow, SELECT_OPERATOR_COLUMN);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

        });

        // --- cell editors ---

        if (!editing)
            return;

        colNameEditor = new StringPicker();
        DefaultCellEditor colStrEditor = new DefaultCellEditor(colNameEditor) {
            @Override
            public Object getCellEditorValue() {
                return colNameEditor.getValue();
            }
        };

        descEditor = new StringPicker();
        DefaultCellEditor descStrEditor = new DefaultCellEditor(descEditor) {
            @Override
            public Object getCellEditorValue() {
                return descEditor.getValue();
            }
        };

        tcm.getColumn(NAME_COLUMN).setCellEditor(colStrEditor);
        tcm.getColumn(DESCRIPTION_COLUMN).setCellEditor(descStrEditor);
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

    private void selectionChanged() {
        if (cursorsTable.getSelectedRow() >= 0) {
            selectedRow = cursorsTable.getSelectedRow();
            sqlTextPanel.setSQLText(cursorsVector.get(selectedRow).getSelectOperator());
        }
    }

    public void setDatabaseConnection(DatabaseConnection databaseConnection) {
        connection = databaseConnection;
        cursorsVector.forEach(columnData -> columnData.setConnection(connection));
    }

    public int getSelectedRow() {
        return cursorsTable.getSelectedRow();
    }

    public boolean isEditing() {
        return cursorsTable.isEditing();
    }

    public void insertRow(ColumnData cd, int position) {
        cursorsVector.insertElementAt(cd, position);
    }

    public void addRow(ColumnData cd) {
        cd.setCursor(true);
        cursorsVector.add(cd);
    }

    public void removeRow(int position) {
        cursorsVector.remove(position);
    }

    public void clearRows() {
        cursorsVector.clear();
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

        if (cursorsVector.isEmpty())
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

    public Vector<ColumnData> getCursorsVector() {
        return cursorsVector;
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

        int lastRow = cursorsVector.size() - 1;
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
        if (selection == -1 || cursorsVector.isEmpty())
            return;

        removeRow(selection);
        parameterModel.fireTableRowsDeleted(selection, selection);

        if (cursorsVector.isEmpty()) {
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
        ColumnData columnDataToMove = cursorsVector.elementAt(selection);

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
        if (selectedRow == -1 || selectedRow == cursorsVector.size() - 1)
            return;

        cursorsTable.editingStopped(null);
        if (cursorsTable.isEditing())
            cursorsTable.removeEditor();

        int newPosition = selectedRow + 1;
        ColumnData columnDataToMove = cursorsVector.elementAt(selectedRow);

        removeRow(selectedRow);
        insertRow(columnDataToMove, newPosition);
        cursorsTable.setRowSelectionInterval(newPosition, newPosition);
        parameterModel.fireTableRowsUpdated(selectedRow, newPosition);
    }

    // ---

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

    } // DatabaseTable class

    private class ProcedureParameterModel extends AbstractPrintableTableModel {

        protected String[] header = Bundles.get(TableDefinitionPanel.class, new String[]{
                "Name", "Scroll", "SelectOperator", "Description"
        });

        public ProcedureParameterModel() {
            cursorsVector = new Vector<>();
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
            return cursorsVector.size();
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

            if (row >= cursorsVector.size())
                return null;

            ColumnData cd = cursorsVector.elementAt(row);
            switch (col) {

                case NAME_COLUMN:
                    return cd.getColumnName();

                case SCROLL_COLUMN:
                    return cd.isScroll();

                case SELECT_OPERATOR_COLUMN:
                    return cd.getSelectOperator();

                case DESCRIPTION_COLUMN:
                    return cd.getRemarks();

                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {

            ColumnData cd = cursorsVector.elementAt(row);
            switch (col) {

                case NAME_COLUMN:
                    cd.setColumnName((String) value);
                    break;

                case SCROLL_COLUMN:
                    cd.setScroll((boolean) value);
                    break;

                case SELECT_OPERATOR_COLUMN:
                    cd.setSelectOperator((String) value);
                    break;

                case DESCRIPTION_COLUMN:
                    cd.setRemarks((String) value);
                    break;
            }

            fireTableRowsUpdated(row, row);
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col != SELECT_OPERATOR_COLUMN && editing;
        }

        @Override
        public String getColumnName(int col) {
            return header[col];
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return col == SCROLL_COLUMN ? Boolean.class : String.class;
        }

    } // CreateTableModel class

}

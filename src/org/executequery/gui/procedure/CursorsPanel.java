package org.executequery.gui.procedure;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.table.TableDefinitionPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.print.AbstractPrintableTableModel;
import org.underworldlabs.swing.table.StringCellEditor;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Vector;

public class CursorsPanel extends JPanel
        implements TableModelListener, DefinitionPanel {

    /**
     * The table containing all column descriptions
     */
    protected DatabaseTable table;

    /**
     * The table's _model
     */
    protected ProcedureParameterModel _model;

    private StringBuffer sqlText;

    public List<String> descriptions;


    private CreateProcedureToolBar tools;

    /**
     * The <code>Vector</code> of <code>ColumnData</code> objects
     */
    protected Vector<ColumnData> tableVector;

    /**
     * An empty String literal
     */
    private static final String EMPTY = " ";

    protected boolean editing;

    public static final int NAME_COLUMN = 0;
    public static final int SCROLL_COLUMN = 1;
    public static final int SELECT_OPERATOR_COLUMN = 2;
    public static final int DESCRIPTION_COLUMN = 3;
    /**
     * The cell editor for the column names
     */
    protected StringCellEditor colNameEditor;
    protected StringCellEditor selectEditor;
    protected StringCellEditor descEditor;
    DynamicComboBoxModel tableEditorModel;


    DatabaseConnection dc;

    public CursorsPanel() {
        this(true);

    }

    public CursorsPanel(boolean editing) {
        super(new GridBagLayout());
        this.editing = editing;

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setColumnDataArray(ColumnData[] cda) {
        _model.setColumnDataArray(cda);
    }


    private void jbInit() {
        // set the table model to use
        _model = new ProcedureParameterModel();
        table = new DatabaseTable(_model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(NAME_COLUMN).setPreferredWidth(200);
        tcm.getColumn(SCROLL_COLUMN).setMaxWidth(70);
        tcm.getColumn(SELECT_OPERATOR_COLUMN).setPreferredWidth(200);
        tcm.getColumn(DESCRIPTION_COLUMN).setPreferredWidth(200);


        // add the editors if editing
        if (editing) {
            colNameEditor = new StringCellEditor();
            DefaultCellEditor colStrEditor = new DefaultCellEditor(colNameEditor) {
                public Object getCellEditorValue() {
                    return colNameEditor.getValue();
                }
            };
            selectEditor = new StringCellEditor();
            DefaultCellEditor checkStrEditor = new DefaultCellEditor(selectEditor) {
                public Object getCellEditorValue() {
                    return selectEditor.getValue();
                }
            };
            descEditor = new StringCellEditor();
            DefaultCellEditor descStrEditor = new DefaultCellEditor(descEditor) {
                public Object getCellEditorValue() {
                    return descEditor.getValue();
                }
            };


            tcm.getColumn(NAME_COLUMN).setCellEditor(colStrEditor);
            tcm.getColumn(DESCRIPTION_COLUMN).setCellEditor(descStrEditor);


            // create the key listener to notify changes
            KeyAdapter valueKeyListener = new KeyAdapter() {
                public void keyReleased(KeyEvent e) {
                    String value = null;
                    Object object = e.getSource();
                    if (object == colNameEditor) {
                        value = colNameEditor.getValue();
                    } else if (object == selectEditor) {
                        value = selectEditor.getValue();
                    } else if (object == descEditor) {
                        value = descEditor.getValue();
                    } else if (object instanceof JComboBox) {
                        value = String.valueOf(((JComboBox) object).getSelectedItem());
                    }
                    tableChanged(table.getEditingColumn(),
                            table.getEditingRow(),
                            value);
                }
            };
            colNameEditor.addKeyListener(valueKeyListener);
            selectEditor.addKeyListener(valueKeyListener);
            descEditor.addKeyListener(valueKeyListener);

            _model.addTableModelListener(this);
        }

        tools = new CreateProcedureToolBar(this);

        JPanel definitionPanel = new JPanel(new GridBagLayout());

        definitionPanel.add(tools, new GridBagConstraints(
                0, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER,
                GridBagConstraints.VERTICAL,
                new Insets(2, 2, 2, 2), 0, 0));

        definitionPanel.add(new JScrollPane(table), new GridBagConstraints(
                1, 0, 0, 1, 1.0, 1.0,
                GridBagConstraints.NORTHEAST,
                GridBagConstraints.BOTH,
                new Insets(2, 2, 2, 2), 0, 0));

        add(definitionPanel, new GridBagConstraints(
                1, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHEAST,
                GridBagConstraints.BOTH,
                new Insets(2, 2, 2, 2), 0, 0));
    }


    public void tableChanged(TableModelEvent e) {
        int row = table.getEditingRow();
        if (row == -1 || tableVector.size() == row) {
            return;
        }
        tableChanged(table.getEditingColumn(), row, null);
    }

    /**
     * Fires that a table cell value has changed as specified.
     *
     * @param col   - the column index
     * @param row   - the row index
     * @param value - the current value
     */
    public void tableChanged(int col, int row, String value) {
    }

    /**
     * <p>Adds all the column definition lines to
     * the SQL text buffer for display.
     *
     * @param row current row being edited
     */
    public void addColumnLines(int row) {
    }


    public void setDatabaseConnection(DatabaseConnection databaseConnection) {
        dc = databaseConnection;
        tableEditorModel.setElements(new ColumnData(dc).getTables());
        for (ColumnData cd : tableVector) {
            cd.setDatabaseConnection(dc);
        }
    }

    public void tableEditingStopped(ChangeEvent e) {
        table.editingStopped(e);
    }

    public int getEditingRow() {
        return table.getEditingRow();
    }

    public void setEditingRow(int newEditingRow) {
        table.setEditingRow(newEditingRow);
    }

    public int getSelectedRow() {
        return table.getSelectedRow();
    }

    public int getEditingColumn() {
        return table.getEditingColumn();
    }

    public void addTableFocusListener(FocusListener listener) {
        table.addFocusListener(listener);
    }

    /**
     * <p>Propogates the call to <code>removeEditor()</code>
     * on the table displaying the data.
     */
    public void removeEditor() {
        table.removeEditor();
    }

    /**
     * <p>Propogates the call to <code>isEditing()</code>
     * on the table displaying the data.
     *
     * @return if a data edit is in progress on the table
     */
    public boolean isEditing() {
        return table.isEditing();
    }

    /**
     * <p>Returns the table displaying the
     * column data.
     *
     * @return the table displaying the data
     */
    public JTable getTable() {
        return table;
    }

    /**
     * <p>Moves the selected column up one row within
     * the table moving the column above the selection
     * below the selection.
     */
    public void moveColumnUp() {
        int selection = table.getSelectedRow();
        if (selection == -1 || selection == 0) {
            return;
        }

        table.editingStopped(null);
        if (table.isEditing()) {
            table.removeEditor();
        }

        int newPostn = selection - 1;
        ColumnData move = tableVector.elementAt(selection);
        removeRow(selection);
        insertRow(move, newPostn);
        table.setRowSelectionInterval(newPostn, newPostn);
        _model.fireTableRowsUpdated(newPostn, selection);
        addColumnLines(-1);
    }

    /**
     * <p>Moves the selected column down one row within
     * the table moving the column below the selection
     * above the selection.
     */
    public void moveColumnDown() {
        int selection = table.getSelectedRow();
        if (selection == -1 || selection == tableVector.size() - 1) {
            return;
        }

        table.editingStopped(null);
        if (table.isEditing()) {
            table.removeEditor();
        }

        int newPostn = selection + 1;
        ColumnData move = tableVector.elementAt(selection);
        removeRow(selection);
        insertRow(move, newPostn);
        table.setRowSelectionInterval(newPostn, newPostn);
        _model.fireTableRowsUpdated(selection, newPostn);
        addColumnLines(-1);
    }

    /**
     * <p>Inserts a new column before the selected
     * column moving the selected column down one row.
     */
    public void insertBefore() {
        fireEditingStopped();

        if (table.isEditing()) {
            table.removeEditor();
        }

        int selection = table.getSelectedRow();

        if (selection == -1)
            selection = 0;
        insertRow(new ColumnData(dc), selection);


        _model.fireTableRowsInserted(
                selection == 0 ? 0 : selection - 1,
                selection == 0 ? 1 : selection);

        table.setRowSelectionInterval(selection, selection);
        table.setColumnSelectionInterval(1, 1);

        table.setEditingRow(selection);
        table.setEditingColumn(NAME_COLUMN);

    }

    public DefaultCellEditor createColumnEditor(ColumnData cd) {
        DynamicComboBoxModel model = new DynamicComboBoxModel();
        if (cd.getColumns() != null)
            model.setElements(cd.getColumns());
        return new DefaultCellEditor(new JComboBox(model));
    }

    public void insertRow(ColumnData cd, int position) {
        tableVector.insertElementAt(cd, position);

    }

    public void addRow(ColumnData cd) {
        cd.setCursor(true);
        tableVector.add(cd);
    }

    public void removeRow(int position) {
        tableVector.remove(position);
    }

    public void clearRows() {
        tableVector.clear();
    }

    public void fireEditingStopped() {
        table.editingStopped(null);
        if (table.isEditing()) {
            table.removeEditor();
        }
    }


    /**
     * Adding new row
     */


    /**
     * <p>Deletes the selected row from the table.
     * This will also modify the SQL generated text.
     */
    public void deleteRow() {
        table.editingStopped(null);
        if (table.isEditing()) {
            table.removeEditor();
        }

        int selection = table.getSelectedRow();
        if (selection == -1 || tableVector.size() == 0) {
            return;
        }

        removeRow(selection);
        _model.fireTableRowsDeleted(selection, selection);

        if (tableVector.size() == 0) {
            addRow(new ColumnData(true, dc));
            _model.fireTableRowsInserted(0, 0);
        }

        addColumnLines(-1);
    }

    public void deleteEmptyRow() {
        table.editingStopped(null);
        if (table.isEditing()) {
            table.removeEditor();
        }

        if (tableVector.size() == 0) {
            return;
        }

        removeRow(0);
        _model.fireTableRowsDeleted(0, 0);
    }

    public void addMouseListener() {
        table.addMouseListener();
    }

    /**
     * <p>Inserts a new column after the selected
     * column moving the selected column up one row.
     */
    public void insertAfter() {
        fireEditingStopped();
        int selection = table.getSelectedRow();
        int newRow = selection + 1;
        if (selection == -1)
            selection = tableVector.size();
        ColumnData cd = new ColumnData(dc);
        cd.setCursor(true);
        if (selection == tableVector.size()) {
            addRow(cd);
        } else {
            insertRow(cd, newRow);
        }

        _model.fireTableRowsInserted(selection, newRow);
        table.setRowSelectionInterval(newRow, newRow);
        table.setColumnSelectionInterval(1, 1);

        table.setEditingRow(newRow);
        table.setEditingColumn(NAME_COLUMN);
        ((DefaultCellEditor) table.getCellEditor(newRow, NAME_COLUMN)).
                getComponent().requestFocus();
    }


    public void setEditingColumn(int col) {
        table.setEditingColumn(col);
    }

    public void setRowSelectionInterval(int row) {
        table.setRowSelectionInterval(row, row);
    }

    public void setColumnSelectionInterval(int col) {
        table.setColumnSelectionInterval(col, col);
    }

    public void setTableColumnData(ColumnData[] cda) {
        tableVector = new Vector<>(cda.length);
        for (ColumnData aCda : cda) {
            addRow(aCda);
        }
        _model.fireTableDataChanged();
        addColumnLines(-1);
    }

    public ColumnData[] getTableColumnData() {
        int v_size = tableVector.size();
        ColumnData[] cda = new ColumnData[v_size];

        for (int i = 0; i < v_size; i++) {
            cda[i] = tableVector.elementAt(i);
        }
        return cda;
    }

    public String getSQLText() {
        return null;
    }

    public ProcedureParameterModel getProcedureParameterModel() {
        return _model;
    }

    public Vector<ColumnData> getTableColumnDataVector() {
        return tableVector;
    }

    /**
     * The table view display.
     */
    private class DatabaseTable extends DefaultTable
            implements MouseListener {

        public DatabaseTable(TableModel _model) {
            super(_model);
            //setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            getTableHeader().setReorderingAllowed(false);
            setCellSelectionEnabled(true);
            setColumnSelectionAllowed(false);
            setRowSelectionAllowed(false);
            setSurrendersFocusOnKeystroke(true);
            //setDefaultRenderer(Object.class,new BrowserTableCellRenderer());
        }

        public void addMouseListener() {
            addMouseListener(this);
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public TableCellEditor getCellEditor(int row, int col) {
            return super.getCellEditor(row, col);
        }
    } // class DatabaseTable


    /**
     * The table's model.
     */
    public class ProcedureParameterModel extends AbstractPrintableTableModel {

        protected String[] header = Bundles.get(TableDefinitionPanel.class,
                new String[]
                        {"Name", "Scroll", "SelectOperator", "Description"});

        public ProcedureParameterModel() {
            tableVector = new Vector<>();
            ColumnData cd = new ColumnData(dc);
            cd.setCursor(true);
            addRow(cd);
        }

        public ProcedureParameterModel(Vector<ColumnData> data) {
            tableVector = data;
        }

        public void setColumnDataArray(ColumnData[] cda) {

            if (cda != null) {
                if (tableVector == null) {
                    tableVector = new Vector<>(cda.length);
                } else {
                    clearRows();
                }

                for (ColumnData aCda : cda) {
                    addRow(aCda);
                }
            } else {
                clearRows();
            }

            fireTableDataChanged();
        }

        public int getColumnCount() {
            return header.length;
        }

        public int getRowCount() {
            return tableVector.size();
        }

        /**
         * Returns the printable value at the specified row and column.
         *
         * @param row - the row index
         * @param col - the column index
         * @return the value to print
         */
        public String getPrintValueAt(int row, int col) {
            if (col >= 0) {
                Object value = getValueAt(row, col);
                if (value != null) {
                    return value.toString();
                }
                return EMPTY;
            } else return EMPTY;
        }

        public Object getValueAt(int row, int col) {

            if (row >= tableVector.size()) {
                return null;
            }
            ColumnData cd = tableVector.elementAt(row);

            switch (col) {

                case NAME_COLUMN:
                    return cd.getColumnName();

                case SCROLL_COLUMN:
                    return cd.isScroll();

                case SELECT_OPERATOR_COLUMN:
                    return cd.getSelectOperator();

                case DESCRIPTION_COLUMN:
                    return cd.getDescription();

                default:
                    return null;

            }
        }

        public void setValueAt(Object value, int row, int col) {
            ColumnData cd = tableVector.elementAt(row);

            //Log.debug("setValueAt [row: "+row+" col: "+col+" value: "+value+"]");

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
                    cd.setDescription((String) value);
                    break;
            }

            fireTableRowsUpdated(row, row);
        }


        public boolean isCellEditable(int row, int col) {
            return editing;
        }

        public String getColumnName(int col) {
            return header[col];
        }

        public Class getColumnClass(int col) {
            if (col == SCROLL_COLUMN)
                return Boolean.class;
            else {
                return String.class;
            }
        }

        public void addNewRow() {
            ColumnData cd = tableVector.lastElement();
            if (!cd.isNewColumn()) {
                addRow(new ColumnData(true, dc));
            }

        }

        public Vector<ColumnData> getTableVector() {
            return tableVector;
        }
    } // class CreateTableModel


    /**
     * Called when the selction is cancelled.
     */

}

package org.executequery.gui.procedure;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseTypeConverter;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.table.CreateTableSQLSyntax;
import org.executequery.gui.table.TableDefinitionPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.print.AbstractPrintableTableModel;
import org.underworldlabs.swing.table.NumberCellEditor;
import org.underworldlabs.util.FileUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.FocusListener;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class UDFDefinitionPanel extends JPanel
        implements TableModelListener, DefinitionPanel {

    public static final int TYPE_COLUMN = 0;
    public static final int SIZE_COLUMN = 1;
    public static final int SCALE_COLUMN = 2;
    public static final int SUBTYPE_COLUMN = 3;
    public static final int ENCODING_COLUMN = 4;

    //protected ComboBoxCellEditor comboCell;
    public static final int MECHANISM_COLUMN = 5;
    public static final int NULL_COLUMN = 6;
    public static final int CSTRING_COLUMN = 7;
    /**
     * An empty String literal
     */
    private static final String EMPTY = " ";
    /**
     * The table containing all column descriptions
     */
    protected DatabaseTable table;
    /**
     * The table's _model
     */
    protected UDFParameterModel _model;
    /**
     * The cell editor for the column size
     */
    protected NumberCellEditor sizeEditor;
    /**
     * The cell editor for the column scale
     */
    protected NumberCellEditor scaleEditor;
    /**
     * The cell editor for the column subtype
     */
    protected NumberCellEditor subtypeEditor;
    /**
     * The cell editor for the datatype column
     */
    protected DefaultCellEditor dataTypeCell;
    protected DynamicComboBoxModel typeModel;
    /**
     * The <code>Vector</code> of <code>ColumnData</code> objects
     */
    protected Vector<ColumnData> tableVector;
    protected boolean editing;
    DatabaseConnection dc;
    List<String> charsets;
    private CreateProcedureToolBar tools;
    /**
     * the available data types
     */
    private String[] dataTypes;
    private int[] intDataTypes;

    public UDFDefinitionPanel() {
        this(true, null);

    }

    public UDFDefinitionPanel(boolean editing, String[] dataTypes) {
        super(new GridBagLayout());
        this.editing = editing;
        this.dataTypes = dataTypes;


        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setColumnDataArray(ColumnData[] cda) {
        _model.setColumnDataArray(cda);
    }

    public void setColumnDataArray(ColumnData[] cda, String[] dataTypes) {
        _model.setColumnDataArray(cda);
        this.dataTypes = dataTypes;
    }

    public void setDataTypes(String[] dataTypes, int[] intDataTypes) {
        this.dataTypes = dataTypes;
        this.intDataTypes = intDataTypes;
        sortTypes();
        removeDuplicates();
        typeModel.setElements(this.dataTypes);
    }

    void removeDuplicates() {
        List<String> newTypes = new ArrayList<>();
        List<Integer> newIntTypes = new ArrayList<>();
        for (int i = 0; i < this.dataTypes.length; i++) {
            if (!newTypes.contains(this.dataTypes[i])) {
                newTypes.add(this.dataTypes[i]);
                newIntTypes.add(this.intDataTypes[i]);
            }
        }
        this.dataTypes = newTypes.toArray(new String[0]);
        this.intDataTypes = newIntTypes.stream().mapToInt(Integer::intValue).toArray();
    }

    private void jbInit() {
        // set the table model to use
        _model = new UDFParameterModel();
        table = new DatabaseTable(_model);
        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(TYPE_COLUMN).setPreferredWidth(130);
        tcm.getColumn(SIZE_COLUMN).setPreferredWidth(50);
        tcm.getColumn(SCALE_COLUMN).setPreferredWidth(70);
        tcm.getColumn(SUBTYPE_COLUMN).setPreferredWidth(70);
        tcm.getColumn(ENCODING_COLUMN).setPreferredWidth(70);
        tcm.getColumn(MECHANISM_COLUMN).setPreferredWidth(100);
        tcm.getColumn(NULL_COLUMN).setPreferredWidth(70);
        tcm.getColumn(CSTRING_COLUMN).setPreferredWidth(70);

        // add the editors if editing
        if (editing) {


            scaleEditor = new NumberCellEditor();
            DefaultCellEditor scEditor = new DefaultCellEditor(scaleEditor) {
                public Object getCellEditorValue() {
                    return scaleEditor.getStringValue();
                }
            };

            subtypeEditor = new NumberCellEditor();
            DefaultCellEditor stEditor = new DefaultCellEditor(subtypeEditor) {
                public Object getCellEditorValue() {
                    return subtypeEditor.getStringValue();
                }
            };

            sizeEditor = new NumberCellEditor();
            DefaultCellEditor szEditor = new DefaultCellEditor(sizeEditor) {
                public Object getCellEditorValue() {
                    return sizeEditor.getStringValue();
                }
            };

            loadCharsets();
            final JComboBox charsetEditor = new JComboBox(charsets.toArray(new String[charsets.size()]));
            DefaultCellEditor charsetCellEditor = new DefaultCellEditor(charsetEditor);


            tcm.getColumn(SIZE_COLUMN).setCellEditor(szEditor);
            tcm.getColumn(SCALE_COLUMN).setCellEditor(scEditor);
            tcm.getColumn(SUBTYPE_COLUMN).setCellEditor(stEditor);
            typeModel = new DynamicComboBoxModel();
            JComboBox comboBoxType = new JComboBox(typeModel);
            dataTypeCell = new DefaultCellEditor(comboBoxType);
            tcm.getColumn(TYPE_COLUMN).setCellEditor(dataTypeCell);
            tcm.getColumn(ENCODING_COLUMN).setCellEditor(charsetCellEditor);

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

    void sortTypes() {
        if (dataTypes != null) {
            for (int i = 0; i < dataTypes.length; i++) {
                for (int g = 0; g < dataTypes.length - 1; g++) {
                    int compare = dataTypes[g].compareTo(dataTypes[g + 1]);
                    if (compare > 0) {
                        int temp1 = intDataTypes[g];
                        String temp2 = dataTypes[g];
                        intDataTypes[g] = intDataTypes[g + 1];
                        dataTypes[g] = dataTypes[g + 1];
                        intDataTypes[g + 1] = temp1;
                        dataTypes[g + 1] = temp2;
                    }
                }
            }
        }
    }

    public void tableChanged(TableModelEvent e) {
        int row = table.getEditingRow();
        if (row == -1 || tableVector.size() == row) {
        }
    }

    private void loadCharsets() {
        try {
            if (charsets == null)
                charsets = new ArrayList<>();
            else
                charsets.clear();

            String resource = FileUtils.loadResource("org/executequery/charsets.properties");
            String[] strings = resource.split("\n"/*System.getProperty("line.separator")*/);
            for (String s : strings) {
                if (!s.startsWith("#") && !s.isEmpty())
                    charsets.add(s);
            }
            java.util.Collections.sort(charsets);
            charsets.add(0, CreateTableSQLSyntax.NONE);
            charsets.add(0, "");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDatabaseConnection(DatabaseConnection databaseConnection) {
        dc = databaseConnection;
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

    public void setEditingColumn(int col) {
        table.setEditingColumn(col);
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
        if (selection == -1) {
            return;
        } else {
            insertRow(new ColumnData(dc), selection);
        }

        _model.fireTableRowsInserted(
                selection == 0 ? 0 : selection - 1,
                selection == 0 ? 1 : selection);

        table.setRowSelectionInterval(selection, selection);
        table.setColumnSelectionInterval(1, 1);

        table.setEditingRow(selection);
        table.setEditingColumn(TYPE_COLUMN);

    }

    public void insertRow(ColumnData cd, int position) {
        tableVector.insertElementAt(cd, position);

    }

    public void addRow(ColumnData cd) {
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

    /**
     * <p>Inserts a new column after the selected
     * column moving the selected column up one row.
     */
    public void insertAfter() {
        fireEditingStopped();
        int selection = table.getSelectedRow();
        int newRow = selection + 1;

        if (selection == -1) {
            return;
        } else {
            ColumnData cd = new ColumnData(dc);
            if (selection == tableVector.size()) {
                addRow(cd);
            } else {
                insertRow(cd, newRow);
            }
        }

        _model.fireTableRowsInserted(selection, newRow);
        table.setRowSelectionInterval(newRow, newRow);
        table.setColumnSelectionInterval(1, 1);

        table.setEditingRow(newRow);
        table.setEditingColumn(TYPE_COLUMN);
        ((DefaultCellEditor) table.getCellEditor(newRow, TYPE_COLUMN)).
                getComponent().requestFocus();
    }

    public void setRowSelectionInterval(int row) {
        table.setRowSelectionInterval(row, row);
    }

    public void setColumnSelectionInterval(int col) {
        table.setColumnSelectionInterval(col, col);
    }

    public ColumnData[] getTableColumnData() {
        int v_size = tableVector.size();
        ColumnData[] cda = new ColumnData[v_size];

        for (int i = 0; i < v_size; i++) {
            cda[i] = tableVector.elementAt(i);
        }
        return cda;
    }

    public void setTableColumnData(ColumnData[] cda) {
        tableVector = new Vector<>(cda.length);
        for (ColumnData aCda : cda) {
            addRow(aCda);
        }
        _model.fireTableDataChanged();
    }

    public UDFParameterModel getUDFParameterModel() {
        return _model;
    }

    public Vector<ColumnData> getTableColumnDataVector() {
        return tableVector;
    }

    /**
     * The table view display.
     */
    private class DatabaseTable extends DefaultTable {

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
    } // class DatabaseTable


    /**
     * The table's model.
     */
    public class UDFParameterModel extends AbstractPrintableTableModel {

        protected String[] header = Bundles.get(TableDefinitionPanel.class,
                new String[]
                        {"Datatype", "SizePrecision", "Scale", "Subtype", "Encoding", "ByDescriptor", "NULL", "CSTRING"});

        public UDFParameterModel() {
            tableVector = new Vector<>();
            ColumnData cd = new ColumnData(dc);
            addRow(cd);
        }

        public UDFParameterModel(Vector<ColumnData> data) {
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

                case TYPE_COLUMN:
                    return cd.getColumnType();

                case SIZE_COLUMN:
                    return cd.getColumnSize();

                case SCALE_COLUMN:
                    return cd.getColumnScale();

                case SUBTYPE_COLUMN:
                    return cd.getColumnSubtype();

                case ENCODING_COLUMN:
                    return cd.getCharset();

                case MECHANISM_COLUMN:
                    return cd.getMechanism() == "BY DESCRIPTOR";

                case NULL_COLUMN:
                    return cd.isRequired();

                case CSTRING_COLUMN:
                    return cd.isCstring();

                default:
                    return null;

            }
        }

        public void setValueAt(Object value, int row, int col) {
            ColumnData cd = tableVector.elementAt(row);

            //Log.debug("setValueAt [row: "+row+" col: "+col+" value: "+value+"]");

            switch (col) {
                case TYPE_COLUMN:
                    if (value.getClass() == String.class) {
                        cd.setSQLType(DatabaseTypeConverter.getSQLDataTypeFromName((String) value));
                        cd.setColumnType((String) value);
                        if (!isEditSize(row))
                            _model.setValueAt("-1", row, SIZE_COLUMN);
                        else
                            _model.setValueAt((cd.getSQLType() == Types.BLOB || cd.getSQLType() == Types.LONGVARCHAR
                                    || cd.getSQLType() == Types.LONGVARBINARY) ? "80" : "10", row, SIZE_COLUMN);
                        if (!isEditScale(row))
                            _model.setValueAt("-1", row, SCALE_COLUMN);
                        else
                            _model.setValueAt("0", row, SCALE_COLUMN);
                        if (!isEditSubtype(row))
                            _model.setValueAt((cd.getSQLType() == Types.LONGVARBINARY) ? "0" : "1", row, SUBTYPE_COLUMN);
                        else
                            _model.setValueAt("0", row, SUBTYPE_COLUMN);

                        if (!isEditEncoding(row))
                            cd.setCharset(charsets.get(0));
                    }
                    break;
                case SIZE_COLUMN:
                    cd.setColumnSize(Integer.parseInt((String) value));
                    break;
                case SCALE_COLUMN:
                    cd.setColumnScale(Integer.parseInt((String) value));
                    break;
                case SUBTYPE_COLUMN:
                    cd.setColumnSubtype(Integer.parseInt(value.toString()));
                    break;
                case ENCODING_COLUMN:
                    cd.setCharset((String) value);
                    break;
                case MECHANISM_COLUMN:
                    if ((Boolean) value) {
                        cd.setMechanism("BY DESCRIPTOR");
                        cd.setNotNull(false);
                    } else cd.setMechanism("");
                    break;
                case NULL_COLUMN:
                    cd.setNotNull((Boolean) value);
                    break;
                case CSTRING_COLUMN:
                    cd.setCstring((Boolean) value);
                    if (cd.isCstring())
                        _model.setValueAt(false, row, MECHANISM_COLUMN);
                    break;
            }

            fireTableRowsUpdated(row, row);
        }

        String getStringType(int x) {
            for (int i = 0; i < intDataTypes.length; i++)
                if (x == intDataTypes[i])
                    return dataTypes[i];
            return "";
        }

        boolean isEditEncoding(int row) {
            ColumnData cd = tableVector.elementAt(row);
            return isEditSize(row) && cd.getSQLType() != Types.NUMERIC && cd.getSQLType() != Types.DECIMAL
                    && cd.getSQLType() != Types.BLOB && cd.getColumnType() != "CSTRING";
        }

        boolean isEditSize(int row) {
            ColumnData cd = tableVector.elementAt(row);
            return cd.getColumnType() != null && (cd.getSQLType() == Types.NUMERIC || cd.getSQLType() == Types.CHAR || cd.getSQLType() == Types.VARCHAR
                    || cd.getSQLType() == Types.DECIMAL || cd.getSQLType() == Types.BLOB || cd.getSQLType() == Types.LONGVARCHAR
                    || cd.getSQLType() == Types.LONGVARBINARY
                    || cd.getColumnType().equalsIgnoreCase("VARCHAR")
                    || cd.getColumnType().equalsIgnoreCase("CHAR"))
                    || cd.isCstring();
        }

        boolean isEditMechanism(int row) {
            ColumnData cd = tableVector.elementAt(row);
            return !cd.isCstring();
        }

        boolean isEditScale(int row) {
            ColumnData cd = tableVector.elementAt(row);
            return cd.getSQLType() == Types.NUMERIC || cd.getSQLType() == Types.DECIMAL;
        }

        boolean isEditSubtype(int row) {
            ColumnData cd = tableVector.elementAt(row);
            return cd.getSQLType() == Types.BLOB;
        }

        public boolean isCellEditable(int row, int col) {
            if (editing) {
                switch (col) {
                    case SIZE_COLUMN:
                        return isEditSize(row);
                    case SCALE_COLUMN:
                        return isEditScale(row);
                    case SUBTYPE_COLUMN:
                        return isEditSubtype(row);
                    case ENCODING_COLUMN:
                        return isEditEncoding(row);
                    case MECHANISM_COLUMN:
                        return isEditMechanism(row);
                    default:
                        return editing;
                }
            } else return false;


        }

        public String getColumnName(int col) {
            return header[col];
        }

        public Class getColumnClass(int col) {
            if (col == NULL_COLUMN || col == MECHANISM_COLUMN || col == CSTRING_COLUMN)
                return Boolean.class;
            if (col == SIZE_COLUMN || col == SCALE_COLUMN || col == SUBTYPE_COLUMN) {
                return Integer.class;
            } else {
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
    }

}

package org.executequery.gui.procedure;

import org.executequery.GUIUtilities;
import org.executequery.components.table.BrowsingCellEditor;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.Parameter;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.table.CreateTableSQLSyntax;
import org.executequery.gui.table.DataTypeSelectionListener;
import org.executequery.gui.table.DataTypesDialog;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.print.AbstractPrintableTableModel;
import org.underworldlabs.swing.table.NumberCellEditor;
import org.underworldlabs.swing.table.StringCellEditor;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * @author vasiliy
 */
public abstract class ProcedureDefinitionPanel extends JPanel
        implements TableModelListener, DefinitionPanel {

    /**
     * The table containing all column descriptions
     */
    protected DatabaseTable table;

    /**
     * The table's _model
     */
    protected ProcedureParameterModel _model;

    public static final int TYPE_OF_COLUMN = 2;
    public static final int DOMAIN_COLUMN = 3;
    public static final int TABLE_COLUMN = 4;
    public static final int COLUMN_COLUMN = 5;
    public static final int SIZE_COLUMN = 6;

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

    //protected ComboBoxCellEditor comboCell;

    /**
     * The cell editor for the datatype column
     */
    protected DataTypeSelectionTableCell dataTypeCell;

    protected DomainSelectionTableCell domainCell;

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

    /**
     * the available data types
     */
    private String[] dataTypes;

    private int[] intDataTypes;

    public static final int NAME_COLUMN = 0;

    public static final int TYPE_COLUMN = 1;
    public static final int SCALE_COLUMN = 7;
    public static final int SUBTYPE_COLUMN = 8;
    public static final int DESCRIPTION_COLUMN = 9;
    public static final int DEFAULT_COLUMN = 10;
    public static final int ENCODING_COLUMN = 11;
    public static final int REQUIRED_COLUMN = 12;
    /**
     * The cell editor for the column names
     */
    protected StringCellEditor colNameEditor;
    protected StringCellEditor checkEditor;
    protected StringCellEditor descEditor;
    protected StringCellEditor computedEditor;
    protected StringCellEditor defaultValueEditor;
    DynamicComboBoxModel tableEditorModel;

    private String[] domains;

    DatabaseConnection dc;

    List<String> charsets;

    int parameterType;
    Vector<DefaultCellEditor> listColumnsEditors;

    public ProcedureDefinitionPanel(int parameterType) {
        this(true, null, parameterType);

    }

    public ProcedureDefinitionPanel(boolean editing, String[] dataTypes, int parameterType) {
        super(new GridBagLayout());
        this.editing = editing;
        this.dataTypes = dataTypes;
        this.parameterType = parameterType;

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

    public void setDomains(String[] domains) {
        this.domains = domains;
    }

    /**
     * Sets the available data types to the values specified.
     *
     * @param dataTypes data type values
     */
    public void setDataTypes(String[] dataTypes, int[] intDataTypes) {
        this.dataTypes = dataTypes;
        this.intDataTypes = intDataTypes;
        sortTypes();
        removeDuplicates();
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
        listColumnsEditors = new Vector<>();
        _model = new ProcedureParameterModel(parameterType);
        table = new DatabaseTable(_model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(NAME_COLUMN).setPreferredWidth(200);
        tcm.getColumn(TYPE_COLUMN).setPreferredWidth(130);
        tcm.getColumn(TYPE_OF_COLUMN).setPreferredWidth(130);
        tcm.getColumn(DOMAIN_COLUMN).setPreferredWidth(130);
        tcm.getColumn(TABLE_COLUMN).setPreferredWidth(130);
        tcm.getColumn(COLUMN_COLUMN).setPreferredWidth(130);
        tcm.getColumn(SIZE_COLUMN).setPreferredWidth(50);
        tcm.getColumn(SCALE_COLUMN).setPreferredWidth(70);
        tcm.getColumn(SUBTYPE_COLUMN).setPreferredWidth(70);
        tcm.getColumn(DESCRIPTION_COLUMN).setPreferredWidth(200);
        tcm.getColumn(DEFAULT_COLUMN).setPreferredWidth(200);
        tcm.getColumn(ENCODING_COLUMN).setPreferredWidth(70);
        tcm.getColumn(REQUIRED_COLUMN).setMaxWidth(70);

        // add the editors if editing
        if (editing) {
            colNameEditor = new StringCellEditor();
            DefaultCellEditor colStrEditor = new DefaultCellEditor(colNameEditor) {
                public Object getCellEditorValue() {
                    return colNameEditor.getValue();
                }
            };
            checkEditor = new StringCellEditor();
            DefaultCellEditor checkStrEditor = new DefaultCellEditor(checkEditor) {
                public Object getCellEditorValue() {
                    return checkEditor.getValue();
                }
            };
            descEditor = new StringCellEditor();
            DefaultCellEditor descStrEditor = new DefaultCellEditor(descEditor) {
                public Object getCellEditorValue() {
                    return descEditor.getValue();
                }
            };
            computedEditor = new StringCellEditor();
            DefaultCellEditor computedStrEditor = new DefaultCellEditor(computedEditor) {
                @Override
                public Object getCellEditorValue() {
                    return computedEditor.getValue();
                }
            };
            defaultValueEditor = new StringCellEditor();
            DefaultCellEditor defaultValueStrEditor = new DefaultCellEditor(defaultValueEditor) {
                @Override
                public Object getCellEditorValue() {
                    return defaultValueEditor.getValue();
                }
            };

            tcm.getColumn(NAME_COLUMN).setCellEditor(colStrEditor);
            tcm.getColumn(DESCRIPTION_COLUMN).setCellEditor(descStrEditor);

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

            tableEditorModel = new DynamicComboBoxModel();

            final JComboBox tableEditor = new JComboBox(tableEditorModel);

            DefaultCellEditor tableCellEditor = new DefaultCellEditor(tableEditor);

            tcm.getColumn(SIZE_COLUMN).setCellEditor(szEditor);
            tcm.getColumn(SCALE_COLUMN).setCellEditor(scEditor);
            tcm.getColumn(SUBTYPE_COLUMN).setCellEditor(stEditor);
            domainCell = new DomainSelectionTableCell();
            tcm.getColumn(DOMAIN_COLUMN).setCellRenderer(domainCell);
            tcm.getColumn(DOMAIN_COLUMN).setCellEditor(domainCell);
            dataTypeCell = new DataTypeSelectionTableCell();
            tcm.getColumn(TYPE_COLUMN).setCellRenderer(dataTypeCell);
            tcm.getColumn(TYPE_COLUMN).setCellEditor(dataTypeCell);
            tcm.getColumn(ENCODING_COLUMN).setCellEditor(charsetCellEditor);
            tcm.getColumn(TABLE_COLUMN).setCellEditor(tableCellEditor);
            tcm.getColumn(DEFAULT_COLUMN).setCellEditor(defaultValueStrEditor);

            // create the key listener to notify changes
            KeyAdapter valueKeyListener = new KeyAdapter() {
                public void keyReleased(KeyEvent e) {
                    String value = null;
                    Object object = e.getSource();
                    if (object == colNameEditor) {
                        value = colNameEditor.getValue();
                    } else if (object == checkEditor) {
                        value = checkEditor.getValue();
                    } else if (object == descEditor) {
                        value = descEditor.getValue();
                    } else if (object == computedEditor) {
                        value = computedEditor.getValue();
                    } else if (object == defaultValueEditor) {
                        value = defaultValueEditor.getValue();
                    } else if (object == sizeEditor) {
                        value = sizeEditor.getEditorValue();
                    } else if (object == scaleEditor) {
                        value = scaleEditor.getEditorValue();
                    } else if (object == subtypeEditor) {
                        value = subtypeEditor.getEditorValue();
                    } else if (object == dataTypeCell.getComponent()) {
                        value = dataTypeCell.getEditorValue();
                    } else if (object == domainCell.getComponent()) {
                        value = String.valueOf(domainCell.getEditorValue());
                    } else if (object == charsetCellEditor.getComponent()) {
                        value = String.valueOf(charsetCellEditor.getCellEditorValue());
                    } else if (object == tableCellEditor.getComponent()) {
                        value = String.valueOf(tableCellEditor.getCellEditorValue());
                    } else if (object instanceof JComboBox) {
                        value = String.valueOf(((JComboBox) object).getSelectedItem());
                    }
                    tableChanged(table.getEditingColumn(),
                            table.getEditingRow(),
                            value);
                }
            };
            colNameEditor.addKeyListener(valueKeyListener);
            checkEditor.addKeyListener(valueKeyListener);
            descEditor.addKeyListener(valueKeyListener);
            computedEditor.addKeyListener(valueKeyListener);
            defaultValueEditor.addKeyListener(valueKeyListener);
            dataTypeCell.addKeyListener(valueKeyListener);
            sizeEditor.addKeyListener(valueKeyListener);
            scaleEditor.addKeyListener(valueKeyListener);
            subtypeEditor.addKeyListener(valueKeyListener);
            domainCell.addKeyListener(valueKeyListener);
            charsetEditor.addKeyListener(valueKeyListener);

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
    public abstract void tableChanged(int col, int row, String value);

    /**
     * <p>Adds all the column definition lines to
     * the SQL text buffer for display.
     *
     * @param row current row being edited
     */
    public abstract void addColumnLines(int row);

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

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public DynamicComboBoxModel getColumnEditorModel(int row) {
        return ((DynamicComboBoxModel) ((JComboBox) listColumnsEditors.get(row).getComponent()).getModel());
    }

    public DefaultCellEditor createColumnEditor(ColumnData cd) {
        DynamicComboBoxModel model = new DynamicComboBoxModel();
        if (cd.getColumns() != null)
            model.setElements(cd.getColumns());
        return new DefaultCellEditor(new JComboBox(model));
    }

    public void insertRow(ColumnData cd, int position) {
        tableVector.insertElementAt(cd, position);
        listColumnsEditors.insertElementAt(createColumnEditor(cd), position);

    }

    public void addRow(ColumnData cd) {
        tableVector.add(cd);
        listColumnsEditors.add(createColumnEditor(cd));
    }

    public void removeRow(int position) {
        tableVector.remove(position);
        listColumnsEditors.remove(position);
    }

    public void clearRows() {
        tableVector.clear();
        listColumnsEditors.clear();
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
    public void addRow(Parameter parameter) {
        table.editingStopped(null);
        if (table.isEditing()) {
            table.removeEditor();
        }


        ColumnData cd = SQLUtils.columnDataFromProcedureParameter(parameter, dc);
        addRow(cd);
        table.setEditingRow(tableVector.size() - 1);
        _model.fireTableRowsUpdated(tableVector.size() - 1, tableVector.size() - 1);
        addColumnLines(-1);
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
            cd.setTypeParameter(parameterType);
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

    public abstract String getSQLText();

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
            if (col == COLUMN_COLUMN)
                return listColumnsEditors.get(row);
            return super.getCellEditor(row, col);
        }
    } // class DatabaseTable


    /**
     * The table's model.
     */
    public class ProcedureParameterModel extends AbstractPrintableTableModel {

        protected String[] header = {"Name", "Datatype", "Type of", "Domain", "Table", "Column",
                "Size", "Scale", "Subtype", "Description", "Default Value", "Encoding", "Required"};

        public ProcedureParameterModel(int parameterType) {
            tableVector = new Vector<>();
            ColumnData cd = new ColumnData(dc);
            cd.setTypeParameter(parameterType);
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

                case TYPE_COLUMN:
                    return cd.getColumnType();

                case TYPE_OF_COLUMN:
                    return cd.isTypeOf();

                case DOMAIN_COLUMN:
                    return cd.getDomain();

                case TABLE_COLUMN:
                    return cd.getTable();

                case COLUMN_COLUMN:
                    return cd.getColumnTable();

                case SIZE_COLUMN:
                    return cd.getColumnSize();

                case SCALE_COLUMN:
                    return cd.getColumnScale();

                case SUBTYPE_COLUMN:
                    return cd.getColumnSubtype();

                case DESCRIPTION_COLUMN:
                    return cd.getDescription();

                case DEFAULT_COLUMN:
                    return cd.getDefaultValue();

                case ENCODING_COLUMN:
                    return cd.getCharset();

                case REQUIRED_COLUMN:
                    return cd.isRequired();

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
                case TYPE_COLUMN:
                    if (value.getClass() == String.class) {
                        cd.setColumnType((String) value);
                        if (cd.getSQLType() != cd.getDomainType()) {
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
                                _model.setValueAt((cd.getSQLType() == Types.LONGVARBINARY) ? "0" : "1", row, SCALE_COLUMN);
                            else
                                _model.setValueAt("0", row, SCALE_COLUMN);
                        } else {
                            if (!isEditSize(row))
                                _model.setValueAt("-1", row, SIZE_COLUMN);
                            else
                                _model.setValueAt(String.valueOf(cd.getColumnSize()), row, SIZE_COLUMN);
                            if (!isEditScale(row))
                                _model.setValueAt("-1", row, SCALE_COLUMN);
                            else
                                _model.setValueAt(String.valueOf(cd.getColumnScale()), row, SCALE_COLUMN);
                            if (!isEditSubtype(row))
                                _model.setValueAt((cd.getSQLType() == Types.LONGVARBINARY) ? "0" : "1", row, SUBTYPE_COLUMN);
                            else
                                _model.setValueAt(String.valueOf(cd.getColumnSubtype()), row, SUBTYPE_COLUMN);
                        }
                    } else {
                        cd.setColumnType(dataTypes[(int) value]);
                        cd.setSQLType(intDataTypes[(int) value]);
                        if (cd.getSQLType() != cd.getDomainType()) {
                            _model.setValueAt("", row, DOMAIN_COLUMN);
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
                        } else {
                            if (!isEditSize(row))
                                _model.setValueAt("-1", row, SIZE_COLUMN);
                            else
                                _model.setValueAt(String.valueOf(cd.getColumnSize()), row, SIZE_COLUMN);
                            if (!isEditScale(row))
                                _model.setValueAt("-1", row, SCALE_COLUMN);
                            else
                                _model.setValueAt(String.valueOf(cd.getColumnScale()), row, SCALE_COLUMN);
                            if (!isEditSubtype(row))
                                _model.setValueAt((cd.getSQLType() == Types.LONGVARBINARY) ? "0" : "1", row, SUBTYPE_COLUMN);
                            else
                                _model.setValueAt(String.valueOf(cd.getColumnSubtype()), row, SUBTYPE_COLUMN);
                        }
                        if (!isEditEncoding(row))
                            cd.setCharset(charsets.get(0));
                    }
                    break;
                case TYPE_OF_COLUMN:
                    cd.setTypeOf((Boolean) value);
                    break;
                case DOMAIN_COLUMN:
                    if (value.getClass() == String.class) {
                        cd.setDomain((String) value);
                    } else {
                        cd.setDatabaseConnection(dc);
                        cd.setDomain(domains[(int) value]);
                        cd.setColumnType(getStringType(cd.getDomainType()));
                        _model.setValueAt(cd.getColumnType(), row, TYPE_COLUMN);
                    }
                    cd.setTypeOfFrom(ColumnData.TYPE_OF_FROM_DOMAIN);
                    break;
                case TABLE_COLUMN:
                    cd.setTable((String) value);
                    getColumnEditorModel(row).setElements(cd.getColumns());
                    cd.setTypeOfFrom(ColumnData.TYPE_OF_FROM_COLUMN);
                    _model.setValueAt(cd.getColumnTable(), row, COLUMN_COLUMN);
                    break;
                case COLUMN_COLUMN:
                    cd.setColumnTable((String) value);
                    cd.setTypeOfFrom(ColumnData.TYPE_OF_FROM_COLUMN);
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
                case DESCRIPTION_COLUMN:
                    cd.setDescription((String) value);
                    break;
                case DEFAULT_COLUMN:
                    cd.setDefaultValue((String) value);
                    break;
                case ENCODING_COLUMN:
                    cd.setCharset((String) value);
                    break;
                case REQUIRED_COLUMN:
                    cd.setColumnRequired((Boolean) value ? 0 : 1);
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
            return isEditSize(row) && cd.getSQLType() != Types.NUMERIC && cd.getSQLType() != Types.DECIMAL && cd.getSQLType() != Types.BLOB;
        }

        boolean isEditSize(int row) {
            ColumnData cd = tableVector.elementAt(row);
            return cd.getColumnType() != null && (cd.getSQLType() == Types.NUMERIC || cd.getSQLType() == Types.CHAR || cd.getSQLType() == Types.VARCHAR
                    || cd.getSQLType() == Types.DECIMAL || cd.getSQLType() == Types.BLOB || cd.getSQLType() == Types.LONGVARCHAR
                    || cd.getSQLType() == Types.LONGVARBINARY
                    || cd.getColumnType().toUpperCase().equals("VARCHAR")
                    || cd.getColumnType().toUpperCase().equals("CHAR"));
        }

        boolean isEditScale(int row) {
            ColumnData cd = tableVector.elementAt(row);
            return cd.getSQLType() == Types.NUMERIC || cd.getSQLType() == Types.DECIMAL;
        }

        boolean isEditSubtype(int row) {
            ColumnData cd = tableVector.elementAt(row);
            return cd.getSQLType() == Types.BLOB;
        }

        boolean isEditTableAndColumn(int row) {
            ColumnData cd = tableVector.elementAt(row);
            return cd.isTypeOf();
        }

        public boolean isCellEditable(int row, int col) {
            if (editing) {
                if (isEditTableAndColumn(row))
                    return col == NAME_COLUMN || col == TYPE_OF_COLUMN || col == DOMAIN_COLUMN || col == TABLE_COLUMN
                            || col == COLUMN_COLUMN || col == DESCRIPTION_COLUMN || col == DEFAULT_COLUMN || col == REQUIRED_COLUMN;
                switch (col) {
                    case SIZE_COLUMN:
                        return isEditSize(row);
                    case SCALE_COLUMN:
                        return isEditScale(row);
                    case SUBTYPE_COLUMN:
                        return isEditSubtype(row);
                    case ENCODING_COLUMN:
                        return isEditEncoding(row);
                    case TABLE_COLUMN:
                        return false;
                    case COLUMN_COLUMN:
                        return false;
                    default:
                        return editing;
                }
            } else return false;


        }

        public String getColumnName(int col) {
            return header[col];
        }

        public Class getColumnClass(int col) {
            if (col == REQUIRED_COLUMN || col == TYPE_OF_COLUMN)
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
    } // class CreateTableModel

    private class DataTypeSelectionTableCell extends BrowsingCellEditor
            implements DataTypeSelectionListener {

        private int lastEditingRow;
        private int lastEditingColumn;

        public DataTypeSelectionTableCell() {
        }

        public void actionPerformed(ActionEvent e) {
            // store the current edit row and column
            lastEditingRow = table.getEditingRow();
            lastEditingColumn = table.getEditingColumn();

            fireEditingStopped();
            if (dataTypes == null || dataTypes.length == 0) {
                GUIUtilities.displayWarningMessage("Data type values are not available");
                return;
            }

            SwingUtilities.invokeLater(() -> new DataTypesDialog(GUIUtilities.getParentFrame(),
                    DataTypeSelectionTableCell.this,
                    dataTypes));
        }

        /**
         * Called when the selction is cancelled.
         */
        public void dataTypeSelectionCancelled() {
            fireEditingCanceled();
        }

        /**
         * Called when a data type has been selected.
         *
         * @param item number for item of dataTypes
         */
        public void dataTypeSelected(int item) {
            //setDelegateValue(dataType);
            if (lastEditingRow != -1 && lastEditingColumn != -1) {
                _model.setValueAt(item, lastEditingRow, lastEditingColumn);
                tableChanged(lastEditingColumn, lastEditingRow, dataTypes[item]);
            }
            fireEditingStopped();

            // reset row and column values
            lastEditingRow = -1;
            lastEditingColumn = -1;
        }

    } // class DataTypeSelectionTableCell

    private class DomainSelectionTableCell extends BrowsingCellEditor
            implements DataTypeSelectionListener {

        private int lastEditingRow;
        private int lastEditingColumn;

        public DomainSelectionTableCell() {
        }

        public void actionPerformed(ActionEvent e) {
            // store the current edit row and column
            lastEditingRow = table.getEditingRow();
            lastEditingColumn = table.getEditingColumn();

            fireEditingStopped();
            if (domains == null || domains.length == 0) {
                GUIUtilities.displayWarningMessage("Domains are not available");
                return;
            }

            SwingUtilities.invokeLater(() -> new DataTypesDialog(GUIUtilities.getParentFrame(),
                    DomainSelectionTableCell.this,
                    domains));
        }

        /**
         * Called when the selction is cancelled.
         */
        public void dataTypeSelectionCancelled() {
            fireEditingCanceled();
        }

        /**
         * Called when a data type has been selected.
         *
         * @param item number for item of dataTypes
         */
        public void dataTypeSelected(int item) {
            //setDelegateValue(dataType);
            if (lastEditingRow != -1 && lastEditingColumn != -1) {
                _model.setValueAt(item, lastEditingRow, lastEditingColumn);
                tableChanged(lastEditingColumn, lastEditingRow, domains[item]);
            }
            fireEditingStopped();

            // reset row and column values
            lastEditingRow = -1;
            lastEditingColumn = -1;
        }

    }
}
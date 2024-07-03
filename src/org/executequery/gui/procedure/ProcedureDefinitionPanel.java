package org.executequery.gui.procedure;

import org.executequery.Constants;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.Parameter;
import org.executequery.databaseobjects.Types;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.table.CreateTableSQLSyntax;
import org.executequery.gui.table.CreateTableToolBar;
import org.executequery.gui.table.TableDefinitionPanel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.print.AbstractPrintableTableModel;
import org.underworldlabs.swing.table.ComboBoxCellEditor;
import org.underworldlabs.swing.table.NumberCellEditor;
import org.underworldlabs.swing.celleditor.picker.StringPicker;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * @author vasiliy
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ProcedureDefinitionPanel extends JPanel
        implements DefinitionPanel {

    public static final int NAME_COLUMN = 0;
    public static final int TYPE_COLUMN = 1;
    public static final int TYPE_OF_COLUMN = 2;
    public static final int DOMAIN_COLUMN = 3;
    public static final int TABLE_COLUMN = 4;
    public static final int COLUMN_COLUMN = 5;
    public static final int SIZE_COLUMN = 6;
    public static final int SCALE_COLUMN = 7;
    public static final int SUBTYPE_COLUMN = 8;
    public static final int DESCRIPTION_COLUMN = 9;
    public static final int DEFAULT_COLUMN = 10;
    public static final int ENCODING_COLUMN = 11;
    public static final int REQUIRED_COLUMN = 12;

    protected NumberCellEditor sizeEditor;
    protected NumberCellEditor scaleEditor;
    protected NumberCellEditor subtypeEditor;

    protected ComboBoxCellEditor domainCell;
    protected ComboBoxCellEditor dataTypeCell;

    protected StringPicker descEditor;
    protected StringPicker colNameEditor;
    protected StringPicker defaultValueEditor;

    private String[] domains;
    private String[] dataTypes;
    private int[] intDataTypes;
    private List<String> charsets;

    private DatabaseConnection connection;
    protected Vector<ColumnData> tableVector;

    private DatabaseTable table;
    private ProcedureParameterModel tableModel;

    private CreateTableToolBar tools;
    private DynamicComboBoxModel tableEditorModel;

    private final boolean editing;
    private final int parameterType;
    private final Vector<DefaultCellEditor> listColumnsEditors;

    public ProcedureDefinitionPanel(int parameterType) {
        this(true, null, parameterType);
    }

    public ProcedureDefinitionPanel(boolean editing, String[] dataTypes, int parameterType) {
        super(new GridBagLayout());
        this.editing = editing;
        this.dataTypes = dataTypes;
        this.parameterType = parameterType;
        this.listColumnsEditors = new Vector<>();

        init();
        arrange();
    }

    private void init() {
        tools = new CreateTableToolBar(this);

        tableModel = new ProcedureParameterModel(parameterType);
        table = new DatabaseTable(tableModel);
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

        if (!editing)
            return;

        // --- add the editors if editing ---

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

        defaultValueEditor = new StringPicker();
        DefaultCellEditor defaultValueStrEditor = new DefaultCellEditor(defaultValueEditor) {
            @Override
            public Object getCellEditorValue() {
                return defaultValueEditor.getValue();
            }
        };

        scaleEditor = new NumberCellEditor();
        DefaultCellEditor scEditor = new DefaultCellEditor(scaleEditor) {
            @Override
            public Object getCellEditorValue() {
                return scaleEditor.getStringValue();
            }
        };

        subtypeEditor = new NumberCellEditor();
        DefaultCellEditor stEditor = new DefaultCellEditor(subtypeEditor) {
            @Override
            public Object getCellEditorValue() {
                return subtypeEditor.getStringValue();
            }
        };

        sizeEditor = new NumberCellEditor();
        DefaultCellEditor szEditor = new DefaultCellEditor(sizeEditor) {
            @Override
            public Object getCellEditorValue() {
                return sizeEditor.getStringValue();
            }
        };

        domainCell = new ComboBoxCellEditor();
        dataTypeCell = new ComboBoxCellEditor();
        tableEditorModel = new DynamicComboBoxModel();

        loadCharsets();
        final JComboBox<String> charsetEditor = new JComboBox<>(charsets.toArray(new String[0]));
        DefaultCellEditor charsetCellEditor = new DefaultCellEditor(charsetEditor);

        final JComboBox tablesEditor = new JComboBox(tableEditorModel);
        DefaultCellEditor tableCellEditor = new DefaultCellEditor(tablesEditor);

        tcm.getColumn(NAME_COLUMN).setCellEditor(colStrEditor);
        tcm.getColumn(DESCRIPTION_COLUMN).setCellEditor(descStrEditor);
        tcm.getColumn(SIZE_COLUMN).setCellEditor(szEditor);
        tcm.getColumn(SCALE_COLUMN).setCellEditor(scEditor);
        tcm.getColumn(SUBTYPE_COLUMN).setCellEditor(stEditor);
        tcm.getColumn(DOMAIN_COLUMN).setCellEditor(domainCell);
        tcm.getColumn(TYPE_COLUMN).setCellEditor(dataTypeCell);
        tcm.getColumn(ENCODING_COLUMN).setCellEditor(charsetCellEditor);
        tcm.getColumn(TABLE_COLUMN).setCellEditor(tableCellEditor);
        tcm.getColumn(DEFAULT_COLUMN).setCellEditor(defaultValueStrEditor);
    }

    private void arrange() {
        JPanel definitionPanel = new JPanel(new GridBagLayout());

        definitionPanel.add(tools, new GridBagConstraints(
                0, 0, 1, 2, 0, 0,
                GridBagConstraints.CENTER,
                GridBagConstraints.VERTICAL,
                new Insets(2, 2, 2, 2), 0, 0));

        definitionPanel.add(new JScrollPane(table), new GridBagConstraints(
                1, 1, 0, 1, 1.0, 1.0,
                GridBagConstraints.NORTHEAST,
                GridBagConstraints.BOTH,
                new Insets(2, 2, 2, 2), 0, 0));

        add(definitionPanel, new GridBagConstraints(
                1, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.NORTHEAST,
                GridBagConstraints.BOTH,
                new Insets(2, 2, 2, 2), 0, 0));
    }

    public void setDomains(String[] domains) {
        this.domains = domains;

        domainCell.removeAllItems();
        domainCell.addItem(Constants.EMPTY);
        for (String domain : this.domains)
            domainCell.addItem(domain);
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

        dataTypeCell.removeAllItems();
        for (String dataType : this.dataTypes)
            dataTypeCell.addItem(dataType);
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

    private void sortTypes() {

        if (dataTypes == null)
            return;

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

    private void loadCharsets() {

        if (charsets == null)
            charsets = new ArrayList<>();
        else
            charsets.clear();

        try {
            String resource = FileUtils.loadResource("org/executequery/charsets.properties");
            for (String line : resource.split("\n"))
                if (!line.startsWith("#") && !line.isEmpty())
                    charsets.add(line);

            Collections.sort(charsets);
            charsets.add(0, CreateTableSQLSyntax.NONE);
            charsets.add(0, "");

        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }

    public void setDatabaseConnection(DatabaseConnection databaseConnection) {
        connection = databaseConnection;
        tableEditorModel.setElements(new ColumnData(connection).getTableNames());
        tableVector.forEach(cd -> cd.setConnection(connection));
    }

    public int getEditingRow() {
        return table.getEditingRow();
    }

    public int getSelectedRow() {
        return table.getSelectedRow();
    }

    /**
     * <p>Propagates the call to <code>removeEditor()</code>
     * on the table displaying the data.
     */
    public void removeEditor() {
        table.removeEditor();
    }

    /**
     * <p>Propagates the call to <code>isEditing()</code>
     * on the table displaying the data.
     *
     * @return if a data edit is in progress on the table
     */
    public boolean isEditing() {
        return table.isEditing();
    }

    /**
     * <p>Moves the selected column up one row within
     * the table moving the column above the selection
     * below the selection.
     */
    @Override
    public void moveRowUp() {

        int selection = table.getSelectedRow();
        if (selection == -1 || selection == 0)
            return;

        table.editingStopped(null);
        if (table.isEditing())
            table.removeEditor();

        int newPosition = selection - 1;
        ColumnData rowToMove = tableVector.elementAt(selection);
        removeRow(selection);

        insertRow(rowToMove, newPosition);
        table.setRowSelectionInterval(newPosition, newPosition);
        tableModel.fireTableRowsUpdated(newPosition, selection);
    }

    /**
     * <p>Moves the selected column down one row within
     * the table moving the column below the selection
     * above the selection.
     */
    @Override
    public void moveRowDown() {

        int selection = table.getSelectedRow();
        if (selection == -1 || selection == tableVector.size() - 1)
            return;

        table.editingStopped(null);
        if (table.isEditing())
            table.removeEditor();

        int newPosition = selection + 1;
        ColumnData rowToMove = tableVector.elementAt(selection);
        removeRow(selection);

        insertRow(rowToMove, newPosition);
        table.setRowSelectionInterval(newPosition, newPosition);
        tableModel.fireTableRowsUpdated(selection, newPosition);
    }

    public DynamicComboBoxModel getColumnEditorModel(int row) {
        return ((DynamicComboBoxModel) ((JComboBox<?>) listColumnsEditors.get(row).getComponent()).getModel());
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
        if (table.isEditing())
            table.removeEditor();
    }

    public void addRow(Parameter parameter) {

        table.editingStopped(null);
        if (table.isEditing())
            table.removeEditor();

        addRow(SQLUtils.columnDataFromProcedureParameter(parameter, connection, true));
        table.setEditingRow(tableVector.size() - 1);
        tableModel.fireTableRowsUpdated(tableVector.size() - 1, tableVector.size() - 1);
    }

    /**
     * <p>Deletes the selected row from the table.
     * This will also modify the SQL generated text.
     */
    @Override
    public void deleteRow() {

        table.editingStopped(null);
        if (table.isEditing())
            table.removeEditor();

        int selection = table.getSelectedRow();
        if (selection == -1 || tableVector.isEmpty())
            return;

        removeRow(selection);
        tableModel.fireTableRowsDeleted(selection, selection);

        if (tableVector.isEmpty()) {
            addRow(new ColumnData(true, connection));
            tableModel.fireTableRowsInserted(0, 0);
        }
    }

    public void deleteEmptyRow() {

        table.editingStopped(null);
        if (table.isEditing())
            table.removeEditor();

        if (tableVector.isEmpty())
            return;

        removeRow(0);
        tableModel.fireTableRowsDeleted(0, 0);
    }

    /**
     * <p>Inserts a new column after the selected
     * column moving the selected column up one row.
     */
    @Override
    public void addRow() {
        fireEditingStopped();

        int lastRow = tableVector.size() - 1;
        int newRow = lastRow + 1;

        ColumnData columnData = new ColumnData(connection);
        columnData.setTypeParameter(parameterType);
        addRow(columnData);

        tableModel.fireTableRowsInserted(lastRow, newRow);
        table.setRowSelectionInterval(newRow, newRow);
        table.setColumnSelectionInterval(1, 1);

        table.setEditingRow(newRow);
        table.setEditingColumn(NAME_COLUMN);
        ((DefaultCellEditor) table.getCellEditor(newRow, NAME_COLUMN)).getComponent().requestFocus();
    }

    public void setRowSelectionInterval(int row) {
        table.setRowSelectionInterval(row, row);
    }

    public ColumnData[] getTableColumnData() {

        int vectorSize = tableVector.size();
        ColumnData[] columnData = new ColumnData[vectorSize];
        for (int i = 0; i < vectorSize; i++)
            columnData[i] = tableVector.elementAt(i);

        return columnData;
    }

    public ProcedureParameterModel getProcedureParameterModel() {
        return tableModel;
    }

    public Vector<ColumnData> getTableColumnDataVector() {
        return tableVector;
    }

    /**
     * The table view display.
     */
    private class DatabaseTable extends DefaultTable {

        public DatabaseTable(TableModel tableModel) {
            super(tableModel);

            getTableHeader().setReorderingAllowed(false);
            setCellSelectionEnabled(true);
            setColumnSelectionAllowed(false);
            setRowSelectionAllowed(false);
            setSurrendersFocusOnKeystroke(true);
        }

        @Override
        public TableCellEditor getCellEditor(int row, int col) {
            if (col == COLUMN_COLUMN)
                return listColumnsEditors.get(row);
            return super.getCellEditor(row, col);
        }

    } // DatabaseTable class

    /**
     * The table's model.
     */
    public class ProcedureParameterModel extends AbstractPrintableTableModel {

        protected String[] header = Bundles.get(TableDefinitionPanel.class, new String[]{
                "Name", "Datatype", "TypeOf", "Domain", "Table", "Column", "SizePrecision",
                "Scale", "Subtype", "Description", "DefaultValue", "Encoding", "Required"
        });

        public ProcedureParameterModel(int parameterType) {
            tableVector = new Vector<>();
            ColumnData cd = new ColumnData(connection);
            cd.setTypeParameter(parameterType);
            addRow(cd);
        }

        @Override
        public int getColumnCount() {
            return header.length;
        }

        @Override
        public int getRowCount() {
            return tableVector.size();
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

            if (col >= 0) {
                Object value = getValueAt(row, col);
                if (value != null)
                    return value.toString();
            }

            return Constants.EMPTY;
        }

        @Override
        public Object getValueAt(int row, int col) {

            if (row >= tableVector.size())
                return null;

            ColumnData cd = tableVector.elementAt(row);
            switch (col) {

                case NAME_COLUMN:
                    return cd.getColumnName();

                case TYPE_COLUMN:
                    return cd.getTypeName();

                case TYPE_OF_COLUMN:
                    return cd.isTypeOf();

                case DOMAIN_COLUMN:
                    return cd.getDomain();

                case TABLE_COLUMN:
                    return cd.getTable();

                case COLUMN_COLUMN:
                    return cd.getColumnTable();

                case SIZE_COLUMN:
                    return cd.getSize();

                case SCALE_COLUMN:
                    return cd.getScale();

                case SUBTYPE_COLUMN:
                    return cd.getSubtype();

                case DESCRIPTION_COLUMN:
                    return cd.getRemarks();

                case DEFAULT_COLUMN:
                    ColumnData.DefaultValue value = cd.getDefaultValue();
                    return value != null ? value.getValue() : null;

                case ENCODING_COLUMN:
                    return cd.getCharset();

                case REQUIRED_COLUMN:
                    return cd.isNotNull();
            }

            return null;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            ColumnData cd = tableVector.elementAt(row);
            switch (col) {

                case NAME_COLUMN:
                    cd.setColumnName((String) value);
                    break;

                case TYPE_COLUMN:
                    if (value.getClass() == String.class) {
                        cd.setTypeName((String) value);

                    } else {
                        cd.setTypeName(dataTypes[(int) value]);
                        cd.setSQLType(intDataTypes[(int) value]);
                    }

                    String stringValue = isEditSize(row) ? String.valueOf(cd.getSize()) : "-1";
                    tableModel.setValueAt(stringValue, row, SIZE_COLUMN);

                    stringValue = isEditScale(row) ? String.valueOf(cd.getScale()) : "-1";
                    tableModel.setValueAt(stringValue, row, SCALE_COLUMN);

                    stringValue = isEditSubtype(row) ? String.valueOf(cd.getSubtype()) : (cd.getSQLType() == Types.LONGVARBINARY) ? "0" : "1";
                    tableModel.setValueAt(stringValue, row, SUBTYPE_COLUMN);

                    if (!isEditEncoding(row))
                        cd.setCharset(charsets.get(0));

                    break;

                case TYPE_OF_COLUMN:
                    cd.setTypeOf((Boolean) value);
                    break;

                case DOMAIN_COLUMN:
                    if (value.getClass() == String.class) {
                        cd.setDomain((String) value);

                    } else {
                        cd.setConnection(connection);
                        cd.setDomain(domains[(int) value]);
                        cd.setTypeName(getStringType(cd.getSQLType()));
                        tableModel.setValueAt(cd.getTypeName(), row, TYPE_COLUMN);
                    }

                    cd.setTypeOfFrom(ColumnData.TYPE_OF_FROM_DOMAIN);
                    break;

                case TABLE_COLUMN:
                    cd.setTable((String) value);
                    getColumnEditorModel(row).setElements(cd.getColumns());
                    cd.setTypeOfFrom(ColumnData.TYPE_OF_FROM_COLUMN);
                    tableModel.setValueAt(cd.getColumnTable(), row, COLUMN_COLUMN);
                    break;

                case COLUMN_COLUMN:
                    cd.setColumnTable((String) value);
                    cd.setTypeOfFrom(ColumnData.TYPE_OF_FROM_COLUMN);
                    break;

                case SIZE_COLUMN:
                    cd.setSize(Integer.parseInt((String) value));
                    break;

                case SCALE_COLUMN:
                    cd.setScale(Integer.parseInt((String) value));
                    break;

                case SUBTYPE_COLUMN:
                    cd.setSubtype(Integer.parseInt(value.toString()));
                    break;

                case DESCRIPTION_COLUMN:
                    cd.setRemarks((String) value);
                    break;

                case DEFAULT_COLUMN:
                    cd.setDefaultValue((String) value);
                    break;

                case ENCODING_COLUMN:
                    cd.setCharset((String) value);
                    break;

                case REQUIRED_COLUMN:
                    cd.setNotNull((Boolean) value);
                    break;
            }

            fireTableRowsUpdated(row, row);
        }

        private String getStringType(int x) {
            for (int i = 0; i < intDataTypes.length; i++)
                if (x == intDataTypes[i])
                    return dataTypes[i];

            return Constants.EMPTY;
        }

        boolean isEditEncoding(int row) {
            int type = tableVector.elementAt(row).getSQLType();
            return isEditSize(row) && type != Types.NUMERIC && type != Types.DECIMAL && type != Types.BLOB;
        }

        boolean isEditSize(int row) {

            ColumnData cd = tableVector.elementAt(row);
            String typeName = cd.getTypeName();
            int type = cd.getSQLType();

            return typeName != null
                    && (type == Types.NUMERIC
                    || type == Types.CHAR
                    || type == Types.VARCHAR
                    || type == Types.DECIMAL
                    || type == Types.BLOB
                    || type == Types.LONGVARCHAR
                    || type == Types.LONGVARBINARY
                    || typeName.equalsIgnoreCase("VARCHAR")
                    || typeName.equalsIgnoreCase("CHAR"));
        }

        boolean isEditScale(int row) {
            int type = tableVector.elementAt(row).getSQLType();
            return type == Types.NUMERIC || type == Types.DECIMAL;
        }

        boolean isEditSubtype(int row) {
            ColumnData cd = tableVector.elementAt(row);
            return cd.getSQLType() == Types.BLOB;
        }

        boolean isEditTableAndColumn(int row) {
            ColumnData cd = tableVector.elementAt(row);
            return cd.isTypeOf();
        }

        @Override
        public boolean isCellEditable(int row, int col) {

            if (!editing)
                return false;

            if (isEditTableAndColumn(row)) {
                return col == NAME_COLUMN
                        || col == TYPE_OF_COLUMN
                        || col == DOMAIN_COLUMN
                        || col == TABLE_COLUMN
                        || col == COLUMN_COLUMN
                        || col == DESCRIPTION_COLUMN
                        || col == DEFAULT_COLUMN
                        || col == REQUIRED_COLUMN;
            }

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
                case COLUMN_COLUMN:
                    return false;
            }

            return true;
        }

        @Override
        public String getColumnName(int col) {
            return header[col];
        }

        @Override
        public Class<?> getColumnClass(int col) {

            if (col == REQUIRED_COLUMN || col == TYPE_OF_COLUMN)
                return Boolean.class;

            if (col == SIZE_COLUMN || col == SCALE_COLUMN || col == SUBTYPE_COLUMN)
                return Integer.class;

            return String.class;
        }

        public Vector<ColumnData> getTableVector() {
            return tableVector;
        }

    } // CreateTableModel class

}

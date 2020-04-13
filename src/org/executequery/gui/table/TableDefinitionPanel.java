/*
 * TableDefinitionPanel.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.gui.table;

import org.executequery.GUIUtilities;
import org.executequery.components.table.BrowsingCellEditor;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.browser.ColumnData;
import org.underworldlabs.swing.print.AbstractPrintableTableModel;
import org.underworldlabs.swing.table.NumberCellEditor;
import org.underworldlabs.swing.table.StringCellEditor;
import org.underworldlabs.util.FileUtils;

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
 * @author Takis Diakoumis
 */
public abstract class TableDefinitionPanel extends JPanel
        implements TableModelListener {

    /**
     * The table containing all column descriptions
     */
    protected DatabaseTable table;

    /**
     * The table's _model
     */
    protected CreateTableModel _model;

    /**
     * The cell editor for the column names
     */
    protected static StringCellEditor colNameEditor;

    protected static StringCellEditor checkEditor;

    protected static StringCellEditor descEditor;

    protected static StringCellEditor computedEditor;

    protected static StringCellEditor defaultValueEditor;

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

    /** The cell editor for the datatype column */
    //protected ComboBoxCellEditor comboCell;

    /**
     * The cell editor for the datatype column
     */
    protected DataTypeSelectionTableCell dataTypeCell;

    protected DomainSelectionTableCell domainCell;

    /**
     * The <code>Vector</code> of <code>ColumnData</code> objects
     */
    protected Vector<ColumnData> tableVector;

    /**
     * The literal 'PK'
     */
    private static final String PRIMARY = "PK";

    /**
     * The literal 'FK'
     */
    private static final String FOREIGN = "FK";

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

    public static final int PK_COLUMN = 0;

    public static final int NAME_COLUMN = 1;

    public static final int TYPE_COLUMN = 2;

    public static final int DOMAIN_COLUMN = 3;

    public static final int SIZE_COLUMN = 4;

    public static final int SCALE_COLUMN = 5;

    public static final int SUBTYPE_COLUMN = 6;

    public static final int REQUIRED_COLUMN = 7;

    public static final int CHECK_COLUMN = 8;

    public static final int DESCRIPTION_COLUMN = 9;

    public static final int COMPUTED_BY_COLUMN = 10;

    public static final int AUTOINCREMENT_COLUMN = 11;

    public static final int DEFAULT_COLUMN = 12;

    public static final int ENCODING_COLUMN = 13;

    public static final String SUBSTITUTE_NAME = "<TABLE_NAME>";

    private String[] domains;

    private String[] generators;

    DatabaseConnection dc;

    List<String> charsets;

    String AutoincrementSQLText = "";

    public TableDefinitionPanel() {
        this(true, null);
    }

    public TableDefinitionPanel(boolean editing, String[] dataTypes) {
        super(new GridBagLayout());
        this.editing = editing;
        this.dataTypes = dataTypes;

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void jbInit() throws Exception {
        // set the table model to use
        _model = new CreateTableModel();
        table = new DatabaseTable(_model);
        table.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                click(mouseEvent);
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {

            }
        });
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(PK_COLUMN).setPreferredWidth(25);
        tcm.getColumn(PK_COLUMN).setMaxWidth(25);
        tcm.getColumn(NAME_COLUMN).setPreferredWidth(200);
        tcm.getColumn(TYPE_COLUMN).setPreferredWidth(130);
        tcm.getColumn(DOMAIN_COLUMN).setPreferredWidth(130);
        tcm.getColumn(SIZE_COLUMN).setPreferredWidth(50);
        tcm.getColumn(SCALE_COLUMN).setPreferredWidth(70);
        tcm.getColumn(SUBTYPE_COLUMN).setPreferredWidth(70);
        tcm.getColumn(REQUIRED_COLUMN).setPreferredWidth(70);
        tcm.getColumn(REQUIRED_COLUMN).setMaxWidth(70);
        tcm.getColumn(CHECK_COLUMN).setPreferredWidth(200);
        tcm.getColumn(DESCRIPTION_COLUMN).setPreferredWidth(200);
        tcm.getColumn(COMPUTED_BY_COLUMN).setPreferredWidth(200);
        tcm.getColumn(DEFAULT_COLUMN).setPreferredWidth(200);
        tcm.getColumn(AUTOINCREMENT_COLUMN).setPreferredWidth(70);
        tcm.getColumn(ENCODING_COLUMN).setPreferredWidth(200);

        tcm.getColumn(PK_COLUMN).setCellRenderer(new KeyCellRenderer());

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
            tcm.getColumn(CHECK_COLUMN).setCellEditor(checkStrEditor);
            tcm.getColumn(DESCRIPTION_COLUMN).setCellEditor(descStrEditor);
            tcm.getColumn(COMPUTED_BY_COLUMN).setCellEditor(computedStrEditor);

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
            domainCell = new DomainSelectionTableCell();
            tcm.getColumn(DOMAIN_COLUMN).setCellRenderer(domainCell);
            tcm.getColumn(DOMAIN_COLUMN).setCellEditor(domainCell);
            dataTypeCell = new DataTypeSelectionTableCell();
            tcm.getColumn(TYPE_COLUMN).setCellRenderer(dataTypeCell);
            tcm.getColumn(TYPE_COLUMN).setCellEditor(dataTypeCell);
            tcm.getColumn(ENCODING_COLUMN).setCellEditor(charsetCellEditor);

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
        add(new JScrollPane(table), new GridBagConstraints(
                1, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.SOUTHEAST,
                GridBagConstraints.BOTH,
                new Insets(2, 2, 2, 2), 0, 0));

    }

    private void click(MouseEvent e) {
        if (e.getClickCount() > 1) {
            int row = table.getSelectedRow();
            if (table.getSelectedColumn() == PK_COLUMN) {
                tableVector.elementAt(row).setPrimaryKey(!tableVector.elementAt(row).isPrimaryKey());
                _model.setValueAt(null, row, PK_COLUMN);
                tableChanged(PK_COLUMN, row, null);

            } else if (table.getSelectedColumn() == AUTOINCREMENT_COLUMN) {
                if (tableVector.elementAt(row).isAutoincrement()) {
                    tableVector.elementAt(row).getAutoincrement().setCreateGenerator(false);
                    tableVector.elementAt(row).getAutoincrement().setCreateTrigger(false);
                    tableVector.elementAt(row).getAutoincrement().setIdentity(false);
                    tableVector.elementAt(row).getAutoincrement().setSqlAutoincrement("");
                    _model.setValueAt(null, row, AUTOINCREMENT_COLUMN);
                    tableChanged(AUTOINCREMENT_COLUMN, row, null);
                } else {
                    BaseDialog dialog = new BaseDialog("Autoincrement", true);
                    AutoIncrementPanel panel = new AutoIncrementPanel(dc, dialog, tableVector.elementAt(row).getAutoincrement(), SUBSTITUTE_NAME, generators);
                    dialog.addDisplayComponent(panel);
                    dialog.display();
                    _model.setValueAt(null, row, AUTOINCREMENT_COLUMN);
                    tableChanged(AUTOINCREMENT_COLUMN, row, null);
                }

            }
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

    public void setGenerators(String[] generators) {
        this.generators = generators;
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

    public void setDatabaseConnection(DatabaseConnection databaseConnection) {
        dc = databaseConnection;
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
        if (row == -1) {
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
                charsets = new ArrayList<String>();
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
            return;
        }
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
        tableVector.removeElementAt(selection);
        tableVector.add(newPostn, move);
        table.setRowSelectionInterval(newPostn, newPostn);
        _model.fireTableRowsUpdated(newPostn, selection);
        addColumnLines(-1);
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
        tableVector.removeElementAt(selection);
        tableVector.add(newPostn, move);
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
        if (selection == -1) {
            return;
        } else {
            tableVector.insertElementAt(new ColumnData(dc), selection);
        }

        _model.fireTableRowsInserted(
                selection == 0 ? 0 : selection - 1,
                selection == 0 ? 1 : selection);

        table.setRowSelectionInterval(selection, selection);
        table.setColumnSelectionInterval(1, 1);

        table.setEditingRow(selection);
        table.setEditingColumn(NAME_COLUMN);

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

        tableVector.removeElementAt(selection);
        _model.fireTableRowsDeleted(selection, selection);

        if (tableVector.size() == 0) {
            tableVector.addElement(new ColumnData(true, dc));
            _model.fireTableRowsInserted(0, 0);
        }

        addColumnLines(-1);
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

        if (selection == -1) {
            return;
        } else if (selection == tableVector.size()) {
            tableVector.add(new ColumnData(dc));
        } else {
            tableVector.add(newRow, new ColumnData(dc));
        }

        _model.fireTableRowsInserted(selection, newRow);
        table.setRowSelectionInterval(newRow, newRow);
        table.setColumnSelectionInterval(1, 1);

        table.setEditingRow(newRow);
        table.setEditingColumn(NAME_COLUMN);
        ((DefaultCellEditor) table.getCellEditor(newRow, NAME_COLUMN)).
                getComponent().requestFocus();
    }

    public TableCellEditor getCellEditor(int row, int col) {
        return table.getCellEditor(row, col);
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
        tableVector = new Vector<ColumnData>(cda.length);
        for (int i = 0; i < cda.length; i++) {
            tableVector.add(cda[i]);
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

    public Vector<ColumnData> getTableColumnDataVector() {
        return tableVector;
    }

    /**
     * The table view display.
     */

    public void stopEditing() {
        table.stopEditing();
    }
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
            int mouseX = e.getX();
            int mouseY = e.getY();

            int col = columnAtPoint(new Point(mouseX, mouseY));
            if (col != PK_COLUMN) {
                return;
            }

            ColumnData[] cda = getTableColumnData();
            int row = rowAtPoint(new Point(mouseX, mouseY));
            for (int i = 0; i < cda.length; i++) {
                if (i == row && !cda[i].isPrimaryKey()) {
                    cda[i].setPrimaryKey(true);
                } else {
                    cda[i].setPrimaryKey(false);
                }
            }

            _model.fireTableRowsUpdated(0, cda.length);
            addColumnLines(-1);
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void stopEditing() {
            if (isEditing())
                getCellEditor().stopCellEditing();
        }

    } // class DatabaseTable


    /**
     * The table's model.
     */
    protected class CreateTableModel extends AbstractPrintableTableModel {

        protected String[] header = {"PK", "Name", "Datatype", "Domain",
                "Size", "Scale", "Subtype", "Required", "Check",
                "Description", "Computed by", "Autoincrement", "Default Value",
                "Encoding"};

        public CreateTableModel() {
            tableVector = new Vector<ColumnData>();
            tableVector.addElement(new ColumnData(dc));
        }

        public CreateTableModel(Vector<ColumnData> data) {
            tableVector = data;
        }

        public void setColumnDataArray(ColumnData[] cda) {

            if (cda != null) {
                if (tableVector == null) {
                    tableVector = new Vector<ColumnData>(cda.length);
                } else {
                    tableVector.clear();
                }

                for (int i = 0; i < cda.length; i++) {
                    tableVector.add(cda[i]);
                }
            } else {
                tableVector.clear();
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
            if (col != PK_COLUMN && col >= 0) {
                Object value = getValueAt(row, col);
                if (value != null) {
                    return value.toString();
                }
                return EMPTY;
            } else if (col == PK_COLUMN) {
                ColumnData cd = tableVector.elementAt(row);
                if (cd.isPrimaryKey()) {
                    if (cd.isForeignKey()) {
                        return "PFK";
                    }
                    return "PK";
                } else if (cd.isForeignKey()) {
                    return "FK";
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

                case PK_COLUMN:
                    return cd;
                case NAME_COLUMN:
                    return cd.getColumnName();

                case TYPE_COLUMN:
                    return cd.getColumnType();

                case DOMAIN_COLUMN:
                    return cd.getDomain();

                case SIZE_COLUMN:
                    return Integer.valueOf(cd.getColumnSize());

                case SCALE_COLUMN:
                    return Integer.valueOf(cd.getColumnScale());

                case SUBTYPE_COLUMN:
                    return Integer.valueOf(cd.getColumnSubtype());

                case REQUIRED_COLUMN:
                    return Boolean.valueOf(cd.isRequired());

                case CHECK_COLUMN:
                    return cd.getCheck();

                case DESCRIPTION_COLUMN:
                    return cd.getDescription();

                case COMPUTED_BY_COLUMN:
                    return cd.getComputedBy();

                case DEFAULT_COLUMN:
                    return cd.getDefaultValue();

                case AUTOINCREMENT_COLUMN:
                    return cd.isAutoincrement();

                case ENCODING_COLUMN:
                    return cd.getCharset();

                default:
                    return null;

            }
        }

        public void setValueAt(Object value, int row, int col) {
            ColumnData cd = tableVector.elementAt(row);

            //Log.debug("setValueAt [row: "+row+" col: "+col+" value: "+value+"]");

            switch (col) {
                case PK_COLUMN:
                    if (cd.isPrimaryKey()) {
                        cd.setKeyType(PRIMARY);
                    } else if (cd.isForeignKey()) {
                        cd.setKeyType(FOREIGN);
                    } else {
                        cd.setKeyType(null);
                    }
                    break;
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
                case DOMAIN_COLUMN:
                    if (value.getClass() == String.class) {
                        cd.setDomain((String) value);
                    } else {
                        cd.setDatabaseConnection(dc);
                        cd.setDomain(domains[(int) value]);
                        cd.setColumnType(getStringType(cd.getDomainType()));
                        _model.setValueAt(cd.getColumnType(), row, TYPE_COLUMN);
                    }
                    break;
                case SIZE_COLUMN:
                    cd.setColumnSize(Integer.parseInt((String) value));
                    break;
                case SCALE_COLUMN:
                    cd.setColumnScale(Integer.parseInt((String) value));
                    break;
                case SUBTYPE_COLUMN:
                    cd.setColumnSubtype(Integer.parseInt((String) value));
                    break;
                case REQUIRED_COLUMN:
                    cd.setColumnRequired(((Boolean) value).booleanValue() ? 0 : 1);
                    break;
                case CHECK_COLUMN:
                    cd.setCheck((String) value);
                    break;
                case DESCRIPTION_COLUMN:
                    cd.setDescription((String) value);
                    break;
                case COMPUTED_BY_COLUMN:
                    cd.setComputedBy((String) value);
                    break;
                case DEFAULT_COLUMN:
                    cd.setDefaultValue((String) value);
                case AUTOINCREMENT_COLUMN:
                    break;
                case ENCODING_COLUMN:
                    cd.setCharset((String) value);
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

        public boolean isCellEditable(int row, int col) {
            if (editing)
                switch (col) {
                    case PK_COLUMN:
                        return false;
                    case SIZE_COLUMN:
                        return isEditSize(row);
                    case SCALE_COLUMN:
                        return isEditScale(row);
                    case SUBTYPE_COLUMN:
                        return isEditSubtype(row);
                    case AUTOINCREMENT_COLUMN:
                        return false;
                    case ENCODING_COLUMN:
                        return isEditEncoding(row);
                    default:
                        return editing;
                }
            else return editing;

        }

        public String getColumnName(int col) {
            return header[col];
        }

        public Class getColumnClass(int col) {
            if (col == REQUIRED_COLUMN || col == AUTOINCREMENT_COLUMN) {
                return Boolean.class;
            } else if (col == SIZE_COLUMN || col == SCALE_COLUMN || col == SUBTYPE_COLUMN) {
                return Integer.class;
            } else {
                return String.class;
            }
        }

        public void addNewRow() {
            ColumnData cd = tableVector.lastElement();
            if (!cd.isNewColumn()) {
                tableVector.addElement(new ColumnData(true, dc));
            }

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

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new DataTypesDialog(GUIUtilities.getParentFrame(),
                            DataTypeSelectionTableCell.this,
                            dataTypes);
                }
            });
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

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new DataTypesDialog(GUIUtilities.getParentFrame(),
                            DomainSelectionTableCell.this,
                            domains);
                }
            });
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

    public String getAutoincrementSQLText() {
        return AutoincrementSQLText;
    }
}















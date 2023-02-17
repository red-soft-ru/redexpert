/*
 * TableConstraintsPanel.java
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

import org.executequery.Constants;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.browser.ColumnConstraint;
import org.executequery.gui.browser.ColumnData;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.table.ComboBoxCellEditor;
import org.underworldlabs.swing.table.StringCellEditor;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import static org.executequery.databaseobjects.NamedObject.*;

/**
 * @author Takis Diakoumis
 */
public abstract class TableConstraintsPanel extends JPanel
        implements CreateTableSQLSyntax {

    /**
     * The table containing the constraint data
     */
    protected JTable table;

    /**
     * The table's model
     */
    protected ColumnConstraintModel model;

    /**
     * The constraint name cell editor
     */
    protected StringCellEditor conNameEditor;

    /**
     * The string cell editor
     */
    protected DefaultCellEditor strEditor;

    /**
     * The keys combo box cell editor
     */
    protected ComboBoxCellEditor keysCombo;

    public static final int X = 0;
    public static final int TYPE = X + 1;
    public static final int NAME = TYPE + 1;
    public static final int TABLE_COLUMN = NAME + 1;
    public static final int REFERENCE_TABLE = TABLE_COLUMN + 1;
    public static final int REFERENCE_COLUMN = REFERENCE_TABLE + 1;
    public static final int UPDATE_RULE = REFERENCE_COLUMN + 1;
    public static final int DELETE_RULE = UPDATE_RULE + 1;


    public TableConstraintsPanel() {
        super(new BorderLayout());

        try {
            jbInit();
        } catch (Exception e) {
            Log.error("Error init class TableConstraintsPanel:", e);
        }

    }

    private void jbInit() throws Exception {
        table = new DefaultTable();

        conNameEditor = new StringCellEditor();

        // create the key listener to notify changes
        KeyAdapter colKeyListener = new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                columnValuesChanged(table.getEditingColumn(),
                        table.getEditingRow(),
                        conNameEditor.getValue());
            }
        };

        conNameEditor.addKeyListener(colKeyListener);

        if (getMode() == CREATE_TABLE_MODE) {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }
        JScrollPane jScrollPane = new JScrollPane(table);

        add(jScrollPane, BorderLayout.CENTER);

        keysCombo = new ComboBoxCellEditor(CreateTableSQLSyntax.KEY_NAMES);

        strEditor = new DefaultCellEditor(conNameEditor) {
            public Object getCellEditorValue() {
                return conNameEditor.getValue();
            }
        };
    }

    public abstract ColumnData[] getTableColumnData();

    public abstract void updateCellEditor(int col, int row, String value);

    public abstract void columnValuesChanged();

    public abstract void columnValuesChanged(int col, int row, String value);

    public abstract int getMode();

    public Vector getKeys() {
        return model.getKeys();
    }

    public ColumnConstraint[] getColumnConstraintArray() {
        Vector keys = model.getKeys();
        int v_size = keys.size();

        ColumnConstraint[] cca = new ColumnConstraint[v_size];
        for (int i = 0; i < v_size; i++) {
            cca[i] = new ColumnConstraint();
            cca[i].setValues((ColumnConstraint) keys.elementAt(i));
        }

        return cca;
    }

    public void fireEditingStopped() {
        table.editingStopped(null);
        if (table.isEditing()) {
            table.removeEditor();
        }
    }

    public void stopEditing() {
        if (table.isEditing())
            table.getCellEditor().stopCellEditing();
    }

    public void setData(Vector keys, boolean fillCombos) {

        boolean keysEmpty = keys.isEmpty();

        if (table.isEditing()) {
            table.removeEditor();
        }

        // add an empty constraint for input if vector empty
        if (keysEmpty) {
            keys.add(new ColumnConstraint(getMode() == EDIT_TABLE_MODE));
        }

        if (model == null) {
            model = new ColumnConstraintModel(keys);
            setModel(model);
            setColumnProperties();
        } else {
            model.setNewData(keys);
            setModel(model);
        }

        if (keysEmpty || fillCombos) {
            try {
                table.getColumnModel().getColumn(TYPE).setCellEditor(keysCombo);
                table.getColumnModel().getColumn(TABLE_COLUMN).setCellEditor(
                        new ComboBoxCellEditor(getTableColumnData()));
            } catch (ArrayIndexOutOfBoundsException e) { // TODO: what is this - test
                Log.error("Error method setData in class TableConstraintsPanel:", e);
            }
        }

        model.fireTableDataChanged();
    }

    public int getSelectedRow() {
        return table.getSelectedRow();
    }

    public void insertRowAfter() {
        model.insertRowAfter(getMode() == EDIT_TABLE_MODE);
    }

    public void deleteSelectedRow() {
        int row = table.getSelectedRow();
        if (row == -1) {
            return;
        }

        table.editingStopped(null);
        if (table.isEditing()) {
            table.removeEditor();
        }

        model.deleteRow(row);
        model.fireTableRowsDeleted(row, row);

        if (model.getKeys().size() == 0) {
            model.insertRowAfter(getMode() == EDIT_TABLE_MODE);
            table.setEditingRow(0);
        } else {
            table.setEditingRow(row);
        }

        table.setEditingColumn(TYPE);
        columnValuesChanged();
    }

    public void setCellEditor(int col, TableCellEditor editor) {
        table.getColumnModel().getColumn(col).setCellEditor(editor);
    }

    /**
     * Sets some default column property values on the table
     * display such as renderers, editors and column widths.
     */
    protected void setColumnProperties() {
        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(X).setPreferredWidth(25);
        //tcm.getColumn(0).setMinWidth(25);
        tcm.getColumn(X).setMaxWidth(25);
        tcm.getColumn(NAME).setPreferredWidth(125);
        tcm.getColumn(TYPE).setPreferredWidth(75);
        tcm.getColumn(TABLE_COLUMN).setPreferredWidth(110);
        tcm.getColumn(REFERENCE_TABLE).setPreferredWidth(120);
        tcm.getColumn(REFERENCE_COLUMN).setPreferredWidth(120);

        tcm.getColumn(X).setCellRenderer(new ConstraintCellRenderer());
        tcm.getColumn(NAME).setCellEditor(strEditor);
        tcm.getColumn(TYPE).setCellEditor(keysCombo);
    }

    public boolean tableHasFocus(JTable _table) {
        return table == _table;
    }

    private void setTableProperty(int col, int width,
                                  DefaultCellEditor editor) {

        TableColumn column = table.getColumnModel().getColumn(col);
        if (editor != null) {
            column.setCellEditor(editor);
        }
    }

    /**
     * <p>Adds the specified focus listener to the table.
     *
     * @param  listener to add to the table
     */
    public void addTableFocusListener(FocusListener listener) {
        table.addFocusListener(listener);
    }

    /**
     * <p>Sets the specified table model to the table.
     *
     * @param   model
     */
    public void setModel(AbstractTableModel model) {
        table.setModel(model);
    }

    public ColumnConstraint getConstraintAt(int row) {
        return model.getConstraintAt(row);
    }

    class ColumnConstraintModel extends AbstractTableModel {

        private final String[] header = Bundles.get(TableConstraintsPanel.class,
                new String[]
                        {"X",
                                "Type",
                                "Name",
                                "TableColumn",
                                "ReferenceTable",
                                "ReferenceColumn",
                                "UpdateRule",
                                "DeleteRule"
                        });

        private Vector keys;

        public ColumnConstraintModel(Vector v) {
            keys = v;
        }

        public void setNewData(Vector v) {
            keys = v;
        }

        public int getColumnCount() {
            return header.length;
        }

        public int getRowCount() {
            return keys.size();
        }

        /**
         * Inserts a constraint to the end of this model.
         *
         * @param isNew - whether the constraint will be marked as new
         */
        public void insertRowAfter(boolean isNew) {
            keys.add(new ColumnConstraint(isNew));
            int newIndex = keys.size() - 1; // end of vector
            fireTableRowsInserted(newIndex, newIndex);
        }

        public Object getValueAt(int row, int col) {
            ColumnConstraint cc = (ColumnConstraint) keys.elementAt(row);

            // check the column type
            boolean canHaveReference = (cc.getType() == FOREIGN_KEY);

            switch (col) {
                case X:
                    return cc;
                case TYPE:
                    return cc.getTypeName();
                case NAME:
                    if (cc.isGeneratedName())
                        return "";
                    return cc.getName();
                case TABLE_COLUMN:
                    return cc.getColumn();
                case REFERENCE_TABLE:
                    if (!canHaveReference) {
                        return null;
                    }
                    return cc.getRefTable();
                case REFERENCE_COLUMN:
                    if (!canHaveReference) {
                        return null;
                    }
                    return cc.getRefColumn();
                case UPDATE_RULE:
                    if (!canHaveReference) {
                        return null;
                    }
                    return cc.getUpdateRule();
                case DELETE_RULE:
                    if (!canHaveReference) {
                        return null;
                    }
                    return cc.getDeleteRule();
                default:
                    return null;
            }
        }

        public void setValueAt(Object value, int row, int col) {

            if (row < 0 || row > (keys.size() - 1)) {
                return;
            }

            ColumnConstraint cc = (ColumnConstraint) keys.elementAt(row);

            switch (col) {
                case X:
                    return;
                case TYPE:
                    String colType = (String) value;
                    if (colType == ColumnConstraint.PRIMARY) {
                        cc.setType(PRIMARY_KEY);
                    } else if (colType == ColumnConstraint.FOREIGN) {
                        cc.setType(FOREIGN_KEY);
                    } else if (colType == ColumnConstraint.UNIQUE) {
                        cc.setType(UNIQUE_KEY);
                    }

                    if (colType != null) {
                        updateCellEditor(col, row, colType);
                        columnValuesChanged(col, row, null);
                    }

                    cc.setColumn(Constants.EMPTY);
                    cc.setRefSchema(Constants.EMPTY);
                    cc.setRefTable(Constants.EMPTY);
                    cc.setRefColumn(Constants.EMPTY);
                    break;
                case NAME:
                    cc.setName((String) value);
                    columnValuesChanged(col, row, cc.getName());
                    break;
                case TABLE_COLUMN:
                    cc.setColumn(value != null ? value.toString() : "");
                    columnValuesChanged(col, row, null);
                    break;
                case REFERENCE_TABLE:
                    String tbl = (String) value;
                    cc.setRefColumn(Constants.EMPTY);
                    cc.setRefTable(tbl);
                    if (tbl != null) {
                        updateCellEditor(col, row, tbl);
                        columnValuesChanged(col, row, null);
                    }
                    break;
                case REFERENCE_COLUMN:
                    cc.setRefColumn((String) value);
                    columnValuesChanged(col, row, null);
                    break;
                case UPDATE_RULE:
                    cc.setUpdateRule((String) value);
                    columnValuesChanged(col, row, null);
                    break;
                case DELETE_RULE:
                    cc.setDeleteRule((String) value);
                    columnValuesChanged(col, row, null);
                    break;
            }

            fireTableRowsUpdated(row, row);
        }

        public ColumnConstraint getConstraintAt(int row) {
            return (ColumnConstraint) keys.elementAt(row);
        }

        public boolean isCellEditable(int row, int col) {
            ColumnConstraint cc = (ColumnConstraint) keys.elementAt(row);

            // check if its a new table create
            if (getMode() == CREATE_TABLE_MODE) {
                switch (col) {
                    case X:
                        return false;
                    case TYPE:
                    case NAME:
                    case TABLE_COLUMN:
                        return true;
                    case REFERENCE_TABLE:
                    case REFERENCE_COLUMN:
                    case UPDATE_RULE:
                    case DELETE_RULE:
                        return (cc.getType() != UNIQUE_KEY &&
                                cc.getType() != PRIMARY_KEY);
                }
            } else {
                if (col == NAME) {
                    return true;
                }

                if (cc.isNewConstraint()) {

                    return col <= TABLE_COLUMN || (cc.getType() != UNIQUE_KEY &&
                            cc.getType() != PRIMARY_KEY);

                }

            }
            return false;
        }

        public void deleteRow(int row) {
            keys.remove(row);
        }

        public String getColumnName(int col) {
            return header[col];
        }

        public void deleteConstraint(String refColumn) {
            int v_size = keys.size();
            if (v_size == 0) {
                insertRowAfter(getMode() == EDIT_TABLE_MODE);
                return;
            }

            for (int i = 0; i < v_size; i++) {
                ColumnConstraint cc = (ColumnConstraint) keys.elementAt(i);

                if (cc.getColumn() != null && cc.getColumn().equalsIgnoreCase(refColumn)) {
                    deleteRow(i);
                    fireTableRowsDeleted(i, i);
                    break;
                }

            }

        }

        public void deleteConstraint(int index) {
            deleteRow(index);
            fireTableRowsDeleted(index, index);
        }

        public Vector getKeys() {
            return keys;
        }




    } // class ColumnConstraintModel


}



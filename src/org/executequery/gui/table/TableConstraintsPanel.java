/*
 * TableConstraintsPanel.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

import java.awt.BorderLayout;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.executequery.Constants;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.browser.ColumnConstraint;
import org.executequery.gui.browser.ColumnData;
import org.underworldlabs.swing.table.ComboBoxCellEditor;
import org.underworldlabs.swing.table.StringCellEditor;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public abstract class TableConstraintsPanel extends JPanel
                                            implements CreateTableSQLSyntax {
    
    /** The table containing the constraint data */
    protected JTable table;
    
    /** The table's model */
    protected ColumnConstraintModel model;
    
    /** The constraint name cell editor */
    protected StringCellEditor conNameEditor;
    
    /** The string cell editor */
    protected DefaultCellEditor strEditor;
    
    /** The keys combo box cell editor */
    protected ComboBoxCellEditor keysCombo;
    
    public TableConstraintsPanel() {
        super(new BorderLayout());
        
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
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
        
        add(new JScrollPane(table), BorderLayout.CENTER);
        
        if (getMode() == CREATE_TABLE_MODE) {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }

        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        
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
            cca[i].setValues((ColumnConstraint)keys.elementAt(i));
        }
        
        return cca;
    }
    
    public void fireEditingStopped() {
        table.editingStopped(null);
        if (table.isEditing()) {
            table.removeEditor();
        }
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
        }
        else {
            model.setNewData(keys);
            setModel(model);
        }
        
        if (keysEmpty || fillCombos) {
            try {
                table.getColumnModel().getColumn(2).setCellEditor(keysCombo);
                table.getColumnModel().getColumn(3).setCellEditor(
                            new ComboBoxCellEditor(getTableColumnData()));
            } catch (ArrayIndexOutOfBoundsException e) { // TODO: what is this - test
                e.printStackTrace();
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
        }
        else {
            table.setEditingRow(row);
        }
        
        table.setEditingColumn(1);
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
        tcm.getColumn(0).setPreferredWidth(25);
        //tcm.getColumn(0).setMinWidth(25);
        tcm.getColumn(0).setMaxWidth(25);
        tcm.getColumn(1).setPreferredWidth(125);
        tcm.getColumn(2).setPreferredWidth(75);
        tcm.getColumn(3).setPreferredWidth(110);
        tcm.getColumn(4).setPreferredWidth(120);
        tcm.getColumn(5).setPreferredWidth(120);
        tcm.getColumn(6).setPreferredWidth(120);
        
        tcm.getColumn(0).setCellRenderer(new ConstraintCellRenderer());
        tcm.getColumn(1).setCellEditor(strEditor);
        tcm.getColumn(2).setCellEditor(keysCombo);
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
    
    /** <p>Adds the specified focus listener to the table.
     *
     *  @param the listener to add to the table
     */
    public void addTableFocusListener(FocusListener listener) {
        table.addFocusListener(listener);
    }
    
    /** <p>Sets the specified table model to the table.
     *
     *  @param the table model
     */
    public void setModel(AbstractTableModel model) {
        table.setModel(model);
    }
    
    public ColumnConstraint getConstraintAt(int row) {
        return model.getConstraintAt(row);
    }
    
    class ColumnConstraintModel extends AbstractTableModel {
        
        private String[] header = {"",
                                   "Name", 
                                   "Type", 
                                   "Table Column", 
                                   "Reference Schema",
                                   "Reference Table", 
                                   "Reference Column"};

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
            ColumnConstraint cc = (ColumnConstraint)keys.elementAt(row);
            
            // check the column type
            boolean canHaveReference = (cc.getType() == ColumnConstraint.FOREIGN_KEY);

            switch(col) {
                case 0:
                    return cc;
                case 1:
                    return cc.getName();
                case 2:
                    return cc.getTypeName();
                case 3:
                    return cc.getColumn();
                case 4:
                    if (!canHaveReference) {
                        return null;
                    }
                    return cc.getRefSchema();
                case 5:
                    if (!canHaveReference) {
                        return null;
                    }
                    return cc.getRefTable();
                case 6:
                    if (!canHaveReference) {
                        return null;
                    }
                    return cc.getRefColumn();
                default:
                    return null;
            }
        }
        
        public void setValueAt(Object value, int row, int col) {

            if (row < 0 || row > (keys.size() - 1)) {
                return;
            }

            ColumnConstraint cc = (ColumnConstraint)keys.elementAt(row);            
            
            switch (col) {
                case 0:
                    return;
                case 1:
                    cc.setName((String)value);
                    columnValuesChanged(col, row, cc.getName());
                    break;
                case 2:
                    String colType = (String)value;
                    if (colType == ColumnConstraint.PRIMARY) {
                        cc.setType(ColumnConstraint.PRIMARY_KEY);
                    } else if (colType == ColumnConstraint.FOREIGN) {
                        cc.setType(ColumnConstraint.FOREIGN_KEY);
                    } else if (colType == ColumnConstraint.UNIQUE) {
                        cc.setType(ColumnConstraint.UNIQUE_KEY);
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
                case 3:
                    cc.setColumn(value != null ? value.toString() : "");
                    columnValuesChanged(col, row, null);
                    break;
                case 4:
                    String schema = (String)value;
                    cc.setRefSchema(schema);
                    cc.setRefTable(Constants.EMPTY);
                    cc.setRefColumn(Constants.EMPTY);
                    
                    if (schema != null) {
                        updateCellEditor(col, row, schema);
                        columnValuesChanged(col, row, null);
                    }
                    
                    break;
                case 5:
                    String tbl = (String)value;
                    cc.setRefColumn(Constants.EMPTY);
                    cc.setRefTable(tbl);
                    if (tbl != null) {
                        updateCellEditor(col, row, tbl);
                        columnValuesChanged(col, row, null);
                    }
                    
                    break;
                case 6:
                    cc.setRefColumn((String)value);
                    columnValuesChanged(col, row, null);
                    
                    break;
            }
            
            fireTableRowsUpdated(row, row);
        }
        
        public ColumnConstraint getConstraintAt(int row) {
            return (ColumnConstraint)keys.elementAt(row);
        }
        
        public boolean isCellEditable(int row, int col) {
            ColumnConstraint cc = (ColumnConstraint)keys.elementAt(row);
            
            // check if its a new table create
            if (getMode() == CREATE_TABLE_MODE) {
                switch (col) {
                    case 0:
                        return false;
                    case 1:
                    case 2:
                    case 3:
                        return true;
                    case 4:
                    case 5:
                    case 6:
                        if ((cc.getType() == ColumnConstraint.UNIQUE_KEY ||
                                cc.getType() == ColumnConstraint.PRIMARY_KEY)) {
                            return false;
                        }
                        return true;
                }
            }
            else {
                if (col == 1) {
                    return true;
                }

                if (cc.isNewConstraint()) {

                    if (col > 3 && (cc.getType() == ColumnConstraint.UNIQUE_KEY ||
                            cc.getType() == ColumnConstraint.PRIMARY_KEY)) {
                        return false;
                    } 
                    else {
                        return true;
                    }

                }
                
            }
            return false;
            /*
            if (cc.isNewConstraint()) {
                
                if ((cc.getType() == cc.UNIQUE_KEY ||
                        cc.getType() == cc.PRIMARY_KEY) && col > 3) {
                    return false;
                } 
                else {
                    return true;
                }

            }
            else if (col == 1) {
                return true;
            }            
            else {
                return false;
            }
            */
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
                ColumnConstraint cc = (ColumnConstraint)keys.elementAt(i);
                
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


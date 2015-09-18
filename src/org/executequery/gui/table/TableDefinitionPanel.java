/*
 * TableDefinitionPanel.java
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


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.executequery.GUIUtilities;
import org.executequery.components.table.BrowsingCellEditor;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.browser.ColumnData;
import org.underworldlabs.swing.print.AbstractPrintableTableModel;
import org.underworldlabs.swing.table.NumberCellEditor;
import org.underworldlabs.swing.table.StringCellEditor;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public abstract class TableDefinitionPanel extends JPanel
                                           implements TableModelListener {
    
    /** The table containing all column descriptions */
    protected DatabaseTable table;
    
    /** The table's _model */
    protected CreateTableModel _model;
    
    /** The cell editor for the column names */
    protected static StringCellEditor colNameEditor;
    
    /** The cell editor for the column size */
    protected NumberCellEditor sizeEditor;
    
    /** The cell editor for the column scale */
    protected NumberCellEditor scaleEditor;
    
    /** The cell editor for the datatype column */
    //protected ComboBoxCellEditor comboCell;
    
    /** The cell editor for the datatype column */
    protected DataTypeSelectionTableCell dataTypeCell;

    /** The <code>Vector</code> of <code>ColumnData</code> objects */
    protected Vector<ColumnData> tableVector;
    
    /** The literal 'PK' */
    private static final String PRIMARY = "PK";
    
    /** The literal 'FK' */
    private static final String FOREIGN = "FK";
    
    /** An empty String literal */
    private static final String EMPTY = " ";
    
    protected boolean editing;
    
    /** the available data types */
    private String[] dataTypes;
    
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
            //e.printStackTrace();
        }
        
    }
    
    private void jbInit() throws Exception {
        // set the table model to use
        _model = new CreateTableModel();
        table = new DatabaseTable(_model);
        
        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(25);
        //tcm.getColumn(0).setMinWidth(25);
        tcm.getColumn(0).setMaxWidth(25);
        tcm.getColumn(1).setPreferredWidth(200);
        tcm.getColumn(2).setPreferredWidth(130);
        tcm.getColumn(3).setPreferredWidth(50);
        tcm.getColumn(4).setPreferredWidth(50);
        //tcm.getColumn(5).setPreferredWidth(60);
        tcm.getColumn(5).setPreferredWidth(70);
        tcm.getColumn(5).setMaxWidth(70);

        tcm.getColumn(0).setCellRenderer(new KeyCellRenderer());
        
        // add the editors if editing
        if (editing) {
            colNameEditor = new StringCellEditor();
            DefaultCellEditor colStrEditor = new DefaultCellEditor(colNameEditor) {
                public Object getCellEditorValue() {
                    return colNameEditor.getValue(); }
            };
            tcm.getColumn(1).setCellEditor(colStrEditor);
            //tcm.getColumn(5).setCellEditor(colStrEditor);

            scaleEditor = new NumberCellEditor();
            DefaultCellEditor scEditor = new DefaultCellEditor(scaleEditor) {
                public Object getCellEditorValue() {
                    return scaleEditor.getStringValue(); }
            };
            
            sizeEditor = new NumberCellEditor();
            DefaultCellEditor szEditor = new DefaultCellEditor(sizeEditor) {
                public Object getCellEditorValue() {
                    return sizeEditor.getStringValue(); }
            };
            tcm.getColumn(3).setCellEditor(szEditor);
            tcm.getColumn(4).setCellEditor(scEditor);
            
            dataTypeCell = new DataTypeSelectionTableCell();
            tcm.getColumn(2).setCellRenderer(dataTypeCell);
            tcm.getColumn(2).setCellEditor(dataTypeCell);

            // create the key listener to notify changes
            KeyAdapter valueKeyListener = new KeyAdapter() {
                public void keyReleased(KeyEvent e) {
                    String value = null;
                    Object object = e.getSource();
                    if (object == colNameEditor) {
                        value = colNameEditor.getValue();
                    }
                    else if (object == sizeEditor) {    
                        value = sizeEditor.getEditorValue();
                    }
                    else if (object == scaleEditor) {
                        value = scaleEditor.getEditorValue();
                    }
                    else if (object == dataTypeCell.getComponent()) {
                        value = dataTypeCell.getEditorValue();
                    }
                    tableChanged(table.getEditingColumn(),
                                 table.getEditingRow(), 
                                 value); 
                }
            };
            colNameEditor.addKeyListener(valueKeyListener);
            dataTypeCell.addKeyListener(valueKeyListener);
            sizeEditor.addKeyListener(valueKeyListener);
            scaleEditor.addKeyListener(valueKeyListener);
            
            _model.addTableModelListener(this);
        }
        
        //setPreferredSize(new Dimension(460, 150));
        //setPreferredSize(new Dimension(500, 150));
        add(new JScrollPane(table), new GridBagConstraints(
                                                1, 1, 1, 1, 1.0, 1.0, 
                                                GridBagConstraints.SOUTHEAST,
                                                GridBagConstraints.BOTH, 
                                                new Insets(2, 2, 2, 2), 0, 0));
        
    }

    public void setColumnDataArray(ColumnData[] cda) {
        _model.setColumnDataArray(cda);
    }

    public void setColumnDataArray(ColumnData[] cda, String[] dataTypes) {
        _model.setColumnDataArray(cda);
        this.dataTypes = dataTypes;
        /*
        if (dataTypes != null) {
            comboCell.setSelectionValues(dataTypes);
        }
         */
    }

    /**
     * Sets the available data types to the values specified.
     *
     * @param the data type values
     */
    public void setDataTypes(String[] dataTypes) {
        this.dataTypes = dataTypes;
        /*
        if (dataTypes != null) {
            comboCell.setSelectionValues(dataTypes);
        }
         */
    }

    public void tableChanged(TableModelEvent e) {
        //Log.debug("tableChanged");
        int row = table.getEditingRow();
        if (row == -1) {
            return;
        }        
        tableChanged(table.getEditingColumn(), row, null);
    }
    
    /**
     * Fires that a table cell value has changed as specified.
     *
     * @param col - the column index
     * @param row - the row index
     * @param value - the current value
     */
    public abstract void tableChanged(int col, int row, String value);
    
    /** <p>Adds all the column definition lines to
     *  the SQL text buffer for display.
     *
     *  @param the current row being edited
     */
    public abstract void addColumnLines(int row);
    
    /** <p>Moves the selected column up one row within
     *  the table moving the column above the selection
     *  below the selection.
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
    
    /** <p>Propogates the call to <code>removeEditor()</code>
     *  on the table displaying the data. */
    public void removeEditor() {
        table.removeEditor();
    }
    
    /** <p>Propogates the call to <code>isEditing()</code>
     *  on the table displaying the data.
     *
     *  @return if a data edit is in progress on the table
     */
    public boolean isEditing() {
        return table.isEditing();
    }
    
    /** <p>Returns the table displaying the
     *  column data.
     *
     *  @return the table displaying the data
     */
    public JTable getTable() {
        return table;
    }
    
    /** <p>Moves the selected column down one row within
     *  the table moving the column below the selection
     *  above the selection. */
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
    
    /** <p>Inserts a new column before the selected
     *  column moving the selected column down one row. */
    public void insertBefore() {
        fireEditingStopped();
        
        if (table.isEditing()) {
            table.removeEditor();
        }
        
        int selection = table.getSelectedRow();
        if (selection == -1) {
            return;
        } else {
            tableVector.insertElementAt(new ColumnData(), selection);
        }
        
        _model.fireTableRowsInserted(
                selection == 0 ? 0 : selection -1,
                selection == 0 ? 1 : selection);

        table.setRowSelectionInterval(selection, selection);
        table.setColumnSelectionInterval(1, 1);
        
        table.setEditingRow(selection);
        table.setEditingColumn(1);
        
    }
    
    public void fireEditingStopped() {        
        table.editingStopped(null);
        if (table.isEditing()) {
            table.removeEditor();
        }        
    }
    
    /** <p>Deletes the selected row from the table.
     *  This will also modify the SQL generated text. */
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
            tableVector.addElement(new ColumnData(true));
            _model.fireTableRowsInserted(0, 0);
        }
        
        addColumnLines(-1);
    }
    
    public void addMouseListener() {
        table.addMouseListener();
    }
    
    /** <p>Inserts a new column after the selected
     *  column moving the selected column up one row. */
    public void insertAfter() {
        fireEditingStopped();
        int selection = table.getSelectedRow();
        int newRow = selection + 1;

        if (selection == -1) {
            return;
        }
        else if (selection == tableVector.size()) {
            tableVector.add(new ColumnData());
        }
        else {
            tableVector.add(newRow, new ColumnData());
        }
        
        _model.fireTableRowsInserted(selection, newRow);        
        table.setRowSelectionInterval(newRow, newRow);
        table.setColumnSelectionInterval(1, 1);
        
        table.setEditingRow(newRow);
        table.setEditingColumn(1);
        ((DefaultCellEditor)table.getCellEditor(newRow, 1)).
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
        }
        
        public void addMouseListener() {
            addMouseListener(this);
        }
        
        public void mouseClicked(MouseEvent e) {
            int mouseX = e.getX();
            int mouseY = e.getY();
            
            int col = columnAtPoint(new Point(mouseX, mouseY));
            if (col != 0) {
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
        
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
        public void mousePressed(MouseEvent e) {}
        public void mouseReleased(MouseEvent e) {}
        
    } // class DatabaseTable
    
    
    /**
     * The table's model.
     */
    protected class CreateTableModel extends AbstractPrintableTableModel {
        
        protected String[] header = {EMPTY, "Name", "Datatype",
                                     "Size", "Scale", "Required"};

        public CreateTableModel() {
            tableVector = new Vector<ColumnData>();
            tableVector.addElement(new ColumnData());
        }
        
        public CreateTableModel(Vector<ColumnData> data) {
            tableVector = data;
        }
        
        public void setColumnDataArray(ColumnData[] cda) {
            
            if (cda != null) {
                if (tableVector == null) {
                    tableVector = new Vector<ColumnData>(cda.length);
                }
                else {
                    tableVector.clear();
                }

                for (int i = 0; i < cda.length; i++) {
                    tableVector.add(cda[i]);
                }
            }
            else {
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
            if (col > 0) {
                Object value = getValueAt(row, col);
                if (value != null) {
                    return value.toString();
                }
                return EMPTY;
            }
            else {
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
            }
        }

        public Object getValueAt(int row, int col) {
            
            if (row >= tableVector.size()) {
                return null;
            }
            
            ColumnData cd = tableVector.elementAt(row);
            
            switch(col) {
                
                case 0:
                    return cd;
                case 1:
                    return cd.getColumnName();
                    
                case 2:
                    return cd.getColumnType();
                    
                case 3:
                    return Integer.valueOf(cd.getColumnSize());
                    
                case 4:
                    return Integer.valueOf(cd.getColumnScale());

//                case 5:
//                    return cd.getDefaultValue();

                case 5:
                    return Boolean.valueOf(cd.isRequired());
                    
                default:
                    return null;
                    
            }
        }
        
        public void setValueAt(Object value, int row, int col) {
            ColumnData cd = tableVector.elementAt(row);

            //Log.debug("setValueAt [row: "+row+" col: "+col+" value: "+value+"]");
            
            switch (col) {
                case 0:
                    if (cd.isPrimaryKey()) {
                        cd.setKeyType(PRIMARY);
                    } else if (cd.isForeignKey()) {
                        cd.setKeyType(FOREIGN);
                    } else {
                        cd.setKeyType(null);
                    }
                    break;
                case 1:
                    cd.setColumnName((String)value);
                    break;
                case 2:
                    cd.setColumnType((String)value);
                    break;
                case 3:
                    cd.setColumnSize(Integer.parseInt((String)value));
                    break;
                case 4:
                    cd.setColumnScale(Integer.parseInt((String)value));
                    break;
//                case 5:
//                    cd.setDefaultValue((String)value);
//                    break;
                case 5:
                    cd.setColumnRequired(((Boolean)value).booleanValue() ? 0 : 1);
                    break;
            }
            
            fireTableRowsUpdated(row, row);
        }
        
        public boolean isCellEditable(int row, int col) {
            return editing && col != 0;
        }
        
        public String getColumnName(int col) {
            return header[col];
        }
        
        public Class getColumnClass(int col) {
            if (col == 5) {
                return Boolean.class;
            }
            else if (col == 3 || col == 4) {
                return Integer.class;
            }
            else {
                return String.class;
            }
        }
        
        public void addNewRow() {
            ColumnData cd = tableVector.lastElement();
            if (!cd.isNewColumn()) {
                tableVector.addElement(new ColumnData(true));
            }
            
        }
        
    } // class CreateTableModel
    
    private class DataTypeSelectionTableCell extends BrowsingCellEditor 
                                             implements DataTypeSelectionListener {

        private int lastEditingRow;
        private int lastEditingColumn;
        
        public DataTypeSelectionTableCell() {}

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
         * @param the data type value string
         */
        public void dataTypeSelected(String dataType) {
            //setDelegateValue(dataType);
            if (lastEditingRow != -1 && lastEditingColumn != -1) {
                _model.setValueAt(dataType, lastEditingRow, lastEditingColumn);
                tableChanged(lastEditingColumn, lastEditingRow, dataType);
            }
            fireEditingStopped();

            // reset row and column values
            lastEditingRow = -1;
            lastEditingColumn = -1;
        }

    } // class DataTypeSelectionTableCell
    
}














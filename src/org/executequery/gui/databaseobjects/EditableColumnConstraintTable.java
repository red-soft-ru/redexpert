/*
 * EditableColumnConstraintTable.java
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

package org.executequery.gui.databaseobjects;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.DatabaseSchema;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.impl.ColumnConstraint;
import org.executequery.databaseobjects.impl.TableColumnConstraint;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.table.ComboBoxCellEditor;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class EditableColumnConstraintTable extends DefaultColumnConstraintTable
        implements KeyListener {//,
    //CellEditorListener {

    /**
     * the db table displayed
     */
    private DatabaseTable databaseTable;

    /**
     * The constraint type combo box cell editor
     */
    private ComboBoxCellEditor keyTypeEditor;

    /**
     * The column name combo box cell editor
     */
    private ComboBoxCellEditor columnNameEditor;

    /**
     * The referenced schema name combo box cell editor
     */
    private ComboBoxCellEditor refSchemaEditor;

    /**
     * The referenced table name combo box cell editor
     */
    private ComboBoxCellEditor refTableEditor;

    /**
     * The referenced column name combo box cell editor
     */
    private ComboBoxCellEditor refColumnEditor;

    private static final int KEY_TYPE_COL_INDEX = 2;
    private static final int COLUMN_NAME_COL_INDEX = 3;
    private static final int REF_SCHEMA_COL_INDEX = 4;
    private static final int REF_TABLE_COL_INDEX = 5;
    private static final int REF_COLUMN_COL_INDEX = 6;
    private static final int CHECK_COL_INDEX = 7;
    private static final int UPDATE_RULE_COL_INDEX = 8;
    private static final int DELETE_RULE_COL_INDEX = 9;

    /**
     * Creates a new instance of EditableColumnConstraintTable
     */
    public EditableColumnConstraintTable() {
        super();
        getColumnConstraintTableModel().setEditable(false);
        setCellEditorListeners();
        setCellEditors();
        addMouseListener(new MouseHandler());
    }

    /**
     * Resets and clears the currently displayed table.
     */
    public void resetConstraintsTable() {
        try {
            setDatabaseTable(null);
        } catch (DataSourceException e) {
        }
    }

    /**
     * Appends the specified constraint to end of the list.
     *
     * @param constraint the new constraint
     */
    public void addConstraint(TableColumnConstraint constraint) {
        getColumnConstraintTableModel().addConstraint(constraint);
    }

    /**
     * Deletes or marks to delete the currently selected
     * database table constraint (JTable row).
     */
    public void deleteSelectedConstraint() {
        int selectedRow = getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        ColumnConstraintTableModel _model = getColumnConstraintTableModel();
        TableColumnConstraint constraint =
                (TableColumnConstraint) _model.getValueAt(selectedRow, 0);

        // if its a new column - just remove it
        if (constraint.isNewConstraint()) {
            _model.deleteConstraintAt(selectedRow);
        } else { // otherwise mark to drop
            constraint.setMarkedDeleted(true);
            _model.fireTableRowsUpdated(selectedRow, selectedRow);
        }

    }

    /**
     * Sets the currently displayed table to that specified.
     *
     * @param databaseTable the db table shown
     */
    public void setDatabaseTable(DatabaseTable databaseTable) {

        this.databaseTable = databaseTable;

        if (databaseTable != null) {

            setConstraintData(databaseTable.getConstraints());

        } else {

            setConstraintData(null);
        }

        if (!editorsLoaded) {

            setCellEditors();
        }

        if (databaseTable != null) {

            List<DatabaseColumn> tableColumns = databaseTable.getColumns();
            if (tableColumns != null && !tableColumns.isEmpty()) {

                columnNameEditor.setSelectionValues(tableColumns.toArray());
            }

            Object[] refSchemas = databaseTable.getHost().getSchemas().toArray();
            refSchemaEditor.setSelectionValues(refSchemas);
        }

    }

    /**
     * flag indicating that the editors have been initialised
     */
    private boolean editorsLoaded;

    /**
     * Sets the editor renderers for respective cells.
     */
    protected void setCellEditors() {
        TableColumnModel tcm = getColumnModel();
        if (keyTypeEditor == null) {
            String[] keys = {
                    ColumnConstraint.PRIMARY,
                    ColumnConstraint.FOREIGN,
                    ColumnConstraint.UNIQUE,
                    ColumnConstraint.CHECK
            };
            keyTypeEditor = new ComboBoxCellEditor(keys);
        }
        tcm.getColumn(KEY_TYPE_COL_INDEX).setCellEditor(keyTypeEditor);

        if (columnNameEditor == null) {
            columnNameEditor = new ComboBoxCellEditor();
        }
        tcm.getColumn(COLUMN_NAME_COL_INDEX).setCellEditor(columnNameEditor);

        if (refSchemaEditor == null) {
            refSchemaEditor = new ComboBoxCellEditor();
        }
        tcm.getColumn(REF_SCHEMA_COL_INDEX).setCellEditor(refSchemaEditor);

        if (refTableEditor == null) {
            refTableEditor = new ComboBoxCellEditor();
        }
        tcm.getColumn(REF_TABLE_COL_INDEX).setCellEditor(refTableEditor);

        if (refColumnEditor == null) {
            refColumnEditor = new ComboBoxCellEditor();
        }
        tcm.getColumn(REF_COLUMN_COL_INDEX).setCellEditor(refColumnEditor);

        editorsLoaded = true;
    }

    /**
     * Informs the listeners the editor has ended editing.
     * Any required lists in subsequent columns are loaded
     * dependant on prior selections.
     */
    public void editingStopped(ChangeEvent e) {

        // retrieve the column edited
        int column = getEditingColumn();

        int editingRow2 = getEditingRow();
        int editingColumn2 = getEditingColumn();

        // call to super implementation
        super.editingStopped(e);

        // load any required lists
        try {

            if (column == REF_SCHEMA_COL_INDEX) { // schema selection
                // load the schema tables
                Object value = refSchemaEditor.getCellEditorValue();

                if (value instanceof DatabaseSchema) {

                    DatabaseSchema schema = (DatabaseSchema) value;
                    Object[] tables = schema.getTables().toArray();
                    refTableEditor.setSelectionValues(tables);

                } else {

                    // clear any existing selections
                    refTableEditor.setSelectionValues(null);
                }

            } else if (column == REF_TABLE_COL_INDEX) { // table selection
                // load the table columns
                Object value = refTableEditor.getCellEditorValue();
                if (value instanceof DatabaseObject) {

                    DatabaseObject table = (DatabaseObject) value;
                    Object[] columns = table.getColumns().toArray();
                    refColumnEditor.setSelectionValues(columns);

                } else {

                    // clear any existing selections
                    refColumnEditor.setSelectionValues(null);
                }

            }

        } catch (DataSourceException exc) {

            GUIUtilities.displayExceptionErrorDialog(
                    "Error retrieving selected schema tables:\n" +
                            exc.getExtendedMessage(), exc);
        }

        Object value = getValueAt(editingRow2, editingColumn2);


    }

    /**
     * Sets listeners on respective column cell editors.
     */
    protected void setCellEditorListeners() {
        // interested in string editors
        Class<?>[] clazzez = new Class[]{String.class};
        for (int i = 0; i < clazzez.length; i++) {
            TableCellEditor cellEditor = getDefaultEditor(clazzez[i]);

            if (cellEditor != null && cellEditor instanceof DefaultCellEditor) {

                DefaultCellEditor _cellEditor = (DefaultCellEditor) cellEditor;
                if (_cellEditor.getComponent() instanceof JTextField) {
                    _cellEditor.
                            getComponent().addKeyListener(this);
                }

            }
        }
    }

    public void keyReleased(KeyEvent e) {

        Object source = e.getSource();

        if (source instanceof JTextField) {

            int row = getEditingRow();
            int col = getEditingColumn();

            // listeners only exist on string class columns
            String value = ((JTextField) source).getText();
            tableChanged(row, col, value);
        }

    }

    /**
     * Indicates that the table data for the specified row and
     * column has changed to the specified value and notifies the
     * table model.
     *
     * @param row   the table row edited
     * @param col   the table column edited
     * @param value the value to be set
     */
    public void tableChanged(int row, int col, Object value) {
        getModel().setValueAt(value, row, col);
    }

    /**
     * Invoked when a key has been typed.
     * This event occurs when a key press is followed by a key release.
     */
    public void keyTyped(KeyEvent e) {
    }

    /**
     * Invoked when a key has been pressed.
     */
    public void keyPressed(KeyEvent e) {
    }

    /**
     * Mouse adapter class to handle click events within
     * the first column to enable reverting a mark deleted flag.
     */
    private class MouseHandler extends MouseAdapter {

        public void mouseClicked(MouseEvent e) {
            int mouseX = e.getX();
            int mouseY = e.getY();

            int col = columnAtPoint(new Point(mouseX, mouseY));
            if (col != 0) {
                return;
            }

            int row = rowAtPoint(new Point(mouseX, mouseY));
            ColumnConstraintTableModel _model = getColumnConstraintTableModel();
            TableColumnConstraint constraint =
                    (TableColumnConstraint) _model.getValueAt(row, 0);

            if (constraint.isMarkedDeleted()) {
                constraint.setMarkedDeleted(false);
                _model.fireTableRowsUpdated(row, row);
            }

        }

    } // class MouseHandler

}







/*
 * EditableDatabaseTable.java
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

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.impl.DatabaseTableColumn;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class EditableDatabaseTable extends DefaultDatabaseObjectTable
                                   implements KeyListener {

    /** Creates a new instance of EditableDatabaseTable */
    public EditableDatabaseTable() {
        super();
        getDatabaseTableModel().setEditable(true);
        setCellEditorListeners();
        addMouseListener(new MouseHandler());
    }

    /** Sets listeners on respective column cell editors. */
    protected void setCellEditorListeners() {

        // interested in string and int editors
        Class<?>[] clazzez = new Class[]{String.class, Integer.class};
        for (int i = 0; i < clazzez.length; i++) {

            TableCellEditor cellEditor = getDefaultEditor(clazzez[i]);

            if (cellEditor instanceof DefaultCellEditor) {

                DefaultCellEditor defaultCellEditor = (DefaultCellEditor) cellEditor;
                if (defaultCellEditor.getComponent() instanceof JTextField) {

                    ((JTextField)defaultCellEditor.getComponent()).addKeyListener(this);
                }

            }
        }
    }

    /**
     * Sets the currently displayed table to that specified.
     *
     * @param databaseTable the db table shown
     */
    public void setDatabaseTable(DatabaseTable databaseTable) {
        if (databaseTable != null) {
            setColumnData(databaseTable.getColumns());
        } else {
            setColumnData(null);
        }
    }

    /**
     * Appends the specified column to end of the list.
     *
     * @param column the new column
     */
    public void addColumn(DatabaseTableColumn column) {
        // stop any editing
        editingStopped(null);
        if (isEditing()) {

            removeEditor();
        }

        int toIndex = -1;
        int selectedRow = getSelectedRow();
        if (selectedRow != -1) {

            toIndex = selectedRow + 1;
        }

        DatabaseObjectTableModel _model = getDatabaseTableModel();
        _model.addNewDatabaseColumn(column, toIndex);

        toIndex = _model.indexOf(column);
        setRowSelectionInterval(toIndex, toIndex);
        setColumnSelectionInterval(1, 1);

        setEditingRow(toIndex);
        setEditingColumn(1);
    }

    /**
     * Deletes or marks to delete the currently selected
     * database table column (JTable row).
     */
    public void deleteSelectedColumn() {

        int selectedRow = getSelectedRow();
        if (selectedRow == -1) {

            return;
        }

        DatabaseObjectTableModel _model = getDatabaseTableModel();
        DatabaseTableColumn column = (DatabaseTableColumn)_model.getValueAt(selectedRow, 0);

        // if its a new column - just remove it
        if (column.isNewColumn()) {

            _model.deleteDatabaseColumnAt(selectedRow);

        } else { // otherwise mark to drop

            column.makeCopy();
            column.setMarkedDeleted(true);
            _model.fireTableRowsUpdated(selectedRow, selectedRow);
        }

    }

    /**
     * Resets and clears the currently displayed table.
     */
    public void resetDatabaseTable() {
        try {
            setDatabaseTable(null);
        } catch (DataSourceException e) {}
    }

    /**
     * Invoked when a key has been released.
     */
    public void keyReleased(KeyEvent e) {

        Object source = e.getSource();
        if (source instanceof JTextField) {

            int row = getEditingRow();
            int col = getEditingColumn();

            // listeners only exist on string
            // and integer class columns

            String value = ((JTextField) source).getText();
            if (getModel().getColumnClass(col) == Integer.class) {

                if (MiscUtils.isValidNumber(value)) {

                    tableChanged(row, col, new Integer(value));
                }

            } else { // if not an int must be a string

                tableChanged(row, col, value);
            }

        }

    }

    /**
     * Indicates that the table data for the specified row and
     * column has changed to the specified value and notifies the
     * table model.
     *
     * @param row the table row edited
     * @param col the table column edited
     * @param value the value to be set
     */
    public void tableChanged(int row, int col, Object value) {
        getModel().setValueAt(value, row, col);
    }

    /**
     * Invoked when a key has been typed.
     * This event occurs when a key press is followed by a key release.
     */
    public void keyTyped(KeyEvent e) {}

    /**
     * Invoked when a key has been pressed.
     */
    public void keyPressed(KeyEvent e) {}

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
            DatabaseObjectTableModel _model = getDatabaseTableModel();
            DatabaseTableColumn column = (DatabaseTableColumn)_model.getValueAt(row, 0);
            if (column.isMarkedDeleted()) {

                column.setMarkedDeleted(false);
                _model.fireTableRowsUpdated(row, row);
            }

        }

    } // class MouseHandler

}







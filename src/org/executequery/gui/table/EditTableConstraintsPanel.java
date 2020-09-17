/*
 * EditTableConstraintsPanel.java
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
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.gui.browser.ColumnConstraint;
import org.executequery.gui.browser.ColumnData;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.table.ComboBoxCellEditor;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import static org.executequery.databaseobjects.NamedObject.*;

/**
 * @author Takis Diakoumis
 */
public class EditTableConstraintsPanel extends TableConstraintsPanel {

    /**
     * The table creator object - parent to this
     */
    private TableConstraintFunction creator;

    /**
     * The buffer off all SQL generated
     */
    private StringBuffer sqlBuffer;

    /**
     * The hosted schemas for the connection
     */
    private Vector hostedSchemas;

    /**
     * The original constraints
     */
    private ColumnConstraint[] fKeys_orig;

    /**
     * Holds temporary SQL text during modifications
     */
    private Hashtable tempSqlText;

    public EditTableConstraintsPanel(TableConstraintFunction creator) {
        super();
        this.creator = creator;
        table.addMouseListener(new MouseHandler());

        sqlBuffer = new StringBuffer(100);
        tempSqlText = new Hashtable();
    }

    public ColumnData[] getTableColumnData() {
        return creator.getTableColumnData();
    }

    public int getMode() {
        return EDIT_TABLE_MODE;
    }

    public void updateCellEditor(int col, int row, String value) {
        Vector fKeys = model.getKeys();
        ColumnConstraint cc = (ColumnConstraint) fKeys.get(row);

        switch (col) {

            case 0:
            case 1:
                return;

            case 2:
                setCellEditor(3, new ComboBoxCellEditor(getTableColumnData()));
/*                
                if (value == ColumnConstraint.PRIMARY) {
                    setCellEditor(3, new ComboBoxCellEditor(getTableColumnData()));
                }
                else */
                if (ColumnConstraint.FOREIGN.equals(value)) {

                    if (hostedSchemas == null) {
                        hostedSchemas = creator.getHostedSchemasVector();
                    }
                    int schemaSize = hostedSchemas.size();

                    // this means that cretor objects with null
                    // schema lists must generate the table values
                    // from elsewhere
                    if (schemaSize == 0) {
                        setCellEditor(5, new ComboBoxCellEditor(creator.getSchemaTables(value)));
                        return;
                    }

                    String[] schemas = new String[schemaSize];
                    for (int i = 0, k = hostedSchemas.size(); i < k; i++) {
                        schemas[i] = (String) hostedSchemas.elementAt(i);
                    }

                    //setCellEditor(4, new ComboBoxCellEditor(schemas));

                }

                break;

            case 3:
                return;

            case 4:

                if (value == null || value.length() == 0) {
                    return;
                }
                setCellEditor(5, new ComboBoxCellEditor(creator.getSchemaTables(value)));
                break;

            case 5:
                setCellEditor(6, new ComboBoxCellEditor(
                        creator.getColumnNamesVector(value, cc.getRefSchema())));
                break;

            case 6:
                return;

        }

    }

    public void columnValuesChanged(int col, int row, String value) {
        ColumnConstraint cc_orig = null;

        Vector fKeys = model.getKeys();
        if (row < 0 || row > (fKeys.size() - 1)) {
            return;
        }

        ColumnConstraint cc = (ColumnConstraint) fKeys.elementAt(row);
        sqlBuffer.setLength(0);

        if (row > fKeys_orig.length - 1 || cc.isNewConstraint()) {

            if (value == null) {
                value = cc.getName();
            } else if (value.length() == 0) {
                tempSqlText.remove(ADD_CONSTRAINT + row);
                generateSQL();
                creator.setSQLText();
                return;
            }

            sqlBuffer.append(ALTER_TABLE).
                    append(creator.getTableName()).
                    append(ADD_CONSTRAINT).
                    append(value).append(SPACE).
                    append(cc.getTypeName() == null ? EMPTY : cc.getTypeName());

            switch (cc.getType()) {

                case FOREIGN_KEY:
                    sqlBuffer.append(KEY).
                            append(B_OPEN).
                            append(cc.getColumn()).
                            append(B_CLOSE).
                            append(SPACE).
                            append(REFERENCES);

                    if (cc.hasSchema()) {
                        sqlBuffer.append(cc.getRefSchema()).append(DOT);
                    }

//                    sqlBuffer.append(creator.getTableName()).append(B_OPEN).
                    sqlBuffer.append(cc.getRefTable()).
                            append(B_OPEN).
                            append(cc.getRefColumn()).
                            append(B_CLOSE).
                            append(SEMI_COLON).
                            append(NEW_LINE);
                    break;

                case UNIQUE_KEY:
                    sqlBuffer.append(B_OPEN).
                            append(cc.getColumn()).
                            append(B_CLOSE).
                            append(SEMI_COLON).
                            append(NEW_LINE);
                    break;

                case PRIMARY_KEY:
                    sqlBuffer.append(KEY).
                            append(B_OPEN).
                            append(cc.getColumn()).
                            append(B_CLOSE).
                            append(SEMI_COLON).
                            append(NEW_LINE);
                    break;

                default:
                    sqlBuffer.append(B_OPEN).
                            append(cc.getColumn()).
                            append(B_CLOSE).
                            append(SEMI_COLON).
                            append(NEW_LINE);
                    break;

            }

            tempSqlText.put(ADD_CONSTRAINT + row, sqlBuffer.toString());

        } else {
            cc_orig = fKeys_orig[row];

            if (cc_orig.getName().equals(value)) {
                tempSqlText.remove(RENAME_CONSTRAINT + row);
            } else {
                sqlBuffer.append(ALTER_TABLE).
                        append(creator.getTableName()).
                        append(RENAME_CONSTRAINT).
                        append(cc_orig.getName()).
                        append(TO).
                        append(value).
                        append(SEMI_COLON).
                        append(NEW_LINE);
                tempSqlText.put(RENAME_CONSTRAINT + row, sqlBuffer.toString());
            }

        }

        generateSQL();
        creator.setSQLText();

    }

    /**
     * Returns the table displaying the constraint data.
     *
     * @return the table displaying the data
     */
    public JTable getTable() {
        return table;
    }

    public void addTableFocusListener(FocusListener listener) {
        table.addFocusListener(listener);
    }

    /**
     * Clears all the SQL text.
     */
    public void reset() {
        sqlBuffer.setLength(0);
        tempSqlText.clear();
    }

    /**
     * Removes the SQL with the specified reference key
     * from the temp buffer.
     *
     * @param key - the reference key to the SQL text
     */
    protected void removeFromBuffer(String key) {
        tempSqlText.remove(key);
    }

    /**
     * Adds the specified value SQL text with the specified key
     * to the temp SQL buffer.
     *
     * @param key   - the key to reference the SQL text value
     * @param value - the SQL text
     */
    protected void addToBuffer(String key, String value) {
        tempSqlText.put(key, value);
    }

    /**
     * Generates the SQL statements based on the stored value changes.
     */
    protected void generateSQL() {
        sqlBuffer.setLength(0);
        for (Enumeration i = tempSqlText.elements(); i.hasMoreElements(); ) {
            sqlBuffer.append((String) i.nextElement());
        }
    }

    /**
     * Stores the original unmodified key values.
     */
    public void setOriginalData() {
        GUIUtils.startWorker(new Runnable() {
            //SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Vector fKeys = model.getKeys();
                int v_size = fKeys.size();

                fKeys_orig = new ColumnConstraint[v_size];
                for (int i = 0; i < v_size; i++) {
                    fKeys_orig[i] = new ColumnConstraint();
                    fKeys_orig[i].setValues((ColumnConstraint) fKeys.elementAt(i));
                }
            }
        });
    }

    /**
     * Marks the currently selected column (table row)
     * to be deleted/dropped from this table.
     */
    public void markDeleteRow() {
        int row = getSelectedRow();
        if (row == -1) {
            return;
        }

        table.editingStopped(null);
        if (table.isEditing()) {
            table.removeEditor();
        }

        ColumnConstraint cc = getConstraintAt(row);
        // if its already a new row - just remove it
        if (cc.isNewConstraint()) {
            //int newEditingRow = (row == tableVector.size() - 1) ? row - 1 : row;
            //setEditingRow(newEditingRow);
            model.deleteConstraint(row);
            tempSqlText.remove(ADD_CONSTRAINT + row);

            // add the dummy row
            if (model.getRowCount() == 0) {
                model.insertRowAfter(true);
            }

            // regenerate the SQL
            generateSQL();
            return;
        }

        // create the drop statement
        sqlBuffer.setLength(0);
        sqlBuffer.append(ALTER_TABLE);
        
        /*
        String schema = cc.getRefSchema();
        if (!MiscUtilities.isNull(schema)) {
            sqlBuffer.append(schema.toUpperCase()).append(DOT);
        }
        */

        sqlBuffer.append(cc.getTable());
        sqlBuffer.append(DROP_CONSTRAINT);
        sqlBuffer.append(cc.getName());
        sqlBuffer.append(NEW_LINE);
        tempSqlText.put(DROP_CONSTRAINT + row, sqlBuffer.toString());

        // mark the column to be dumped
        cc.setMarkedDeleted(true);

        // regenerate the SQL
        generateSQL();

        // fire the event
        model.fireTableRowsUpdated(row, row);
    }

    public void insertRowAfter() {
        model.insertRowAfter(true);
    }

    public void deleteSelectedRow(StatementExecutor qs) {
        table.editingStopped(null);
        if (table.isEditing()) {
            table.removeEditor();
        }

        int row = table.getSelectedRow();
        ColumnConstraint cc = getConstraintAt(row);
        if (!cc.isNewConstraint()) {
            deleteSelectedRow(row, qs);
            return;
        }

        int newEditingRow = row == model.getRowCount() - 1 ? row - 1 : row;
        table.setEditingRow(newEditingRow);

        model.deleteRow(row);
        tempSqlText.remove(ADD_CONSTRAINT + row);
        model.fireTableRowsDeleted(row, row);

        generateSQL();
        creator.setSQLText();

        if (model.getKeys().size() == 0) {
            model.insertRowAfter(true);
        }

    }

    public void deleteSelectedRow(int row, StatementExecutor qs) {
        if (row == -1) {
            return;
        }

        ColumnConstraint cc = getConstraintAt(row);
        int yesNo = GUIUtilities.displayConfirmDialog(
                "Are you sure you want to remove\nthe constraint " +
                        cc.getName() + "?");

        if (yesNo == JOptionPane.NO_OPTION)
            return;

        else {

            try {

                SqlStatementResult result = qs.updateRecords(
                        ALTER_TABLE + creator.getTableName() +
                                DROP_CONSTRAINT + cc.getName());
                if (result.getUpdateCount() >= 0) {
                    int newEditingRow = (row == model.getRowCount() - 1 ?
                            row - 1 : row);
                    table.setEditingRow(newEditingRow);

                    model.deleteRow(row);
                    model.fireTableRowsDeleted(row, row);
                } else {
                    SQLException e = result.getSqlException();
                    if (e != null) {
                        StringBuffer sb = new StringBuffer();
                        sb.append("An error occurred applying the specified changes.").
                                append("\n\nThe system returned:\n").
                                append(MiscUtils.formatSQLError(e));
                        GUIUtilities.displayExceptionErrorDialog(sb.toString(), e);
                    } else {
                        GUIUtilities.displayErrorMessage(result.getErrorMessage());
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                StringBuffer sb = new StringBuffer();
                sb.append("An error occurred applying the specified changes.").
                        append("\n\nThe system returned:\n").
                        append(e.getMessage());
                GUIUtilities.displayExceptionErrorDialog(sb.toString(), e);
            }

        }
    }

    public String getSQLText() {
        return sqlBuffer.toString();
    }

    public void columnValuesChanged() {
        columnValuesChanged(-1, -1, null);
    }

    private class MouseHandler extends MouseAdapter {
        public MouseHandler() {
        }

        public void mouseClicked(MouseEvent e) {
            int mouseX = e.getX();
            int mouseY = e.getY();

            int col = table.columnAtPoint(new Point(mouseX, mouseY));
            // if we haven't clicked on column 0 - bail
            if (col != 0) {
                return;
            }

            int row = table.rowAtPoint(new Point(mouseX, mouseY));
            Object object = model.getValueAt(row, col);
            if (object == null) {
                return;
            }

            ColumnConstraint cc = (ColumnConstraint) object;
            // if this constraint is marked to be dropped, unmark it
            if (cc.isMarkedDeleted()) {
                cc.setMarkedDeleted(false);
                tempSqlText.remove(DROP_CONSTRAINT + row);
                model.fireTableRowsUpdated(row, row);
                generateSQL();
                creator.setSQLText();
            }

        }
    }

}












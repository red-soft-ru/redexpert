/*
 * EditTablePanel.java
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
import org.executequery.gui.browser.ColumnData;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the
 *           release of version 3.0.0beta1 has meant a
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 * @author Takis Diakoumis
 */
public class EditTablePanel extends TableDefinitionPanel {

    /**
     * The table creator object - parent to this
     */
    private TableModifier creator;

    /**
     * The buffer off all SQL generated
     */
    private StringBuffer sqlText;

    /**
     * Holds temporary SQL text during modifications
     */
    private Hashtable tempSqlText;

    /**
     * The column descriptions for the current selection before modifications
     */
    private ColumnData[] originalData;

    /**
     * The string literal 'ALTER TABLE '
     */
    private static String DROP_COLUMN_1 = "ALTER TABLE ";

    /**
     * The string literal ' DROP COLUMN '
     */
    private static String DROP_COLUMN_2 = " DROP COLUMN ";

    //------------------------------------------------
    // The following are reuseable String constants
    // used in the SQL text pane when making table
    // or other schema modifications.
    //------------------------------------------------

    /**
     * The String literal 'ALTER TABLE '
     */
    private static final String ALTER_TABLE = "ALTER TABLE ";
    /**
     * The String literal ' MODIFY ('
     */
    private static final String MODIFY = " MODIFY (";
    /**
     * The String literal ' NOT NULL'
     */
    private static final String NOT_NULL = " NOT NULL";
    /**
     * The String literal ' NULL'
     */
    private static final String NULL = " NULL";
    /**
     * The String literal ' ADD '
     */
    private static final String ADD = " ADD ";

    /**
     * The String literal ' DROP '
     */
    private static final String DROP = " DROP ";

    /**
     * The String literal ')'
     */
    private static final String CLOSE_B = ")";
    /**
     * The character literal '('
     */
    private static final char OPEN_B = '(';
    /**
     * The String literal ' RENAME COLUMN '
     */
    private static final String RENAME_COLUMN = " RENAME COLUMN ";
    /**
     * The String literal ' TO '
     */
    private static final String TO = " TO ";
    /**
     * The character literal ','
     */
    private static final char COMMA = ',';
    /**
     * The character literal '.'
     */
    private static final char DOT = '.';
    /**
     * The character literal ' '
     */
    private static final char SPACE = ' ';
    /**
     * An empty String literal
     */
    private static final String EMPTY = "";
    /**
     * A semi-colon followed by a carriage return ';\n'
     */
    private static final String NEW_LINE = ";\n";

    public EditTablePanel(TableModifier creator) {
        super();
        this.creator = creator;

        sqlText = new StringBuffer(100);
        tempSqlText = new Hashtable();
        getTable().addMouseListener(new MouseHandler());
    }

    public String getSQLText() {
        generateSQL();
        return sqlText.toString();
    }

    public void setOriginalData(ColumnData[] cda) {
        originalData = cda;
    }
    
    /*
    public void setColumnDataArray(ColumnData[] cda) {
        _model.setColumnDataArray(cda);
    }
    */

    /**
     * <p>Sets the SQL changes <code>Hashtable</code> to
     * an existing one as the value passed.
     *
     * @param the SQL changes within a <code>Hashtable</code>
     */
    public void setSQLChangesHash(Hashtable tempSqlText) {
        if (tempSqlText != null) {
            this.tempSqlText = tempSqlText;
        }
    }

    /**
     * <p>Returns the SQL changes within a <code>Hashtable</code>.
     *
     * @return the SQL changes
     */
    public Hashtable getSQLChangesHash() {
        return tempSqlText;
    }

    public void setOriginalData() {

        GUIUtils.startWorker(new Runnable() {
            //SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                int v_size = tableVector.size();
                originalData = new ColumnData[v_size];

                for (int i = 0; i < v_size; i++) {
                    originalData[i] = new ColumnData(dc);
                    originalData[i].setValues(tableVector.elementAt(i));
                }

            }
        });

    }

    /**
     * <p>Drops the currently selected column (table row)
     * from the database table and applies this change
     * to the database.
     *
     * @param the <code>QuerySender</code> to perform the
     *            operation on the database.
     */
    public void deleteRow(StatementExecutor qs) {
        int row = getSelectedRow();
        if (row == -1) {
            return;
        }

        tableEditingStopped(null);
        if (isEditing()) {
            removeEditor();
        }

        ColumnData cd = tableVector.elementAt(row);

        int newEditingRow = row == tableVector.size() - 1 ? row - 1 : row;
        setEditingRow(newEditingRow);

        if (cd.isNewColumn()) {
            tableVector.removeElementAt(row);
            _model.fireTableRowsDeleted(row, row);
            tempSqlText.remove(ADD + row);
            addColumnLines(-1);
            return;
        }

        int yesNo = GUIUtilities.displayConfirmDialog(
                "Are you sure you want to remove\n" +
                        "the column " + cd.getColumnName() + "?");

        if (yesNo == JOptionPane.NO_OPTION || yesNo == JOptionPane.CANCEL_OPTION) {
            return;
        } else if (yesNo == JOptionPane.YES_OPTION) {

            try {
                SqlStatementResult result = qs.updateRecords(
                        DROP_COLUMN_1 + creator.getTableName() +
                                DROP_COLUMN_2 + cd.getColumnName());

                if (result.getUpdateCount() >= 0) {
                    tableVector.removeElementAt(row);
                    _model.fireTableRowsDeleted(row, row);
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

    /**
     * <p>Inserts a new column after the selected
     * column moving the selected column up one row.
     * <p>
     * Overrides to mark the column as new.
     */
    public void insertAfter() {
        fireEditingStopped();

        int selection = getSelectedRow();
        if (selection == -1) {
            return;
        }

        int newRow = selection + 1;
        ColumnData cd = new ColumnData(true, dc);
        cd.setColumnRequired(ColumnData.VALUE_NOT_REQUIRED);

        if (selection == tableVector.size()) {
            tableVector.add(cd);
        } else {
            tableVector.add(newRow, cd);
        }

        _model.fireTableRowsInserted(selection, newRow);
        setRowSelectionInterval(newRow);
        setColumnSelectionInterval(1);

        setEditingRow(newRow);
        setEditingColumn(1);
        ((DefaultCellEditor) getCellEditor(newRow, 1)).getComponent().requestFocus();
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

        tableEditingStopped(null);
        if (isEditing()) {
            removeEditor();
        }

        ColumnData cd = tableVector.elementAt(row);

        // if its already a new row - just remove it
        if (cd.isNewColumn()) {
            int newEditingRow = (row == tableVector.size() - 1) ? row - 1 : row;
            setEditingRow(newEditingRow);
            tableVector.removeElementAt(row);
            _model.fireTableRowsDeleted(row, row);
            tempSqlText.remove(ADD + row);
            addColumnLines(-1);
            return;
        }

        // create the drop statement
        sqlText.setLength(0);
        sqlText.append(DROP_COLUMN_1);

        String schema = cd.getSchema();
        if (!MiscUtils.isNull(schema)) {
            sqlText.append(schema.toUpperCase()).append(DOT);
        }

        sqlText.append(cd.getTableName());
        sqlText.append(DROP_COLUMN_2);
        sqlText.append(cd.getColumnName());
        sqlText.append(NEW_LINE);
        tempSqlText.put(DROP + row, sqlText.toString());

        // mark the column to be dumped
        cd.setMarkedDeleted(true);

        // regenerate the SQL
        generateSQL();

        // fire the event
        _model.fireTableRowsUpdated(row, row);
    }

    public void reset() {
        sqlText.setLength(0);
        tempSqlText.clear();
    }

    public void tableChanged(int col, int row, String value) {
        sqlText.setLength(0);
        ColumnData cd = (ColumnData) tableVector.get(row);

        switch (col) {

            case 1:
                if (value == null) {
                    return;
                }

                if (cd.isNewColumn()) {

                    sqlText.append(ALTER_TABLE);

                    String schema = cd.getSchema();
                    if (!MiscUtils.isNull(schema)) {
                        sqlText.append(schema.toUpperCase()).append(DOT);
                    }

                    sqlText.append(creator.getTableName()).
                            append(ADD).
                            append(value).
                            append(SPACE);

                    value = cd.getColumnType();
                    if (value != null && value.length() > 0) {
                        sqlText.append(value).append(OPEN_B).append(cd.getColumnSize());

                        int scale = cd.getColumnScale();
                        if (scale != 0) {
                            sqlText.append(COMMA).append(scale);
                        }

                        sqlText.append(CLOSE_B).append(cd.isRequired() ? NOT_NULL : NULL);
                    }

                    sqlText.append(NEW_LINE);
                    tempSqlText.put(ADD + row, sqlText.toString());

                } else {

                    if (originalData[row].getColumnName().equals(value)) {
                        tempSqlText.remove(RENAME_COLUMN + row);
                    } else {
                        sqlText.append(ALTER_TABLE);

                        String schema = cd.getSchema();
                        if (!MiscUtils.isNull(schema)) {
                            sqlText.append(schema.toUpperCase()).append(DOT);
                        }

                        sqlText.append(originalData[row].getTableName()).
                                append(RENAME_COLUMN).
                                append(originalData[row].getColumnName()).
                                append(TO).append(value).
                                append(NEW_LINE);

                        tempSqlText.put(RENAME_COLUMN + row, sqlText.toString());

                    }

                }
                break;

            case 2:
            case 3:
            case 4:
            case 5:
                tableChanged(cd, row, col);
                break;

        }

        generateSQL();
        creator.setSQLText();
    }

    private void tableChanged(ColumnData cd, int row, int col) {
        sqlText.setLength(0);

        int size = cd.getColumnSize();
        int scale = cd.getColumnScale();
        String type = cd.getColumnType();
        
        /*
        if (col == 2) {
            type = (String)comboCell.getCellEditorValue();
        } else if (col == 3) {
            size = sizeEditor.getValue();
        } else if (col == 4) {
            scale = scaleEditor.getValue();
        }
        */

        if (cd.isNewColumn()) {
            sqlText.append(ALTER_TABLE);

            String schema = cd.getSchema();
            if (!MiscUtils.isNull(schema)) {
                sqlText.append(schema.toUpperCase()).append(DOT);
            }

            sqlText.append(creator.getTableName()).
                    append(ADD).
                    append(cd.getColumnName()).
                    append(SPACE).
                    append(type).
                    append(OPEN_B).
                    append(size);

            if (scale != 0) {
                sqlText.append(COMMA).append(scale);
            }

            sqlText.append(CLOSE_B).
                    append(cd.isRequired() ? NOT_NULL : NULL).
                    append(NEW_LINE);

            tempSqlText.put(ADD + row, sqlText.toString());
        } else if (originalData[row].getColumnSize() == size &&
                originalData[row].getColumnType().equals(type) &&
                originalData[row].getColumnScale() == scale &&
                originalData[row].isRequired() == cd.isRequired()) {

            tempSqlText.remove(MODIFY + row);

        } else {
            sqlText.append(ALTER_TABLE);

            String schema = cd.getSchema();
            if (!MiscUtils.isNull(schema)) {
                sqlText.append(schema.toUpperCase()).append(DOT);
            }

            sqlText.append(originalData[row].getTableName()).
                    append(MODIFY).
                    append(cd.getColumnName()).
                    append(SPACE).
                    append(type).
                    append(OPEN_B).
                    append(size);

            if (scale != 0) {
                sqlText.append(COMMA).append(scale);
            }

            sqlText.append(CLOSE_B).
                    append(cd.isRequired() ? NOT_NULL : NULL).
                    append(CLOSE_B).
                    append(NEW_LINE);

            tempSqlText.put(MODIFY + row, sqlText.toString());
        }

        generateSQL();
        creator.setSQLText();
    }

    private void generateSQL() {
        sqlText.setLength(0);

        for (Enumeration i = tempSqlText.elements(); i.hasMoreElements(); ) {
            sqlText.append((String) i.nextElement());
        }

    }

    /**
     * <p>Adds all the column definition lines to
     * the SQL text buffer for display.
     *
     * @param the current row being edited
     */
    public void addColumnLines(int row) {
        generateSQL();
        creator.setSQLText();
    }


    private class MouseHandler extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            int mouseX = e.getX();
            int mouseY = e.getY();

            int col = getTable().columnAtPoint(new Point(mouseX, mouseY));
            if (col != 0) {
                return;
            }

            ColumnData[] cda = getTableColumnData();
            int row = getTable().rowAtPoint(new Point(mouseX, mouseY));
            for (int i = 0; i < cda.length; i++) {
                if (i == row) {
                    if (cda[i].isMarkedDeleted()) {
                        cda[i].setMarkedDeleted(false);
                        tempSqlText.remove(DROP + row);
                        _model.fireTableRowsUpdated(row, row);
                        generateSQL();
                        creator.setSQLText();
                    }
                    break;
                }
            }

        }
    }

}
















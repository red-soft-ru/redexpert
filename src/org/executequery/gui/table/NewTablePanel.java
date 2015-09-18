/*
 * NewTablePanel.java
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

import org.executequery.gui.browser.ColumnData;
import org.underworldlabs.util.MiscUtils;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class NewTablePanel extends TableDefinitionPanel
                           implements CreateTableSQLSyntax {
    
    /** The table creator object - parent to this */
    private TableModifier creator;
    
    /** The buffer for the current line */
    private StringBuffer line;
    
    /** The buffer off all SQL generated */
    private StringBuffer sqlText;
    
    public NewTablePanel(TableModifier creator) {
        super();
        this.creator = creator;
        
        line = new StringBuffer(50);
        sqlText = new StringBuffer(100);
    }
    
    /**
     * Returns the SQL scriptlet text.
     *
     * @return the SQL text
     */
    public String getSQLText() {
        return sqlText.toString();
    }
    
    /**
     * Resets the SQL text.
     */
    public void resetSQLText() {
        addColumnLines(-1);
    }
    
    /**
     * Indicates that the table value for the specified row and 
     * column has changed to the value specified.
     *
     * @param col - the last updated col
     * @param row - the last updated row
     * @param value - the new value
     */
    public void tableChanged(int col, int row, String value) {

        //Log.debug("tableChanged [row: "+row+" col: "+col+" value: "+value + "]");
        
        if (value == null) {
            updateScript(row, col);
            return;
        }
        
        //if (row == -1 || (col == 1 && value == null)) {
        if (row == -1) {// || (col == 1 && value == null)) {
            return;
        }

        ColumnData cd = (ColumnData)tableVector.get(row);
        switch(col) {
            case 1:
                cd.setColumnName(value);
                break;
            case 2:
                cd.setColumnType(value);
                break;
            case 3:
                if (!MiscUtils.isNull(value)) {
                    int _value = Integer.parseInt(value);
                    cd.setColumnSize(_value);
                }
                break;
            case 4:
                if (!MiscUtils.isNull(value)) {
                    int _value = Integer.parseInt(value);
                    cd.setColumnScale(_value);
                }
                break;
        }
        updateScript(row, col);
    }
    
    /**
     * Updates the generated scriptlet using the specified
     * row and col as the last upfdaed/modified value.
     *
     * @param row - the last updated row
     * @param col - the last updated col
     */
    private void updateScript(int row, int col) {
        line.setLength(0);
        ColumnData cd = (ColumnData)tableVector.get(row);
        line.setLength(0);
        line.append(NEW_LINE_2).
             append(cd.getColumnName() == null ? EMPTY : cd.getColumnName()).
             append(SPACE);

        if (cd.getColumnType() != null) {
            line.append(cd.getFormattedDataType());
        }

        line.append(cd.isRequired() ? NOT_NULL : EMPTY);
        
        if (row < tableVector.size() - 1) {
            line.append(COMMA);
        }

        if (cd.isNewColumn()) {
            cd.setNewColumn(false);
        }
        
        addColumnLines(row);
    }
    
    /** 
     * Adds all the column definition lines to
     * the SQL text buffer for display.
     *
     * @param the current row being edited
     */
    public void addColumnLines(int row) {
        
        sqlText.setLength(0);

        for (int i = 0, k = tableVector.size(); i < k; i++) {
            ColumnData cd = (ColumnData)tableVector.elementAt(i);

            if (i == row) {
                sqlText.append(line);
            }
            
            else if (!cd.isNewColumn()) {
                
                sqlText.append(NEW_LINE_2).append(
                        cd.getColumnName() == null ? EMPTY : cd.getColumnName()).
                        append(SPACE);
                
                if (cd.getColumnType() != null) {
                    sqlText.append(cd.getColumnType().toUpperCase());
                    
                    if(!cd.getColumnType().equalsIgnoreCase(DATE)) {
                        sqlText.append(B_OPEN).append(cd.getColumnSize());
                        
                        if (cd.getColumnScale() != 0) {
                            sqlText.append(COMMA).append(cd.getColumnScale());
                        }
                        
                        sqlText.append(B_CLOSE);
                    }
                    
                }
                sqlText.append(cd.isRequired() ? NOT_NULL : EMPTY);
                
                if (i != k - 1) {
                    sqlText.append(COMMA);
                }
                
            }
            
        }
        
        creator.setSQLText(sqlText.toString(), TableModifier.COLUMN_VALUES);
        
    }
    
}












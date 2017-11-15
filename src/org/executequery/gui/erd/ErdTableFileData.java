/*
 * ErdTableFileData.java
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

package org.executequery.gui.erd;

import java.awt.Color;
import java.awt.Rectangle;

import java.io.Serializable;

import java.util.Hashtable;

import org.executequery.gui.browser.ColumnData;

/**
 *
 * @author   Takis Diakoumis
 */
@SuppressWarnings({"rawtypes"})
public class ErdTableFileData implements Serializable {
    
    /** The table name displayed */
    private String tableName;
    
    /** The table's columns */
    private ColumnData[] columns;
    
    /** The CREATE TABLE script for a new table */
    private String createTableScript;
    
    /** The ALTER TABLE script for a definition change */
    private String alterTableScript;
    
    /** The ALTER TABLE script for a constraint change */
    private String addConstraintScript;
    
    /** The ALTER TABLE script for a constraint drop */
    private String dropConstraintScript;
    
    /** <code>Hashtable</code> containing table modifications */
    private Hashtable alterTableHash;
    
    /** The table bounds */
    private Rectangle tableBounds;
    
    /** The tables background colour */
    private Color tableBackground;
    
    public ErdTableFileData() {}
    
    public Rectangle getTableBounds() {
        return tableBounds;
    }
    
    public void setTableBounds(Rectangle tableBounds) {
        this.tableBounds = tableBounds;
    }
    
    public void setAddConstraintScript(String addConstraintScript) {
        this.addConstraintScript = addConstraintScript;
    }
    
    public String getAddConstraintScript() {
        return addConstraintScript;
    }
    
    public void setDropConstraintScript(String dropConstraintScript) {
        this.dropConstraintScript = dropConstraintScript;
    }
    
    public String getDropConstraintScript() {
        return dropConstraintScript;
    }
    
    public Hashtable getAlterTableHash() {
        return alterTableHash;
    }
    
    public void setAlterTableHash(Hashtable alterTableHash) {
        this.alterTableHash = alterTableHash;
    }
    
    public String getAlterTableScript() {
        return alterTableScript;
    }
    
    public void setAlterTableScript(String alterTableScript) {
        this.alterTableScript = alterTableScript;
    }
    
    public String getCreateTableScript() {
        return createTableScript;
    }
    
    public void setCreateTableScript(String createTableScript) {
        this.createTableScript = createTableScript;
    }
    
    public ColumnData[] getColumnData() {
        return columns;
    }
    
    public void setColumnData(ColumnData[] columns) {
        this.columns = columns;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Color getTableBackground() {
        return tableBackground;
    }

    public void setTableBackground(Color tableBackground) {
        this.tableBackground = tableBackground;
    }
    
    
}



















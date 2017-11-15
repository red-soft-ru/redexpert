/*
 * ColumnIndex.java
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

package org.executequery.gui.browser;

/** 
 * This object maintains table index data
 * as retrieved from the <code>DatabaseMetaData</code>
 * method <code>getIndexInfo(...)</code> for a particular table 
 * as selected within the Database Browser.<br>
 *
 * @author   Takis Diakoumis
 */
public class ColumnIndex  {
    
    /** Whether the index is non-unique */
    private boolean non_unique;
    
    /** The index name */
    private String name;
    
    /** The indexed column */
    private String column;
    
    /** Whether this a new index value */
    private boolean markedNew;
    
    /** Whether this column is marked as to be deleted */
    private boolean markedDeleted;

    public ColumnIndex() {}
    
    public void setIndexedColumn(String column) {
        this.column = column;
    }
    
    public String getIndexedColumn() {
        return column;
    }
    
    public String getIndexName() {
        return name;
    }
    
    public void setIndexName(String name) {
        this.name = name;
    }
    
    public void setNonUnique(boolean non_unique) {
        this.non_unique = non_unique;
    }
    
    public boolean isNonUnique() {
        return non_unique;
    }
    
    public String toString() {
        return name;
    }

    public boolean isMarkedNew() {
        return markedNew;
    }

    public void setMarkedNew(boolean markedNew) {
        this.markedNew = markedNew;
    }

    public boolean isMarkedDeleted() {
        return markedDeleted;
    }

    public void setMarkedDeleted(boolean markedDeleted) {
        this.markedDeleted = markedDeleted;
    }
    
}







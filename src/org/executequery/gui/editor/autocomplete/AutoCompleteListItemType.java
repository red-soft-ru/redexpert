/*
 * AutoCompleteListItemType.java
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

package org.executequery.gui.editor.autocomplete;

public enum AutoCompleteListItemType {

    SQL92_KEYWORD, 
    USER_DEFINED_KEYWORD,
    DATABASE_DEFINED_KEYWORD, 
    DATABASE_TABLE, 
    DATABASE_TABLE_COLUMN,
    DATABASE_VIEW, 
    DATABASE_SEQUENCE,
    DATABASE_DATA_TYPE,
    DATABASE_FUNCTION,
    DATABASE_PROCEDURE,
    SYSTEM_FUNCTION,
    NOTHING_PROPOSED,
    GENERATING_LIST;

    public boolean isKeyword() {
        
        return this == SQL92_KEYWORD 
            || this == USER_DEFINED_KEYWORD
            || this == DATABASE_DEFINED_KEYWORD;
    }
    
    public boolean isTableColumn() {
        
        return this == DATABASE_TABLE_COLUMN;
    }

    public boolean isTableView() {
        
        return this == DATABASE_VIEW;
    }
    
    public boolean isTable() {

        return this == DATABASE_TABLE;
    }

    public boolean isProcedure() {
        
        return this == DATABASE_PROCEDURE;
    }
    
    public boolean isFunction() {
        
        return this == DATABASE_FUNCTION;
    }

    public boolean isSystemFunction() {
        
        return this == SYSTEM_FUNCTION;
    }
    
}






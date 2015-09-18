/*
 * CreateTableSQLSyntax.java
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

import org.executequery.gui.browser.ColumnConstraint;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public interface CreateTableSQLSyntax {
    
    int CREATE_TABLE_MODE = 0;
    int EDIT_TABLE_MODE = 1;
    
    //------------------------------------------------
    // The following are reuseable String constants
    // used in the SQL text pane when making table
    // or other schema modifications.
    //------------------------------------------------
    
    String[] KEY_NAMES = {ColumnConstraint.PRIMARY,
                          ColumnConstraint.FOREIGN,
                          ColumnConstraint.UNIQUE};
    
    /** The literal 'CREATE TABLE ' */
    String CREATE_TABLE = "CREATE TABLE ";
    
    /** The literal ' NOT NULL' */
    String NOT_NULL = " NOT NULL";
    
    /** The literal 'DATE' */
    String DATE = "DATE";
    
    /** The literal 'pk_' */
    String PK_PREFIX = "pk_";
    
    /** The literal 'NUMBER' */
    String NUMBER = "NUMBER";
    
    /** The literal 'CONSTRAINT' */
    String CONSTRAINT = "CONSTRAINT ";
    
    /** The String literal ' RENAME CONSTRAINT ' */
    String RENAME_CONSTRAINT = " RENAME CONSTRAINT ";
    
    /** The String literal ' ADD CONSTRAINT ' */
    String ADD_CONSTRAINT = " ADD CONSTRAINT ";
    
    /** The String literal ' TO ' */
    String TO = " TO ";
    
    /** New line with 7 space indent */
    String INDENT = "\n       ";
    
    /** The literal 'REFERENCES ' */
    String REFERENCES = " REFERENCES ";
    
    /** The literal ' PRIMARY' */
    String PRIMARY = " PRIMARY";
    
    /** The literal ' KEY ' */
    String KEY = " KEY ";
    
    /** The literal '\n' */
    char NEW_LINE = '\n';
    
    String NEW_LINE_2 = "\n    ";
    
    /** The literal ' ' */
    
    /** The String literal 'ALTER TABLE ' */
    String ALTER_TABLE = "ALTER TABLE ";
    
    /** The String literal ' ADD ' */
    String ADD = " ADD ";
    
    String SPACE = " ";
    
    /** An empty <code>String</code> */
    String EMPTY = "";
    
    /** The literal '(' */
    String B_OPEN = "(";
    
    /** The literal ',' */
    char COMMA = ',';
    
    /** The literal ')' */
    char B_CLOSE = ')';
    
    /** The literal '.' */
    char DOT = '.';
    
    /** The literal ';' */
    char SEMI_COLON = ';';

    /** The literal ' DROP CONSTRAINT ' */
    String DROP_CONSTRAINT = " DROP CONSTRAINT ";

}






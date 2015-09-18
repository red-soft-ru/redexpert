/*
 * StatementGenerator.java
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

package org.executequery.sql;

import java.sql.Connection;

import liquibase.database.Database;

import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.DatabaseView;
import org.executequery.databaseobjects.impl.DatabaseTableColumn;

public interface StatementGenerator {

    String END_DELIMITER = ";";
    
    String columnNameValueEscaped(DatabaseTableColumn tableColumn);
    
    String alterTable(String databaseName, DatabaseTable table);
    
    String createTable(String database, DatabaseTable table);
    
    String createTableWithConstraints(String databaseName, DatabaseTable table);

    String tableConstraintsAsAlter(String databaseName, DatabaseTable table);

    String dropTable(String databaseName, DatabaseTable table);
    
    String dropTableCascade(String databaseName, DatabaseTable table);

    String createUniqueKeyChange(String databaseName, DatabaseTable table);

    String createForeignKeyChange(String databaseName, DatabaseTable table);

    String createPrimaryKeyChange(String databaseName, DatabaseTable table);

    String columnDescription(DatabaseTableColumn column);

    String viewDefinition(String databaseName, DatabaseView view);

}





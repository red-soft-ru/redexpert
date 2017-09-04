/*
 * ShiftyHsqlDatabase.java
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

package org.executequery.sql.spi;

import liquibase.database.core.HsqlDatabase;

public class ShiftyHsqlDatabase extends HsqlDatabase {

    public String escapeColumnName(String schemaName, String tableName, String columnName) {
        if (columnName.contains("(")) { // from liquibase.database.core.HsqlDatabase
            return columnName;
        }
        return "\"" + columnName + "\"";
    }
    
}





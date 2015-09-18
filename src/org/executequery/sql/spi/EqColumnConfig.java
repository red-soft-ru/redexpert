/*
 * EqColumnConfig.java
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

package org.executequery.sql.spi;

import liquibase.change.AddColumnConfig;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;

import org.executequery.sql.liquibase.MaxDBDatabase;

class EqColumnConfig extends AddColumnConfig {

    private final Database database;
    private final String typeName;

    public EqColumnConfig(String typeName, Database database) {
        this.typeName = typeName;
        this.database = database;
    }

    @Override
    public String getType() {

        LiquibaseDataType dataType = DataTypeFactory.getInstance().fromDescription(typeName, database);
        if (dataType.getMaxParameters(database) < 1) {

            return dataType.getName();
        }
        
        return super.getType();
    }
    
    public String getDefaultValue() {

        String columnDefaultValue = super.getDefaultValue();
        if (columnDefaultValue != null) {

            if ("null".equalsIgnoreCase(columnDefaultValue)) {

                return "NULL";
            }

            if (shouldQuoteDefaultValue(database)) {

                return database.escapeStringForDatabase(columnDefaultValue);

                /* 2.0.5
                if (!database.shouldQuoteValue(columnDefaultValue)) {

                    return columnDefaultValue;

                } else {

                    return "'" + columnDefaultValue.replaceAll("'", "''") + "'";
                }
                */

            }

            return columnDefaultValue;
        }

        return super.getDefaultValue();
    }

    private boolean shouldQuoteDefaultValue(Database database) {

        return database instanceof MySQLDatabase
            || database instanceof MaxDBDatabase;
    }

}


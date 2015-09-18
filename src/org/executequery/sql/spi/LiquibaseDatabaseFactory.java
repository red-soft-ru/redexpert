/*
 * LiquibaseDatabaseFactory.java
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

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.database.core.UnsupportedDatabase;

import org.apache.commons.lang.StringUtils;
import org.executequery.sql.liquibase.MaxDBDatabase;

public class LiquibaseDatabaseFactory {

    public Database createDatabase(String databaseName) {

        String name = safelyToUpper(databaseName);
        if (name.contains("POSTGRESQL")) {

            return postgresDatabase();

        } else if (name.contains("MYSQL")) {

            return mysqlDatabase();

        } else if (name.contains("ORACLE")) {

            return oracleDatabase();

        } else if (name.contains("HSQL")) {

            return hsqlDatabase();

        } else if (name.contains("H2")) {

            return h2Database();

        } else if (name.contains("DERBY")) {

            return derbyDatabase();

        } else if (name.contains("MAXDB") || name.contains("SAP")) {

            return maxDbDatabase();

        } else if (name.contains("ADAPTIVE SERVER") || name.contains("SYBASE")) {

            return sybaseDatabase();

        } else if (name.contains("FIREBIRD")) {

            return firebirdDatabase();

        } else if (name.contains("MICROSOFT SQL SERVER")) {

            return msSqlDatabase();

        } else if (name.contains("DB2")) {

            return db2Database();
        }

        return unsupportedDatabase();
    }

    private Database unsupportedDatabase() {

        return new UnsupportedDatabase();
    }

    private Database db2Database() {

        return new DB2Database();
    }

    private Database msSqlDatabase() {

        return new MSSQLDatabase();
    }

    private Database firebirdDatabase() {

        return new FirebirdDatabase();
    }

    private Database sybaseDatabase() {

        return new SybaseDatabase();
    }

    private Database maxDbDatabase() {

        return new MaxDBDatabase();
    }

    private Database derbyDatabase() {

        return new DerbyDatabase();
    }

    private Database h2Database() {

        return new H2Database();
    }

    private Database hsqlDatabase() {

        return new ShiftyHsqlDatabase();
    }

    private Database oracleDatabase() {

        return new OracleDatabase();
    }

    private Database mysqlDatabase() {

        return new MySQLDatabase();
    }

    private Database postgresDatabase() {

        return new PostgresDatabase();
    }

    private String safelyToUpper(String databaseName) {

        return StringUtils.isNotBlank(databaseName) ? databaseName.toUpperCase() : "";
    }

}


/*
 * BrowserQueryExecuter.java
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

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.databaseobjects.NamedObject;

import java.sql.SQLException;

/**
 * Performs SQL execution tasks from browser components.
 *
 * @author Takis Diakoumis
 */
public class BrowserQueryExecuter {

    public static final int UPDATE_CANCELLED = 99;

    /**
     * query sender object
     */
    private StatementExecutor querySender;

    /**
     * Creates a new instance of BorwserQueryExecuter
     */
    public BrowserQueryExecuter() {
    }

    /**
     * Drops the specified database object.
     *
     * @param dc     - the database connection
     * @param object - the object to be dropped
     */
    public int dropObject(DatabaseConnection dc, BaseDatabaseObject object)
            throws SQLException {

        String queryStart = null;
        int type = object.getType();
        switch (type) {

            case NamedObject.CATALOG:
            case NamedObject.SCHEMA:
            case NamedObject.OTHER:
                GUIUtilities.displayErrorMessage(
                        "Dropping objects of this type is not currently supported");
                return UPDATE_CANCELLED;

            case NamedObject.FUNCTION:
                queryStart = "DROP FUNCTION ";
                break;

            case NamedObject.INDEX:
                queryStart = "DROP INDEX ";
                break;

            case NamedObject.PROCEDURE:
                queryStart = "DROP PROCEDURE ";
                break;

            case NamedObject.SEQUENCE:
                queryStart = "DROP SEQUENCE ";
                break;

            case NamedObject.SYNONYM:
                queryStart = "DROP SYNONYM ";
                break;

            case NamedObject.SYSTEM_TABLE:
            case NamedObject.TABLE:
                queryStart = "DROP TABLE ";
                break;

            case NamedObject.TRIGGER:
                queryStart = "DROP TRIGGER ";
                break;

            case NamedObject.VIEW:
                queryStart = "DROP VIEW ";
                break;

        }

        if (querySender == null) {
            querySender = new DefaultStatementExecutor(dc);
        } else {
            querySender.setDatabaseConnection(dc);
        }

        String name = object.getName();
        return querySender.updateRecords(queryStart + name).getUpdateCount();
    }

}





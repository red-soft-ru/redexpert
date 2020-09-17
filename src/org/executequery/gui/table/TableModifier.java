/*
 * TableModifier.java
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

package org.executequery.gui.table;

import org.executequery.databasemediators.DatabaseConnection;

/**
 * defines those objects with table functions requiring sql output
 *
 * @author Takis Diakoumis
 */
public interface TableModifier extends CreateTableSQLSyntax {

    int COLUMN_VALUES = 0;
    int CONSTRAINT_VALUES = 1;
    int EMPTY_VALUE = -1;

    /**
     * Generates and prints the SQL text.
     */
    void setSQLText();



    /**
     * Retrieves the currently selected/created table name.
     *
     * @return the table name
     */
    String getTableName();

    DatabaseConnection getSelectedConnection();

}













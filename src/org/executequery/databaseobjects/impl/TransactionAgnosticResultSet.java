/*
 * TransactionAgnosticResultSet.java
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

package org.executequery.databaseobjects.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TransactionAgnosticResultSet extends DelegatingResultSet {

    public TransactionAgnosticResultSet(Connection connection, Statement statement, ResultSet resultSet) {

        super(connection, statement, resultSet);
    }

    @Override
    public void close() throws SQLException {

        try {

            super.close();

            Statement statement = getStatement();
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {} 
            }

        } finally {

            Connection connection = getConnection();
            if (connection != null) {
                try {
                    connection.commit();
                    connection.close();
                } catch (SQLException e) {}
            }

        }

    }
    
}





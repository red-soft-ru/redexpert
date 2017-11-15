/*
 * DefaultConnectionRepositoryEvent.java
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

package org.executequery.event;

import java.util.ArrayList;
import java.util.List;

import org.executequery.databasemediators.DatabaseConnection;

/**
 *
 * @author   Takis Diakoumis
 */
public class DefaultConnectionRepositoryEvent extends AbstractApplicationEvent 
                                              implements ConnectionRepositoryEvent {

    private final List<DatabaseConnection> databaseConnections;

    public DefaultConnectionRepositoryEvent(
            Object source, String method, DatabaseConnection databaseConnection) {

        super(source, method);
        databaseConnections = new ArrayList<DatabaseConnection>();
        databaseConnections.add(databaseConnection);
    }

    public DefaultConnectionRepositoryEvent(
            Object source, String method, List<DatabaseConnection> databaseConnections) {
        
        super(source, method);
        this.databaseConnections = databaseConnections;
    }
    
    public List<DatabaseConnection> getDatabaseConnections() {

        return databaseConnections;
    }

    static final long serialVersionUID = -589504120102249710L;
    
}


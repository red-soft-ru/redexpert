/*
 * ConnectionRepositoryEvent.java
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

import java.util.List;

import org.executequery.databasemediators.DatabaseConnection;

/**
 *
 * @author   Takis Diakoumis
 */
public interface ConnectionRepositoryEvent extends ApplicationEvent {

    /** Method name for connection added */
    String CONNECTION_ADDED = "connectionAdded";
    
    /** Method name for connection modified */
    String CONNECTION_MODIFIED = "connectionModified";
    
    /** Method name for connection imported */
    String CONNECTION_IMPORTED = "connectionImported";
    
    /** Method name for connection removed */
    String CONNECTION_REMOVED = "connectionRemoved";

    List<DatabaseConnection> getDatabaseConnections();
    
}












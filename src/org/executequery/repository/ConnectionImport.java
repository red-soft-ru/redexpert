/*
 * ConnectionImport.java
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

package org.executequery.repository;

import java.util.List;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.browser.ConnectionsFolder;

public class ConnectionImport {

    private List<ConnectionsFolder> folders;
    
    private List<DatabaseConnection> connections;

    public ConnectionImport(List<ConnectionsFolder> folders, List<DatabaseConnection> connections) {
        super();
        this.folders = folders;
        this.connections = connections;
    }
    
    public int getFolderCount() {
        
        return folders.size();
    }
    
    public int getConnectionCount() {
        
        return connections.size();
    }
    
    public List<DatabaseConnection> getConnections() {
        
        return connections;
    }
    
    public List<ConnectionsFolder> getFolders() {
     
        return folders;
    }
    
}


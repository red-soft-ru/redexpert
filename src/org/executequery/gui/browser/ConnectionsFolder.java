/*
 * ConnectionsFolder.java
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

package org.executequery.gui.browser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;

public class ConnectionsFolder {

    private String id;
    
    private String name;

    private List<String> connections = new ArrayList<String>();
    
    public ConnectionsFolder() {}

    public ConnectionsFolder(String name) {
        this.name = name;
    }

    public String getConnectionsCommaSeparated() {
        return StringUtils.join(connections, ',');
    }

    public void setConnections(String connectionsCsv) {
        
        String[] split = StringUtils.split(connectionsCsv, ',');
        connections = new ArrayList<String>(split.length);

        for (String connection : split) {
            connections.add(connection);
        }
    }
    
    public void addConnection(String connectionId) {
        
        if (!contains(connectionId)) {
         
            connections.add(connectionId);
        }
    }
    
    private boolean contains(String connectionId) {

        for (String id : connections()) {
            
            if (StringUtils.equals(connectionId, id)) {
                
                return true;
            }
        }
        
        return false;
    }

    private List<String> connections() {

        if (connections == null) {
            
            connections = new ArrayList<String>();
        }
        return connections;
    }

    public void removeConnection(String id) {
        
        int index = 0;
        for (String connection : connections) {
            
            if (connection.equals(id)) {
                
                connections.remove(index);
                break;
            }
            index++;
        }
        
    }
    
    public List<String> getConnectionIds() {
        return connections;
    }

    public List<DatabaseConnection> getConnections() {

        List<DatabaseConnection> list = new ArrayList<DatabaseConnection>(connections.size());
        DatabaseConnectionRepository repository = connectionsRepository();
        for (String id : connections) {
        
            DatabaseConnection databaseConnection = repository.findById(id);
            if (databaseConnection != null) {
             
                list.add(databaseConnection);
            }

        }

        return list;
    }
    
    private DatabaseConnectionRepository connectionsRepository() {
        return (DatabaseConnectionRepository)RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID);
    }
    
    public String getId() {
        if (id == null) {
            setId(generateId());
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void empty() {
        connections.clear();
    }

    @Override
    public String toString() {
        return getName();
    }
    
}

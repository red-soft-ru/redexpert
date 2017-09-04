/*
 * ConnectionImporter.java
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.browser.ConnectionsFolder;
import org.executequery.gui.connections.ImportProcessMonitor;

public class ConnectionImporter {

    public ConnectionImport read(String fileName, ImportProcessMonitor importProcessMonitor) {
        
        Map<String, String> connectionMappings = new HashMap<>();
                
        DatabaseConnectionRepository databaseConnectionRepository = databaseConnectionRepository();
        List<DatabaseConnection> connections = databaseConnectionRepository.open(fileName);
        for (DatabaseConnection databaseConnection : connections) {
            
            importProcessMonitor.progress("Importing connection [ " + databaseConnection.getName() + " ]");

            connectionMappings.put(databaseConnection.getId(), databaseConnection.withNewId().getId());
            databaseConnectionRepository.add(databaseConnection);
        }
        
        databaseConnectionRepository.save();
        
        ConnectionFoldersRepository connectionFolderRepository = connectionFolderRepository();
        List<ConnectionsFolder> folders = connectionFolderRepository.open(fileName);
        for (ConnectionsFolder connectionsFolder : folders) {

            String name = connectionsFolder.getName();
            ConnectionsFolder existingFolder = connectionFolderRepository.findByName(name);
            if (existingFolder != null) {

                importProcessMonitor.progress("Merging folder [ " + name + " ]");

                List<String> folderConnectionsId = connectionsFolder.getConnectionIds();
                for (String connectionId : folderConnectionsId) {
                    
                    String newConnectionId = connectionMappings.get(connectionId);
                    if (newConnectionId != null) {
                        
                        existingFolder.addConnection(newConnectionId);
                    }

                }
                
            } else {
            
                importProcessMonitor.progress("Importing folder [ " + name + " ]");

                List<String> folderConnectionsId = connectionsFolder.getConnectionIds();
                for (String connectionId : folderConnectionsId) {
                    
                    String newConnectionId = connectionMappings.get(connectionId);
                    if (newConnectionId != null) {
                        
                        connectionsFolder.addConnection(newConnectionId);
                    }

                }

                connectionFolderRepository.add(connectionsFolder);
            }

        }
        connectionFolderRepository.save();

        return new ConnectionImport(folders, connections);
    }

    private ConnectionFoldersRepository connectionFolderRepository() {
        
        return (ConnectionFoldersRepository) RepositoryCache.load(ConnectionFoldersRepository.REPOSITORY_ID);
    }

    private DatabaseConnectionRepository databaseConnectionRepository() {

        return (DatabaseConnectionRepository) RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID);
    }

}


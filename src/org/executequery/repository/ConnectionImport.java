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

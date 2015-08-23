package org.executequery.datasource;

import org.executequery.databasemediators.DatabaseConnection;


public interface SshTunnel {

    int getTunnelPort();
    
    void connect(DatabaseConnection databaseConnection);
    
    void disconnect(DatabaseConnection databaseConnection);

}

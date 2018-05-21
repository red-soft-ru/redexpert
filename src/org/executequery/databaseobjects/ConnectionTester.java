package org.executequery.databaseobjects;

import org.executequery.databasemediators.ConnectionMediator;
import org.executequery.databasemediators.DatabaseConnection;

public class ConnectionTester {

    public boolean test(DatabaseConnection databaseConnection) {
        
        return ConnectionMediator.getInstance().test(databaseConnection);
    }
    
}

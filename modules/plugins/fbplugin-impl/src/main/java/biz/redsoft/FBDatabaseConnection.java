package biz.redsoft;

import org.firebirdsql.jdbc.FBConnection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Vasiliy on 4/6/2017.
 */
public class FBDatabaseConnection implements IFBDatabaseConnection {

    private FBConnection fbConnection = null;

    @Override
    public void setConnection(Connection connection) throws SQLException {

        this.fbConnection = (FBConnection) connection;
    }

    @Override
    public int getMajorVersion() throws SQLException {

        return fbConnection.getGDSHelper().getDatabaseProductMajorVersion();
    }

    @Override
    public int getMinorVersion() throws SQLException {

        return fbConnection.getGDSHelper().getDatabaseProductMinorVersion();
    }
}

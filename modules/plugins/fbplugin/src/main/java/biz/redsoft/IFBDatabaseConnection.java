package biz.redsoft;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Vasiliy on 4/6/2017.
 */
public interface IFBDatabaseConnection {

    public void setConnection(Connection connection) throws SQLException;

    public int getMajorVersion() throws SQLException;

    public int getMinorVersion() throws SQLException;

    public IFBBatch createBatch(String query) throws SQLException;
}

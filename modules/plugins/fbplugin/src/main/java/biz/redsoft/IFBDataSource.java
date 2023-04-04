package biz.redsoft;

import javax.resource.ResourceException;
import java.sql.Connection;
import java.sql.SQLException;

public interface IFBDataSource {

    void setUserName(String userName);

    void setPassword(String password);

    void setURL(String url);

    void setCharset(String charset);

    Connection getConnection() throws SQLException;

    Connection getConnection(ITPB tpb) throws SQLException;

    void close() throws ResourceException, SQLException;

    void setCertificate(String certificate);

    void setNonStandardProperty(String key, String value);

    void setTransactionParameters(Connection connection, ITPB tpb) throws SQLException;

    long getIDTransaction(Connection con) throws SQLException;
}

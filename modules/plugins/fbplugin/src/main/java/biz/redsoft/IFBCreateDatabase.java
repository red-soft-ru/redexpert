package biz.redsoft;

import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by Vasiliy on 05.04.2017.
 */
public interface IFBCreateDatabase {

    void setServer(String server);

    void setPort(int port);

    void setUser(String user);

    void setPassword(String password);

    void setDatabaseName(String databaseName);

    void setEncoding(String encoding);

    void setPageSize(int pageSize);

    void setJdbcProperties(Properties jdbcProperties);

    void exec() throws SQLException;
}

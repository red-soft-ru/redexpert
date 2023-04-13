package biz.redsoft;

import java.io.InputStream;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author vasiliy
 */
public interface IFBClob {

    void detach(Clob clob, Statement statement) throws SQLException;

    InputStream open() throws SQLException;

    void close() throws SQLException;
}

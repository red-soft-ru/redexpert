package biz.redsoft;

import java.io.InputStream;
import java.sql.Clob;
import java.sql.SQLException;

/**
 * @author vasiliy
 */
public interface IFBClob {

    void detach(Clob clob) throws SQLException;

    InputStream open() throws SQLException;

    void close() throws SQLException;
}

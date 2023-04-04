package biz.redsoft;

import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author vasiliy
 */
public interface IFBBlob {

    void detach(Blob blob, Statement statement) throws SQLException;

    byte[] getBytes(long pos, int lenght) throws SQLException;

    long lenght();

    void close() throws SQLException;
}

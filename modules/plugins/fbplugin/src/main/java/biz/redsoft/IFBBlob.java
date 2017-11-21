package biz.redsoft;

import java.sql.Blob;
import java.sql.SQLException;

/**
 * @author vasiliy
 */
public interface IFBBlob {

    void detach(Blob blob) throws SQLException;

    byte[] getBytes(long pos, int lenght) throws SQLException;

    long lenght();

    void close() throws SQLException;
}

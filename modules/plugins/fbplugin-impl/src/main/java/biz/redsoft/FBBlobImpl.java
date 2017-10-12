package biz.redsoft;

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.TransactionParameterBufferImpl;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.jdbc.FBBlob;
import org.firebirdsql.jdbc.FirebirdBlob;

import java.sql.Blob;
import java.sql.SQLException;

/**
 * @author vasiliy
 */
public class FBBlobImpl implements IFBBlob {
    DatabaseParameterBuffer buffer;
    FirebirdBlob detached = null;
    long lenght = 0;

    @Override
    public void detach(Blob blob) throws SQLException {
        FBBlob fbBlob = (FBBlob) blob;
        detached = fbBlob.detach();
        buffer = ((FBBlob) detached).getGdsHelper().getDatabaseParameterBuffer();
        lenght = detached.length();
    }

    @Override
    public byte[] getBytes(long pos, int lenght) throws SQLException {
        if (((FBBlob) detached).getGdsHelper().getCurrentTransaction() == null) {
            TransactionParameterBuffer tpb = new TransactionParameterBufferImpl();
            FbTransaction transaction = ((FBBlob) detached).getGdsHelper().startTransaction(tpb);
            ((FBBlob) detached).getGdsHelper().setCurrentTransaction(transaction);
        }
        return detached.getBytes(pos, lenght);
    }

    @Override
    public long lenght() {
        return lenght;
    }
}

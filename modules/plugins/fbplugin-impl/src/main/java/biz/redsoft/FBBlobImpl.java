package biz.redsoft;

import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.impl.TransactionParameterBufferImpl;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.jdbc.FBBlob;
import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.jdbc.FBStatement;

import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author vasiliy
 */
public class FBBlobImpl implements IFBBlob {
    long lenght = 0;
    FbTransaction transaction = null;
    GDSHelper gdsHelper = null;
    FBBlob fbBlob = null;
    long blobId = 0;

    @Override
    public void detach(Blob blob, Statement statement) throws SQLException {
        blobId = ((FBBlob) blob).getBlobId();
        FBStatement fbStatement = (FBStatement) statement;
        FBConnection connection = (FBConnection) fbStatement.getConnection();
        gdsHelper = new GDSHelper(connection.getFbDatabase());
        fbBlob = new FBBlob(gdsHelper, blobId);
    }

    @Override
    public byte[] getBytes(long pos, int lenght) throws SQLException {
        if (gdsHelper.getCurrentTransaction() == null) {
            TransactionParameterBuffer tpb = new TransactionParameterBufferImpl();
            transaction = gdsHelper.startTransaction(tpb);
            gdsHelper.setCurrentTransaction(transaction);
        }
        return fbBlob.getBytes(pos, lenght);
    }

    @Override
    public long lenght() {
        return lenght;
    }

    @Override
    public void close() throws SQLException {
        if (transaction == null)
            return;
        if (transaction.getState() != TransactionState.COMMITTED)
            transaction.commit();
        fbBlob.free();
        gdsHelper.setCurrentTransaction(null);
    }
}

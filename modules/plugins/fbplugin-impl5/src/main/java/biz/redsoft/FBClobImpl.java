package biz.redsoft;

import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.impl.TransactionParameterBufferImpl;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.jdbc.FBBlob;
import org.firebirdsql.jdbc.FBClob;
import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.jdbc.FBStatement;

import java.io.InputStream;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author vasiliy
 */
public class FBClobImpl implements IFBClob {
    FbTransaction transaction = null;
    GDSHelper gdsHelper = null;
    FBBlob fbBlob = null;
    long blobId = 0;

    @Override
    public void detach(Clob clob, Statement statement) throws SQLException {
        blobId = ((FBClob) clob).getWrappedBlob().getBlobId();
        FBStatement fbStatement = (FBStatement) statement;
        FBConnection connection = (FBConnection) fbStatement.getConnection();
        gdsHelper = new GDSHelper(connection.getFbDatabase());
    }

    @Override
    public InputStream open() throws SQLException {
        if (gdsHelper.getCurrentTransaction() == null) {
            TransactionParameterBuffer tpb = new TransactionParameterBufferImpl();
            transaction = gdsHelper.startTransaction(tpb);
            gdsHelper.setCurrentTransaction(transaction);
        }
        fbBlob = new FBBlob(gdsHelper, blobId);
        return fbBlob.getBinaryStream();
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

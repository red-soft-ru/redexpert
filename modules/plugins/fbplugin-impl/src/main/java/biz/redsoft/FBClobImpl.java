package biz.redsoft;

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.TransactionParameterBufferImpl;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.jdbc.FBBlob;
import org.firebirdsql.jdbc.FBClob;
import org.firebirdsql.jdbc.FirebirdBlob;

import java.io.InputStream;
import java.sql.Clob;
import java.sql.SQLException;

/**
 * @author vasiliy
 */
public class FBClobImpl implements IFBClob {
    DatabaseParameterBuffer buffer;
    FirebirdBlob detached = null;
    FbTransaction transaction = null;

    @Override
    public void detach(Clob clob) throws SQLException {
        FBClob fbClob = (FBClob) clob;
        detached = fbClob.getWrappedBlob().detach();
        buffer = ((FBBlob) detached).getGdsHelper().getDatabaseParameterBuffer();
    }

    @Override
    public InputStream open() throws SQLException {
        if (((FBBlob) detached).getGdsHelper().getCurrentTransaction() == null) {
            TransactionParameterBuffer tpb = new TransactionParameterBufferImpl();
            transaction = ((FBBlob) detached).getGdsHelper().startTransaction(tpb);
            ((FBBlob) detached).getGdsHelper().setCurrentTransaction(transaction);
        }
        return detached.getBinaryStream();
    }

    @Override
    public void close() throws SQLException {
        if (transaction == null)
            return;
        transaction.commit();
        detached.free();
        ((FBBlob) detached).getGdsHelper().setCurrentTransaction(null);
    }
}

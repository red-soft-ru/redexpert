package biz.redsoft;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.TransactionParameterBufferImpl;
import org.firebirdsql.gds.ng.FbBatch;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.nativeoo.gds.ng.IBatchImpl;

import java.sql.SQLException;

public class FBBatchImpl implements IFBBatch {

    private final IBatchImpl batch;

    public FBBatchImpl(FbBatch fbBatch) {
        batch = (IBatchImpl) fbBatch;
    }

    @Override
    public void setObject(int index, Object o) throws SQLException {
        batch.setObject(index, o);
    }

    @Override
    public IFBBlob addBlob(int index, byte[] inBuffer) throws SQLException {
        // TODO check blob is closed
        batch.addBlob(index, inBuffer, null);
        return null;
    }

    public void addBatch() throws SQLException {
        batch.addBatch();
    }

    @Override
    public IFBBatchCompletionState execute() throws SQLException {
        return new FBBatchCompletionStateImpl(batch.execute());
    }

    @Override
    public void startTransaction() throws SQLException {
        TransactionParameterBuffer tpb = new TransactionParameterBufferImpl();
        tpb.addArgument(ISCConstants.isc_tpb_read_committed);
        tpb.addArgument(ISCConstants.isc_tpb_rec_version);
        tpb.addArgument(ISCConstants.isc_tpb_write);
        tpb.addArgument(ISCConstants.isc_tpb_wait);

        FbTransaction fbTransaction = batch.getDatabase().startTransaction(tpb);

        batch.setTransaction(fbTransaction);
    }

    @Override
    public void commit() throws SQLException {
        batch.getTransaction().commit();
    }

    @Override
    public void cancel() throws SQLException {
        batch.cancel();
    }
}

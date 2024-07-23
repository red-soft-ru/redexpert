package biz.redsoft;


import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.jaybird.xca.FBSADataSource;
import org.firebirdsql.jdbc.FBConnection;

import javax.resource.ResourceException;
import java.sql.Connection;
import java.sql.SQLException;

import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.firebirdsql.gds.VaxEncoding.iscVaxLong;


public class FBDataSourceImpl implements IFBDataSource {
    private final FBSADataSource fbDataSource;

    public FBDataSourceImpl() {
        fbDataSource = new FBSADataSource(GDSType.getType("PURE_JAVA"));
    }

    public FBDataSourceImpl(String type) {
        fbDataSource = new FBSADataSource(GDSType.getType(type));
    }

    @Override
    public void setUserName(String userName) {
        fbDataSource.setUserName(userName);
    }

    @Override
    public void setPassword(String password) {
        fbDataSource.setPassword(password);
    }


    @Override
    public void setURL(String url) {
        if (url.startsWith("jdbc:firebirdsql://"))
            url = url.substring(17);
        fbDataSource.setDatabase(url);
    }

    @Override
    public void setCharset(String charset) {
        fbDataSource.setEncoding(charset);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return fbDataSource.getConnection();
    }

    @Override
    public Connection getConnection(ITPB tpb) throws SQLException {
        Connection conn = getConnection();
        setTransactionParameters(conn, tpb);
        return conn;
    }

    @Override
    public void setCertificate(String certificate) {
        if (!certificate.equals(""))
            fbDataSource.setCertificate(certificate);
    }

    @Override
    public void setNonStandardProperty(String key, String value) {
        // add any parameters with isc_ prefix
        fbDataSource.setNonStandardProperty(key, value);
    }

    @Override
    public void close() throws ResourceException, SQLException {
        fbDataSource.close();
    }

    public void setTransactionParameters(Connection connection, biz.redsoft.ITPB tpb) throws SQLException {
        if (connection instanceof FBConnection) {
            if (tpb != null)
                ((FBConnection) connection).setTransactionParameters((TransactionParameterBuffer) tpb.getTpb());
        }
    }

    public long getIDTransaction(Connection con) throws SQLException {
        FbTransaction transaction = ((FBConnection) con).getGDSHelper().getCurrentTransaction();
        if (transaction != null)
            return transaction.getTransactionId();
        else return -1;
    }

    @Override
    public long getSnapshotTransaction(Connection con) throws SQLException {
        FbTransaction transaction = ((FBConnection) con).getGDSHelper().getCurrentTransaction();
        if (transaction != null) {
            return transaction.getTransactionInfo(new byte[]{ITPBConstants.fb_info_tra_snapshot_number}, 16, infoResponse -> {
                if (infoResponse[0] != ITPBConstants.fb_info_tra_snapshot_number) {
                    throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_unexpectedInfoResponse)
                            .messageParameter(
                                    "transaction", "fb_info_tra_snapshot_number", ITPBConstants.fb_info_tra_snapshot_number, infoResponse[0])
                            .toSQLException();
                }
                int length = iscVaxInteger2(infoResponse, 1);
                return iscVaxLong(infoResponse, 3, length);
            });
        } else return -1;

    }
}


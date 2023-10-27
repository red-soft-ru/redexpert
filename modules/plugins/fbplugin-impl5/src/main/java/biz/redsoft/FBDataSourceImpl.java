package biz.redsoft;


import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.jaybird.xca.FBSADataSource;
import org.firebirdsql.jdbc.FBConnection;

import javax.resource.ResourceException;
import java.sql.Connection;
import java.sql.SQLException;


public class FBDataSourceImpl implements IFBDataSource {
    private FBSADataSource fbDataSource;

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
}


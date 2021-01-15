package biz.redsoft;


import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jca.FBSADataSource;

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
    public void close() throws ResourceException {
        fbDataSource.close();
    }
}


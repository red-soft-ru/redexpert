package biz.redsoft;

import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbDatabaseFactory;

import java.sql.SQLException;

/*** Created by Vasiliy on 05.04.2017.
 */
public class FBCreateDatabaseImpl implements IFBCreateDatabase {
    private String server;
    private int port;
    private String user;
    private String password;
    private String databaseName;
    private String encoding;
    private int pageSize;

    @Override
    public void setServer(String server) {
        this.server = server;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public void exec() throws SQLException {
        if (port == 0) {
            port = 3050;
        }
        final FbConnectionProperties connectionInfo;
        {
            connectionInfo = new FbConnectionProperties();
            connectionInfo.setServerName(server);
            connectionInfo.setPortNumber(port);
            connectionInfo.setUser(user);
            connectionInfo.setPassword(password);
            connectionInfo.setDatabaseName(databaseName);
            connectionInfo.setEncoding(encoding);
            connectionInfo.getExtraDatabaseParameters().addArgument(4, pageSize);
        }

        FbDatabaseFactory factory = null;
        FbDatabase db = null;
        factory = GDSFactory.getDatabaseFactoryForType(GDSFactory.getDefaultGDSType());

        db = factory.connect(connectionInfo);
        db.createDatabase();
        db.close();
    }
}

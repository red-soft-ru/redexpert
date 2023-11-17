package biz.redsoft;

import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.ng.FbService;
import org.firebirdsql.management.FBServiceManager;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

public abstract class AbstractServiceManager implements IFBServiceManager {
    protected FBServiceManager fbServiceManager;

    public AbstractServiceManager() {
        initServiceManager();
    }

    protected abstract void initServiceManager();

    @Override
    public String getCharSet() {
        return fbServiceManager.getCharSet();
    }

    @Override
    public void setCharSet(String var1) {
        fbServiceManager.setCharSet(var1);
    }

    @Override
    public String getUser() {
        return fbServiceManager.getUser();
    }

    @Override
    public void setUser(String var1) {
        fbServiceManager.setUser(var1);
    }

    @Override
    public String getPassword() {
        return fbServiceManager.getPassword();
    }

    @Override
    public void setPassword(String var1) {
        fbServiceManager.setPassword(var1);
    }

    @Override
    public String getDatabase() {
        return fbServiceManager.getDatabase();
    }

    @Override
    public void setDatabase(String var1) {
        fbServiceManager.setDatabase(var1);
    }

    @Override
    public String getHost() {
        return fbServiceManager.getHost();
    }

    @Override
    public void setHost(String var1) {
        fbServiceManager.setHost(var1);
    }

    @Override
    public int getPort() {
        return fbServiceManager.getPort();
    }

    @Override
    public void setPort(int var1) {
        fbServiceManager.setPort(var1);
    }

    @Override
    public OutputStream getLogger() {
        return fbServiceManager.getLogger();
    }

    @Override
    public void setLogger(OutputStream var1) {
        fbServiceManager.setLogger(var1);
    }

    public void queueService(FbService service) throws SQLException, IOException {
        fbServiceManager.queueService(service);
    }

    protected final void executeServicesOperation(FbService service, ServiceRequestBuffer srb) throws SQLException {
        try {
            service.startServiceAction(srb);
            queueService(service);
        } catch (IOException ioe) {
            throw new SQLException(ioe);
        }
    }
}

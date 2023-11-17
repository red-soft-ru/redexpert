package biz.redsoft;

import org.firebirdsql.management.FBTraceManager;

import java.sql.SQLException;

public class FBTraceManagerImpl extends AbstractServiceManager implements IFBTraceManager {
    FBTraceManager fbTraceManager;

    public FBTraceManagerImpl() {
        super();
    }

    @Override
    protected void initServiceManager() {
        fbTraceManager = new FBTraceManager();
        fbServiceManager = fbTraceManager;
    }

    @Override
    public Integer getSessionID(String sessionName) {
        return fbTraceManager.getSessionId(sessionName);
    }

    @Override
    public void startTraceSession(String var1, String var2) throws SQLException {
        fbTraceManager.startTraceSession(var1, var2);
    }

    @Override
    public void stopTraceSession(int var1) throws SQLException {
        fbTraceManager.stopTraceSession(var1);
    }

    @Override
    public void suspendTraceSession(int var1) throws SQLException {
        fbTraceManager.suspendTraceSession(var1);
    }

    @Override
    public void resumeTraceSession(int var1) throws SQLException {
        fbTraceManager.resumeTraceSession(var1);
    }

    @Override
    public void listTraceSessions() throws SQLException {
        fbTraceManager.listTraceSessions();
    }

}
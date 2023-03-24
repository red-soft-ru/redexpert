package biz.redsoft;

import org.firebirdsql.management.FBTraceManager;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

public class FBTraceManagerImpl implements IFBTraceManager {
    FBTraceManager fbTraceManager;

    public FBTraceManagerImpl() {
        fbTraceManager = new FBTraceManager();
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

    @Override
    public String getCharSet() {
        return fbTraceManager.getCharSet();
    }

    @Override
    public void setCharSet(String var1) {
        fbTraceManager.setCharSet(var1);
    }

    @Override
    public String getUser() {
        return fbTraceManager.getUser();
    }

    @Override
    public void setUser(String var1) {
        fbTraceManager.setUser(var1);
    }

    @Override
    public String getPassword() {
        return fbTraceManager.getPassword();
    }

    @Override
    public void setPassword(String var1) {
        fbTraceManager.setPassword(var1);
    }

    @Override
    public String getDatabase() {
        return fbTraceManager.getDatabase();
    }

    @Override
    public void setDatabase(String var1) {
        fbTraceManager.setDatabase(var1);
    }

    @Override
    public String getHost() {
        return fbTraceManager.getHost();
    }

    @Override
    public void setHost(String var1) {
        fbTraceManager.setHost(var1);
    }

    @Override
    public int getPort() {
        return fbTraceManager.getPort();
    }

    @Override
    public void setPort(int var1) {
        fbTraceManager.setPort(var1);
    }

    @Override
    public OutputStream getLogger() {
        return fbTraceManager.getLogger();
    }

    @Override
    public void setLogger(OutputStream var1) {
        fbTraceManager.setLogger(var1);
    }

}

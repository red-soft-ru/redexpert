package biz.redsoft;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

public interface IFBTraceManager extends IFBServiceManager {
    Integer getSessionID(String sessionName);

    void startTraceSession(String var1, String var2) throws SQLException;

    void stopTraceSession(int var1) throws SQLException;

    void suspendTraceSession(int var1) throws SQLException;

    void resumeTraceSession(int var1) throws SQLException;

    void listTraceSessions() throws SQLException;


}

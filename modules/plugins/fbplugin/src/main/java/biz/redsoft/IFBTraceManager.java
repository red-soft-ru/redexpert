package biz.redsoft;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

public interface IFBTraceManager {
    Integer getSessionID(String sessionName);

    void startTraceSession(String var1, String var2) throws SQLException;

    void stopTraceSession(int var1) throws SQLException;

    void suspendTraceSession(int var1) throws SQLException;

    void resumeTraceSession(int var1) throws SQLException;

    void listTraceSessions() throws SQLException;

    String getCharSet();

    void setCharSet(String var1);

    String getUser();

    void setUser(String var1);

    String getPassword();

    void setPassword(String var1);

    String getDatabase();

    void setDatabase(String var1);

    String getHost();

    void setHost(String var1);

    int getPort();

    void setPort(int var1);

    OutputStream getLogger();

    void setLogger(OutputStream var1);
}

package biz.redsoft;

import java.io.OutputStream;

public interface IFBServiceManager {
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

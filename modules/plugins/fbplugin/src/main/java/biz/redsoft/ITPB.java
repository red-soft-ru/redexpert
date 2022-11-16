package biz.redsoft;

import java.io.IOException;
import java.io.OutputStream;

public interface ITPB {

    void initTPB();

    int getType();

    void addArgument(int var1);

    void addArgument(int var1, String var2);

    void addArgument(int var1, String var2, Object var3);

    void addArgument(int var1, int var2);

    void addArgument(int var1, long var2);

    void addArgument(int var1, byte[] var2);

    void removeArgument(int var1);

    String getArgumentAsString(int var1);

    int getArgumentAsInt(int var1);

    boolean hasArgument(int var1);

    void writeArgumentsTo(OutputStream var1) throws IOException;

    byte[] toBytes();

    byte[] toBytesWithType();

    int size();

    Object getTpb();
}

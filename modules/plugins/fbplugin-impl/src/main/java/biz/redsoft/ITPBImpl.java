package biz.redsoft;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.impl.TransactionParameterBufferImpl;

import java.io.IOException;
import java.io.OutputStream;

public class ITPBImpl implements ITPB {
    TransactionParameterBufferImpl tpb;

    @Override
    public void initTPB() {
        tpb = new TransactionParameterBufferImpl();
    }

    @Override
    public int getType() {
        return tpb.getType();
    }

    @Override
    public void addArgument(int var1) {
        tpb.addArgument(var1);
    }

    @Override
    public void addArgument(int var1, String var2) {
        tpb.addArgument(var1, var2);
    }

    @Override
    public void addArgument(int var1, String var2, Object var3) {
        tpb.addArgument(var1, var2, (Encoding) var3);
    }

    @Override
    public void addArgument(int var1, int var2) {
        tpb.addArgument(var1, var2);
    }

    @Override
    public void addArgument(int var1, long var2) {
        tpb.addArgument(var1, var2);
    }

    @Override
    public void addArgument(int var1, byte[] var2) {
        tpb.addArgument(var1, var2);
    }

    @Override
    public void removeArgument(int var1) {
        tpb.removeArgument(var1);
    }

    @Override
    public String getArgumentAsString(int var1) {
        return tpb.getArgumentAsString(var1);
    }

    @Override
    public int getArgumentAsInt(int var1) {
        return tpb.getArgumentAsInt(var1);
    }

    @Override
    public boolean hasArgument(int var1) {
        return tpb.hasArgument(var1);
    }

    @Override
    public void writeArgumentsTo(OutputStream var1) throws IOException {
        tpb.writeArgumentsTo(var1);
    }

    @Override
    public byte[] toBytes() {
        return tpb.toBytes();
    }

    @Override
    public byte[] toBytesWithType() {
        return tpb.toBytesWithType();
    }

    @Override
    public int size() {
        return tpb.size();
    }

    public TransactionParameterBufferImpl getTpb() {
        return tpb;
    }

    public void setTpb(TransactionParameterBufferImpl tpb) {
        this.tpb = tpb;
    }
}


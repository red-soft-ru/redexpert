package biz.redsoft;

import org.firebirdsql.gds.ng.jna.NativeResourceTracker;

@SuppressWarnings("unused")
public class FBClientLoaderImpl implements IFBClientLoader {

    @Override
    public void dispose(Object fbclient) {
        NativeResourceTracker.shutdownNativeResources();
    }

    @Override
    public Object load(int driverVersion) {
        return null;
    }

}

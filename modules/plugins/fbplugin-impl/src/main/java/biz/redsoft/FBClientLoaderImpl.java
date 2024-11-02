package biz.redsoft;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;
import org.firebirdsql.gds.ng.jna.NativeLibraryLoadException;
import org.firebirdsql.gds.ng.jna.NativeResourceTracker;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.jna.fbclient.WinFbClientLibrary;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class FBClientLoaderImpl implements IFBClientLoader {
    private static final Logger log = LoggerFactory.getLogger(FBClientLoaderImpl.class);

    @Override
    public void dispose(Object fbclient) {
        if (fbclient != null)
            manuallyShutdown((FbClientLibrary) fbclient);
        else
            NativeResourceTracker.shutdownNativeResources();
    }

    @Override
    public Object load(int driverVersion) {
        return driverVersion < 4 ? createClientLibrary() : null;
    }

    @SuppressWarnings("deprecation")
    private static void manuallyShutdown(FbClientLibrary fbClientLibrary) {

        InvocationHandler invocationHandler = Proxy.getInvocationHandler(fbClientLibrary);
        Library.Handler originalHandler = (Library.Handler) invocationHandler;
        NativeLibrary nativeLibrary = originalHandler.getNativeLibrary();

        fbClientLibrary.fb_shutdown(0, 1);
        nativeLibrary.dispose();
    }

    private FbClientLibrary createClientLibrary() {

        final List<Throwable> throwablesList = new ArrayList<>();
        final List<String> librariesToTry = Arrays.asList("fbembed", "fbclient");

        for (String libraryName : librariesToTry) {
            try {
                if (Platform.isWindows()) {
                    return Native.load(libraryName, WinFbClientLibrary.class);
                } else {
                    return Native.load(libraryName, FbClientLibrary.class);
                }

            } catch (RuntimeException | UnsatisfiedLinkError e) {
                throwablesList.add(e);
                log.debug("Attempt to load " + libraryName + " failed", e);
                // continue with next
            }
        }

        assert throwablesList.size() == librariesToTry.size();
        log.error("Could not load any of the libraries in " + librariesToTry + ":");

        for (int idx = 0; idx < librariesToTry.size(); idx++)
            log.error("Loading " + librariesToTry.get(idx) + " failed", throwablesList.get(idx));

        throw new NativeLibraryLoadException(
                "Could not load any of " + librariesToTry + "; linking first exception",
                throwablesList.get(0)
        );
    }

}

package biz.redsoft.net;

import ch.sla.jdbcperflogger.model.ConnectionInfo;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ServerLogReceiver extends AbstractLogReceiver {
    public Set<AbstractLogReceiver> getChildReceivers() {
        return childReceivers;
    }

    private final Set<AbstractLogReceiver> childReceivers = new CopyOnWriteArraySet<>();
    private int listenPort;

    private volatile ServerSocket serverSocket;
    private final CountDownLatch serverStartedLatch = new CountDownLatch(1);

    public ServerLogReceiver(final int listenPort) {
        this.listenPort = listenPort;
    }

    @Override
    public void dispose() {
        super.dispose();
        try {
            final ServerSocket serverSocketLocalVar = serverSocket;
            if (serverSocketLocalVar != null) {
                serverSocketLocalVar.close();
            }
        } catch (final IOException e) {
        }
        try {
            this.join();
        } catch (final InterruptedException e) {
            // ignore
        }
    }

    @Override
    public int getConnectionsCount() {
        int cnt = 0;
        for (final AbstractLogReceiver receiver : childReceivers) {
            cnt += receiver.getConnectionsCount();
        }
        return cnt;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocketLocalVar = new ServerSocket(listenPort)) {

            listenPort = serverSocketLocalVar.getLocalPort();
            serverSocketLocalVar.setSoTimeout((int) TimeUnit.MINUTES.toMillis(5));
            serverSocket = serverSocketLocalVar;
            this.setName("ServerLogReceiver " + listenPort);

            try (LogProcessor logProcessor = new LogProcessor()) {
                logProcessor.start();

                // signal threads that might be waiting for the server to be ready
                serverStartedLatch.countDown();

                while (!disposed) {
                    try {

                        final Socket socket = serverSocketLocalVar.accept();

                        final AbstractLogReceiver logReceiver = new AbstractLogReceiver() {
                            @Override
                            public void run() {
                                try {
                                    handleConnection(socket/*, logProcessor*/);
                                } catch (final IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    childReceivers.remove(this);
                                }
                            }

                            @Override
                            public boolean isServerMode() {
                                return true;
                            }
                        };
                        logReceiver.setName("LogReceiver " + socket.getRemoteSocketAddress());
                        if (isPaused()) {
                            logReceiver.pauseReceivingLogs();
                        }
                        childReceivers.add(logReceiver);
                        logReceiver.start();
                    } catch (final SocketTimeoutException e) {

                    } catch (final IOException e) {
                        if (!disposed) {

                        } else {

                        }
                        e.printStackTrace();
                    }
                }
            } finally {

                if (!serverSocketLocalVar.isClosed()) {
                    try {
                        serverSocketLocalVar.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean isServerMode() {
        return true;
    }

    @Override
    public void pauseReceivingLogs() {
        super.pauseReceivingLogs();
        for (final AbstractLogReceiver child : childReceivers) {
            child.pauseReceivingLogs();
        }
    }

    @Override
    public void resumeReceivingLogs() {
        super.resumeReceivingLogs();
        for (final AbstractLogReceiver child : childReceivers) {
            child.resumeReceivingLogs();
        }
    }

    public static URL[] loadURLs(String paths) throws MalformedURLException {
        String token = ";";
        Vector<String> pathsVector = new Vector<String>();

        if (paths.indexOf(token) != -1) {
            StringTokenizer st = new StringTokenizer(paths, token);
            while (st.hasMoreTokens()) {
                pathsVector.add(st.nextToken());
            }
        }
        else {
            pathsVector.add(paths);
        }

        URL[] urls = new URL[pathsVector.size()];
        for (int i = 0; i < urls.length; i++) {
            File f = new File((String)pathsVector.elementAt(i));
            urls[i] = f.toURI().toURL();
        }
        return urls;
    }

    public static String formatNumber(long number, String pattern) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        DecimalFormat df = (DecimalFormat)nf;
        df.applyPattern(pattern);
        return df.format(number);
    }


}

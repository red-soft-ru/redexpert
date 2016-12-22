package org.executequery.gui.jdbclogger.net;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import ch.sla.jdbcperflogger.model.LogMessage;

public abstract class AbstractLogReceiver extends Thread {
    protected int SOCKET_TIMEOUT = 60 * 1000;

    protected volatile boolean connected;
    protected volatile boolean paused = false;
    protected volatile boolean disposed = false;
    protected volatile LogProcessor logProcessor = new LogProcessor();

    public LogProcessor getLogProcessor() {
        return logProcessor;
    }

    public AbstractLogReceiver() {
        this.setDaemon(true);
        logProcessor.start();
    }

    public int getConnectionsCount() {
        return connected ? 1 : 0;
    }

    public void pauseReceivingLogs() {
        paused = true;
    }

    public void resumeReceivingLogs() {
        paused = false;
    }

    public boolean isPaused() {
        return paused;
    }

    public void dispose() {
        disposed = true;
    }

    protected void handleConnection(final Socket socket/*, final LogProcessor logProcessor*/) throws IOException {
        socket.setKeepAlive(true);
        socket.setSoTimeout(SOCKET_TIMEOUT);

        final InputStream is = socket.getInputStream();

        try (ObjectInputStream ois = new ObjectInputStream(is)) {
            connected = true;
            while (!disposed) {
                Object o;
                try {
                    o = ois.readObject();
                } catch (final ClassNotFoundException e) {
                    e.printStackTrace();
                    continue;
                } catch (final EOFException e) {
                    e.printStackTrace();
                    break;
                } catch (final SocketTimeoutException e) {
                    e.printStackTrace();
                    continue;
                }
                if (o == null || paused || disposed) {
                    continue;
                }

                this.logProcessor.putMessage((LogMessage) o);

            }
        } finally {
            connected = false;
            socket.close();
        }

    }

    public abstract boolean isServerMode();
}

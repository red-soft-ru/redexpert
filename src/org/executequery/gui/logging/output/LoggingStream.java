package org.executequery.gui.logging.output;

import org.executequery.GUIUtilities;
import org.executequery.log.Log;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class that allows to work with the <code>LoggingOutputStream</code> as a <code>OutputStream</code>
 *
 * @author Aleksey Kozlov
 */
public final class LoggingStream extends ByteArrayOutputStream {

    private final LoggingOutputPane outputPane;
    private final StreamBuffer streamBuffer;
    private final Timer updateTimer;

    private FileOutputStream fileStream;
    private boolean enableFileStream;

    public LoggingStream(LoggingOutputPane outputPane, int bufferSize, boolean trimByLine) {
        this.streamBuffer = new StreamBuffer(bufferSize, trimByLine);
        this.enableFileStream = false;
        this.outputPane = outputPane;

        updateTimer = new Timer(100, e -> updatePanel());
        updateTimer.start();
    }

    public void setLogFilePath(String filePath) {
        enableFileStream = false;

        if (MiscUtils.isNull(filePath))
            return;

        try {
            Path logFilePath = Paths.get(filePath).toAbsolutePath();

            Path parentPath = logFilePath.getParent();
            if (parentPath != null && Files.notExists(parentPath))
                Files.createDirectories(parentPath);

            fileStream = new FileOutputStream(logFilePath.toFile(), true);
            enableFileStream = true;

        } catch (IOException e) {
            Log.error(e.getMessage(), e);
            GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e, getClass());
        }
    }

    private void updatePanel() {
        if (!streamBuffer.isEmpty())
            GUIUtils.invokeLater(() -> outputPane.setText(streamBuffer.toString()));
    }

    // --- ByteArrayOutputStream impl ---

    @Override
    public synchronized void write(int b) {
        try {
            streamBuffer.append((char) b);
            if (enableFileStream)
                fileStream.write(b);

        } catch (IOException e) {
            Log.debug(e.getMessage(), e);
        }
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) {
        try {
            streamBuffer.append(new String(b, off, len));
            if (enableFileStream)
                fileStream.write(b, off, len);

        } catch (IOException e) {
            Log.debug(e.getMessage(), e);
        }
    }

    @Override
    public void close() throws IOException {
        updateTimer.stop();
        updatePanel();

        if (enableFileStream)
            fileStream.close();
    }

    // --- OutputStream impl ---

    @Override
    public void flush() throws IOException {
        if (enableFileStream)
            fileStream.flush();
    }

}

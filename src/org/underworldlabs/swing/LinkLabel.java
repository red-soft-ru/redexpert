package org.underworldlabs.swing;

import org.executequery.Constants;
import org.executequery.log.Log;
import org.underworldlabs.swing.listener.MouseHoverPainter;
import org.underworldlabs.swing.util.SwingWorker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * JLabel implementation with the hyperlink behaviour.
 *
 * @author Alexey Kozlov
 */
public class LinkLabel extends JLabel {

    private URI uri;
    private final String link;

    public LinkLabel(String text, String link) {
        super(text);
        this.link = link;

        init();
    }

    private void init() {
        try {
            uri = new URI(link);
        } catch (URISyntaxException e) {
            Log.error("Error constructing link URI", e);
        }

        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new LinkMouseListener());
        addMouseListener(new MouseHoverPainter(this, MouseHoverPainter.Preset.COLORED_FOREGROUND));
    }

    private final class LinkMouseListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            new SwingWorker(String.format("Browse Link [%s]", link)) {

                @Override
                public Object construct() {

                    if (uri == null)
                        return Constants.WORKER_CANCEL;

                    try {
                        Desktop desktop = Desktop.getDesktop();
                        desktop.browse(uri);
                        return Constants.WORKER_SUCCESS;

                    } catch (IOException e) {
                        Log.error("Error browsing link + " + link, e);
                        return Constants.WORKER_FAIL;
                    }
                }

            }.start();
        }

    } // LinkMouseListener

}

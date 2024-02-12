package org.executequery.gui.browser.managment;

import org.executequery.GUIUtilities;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionEvent;
import org.executequery.event.ConnectionListener;
import org.executequery.gui.browser.GrantManagerPanel;
import org.executequery.gui.browser.UserManagerPanel;

public class GrantManagerConnectionListener implements ConnectionListener {

    public GrantManagerConnectionListener() {


    }

    @Override
    public void connected(ConnectionEvent connectionEvent) {

    }

    @Override
    public void disconnected(ConnectionEvent connectionEvent) {
        if (GUIUtilities.getCentralPane(GrantManagerPanel.TITLE) != null) {
            GrantManagerPanel gmp = (GrantManagerPanel) GUIUtilities.getCentralPane(GrantManagerPanel.TITLE);
            if (connectionEvent.getDatabaseConnection() == gmp.connection)
                gmp.loadConnections();
        }
        if (GUIUtilities.getCentralPane(UserManagerPanel.TITLE) != null) {
            UserManagerPanel ump = (UserManagerPanel) GUIUtilities.getCentralPane(UserManagerPanel.TITLE);
            if (connectionEvent.getDatabaseConnection() == ump.getSelectedConnection()) {
                try {
                    ump.refreshNoConnection();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean canHandleEvent(ApplicationEvent event) {
        return (event instanceof ConnectionEvent);
    }
}

package org.executequery.actions.databasecommands;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;

public class ConnectToDatabaseCommand extends OpenFrameCommand implements BaseCommand {

    public void execute(ActionEvent e) {

        GUIUtilities.ensureDockedTabVisible(ConnectionsTreePanel.PROPERTY_KEY);
        ConnectionsTreePanel panel = connectionsPanel();
        DatabaseConnection connection = panel.getSelectedDatabaseConnection();
        if (connection != null && !connection.isConnected()) {
            ((DatabaseHost) panel.getHostNode(connection).getDatabaseObject()).connect();
        }


    }


    private ConnectionsTreePanel connectionsPanel() {

        return (ConnectionsTreePanel) GUIUtilities.
                getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY);
    }

}
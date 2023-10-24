package org.executequery.gui.components;


import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.ConnectionManager;
import org.underworldlabs.swing.ListSelectionPanel;
import org.underworldlabs.swing.layouts.GridBagHelper;

import java.awt.*;
import java.util.List;
import java.util.Vector;

public class SelectConnectionsPanel extends AbstractDialogPanel {
    ListSelectionPanel listSelectionPanel;

    public SelectConnectionsPanel() {
        init();
    }

    private void init() {
        Vector<DatabaseConnection> connections = ConnectionManager.getActiveConnections();
        listSelectionPanel = new ListSelectionPanel();
        listSelectionPanel.createAvailableList(connections);

        mainPanel.setLayout(new GridBagLayout());
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaultsStatic().defaults();
        mainPanel.add(listSelectionPanel, gbh.fillBoth().spanX().spanY().get());
    }

    @Override
    protected void ok() {

    }

    public List<DatabaseConnection> getSelectedConnections() {
        return listSelectionPanel.getSelectedValues();
    }
}

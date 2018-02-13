package org.executequery.gui.components;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.DynamicComboBoxModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Vector;

public class OpenConnectionsComboboxPanel extends JPanel {
    public JComboBox connectionsCombo;
    public DynamicComboBoxModel connectionsModel;

    public OpenConnectionsComboboxPanel() {
        init();
    }

    void init() {
        Vector<DatabaseConnection> connections = ConnectionManager.getActiveConnections();
        connectionsModel = new DynamicComboBoxModel(connections);
        connectionsCombo = WidgetFactory.createComboBox(connectionsModel);

        setLayout(new GridBagLayout());
        JLabel connLabel = new JLabel(Bundles.getCommon("connection"));
        add(connLabel, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        add(connectionsCombo, new GridBagConstraints(1, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
    }

    public void addActionListener(ActionListener listener) {
        connectionsCombo.addActionListener(listener);
    }

    public DatabaseConnection getSelectedConnection() {
        return (DatabaseConnection) connectionsCombo.getSelectedItem();
    }

    public void setSelectedConnection(DatabaseConnection connection) {
        connectionsCombo.setSelectedItem(connection);
    }
}

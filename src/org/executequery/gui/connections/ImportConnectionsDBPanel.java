package org.executequery.gui.connections;

import org.executequery.GUIUtilities;
import org.executequery.components.BottomButtonPanel;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseConnectionFactory;
import org.executequery.databasemediators.spi.DatabaseConnectionFactoryImpl;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.components.OpenConnectionsComboboxPanel;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class ImportConnectionsDBPanel extends JPanel {

    public static final String TITLE = "Import Connections";
    public static final String FRAME_ICON = "ImportConnections16.svg";
    ActionContainer parent;
    OpenConnectionsComboboxPanel connectionsComboboxPanel;
    DatabaseConnectionFactory databaseConnectionFactory;


    public ImportConnectionsDBPanel(ActionContainer dialog) {
        parent = dialog;
        init();
    }

    void init() {
        connectionsComboboxPanel = new OpenConnectionsComboboxPanel();
        BottomButtonPanel bottomButtonPanel = new BottomButtonPanel(parent.isDialog());
        bottomButtonPanel.setOkButtonAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doImport();
            }
        });
        bottomButtonPanel.setOkButtonText("OK");
        bottomButtonPanel.setHelpButtonVisible(false);

        setLayout(new BorderLayout());

        this.add(connectionsComboboxPanel, BorderLayout.NORTH);
        this.add(bottomButtonPanel, BorderLayout.SOUTH);

    }

    void doImport() {
        DatabaseConnection connection = connectionsComboboxPanel.getSelectedConnection();
        DefaultStatementExecutor sender = new DefaultStatementExecutor(connection, true);
        String query = "SELECT PROPS FROM DATABASES";
        try {
            ResultSet rs = sender.getResultSet(query).getResultSet();
            if (rs == null)
                GUIUtilities.displayErrorMessage("Sorry, this connection does not seem to contain connection settings");
            else
                while (rs.next()) {
                    Properties properties = new Properties();
                    String str = rs.getString(1);
                    String[] strs = str.split("\r\n");
                    for (int i = 0; i < strs.length; i++) {
                        int ind = strs[i].indexOf("=");
                        String key = strs[i].substring(0, ind);
                        String prop = strs[i].substring(ind + 1);
                        properties.setProperty(key, prop);
                    }
                    String name = properties.getProperty("Alias");
                    if (!MiscUtils.isNull(name)) {
                        DatabaseConnection databaseConnection = databaseConnectionFactory().create(name);
                        ConnectionsTreePanel connectionsTreePanel = null;
                        if (!connectionNameExists(databaseConnection.getName(), databaseConnection)) {
                            try {
                                connectionsTreePanel = (ConnectionsTreePanel) GUIUtilities.
                                        getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            /***/
                            databaseConnection.setUserName(properties.getProperty("UserName"));
                            databaseConnection.setPasswordStored(true);
                            databaseConnection.setPassword(properties.getProperty("Password"));
                            databaseConnection.setRole(properties.getProperty("Role"));
                            databaseConnection.setCharset(properties.getProperty("Charset"));
                            String s = properties.getProperty("DBName");
                            databaseConnection.setSourceName(s);
                            String host = properties.getProperty("SrvName");
                            if (MiscUtils.isNull(host)) {
                                databaseConnection.setHost("127.0.0.1");
                                databaseConnection.setPort("3050");
                            } else {
                                String server;
                                String port;
                                if (host.contains("/")) {
                                    String[] serverport = host.split("/");
                                    server = serverport[0];
                                    port = serverport[1];
                                } else {
                                    server = host;
                                    port = "3050";
                                }
                                databaseConnection.setHost(server);
                                databaseConnection.setPort(port);
                            }

                            try {
                                connectionsTreePanel.newConnection(databaseConnection);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        /***/
                    }

                }
            parent.finished();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            sender.releaseResources();
        }

    }

    private boolean connectionNameExists(String name, DatabaseConnection databaseConnection) {


        if (databaseConnectionRepository().nameExists(databaseConnection, name)) {

            GUIUtilities.displayErrorMessage("The name [ " + name
                    + " ] entered for this connection already exists");
            return true;
        }

        return false;
    }


    private DatabaseConnectionFactory databaseConnectionFactory() {

        if (databaseConnectionFactory == null) {

            databaseConnectionFactory = new DatabaseConnectionFactoryImpl();
        }

        return databaseConnectionFactory;
    }

    private DatabaseConnectionRepository databaseConnectionRepository() {

        return (DatabaseConnectionRepository) RepositoryCache.load(
                DatabaseConnectionRepository.REPOSITORY_ID);
    }


}


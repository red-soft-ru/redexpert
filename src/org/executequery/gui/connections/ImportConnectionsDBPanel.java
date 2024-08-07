package org.executequery.gui.connections;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseConnectionFactory;
import org.executequery.databasemediators.spi.DatabaseConnectionFactoryImpl;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.ConnectionsComboBox;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ImportConnectionsDBPanel extends JPanel {
    public static final String TITLE = bundleString("title");

    private final ActionContainer parent;
    private final List<String> loadedConnections;
    private final List<String> availableConnections;
    private DatabaseConnectionFactory connectionFactory;

    private JButton applyButton;
    private JButton cancelButton;
    private ConnectionsComboBox connectionsCombo;

    public ImportConnectionsDBPanel(ActionContainer parent) {
        this.parent = parent;
        this.loadedConnections = new ArrayList<>();
        this.availableConnections = new ArrayList<>();

        init();
        arrange();
    }

    private void init() {
        connectionsCombo = WidgetFactory.createConnectionComboBox("connectionsCombo", true);
        applyButton = WidgetFactory.createButton("applyButton", Bundles.get("common.ok.button"), e -> importConnections());
        cancelButton = WidgetFactory.createButton("cancelButton", Bundles.get("common.cancel.button"), e -> finished(false));
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- button panel ---

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorSouthWest();
        buttonPanel.add(new JPanel(), gbh.setMaxWeightX().get());
        buttonPanel.add(applyButton, gbh.nextCol().setMinWeightX().get());
        buttonPanel.add(cancelButton, gbh.nextCol().leftGap(5).get());

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        mainPanel.add(new JLabel(Bundles.get("common.connection")), gbh.topGap(3).setMinWeightX().get());
        mainPanel.add(connectionsCombo, gbh.nextCol().topGap(0).leftGap(5).setMaxWeightX().get());
        mainPanel.add(buttonPanel, gbh.nextRowFirstCol().leftGap(0).topGap(10).spanX().spanY().get());

        // --- base ---

        setLayout(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(5, 5, 5, 5).fillBoth().spanX().spanY();
        add(mainPanel, gbh.get());

        setPreferredSize(new Dimension(300, getPreferredSize().height));
        parent.setResizable(false);
    }

    private void importConnections() {

        DatabaseConnection connection = getSelectedConnection();
        DefaultStatementExecutor sender = new DefaultStatementExecutor(connection, true);

        ConnectionsTreePanel treePanel = null;
        JPanel tabbedComponent = GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY);
        if (tabbedComponent instanceof ConnectionsTreePanel)
            treePanel = (ConnectionsTreePanel) tabbedComponent;

        if (treePanel == null) {
            GUIUtilities.displayWarningMessage(bundleString("connectionsTreeUnavailable"));
            finished(false);
            return;
        }

        try {

            String query = "SELECT PROPS FROM DATABASES";
            ResultSet rs = sender.getResultSet(query).getResultSet();
            if (rs == null) {
                GUIUtilities.displayErrorMessage(bundleString("noConnections"));
                finished(false);
                return;
            }

            while (rs.next()) {

                String rsValue = rs.getString(1);
                if (MiscUtils.isNull(rsValue))
                    continue;

                String[] connectionProperties = rsValue.split("\r\n");
                if (MiscUtils.isEmpty(connectionProperties))
                    continue;

                Properties properties = new Properties();
                for (String propertyString : connectionProperties) {
                    int splitIndex = propertyString.indexOf("=");
                    properties.setProperty(
                            propertyString.substring(0, splitIndex),
                            propertyString.substring(splitIndex + 1)
                    );
                }

                String alias = properties.getProperty("Alias");
                if (MiscUtils.isNull(alias))
                    continue;

                availableConnections.add(alias);
                DatabaseConnection databaseConnection = databaseConnectionFactory().create(alias);
                if (!connectionNameExists(databaseConnection.getName(), databaseConnection)) {

                    String port = "3050";
                    String host = "localhost";
                    String serverString = properties.getProperty("SrvName");
                    if (!MiscUtils.isNull(serverString)) {
                        if (serverString.contains("/")) {
                            String[] serverProperties = serverString.split("/");
                            host = serverProperties[0];
                            port = serverProperties[1];
                        } else {
                            host = serverString;
                            port = "3050";
                        }
                    }

                    databaseConnection.setHost(host);
                    databaseConnection.setPort(port);
                    databaseConnection.setPasswordStored(true);
                    databaseConnection.setRole(properties.getProperty("Role"));
                    databaseConnection.setCharset(properties.getProperty("Charset"));
                    databaseConnection.setSourceName(properties.getProperty("DBName"));
                    databaseConnection.setUserName(properties.getProperty("UserName"));
                    databaseConnection.setPassword(properties.getProperty("Password"));

                    try {
                        treePanel.newConnection(databaseConnection, false);
                        loadedConnections.add(alias);

                    } catch (Exception e) {
                        Log.error(e.getMessage(), e);
                    }
                }
            }
            finished(true);

        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
            GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e, getClass());

        } finally {
            sender.releaseResources();
        }
    }

    private void finished(boolean showMessage) {
        parent.finished();

        if (!showMessage)
            return;

        if (availableConnections.size() == loadedConnections.size()) {
            GUIUtilities.displayInformationMessage(bundleString("loadedSuccessfully"));
            return;
        }

        availableConnections.removeAll(loadedConnections);
        GUIUtilities.displayWarningMessage(bundleString(
                "loadedWithError",
                availableConnections.size(),
                String.join("\n", availableConnections)
        ));
    }

    private DatabaseConnection getSelectedConnection() {
        return connectionsCombo.getSelectedConnection();
    }

    private boolean connectionNameExists(String name, DatabaseConnection databaseConnection) {

        if (databaseConnectionRepository().nameExists(databaseConnection, name, databaseConnection.getFolderId())) {
            GUIUtilities.displayErrorMessage(bundleString("nameExist", name));
            return true;
        }

        return false;
    }

    private DatabaseConnectionFactory databaseConnectionFactory() {
        if (connectionFactory == null)
            connectionFactory = new DatabaseConnectionFactoryImpl();
        return connectionFactory;
    }

    private DatabaseConnectionRepository databaseConnectionRepository() {
        return (DatabaseConnectionRepository) RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID);
    }

    private static String bundleString(String key, Object... args) {
        return Bundles.get(ImportConnectionsDBPanel.class, key, args);
    }

}

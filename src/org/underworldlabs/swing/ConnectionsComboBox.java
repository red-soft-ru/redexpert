package org.underworldlabs.swing;

import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.ConnectionMediator;
import org.executequery.databasemediators.ConnectionType;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultDatabaseConnection;
import org.executequery.datasource.ConnectionManager;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionEvent;
import org.executequery.event.ConnectionListener;
import org.executequery.gui.IconManager;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.util.UserProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Objects;

import static org.executequery.gui.browser.BrowserConstants.*;

public class ConnectionsComboBox extends JComboBox<DatabaseConnection>
        implements ConnectionListener,
        ItemListener {

    public static final String USER_DEFINED_CONNECTION_ID = "USER_DEFINED_CONNECTION";

    private DatabaseConnection lastSelection;
    private final boolean allowAutoConnect;
    private final boolean preventEmbeddedSelection;

    public ConnectionsComboBox(boolean showOnlyActive, boolean allowAutoConnect, boolean embeddedFilter) {
        super();

        this.allowAutoConnect = !showOnlyActive && allowAutoConnect;
        this.preventEmbeddedSelection = !showOnlyActive && embeddedFilter;

        setModel(getModel(showOnlyActive));
        setRenderer(new ConnectionsComboRenderer());
        selectFirstActiveConnection();
        addItemListener(this);

        if (showOnlyActive)
            EventMediator.registerListener(this);
    }

    public DatabaseConnection getSelectedConnection() {
        return (DatabaseConnection) getSelectedItem();
    }

    public void selectCustomConnection(DatabaseConnection connection) {
        connection.setName(Bundles.get("common.user-defined"));
        connection.setId(USER_DEFINED_CONNECTION_ID);
        connection.setConnected(false);

        for (int i = 0; i < getItemCount(); i++) {
            DatabaseConnection dc = getItemAt(i);
            if (Objects.equals(dc.getId(), USER_DEFINED_CONNECTION_ID)) {
                dc.setValues(connection);
                setSelectedItem(dc);
                return;
            }
        }

        super.addItem(connection);
        setSelectedItem(connection);
        updateEnable();
    }

    public void selectCustomConnection() {
        UserProperties properties = UserProperties.getInstance();

        DatabaseConnection dc = new DefaultDatabaseConnection();
        dc.setUserName(properties.getProperty("startup.default.connection.username"));
        dc.setPassword(properties.getProperty("startup.default.connection.password"));
        dc.setCharset(properties.getProperty("startup.default.connection.charset"));
        dc.setHost("localhost");
        dc.setPort("3050");

        selectCustomConnection(dc);
    }

    public boolean hasConnection(DatabaseConnection connection) {

        for (int i = 0; i < getItemCount(); i++)
            if (Objects.equals(getItemAt(i), connection))
                return true;

        return false;
    }

    private static DefaultComboBoxModel<DatabaseConnection> getModel(boolean showOnlyActive) {
        List<DatabaseConnection> connections = showOnlyActive ?
                ConnectionManager.getActiveConnections() :
                ConnectionManager.getAllConnections();

        return new DefaultComboBoxModel<>(connections.toArray(new DatabaseConnection[0]));
    }

    // --- helper methods ---

    private void selectFirstActiveConnection() {
        for (int i = 0; i < getItemCount(); i++) {
            DatabaseConnection dc = getItemAt(i);
            if (!ignoreConnection(dc) && dc.isConnected()) {
                setSelectedItem(dc);
                return;
            }
        }
    }

    private void rollbackSelection() {
        removeItemListener(this);
        setSelectedItem(lastSelection);
        addItemListener(this);
    }

    private boolean ignoreConnection(DatabaseConnection dc) {
        return preventEmbeddedSelection && ConnectionType.isEmbedded(dc);
    }

    private void updateEnable() {
        setEnabled(getItemCount() > 0);
    }

    private static String bundledString(String key, Object... args) {
        return Bundles.get(ConnectionsComboBox.class, key, args);
    }

    // --- ItemListener impl ---

    @Override
    public void itemStateChanged(ItemEvent e) {

        if (e.getStateChange() == ItemEvent.DESELECTED) {
            lastSelection = (DatabaseConnection) e.getItem();
            return;
        }

        filterSelection();
        connectSelection();
    }

    private void filterSelection() {

        DatabaseConnection dc = getSelectedConnection();
        if (!preventEmbeddedSelection || Objects.equals(lastSelection, dc))
            return;

        if (ignoreConnection(dc)) {
            GUIUtilities.displayWarningMessage(bundledString("embeddedNotAllowed"));
            rollbackSelection();
        }
    }

    private void connectSelection() {

        DatabaseConnection dc = getSelectedConnection();
        if (!allowAutoConnect || Objects.equals(lastSelection, dc))
            return;

        if (!dc.isConnected()) {
            try {
                ConnectionMediator.getInstance().connect(dc, true);
            } catch (Exception e) {
                Log.debug(e.getMessage(), e);
            }
        }

        if (!dc.isConnected()) {
            GUIUtilities.displayWarningMessage(bundledString("connectionError"));
            rollbackSelection();
        }
    }

    // --- ConnectionListener impl ---

    @Override
    public void connected(ConnectionEvent connectionEvent) {
        super.addItem(connectionEvent.getDatabaseConnection());
        updateEnable();
    }

    @Override
    public void disconnected(ConnectionEvent connectionEvent) {
        super.removeItem(connectionEvent.getDatabaseConnection());
        updateEnable();
    }

    @Override
    public boolean canHandleEvent(ApplicationEvent event) {
        return event instanceof ConnectionEvent;
    }

    // --- JComboBox impl ---

    @Override
    public void setModel(ComboBoxModel<DatabaseConnection> aModel) {
        super.setModel(aModel);
        updateEnable();
    }

    @Override
    public void addItem(DatabaseConnection item) {
        super.addItem(item);
        updateEnable();
    }

    @Override
    public void insertItemAt(DatabaseConnection item, int index) {
        super.insertItemAt(item, index);
        updateEnable();
    }

    @Override
    public void removeItem(Object anObject) {
        super.removeItem(anObject);
        updateEnable();
    }

    @Override
    public void removeItemAt(int anIndex) {
        super.removeItemAt(anIndex);
        updateEnable();
    }

    @Override
    public void removeAllItems() {
        super.removeAllItems();
        updateEnable();
    }

    // ---

    private class ConnectionsComboRenderer extends DefaultListCellRenderer {

        private final Icon blockedIcon = IconManager.getIcon(REVOKE_IMAGE, "svg", 10, IconManager.IconFolder.BASE);
        private final Icon connectedIcon = IconManager.getIcon(GRANT_IMAGE, "svg", 10, IconManager.IconFolder.BASE);
        private final Icon disconnectedIcon = IconManager.getIcon(FIELD_GRANT_IMAGE, "svg", 10, IconManager.IconFolder.BASE);

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            if (value == null)
                return super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);

            JLabel component = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            component.setIcon(getIcon((DatabaseConnection) value));

            return component;
        }

        private Icon getIcon(DatabaseConnection dc) {
            if (dc.isConnected())
                return ignoreConnection(dc) ? blockedIcon : connectedIcon;
            return disconnectedIcon;
        }

    } // ConnectionsComboRenderer class

}

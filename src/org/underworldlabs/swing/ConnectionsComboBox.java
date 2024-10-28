package org.underworldlabs.swing;

import org.executequery.EventMediator;
import org.executequery.databasemediators.ConnectionType;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.ConnectionManager;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionEvent;
import org.executequery.event.ConnectionListener;
import org.executequery.gui.IconManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.executequery.gui.browser.BrowserConstants.*;

public class ConnectionsComboBox extends JComboBox<DatabaseConnection>
        implements ConnectionListener {

    private final boolean showEmbedded;

    public ConnectionsComboBox(boolean showOnlyActive) {
        this(showOnlyActive, true);
    }

    public ConnectionsComboBox(boolean showOnlyActive, boolean showEmbedded) {
        super();
        this.showEmbedded = showEmbedded;
        setModel(getModel(showOnlyActive, showEmbedded));

        if (showOnlyActive) {
            EventMediator.registerListener(this);

        } else {
            setRenderer(new ConnectionsComboRenderer());
            selectFirstActiveConnection();
        }
    }

    public DatabaseConnection getSelectedConnection() {
        return (DatabaseConnection) getSelectedItem();
    }

    public boolean hasConnection(DatabaseConnection connection) {

        for (int i = 0; i < getItemCount(); i++)
            if (Objects.equals(getItemAt(i), connection))
                return true;

        return false;
    }

    private static DefaultComboBoxModel<DatabaseConnection> getModel(boolean showOnlyActive, boolean showEmbedded) {
        List<DatabaseConnection> connections = showOnlyActive ?
                ConnectionManager.getActiveConnections() :
                ConnectionManager.getAllConnections();

        if (!showEmbedded)
            connections = connections.stream().filter(dc -> !ConnectionType.isEmbedded(dc)).collect(Collectors.toList());

        return new DefaultComboBoxModel<>(connections.toArray(new DatabaseConnection[0]));
    }

    private void selectFirstActiveConnection() {
        for (int i = 0; i < getItemCount(); i++) {
            DatabaseConnection dc = getItemAt(i);
            if (dc.isConnected()) {
                setSelectedItem(dc);
                return;
            }
        }
    }

    private void updateEnable() {
        setEnabled(getItemCount() > 0);
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
        if (showEmbedded || !ConnectionType.isEmbedded(item)) {
            super.addItem(item);
            updateEnable();
        }
    }

    @Override
    public void insertItemAt(DatabaseConnection item, int index) {
        if (showEmbedded || !ConnectionType.isEmbedded(item)) {
            super.insertItemAt(item, index);
            updateEnable();
        }
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

    private static class ConnectionsComboRenderer extends DefaultListCellRenderer {

        private static final Icon CONNECTED_ICON = IconManager.getIcon(GRANT_IMAGE, "svg", 10, IconManager.IconFolder.BASE);
        private static final Icon NOT_CONNECTED_ICON = IconManager.getIcon(FIELD_GRANT_IMAGE, "svg", 10, IconManager.IconFolder.BASE);

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            if (value == null)
                return super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);

            JLabel component = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            component.setIcon(((DatabaseConnection) value).isConnected() ? CONNECTED_ICON : NOT_CONNECTED_ICON);

            return component;
        }

    } // ConnectionsComboRenderer class

}

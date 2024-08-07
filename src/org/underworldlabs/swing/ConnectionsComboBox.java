package org.underworldlabs.swing;

import org.executequery.EventMediator;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.ConnectionManager;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionEvent;
import org.executequery.event.ConnectionListener;

import javax.swing.*;
import java.util.Objects;

public class ConnectionsComboBox extends JComboBox<DatabaseConnection>
        implements ConnectionListener {

    public ConnectionsComboBox(boolean showOnlyActiveConnections) {
        super();

        setModel(new DefaultComboBoxModel<>(showOnlyActiveConnections ?
                ConnectionManager.getActiveConnections() :
                ConnectionManager.getAllConnections()
        ));

        if (showOnlyActiveConnections)
            EventMediator.registerListener(this);
        else
            selectFirstActiveConnection();
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

}

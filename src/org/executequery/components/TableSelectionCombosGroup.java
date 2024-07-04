/*
 * TableSelectionCombosGroup.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.components;

import org.executequery.ApplicationException;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.*;
import org.executequery.databaseobjects.impl.DatabaseObjectFactoryImpl;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.util.ThreadUtils;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.DynamicComboBoxModel;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Combo box group controller containing connection -> table
 * selection combo boxes.
 *
 * @author Takis Diakoumis
 */
public class TableSelectionCombosGroup implements ItemListener {

    private final JComboBox connectionsCombo;

    private final JComboBox tablesCombo;

    private final JComboBox columnsCombo;

    private List<ItemSelectionListener> itemListeners;

    public TableSelectionCombosGroup() {
        this(WidgetFactory.createComboBox("connectionsCombo"), WidgetFactory.createComboBox("tableCombo"), null);
    }

    public TableSelectionCombosGroup(JComboBox connectionsCombo) {
        this(connectionsCombo, null, null);
    }

    public TableSelectionCombosGroup(JComboBox connectionsCombo, JComboBox tablesCombo, JComboBox columnsCombo) {

        super();

        this.connectionsCombo = connectionsCombo;
        this.tablesCombo = tablesCombo;
        this.columnsCombo = columnsCombo;

        init();

        connectionSelected();
    }

    private void init() {

        initConnectionsCombo(connectionsCombo);

        if (tablesCombo != null)
            initTablesCombo(tablesCombo);

        if (columnsCombo != null)
            initTablesCombo(columnsCombo);
    }

    public void connectionOpened(DatabaseConnection databaseConnection) {

        DynamicComboBoxModel model = connectionComboModel();
        model.addElement(databaseObjectFactory().createDatabaseHost(databaseConnection));
    }

    public void connectionClosed(DatabaseConnection databaseConnection) {

        DynamicComboBoxModel model = connectionComboModel();

        DatabaseHost host = null;
        DatabaseHost selectedHost = getSelectedHost();

        if (selectedHost.getDatabaseConnection() == databaseConnection) {

            host = selectedHost;

        } else {

            host = hostForConnection(databaseConnection);
        }

        if (host != null) {

            model.removeElement(host);
        }

    }

    private DynamicComboBoxModel connectionComboModel() {

        return (DynamicComboBoxModel) connectionsCombo.getModel();
    }

    private DatabaseHost hostForConnection(DatabaseConnection databaseConnection) {

        ComboBoxModel model = connectionComboModel();

        for (int i = 0, n = model.getSize(); i < n; i++) {

            DatabaseHost host = (DatabaseHost) model.getElementAt(i);

            if (host.getDatabaseConnection() == databaseConnection) {

                return host;
            }

        }

        return null;
    }

    public void addItemSelectionListener(ItemSelectionListener listener) {

        if (itemListeners == null) {

            itemListeners = new ArrayList<ItemSelectionListener>();
        }

        itemListeners.add(listener);
    }

    public DatabaseHost getSelectedHost() {

        return (DatabaseHost) connectionsCombo.getSelectedItem();
    }

    public void setSelectedDatabaseHost(DatabaseHost databaseHost) {

        if (connectionsCombo.getSelectedItem() == databaseHost) {

            return;
        }

        try {

            connectionSelectionPending = true;

            if (comboContains(connectionsCombo, databaseHost)) {

                connectionsCombo.setSelectedItem(databaseHost);

            } else {

                ComboBoxModel model = connectionComboModel();

                String connectionId = databaseHost.getDatabaseConnection().getId();

                for (int i = 0, n = model.getSize(); i < n; i++) {

                    DatabaseHost host = (DatabaseHost) model.getElementAt(i);

                    if (connectionId.equals(host.getDatabaseConnection().getId())) {

                        connectionsCombo.setSelectedItem(host);
                        break;
                    }

                }

            }

            connectionSelected();

        } finally {

            connectionSelectionPending = false;
        }

    }

    private boolean comboContains(JComboBox comboBox, Object item) {

        return ((DynamicComboBoxModel) comboBox.getModel()).contains(item);
    }

    private boolean connectionSelectionPending;
    private boolean tableSelectionPending;

    public void setSelectedDatabaseTable(DatabaseTable databaseTable) {

        if (tablesCombo.getSelectedItem() == databaseTable) {

            return;
        }

        if (comboContains(tablesCombo, databaseTable)) {

            tablesCombo.setSelectedItem(databaseTable);

        } else {

            setSelectedDatabaseHost(databaseTable.getDatabaseSource().getHost());

            try {

                tableSelectionPending = true;

                ComboBoxModel model = tablesCombo.getModel();

                String tableName = databaseTable.getName();

                for (int i = 0, n = model.getSize(); i < n; i++) {

                    DatabaseTable table = (DatabaseTable) model.getElementAt(i);

                    if (tableName.equals(table.getName())) {

                        tablesCombo.setSelectedItem(table);
                        break;
                    }

                }

            } finally {

                tableSelectionPending = false;
            }

        }

    }

    public DatabaseTable getSelectedTable() {

        if (tablesCombo.getSelectedItem() != null) {

            return (DatabaseTable) tablesCombo.getSelectedItem();
        }

        return null;
    }

    public DatabaseColumn getSelectedColumn() {

        if (columnsCombo.getSelectedItem() != null) {

            return (DatabaseColumn) columnsCombo.getSelectedItem();
        }

        return null;
    }

    public void itemStateChanged(final ItemEvent e) {

        if (selectionPending() || (e.getStateChange() == ItemEvent.DESELECTED)) {

            return;
        }

        final Object source = e.getSource();

        ThreadUtils.startWorker(new Runnable() {
            public void run() {

                try {

                    fireItemStateChanging(e);

                    if (source == connectionsCombo) {

                        connectionSelected();

                    } else if (source == tablesCombo) {

                        tableSelected();
                    }

                } finally {

                    fireItemStateChanged(e);
                }

            }
        });

    }

    private boolean selectionPending() {
        return connectionSelectionPending || tableSelectionPending;
    }

    private synchronized void fireItemStateChanged(ItemEvent e) {

        if (hasItemListeners()) {

            for (ItemSelectionListener listener : itemListeners) {

                listener.itemStateChanged(e);
            }

        }

    }

    private synchronized void fireItemStateChanging(ItemEvent e) {

        if (hasItemListeners()) {

            for (ItemSelectionListener listener : itemListeners) {

                listener.itemStateChanging(e);
            }

        }

    }

    private boolean hasItemListeners() {

        return (itemListeners != null && !itemListeners.isEmpty());
    }

    private void connectionSelected() {
        clearCombos();
    }

    private void tableSelected() {

        if (columnsCombo != null) {

            try {

                DatabaseTable table = getSelectedTable();

                if (table != null) {

                    List<DatabaseColumn> columns = table.getColumns();

                    populateModelForCombo(columnsCombo, columns);

                } else {

                    populateModelForCombo(columnsCombo, null);
                }

            } catch (DataSourceException e) {

                handleDataSourceException(e);
            }

        }

    }

    private void populateModelForCombo(JComboBox comboBox, List<?> list) {

        if (comboBox == null) {

            return;
        }

        DynamicComboBoxModel model = (DynamicComboBoxModel) comboBox.getModel();

        if (list != null && !list.isEmpty()) {

            try {

                comboBox.removeItemListener(this);
                model.setElements(list);

            } finally {

                comboBox.addItemListener(this);
            }

            comboBox.setEnabled(true);

        } else {

            try {

                comboBox.removeItemListener(this);
                model.removeAllElements();

            } finally {

                comboBox.addItemListener(this);
            }

            comboBox.setEnabled(false);
        }

    }

    private void clearCombos() {
        if (tablesCombo != null)
            populateModelForCombo(tablesCombo, null);
    }

    private void initTablesCombo(JComboBox comboBox) {

        comboBox.setModel(new DynamicComboBoxModel());
        initComboBox(comboBox);
    }

    private void initConnectionsCombo(JComboBox comboBox) {

        DatabaseObjectFactory factory = databaseObjectFactory();

        Vector<DatabaseHost> hosts = new Vector<DatabaseHost>();

        for (DatabaseConnection connection : activeConnections()) {

            hosts.add(factory.createDatabaseHost(connection));
        }

        ComboBoxModel model = new DynamicComboBoxModel(hosts);

        comboBox.setModel(model);
        initComboBox(comboBox);
        comboBox.setEnabled(true);
    }

    private void initComboBox(JComboBox comboBox) {

        comboBox.addItemListener(this);
        comboBox.setEnabled(false);
    }

    private DatabaseObjectFactory databaseObjectFactory() {

        return new DatabaseObjectFactoryImpl();
    }

    private Vector<DatabaseConnection> activeConnections() {

        return ConnectionManager.getActiveConnections();
    }

    private void handleDataSourceException(DataSourceException e) {

        Log.error(bundleString("error.selection-object"), e);

        throw new ApplicationException(e);
    }

    public void close() {

        ComboBoxModel model = connectionComboModel();

        for (int i = 0, n = model.getSize(); i < n; i++) {

            DatabaseHost host = (DatabaseHost) model.getElementAt(i);
            host.close();
        }
    }

    public JComboBox getConnectionsCombo() {
        return connectionsCombo;
    }

    public JComboBox getTablesCombo() {
        return tablesCombo;
    }

    public String bundleString(String key) {
        return Bundles.get(getClass(), key);
    }

}

/*
 * TableSelectionCombosGroup.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

import org.executequery.ApplicationException;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseCatalog;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseObjectFactory;
import org.executequery.databaseobjects.DatabaseSchema;
import org.executequery.databaseobjects.DatabaseSource;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DatabaseObjectFactoryImpl;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.WidgetFactory;
import org.executequery.log.Log;
import org.executequery.util.ThreadUtils;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.DynamicComboBoxModel;

/** 
 * Combo box group controller containing connection -> catalog/schema -> table 
 * selection combo boxes.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class TableSelectionCombosGroup implements ItemListener {

    private final JComboBox connectionsCombo;
    
    private final JComboBox schemasCombo;
    
    private final JComboBox tablesCombo;

    private final JComboBox columnsCombo;
    
    private List<ItemSelectionListener> itemListeners;

    public TableSelectionCombosGroup() {
        
        this(WidgetFactory.createComboBox(), WidgetFactory.createComboBox(), WidgetFactory.createComboBox(), null);
    }
    
    public TableSelectionCombosGroup(JComboBox connectionsCombo) {

        this(connectionsCombo, null, null);
    }

    public TableSelectionCombosGroup(JComboBox connectionsCombo,
            JComboBox schemasCombo) {

        this(connectionsCombo, schemasCombo, null);
    }

    public TableSelectionCombosGroup(JComboBox connectionsCombo,
            JComboBox schemasCombo, JComboBox tablesCombo) {

        this(connectionsCombo, schemasCombo, tablesCombo, null);        
    }

    public TableSelectionCombosGroup(JComboBox connectionsCombo,
            JComboBox schemasCombo, JComboBox tablesCombo, JComboBox columnsCombo) {

        super();

        this.connectionsCombo = connectionsCombo;
        this.schemasCombo = schemasCombo;
        this.tablesCombo = tablesCombo;
        this.columnsCombo = columnsCombo;
        
        init();
        
        connectionSelected();
    }

    private void init() {

        initConnectionsCombo(connectionsCombo);

        if (schemasCombo != null) {
            
            initSchemasCombo(schemasCombo);
        }
        
        if (tablesCombo != null) {

            initTablesCombo(tablesCombo);
        }

        if (columnsCombo != null) {

            initTablesCombo(columnsCombo);
        }

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
    private boolean schemaSelectionPending;
    private boolean tableSelectionPending;
    
    public void setSelectedDatabaseTable(DatabaseTable databaseTable) {
        
        if (tablesCombo.getSelectedItem() == databaseTable) {
            
            return;
        }

        if (comboContains(tablesCombo, databaseTable)) {
            
            tablesCombo.setSelectedItem(databaseTable);
            
        } else {
            
            setSelectedDatabaseSource(databaseTable.getDatabaseSource());
            
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
    
    public void setSelectedDatabaseSource(DatabaseSource databaseSource) {
        
        if (schemasCombo.getSelectedItem() == databaseSource) {
            
            return;
        }
        
        try {
        
            schemaSelectionPending = true;
            
            if (comboContains(schemasCombo, databaseSource)) {
                
                schemasCombo.setSelectedItem(databaseSource);
    
            } else {
    
                setSelectedDatabaseHost(databaseSource.getHost());
                
                ComboBoxModel model = schemasCombo.getModel();
                
                String schemaName = databaseSource.getName();
                
                for (int i = 0, n = model.getSize(); i < n; i++) {
    
                    DatabaseSource source = (DatabaseSource) model.getElementAt(i);
                    
                    if (schemaName.equals(source.getName())) {
                        
                        schemasCombo.setSelectedItem(source);
                        break;
                    }
                    
                }
                
            }
            
            schemaSelected();
        
        } finally {
            
            schemaSelectionPending = false;
        }

    }
    
    public DatabaseSource getSelectedSource() {

        if (schemasCombo != null && schemasCombo.getSelectedItem() != null) {

            return (DatabaseSource) schemasCombo.getSelectedItem();
        }
        
        return null;
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

                    } else if (source == schemasCombo) {
                        
                        schemaSelected();

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

        return (connectionSelectionPending
            || schemaSelectionPending || tableSelectionPending);
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

        try {
        
            DatabaseHost host = getSelectedHost();

            if (host != null && schemasCombo != null) {

                List<DatabaseSchema> schemas = host.getSchemas();

                if (schemas != null && schemas.size() > 0) {

                    populateModelForCombo(schemasCombo, schemas);

                } else {

                    List<DatabaseCatalog> catalogs = host.getCatalogs();
                    
                    if (catalogs != null && catalogs.size() > 0) {
                        
                        populateModelForCombo(schemasCombo, catalogs);

                    } else {
                        
                        clearCombos();
                    }

                }
                
                schemaSelected();

            } else {
                
                clearCombos();
            }

        } catch (DataSourceException e) {

            handleDataSourceException(e);
        }
    }

    private void schemaSelected() {

        try {
            
            DatabaseSource schema = getSelectedSource();

            if (schema != null) {

                List<NamedObject> tables = tablesForSchema(schema);

                populateModelForCombo(tablesCombo, tables);

            } else {

                populateModelForCombo(tablesCombo, null);
            }
            
        } catch (DataSourceException e) {
            
            handleDataSourceException(e);
        }

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

    public List<NamedObject> tablesForSchema(DatabaseSource schema) {
    
        DatabaseMetaTag databaseMetaTag = schema.getDatabaseMetaTag("TABLE");
        
        if (databaseMetaTag != null) {
            
            return databaseMetaTag.getObjects();
        }
        
        return null;
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

        if (schemasCombo != null) {

            populateModelForCombo(schemasCombo, null);
        }
        
        if (tablesCombo != null) {
        
            populateModelForCombo(tablesCombo, null);
        }

    }

    private void initSchemasCombo(JComboBox comboBox) {

        comboBox.setModel(new DynamicComboBoxModel());
        initComboBox(comboBox);
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
        
        Log.error("Error during database object selection", e);
        
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

    public JComboBox getSchemasCombo() {
        return schemasCombo;
    }

    public JComboBox getTablesCombo() {
        return tablesCombo;
    }

    public JComboBox getColumnsCombo() {
        return columnsCombo;
    }

    public void setSchemaSelectionUpdatesEnabled(boolean enable) {

        if (schemasCombo != null) {
            
            if (enable) {

                schemasCombo.addItemListener(this);
//                schemaSelected();

            } else {

                schemasCombo.removeItemListener(this);
                populateModelForCombo(tablesCombo, null);
            }
            
        }
        
    }

}







/*
 * TableSelectionPanel.java
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.executequery.ApplicationException;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseCatalog;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseObjectFactory;
import org.executequery.databaseobjects.DatabaseSchema;
import org.executequery.databaseobjects.DatabaseSource;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DatabaseObjectFactoryImpl;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.WidgetFactory;
import org.executequery.log.Log;
import org.executequery.util.StringBundle;
import org.executequery.util.SystemResources;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.ActionPanel;
import org.underworldlabs.swing.DynamicComboBoxModel;

/** 
 * Panel containing connection -> catalog/schema -> table
 * selection combo boxes.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class TableSelectionPanel extends ActionPanel
                                 implements ItemListener {

    private JComboBox connectionsCombo;
    
    private JComboBox schemasCombo;
    
    private JComboBox tablesCombo;

    private StringBundle bundle;
    
    public TableSelectionPanel() {
        
        super(new GridBagLayout());

        init();
        
        connectionSelected();
    }

    private void init() {

        connectionsCombo = createConnectionsCombo();
        schemasCombo = createSchemasCombo();
        tablesCombo = createTablesCombo();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets.right = 5;
        gbc.gridy = 0;
        gbc.gridx = 0;
        add(createLabel("connection"), gbc);
        gbc.gridx = 1;
        gbc.insets.right = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(connectionsCombo, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.insets.top = 5;
        gbc.insets.right = 0;
        gbc.fill = GridBagConstraints.NONE;
        add(createLabel("schema"), gbc);
        gbc.gridx = 1;
        gbc.insets.right = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(schemasCombo, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weighty = 1.0;
        gbc.insets.right = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        add(createLabel("table"), gbc);
        gbc.gridx = 1;
        gbc.insets.right = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(tablesCombo, gbc);
        
    }

    public DatabaseHost getSelectedHost() {

        return (DatabaseHost) connectionsCombo.getSelectedItem();
    }

    public DatabaseSource getSelectedSource() {

        return (DatabaseSource) schemasCombo.getSelectedItem();
    }

    public DatabaseTable getSelectedTable() {

        return (DatabaseTable) tablesCombo.getSelectedItem();
    }

    public void itemStateChanged(ItemEvent e) {
        
        Object source = e.getSource();

        if (source == connectionsCombo) {
            
            connectionSelected();

        } else if (source == schemasCombo) {
            
            schemaSelected();
            
        } else if (source == tablesCombo) {
            
            tableSelected();
        }
        
    }

    private void connectionSelected() {

        try {
        
            DatabaseHost host = getSelectedHost();

            if (host != null) {

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

                List<NamedObject> tables = schema.getTables();
    
                populateModelForCombo(tablesCombo, tables);

            } else {

                populateModelForCombo(tablesCombo, null);
            }
            
        } catch (DataSourceException e) {
            
            handleDataSourceException(e);
        }

    }

    private void tableSelected() {

        
    }

    private void populateModelForCombo(JComboBox comboBox, List<?> list) {

        DynamicComboBoxModel model = (DynamicComboBoxModel) comboBox.getModel();

        if (list != null && list.size() > 0) {
            
            model.setElements(list);
            comboBox.setEnabled(true);

        } else {
            
            model.removeAllElements();
            comboBox.setEnabled(false);
        }
        
    }

    private void clearCombos() {
        
        populateModelForCombo(schemasCombo, null);
        
        populateModelForCombo(tablesCombo, null);
    }
    
    private JLabel createLabel(String key) {

        return new JLabel(bundleString(key));
    }

    private JComboBox createSchemasCombo() {

        return comboBoxForModel(new DynamicComboBoxModel());
    }

    private JComboBox createTablesCombo() {

        return comboBoxForModel(new DynamicComboBoxModel());
    }

    private JComboBox createConnectionsCombo() {

        DatabaseObjectFactory factory = databaseObjectFactory();
        
        Vector<DatabaseHost> hosts = new Vector<DatabaseHost>();
        
        for (DatabaseConnection connection : activeConnections()) {

            hosts.add(factory.createDatabaseHost(connection));
        }

        ComboBoxModel model = new DynamicComboBoxModel(hosts);
        
        JComboBox comboBox = comboBoxForModel(model);
        comboBox.setEnabled(true);
        
        return comboBox;
    }

    private JComboBox comboBoxForModel(ComboBoxModel model) {

        JComboBox comboBox = WidgetFactory.createComboBox(model);
        comboBox.addItemListener(this);
        comboBox.setEnabled(false);

        return comboBox;
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

    private StringBundle bundle() {
        
        if (bundle == null) {
        
            bundle = SystemResources.loadBundle(TableSelectionPanel.class);
        }

        return bundle;
    }

    private String bundleString(String key) {

        return bundle().getString("TableSelectionPanel." + key);
    }

}







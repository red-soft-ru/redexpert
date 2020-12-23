/*
 * ErdSelectionPanel.java
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

package org.executequery.gui.erd;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.MetaDataValues;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.ListSelectionPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

/**
 * @author Takis Diakoumis
 */
@SuppressWarnings({"rawtypes"})
public class ErdSelectionPanel extends JPanel
        implements ItemListener {
    //ActionListener {

    /**
     * The schema combo box
     */
    protected JComboBox schemaCombo;

    /**
     * the schema combo box model
     */
    protected DynamicComboBoxModel schemaModel;

    /**
     * The connection combo selection
     */
    protected JComboBox connectionsCombo;

    /**
     * the schema combo box model
     */
    protected DynamicComboBoxModel connectionsModel;

    /**
     * The add/remove table selections panel
     */
    private ListSelectionPanel listPanel;

    /**
     * the database connection props object
     */
    private DatabaseConnection databaseConnection;

    private boolean useCatalogs;

    private MetaDataValues metaData;

    public ErdSelectionPanel() {
        this(null);
    }

    public ErdSelectionPanel(DatabaseConnection databaseConnection) {
        super(new GridBagLayout());
        this.databaseConnection = databaseConnection;

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void jbInit() throws Exception {

        listPanel = new ListSelectionPanel(bundleString("availableTables"), bundleString("selected Tables"));
        metaData = new MetaDataValues(true);

        // combo boxes
        Vector connections = ConnectionManager.getActiveConnections();
        connectionsModel = new DynamicComboBoxModel(connections);
        connectionsCombo = WidgetFactory.createComboBox(connectionsModel);
        connectionsCombo.addItemListener(this);

        schemaModel = new DynamicComboBoxModel();
        schemaCombo = WidgetFactory.createComboBox(schemaModel);
        schemaCombo.addItemListener(this);

        setBorder(BorderFactory.createEtchedBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(13, 10, 0, 10);
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(new JLabel(Bundles.getCommon("connection")), gbc);
        gbc.insets.top = 10;
        gbc.insets.left = 0;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(connectionsCombo, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.insets.top = 10;
        gbc.insets.left = 10;
        gbc.insets.bottom = 10;
        gbc.insets.right = 10;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(listPanel, gbc);

        // check initial values for possible value inits
        if (connections == null || connections.isEmpty()) {
            schemaCombo.setEnabled(false);
            connectionsCombo.setEnabled(false);
        } else {
            DatabaseConnection connection =
                    (DatabaseConnection) connections.elementAt(0);
            metaData.setDatabaseConnection(connection);
            Vector schemas = metaData.getHostedSchemasVector();
            if (schemas == null || schemas.isEmpty()) {
                useCatalogs = true;
                schemas = metaData.getHostedCatalogsVector();
            }
            schemaModel.setElements(schemas);
            schemaChanged();
        }

        setPreferredSize(new Dimension(700, 380));
    }

    public void setInProcess(boolean inProcess) {
    }

    /**
     * Invoked when an item has been selected or deselected by the user.
     */
    public void itemStateChanged(ItemEvent event) {
        // interested in selections only
        if (event.getStateChange() == ItemEvent.DESELECTED) {
            return;
        }
        listPanel.clear();

        final Object source = event.getSource();
        GUIUtils.startWorker(new Runnable() {
            public void run() {
                try {
                    setInProcess(true);
                    if (source == connectionsCombo) {
                        connectionChanged();
                    } else if (source == schemaCombo) {
                        schemaChanged();
                    }
                } finally {
                    setInProcess(false);
                }
            }
        });
        
        
        /*
        if (source == connectionsCombo) {
            try {
                // retrieve connection selection
                DatabaseConnection connection = 
                        (DatabaseConnection)connectionsCombo.getSelectedItem();
                // reset meta data
                metaData.setDatabaseConnection(connection);
                // reset schema values
                Vector schemas = metaData.getHostedSchemasVector();
                if (schemas == null || schemas.isEmpty()) {
                    useCatalogs = true;
                    schemas = metaData.getHostedCatalogsVector();
                } else {
                    useCatalogs = false;
                }
                schemaCombo.removeItemListener(this);
                schemaModel.setElements(schemas);
                schemaCombo.setEnabled(true);
                //schemaChanged();
            }
            catch (DataSourceException e) {
                GUIUtilities.displayExceptionErrorDialog(
                        "Error retrieving the catalog/schema names for the " +
                        "current connection.\n\nThe system returned:\n" + 
                        e.getExtendedMessage(), e);
            }
            finally {
                schemaCombo.addItemListener(this);
            }
        }
        else if (source == schemaCombo) {
            schemaChanged();
        }
         */
    }

    private void connectionChanged() {
        try {
            // retrieve connection selection
            DatabaseConnection connection =
                    (DatabaseConnection) connectionsCombo.getSelectedItem();
            // reset meta data
            metaData.setDatabaseConnection(connection);
            // reset schema values
            Vector schemas = metaData.getHostedSchemasVector();
            if (schemas == null || schemas.isEmpty()) {
                useCatalogs = true;
                schemas = metaData.getHostedCatalogsVector();
            } else {
                useCatalogs = false;
            }
            populateSchemaValues(schemas);
        } catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(
                    "Error retrieving the catalog/schema names for the " +
                            "current connection.\n\nThe system returned:\n" +
                            e.getExtendedMessage(), e);
        }
    }

    private void populateSchemaValues(final Vector<?> schemas) {
        GUIUtils.invokeAndWait(new Runnable() {
            public void run() {
                schemaModel.setElements(schemas);
                schemaCombo.setEnabled(true);
                if (schemas != null && schemas.size() > 0) {
                    schemaCombo.setSelectedIndex(0);
                }
            }
        });
    }

    private void schemaChanged() {
        try {
            String catalogName = null;
            String schemaName = null;
            Object value = schemaCombo.getSelectedItem();

            if (value != null) {
                if (useCatalogs) {
                    catalogName = value.toString();
                } else {
                    schemaName = value.toString();
                }
            }

            String[] tables = metaData.getTables(catalogName, schemaName, "TABLE");
            populateTableValues(tables);
        } catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(
                    "Error retrieving the table names for the selected " +
                            "catalog/schema.\n\nThe system returned:\n" +
                            e.getExtendedMessage(), e);
            populateTableValues(new String[0]);
        }
    }

    private void populateTableValues(final String[] tables) {
        GUIUtils.invokeAndWait(new Runnable() {
            public void run() {
                listPanel.createAvailableList(tables);
            }
        });
    }

    public String getSchema() {

        Object schema = schemaCombo.getSelectedItem();
        if (schema != null) {

            return schema.toString();
        }

        return null;
    }

    /**
     * Releases database resources before closing.
     */
    public void cleanup() {
        metaData.closeConnection();
    }

    public Vector getSelectedValues() {
        return listPanel.getSelectedValues();
    }

    public boolean hasSelections() {
        return listPanel.hasSelections();
    }

    public DatabaseConnection getDatabaseConnection() {
        if (databaseConnection == null) {
            return (DatabaseConnection) connectionsCombo.getSelectedItem();
        }
        return databaseConnection;
    }

    private String bundleString(String key) {
        return Bundles.get(ErdSelectionPanel.class, key);
    }

}












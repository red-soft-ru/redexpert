/*
 * BaseScriptGeneratorPanel.java
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

package org.executequery.gui.scriptgenerators;

import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.MetaDataValues;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.DefaultPanelButton;
import org.executequery.gui.WidgetFactory;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.ComponentTitledPanel;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.FileSelector;
import org.underworldlabs.swing.ListSelectionPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

/**
 * @author Takis Diakoumis
 * @deprecated
 */
public class BaseScriptGeneratorPanel extends JPanel
        implements ActionListener,
        ItemListener {

    public static final String TITLE = "Generate SQL Scripts";
    public static final String FRAME_ICON = "CreateScripts16.svg";

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

    protected boolean useCatalogs;
    protected ListSelectionPanel listPanel;

    protected MetaDataValues metaData;

    protected JButton browseButton;
    protected JTextField pathField;

    protected JCheckBox constraintsCheck;
    protected JCheckBox consAsAlterCheck;
    protected JCheckBox consInCreateCheck;

    protected Vector tables;

    /**
     * the database connection object
     */
    protected DatabaseConnection databaseConnection;

    public BaseScriptGeneratorPanel() {
        super(new BorderLayout());

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public BaseScriptGeneratorPanel(Vector tables) {
        super(new BorderLayout());

        this.tables = tables;

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void jbInit() throws Exception {
        metaData = new MetaDataValues(true);

        listPanel = new ListSelectionPanel("Available Tables:",
                "Selected Tables:");

        // combo boxes
        connectionsCombo = WidgetFactory.createComboBox("connectionsCombo");
        schemaCombo = WidgetFactory.createComboBox("schemaCombo");

        if (tables != null) {
            schemaCombo.setEnabled(false);
            connectionsCombo.setEnabled(false);
            listPanel.createAvailableList(tables);
        } else {
            // retrieve selection lists
            Vector connections = ConnectionManager.getActiveConnections();
            connectionsModel = new DynamicComboBoxModel(connections);
            connectionsCombo.setModel(connectionsModel);
            connectionsCombo.addItemListener(this);

            schemaModel = new DynamicComboBoxModel();
            schemaCombo.setModel(schemaModel);
            schemaCombo.addItemListener(this);

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
                schemaCombo.setSelectedIndex(0);
                createTablesList();
            }
        }

        pathField = WidgetFactory.createTextField("pathField");
        browseButton = new DefaultPanelButton("Browse");
        browseButton.setMnemonic('B');

        constraintsCheck = new JCheckBox("Include constraints");
        consAsAlterCheck = new JCheckBox("As ALTER TABLE statements", true);
        consInCreateCheck = new JCheckBox("Within CREATE TABLE statements");

        constraintsCheck.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                enableConstraintChecks(e.getStateChange() == ItemEvent.SELECTED);
            }
        });

        ButtonGroup bg = new ButtonGroup();
        bg.add(consAsAlterCheck);
        bg.add(consInCreateCheck);

        consInCreateCheck.setEnabled(false);
        consAsAlterCheck.setEnabled(false);

        browseButton.addActionListener(this);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEtchedBorder());

        ComponentTitledPanel optionsPanel = new ComponentTitledPanel(constraintsCheck);
        JPanel _panel = optionsPanel.getContentPane();
        _panel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 5));
        _panel.add(consAsAlterCheck);
        _panel.add(consInCreateCheck);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx++;
        gbc.gridy++;
        gbc.insets = new Insets(7, 5, 0, 5);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        mainPanel.add(new JLabel("Connection:"), gbc);
        gbc.gridx = 1;
        gbc.insets.left = 0;
        gbc.insets.top = 5;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(connectionsCombo, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.insets.left = 5;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel(useCatalogs ? "Catalog:" : "Schema:"), gbc);
        gbc.insets.top = 3;
        gbc.insets.left = 0;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(schemaCombo, gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.gridy++;
        gbc.insets.top = 10;
        gbc.insets.left = 5;
        gbc.insets.bottom = 10;
        gbc.insets.right = 5;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(listPanel, gbc);
        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.insets.top = 10;
        gbc.weightx = 0;
        gbc.insets.right = 0;
        gbc.insets.left = 5;
        gbc.insets.bottom = 5;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Save Path:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(pathField, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.insets.top = 3;
        gbc.insets.right = 5;
        mainPanel.add(browseButton, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.insets.top = 0;
        gbc.insets.bottom = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(optionsPanel, gbc);

        JPanel base = new JPanel(new GridBagLayout());
        base.add(mainPanel, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.SOUTHEAST,
                GridBagConstraints.BOTH,
                new Insets(5, 5, 0, 5), 0, 0));

        add(base, BorderLayout.CENTER);
    }

    /**
     * Invoked when an item has been selected or deselected by the user.
     * The code written for this method performs the operations
     * that need to occur when an item is selected (or deselected).
     */
    public void itemStateChanged(ItemEvent event) {
        // interested in selections only
        if (event.getStateChange() == ItemEvent.DESELECTED) {
            return;
        }

        Object source = event.getSource();
        if (source == connectionsCombo) {
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
                }
                schemaModel.setElements(schemas);
                schemaCombo.setSelectedIndex(0);
                schemaCombo.setEnabled(true);
                createTablesList();
            } catch (DataSourceException e) {
                GUIUtilities.displayExceptionErrorDialog(
                        "Error retrieving the catalog/schema names for " +
                                "the current connection.\n\nThe system returned:\n" +
                                e.getExtendedMessage(), e);
            }
        } else if (source == schemaCombo) {
            createTablesList();
        }
    }

    private void createTablesList() {
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
            listPanel.createAvailableList(metaData.getTables(
                    catalogName,
                    schemaName,
                    "TABLE"));
        } catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(
                    "Error retrieving the table names for the " +
                            "selected catalog/schema.\n\nThe system returned:\n" +
                            e.getExtendedMessage(), e);
        }
    }

    public void actionPerformed(ActionEvent e) {
        browseButton_actionPerformed();
    }

    private void enableConstraintChecks(boolean enable) {
        consAsAlterCheck.setEnabled(enable);
        consInCreateCheck.setEnabled(enable);
    }

    protected boolean hasRequiredFields() {

        if (!listPanel.hasSelections()) {
            GUIUtilities.displayErrorMessage("You must select at least one table.");
            return false;
        }

        if (pathField.getText().length() == 0) {
            GUIUtilities.displayErrorMessage("You must select a file.");
            return false;
        }

        return true;

    }

    private void browseButton_actionPerformed() {
        FileSelector textFiles = new FileSelector(new String[]{"txt"}, "Text files");
        FileSelector sqlFiles = new FileSelector(new String[]{"sql"}, "SQL files");

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.addChoosableFileFilter(textFiles);
        fileChooser.addChoosableFileFilter(sqlFiles);

        fileChooser.setDialogTitle("Select File...");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);

        int result = fileChooser.showDialog(GUIUtilities.getInFocusDialogOrWindow(), "Select");

        if (result == JFileChooser.CANCEL_OPTION) {
            return;
        }

        pathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
    }

}
















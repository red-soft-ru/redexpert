/*
 * ConnectionPanel.java
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

package org.executequery.gui.browser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.commons.lang.StringUtils;
import org.executequery.Constants;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.components.TextFieldPanel;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.datasource.ConnectionManager;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionRepositoryEvent;
import org.executequery.event.DatabaseDriverEvent;
import org.executequery.event.DatabaseDriverListener;
import org.executequery.event.DefaultConnectionRepositoryEvent;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.FormPanelButton;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.drivers.DialogDriverPanel;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.DatabaseDriverRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.DefaultFieldLabel;
import org.underworldlabs.swing.DefaultTextField;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.LinkButton;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.actions.ActionUtilities;
import org.underworldlabs.util.MiscUtils;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ConnectionPanel extends AbstractConnectionPanel 
                             implements DatabaseDriverListener,
                                        ChangeListener {
    
    // -------------------------------
    // text fields and combos
    
    private static final String CONNECT_ACTION_COMMAND = "connect";

    private JComboBox driverCombo;
    private JCheckBox encryptPwdCheck;
    private JCheckBox savePwdCheck;

    private JTextField nameField;
    private JTextField userField;
    private JPasswordField passwordField;
    private JTextField hostField;
    private NumberTextField portField;
    private JTextField sourceField;
    private JTextField urlField;

    private JLabel statusLabel;
    
    private JComboBox txCombo;
    private JButton txApplyButton;
    
    // -------------------------------

    /** table model for jdbc properties key/values */
    private JdbcPropertiesTableModel model;
    
    /** connect button */
    private JButton connectButton;

    /** disconnect button */
    private JButton disconnectButton;

    /** the saved jdbc drivers */
    private List<DatabaseDriver> jdbcDrivers;

    /** any advanced property keys/values */
    private String[][] advancedProperties;
    
    /** the tab basic/advanced tab pane */
    private JTabbedPane tabPane;
    
    /** the connection properties displayed */
    private DatabaseConnection databaseConnection;

    /** the host object representing this connection */
    private DatabaseHost host;
    
    /** the browser's control object */
    private BrowserController controller;

    private SSHTunnelConnectionPanel sshTunnelConnectionPanel;
    
    /** Creates a new instance of ConnectionPanel */
    public ConnectionPanel(BrowserController controller) {
        super(new BorderLayout());
        this.controller = controller;
        init();
    }
    
    private void init() {

        // ---------------------------------
        // create the basic props panel
        
        // initialise the fields
        nameField = createTextField();
        passwordField = createPasswordField();
        hostField = createTextField();
        portField = createNumberTextField();
        sourceField = createMatchedWidthTextField();
        userField = createTextField();
        urlField = createMatchedWidthTextField();

        nameField.addFocusListener(new ConnectionNameFieldListener(this));
        
        savePwdCheck = ActionUtilities.createCheckBox("Store Password", "setStorePassword");
        encryptPwdCheck = ActionUtilities.createCheckBox("Encrypt Password", "setEncryptPassword");
        
        savePwdCheck.addActionListener(this);
        encryptPwdCheck.addActionListener(this);
        
        // retrieve the drivers
        buildDriversList();
        
        // ---------------------------------
        // add the basic connection fields
        
        TextFieldPanel mainPanel = new TextFieldPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridy = 0;
        gbc.gridx = 0;
        
        statusLabel = new DefaultFieldLabel();
        addLabelFieldPair(mainPanel, "Status:", 
                statusLabel, "Current connection status", gbc);
        
        gbc.insets.bottom = 5;
        addLabelFieldPair(mainPanel, "Connection Name:", 
                nameField, "A friendly name for this connection", gbc);

        addLabelFieldPair(mainPanel, "User Name:", 
                userField, "Login user name", gbc);

        addLabelFieldPair(mainPanel, "Password:", 
                passwordField, "Login password", gbc);

        JButton showPassword = new LinkButton("Show Password");
        showPassword.setActionCommand("showPassword");
        showPassword.addActionListener(this);

        JPanel passwordOptionsPanel = new JPanel(new GridBagLayout());
        addComponents(passwordOptionsPanel,
                      new ComponentToolTipPair[]{
                        new ComponentToolTipPair(savePwdCheck, "Store the password with the connection information"),
                        new ComponentToolTipPair(encryptPwdCheck, "Encrypt the password when saving"),
                        new ComponentToolTipPair(showPassword, "Show the password in plain text")});
        
        gbc.gridy++;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        mainPanel.add(passwordOptionsPanel, gbc);
        
        addLabelFieldPair(mainPanel, "Host Name:", 
                hostField, "Server host name or IP address", gbc);

        addLabelFieldPair(mainPanel, "Port:", 
                portField, "Database port number", gbc);

        addLabelFieldPair(mainPanel, "Data Source:", 
                sourceField, "Data source name", gbc);

        addLabelFieldPair(mainPanel, "JDBC URL:", 
                urlField, "The full JDBC URL for this connection (optional)", gbc);

        addDriverFields(mainPanel, gbc);
        
        connectButton = createButton("Connect", CONNECT_ACTION_COMMAND, 'T');
        disconnectButton = createButton("Disconnect", "disconnect", 'D');

        JPanel buttons = new JPanel(new GridBagLayout());
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.insets.top = 5;
        gbc.insets.left = 0;
        gbc.insets.right = 10;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.fill = GridBagConstraints.NONE; 
        buttons.add(connectButton, gbc);
        gbc.gridx++;
        gbc.weightx = 0;
        buttons.add(disconnectButton, gbc);
        
        gbc.insets.right = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        mainPanel.add(buttons, gbc);
        
        // ---------------------------------
        // create the advanced panel
        
        model = new JdbcPropertiesTableModel();
        JTable table = new DefaultTable(model);
        table.getTableHeader().setReorderingAllowed(false);
        
        TableColumnModel tcm = table.getColumnModel();
        
        TableColumn column = tcm.getColumn(2);
        column.setCellRenderer(new DeleteButtonRenderer());
        column.setCellEditor(new DeleteButtonEditor(table, new JCheckBox()));
        column.setMaxWidth(24);
        column.setMinWidth(24);
        
        JScrollPane scroller = new JScrollPane(table);
        
        // advanced jdbc properties
        JPanel advPropsPanel = new JPanel(new GridBagLayout());
        advPropsPanel.setBorder(BorderFactory.createTitledBorder("JDBC Properties"));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.insets.top = 0;
        gbc.insets.left = 10;
        gbc.insets.right = 10;
        gbc.weighty = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        advPropsPanel.add(
                new DefaultFieldLabel("Enter any key/value pair properties for this connection"), gbc);
        gbc.gridy++;
        advPropsPanel.add(
                new DefaultFieldLabel("Refer to the relevant JDBC driver documentation for possible entries"), gbc);
        gbc.gridy++;
        gbc.insets.bottom = 10;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        advPropsPanel.add(scroller, gbc);        
        
        // transaction isolation
        txApplyButton =  WidgetFactory.createInlineFieldButton("Apply", "transactionLevelChanged");        
        txApplyButton.setToolTipText("Apply this level to all open connections of this type");
        txApplyButton.setEnabled(false);
        txApplyButton.addActionListener(this);

        // add a dummy select value to the tx levels
        String[] txLevels = new String[Constants.TRANSACTION_LEVELS.length + 1];
        txLevels[0] = "Database Default";
        for (int i = 1; i < txLevels.length; i++) {
            txLevels[i] = Constants.TRANSACTION_LEVELS[i - 1];
        }
        txCombo = WidgetFactory.createComboBox(txLevels);

        JPanel advTxPanel = new JPanel(new GridBagLayout());
        advTxPanel.setBorder(BorderFactory.createTitledBorder("Transaction Isolation"));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets.top = 0;
        gbc.insets.left = 10;
        gbc.insets.right = 10;
        gbc.insets.bottom = 5;
        gbc.weighty = 0;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        advTxPanel.add(
                new DefaultFieldLabel("Default transaction isolation level for this connection"), gbc);
        gbc.gridy++;
        gbc.insets.bottom = 10;
        advTxPanel.add(
                new DefaultFieldLabel("Note: the selected isolation level " +
                           "will apply to ALL open connections of this type."), gbc);        
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.insets.top = 0;
        gbc.insets.left = 10;
        gbc.weightx = 0;
        advTxPanel.add(new DefaultFieldLabel("Isolation Level:"), gbc);
        gbc.gridx = 1;
        gbc.insets.left = 5;
        gbc.weightx = 1.0;
        gbc.insets.right = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        advTxPanel.add(txCombo, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.insets.left = 0;
        gbc.insets.right = 10;
        advTxPanel.add(txApplyButton, gbc);
        
        JPanel advancedPanel = new JPanel(new BorderLayout());
        advancedPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        advancedPanel.add(advPropsPanel, BorderLayout.CENTER);
        advancedPanel.add(advTxPanel, BorderLayout.SOUTH);
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);

        sshTunnelConnectionPanel = new SSHTunnelConnectionPanel();
        
        tabPane = new JTabbedPane(JTabbedPane.BOTTOM);
        tabPane.addTab("Basic", scrollPane);
        tabPane.addTab("Advanced", advancedPanel);
        tabPane.addTab("SSH Tunnel", sshTunnelConnectionPanel);

        tabPane.addChangeListener(this);
        
        add(tabPane, BorderLayout.CENTER);
        
        EventMediator.registerListener(this);
    }

    @Override
    public void stateChanged(ChangeEvent e) {

        if (tabPane.getSelectedIndex() == 2) {
            
            populateConnectionObject();
            sshTunnelConnectionPanel.setValues(databaseConnection);
        }
        
    }
    
    private NumberTextField createNumberTextField() {
        
        NumberTextField textField = WidgetFactory.createNumberTextField();
        formatTextField(textField);
        
        return textField;
    }

    private JPasswordField createPasswordField() {
        
        JPasswordField field = WidgetFactory.createPasswordField();
        formatTextField(field);
        
        return field;
    }

    private JTextField createMatchedWidthTextField() {
        
        JTextField textField = new DefaultTextField() {
            public Dimension getPreferredSize() {
                return nameField.getPreferredSize();
            };
        };
        formatTextField(textField);

        return textField;
    }

    private JTextField createTextField() {
        
        JTextField textField = WidgetFactory.createTextField();
        formatTextField(textField);
        
        return textField;
    }
    
    private void formatTextField(JTextField textField) {
        textField.setActionCommand(CONNECT_ACTION_COMMAND);
        textField.addActionListener(this);
    }
    
    private JButton createButton(String text, String actionCommand, int mnemonic) {

        FormPanelButton button = new FormPanelButton(text, actionCommand);
        
        button.setMnemonic(mnemonic);
        button.addActionListener(this);
        button.applyMaximumSize();
        
        return button;
    }
    
    public void connectionNameChanged() {
        if (panelSelected) {
            populateConnectionObject();
        }
    }

    public void connectionNameChanged(String name) {
        nameField.setText(name);
        populateConnectionObject();
    }

    private DatabaseDriverRepository driverRepository() {

        return (DatabaseDriverRepository) RepositoryCache.load(
                DatabaseDriverRepository.REPOSITORY_ID);
    }

    private List<DatabaseDriver> loadDrivers() {

        return driverRepository().findAll();
    }

    /**
     * Retrieves and populates the drivers list.
     */
    protected void buildDriversList() {

        jdbcDrivers = loadDrivers();

        int size = jdbcDrivers.size();

        String[] driverNames = new String[size + 1];
        driverNames[0] = "Select...";

        for (int i = 0; i < size; i++) {

            driverNames[i+1] = jdbcDrivers.get(i).toString();
        }
        
        if (driverCombo == null) {

            DynamicComboBoxModel comboModel = new DynamicComboBoxModel();
            comboModel.setElements(driverNames);
            driverCombo = WidgetFactory.createComboBox(comboModel);

        } else {

            DynamicComboBoxModel comboModel = (DynamicComboBoxModel)driverCombo.getModel();
            comboModel.setElements(driverNames);
            driverCombo.setModel(comboModel);
            selectDriver();
        }
        
    }

    /**
     * Action performed upon selection of the Apply button
     * when selecting a tx isolation level.
     */
    public void transactionLevelChanged() {
        if (ConnectionManager.isTransactionSupported(databaseConnection)) {
            try {
                applyTransactionLevel(true);
                if (databaseConnection.getTransactionIsolation() == -1) {
                    return;
                }

                String txLevel = txCombo.getSelectedItem().toString();
                GUIUtilities.displayInformationMessage(
                        "The transaction isolation level " + txLevel + 
                        " was applied successfully.");
            }
            catch (DataSourceException e) {
                GUIUtilities.displayWarningMessage(
                        "The selected isolation level could not be applied.\n" +
                        "The JDBC driver returned:\n\n" +
                        e.getMessage() + "\n\n");
            }
            catch (Exception e) {}
        }
        else {
            GUIUtilities.displayWarningMessage(
                    "The specified driver for this connection does " +
                    "not support transactions.\nThis feature is unavailable.");
        }
    }
    
    /**
     * Applies the tx level on open connections of the type selected.
     */
    private void applyTransactionLevel(boolean reloadProperties) throws DataSourceException {
        
        // set the tx level from the combo selection
        getTransactionIsolationLevel();        
        int isolationLevel = databaseConnection.getTransactionIsolation();

        // apply to open connections
        ConnectionManager.setTransactionIsolationLevel(databaseConnection, isolationLevel);
        
        if (reloadProperties) {
        
            controller.updateDatabaseProperties();
        }
        
    }

    private boolean connectionNameExists() {

        String name = nameField.getText().trim();
        if (databaseConnectionRepository().nameExists(databaseConnection, name)) {

            GUIUtilities.displayErrorMessage("The name [ " + name 
                    + " ] entered for this connection already exists");        	
            return true;
        }

        return false;
    }

    private DatabaseConnectionRepository databaseConnectionRepository() {

        return (DatabaseConnectionRepository)RepositoryCache.load(
                    DatabaseConnectionRepository.REPOSITORY_ID);        
    }

    /**
     * Acion implementation on selection of the Connect button.
     */
    public void connect() {
        
        if (databaseConnection.isConnected()) {

            return;
        }
        
        // ----------------------------
        // some validation
        
        // make sure a name has been entered
        if (nameField.getText().trim().length() == 0) {
            GUIUtilities.displayErrorMessage("You must enter a name for this connection");
            return;
        }

        if (connectionNameExists()) {
            focusNameField();
            return;
        }
        
        // check a driver is selected
        if (driverCombo.getSelectedIndex() == 0) {
            GUIUtilities.displayErrorMessage("You must select a driver");
            return;
        }

        // check if we have a url - if not check the port is valid
        if (StringUtils.isBlank(urlField.getText())) {

            String port = portField.getText();
            if (!StringUtils.isNumeric(port)) {
                    
                GUIUtilities.displayErrorMessage("Invalid port number");
                return;                    
            }

        }

        if (!sshTunnelConnectionPanel.canConnect()) {
            
            return;
        }
        
        // otherwise - good to proceed
        
        // populate the object with field values
        //populateConnectionObject();
        
        populateAndSave();
        
        try {
        
            // connect
            GUIUtilities.showWaitCursor();
            
            boolean connected = host.connect();
            if (connected) {
            
                // apply the tx level if supplied
                try {
                
                    applyTransactionLevel(false);

                } catch (DataSourceException e) {

                    GUIUtilities.displayWarningMessage(
                            "The selected isolation level could not be applied.\n" +
                            "The JDBC driver returned:\n\n" +
                            e.getMessage() + "\n\n");
                }

            }

        } catch (DataSourceException e) {

            StringBuilder sb = new StringBuilder();
            sb.append("The connection to the database could not be established.");
            sb.append("\nPlease ensure all required fields have been entered ");
            sb.append("correctly and try again.\n\nThe system returned:\n");
            sb.append(e.getExtendedMessage());
            GUIUtilities.displayExceptionErrorDialog(sb.toString(), e);

        } finally {

            GUIUtilities.showNormalCursor();
        }

    }

    public void showPassword() {
        
        new ShowPasswordDialog(nameField.getText(), 
                MiscUtils.charsToString(passwordField.getPassword()));
    }
    
    /**
     * Informed by a tree selection, this readies the form for
     * a new connection object and value change.
     */
    protected void selectionChanging() {
        if (databaseConnection != null) {
            populateConnectionObject();
        }
    }

    private boolean panelSelected = true;
    
    private boolean populateAndSave() {
        
        populateConnectionObject();

        EventMediator.fireEvent(
                new DefaultConnectionRepositoryEvent(
                        this, ConnectionRepositoryEvent.CONNECTION_MODIFIED, null));
        
        return true;
    }

    /**
     * Indicates the panel is being de-selected in the pane
     */
    public boolean tabViewDeselected() {

        panelSelected = false;

        return populateAndSave();
    }

    /**
     * Indicates the panel is being selected in the pane
     */
    public boolean tabViewSelected() {

        panelSelected = true;
        if (databaseConnection != null) {
            
            enableFields(databaseConnection.isConnected());
        }
        return true;
    }

    /**
     * Checks the current selection for a name change
     * to be propagated back to the tree view.
     */
    private void checkNameUpdate() {
    	
    	if (connectionNameExists()) {
            focusNameField();
    	    return;
    	}

        String oldName = databaseConnection.getName();
        String newName = nameField.getText().trim();
        if (!oldName.equals(newName)) {
            databaseConnection.setName(newName);
            controller.nodeNameValueChanged(host);
        }        
    }

    /**
     * Acion implementation on selection of the Disconnect button.
     */
    public void disconnect() {
        try {
            host.disconnect();
        }
        catch (DataSourceException e) {
            GUIUtilities.displayErrorMessage(
                    "Error disconnecting from data source:\n" + e.getMessage());
        }
    }
    
    /**
     * Retrieves the values from the jdbc properties table
     * and stores them within the current database connection.
     */
    private void storeJdbcProperties() {

        Properties properties = databaseConnection.getJdbcProperties();
        if (properties == null) {

            properties = new Properties();

        } else {

            properties.clear();
        }

        for (int i = 0; i < advancedProperties.length; i++) {

            String key = advancedProperties[i][0];
            String value = advancedProperties[i][1];

            if (!MiscUtils.isNull(key) && !MiscUtils.isNull(value)) {

                properties.setProperty(key, value);
            }

        }

        databaseConnection.setJdbcProperties(properties);
    }

    /**
     * Sets the values of the current database connection
     * within the jdbc properties table.
     */
    private void setJdbcProperties() {
        advancedProperties = new String[20][2];
        Properties properties = databaseConnection.getJdbcProperties();
        if (properties == null || properties.size() == 0) {
            model.fireTableDataChanged();
            return;
        }

        int count = 0;
        for (Enumeration<?> i = properties.propertyNames(); i.hasMoreElements();) {
            String name = (String)i.nextElement();
            if (!name.equalsIgnoreCase("password")) {
                advancedProperties[count][0] = name;
                advancedProperties[count][1] = properties.getProperty(name);
                count++;
            }
        }
        model.fireTableDataChanged();
    }
    
    /**
     * Indicates a connection has been established.
     * 
     * @param the connection properties object
     */
    public void connected(DatabaseConnection databaseConnection) {

        populateConnectionFields(databaseConnection);

        /*
        EventMediator.fireEvent(new DefaultConnectionRepositoryEvent(this,
                        ConnectionRepositoryEvent.CONNECTION_MODIFIED, 
                        databaseConnection));
        */
    }

    /**
     * Indicates a connection has been closed.
     * 
     * @param the connection properties object
     */
    public void disconnected(DatabaseConnection databaseConnection) {
        enableFields(false);
    }

    /** 
     * Enables/disables fields as specified.
     */
    private void enableFields(boolean enable) {

        txApplyButton.setEnabled(enable);
        connectButton.setEnabled(!enable);
        disconnectButton.setEnabled(enable);

        if (enable) {

            int count = ConnectionManager.getOpenConnectionCount(databaseConnection);

            statusLabel.setText("Connected [ " + count +
                    (count > 1 ? " connections open ]" : " connection open ]") );

        } else {

            statusLabel.setText("Not Connected");
        }

        paintStatusLabel();
        setEncryptPassword();
    }
    
    /**
     * Changes the state of the save and encrypt password
     * check boxes depending on the whether the encrypt
     * check box is selected.
     */
    public void setEncryptPassword() {

        boolean encrypt = encryptPwdCheck.isSelected();

        if (encrypt && !savePwdCheck.isSelected()) {
        
            savePwdCheck.setSelected(encrypt);
        }
        
    }

    /**
     * Changes the state of the encrypt password check
     * box depending on the whether the save password
     * check box is selected.
     */
    public void setStorePassword() {
        
        boolean store = savePwdCheck.isSelected();
        encryptPwdCheck.setEnabled(store);
    }
    
    /**
     * Sets the values for the tx level on the connection object
     * based on the tx level in the tx combo.
     */
    private void getTransactionIsolationLevel() {

        int index = txCombo.getSelectedIndex();
        if (index == 0) {

            databaseConnection.setTransactionIsolation(-1);
            return;
        }

        int isolationLevel = isolationLevelFromSelection(index);        
        databaseConnection.setTransactionIsolation(isolationLevel);
    }

    private int isolationLevelFromSelection(int index) {
        int isolationLevel = -1;
        switch (index) {
            case 1:
                isolationLevel = Connection.TRANSACTION_NONE;
                break;
            case 2:
                isolationLevel = Connection.TRANSACTION_READ_UNCOMMITTED;
                break;
            case 3:
                isolationLevel = Connection.TRANSACTION_READ_COMMITTED;
                break;
            case 4:
                isolationLevel = Connection.TRANSACTION_REPEATABLE_READ;
                break;
            case 5:
                isolationLevel = Connection.TRANSACTION_SERIALIZABLE;
                break;
        }
        return isolationLevel;
    }

    /**
     * Sets the values for the tx level on the tx combo
     * based on the tx level in the connection object.
     */
    private void setTransactionIsolationLevel() {
        int index = 0;
        int isolationLevel = databaseConnection.getTransactionIsolation();
        switch (isolationLevel) {
            case Connection.TRANSACTION_NONE:
                index = 1;
                break;
            case Connection.TRANSACTION_READ_UNCOMMITTED:
                index = 2;
                break;
            case Connection.TRANSACTION_READ_COMMITTED:
                index = 3;
                break;
            case Connection.TRANSACTION_REPEATABLE_READ:
                index = 4;
                break;
            case Connection.TRANSACTION_SERIALIZABLE:
                index = 5;
                break;
        }
        txCombo.setSelectedIndex(index);
    }
    
    /**
     * Selects the driver for the current connection.
     */
    private void selectDriver() {

        if (databaseConnection == null) {

            return;
        }

        if (databaseConnection.getDriverId() == 0) {

            driverCombo.setSelectedIndex(0);

        } else {

            long driverId = databaseConnection.getDriverId();

            if (driverId != 0) {

                DatabaseDriver driver = driverRepository().findById(driverId);
                if (driver != null) {

                    driverCombo.setSelectedItem(driver.getName());
                }

            }

        }
    }
    
    /**
     * Populates the values of the fields with the values of
     * the specified connection.
     */
    private void populateConnectionFields(DatabaseConnection databaseConnection) {

        // rebuild the driver list
        buildDriversList();
        
        // populate the field values/selections
        savePwdCheck.setSelected(databaseConnection.isPasswordStored());
        encryptPwdCheck.setSelected(databaseConnection.isPasswordEncrypted());
        userField.setText(databaseConnection.getUserName());
        passwordField.setText(databaseConnection.getUnencryptedPassword());
        hostField.setText(databaseConnection.getHost());
        portField.setText(databaseConnection.getPort());
        sourceField.setText(databaseConnection.getSourceName());
        urlField.setText(databaseConnection.getURL());
        nameField.setText(databaseConnection.getName());

        // assign as the current connection
        this.databaseConnection = databaseConnection;

        // set the correct driver selected
        selectDriver();

        // set the jdbc properties
        setJdbcProperties();
        
        // the tx level
        setTransactionIsolationLevel();
        
        // enable/disable fields
        enableFields(databaseConnection.isConnected());
        
        // shh tunnel where applicable
        sshTunnelConnectionPanel.setValues(databaseConnection);
        
    }
    
    /**
     * Populates the values of the selected connection
     * properties bject with the field values.
     */
    private void populateConnectionObject() {

        if (databaseConnection == null) {

            return;
        }
        
        databaseConnection.setPasswordStored(savePwdCheck.isSelected());
        databaseConnection.setPasswordEncrypted(encryptPwdCheck.isSelected());
        databaseConnection.setUserName(userField.getText());
        databaseConnection.setPassword(MiscUtils.charsToString(passwordField.getPassword()));
        databaseConnection.setHost(hostField.getText());
        databaseConnection.setPort(portField.getText());
        databaseConnection.setSourceName(sourceField.getText());
        databaseConnection.setURL(urlField.getText());

        // jdbc driver selection
        int driverIndex = driverCombo.getSelectedIndex();
        if (driverIndex >= jdbcDrivers.size() + 1) {

            driverIndex = jdbcDrivers.size();

            driverCombo.setSelectedIndex(driverIndex);
        }
        
        if (driverIndex > 0) {

            DatabaseDriver driver = (DatabaseDriver)jdbcDrivers.get(driverIndex - 1);

            databaseConnection.setJDBCDriver(driver);
            databaseConnection.setDriverName(driver.getName());
            databaseConnection.setDriverId(driver.getId());
            databaseConnection.setDatabaseType(Integer.toString(driver.getType()));

        } else {

            databaseConnection.setDriverId(0);
            databaseConnection.setJDBCDriver(null);
            databaseConnection.setDriverName(null);
            databaseConnection.setDatabaseType(null);
        }

        sshTunnelConnectionPanel.update(databaseConnection);

        // retrieve the jdbc properties
        storeJdbcProperties();

        // set the tx level on the connection props object
        getTransactionIsolationLevel();
        
        // check if the name has to update the tree display
        checkNameUpdate();
    }

    /**
     * Sets the connection fields on this panel to the
     * values as held within the specified connection
     * properties object.
     *
     * @param the connection to set the fields to
     */
    public void setConnectionValue(DatabaseHost host) {
        
        connectButton.setEnabled(false);
        disconnectButton.setEnabled(false);

        if (databaseConnection != null) {

            populateConnectionObject();
        }

        this.host = host;

        populateConnectionFields(host.getDatabaseConnection());
        
        // set the focus field
        focusNameField();

        // queue a save
        EventMediator.fireEvent(new DefaultConnectionRepositoryEvent(this,
                ConnectionRepositoryEvent.CONNECTION_MODIFIED, 
                databaseConnection));

    }
    
    private void focusNameField() {
        nameField.requestFocusInWindow();
        nameField.selectAll();
    }
    
    /**
     * Forces a repaint using paintImmediately(...) on the
     * connection status label.
     */
    private void paintStatusLabel() {
        Runnable update = new Runnable() {
            public void run() {
                repaint();
                Dimension dim = statusLabel.getSize();
                statusLabel.paintImmediately(0, 0, dim.width, dim.height);
            }
        };
        SwingUtilities.invokeLater(update);
    }
    
    private class JdbcPropertiesTableModel extends AbstractTableModel {
        
        protected String[] header = {"Name", "Value", ""};
        
        public JdbcPropertiesTableModel() {
            advancedProperties = new String[20][2];
        }
        
        public int getColumnCount() {
            return 3;
        }
        
        public int getRowCount() {
            return advancedProperties.length;
        }
        
        public Object getValueAt(int row, int col) {
            
            if (col < 2) {
                
                return advancedProperties[row][col];
                
            } else {
                
                return "";
            }
        }
        
        public void setValueAt(Object value, int row, int col) {
            if (col < 2) {
                advancedProperties[row][col] = (String)value;
                fireTableRowsUpdated(row, row);
            }
        }
        
        public boolean isCellEditable(int row, int col) {
            return true;
        }
        
        public String getColumnName(int col) {
            return header[col];
        }
        
        public Class<?> getColumnClass(int col) {
            return String.class;
        }
        
    } // AdvConnTableModel

    private void addDriverFields(JPanel panel, GridBagConstraints gbc) {

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.insets.top = 0;
        gbc.insets.left = 10;
        gbc.weightx = 0;
        panel.add(new DefaultFieldLabel("JDBC Driver:"), gbc);
        gbc.gridx = 1;
        gbc.insets.left = 5;
        gbc.insets.right = 5;
        gbc.weightx = 1.0;
        gbc.insets.top = 0;
        panel.add(driverCombo, gbc);

        driverCombo.setToolTipText("The JDBC driver to be used for this database");

        JButton button = WidgetFactory.createInlineFieldButton("New Driver");
        button.setActionCommand("addNewDriver");
        button.addActionListener(this);
        button.setMnemonic('r');

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.insets.left = 0;
        gbc.ipadx = 10;
        gbc.insets.right = 10;
        panel.add(button, gbc);
    }
    
    public void addNewDriver() {
        
        new DialogDriverPanel();
    }

    public void driversUpdated(DatabaseDriverEvent databaseDriverEvent) {

        buildDriversList();

        DatabaseDriver driver = (DatabaseDriver)databaseDriverEvent.getSource();
        driverCombo.setSelectedItem(driver.getName());
    }

    public boolean canHandleEvent(ApplicationEvent event) {

        return (event instanceof DatabaseDriverEvent);
    }
    
    
    
    class DeleteButtonEditor extends DefaultCellEditor {
        
        private JButton button;
        private boolean isPushed;
        private final JTable table;
        
        public DeleteButtonEditor(JTable table, JCheckBox checkBox) {
            
            super(checkBox);
            this.table = table;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }
        
        public Component getTableCellEditorComponent(JTable table, 
                                                     Object value,
                                                     boolean isSelected, 
                                                     int row, 
                                                     int column) {
            isPushed = true;
            return button;
        }
        
        public Object getCellEditorValue() {
            
            if (isPushed) {

                clearValueAt(table.getEditingRow());
            }
            
            isPushed = false;
            return Constants.EMPTY;
        }
        
        private void clearValueAt(int row) {

            table.setValueAt("", row, 0);
            table.setValueAt("", row, 1);
        }

        public boolean stopCellEditing() {

            isPushed = false;
            return super.stopCellEditing();
        }
        
        protected void fireEditingStopped() {

            super.fireEditingStopped();
      }

    } // DeleteButtonEditor
    
    class DeleteButtonRenderer extends JButton implements TableCellRenderer {
        
        public DeleteButtonRenderer() {

            setFocusPainted(false);
            setBorderPainted(false);
            setMargin(Constants.EMPTY_INSETS);
            setIcon(GUIUtilities.loadIcon("GcDelete16.png"));
            setPressedIcon(GUIUtilities.loadIcon("GcDeletePressed16.png"));
            
            try {
                setUI(new javax.swing.plaf.basic.BasicButtonUI());
            } catch (NullPointerException nullExc) {}

            setToolTipText("Clear this key/value pair");
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            return this;
        }

    } // DeleteButtonRenderer

    
}







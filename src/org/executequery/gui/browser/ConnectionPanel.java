/*
 * ConnectionPanel.java
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

package org.executequery.gui.browser;

import org.apache.commons.lang.StringUtils;
import org.executequery.Constants;
import org.executequery.EventMediator;
import org.executequery.ExecuteQuery;
import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.components.TextFieldPanel;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.datasource.ConnectionManager;
import org.executequery.datasource.DefaultDriverLoader;
import org.executequery.event.*;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.FormPanelButton;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.drivers.DialogDriverPanel;
import org.executequery.localization.Bundles;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.DatabaseDriverRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.*;
import org.underworldlabs.swing.actions.ActionUtilities;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.Driver;
import java.util.*;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class ConnectionPanel extends AbstractConnectionPanel
        implements DatabaseDriverListener,
        ChangeListener {

    private static class TraceMessageLoop
            implements Runnable {

        //        FBTraceManager fbTraceManager;
        String configuration = null;
        String database;
        String charSet;
        String user;
        String password;
        String host;
        int port;

        String traceUUID;

        TraceMessageLoop() {
//            fbTraceManager = new FBTraceManager();
//            fbTraceManager.setLogger(System.out);
//            traceUUID = UUID.randomUUID().toString();
//            try {
//                configuration = fbTraceManager.loadConfigurationFromFile("config/fbtrace.conf", true);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public void setCharSet(String charSet) {
            this.charSet = charSet;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public void run() {
            try {
//                fbTraceManager.setHost(this.host);
//                fbTraceManager.setPort(this.port);
//                fbTraceManager.setCharSet(this.charSet);
//                fbTraceManager.setPassword(this.password);
//                fbTraceManager.setUser(this.user);
//                fbTraceManager.setDatabase(this.database);
//                fbTraceManager.startTraceSession(traceUUID, configuration);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void stop() {
            try {
//                int traceSessionId = fbTraceManager.getSessionId(traceUUID);
//                fbTraceManager.stopTraceSession(traceSessionId);
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
    }

    TraceMessageLoop traceMessageLoop = null;

    private List<String> charsets;

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
    //    private NumberTextField portField;
    private JTextField portField;
    private JTextField sourceField;
    private JTextField urlField;

    private JComboBox charsetsCombo;
    private JTextField roleField;

    private JTextField certificateFileField;

    private JComboBox authCombo;
    private JComboBox methodCombo;

    private JLabel statusLabel;

    private JComboBox txCombo;
    private JButton txApplyButton;

    JPanel basicPanel;
    JPanel standardPanel;
    JPanel multifactorPanel;
    JPanel jdbcUrlPanel;

    // -------------------------------

    /**
     * table model for jdbc properties key/values
     */
    private JdbcPropertiesTableModel model;

    /**
     * connect button
     */
    private JButton connectButton;

    /**
     * disconnect button
     */
    private JButton disconnectButton;

    /**
     * the saved jdbc drivers
     */
    private List<DatabaseDriver> jdbcDrivers;

    /**
     * any advanced property keys/values
     */
    private String[][] advancedProperties;

    /**
     * the tab basic/advanced tab pane
     */
    private JTabbedPane tabPane;

    /**
     * the connection properties displayed
     */
    private DatabaseConnection databaseConnection;

    /**
     * the host object representing this connection
     */
    private DatabaseHost host;

    /**
     * the browser's control object
     */
    private BrowserController controller;

    private SSHTunnelConnectionPanel sshTunnelConnectionPanel;

    /**
     * Creates a new instance of ConnectionPanel
     */
    public ConnectionPanel(BrowserController controller) {
        super(new BorderLayout());
        this.controller = controller;
        init();
    }

    private void init() {

        // ---------------------------------
        // create the basic props panel

        List<String> auth = new ArrayList<>();
        auth.add(bundleString("BasicAu"));
        auth.add("GSS");
        auth.add("Multifactor");
        authCombo = new JComboBox(auth.toArray());
        authCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Object selectedItem = e.getItem();
                if (selectedItem.toString().equalsIgnoreCase(bundleString("BasicAu"))) {
                    basicPanel.setVisible(true);
                    multifactorPanel.setVisible(false);
                } else if (selectedItem.toString().equalsIgnoreCase("gss")) {
                    basicPanel.setVisible(false);
                    multifactorPanel.setVisible(false);
                } else if (selectedItem.toString().equalsIgnoreCase("multifactor")) {
                    basicPanel.setVisible(true);
                    multifactorPanel.setVisible(true);
                }
            }
        });

        List<String> methods = new ArrayList<>();
        methods.add(bundleString("Standard"));
        methods.add("JDBC");
        methodCombo = new JComboBox(methods.toArray());
        methodCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Object selectedItem = e.getItem();
                if (selectedItem.toString().equalsIgnoreCase(bundleString("Standard"))) {
                    standardPanel.setVisible(true);
                    jdbcUrlPanel.setVisible(false);
                } else if (selectedItem.toString().equalsIgnoreCase("jdbc")) {
                    standardPanel.setVisible(false);
                    jdbcUrlPanel.setVisible(true);
                }
            }
        });

        // initialise the fields
        nameField = createTextField();
        passwordField = createPasswordField();
        hostField = createTextField();
        hostField.setText("localhost");
        portField = createNumberTextField();
        portField.setText("3050");
        sourceField = createMatchedWidthTextField();
        roleField = createTextField();
        userField = createTextField();
        urlField = createMatchedWidthTextField();

        certificateFileField = createMatchedWidthTextField();

        nameField.addFocusListener(new ConnectionNameFieldListener(this));

        savePwdCheck = ActionUtilities.createCheckBox(bundleString("StorePassword"), "setStorePassword");
        encryptPwdCheck = ActionUtilities.createCheckBox(bundleString("EncryptPassword"), "setEncryptPassword");

        savePwdCheck.addActionListener(this);
        encryptPwdCheck.addActionListener(this);

        // retrieve the drivers
        buildDriversList();

        // retrieve the available charsets
        loadCharsets();
        charsetsCombo = WidgetFactory.createComboBox(charsets.toArray());

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
        addLabelFieldPair(mainPanel, bundleString("statusLabel"),
                statusLabel, bundleString("statusLabel.tool-tip"), gbc);

        addDriverFields(mainPanel, gbc);

        addLabelFieldPair(mainPanel, bundleString("nameField"),
                nameField, bundleString("nameField.tool-tip"), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        basicPanel = new JPanel(new GridBagLayout());
        GridBagConstraints bgbc = new GridBagConstraints();
        bgbc.fill = GridBagConstraints.HORIZONTAL;
        bgbc.anchor = GridBagConstraints.NORTHWEST;
        bgbc.insets = new Insets(5, 5, 5, 5);

        JLabel userLabel = new DefaultFieldLabel(bundleString("userField"));
        bgbc.gridx = 0;
        bgbc.gridwidth = 1;
        bgbc.weightx = 0;
        basicPanel.add(userLabel, bgbc);

        bgbc.gridx = 1;
        bgbc.insets.left = 0;
        bgbc.weightx = 0.25;
        basicPanel.add(userField, bgbc);

        JLabel passwordLabel = new DefaultFieldLabel(bundleString("passwordField"));
        bgbc.gridx = 2;
        bgbc.gridwidth = 1;
        bgbc.weightx = 0;
        basicPanel.add(passwordLabel, bgbc);

        bgbc.gridx = 3;
        bgbc.insets.left = 5;
        bgbc.weightx = 0.25;
        basicPanel.add(passwordField, bgbc);

//        addLabelFieldPair(basicPanel, "User Name:",
//                userField, "Login user name", gbc);
//
//        addLabelFieldPair(basicPanel, "Password:",
//                passwordField, "Login password", gbc);

        JButton showPassword = new LinkButton(bundleString("ShowPassword"));
        showPassword.setActionCommand("showPassword");
        showPassword.addActionListener(this);

        JPanel passwordOptionsPanel = new JPanel(new GridBagLayout());
        addComponents(passwordOptionsPanel,
                new ComponentToolTipPair[]{
                        new ComponentToolTipPair(savePwdCheck, bundleString("StorePassword.tool-tip")),
                        new ComponentToolTipPair(encryptPwdCheck, bundleString("EncryptPassword.tool-tip")),
                        new ComponentToolTipPair(showPassword, bundleString("ShowPassword.tool-tip"))});

        bgbc.gridx = 3;
        bgbc.gridy = 1;
        bgbc.insets.left = 5;
        bgbc.insets.right = 0;
        bgbc.weightx = 0.1;
        basicPanel.add(passwordOptionsPanel, bgbc);

        addLabelFieldPair(mainPanel, bundleString("ConnectionParameters"),
                methodCombo, bundleString("ConnectionParameters.tool-tip"), gbc);

        standardPanel = new JPanel(new GridBagLayout());
        GridBagConstraints sgbc = new GridBagConstraints();
        sgbc.fill = GridBagConstraints.HORIZONTAL;
        sgbc.anchor = GridBagConstraints.NORTHWEST;
        sgbc.insets = new Insets(5, 5, 5, 5);

        JLabel hostLabel = new DefaultFieldLabel(bundleString("hostField"));
        sgbc.gridx = 0;
        sgbc.gridwidth = 1;
        sgbc.weightx = 0;
        standardPanel.add(hostLabel, sgbc);

        sgbc.gridx = 1;
        sgbc.insets.left = 5;
        sgbc.weightx = 0.25;
        standardPanel.add(hostField, sgbc);

        JLabel portLabel = new DefaultFieldLabel(bundleString("portField"));
        sgbc.gridx = 2;
        sgbc.insets.left = 5;
        sgbc.weightx = 0;
        standardPanel.add(portLabel, sgbc);

        sgbc.gridx = 3;
        sgbc.insets.left = 5;
        sgbc.weightx = 0.1;
        standardPanel.add(portField, sgbc);

        JLabel dataSourceLabel = new DefaultFieldLabel(bundleString("sourceField"));
        sgbc.gridx = 4;
        sgbc.insets.left = 5;
        sgbc.weightx = 0;
        standardPanel.add(dataSourceLabel, sgbc);

        JButton openFile = new JButton(bundleString("ChooseFile"));
        openFile.addActionListener(new ActionListener() {
            FileChooserDialog fileChooser = new FileChooserDialog();

            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showOpenDialog(openFile);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    sourceField.setText(file.getAbsolutePath());
                }
            }
        });

        JPanel selectFilePanel = new JPanel(new BorderLayout());

        selectFilePanel.add(sourceField, BorderLayout.CENTER);
        selectFilePanel.add(openFile, BorderLayout.LINE_END);

        sgbc.gridx = 5;
        sgbc.insets.left = 5;
        sgbc.weightx = 0.25;
        standardPanel.add(selectFilePanel, sgbc);

        JLabel authLabel = new DefaultFieldLabel(bundleString("Authentication"));
        sgbc.gridy = 1;
        sgbc.gridx = 0;
        sgbc.gridwidth = 1;
        sgbc.weightx = 0;
        standardPanel.add(authLabel, sgbc);

        sgbc.gridx = 1;
        sgbc.insets.left = 5;
        sgbc.weightx = 0.25;
        sgbc.gridwidth = GridBagConstraints.REMAINDER;
        standardPanel.add(authCombo, sgbc);

        sgbc.gridy++;
        sgbc.gridx = 0;
        sgbc.insets.left = 0;
        sgbc.insets.right = 0;
        sgbc.gridwidth = GridBagConstraints.REMAINDER;
        standardPanel.add(basicPanel, sgbc);

        multifactorPanel = new JPanel(new GridBagLayout());
        multifactorPanel.setVisible(false);
        GridBagConstraints mCons = new GridBagConstraints();
        JLabel certLabel = new DefaultFieldLabel("Certificate file (X.509 format):");
        mCons.gridy = 0;
        mCons.gridx = 0;
        mCons.insets = new Insets(0, 0, 5, 5);
        multifactorPanel.add(certLabel, mCons);

        mCons.gridx = 1;
        mCons.gridwidth = 1;
        mCons.insets.left = 5;
        mCons.weightx = 1;
        mCons.fill = GridBagConstraints.HORIZONTAL;
        multifactorPanel.add(certificateFileField, mCons);

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(
                new FileNameExtensionFilter("Certificate file (X.509)", "cer"));

        JButton openCertFile = new JButton("Choose file");
        openCertFile.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showOpenDialog(openCertFile);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    certificateFileField.setText(file.getAbsolutePath());
                }
            }
        });

        mCons.gridx = 2;
        mCons.gridwidth = 1;
        mCons.insets.left = 5;
        mCons.weightx = 0;
        mCons.fill = GridBagConstraints.NONE;
        multifactorPanel.add(openCertFile, mCons);

        sgbc.gridy++;
        sgbc.gridx = 0;
        sgbc.gridwidth = 0;
        sgbc.weightx = 0;
        sgbc.insets.left = 5;
        standardPanel.add(multifactorPanel, sgbc);

        JLabel charsetLabel = new DefaultFieldLabel(bundleString("CharacterSet"));
        sgbc.gridy++;
        sgbc.gridx = 0;
        sgbc.gridwidth = 1;
        sgbc.weightx = 0;
        standardPanel.add(charsetLabel, sgbc);

        sgbc.gridx = 1;
        sgbc.gridwidth = 1;
        sgbc.insets.left = 5;
        sgbc.weightx = 0.25;
        standardPanel.add(charsetsCombo, sgbc);

        JLabel roleLabel = new DefaultFieldLabel(bundleString("Role"));
        sgbc.gridx = 2;
        sgbc.gridwidth = 1;
        sgbc.weightx = 0;
        standardPanel.add(roleLabel, sgbc);

        sgbc.gridx = 3;
        sgbc.gridwidth = 2;
        sgbc.insets.left = 5;
        sgbc.weightx = 0.5;
        standardPanel.add(roleField, sgbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.insets.top = 10;
        gbc.insets.right = 5;

        mainPanel.add(standardPanel, gbc);

        jdbcUrlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints jgbc = new GridBagConstraints();
        jgbc.fill = GridBagConstraints.HORIZONTAL;
        jgbc.anchor = GridBagConstraints.NORTHWEST;
        jgbc.insets = new Insets(5, 5, 5, 5);

        JLabel urlLabel = new DefaultFieldLabel(bundleString("urlField"));
        jgbc.gridx = 0;
        jgbc.gridwidth = 1;
        jgbc.weightx = 0;
        jdbcUrlPanel.add(urlLabel, jgbc);

        jgbc.gridx = 1;
        jgbc.insets.left = 5;
        jgbc.weightx = 1.0;
        jdbcUrlPanel.add(urlField, jgbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.insets.top = 10;
        gbc.insets.right = 5;
        mainPanel.add(jdbcUrlPanel, gbc);
        jdbcUrlPanel.setVisible(false);

        connectButton = createButton(Bundles.getCommon("connect.button"), CONNECT_ACTION_COMMAND, 'T');
        disconnectButton = createButton(Bundles.getCommon("disconnect.button"), "disconnect", 'D');

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
        advPropsPanel.setBorder(BorderFactory.createTitledBorder(bundleString("JDBCProperties")));
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
                new DefaultFieldLabel(bundleString("advPropsPanel.text1")), gbc);
        gbc.gridy++;
        advPropsPanel.add(
                new DefaultFieldLabel(bundleString("advPropsPanel.text2")), gbc);
        gbc.gridy++;
        gbc.insets.bottom = 10;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        advPropsPanel.add(scroller, gbc);

        // transaction isolation
        txApplyButton = WidgetFactory.createInlineFieldButton(Bundles.get("common.apply.button"), "transactionLevelChanged");
        txApplyButton.setToolTipText(bundleString("txApplyButton.tool-tip"));
        txApplyButton.setEnabled(false);
        txApplyButton.addActionListener(this);

        // add a dummy select value to the tx levels
        String[] txLevels = new String[Constants.TRANSACTION_LEVELS.length + 1];
        txLevels[0] = bundleString("DatabaseDefault");
        for (int i = 1; i < txLevels.length; i++) {
            txLevels[i] = Constants.TRANSACTION_LEVELS[i - 1];
        }
        txCombo = WidgetFactory.createComboBox(txLevels);

        JPanel advTxPanel = new JPanel(new GridBagLayout());
        advTxPanel.setBorder(BorderFactory.createTitledBorder(bundleString("TransactionIsolation")));
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
                new DefaultFieldLabel(bundleString("advTxPanel.Text1")), gbc);
        gbc.gridy++;
        gbc.insets.bottom = 10;
        advTxPanel.add(
                new DefaultFieldLabel(bundleString("advTxPanel.Text2")), gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.insets.top = 0;
        gbc.insets.left = 10;
        gbc.weightx = 0;
        advTxPanel.add(new DefaultFieldLabel(bundleString("IsolationLevel")), gbc);
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
        advancedPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        advancedPanel.add(advPropsPanel, BorderLayout.CENTER);
        advancedPanel.add(advTxPanel, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);

        sshTunnelConnectionPanel = new SSHTunnelConnectionPanel();

        tabPane = new JTabbedPane(JTabbedPane.BOTTOM);
        tabPane.addTab(bundleString("Basic"), scrollPane);
        tabPane.addTab(bundleString("Advanced"), advancedPanel);
        tabPane.addTab(bundleString("SSHTunnel"), sshTunnelConnectionPanel);

        tabPane.addChangeListener(this);

        add(tabPane, BorderLayout.CENTER);

        EventMediator.registerListener(this);
    }

    private void loadCharsets() {
        Properties props;
        try {
            if (charsets == null)
                charsets = new ArrayList<String>();
            else
                charsets.clear();

            String resource = FileUtils.loadResource("org/executequery/charsets.properties");
            String[] strings = resource.split("\n"/*System.getProperty("line.separator")*/);
            for (String s : strings) {
                if (!s.startsWith("#") && !s.isEmpty())
                    charsets.add(s);
            }
            java.util.Collections.sort(charsets);
            charsets.add(0, "NONE");

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
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
            }

            ;
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
        driverNames[0] = bundleString("selectDriver");

        for (int i = 0; i < size; i++) {

            driverNames[i + 1] = jdbcDrivers.get(i).toString();
        }

        if (driverCombo == null) {

            DynamicComboBoxModel comboModel = new DynamicComboBoxModel();
            comboModel.setElements(driverNames);
            driverCombo = WidgetFactory.createComboBox(comboModel);

        } else {

            DynamicComboBoxModel comboModel = (DynamicComboBoxModel) driverCombo.getModel();
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
                        bundleString("message.level-change1") + txLevel +
                                bundleString("message.level-change2"));
            } catch (DataSourceException e) {
                GUIUtilities.displayWarningMessage(
                        bundleString("warning.DataSourceException.level-change") +
                                e.getMessage() + "\n\n");
            } catch (Exception e) {
            }
        } else {
            GUIUtilities.displayWarningMessage(
                    bundleString("warning.level-change"));
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

            GUIUtilities.displayErrorMessage(bundleString("error.nameExist1") + name
                    + bundleString("error.nameExist2"));
            return true;
        }

        return false;
    }

    private DatabaseConnectionRepository databaseConnectionRepository() {

        return (DatabaseConnectionRepository) RepositoryCache.load(
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
            GUIUtilities.displayErrorMessage(bundleString("error.emptyName"));
            return;
        }

        if (connectionNameExists()) {
            focusNameField();
            return;
        }

        // check a driver is selected
        if (driverCombo.getSelectedIndex() == 0) {
            GUIUtilities.displayErrorMessage(bundleString("error.emptyDriver"));
            return;
        }

        // check if we have a url - if not check the port is valid
        if (StringUtils.isBlank(urlField.getText())) {

            String port = portField.getText();
            if (!StringUtils.isNumeric(port)) {

                GUIUtilities.displayErrorMessage(bundleString("error.invalidPort"));
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

            System.setProperty("java.security.auth.login.config", "config/gss.login.conf");

            boolean connected = host.connect();

            if (connected) {

                // apply the tx level if supplied
                try {

                    applyTransactionLevel(false);

                } catch (DataSourceException e) {

                    GUIUtilities.displayWarningMessage(
                            bundleString("warning.DataSourceException.level-change") +
                                    e.getMessage() + "\n\n");
                }

                Connection connection = host.getConnection();
                DefaultDriverLoader driverLoader = new DefaultDriverLoader();
                Map<String, Driver> loadedDrivers = driverLoader.getLoadedDrivers();
                DatabaseDriver jdbcDriver = databaseConnection.getJDBCDriver();
                Driver driver = loadedDrivers.get(jdbcDriver.getId() + "-" + jdbcDriver.getClassName());

                int majorVersion = driver.getMajorVersion();

                try {
                    if (majorVersion >= 3) {
//
//                        FBConnection fbConnection = connection.unwrap(FBConnection.class);
//
//                        Log.info("Connection encoding: " + fbConnection.getFbDatabase().getEncoding().getCharsetName());
                    }

                } catch (Exception e) {
                    // nothing
                }

                if (majorVersion >= 3) {
                    String className = host.getDatabaseConnection().getJDBCDriver().getClassName();
                    if (className.contains("FBDriver")) {

                        traceMessageLoop = new TraceMessageLoop();
                        traceMessageLoop.setDatabase(host.getDatabaseConnection().getSourceName());
                        traceMessageLoop.setPort(host.getDatabaseConnection().getPortInt());
                        traceMessageLoop.setUser(host.getDatabaseConnection().getUserName());
                        traceMessageLoop.setPassword(host.getDatabaseConnection().getPassword());
                        traceMessageLoop.setCharSet("UTF8");
                        traceMessageLoop.setHost(host.getDatabaseConnection().getHost());

//                        Thread traceThread = new Thread(traceMessageLoop);
//                        traceThread.start();
                    }
                }
            }

        } catch (DataSourceException e) {

            StringBuilder sb = new StringBuilder();
            sb.append(Bundles.getCommon("error.connection"));
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
                        this, ConnectionRepositoryEvent.CONNECTION_MODIFIED, (DatabaseConnection) null));

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
            if (traceMessageLoop != null) {
                traceMessageLoop.stop();
            }
        } catch (DataSourceException e) {
            GUIUtilities.displayErrorMessage(
                    bundleString("error.disconnect") + e.getMessage());
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

                if (key.equalsIgnoreCase("lc_ctype")
                        || key.equalsIgnoreCase("useGSSAuth")
                        || key.equalsIgnoreCase("roleName")
                        || key.equalsIgnoreCase("isc_dpb_trusted_auth")
                        || key.equalsIgnoreCase("isc_dpb_multi_factor_auth")
                        || key.equalsIgnoreCase("isc_dpb_certificate"))
                    continue;
                properties.setProperty(key, value);
            }

        }

        if (!properties.containsKey("lc_ctype"))
            properties.setProperty("lc_ctype", charsetsCombo.getSelectedItem().toString());

        if (!properties.containsKey("useGSSAuth") && authCombo.getSelectedItem().toString().equalsIgnoreCase("gss"))
            properties.setProperty("useGSSAuth", "true");

        if (!properties.containsKey("roleName") && !roleField.getText().isEmpty())
            properties.setProperty("roleName", roleField.getText());

        if (!properties.containsKey("isc_dpb_trusted_auth")
                && !properties.containsKey("isc_dpb_multi_factor_auth")) {
            properties.setProperty("isc_dpb_trusted_auth", "1");
            properties.setProperty("isc_dpb_multi_factor_auth", "1");
        }
        if (!certificateFileField.getText().isEmpty())
            properties.setProperty("isc_dpb_certificate", certificateFileField.getText());

        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pid = name.split("@")[0];
        properties.setProperty("process_id", pid);
        try {
            String path = ExecuteQuery.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            properties.setProperty("process_name", path);
        } catch (URISyntaxException e) {
            e.printStackTrace();
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
        for (Enumeration<?> i = properties.propertyNames(); i.hasMoreElements(); ) {
            String name = (String) i.nextElement();
            if (!name.equalsIgnoreCase("password") && !name.equalsIgnoreCase("lc_ctype")
                    && !name.equalsIgnoreCase("useGSSAuth")
                    && !name.equalsIgnoreCase("process_id")
                    && !name.equalsIgnoreCase("process_name")
                    && !name.equalsIgnoreCase("roleName")
                    && !name.equalsIgnoreCase("isc_dpb_trusted_auth")
                    && !name.equalsIgnoreCase("isc_dpb_multi_factor_auth")
                    && !name.equalsIgnoreCase("isc_dpb_certificate")) {
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
     * @param databaseConnection connection properties object
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
     * @param databaseConnection connection properties object
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

            statusLabel.setText(bundleString("status.Connected") + count +
                    (count > 1 ? bundleString("status.Connected.connections") : bundleString("status.Connected.connection")));

        } else {

            statusLabel.setText(bundleString("status.NotConnected"));
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
        hostField.setText(databaseConnection.getHost().isEmpty() ? "localhost" : databaseConnection.getHost());
        portField.setText(databaseConnection.getPort().isEmpty() ? "3050" : databaseConnection.getPort());
        sourceField.setText(databaseConnection.getSourceName());
        urlField.setText(databaseConnection.getURL());
        nameField.setText(databaseConnection.getName());
        charsetsCombo.setSelectedItem(databaseConnection.getCharset());
        roleField.setText(databaseConnection.getRole());
        authCombo.setSelectedItem(databaseConnection.getAuthMethod());
        methodCombo.setSelectedItem(databaseConnection.getConnectionMethod());

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
        String path = sourceField.getText().replace("\\", "/");
        databaseConnection.setPasswordStored(savePwdCheck.isSelected());
        databaseConnection.setPasswordEncrypted(encryptPwdCheck.isSelected());
        databaseConnection.setUserName(userField.getText());
        databaseConnection.setPassword(MiscUtils.charsToString(passwordField.getPassword()));
        databaseConnection.setHost(hostField.getText());
        databaseConnection.setPort(portField.getText());
        databaseConnection.setSourceName(path);
        databaseConnection.setURL(urlField.getText());
        databaseConnection.setRole(roleField.getText());
        databaseConnection.setCharset(charsetsCombo.getSelectedItem().toString());
        databaseConnection.setAuthMethod(authCombo.getSelectedItem().toString());
        databaseConnection.setConnectionMethod(methodCombo.getSelectedItem().toString());

        // jdbc driver selection
        int driverIndex = driverCombo.getSelectedIndex();
        if (driverIndex >= jdbcDrivers.size() + 1) {

            driverIndex = jdbcDrivers.size();

            driverCombo.setSelectedIndex(driverIndex);
        }

        if (driverIndex > 0) {

            DatabaseDriver driver = (DatabaseDriver) jdbcDrivers.get(driverIndex - 1);

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
     * @param host connection to set the fields to
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

        protected String[] header = Bundles.getCommons(new String[]{"key", "value", ""});

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
                advancedProperties[row][col] = (String) value;
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
        panel.add(new DefaultFieldLabel(bundleString("driverField")), gbc);
        gbc.gridx = 1;
        gbc.insets.left = 5;
        gbc.insets.right = 5;
        gbc.weightx = 1.0;
        gbc.insets.top = 0;
        panel.add(driverCombo, gbc);

        driverCombo.setToolTipText(bundleString("driverField.tool-tip"));

        JButton button = WidgetFactory.createInlineFieldButton(bundleString("addNewDriver"));
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

        DatabaseDriver driver = (DatabaseDriver) databaseDriverEvent.getSource();
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
            } catch (NullPointerException nullExc) {
            }

            setToolTipText(bundleString("delete.tool-tip"));
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            return this;
        }

    } // DeleteButtonRenderer


}








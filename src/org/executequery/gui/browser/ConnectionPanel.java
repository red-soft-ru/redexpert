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
import org.executequery.*;
import org.executequery.components.FileChooserDialog;
import org.executequery.components.TextFieldPanel;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.databaseobjects.ConnectionTester;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.datasource.ConnectionManager;
import org.executequery.datasource.DefaultDriverLoader;
import org.executequery.event.*;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.drivers.DialogDriverPanel;
import org.executequery.gui.editor.TransactionIsolationCombobox;
import org.executequery.localization.Bundles;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.DatabaseDriverRepository;
import org.executequery.repository.RepositoryCache;
import org.executequery.util.Base64;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.*;
import org.underworldlabs.swing.actions.ActionUtilities;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * @author Takis Diakoumis
 */
public class ConnectionPanel extends AbstractConnectionPanel
        implements DatabaseDriverListener,
        ChangeListener {

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
    private JPasswordField containerPasswordField;
    private JCheckBox saveContPwdCheck;
    private JCheckBox verifyServerCertCheck;
    private JCheckBox namesToUpperBox;
    private JCheckBox useNewAPI;

    private JComboBox authCombo;
    private JComboBox methodCombo;

    private JLabel statusLabel;

    private TransactionIsolationCombobox txCombo;
    private JButton txApplyButton;
    private Border redBorder;
    private Border blackBorder;
    private DocumentListener userNameDocumentListener;
    private DocumentListener passwordDocumentListener;

    List<JComponent> basicComponents;
    List<JComponent> multifactorComponents;
    List<JComponent> jdbcUrlComponents;
    List<JComponent> standardComponents;
    //JPanel basicPanel;
    //JPanel standardPanel;
    //JPanel multifactorPanel;
    //JPanel jdbcUrlPanel;

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

    GridBagHelper gbh;

    private void init() {

        // ---------------------------------
        // create the basic props panel
        basicComponents = new ArrayList<>();
        multifactorComponents = new ArrayList<>();
        jdbcUrlComponents = new ArrayList<>();
        standardComponents = new ArrayList<>();
        gbh = new GridBagHelper();
        redBorder = BorderFactory.createLineBorder(Color.RED);
        blackBorder = new JTextField().getBorder();
        List<String> auth = new ArrayList<>();
        auth.add(bundleString("BasicAu"));
        auth.add("GSS");
        auth.add(bundleString("Multifactor"));
        authCombo = new JComboBox(auth.toArray());
        authCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                checkVisibleComponents();
            }

        });

        List<String> methods = new ArrayList<>();
        methods.add(bundleString("Standard"));
        methods.add("JDBC");
        methodCombo = new JComboBox(methods.toArray());
        methodCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                checkVisibleComponents();
            }
        });

        // initialise the fields
        nameField = createTextField();
        addCheckEmptyField(nameField);
        nameField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {

            }

            @Override
            public void focusLost(FocusEvent e) {
                checkNameUpdate();
            }
        });
        passwordField = createPasswordField();
        passwordDocumentListener = addCheckEmptyField(passwordField);
        hostField = createTextField();
        addCheckEmptyField(hostField);
        hostField.setText("localhost");
        portField = createNumberTextField();
        addCheckEmptyField(portField);
        portField.setText("3050");
        sourceField = createMatchedWidthTextField();
        addCheckEmptyField(sourceField);
        roleField = createTextField();
        userField = createTextField();
        userNameDocumentListener = addCheckEmptyField(userField);
        urlField = createMatchedWidthTextField();
        nameField.setPreferredSize(hostField.getPreferredSize());
        hostField.setPreferredSize(hostField.getPreferredSize());
        portField.setPreferredSize(hostField.getPreferredSize());
        userField.setPreferredSize(hostField.getPreferredSize());
        passwordField.setPreferredSize(hostField.getPreferredSize());

        certificateFileField = createMatchedWidthTextField();
        containerPasswordField = createPasswordField();
        saveContPwdCheck = ActionUtilities.createCheckBox(bundleString("Store-container-password"), "setStoreContainerPassword");
        saveContPwdCheck.addActionListener(this);
        verifyServerCertCheck = ActionUtilities.createCheckBox(bundleString("Verify-server-certificate"), "setVerifyServerCertCheck");
        verifyServerCertCheck.addActionListener(this);

        nameField.addFocusListener(new ConnectionNameFieldListener(this));

        savePwdCheck = ActionUtilities.createCheckBox(bundleString("StorePassword"), "setStorePassword");
        encryptPwdCheck = ActionUtilities.createCheckBox(bundleString("EncryptPassword"), "setEncryptPassword");
        namesToUpperBox = ActionUtilities.createCheckBox(bundleString("namesToUpperCase"), "namesToUpperCase");
        namesToUpperBox.setSelected(true);
        useNewAPI = ActionUtilities.createCheckBox(bundleString("UseNewAPI"), "setNewAPI");

        savePwdCheck.addActionListener(this);
        encryptPwdCheck.addActionListener(this);
        namesToUpperBox.addActionListener(this);
        useNewAPI.addActionListener(this);

        // retrieve the drivers
        buildDriversList();

        // retrieve the available charsets
        loadCharsets();
        charsetsCombo = WidgetFactory.createComboBox(charsets.toArray());

        // ---------------------------------
        // add the basic connection fields

        TextFieldPanel mainPanel = new TextFieldPanel(new GridBagLayout());
        GridBagConstraints gbc_def = new GridBagConstraints();
        gbc_def.fill = GridBagConstraints.HORIZONTAL;
        gbc_def.anchor = GridBagConstraints.NORTHWEST;
        gbc_def.insets = new Insets(5, 10, 10, 10);
        gbc_def.gridy = -1;
        gbc_def.gridx = 0;
        gbh.setDefaults(gbc_def).defaults();
        int fieldWidth = 2;

        gbh.insertEmptyRow(mainPanel, 0);

        statusLabel = new DefaultFieldLabel();
        gbh.addLabelFieldPair(mainPanel, bundleString("statusLabel"),
                statusLabel, bundleString("statusLabel.tool-tip"), true, false, fieldWidth);


        gbh.addLabelFieldPair(mainPanel, bundleString("nameField"),
                nameField, bundleString("nameField.tool-tip"), true, false, fieldWidth);


        JLabel hostLabel = new JLabel(bundleString("hostField"));
        standardComponents.add(hostLabel);
        standardComponents.add(hostField);
        gbh.addLabelFieldPair(mainPanel, hostLabel, hostField, null, true, false, fieldWidth);


        JLabel portLabel = new JLabel(bundleString("portField"));
        standardComponents.add(portLabel);
        standardComponents.add(portField);
        gbh.addLabelFieldPair(mainPanel, portLabel, portField, null, true, false, fieldWidth);


        JButton openFile = new JButton("...");
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

        //JPanel selectFilePanel = new JPanel(new BorderLayout());

        JLabel dataSourceLabel = new JLabel(bundleString("sourceField"));
        standardComponents.add(dataSourceLabel);
        standardComponents.add(sourceField);
        standardComponents.add(openFile);
        mainPanel.add(dataSourceLabel, gbh.nextRowFirstCol().setLabelDefault().get());
        mainPanel.add(sourceField, gbh.nextCol().setMaxWeightX().get());
        mainPanel.add(openFile, gbh.nextCol().setLabelDefault().get());

        standardComponents.addAll(multifactorComponents);
        JLabel charsetLabel = new JLabel(bundleString("CharacterSet"));
        standardComponents.add(charsetLabel);
        standardComponents.add(charsetsCombo);
        gbh.addLabelFieldPair(mainPanel, charsetLabel, charsetsCombo, null, true, false, fieldWidth);

        mainPanel.add(namesToUpperBox, gbh.nextRowFirstCol().setLabelDefault().setWidth(3).get());
        standardComponents.add(namesToUpperBox);

        mainPanel.add(useNewAPI, gbh.nextRowFirstCol().setLabelDefault().setWidth(3).get());
        standardComponents.add(useNewAPI);

        gbh.setY(2).nextCol().makeCurrentXTheDefaultForNewline().setWidth(1).previousCol();

        addDriverFields(mainPanel, gbh);

        gbh.addLabelFieldPair(mainPanel, bundleString("ConnectionParameters"),
                methodCombo, bundleString("ConnectionParameters.tool-tip"), true, true);

        JLabel authLabel = new JLabel(bundleString("Authentication"));
        standardComponents.add(authLabel);
        standardComponents.add(authCombo);
        gbh.addLabelFieldPair(mainPanel, authLabel, authCombo, null, true, true);

        JLabel roleLabel = new JLabel(bundleString("Role"));
        standardComponents.add(roleLabel);
        standardComponents.add(roleField);
        gbh.addLabelFieldPair(mainPanel, roleLabel, roleField, null, true, true);

        JLabel userLabel = new JLabel(bundleString("userField"));
        basicComponents.add(userLabel);
        basicComponents.add(userField);
        gbh.addLabelFieldPair(mainPanel, userLabel, userField, null, true, true);

        JLabel passwordLabel = new JLabel(bundleString("passwordField"));
        basicComponents.add(passwordLabel);
        basicComponents.add(passwordField);
        gbh.addLabelFieldPair(mainPanel, passwordLabel, passwordField, null, true, true);


        JButton showPassword = new LinkButton(bundleString("ShowPassword"));
        showPassword.setActionCommand("showPassword");
        showPassword.addActionListener(this);

        JPanel passwordOptionsPanel = new JPanel(new GridBagLayout());
        addComponents(passwordOptionsPanel,
                new ComponentToolTipPair(savePwdCheck, bundleString("StorePassword.tool-tip")),
                new ComponentToolTipPair(encryptPwdCheck, bundleString("EncryptPassword.tool-tip")),
                new ComponentToolTipPair(showPassword, bundleString("ShowPassword.tool-tip")));

        basicComponents.add(passwordOptionsPanel);
        mainPanel.add(passwordOptionsPanel, gbh.nextRowFirstCol().fillHorizontally().setMaxWeightX().setWidth(2).get());

        JLabel contLabel = new JLabel(bundleString("contLabel"));
        multifactorComponents.add(contLabel);
        multifactorComponents.add(containerPasswordField);
        gbh.addLabelFieldPair(mainPanel, contLabel, containerPasswordField, null, true, true);

        JLabel certLabel = new JLabel(bundleString("certLabel"));
        mainPanel.add(certLabel, gbh.nextRowFirstCol().setLabelDefault().get());
        multifactorComponents.add(certLabel);
        mainPanel.add(certificateFileField, gbh.nextCol().setMaxWeightX().get());
        multifactorComponents.add(certificateFileField);

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(
                new FileNameExtensionFilter("Certificate file X.509 (CER, DER)", "cer", "der"));

        JButton openCertFile = new JButton(bundleString("ChooseFile"));
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

        mainPanel.add(openCertFile, gbh.nextColWidth().setLabelDefault().get());
        multifactorComponents.add(openCertFile);

        mainPanel.add(saveContPwdCheck, gbh.nextRowFirstCol().setLabelDefault().get());
        multifactorComponents.add(saveContPwdCheck);

        mainPanel.add(verifyServerCertCheck, gbh.nextCol().setLabelDefault().get());
        multifactorComponents.add(verifyServerCertCheck);

        JLabel urlLabel = new JLabel(bundleString("urlField"));

        gbh.resetDefaultX();

        mainPanel.add(urlLabel, gbh.nextRowFirstCol().setLabelDefault().get());
        jdbcUrlComponents.add(urlLabel);

        mainPanel.add(urlField, gbh.nextCol().spanX().get());
        jdbcUrlComponents.add(urlField);

        standardComponents.addAll(jdbcUrlComponents);


        JButton testButton = createButton(Bundles.getCommon("test.button"), "test", -1);
        connectButton = createButton(Bundles.getCommon("connect.button"), CONNECT_ACTION_COMMAND, 'T');
        disconnectButton = createButton(Bundles.getCommon("disconnect.button"), "disconnect", 'D');

        JPanel buttons = new JPanel(new GridBagLayout());
        GridBagHelper gbh_b = new GridBagHelper();
        gbh_b.setDefaults(gbh.getDefaultConstraints());
        mainPanel.add(buttons, gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
        gbh_b.leftGap(0).rightGap(15).setWeightX(0.1);
        buttons.add(connectButton, gbh_b.nextRowFirstCol().setWidth(1).anchorNorthWest().fillHorizontally().spanY().get());
        buttons.add(disconnectButton, gbh_b.nextCol().get());
        buttons.add(testButton, gbh_b.nextCol().setWidth(1).get());
        buttons.add(new JPanel(), gbh_b.setMaxWeightX().fillHorizontally().setWidth(4).nextCol().anchorNorthWest().get());


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
        gbh.setXY(0, 0).setWidth(1).setLabelDefault();
        advPropsPanel.add(
                new DefaultFieldLabel(bundleString("advPropsPanel.text1")), gbh.get());
        gbh.nextRowFirstCol().setLabelDefault();
        advPropsPanel.add(
                new DefaultFieldLabel(bundleString("advPropsPanel.text2")), gbh.get());
        gbh.nextRowFirstCol().spanX().spanY().fillBoth();
        advPropsPanel.add(scroller, gbh.get());

        // transaction isolation
        txApplyButton = WidgetFactory.createButton(Bundles.get("common.apply.button"));
        txApplyButton.setActionCommand("transactionLevelChanged");
        txApplyButton.setToolTipText(bundleString("txApplyButton.tool-tip"));
        txApplyButton.setEnabled(false);
        txApplyButton.addActionListener(this);

        txCombo = new TransactionIsolationCombobox();

        JPanel advTxPanel = new JPanel(new GridBagLayout());
        advTxPanel.setBorder(BorderFactory.createTitledBorder(bundleString("TransactionIsolation")));
        gbh.setXY(0, 0).setLabelDefault().setWidth(2);
        advTxPanel.add(
                new DefaultFieldLabel(bundleString("advTxPanel.Text1")), gbh.get());
        gbh.nextRow();
        advTxPanel.add(
                new DefaultFieldLabel(bundleString("advTxPanel.Text2")), gbh.get());
        gbh.nextRowFirstCol().setLabelDefault();
        advTxPanel.add(new DefaultFieldLabel(bundleString("IsolationLevel")), gbh.get());
        gbh.nextCol().setWeightX(1).fillHorizontally();
        advTxPanel.add(txCombo, gbh.get());
        gbh.setLabelDefault().nextCol();
        advTxPanel.add(txApplyButton, gbh.get());

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
        standardComponents.addAll(basicComponents);
        checkVisibleComponents();
    }

    private void checkVisibleComponents() {
        Object selectedItem = methodCombo.getSelectedItem();
        if (selectedItem.toString().equalsIgnoreCase(bundleString("Standard"))) {
            setVisibleComponents(standardComponents, true);
            setVisibleComponents(jdbcUrlComponents, false);
            selectedItem = authCombo.getSelectedItem();
            if (selectedItem.toString().equalsIgnoreCase(bundleString("BasicAu"))) {
                if (userNameDocumentListener == null) {
                    userNameDocumentListener = addCheckEmptyField(userField);
                }
                if (passwordDocumentListener == null) {
                    passwordDocumentListener = addCheckEmptyField(passwordField);
                }
                setVisibleComponents(basicComponents, true);
                setVisibleComponents(multifactorComponents, false);
            } else if (selectedItem.toString().equalsIgnoreCase("gss")) {
                setVisibleComponents(basicComponents, false);
                setVisibleComponents(multifactorComponents, false);
            } else if (selectedItem.toString().equalsIgnoreCase(bundleString("Multifactor"))) {
                if (userNameDocumentListener != null) {
                    userField.getDocument().removeDocumentListener(userNameDocumentListener);
                    userField.setBorder(blackBorder);
                    userNameDocumentListener = null;
                }
                if (passwordDocumentListener != null) {
                    passwordField.getDocument().removeDocumentListener(passwordDocumentListener);
                    passwordField.setBorder(blackBorder);
                    passwordDocumentListener = null;
                }
                setVisibleComponents(basicComponents, true);
                setVisibleComponents(multifactorComponents, true);
            }

        } else if (selectedItem.toString().equalsIgnoreCase("jdbc")) {
            setVisibleComponents(standardComponents, false);
            setVisibleComponents(jdbcUrlComponents, true);
        }
    }

    private void setVisibleComponents(List<JComponent> components, boolean flag) {
        for (int i = 0; i < components.size(); i++) {
            components.get(i).setVisible(flag);
        }
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

        JTextField textField = new JTextField() {
            public Dimension getPreferredSize() {
                return nameField.getPreferredSize();
            }

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

        JButton button = new JButton(text);
        button.setActionCommand(actionCommand);
        button.setMnemonic(mnemonic);
        button.addActionListener(this);
        //button.applyMaximumSize();

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

        String[] driverNames = new String[size];

        for (int i = 0; i < size; i++) {

            driverNames[i] = jdbcDrivers.get(i).toString();
        }

        if (driverCombo == null) {

            DynamicComboBoxModel comboModel = new DynamicComboBoxModel();
            comboModel.setElements(driverNames);
            driverCombo = WidgetFactory.createComboBox(comboModel);
            driverCombo.setBorder(redBorder);
            driverCombo.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        if (driverCombo.getSelectedIndex() >= 0)
                            driverCombo.setBorder(null);
                        else driverCombo.setBorder(redBorder);
                    }
                }
            });

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
        return databaseConnectionRepository().nameExists(databaseConnection, name);

    }

    private DatabaseConnectionRepository databaseConnectionRepository() {

        return (DatabaseConnectionRepository) RepositoryCache.load(
                DatabaseConnectionRepository.REPOSITORY_ID);
    }


    /**
     * Acion implementation on selection of the Connect button.
     */
    public void connect() {

        if (!valid()) {

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
            }

        } catch (DataSourceException e) {

            connectionError(e);

        } finally {

            GUIUtilities.showNormalCursor();
        }

    }

    public void test() {

        if (!valid()) {

            return;
        }

        populateAndSave();

        try {

            GUIUtilities.showWaitCursor();
            if (new ConnectionTester().test(databaseConnection)) {

                GUIUtilities.displayInformationMessage(bundleString("test.success"));
            }

        } catch (DataSourceException e) {

            connectionError(e);

        } finally {

            GUIUtilities.showNormalCursor();
        }

    }

    private void connectionError(DataSourceException e) {

        StringBuilder sb = new StringBuilder();
        sb.append(Bundles.getCommon("error.connection"));
        sb.append(e.getExtendedMessage());
        GUIUtilities.displayExceptionErrorDialog(sb.toString(), e);
    }

    private boolean valid() {

        if (databaseConnection.isConnected()) {

            return false;
        }

        // make sure a name has been entered
        if (StringUtils.isBlank(nameField.getText())) {
            GUIUtilities.displayErrorMessage(bundleString("error.emptyName"));
            return false;
        }

        if (connectionNameExists()) {
            focusNameField();
            return false;
        }

        // check a driver is selected
        if (driverCombo.getSelectedIndex() < 0) {

            GUIUtilities.displayErrorMessage(bundleString("error.emptyDriver"));
            return false;
        }

        // check if we have a url - if not check the port is valid
        if (StringUtils.isBlank(urlField.getText())) {

            String port = portField.getText();
            if (!StringUtils.isNumeric(port)) {

                GUIUtilities.displayErrorMessage(bundleString("error.invalidPort"));
                return false;
            }

        }

        return sshTunnelConnectionPanel.canConnect();
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
            GUIUtilities.displayErrorMessage(bundleString("error.nameExist1") + nameField.getText().trim()
                    + bundleString("error.nameExist2"));
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
                        || key.equalsIgnoreCase("isc_dpb_certificate_base64"))
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
                && !properties.containsKey("isc_dpb_multi_factor_auth")
                && authCombo.getSelectedItem().toString().equalsIgnoreCase("multifactor")) {
            properties.setProperty("isc_dpb_trusted_auth", "1");
            properties.setProperty("isc_dpb_multi_factor_auth", "1");
        }
        if (!certificateFileField.getText().isEmpty()
                && authCombo.getSelectedItem().toString().equalsIgnoreCase("multifactor"))
            loadCertificate(properties, certificateFileField.getText());

        if (containerPasswordField.getPassword() != null && containerPasswordField.getPassword().length != 0
                && authCombo.getSelectedItem().toString().equalsIgnoreCase("multifactor"))
            properties.setProperty("isc_dpb_repository_pin", MiscUtils.charsToString(containerPasswordField.getPassword()));

        if (verifyServerCertCheck.isSelected()
                && authCombo.getSelectedItem().toString().equalsIgnoreCase("multifactor"))
            properties.setProperty("isc_dpb_verify_server", "1");

        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pid = name.split("@")[0];
        String path = null;
        if (ApplicationContext.getInstance().getExternalProcessName() != null &&
                !ApplicationContext.getInstance().getExternalProcessName().isEmpty()) {
            path = ApplicationContext.getInstance().getExternalProcessName();
        }
        if (ApplicationContext.getInstance().getExternalPID() != null &&
                !ApplicationContext.getInstance().getExternalPID().isEmpty()) {
            pid = ApplicationContext.getInstance().getExternalPID();
        }
        properties.setProperty("process_id", pid);
        try {
            if (path == null)
                path = ExecuteQuery.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            properties.setProperty("process_name", path);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        databaseConnection.setJdbcProperties(properties);
    }

    /**
     * Loads the certificate from a file. If it is in the der format,
     * it converts it to base64.
     *
     * @param properties connection properties
     * @param certificatePath path to x509 certificate file
     */
    private void loadCertificate(Properties properties, String certificatePath) {
        try {
            byte[] bytes = FileUtils.readBytes(new File(certificatePath));
            String base64cert = new String(bytes);

            if (checkBase64Format(base64cert)) {
                // If the certificate is in the BASE64 format, then add to properties
                properties.setProperty("isc_dpb_certificate_base64", base64cert);
            } else {
                // Convert from the DER to BASE64
                base64cert = Base64.encodeBytes(bytes);
                StringBuilder sb = new StringBuilder();
                sb.append("-----BEGIN CERTIFICATE-----");
                sb.append("\n");
                sb.append(base64cert);
                sb.append("\n");
                sb.append("-----END CERTIFICATE-----");
                base64cert = sb.toString();
                properties.setProperty("isc_dpb_certificate_base64", base64cert);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return true if the certificate is in the BASE64 format, otherwise false.
     *
     * @param certificate certificate body
     */
    private boolean checkBase64Format(String certificate) {
        return StringUtils.contains(certificate, "-----BEGIN CERTIFICATE-----") ? true : false;
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
                    && !name.equalsIgnoreCase("isc_dpb_certificate_base64")
                    && !name.equalsIgnoreCase("isc_dpb_repository_pin")
                    && !name.equalsIgnoreCase("isc_dpb_verify_server")) {
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

            statusLabel.setText(bundleString("status.Connected"));

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

    public void namesToUpperCase() {

        boolean store = namesToUpperBox.isSelected();
        databaseConnection.setNamesToUpperCase(store);
    }

    public void setStoreContainerPassword() {

        boolean store = saveContPwdCheck.isSelected();
        databaseConnection.setContainerPasswordStored(store);
    }

    public void setVerifyServerCertCheck() {

        boolean store = verifyServerCertCheck.isSelected();
        databaseConnection.setVerifyServerCertCheck(store);
    }


    public void setNewAPI() {

        boolean useAPI = useNewAPI.isSelected();
        int majorVersion = new DefaultDriverLoader().load(jdbcDrivers.get(driverCombo.getSelectedIndex())).getMajorVersion();
        if (majorVersion < 4 && useAPI) {
            GUIUtilities.displayWarningMessage(bundleString("warning.useNewAPI"));
            useNewAPI.setSelected(false);
            databaseConnection.setUseNewAPI(false);
        } else {
            if (databaseConnection != null)
                databaseConnection.setUseNewAPI(useAPI);
        }
    }

    /**
     * Sets the values for the tx level on the connection object
     * based on the tx level in the tx combo.
     */
    private void getTransactionIsolationLevel() {

        int isolationLevel = txCombo.getSelectedLevel();
        databaseConnection.setTransactionIsolation(isolationLevel);
    }

    /**
     * Sets the values for the tx level on the tx combo
     * based on the tx level in the connection object.
     */
    private void setTransactionIsolationLevel() {
        int isolationLevel = databaseConnection.getTransactionIsolation();
        txCombo.setSelectedLevel(isolationLevel);
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

        setNewAPI();
    }

    /**
     * Populates the values of the fields with the values of
     * the specified connection.
     */
    private void populateConnectionFields(DatabaseConnection databaseConnection) {

        // rebuild the driver list
        buildDriversList();

        // populate the field values/selections
        saveContPwdCheck.setSelected(databaseConnection.isContainerPasswordStored());
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
        certificateFileField.setText(databaseConnection.getCertificate());
        containerPasswordField.setText(databaseConnection.getContainerPassword());
        verifyServerCertCheck.setSelected(databaseConnection.isVerifyServerCertCheck());
        useNewAPI.setSelected(databaseConnection.useNewAPI());
        authCombo.setSelectedItem(databaseConnection.getAuthMethod());
        methodCombo.setSelectedItem(databaseConnection.getConnectionMethod());
        namesToUpperBox.setSelected(databaseConnection.isNamesToUpperCase());

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
        databaseConnection.setCertificate(certificateFileField.getText());
        databaseConnection.setContainerPassword(MiscUtils.charsToString(containerPasswordField.getPassword()));
        databaseConnection.setContainerPasswordStored(saveContPwdCheck.isSelected());
        databaseConnection.setVerifyServerCertCheck(verifyServerCertCheck.isSelected());
        databaseConnection.setCharset(charsetsCombo.getSelectedItem().toString());
        databaseConnection.setAuthMethod(authCombo.getSelectedItem().toString());
        databaseConnection.setConnectionMethod(methodCombo.getSelectedItem().toString());
        databaseConnection.setNamesToUpperCase(namesToUpperBox.isSelected());
        // jdbc driver selection
        int driverIndex = driverCombo.getSelectedIndex();
        if (driverIndex >= jdbcDrivers.size() + 1) {

            driverIndex = jdbcDrivers.size();

            driverCombo.setSelectedIndex(driverIndex);
        }

        if (driverIndex >= 0) {

            DatabaseDriver driver = jdbcDrivers.get(driverIndex);

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
        //checkNameUpdate();
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

    private DocumentListener addCheckEmptyField(JTextField field) {
        checkEmptyTextField(field);
        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkEmptyTextField(field);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkEmptyTextField(field);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkEmptyTextField(field);
            }
        };
        field.getDocument().addDocumentListener(documentListener);
        return documentListener;
    }

    private void checkEmptyTextField(JTextField field) {
        if (field.getText().isEmpty()) {
            field.setBorder(redBorder);
        } else field.setBorder(blackBorder);
        repaint();
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

    private void addDriverFields(JPanel panel, GridBagHelper gbh) {
        gbh.nextCol().setLabelDefault();
        panel.add(new JLabel(bundleString("driverField")), gbh.get());
        panel.add(driverCombo, gbh.nextCol().fillHorizontally().setMaxWeightX().get());
        driverCombo.setToolTipText(bundleString("driverField.tool-tip"));
        JButton button = new JButton(bundleString("addNewDriver"));
        button.setActionCommand("addNewDriver");
        button.addActionListener(this);
        button.setMnemonic('r');
        gbh.nextCol().setLabelDefault();
        panel.add(button, gbh.get());


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
            button = new DefaultButton();
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








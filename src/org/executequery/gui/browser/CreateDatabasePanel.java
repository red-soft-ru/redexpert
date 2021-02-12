package org.executequery.gui.browser;

import biz.redsoft.IFBCreateDatabase;
import biz.redsoft.IFBCryptoPluginInit;
import org.apache.commons.lang.StringUtils;
import org.executequery.*;
import org.executequery.components.FileChooserDialog;
import org.executequery.components.TextFieldPanel;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseConnectionFactory;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.databasemediators.spi.DatabaseConnectionFactoryImpl;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.datasource.ConnectionManager;
import org.executequery.event.*;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.drivers.DialogDriverPanel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.DatabaseDriverRepository;
import org.executequery.repository.RepositoryCache;
import org.executequery.util.Base64;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.*;
import org.underworldlabs.swing.actions.ActionUtilities;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.DynamicLibraryLoader;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
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
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * Created by vasiliy.yashkov on 10.07.2015.
 */
public class CreateDatabasePanel extends ActionPanel
        implements DatabaseDriverListener {

    // -------------------------------
    // text fields and combos

    /**
     * This objects title as an internal frame
     */
    public static final String TITLE = Bundles.get(ConnectionPanel.class, "CreateDatabase");

    /**
     * This objects icon as an internal frame
     */
    public static final String FRAME_ICON = "create_database16.png";

    private static final String CREATE_ACTION_COMMAND = "create";

    protected GradientLabel gradientLabel;

    private java.util.List<String> charsets;

    private JComboBox driverCombo;
    private JCheckBox encryptPwdCheck;
    private JCheckBox savePwdCheck;

    private JTextField nameField;
    private JTextField userField;
    private JPasswordField passwordField;
    private JTextField hostField;
    private NumberTextField portField;
    private JTextField sourceField;

    private JComboBox charsetsCombo;
    private JComboBox pageSizeCombo;

    private List<String> pageSizes;

    private JComboBox txCombo;
    private JButton txApplyButton;

    // -------------------------------

    /**
     * table model for jdbc properties key/values
     */
    private JdbcPropertiesTableModel model;

    /**
     * connect button
     */
    private JButton createButton;

    /**
     * the saved jdbc drivers
     */
    private java.util.List<DatabaseDriver> jdbcDrivers;

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
    private final BrowserController controller;

    /**
     * Creates a new instance of ConnectionPanel
     */


    private List<JComponent> multifactorComponents;
    private List<JComponent> basicComponents;

    private JTextField certificateFileField;
    private JPasswordField containerPasswordField;
    private JCheckBox saveContPwdCheck;
    private JCheckBox verifyServerCertCheck;

    private JComboBox authCombo;

    public CreateDatabasePanel(BrowserController controller) {
        super(new BorderLayout());
        this.controller = controller;
        init();
    }

    GridBagHelper gbh;

    private void init() {


        multifactorComponents = new ArrayList<>();
        basicComponents = new ArrayList<>();
        gbh = new GridBagHelper();

        List<String> auth = new ArrayList<>();
        auth.add(bundledString("BasicAu"));
        auth.add("GSS");
        auth.add(bundledString("Multifactor"));
        authCombo = new JComboBox(auth.toArray());
        authCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                checkVisibleComponents();
            }

        });

        gradientLabel = new GradientLabel();
        gradientLabel.setText(bundledString("CreateDatabase"));

        add(gradientLabel, BorderLayout.NORTH);

        pageSizes = new ArrayList<>();
        pageSizes.add("4096");
        pageSizes.add("8192");
        pageSizes.add("16384");
        // ---------------------------------
        // create the basic props panel

        // initialise the fields
        nameField = createTextField();
        nameField.setName("nameField");
        passwordField = createPasswordField();
        passwordField.setName("passwordField");
        hostField = createTextField();
        hostField.setName("hostField");
        portField = createNumberTextField();
        portField.setName("portField");
        sourceField = createMatchedWidthTextField();
        sourceField.setName("sourceField");
        userField = createTextField();
        userField.setName("userField");

        hostField.setText("localhost");
        portField.setText("3050");

        savePwdCheck = ActionUtilities.createCheckBox(bundledString("StorePassword"), "setStorePassword");
        encryptPwdCheck = ActionUtilities.createCheckBox(bundledString("EncryptPassword"), "setEncryptPassword");

        savePwdCheck.addActionListener(this);
        encryptPwdCheck.addActionListener(this);

        certificateFileField = createMatchedWidthTextField();
        containerPasswordField = createPasswordField();
        saveContPwdCheck = ActionUtilities.createCheckBox(bundledString("Store-container-password"), "setStoreContainerPassword");
        saveContPwdCheck.addActionListener(this);
        verifyServerCertCheck = ActionUtilities.createCheckBox(bundledString("Verify-server-certificate"), "setVerifyServerCertCheck");
        verifyServerCertCheck.addActionListener(this);

        // retrieve the drivers
        buildDriversList();

        // retrieve the available charsets
        loadCharsets();
        charsetsCombo = WidgetFactory.createComboBox(charsets.toArray());
        charsetsCombo.setName("charsetsCombo");

        pageSizeCombo = WidgetFactory.createComboBox(pageSizes.toArray());
        pageSizeCombo.setSelectedItem("8192");
        pageSizeCombo.setEditable(false);

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

        gbh.addLabelFieldPair(mainPanel, bundledString("nameField"),
                nameField, bundleString("nameField.tool-tip"), true, false, fieldWidth);


        JLabel hostLabel = new JLabel(bundledString("hostField"));
        gbh.addLabelFieldPair(mainPanel, hostLabel, hostField, null, true, false, fieldWidth);


        JLabel portLabel = new JLabel(bundledString("portField"));
        gbh.addLabelFieldPair(mainPanel, portLabel, portField, null, true, false, fieldWidth);


        JButton saveFile = new JButton("...");
        saveFile.addActionListener(new ActionListener() {
            final FileChooserDialog fileChooser = new FileChooserDialog();

            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showSaveDialog(saveFile);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    sourceField.setText(file.getAbsolutePath());
                }
            }
        });

        JLabel dataSourceLabel = new DefaultFieldLabel(bundledString("sourceField"));
        mainPanel.add(dataSourceLabel, gbh.nextRowFirstCol().setLabelDefault().get());
        mainPanel.add(sourceField, gbh.nextCol().setMaxWeightX().get());
        mainPanel.add(saveFile, gbh.nextCol().setLabelDefault().get());


        JLabel charsetLabel = new JLabel(bundledString("CharacterSet"));

        gbh.addLabelFieldPair(mainPanel, charsetLabel, charsetsCombo, null, true, false, fieldWidth);

        gbh.addLabelFieldPair(mainPanel, bundledString("PageSize"), pageSizeCombo, null, true, false, fieldWidth);

        gbh.setY(2).nextCol().makeCurrentXTheDefaultForNewline().setWidth(1).previousCol();

        addDriverFields(mainPanel, gbh);

        JLabel authLabel = new JLabel(bundledString("Authentication"));

        gbh.addLabelFieldPair(mainPanel, authLabel, authCombo, null, true, true);


        JLabel userLabel = new JLabel(bundledString("userField"));
        basicComponents.add(userLabel);
        basicComponents.add(userField);
        gbh.addLabelFieldPair(mainPanel, userLabel, userField, null, true, true);

        JLabel passwordLabel = new JLabel(bundledString("passwordField"));
        basicComponents.add(passwordLabel);
        basicComponents.add(passwordField);
        gbh.addLabelFieldPair(mainPanel, passwordLabel, passwordField, null, true, true);


        JButton showPassword = new LinkButton(bundledString("ShowPassword"));
        showPassword.setActionCommand("showPassword");
        showPassword.addActionListener(this);

        JPanel passwordOptionsPanel = new JPanel(new GridBagLayout());
        addComponents(passwordOptionsPanel,
                new ComponentToolTipPair(savePwdCheck, bundledString("StorePassword.tool-tip")),
                new ComponentToolTipPair(encryptPwdCheck, bundledString("EncryptPassword.tool-tip")),
                new ComponentToolTipPair(showPassword, bundledString("ShowPassword.tool-tip")));

        basicComponents.add(passwordOptionsPanel);
        mainPanel.add(passwordOptionsPanel, gbh.nextRowFirstCol().fillHorizontally().setMaxWeightX().setWidth(2).get());

        JLabel contLabel = new JLabel(bundledString("contLabel"));
        multifactorComponents.add(contLabel);
        multifactorComponents.add(containerPasswordField);
        gbh.addLabelFieldPair(mainPanel, contLabel, containerPasswordField, null, true, true);

        JLabel certLabel = new JLabel(bundledString("certLabel"));
        mainPanel.add(certLabel, gbh.nextRowFirstCol().setLabelDefault().get());
        multifactorComponents.add(certLabel);
        mainPanel.add(certificateFileField, gbh.nextCol().setMaxWeightX().get());
        multifactorComponents.add(certificateFileField);

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(
                new FileNameExtensionFilter("Certificate file X.509 (CER, DER)", "cer", "der"));

        JButton openCertFile = new JButton(bundledString("ChooseFile"));
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
        gbh.resetDefaultX();

        createButton = createButton(bundledString("Create"), CREATE_ACTION_COMMAND, 'T');
        mainPanel.add(createButton, gbh.nextRowFirstCol().setWidth(1).anchorNorthWest().setLabelDefault().spanY().get());

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
                new DefaultFieldLabel(bundledString("advPropsPanel.text1")), gbh.get());
        gbh.nextRowFirstCol().setLabelDefault();
        advPropsPanel.add(
                new DefaultFieldLabel(bundledString("advPropsPanel.text2")), gbh.get());
        gbh.nextRowFirstCol().spanX().spanY().fillBoth();
        advPropsPanel.add(scroller, gbh.get());

        // transaction isolation
        txApplyButton = WidgetFactory.createInlineFieldButton(Bundles.get("common.apply.button"), "transactionLevelChanged");
        txApplyButton.setToolTipText(bundledString("txApplyButton.tool-tip"));
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
        advTxPanel.setBorder(BorderFactory.createTitledBorder(bundledString("TransactionIsolation")));
        gbh.setXY(0, 0).setLabelDefault().setWidth(2);
        advTxPanel.add(
                new DefaultFieldLabel(bundledString("advTxPanel.Text1")), gbh.get());
        gbh.nextRow();
        advTxPanel.add(
                new DefaultFieldLabel(bundledString("advTxPanel.Text2")), gbh.get());
        gbh.nextRowFirstCol().setLabelDefault();
        advTxPanel.add(new DefaultFieldLabel(bundledString("IsolationLevel")), gbh.get());
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

        tabPane = new JTabbedPane(JTabbedPane.BOTTOM);
        tabPane.addTab(bundledString("Basic"), scrollPane);
        tabPane.addTab(bundledString("Advanced"), advancedPanel);

        add(tabPane, BorderLayout.CENTER);

        EventMediator.registerListener(this);
        checkVisibleComponents();
    }

    private void checkVisibleComponents() {
        Object selectedItem = authCombo.getSelectedItem();
        if (selectedItem.toString().equalsIgnoreCase(bundledString("BasicAu"))) {
            setVisibleComponents(basicComponents, true);
            setVisibleComponents(multifactorComponents, false);
        } else if (selectedItem.toString().equalsIgnoreCase("gss")) {
            setVisibleComponents(basicComponents, false);
            setVisibleComponents(multifactorComponents, false);
        } else if (selectedItem.toString().equalsIgnoreCase(bundledString("Multifactor"))) {
            setVisibleComponents(basicComponents, true);
            setVisibleComponents(multifactorComponents, true);
        }
    }

    private void setVisibleComponents(List<JComponent> components, boolean flag) {
        for (int i = 0; i < components.size(); i++) {
            components.get(i).setVisible(flag);
        }
    }

    public void setVerifyServerCertCheck() {

        /*boolean store = verifyServerCertCheck.isSelected();
        databaseConnection.setVerifyServerCertCheck(store);*/
    }

    public void setStoreContainerPassword() {
    }

    private void loadCharsets() {
        Properties props;
        try {
            if (charsets == null)
                charsets = new ArrayList<String>();
            else
                charsets.clear();

            String resource = FileUtils.loadResource("org/executequery/charsets.properties");
            String[] strings = resource.split("\n");
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
        textField.setActionCommand(CREATE_ACTION_COMMAND);
        textField.addActionListener(this);
    }

    private JButton createButton(String text, String actionCommand, int mnemonic) {

        JButton button = new JButton(text);
        button.setActionCommand(actionCommand);
        button.setMnemonic(mnemonic);
        button.addActionListener(this);

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

    private java.util.List<DatabaseDriver> loadDrivers() {

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
            driverCombo.setName("driverCombo");

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
                        "The transaction isolation level " + txLevel +
                                " was applied successfully.");
            } catch (DataSourceException e) {
                GUIUtilities.displayWarningMessage(
                        "The selected isolation level could not be applied.\n" +
                                "The JDBC driver returned:\n\n" +
                                e.getMessage() + "\n\n");
            } catch (Exception e) {
            }
        } else {
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
        ConnectionManager.
                setTransactionIsolationLevel(databaseConnection, isolationLevel);

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

        return (DatabaseConnectionRepository) RepositoryCache.load(
                DatabaseConnectionRepository.REPOSITORY_ID);
    }

    /**
     * Acion implementation on selection of the Connect button.
     */
    public void create() {
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
        if (driverCombo.getSelectedIndex() < 0) {
            GUIUtilities.displayErrorMessage("You must select a driver");
            return;
        }
        // otherwise - good to proceed

        // populate the object with field values
        //populateConnectionObject();

        populateAndSave();

        GUIUtilities.showWaitCursor();

        // get driver
        DatabaseDriver databaseDriver = null;
        for (DatabaseDriver dd : jdbcDrivers) {
            if (dd.getName().equals(this.driverCombo.getSelectedItem().toString())) {
                databaseDriver = dd;
                break;
            }
        }

        if (databaseDriver.getClassName().contains("FBDriver"))
            createFirebirdDatabase(databaseDriver);
        else
            GUIUtilities.displayErrorMessage("Creating database for selected driver is not implemented");
    }

    private void storeJdbcProperties(IFBCreateDatabase db) {

        Properties properties = new Properties();

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

        db.setJdbcProperties(properties);
    }

    private boolean checkBase64Format(String certificate) {
        return StringUtils.contains(certificate, "-----BEGIN CERTIFICATE-----") ? true : false;
    }

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


    private void createFirebirdDatabase(DatabaseDriver databaseDriver) {

        String server = this.hostField.getText();
        Integer port = this.portField.getValue();
        if (port == 0) {
            port = 3050;
            this.portField.setValue(3050);
        }
        String path = sourceField.getText();

        URL[] urlDriver = new URL[0];
        Class clazzDriver = null;
        URL[] urls = new URL[0];
        Class clazzdb = null;
        Object odb = null;
        try {
            urlDriver = MiscUtils.loadURLs(databaseDriver.getPath());
            ClassLoader clD = new URLClassLoader(urlDriver);
            clazzDriver = clD.loadClass(databaseDriver.getClassName());
            Object o = clazzDriver.newInstance();
            Driver driver = (Driver) o;

            Log.info("Database creation via jaybird");
            Log.info("Driver version: " + driver.getMajorVersion() + "." + driver.getMinorVersion());

            if (driver.getMajorVersion() == 2) {
                StringBuilder sb = new StringBuilder();
                sb.append("Cannot create database, Jaybird 2.x has no implementation for creation database.");
                GUIUtilities.displayErrorMessage(sb.toString());
                return;
            }

            try {
                Object odb1 = DynamicLibraryLoader.loadingObjectFromClassLoader(driver,
                        "biz.redsoft.FBCryptoPluginInitImpl",
                        "./lib/fbplugin-impl.jar;../lib/fbplugin-impl.jar");
                IFBCryptoPluginInit cryptoPlugin = (IFBCryptoPluginInit) odb1;
                // try to initialize crypto plugin
                cryptoPlugin.init();

            } catch (NoSuchMethodError | Exception | UnsatisfiedLinkError e) {
                Log.warning("Unable to initialize cryptographic plugin. " +
                        "Authentication using cryptographic mechanisms will not be available. " +
                        "Please install the crypto pro library to enable cryptographic modules.");
                //advancedProperties.put("excludeCryptoPlugins", "Multifactor,GostPassword,Certificate");
            }
            odb = DynamicLibraryLoader.loadingObjectFromClassLoader(driver,
                    "biz.redsoft.FBCreateDatabaseImpl",
                    "./lib/fbplugin-impl.jar;../lib/fbplugin-impl.jar");
            ;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | MalformedURLException e) {
            e.printStackTrace();
        }

        IFBCreateDatabase db = (IFBCreateDatabase) odb;
        db.setServer(server);
        db.setPort(port);
        db.setUser(userField.getText());
        db.setPassword(MiscUtils.charsToString(passwordField.getPassword()));
        db.setDatabaseName(path);
        db.setEncoding(charsetsCombo.getSelectedItem().toString());
        db.setPageSize(Integer.valueOf(pageSizeCombo.getSelectedItem().toString()));
        storeJdbcProperties(db);

        try {
            db.exec();

        } catch (UnsatisfiedLinkError linkError) {
            StringBuilder sb = new StringBuilder();
            sb.append("Cannot create database, because fbclient library not found in environment path variable. \n");
            sb.append("Please, add fbclient library to environment path variable.\n");
            sb.append("Example for Windows system: setx path \"%path%;C:\\Program Files (x86)\\RedDatabase\\bin\\\"\n\n");
            sb.append("Example for Linux system: export PATH=$PATH:/opt/RedDatabase/lib\n\n");
            sb.append(linkError.getMessage());
            GUIUtilities.displayExceptionErrorDialog(sb.toString(), linkError);
            return;
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("The connection to the database could not be established.");
            sb.append("\nPlease ensure all required fields have been entered ");
            sb.append("correctly and try again.\n\nThe system returned:\n");
            sb.append(e.getMessage());
            GUIUtilities.displayExceptionErrorDialog(sb.toString(), e);
            return;
        } finally {
            GUIUtilities.showNormalCursor();
            System.gc();
        }
        int result = GUIUtilities.displayYesNoDialog(bundledString("DatabaseRegistration.message"), bundledString("DatabaseRegistration"));
        if (result == JOptionPane.YES_OPTION) {
            DatabaseConnectionFactory databaseConnectionFactory = new DatabaseConnectionFactoryImpl();
            String name = this.nameField.getText();
            ConnectionsTreePanel connectionsTreePanel = (ConnectionsTreePanel) GUIUtilities.
                    getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY);
            DatabaseConnection databaseConnection = databaseConnectionFactory.create(name);
            databaseConnection.setDriverId(databaseDriver.getId());
            databaseConnection.setDriverName(databaseDriver.getName());
            databaseConnection.setJDBCDriver(databaseDriver);
            databaseConnection.setHost(this.hostField.getText());
            databaseConnection.setUserName(this.userField.getText());
            databaseConnection.setPasswordStored(this.savePwdCheck.isSelected());
            databaseConnection.setPasswordEncrypted(this.encryptPwdCheck.isSelected());
            databaseConnection.setPassword(MiscUtils.charsToString(this.passwordField.getPassword()));
            databaseConnection.setPort(this.portField.getStringValue());
            databaseConnection.setSourceName(this.sourceField.getText());
            databaseConnection.setCharset(this.charsetsCombo.getSelectedItem().toString());
            databaseConnection.setCertificate(certificateFileField.getText());
            databaseConnection.setContainerPassword(MiscUtils.charsToString(containerPasswordField.getPassword()));
            databaseConnection.setContainerPasswordStored(saveContPwdCheck.isSelected());
            databaseConnection.setVerifyServerCertCheck(verifyServerCertCheck.isSelected());
            databaseConnection.setAuthMethod(authCombo.getSelectedItem().toString());

            connectionsTreePanel.newConnection(databaseConnection);

            GUIUtilities.closeSelectedCentralPane();
        }
        return;
    }

    public void showPassword() {

        GUIUtilities.displayInformationMessage("Password: " +
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
        enableFields(databaseConnection.isConnected());

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
        } catch (DataSourceException e) {
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
        for (Enumeration<?> i = properties.propertyNames(); i.hasMoreElements(); ) {
            String name = (String) i.nextElement();
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
        createButton.setEnabled(!enable);

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
        //databaseConnection.setSourceName(path);
        databaseConnection.setCertificate(certificateFileField.getText());
        databaseConnection.setContainerPassword(MiscUtils.charsToString(containerPasswordField.getPassword()));
        databaseConnection.setContainerPasswordStored(saveContPwdCheck.isSelected());
        databaseConnection.setVerifyServerCertCheck(verifyServerCertCheck.isSelected());
        databaseConnection.setCharset(charsetsCombo.getSelectedItem().toString());
        databaseConnection.setAuthMethod(authCombo.getSelectedItem().toString());


        // jdbc driver selection
        int driverIndex = driverCombo.getSelectedIndex();
        if (driverIndex >= jdbcDrivers.size() + 1) {

            driverIndex = jdbcDrivers.size();

            driverCombo.setSelectedIndex(driverIndex);
        }

        if (driverIndex > 0) {

            DatabaseDriver driver = jdbcDrivers.get(driverIndex - 1);

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

        createButton.setEnabled(false);

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

    private String bundledString(String key) {
        return Bundles.get(ConnectionPanel.class, key);
    }

    private void addComponents(JPanel panel,
                               ComponentToolTipPair... components) {

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets.bottom = 0;

        int count = 0;
        for (ComponentToolTipPair pair : components) {

            pair.component.setToolTipText(pair.toolTip);

            gbc.gridx++;
            gbc.gridwidth = 1;
            gbc.insets.top = 0;
            gbc.weightx = 0;

            if (count > 0) {

                gbc.insets.left = 15;
            }

            count++;
            if (count == components.length) {

                gbc.weightx = 1.0;
                gbc.insets.right = 5;
            }

            panel.add(pair.component, gbc);
        }

    }

    private void addDriverFields(JPanel panel, GridBagHelper gbh) {

        gbh.nextCol().setLabelDefault();
        panel.add(new DefaultFieldLabel(bundledString("driverField")), gbh.get());
        panel.add(driverCombo, gbh.nextCol().fillHorizontally().setMaxWeightX().get());
        driverCombo.setToolTipText(bundledString("driverField.tool-tip"));
        JButton button = new JButton(bundledString("addNewDriver"));
        button.setActionCommand("addNewDriver");
        button.addActionListener(this);
        button.setMnemonic('r');
        gbh.nextCol().setLabelDefault();
        panel.add(button, gbh.get());

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

    class ComponentToolTipPair {

        final JComponent component;
        final String toolTip;

        public ComponentToolTipPair(JComponent component, String toolTip) {
            this.component = component;
            this.toolTip = toolTip;
        }

    }


    class DeleteButtonEditor extends DefaultCellEditor {

        private final JButton button;
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

            setToolTipText("Clear this key/value pair");
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            return this;
        }

    } // DeleteButtonRenderer


}

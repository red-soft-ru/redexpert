/*
 * AbstractConnectionPanel.java
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

import biz.redsoft.IFBCreateDatabase;
import org.apache.commons.lang.StringUtils;
import org.executequery.ApplicationContext;
import org.executequery.EventMediator;
import org.executequery.ExecuteQuery;
import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.DatabaseDriverEvent;
import org.executequery.event.DatabaseDriverListener;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.connection.AdvancedPropertiesPanel;
import org.executequery.gui.drivers.CreateDriverDialog;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.DatabaseDriverRepository;
import org.executequery.repository.Repository;
import org.executequery.repository.RepositoryCache;
import org.executequery.util.Base64;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.ViewablePasswordField;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.listener.RequiredFieldPainter;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

public abstract class AbstractConnectionPanel extends JPanel
        implements DatabaseDriverListener, KeyListener {

    private static final String GSS_AUTH = "GSS";
    private static final String BASIC_AUTH = bundleString("BasicAu");
    private static final String MULTIFACTOR_AUTH = bundleString("Multifactor");

    protected static final String OLD_SERVER = "Red Database (Firebird) 2.X";
    protected static final String NEW_SERVER = "Red Database (Firebird) 3+";

    private boolean driverChangeEnable;

    // ---

    protected DatabaseHost host;
    protected DatabaseConnection connection;
    protected final BrowserController controller;

    protected List<String> charsets;
    protected List<DatabaseDriver> jdbcDrivers;
    protected final List<JComponent> basicAuthComponents;

    // --- gui components ---

    protected JButton addDriverButton;
    protected JButton browseFileButton;
    protected JButton browseCertFileButton;

    protected JComboBox<String> authCombo;
    protected JComboBox<String> serverCombo;
    protected JComboBox<String> charsetsCombo;
    protected JComboBox<DatabaseDriver> driverCombo;

    protected JCheckBox verifyCertCheck;
    protected JCheckBox storePasswordCheck;
    protected JCheckBox encryptPasswordCheck;
    protected JCheckBox storeContPasswordCheck;

    protected JTextField nameField;
    protected JTextField hostField;
    protected JTextField fileField;
    protected JTextField userField;
    protected JTextField certField;
    protected NumberTextField portField;

    protected ViewablePasswordField userPasswordField;
    protected ViewablePasswordField contPasswordField;

    protected JLabel authLabel;

    protected JTabbedPane tabPane;
    protected JPanel multifactorPanel;
    protected AdvancedPropertiesPanel propertiesPanel;

    protected RequiredFieldPainter hostRequire;
    protected RequiredFieldPainter portRequire;

    private RequiredFieldPainter certRequire;
    private RequiredFieldPainter userRequire;
    private RequiredFieldPainter userPasswordRequire;
    private List<RequiredFieldPainter> requiredPainters;

    // ---

    public AbstractConnectionPanel(BrowserController controller) {
        super(new GridBagLayout());
        this.controller = controller;
        this.driverChangeEnable = true;
        this.basicAuthComponents = new ArrayList<>();

        loadCharsets();
        loadDrivers();

        init();
        arrange();
        addRequired();
        addListeners();
        initComponentsLists();
        updateVisibleComponents();

        EventMediator.registerListener(this);
    }

    protected void init() {

        String[] availableServers = new String[]{OLD_SERVER, NEW_SERVER};
        String[] authMethods = new String[]{BASIC_AUTH, GSS_AUTH, MULTIFACTOR_AUTH};

        tabPane = WidgetFactory.createTabbedPane("tabPane");
        propertiesPanel = new AdvancedPropertiesPanel(connection, controller);

        // --- buttons ---

        addDriverButton = WidgetFactory.createButton(
                "addDriverButton",
                bundleString("addNewDriver"),
                e -> addDriver()
        );

        browseFileButton = WidgetFactory.createButton(
                "browseFileButton",
                Bundles.get("common.browse.button"),
                e -> browseFile(fileField)
        );

        browseCertFileButton = WidgetFactory.createButton(
                "browseCertFileButton",
                Bundles.get("common.browse.button"),
                e -> browseFile(certField)
        );

        // --- combo boxes ---

        authCombo = WidgetFactory.createComboBox("authCombo", authMethods);
        driverCombo = WidgetFactory.createComboBox("driverCombo", jdbcDrivers);
        charsetsCombo = WidgetFactory.createComboBox("charsetsCombo", charsets);
        serverCombo = WidgetFactory.createComboBox("serverCombo", availableServers);

        // --- text fields ---

        nameField = WidgetFactory.createTextField("nameField");
        fileField = WidgetFactory.createTextField("fileField");
        userField = WidgetFactory.createTextField("userField");
        certField = WidgetFactory.createTextField("certField");
        hostField = WidgetFactory.createTextField("hostField", "localhost");

        portField = WidgetFactory.createNumberTextField("portField", "3050");
        userPasswordField = WidgetFactory.createViewablePasswordField("userPasswordField");
        contPasswordField = WidgetFactory.createViewablePasswordField("contPasswordField");

        // --- check boxes ---

        storePasswordCheck = WidgetFactory.createCheckBox("storePasswordCheck", bundleString("StorePassword"));
        encryptPasswordCheck = WidgetFactory.createCheckBox("encryptPasswordCheck", bundleString("EncryptPassword"));
        verifyCertCheck = WidgetFactory.createCheckBox("verifyCertCheck", bundleString("Verify-server-certificate"));
        storeContPasswordCheck = WidgetFactory.createCheckBox("storeContPasswordCheck", bundleString("Store-container-password"));

        // ---

        authLabel = WidgetFactory.createLabel(bundleString("authCombo"));
    }

    protected void arrange() {
        GridBagHelper gbh;

        // --- multifactor panel ---

        multifactorPanel = WidgetFactory.createPanel("multifactorPanel");
        multifactorPanel.setBorder(BorderFactory.createTitledBorder(bundleString("multifactorPanel")));

        gbh = new GridBagHelper().setInsets(5, 5, 5, 0).anchorNorthWest().fillHorizontally();
        multifactorPanel.add(WidgetFactory.createLabel(bundleString("certField")), gbh.setMinWeightX().get());
        multifactorPanel.add(certField, gbh.nextCol().setWidth(3).leftGap(0).setMaxWeightX().get());
        multifactorPanel.add(browseCertFileButton, gbh.nextCol().setWidth(1).setMinWeightX().get());
        multifactorPanel.add(WidgetFactory.createLabel(bundleString("containerPasswordField")), gbh.nextRowFirstCol().leftGap(5).get());
        multifactorPanel.add(contPasswordField, gbh.nextCol().leftGap(0).setMaxWeightX().spanX().get());
        multifactorPanel.add(storeContPasswordCheck, gbh.nextRow().leftGap(-3).bottomGap(5).setMinWeightX().setWidth(1).get());
        multifactorPanel.add(verifyCertCheck, gbh.nextCol().leftGap(0).spanX().get());
    }

    protected void initComponentsLists() {
        basicAuthComponents.addAll(Arrays.asList(
                userField,
                userPasswordField,
                storePasswordCheck,
                encryptPasswordCheck,
                getNearComponent(userField, -6),
                getNearComponent(userPasswordField, -6)
        ));
    }

    protected void addListeners() {

        hostField.addKeyListener(this);
        userField.addKeyListener(this);
        portField.addKeyListener(this);
        fileField.addKeyListener(this);
        certField.addKeyListener(this);
        contPasswordField.getField().addKeyListener(this);
        userPasswordField.getField().addKeyListener(this);

        authCombo.addItemListener(this::authChanged);
        serverCombo.addItemListener(this::serverChanged);
        driverCombo.addItemListener(this::driverChanged);
        charsetsCombo.addItemListener(this::charsetChanged);

        verifyCertCheck.addActionListener(this::setVerifyCertCheck);
        storePasswordCheck.addActionListener(this::setStorePassword);
        encryptPasswordCheck.addActionListener(this::setEncryptPassword);
        storeContPasswordCheck.addActionListener(this::setStoreContPassword);
    }

    protected void addRequired() {

        userRequire = RequiredFieldPainter.initialize(userField);
        hostRequire = RequiredFieldPainter.initialize(hostField);
        portRequire = RequiredFieldPainter.initialize(portField);
        certRequire = RequiredFieldPainter.initialize(certField);
        userPasswordRequire = RequiredFieldPainter.initialize(userPasswordField);

        requiredPainters = Arrays.asList(
                userRequire,
                certRequire,
                hostRequire,
                portRequire,
                userPasswordRequire,
                RequiredFieldPainter.initialize(nameField),
                RequiredFieldPainter.initialize(fileField)
        );
    }

    protected void updateVisibleComponents() {

        if (isGssAuthSelected()) {
            userRequire.disable();
            certRequire.disable();
            userPasswordRequire.disable();

            multifactorPanel.setVisible(false);
            setEnabledComponents(basicAuthComponents, false);

        } else if (isMultifactorAuthSelected()) {
            userRequire.enable();
            userPasswordRequire.disable();
            certRequire.setEnable(!isNewServerSelected());

            multifactorPanel.setVisible(true);
            setEnabledComponents(basicAuthComponents, true);

        } else {
            userRequire.enable();
            certRequire.disable();
            userPasswordRequire.enable();

            multifactorPanel.setVisible(false);
            setEnabledComponents(basicAuthComponents, true);
        }
    }

    protected final JComponent getNearComponent(JComponent comp, int zOffset) {

        Container container = comp.getParent();
        if (container == null)
            return null;

        int componentIndex = container.getComponentZOrder(comp) + zOffset;
        if (componentIndex < 0)
            return null;

        return (JComponent) container.getComponent(componentIndex);
    }

    protected final void setEnabledComponents(List<JComponent> components, boolean enabled) {
        components.stream().filter(Objects::nonNull).forEach(c -> c.setEnabled(enabled));
    }

    // --- handlers ---

    private void addDriver() {
        new CreateDriverDialog();
    }

    private void browseFile(JTextField textField) {

        FileNameExtensionFilter filter = null;
        if (Objects.equals(textField, fileField)) {
            filter = new FileNameExtensionFilter(bundleString("databaseFile"), "fdb", "rdb");

        } else if (Objects.equals(textField, certField)) {
            filter = new FileNameExtensionFilter(bundleString("certFile"), "cer", "der");
        }

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileSelectionMode(FileChooserDialog.FILES_ONLY);
        fileChooser.addChoosableFileFilter(filter);

        int dialogResult = fileChooser.showOpenDialog(this);
        if (dialogResult == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            textField.setText(file.getAbsolutePath());
        }
    }

    private void setStorePassword(ActionEvent e) {
        boolean value = storePasswordCheck.isSelected();
        encryptPasswordCheck.setEnabled(value);
        handleEvent(e);
    }

    protected void setEncryptPassword(ActionEvent e) {
        boolean value = encryptPasswordCheck.isSelected() || storePasswordCheck.isSelected();
        storePasswordCheck.setSelected(value);
        handleEvent(e);
    }

    private void setStoreContPassword(ActionEvent e) {
        boolean value = storeContPasswordCheck.isSelected();
        connection.setContainerPasswordStored(value);
        handleEvent(e);
    }

    private void setVerifyCertCheck(ActionEvent e) {
        boolean value = verifyCertCheck.isSelected();
        connection.setVerifyServerCertCheck(value);
        handleEvent(e);
    }

    private void authChanged(ItemEvent e) {
        if (isSelected(e)) {
            updateVisibleComponents();
            handleEvent(e);
        }
    }

    private void serverChanged(ItemEvent e) {

        if (!isSelected(e))
            return;

        if (isNewServerSelected()) {
            authCombo.setSelectedItem(MULTIFACTOR_AUTH);
            authLabel.setEnabled(false);
            authCombo.setEnabled(false);

        } else {
            authLabel.setEnabled(true);
            authCombo.setEnabled(true);
        }

        updateVisibleComponents();
        handleEvent(e);
    }

    private void charsetChanged(ItemEvent e) {
        if (isSelected(e))
            handleEvent(e);
    }

    protected void driverChanged(ItemEvent e) {
        if (isSelected(e) && isDriverChangeEnable())
            handleEvent(e);
    }

    protected void handleEvent(AWTEvent event) {

        if (event == null || connection == null)
            return;

        Object source = event.getSource();
        if (Objects.equals(source, hostField)) {
            connection.setHost(hostField.getText().trim());

        } else if (Objects.equals(source, portField)) {
            connection.setPort(portField.getText().trim());

        } else if (Objects.equals(source, fileField)) {
            connection.setSourceName(fileField.getText().trim());

        } else if (Objects.equals(source, charsetsCombo)) {
            connection.setCharset((String) charsetsCombo.getSelectedItem());

        } else if (Objects.equals(source, driverCombo)) {

            DatabaseDriver driver = (DatabaseDriver) driverCombo.getSelectedItem();
            if (driver != null) {
                connection.setJDBCDriver(driver);
                connection.setDriverId(driver.getId());
                connection.setDriverName(driver.getName());
                connection.setDatabaseType(Integer.toString(driver.getType()));
            }

        } else if (Objects.equals(source, authCombo)) {
            connection.setAuthMethod((String) authCombo.getSelectedItem());

        } else if (Objects.equals(source, serverCombo)) {
            connection.setAuthMethodMode((String) serverCombo.getSelectedItem());

        } else if (Objects.equals(source, userField)) {
            connection.setUserName(userField.getText().trim());

        } else if (Objects.equals(source, userPasswordField)) {
            connection.setPassword(userPasswordField.getPassword());

        } else if (Objects.equals(source, storePasswordCheck)) {
            connection.setPasswordStored(storePasswordCheck.isSelected());

        } else if (Objects.equals(source, encryptPasswordCheck)) {
            connection.setPasswordEncrypted(encryptPasswordCheck.isSelected());

        } else if (Objects.equals(source, certField)) {
            connection.setCertificate(certField.getText().trim());

        } else if (Objects.equals(source, contPasswordField)) {
            connection.setContainerPassword(contPasswordField.getPassword());

        } else if (Objects.equals(source, storeContPasswordCheck)) {
            connection.setContainerPasswordStored(storeContPasswordCheck.isSelected());

        } else if (Objects.equals(source, verifyCertCheck)) {
            connection.setVerifyServerCertCheck(verifyCertCheck.isSelected());
        }

        storeJdbcProperties();
    }

    /**
     * Populates the values of the selected connection
     * properties object with the field values.
     */
    protected void populateConnectionObject() {

        if (connection == null)
            return;

        // --- basic ---

        connection.setHost(hostField.getText());
        connection.setPort(portField.getText());
        connection.setUserName(userField.getText());
        connection.setPassword(userPasswordField.getPassword());
        connection.setPasswordStored(storePasswordCheck.isSelected());
        connection.setAuthMethod((String) authCombo.getSelectedItem());
        connection.setCharset((String) charsetsCombo.getSelectedItem());
        connection.setPasswordEncrypted(encryptPasswordCheck.isSelected());
        connection.setAuthMethodMode((String) serverCombo.getSelectedItem());
        connection.setSourceName(fileField.getText().replace("\\", "/").trim());

        // --- driver ---

        if (isDriverChangeEnable()) {
            DatabaseDriver driver = (DatabaseDriver) driverCombo.getSelectedItem();
            if (driver != null) {
                connection.setJDBCDriver(driver);
                connection.setDriverId(driver.getId());
                connection.setDriverName(driver.getName());
                connection.setDatabaseType(Integer.toString(driver.getType()));
            }
        }

        // --- certificate ---

        connection.setCertificate(certField.getText());
        connection.setVerifyServerCertCheck(verifyCertCheck.isSelected());
        connection.setContainerPassword(contPasswordField.getPassword());
        connection.setContainerPasswordStored(storeContPasswordCheck.isSelected());

        // --- transaction isolation ---

        connection.setTransactionIsolation(propertiesPanel.getSelectedLevel());

        // ---

        storeJdbcProperties();
    }

    // --- jdbc properties ---

    protected void storeJdbcProperties() {
        connection.setJdbcProperties(getJdbcProperties());
    }

    protected void storeJdbcProperties(IFBCreateDatabase db) {
        db.setJdbcProperties(getJdbcProperties(null));
    }

    protected void storeJdbcProperties(DatabaseConnection connection) {
        connection.setJdbcProperties(getJdbcProperties(connection));
    }

    protected Properties getJdbcProperties() {
        return getJdbcProperties(connection);
    }

    protected Properties getJdbcProperties(DatabaseConnection connection) {

        Properties properties = null;
        if (connection != null)
            properties = connection.getJdbcProperties();

        if (properties == null)
            properties = new Properties();

        if (!properties.containsKey("connectTimeout")) {
            String connectTimeout = String.valueOf(SystemProperties.getIntProperty("user", "connection.connect.timeout"));
            properties.setProperty("connectTimeout", connectTimeout);
        }

        properties.setProperty("lc_ctype", (String) charsetsCombo.getSelectedItem());

        if (isGssAuthSelected())
            properties.setProperty("useGSSAuth", "true");
        else
            properties.remove("useGSSAuth");

        if (!properties.containsKey("isc_dpb_trusted_auth")
                && !properties.containsKey("isc_dpb_multi_factor_auth")
                && isMultifactorAuthSelected()) {

            properties.setProperty("isc_dpb_trusted_auth", "1");
            properties.setProperty("isc_dpb_multi_factor_auth", "1");
        }

        if (!certField.getText().isEmpty() && isMultifactorAuthSelected())
            loadCertificate(properties, certField.getText());

        if (!MiscUtils.isNull(contPasswordField.getPassword())
                && isMultifactorAuthSelected())
            properties.setProperty("isc_dpb_repository_pin", contPasswordField.getPassword());

        if (verifyCertCheck.isSelected() && isMultifactorAuthSelected())
            properties.setProperty("isc_dpb_verify_server", "1");

        String name = ManagementFactory.getRuntimeMXBean().getName();

        String pid = name.split("@")[0];
        if (ApplicationContext.getInstance().getExternalPID() != null &&
                !ApplicationContext.getInstance().getExternalPID().isEmpty()) {
            pid = ApplicationContext.getInstance().getExternalPID();
        }
        properties.setProperty("process_id", pid);

        String path = null;
        if (ApplicationContext.getInstance().getExternalProcessName() != null &&
                !ApplicationContext.getInstance().getExternalProcessName().isEmpty()) {
            path = ApplicationContext.getInstance().getExternalProcessName();
        }

        try {
            if (path == null)
                path = ExecuteQuery.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            properties.setProperty("process_name", path);

        } catch (URISyntaxException e) {
            Log.error(e.getMessage(), e);
        }
        return properties;
    }

    /**
     * Loads the certificate from a file.
     * If it is in the der format it converts it to base64.
     *
     * @param properties      connection properties
     * @param certificatePath path to x509 certificate file
     */
    private void loadCertificate(Properties properties, String certificatePath) {
        try {
            byte[] bytes = FileUtils.readBytes(new File(certificatePath));

            String certificate = new String(bytes);
            if (!checkBase64Format(certificate)) {
                certificate = "-----BEGIN CERTIFICATE-----\n" +
                        Base64.encodeBytes(bytes) +
                        "\n-----END CERTIFICATE-----";
            }

            properties.setProperty("isc_dpb_certificate_base64", certificate);

        } catch (IOException e) {
            Log.error(e.getMessage(), e);
        }
    }

    private boolean checkBase64Format(String certificate) {
        return StringUtils.contains(certificate, "-----BEGIN CERTIFICATE-----");
    }

    // --- drivers ---

    protected void loadDrivers() {
        Repository repo = RepositoryCache.load(DatabaseDriverRepository.REPOSITORY_ID);
        if (repo instanceof DatabaseDriverRepository) {
            DatabaseDriverRepository driversRepo = (DatabaseDriverRepository) repo;
            jdbcDrivers = driversRepo.findAll();
        }
    }

    protected void updateDriversCombo() {
        setDriverChangeEnable(false);

        DynamicComboBoxModel<DatabaseDriver> model = new DynamicComboBoxModel<>();
        model.setElements(jdbcDrivers);
        driverCombo.setModel(model);

        setDriverChangeEnable(true);
    }

    protected void selectActualDriver() {

        if (connection == null)
            return;

        long driverId = connection.getDriverId();
        if (driverId == 0)
            return;

        jdbcDrivers.stream()
                .filter(driver -> Objects.equals(driver.getId(), driverId))
                .findFirst().ifPresent(driver -> driverCombo.setSelectedItem(driver));
    }

    protected boolean isDriverChangeEnable() {
        return driverChangeEnable;
    }

    private void setDriverChangeEnable(boolean enable) {
        this.driverChangeEnable = enable;
    }

    // --- connection name handlers ---

    protected final boolean connectionNameExists() {
        return connectionNameExists(nameField.getText().trim());
    }

    protected final boolean connectionNameExists(String connectionName) {

        Repository repo = RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID);
        if (repo instanceof DatabaseConnectionRepository) {
            DatabaseConnectionRepository dbRepo = (DatabaseConnectionRepository) repo;

            String folderId = connection != null ? connection.getFolderId() : "";
            if (dbRepo.nameExists(connection, connectionName, folderId)) {
                GUIUtilities.displayErrorMessage(bundleString("error.nameExist", connectionName));
                return true;
            }
        }

        return false;
    }

    protected void checkNameUpdate() {

        String newName = nameField.getText().trim();
        if (MiscUtils.isNull(newName)) {
            GUIUtilities.displayWarningMessage(bundleString("error.nameEmpty"));
            focusNameField();
            return;
        }

        if (connectionNameExists(newName))
            focusNameField();
    }

    protected final void focusNameField() {
        nameField.requestFocusInWindow();
        nameField.selectAll();
    }

    // --- others ---

    private void loadCharsets() {
        charsets = new ArrayList<>();

        try {
            String resource = FileUtils.loadResource("org/executequery/charsets.properties");
            Arrays.stream(resource.split("\n"))
                    .filter(line -> !line.startsWith("#"))
                    .filter(line -> !line.isEmpty())
                    .forEach(charsets::add);

            Collections.sort(charsets);
            charsets.add(0, "NONE");

        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }

    protected boolean anyRequiredFieldEmpty() {

        boolean anyRequiredFieldEmpty = requiredPainters.stream().anyMatch(e -> !e.check());
        if (anyRequiredFieldEmpty)
            GUIUtilities.displayWarningMessage(bundleString("somethingEmpty"));

        return anyRequiredFieldEmpty;
    }

    protected boolean isGssAuthSelected() {
        return Objects.equals(authCombo.getSelectedItem(), GSS_AUTH);
    }

    protected boolean isMultifactorAuthSelected() {
        return Objects.equals(authCombo.getSelectedItem(), MULTIFACTOR_AUTH);
    }

    protected boolean isNewServerSelected() {
        return Objects.equals(serverCombo.getSelectedItem(), NEW_SERVER);
    }

    protected static boolean isSelected(ItemEvent e) {
        return e.getStateChange() == ItemEvent.SELECTED;
    }

    protected static String bundleString(String key, Object... args) {
        return Bundles.get(AbstractConnectionPanel.class, key, args);
    }

    // --- DatabaseDriverListener impl ---

    @Override
    public void driversUpdated(DatabaseDriverEvent databaseDriverEvent) {
        loadDrivers();
        updateDriversCombo();
        driverCombo.setSelectedItem(databaseDriverEvent.getSource());
    }

    @Override
    public boolean canHandleEvent(ApplicationEvent event) {
        return event instanceof DatabaseDriverEvent;
    }

    // --- KeyListener impl ---

    @Override
    public void keyReleased(KeyEvent e) {
        handleEvent(e);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

}

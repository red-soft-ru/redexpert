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

import org.executequery.*;
import org.executequery.databasemediators.ConnectionMediator;
import org.executequery.databasemediators.ConnectionType;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.event.*;
import org.executequery.gui.WidgetFactory;
import org.executequery.listeners.SimpleDocumentListener;
import org.executequery.localization.Bundles;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.ViewablePasswordField;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.listener.RequiredFieldPainter;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class ConnectionPanel extends AbstractConnectionPanel {

    private List<JComponent> nativeServerComponents;
    private List<RequiredFieldPainter> sshRequired;

    private int connectButtonWidth;
    private int disconnectButtonWidth;

    // ---  gui components ---

    private JTextField roleField;
    private JTextField sshHostField;
    private JTextField sshUserField;

    private NumberTextField sshPortField;
    private ViewablePasswordField sshPasswordField;

    private JCheckBox useSshCheck;
    private JCheckBox useNewApiCheck;
    private JCheckBox useNativeCheck;
    private JCheckBox useEmbeddedCheck;
    private JCheckBox namesToUpperCheck;
    private JCheckBox storeSshPasswordCheck;

    private JButton saveButton;
    private JButton testButton;
    private JButton connectButton;

    private JPanel sshPanel;

    // ---

    public ConnectionPanel(BrowserController controller) {
        super(controller);

        updateConnectionState();
        useSshCheckTriggered();
        useNewApiCheckTriggered();
    }

    // --- AbstractConnectionPanel impl ---

    @Override
    protected void init() {
        super.init();

        nativeServerComponents = new ArrayList<>();

        roleField = WidgetFactory.createTextField("roleField");
        sshHostField = WidgetFactory.createTextField("sshHostField");
        sshUserField = WidgetFactory.createTextField("sshUserField");
        sshPortField = WidgetFactory.createNumberTextField("sshPortField");
        sshPasswordField = WidgetFactory.createViewablePasswordField("sshPasswordField");

        useSshCheck = WidgetFactory.createCheckBox("useSshCheck", bundleString("useSshCheck"));
        useNewApiCheck = WidgetFactory.createCheckBox("useNewApiCheck", bundleString("useNewApiCheck"));
        useNativeCheck = WidgetFactory.createCheckBox("useNativeCheck", bundleString("useNativeCheck"));
        useEmbeddedCheck = WidgetFactory.createCheckBox("useEmbeddedCheck", bundleString("useEmbeddedCheck"));
        storeSshPasswordCheck = WidgetFactory.createCheckBox("storeSshPasswordCheck", bundleString("StorePassword"));
        namesToUpperCheck = WidgetFactory.createCheckBox("namesToUpperCheck", bundleString("namesToUpperCheck"), true);

        saveButton = WidgetFactory.createButton("saveButton", Bundles.get("common.save.button"), e -> saveConnection());
        testButton = WidgetFactory.createButton("testButton", Bundles.get("common.test.button"), e -> testConnection());
        connectButton = WidgetFactory.createButton("connectButton", Bundles.get("common.connect.button"), e -> toggleConnection());

        connectButtonWidth = (int) connectButton.getPreferredSize().getWidth();
        disconnectButtonWidth = (int) WidgetFactory.createButton("", Bundles.get("common.disconnect.button")).getPreferredSize().getWidth();
    }

    @Override
    protected void arrange() {
        super.arrange();

        GridBagHelper gbh;

        // --- ssh panel ---

        sshPanel = WidgetFactory.createPanel("sshPanel");
        sshPanel.setBorder(BorderFactory.createTitledBorder(bundleString("ssh.title")));

        gbh = new GridBagHelper().setInsets(5, 5, 5, 0).fillHorizontally();
        sshPanel.add(useSshCheck, gbh.spanX().get());
        sshPanel.add(WidgetFactory.createLabel(bundleString("ssh.note.top")), gbh.nextRow().get());
        sshPanel.add(WidgetFactory.createLabel(bundleString("ssh.hostField")), gbh.nextRow().setWidth(1).setMinWeightX().get());
        sshPanel.add(sshHostField, gbh.nextCol().leftGap(0).setMaxWeightX().spanX().get());
        sshPanel.add(WidgetFactory.createLabel(bundleString("ssh.portField")), gbh.nextRowFirstCol().setWidth(1).leftGap(5).setMinWeightX().get());
        sshPanel.add(sshPortField, gbh.nextCol().leftGap(0).setMaxWeightX().spanX().get());
        sshPanel.add(WidgetFactory.createLabel(bundleString("ssh.userField")), gbh.nextRowFirstCol().setWidth(1).leftGap(5).setMinWeightX().get());
        sshPanel.add(sshUserField, gbh.nextCol().leftGap(0).setMaxWeightX().spanX().get());
        sshPanel.add(WidgetFactory.createLabel(bundleString("ssh.passwordField")), gbh.nextRowFirstCol().setWidth(1).leftGap(5).setMinWeightX().get());
        sshPanel.add(sshPasswordField, gbh.nextCol().leftGap(0).setMaxWeightX().get());
        sshPanel.add(storeSshPasswordCheck, gbh.nextCol().setMinWeightX().leftGap(5).get());
        sshPanel.add(WidgetFactory.createLabel(bundleString("ssh.note.bottom")), gbh.nextRowFirstCol().bottomGap(5).spanX().get());

        // --- check panel ---

        JPanel checkPwdPanel = WidgetFactory.createPanel("checkPwdPanel");

        gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        checkPwdPanel.add(storePasswordCheck, gbh.get());
        checkPwdPanel.add(encryptPasswordCheck, gbh.nextCol().leftGap(5).spanX().get());

        // ---

        JPanel checkPanel = WidgetFactory.createPanel("checkPanel");

        gbh = new GridBagHelper().anchorNorthWest().fillNone();
        checkPanel.add(namesToUpperCheck, gbh.spanX().get());
        checkPanel.add(useNativeCheck, gbh.nextRow().topGap(5).get());
//        checkPanel.add(useEmbeddedCheck, gbh.nextRow().get()); TODO test before release feature
        checkPanel.add(useNewApiCheck, gbh.nextRow().setMinWeightX().setWidth(1).get());
        checkPanel.add(useSshCheck, gbh.nextCol().leftGap(5).get());

        // --- buttons panel ---

        JPanel buttonsPanel = WidgetFactory.createPanel("buttonsPanel");

        gbh = new GridBagHelper().rightGap(5).fillHorizontally();
        buttonsPanel.add(connectButton, gbh.get());
        buttonsPanel.add(testButton, gbh.nextCol().get());
        buttonsPanel.add(saveButton, gbh.nextCol().get());
        buttonsPanel.add(new JPanel(), gbh.nextCol().setMaxWeightX().spanX().get());

        // --- left panel ---

        JPanel leftPanel = WidgetFactory.createPanel("leftPanel");

        gbh = new GridBagHelper().leftGap(5).setMinWeightX().anchorNorthWest().fillHorizontally();
        leftPanel.add(WidgetFactory.createLabel(bundleString("nameField")), gbh.get());
        leftPanel.add(WidgetFactory.createLabel(bundleString("hostField")), gbh.nextRow().topGap(5).get());
        leftPanel.add(WidgetFactory.createLabel(bundleString("portField")), gbh.nextRow().get());
        leftPanel.add(WidgetFactory.createLabel(bundleString("fileField")), gbh.nextRow().get());
        leftPanel.add(WidgetFactory.createLabel(bundleString("charsetsCombo")), gbh.nextRow().get());

        gbh.setY(0).nextCol().topGap(0).setMaxWeightX().spanX();
        leftPanel.add(nameField, gbh.get());
        leftPanel.add(hostField, gbh.nextRow().topGap(5).get());
        leftPanel.add(portField, gbh.nextRow().get());
        leftPanel.add(fileField, gbh.nextRow().setWidth(1).get());
        leftPanel.add(browseFileButton, gbh.nextCol().setMinWeightX().get());
        leftPanel.add(charsetsCombo, gbh.nextRow().previousCol().spanX().get());
        leftPanel.add(checkPanel, gbh.nextRowFirstCol().setWidth(2).leftGap(2).get());

        // --- right panel ---

        JPanel rightPanel = WidgetFactory.createPanel("rightPanel");

        gbh = new GridBagHelper().leftGap(5).setMinWeightX().anchorNorthWest().fillHorizontally();
        rightPanel.add(WidgetFactory.createLabel(bundleString("driverCombo")), gbh.get());
        rightPanel.add(WidgetFactory.createLabel(bundleString("serverCombo")), gbh.nextRow().topGap(5).get());
        rightPanel.add(authLabel, gbh.nextRow().get());
        rightPanel.add(WidgetFactory.createLabel(bundleString("roleField")), gbh.nextRow().get());
        rightPanel.add(WidgetFactory.createLabel(bundleString("userField")), gbh.nextRow().get());
        rightPanel.add(WidgetFactory.createLabel(bundleString("passwordField")), gbh.nextRow().get());

        gbh.setY(0).nextCol().topGap(0).setMaxWeightX();
        rightPanel.add(driverCombo, gbh.get());
        rightPanel.add(addDriverButton, gbh.nextCol().setMinWeightX().get());
        rightPanel.add(serverCombo, gbh.nextRow().previousCol().topGap(5).spanX().get());
        rightPanel.add(authCombo, gbh.nextRow().get());
        rightPanel.add(roleField, gbh.nextRow().get());
        rightPanel.add(userField, gbh.nextRow().get());
        rightPanel.add(userPasswordField, gbh.nextRow().get());
        rightPanel.add(checkPwdPanel, gbh.nextRow().nextCol().leftGap(2).get());
        rightPanel.add(new JPanel(), gbh.nextRow().setMaxWeightY().spanY().get());

        // --- bottom panel ---

        JPanel bottomPanel = WidgetFactory.createPanel("mainPanel");

        gbh = new GridBagHelper().anchorNorthWest().spanX();
        bottomPanel.add(sshPanel, gbh.setMinWeightX().bottomGap(10).get());
        bottomPanel.add(multiFactorPanel, gbh.nextRowFirstCol().bottomGap(0).get());

        // --- main panel ---

        JPanel mainPanel = WidgetFactory.createPanel("mainPanel");

        gbh = new GridBagHelper().anchorNorthWest().setInsets(5, 5, 5, 5).fillBoth();
        mainPanel.add(leftPanel, gbh.setMaxWeightX().get());
        mainPanel.add(rightPanel, gbh.nextCol().spanX().get());
        mainPanel.add(bottomPanel, gbh.nextRowFirstCol().fillNone().get());
        mainPanel.add(buttonsPanel, gbh.nextRow().get());
        mainPanel.add(new JPanel(), gbh.nextRow().setMaxWeightY().fillBoth().spanY().get());

        // --- tab pane ---

        tabPane.setTabPlacement(SwingConstants.BOTTOM);
        tabPane.addTab(bundleString("Basic"), new JScrollPane(mainPanel));
        tabPane.addTab(bundleString("Advanced"), propertiesPanel);

        // --- base ---

        gbh = new GridBagHelper().fillBoth().spanX().spanY();
        add(tabPane, gbh.get());
    }

    @Override
    protected void initComponentsLists() {
        addBasicAuthComponents(Arrays.asList(
                roleField,
                userField,
                userPasswordField,
                storePasswordCheck,
                encryptPasswordCheck,
                getNearComponent(roleField, -7),
                getNearComponent(userField, -7),
                getNearComponent(userPasswordField, -7)
        ));

        nativeServerComponents.addAll(Arrays.asList(
                hostField,
                portField,
                authCombo,
                authLabel,
                roleField,
                serverCombo,
                userPasswordField,
                storePasswordCheck,
                encryptPasswordCheck,
                getNearComponent(hostField, -5),
                getNearComponent(portField, -5),
                getNearComponent(roleField, -7),
                getNearComponent(serverCombo, -7),
                getNearComponent(userPasswordField, -7)
        ));
    }

    @Override
    protected void addListeners() {
        super.addListeners();

        FocusHandler focusListener = new FocusHandler();
        nameField.addFocusListener(focusListener);
        nameField.addActionListener(e -> {
            focusListener.block();
            checkNameUpdate();
        });

        SimpleDocumentListener.initialize(hostField, this::enableApplyButton);
        SimpleDocumentListener.initialize(userField, this::enableApplyButton);
        SimpleDocumentListener.initialize(portField, this::enableApplyButton);
        SimpleDocumentListener.initialize(fileField, this::enableApplyButton);
        SimpleDocumentListener.initialize(certField, this::enableApplyButton);
        SimpleDocumentListener.initialize(userPasswordField.getField(), this::enableApplyButton);
        SimpleDocumentListener.initialize(contPasswordField.getField(), this::enableApplyButton);

        SimpleDocumentListener.initialize(roleField, this::roleChanged);
        SimpleDocumentListener.initialize(sshHostField, this::sshHostChanged);
        SimpleDocumentListener.initialize(sshPortField, this::sshPortChanged);
        SimpleDocumentListener.initialize(sshUserField, this::sshUserChanged);
        SimpleDocumentListener.initialize(sshPasswordField, this::sshPasswordChanged);

        useNativeCheck.addActionListener(this::connTypeChanged);
        useEmbeddedCheck.addActionListener(this::connTypeChanged);
        useSshCheck.addActionListener(e -> useSshCheckTriggered());
        useNewApiCheck.addActionListener(e -> useNewApiCheckTriggered());
        namesToUpperCheck.addActionListener(e -> namesToUpperCheckTriggered());
        storeSshPasswordCheck.addActionListener(e -> storeSshPasswordCheckTriggered());
    }

    @Override
    protected void addRequired() {
        super.addRequired();

        sshRequired = Arrays.asList(
                RequiredFieldPainter.initialize(sshHostField),
                RequiredFieldPainter.initialize(sshUserField),
                RequiredFieldPainter.initialize(sshPortField),
                RequiredFieldPainter.initialize(sshPasswordField)
        );
    }

    @Override
    protected Properties getJdbcProperties() {
        Properties properties = super.getJdbcProperties();

        String role = roleField.getText();
        if (!MiscUtils.isNull(role))
            properties.setProperty("roleName", role);

        return properties;
    }

    @Override
    protected void driverChanged(ItemEvent e) {
        super.driverChanged(e);
        if (isSelected(e))
            useNewApiCheckTriggered();
    }

    @Override
    protected void populateConnectionObject() {

        if (connection == null)
            return;

        // --- basic ---

        connection.setRole(roleField.getText());
        connection.setUseNewAPI(useNewApiCheck.isSelected());
        connection.setNamesToUpperCase(namesToUpperCheck.isSelected());
        connection.setConnType(ConnectionType.getConnType(useNativeCheck.isSelected(), isEmbeddedConnectionSelected()).name());

        // --- ssh ---

        connection.setSshHost(sshHostField.getText());
        connection.setSshPort(sshPortField.getValue());
        connection.setSshUserName(sshUserField.getText());
        connection.setSshTunnel(useSshCheck.isSelected());
        connection.setSshPassword(sshPasswordField.getPassword());
        connection.setSshPasswordStored(storeSshPasswordCheck.isSelected());

        // ---

        super.populateConnectionObject();
    }

    @Override
    protected boolean anyRequiredFieldEmpty() {

        boolean anyRequiredFieldEmpty = super.anyRequiredFieldEmpty();
        if (anyRequiredFieldEmpty)
            return true;

        anyRequiredFieldEmpty = sshRequired.stream().anyMatch(e -> !e.check());
        if (anyRequiredFieldEmpty)
            GUIUtilities.displayWarningMessage(bundleString("somethingEmpty"));

        return anyRequiredFieldEmpty;
    }

    @Override
    protected void selectActualDriver() {
        super.selectActualDriver();
        useNewApiCheckTriggered();
    }

    @Override
    protected void checkNameUpdate() {

        String oldName = connection.getName();
        String newName = nameField.getText().trim();

        if (MiscUtils.isNull(newName)) {
            GUIUtilities.displayWarningMessage(bundleString("error.nameEmpty"));

            nameField.setText(oldName);
            focusNameField();
            return;
        }

        if (connectionNameExists(newName)) {
            nameField.setText(oldName);
            focusNameField();
            return;
        }

        if (!Objects.equals(oldName, newName)) {
            connection.setName(newName);
            controller.nodeNameValueChanged(host);
        }
    }

    @Override
    protected void updateVisibleComponents() {

        if (isEmbeddedConnectionSelected()) {
            setEnabledComponents(nativeServerComponents, false);
            multiFactorPanel.setVisible(false);
            userPasswordRequire.disable();
            hostRequire.disable();
            portRequire.disable();
            certRequire.disable();
            userRequire.disable();

        } else {
            setEnabledComponents(nativeServerComponents, true);
            hostRequire.enable();
            portRequire.enable();

            super.updateVisibleComponents();
        }
    }

    // --- handlers ---

    private void connTypeChanged(ActionEvent e) {

        Object source = e.getSource();
        if (Objects.equals(source, useNativeCheck)) {
            if (useNativeCheck.isSelected())
                useEmbeddedCheck.setSelected(false);

        } else if (Objects.equals(source, useEmbeddedCheck) && isEmbeddedConnectionSelected())
            useNativeCheck.setSelected(false);

        useNewApiCheckTriggered();
        updateVisibleComponents();

        if (hasConnection()) {
            ConnectionType type = ConnectionType.getConnType(useNativeCheck.isSelected(), isEmbeddedConnectionSelected());
            connection.setConnType(type.name());
            storeJdbcProperties();
        }
    }

    private void useSshCheckTriggered() {

        boolean enabled = useSshCheck.isSelected();
        if (enabled) {
            setSshValues(connection);
            populateConnectionObject();

            sshPanel.setVisible(true);
            sshRequired.forEach(RequiredFieldPainter::enable);

        } else {
            sshPanel.setVisible(false);
            sshRequired.forEach(RequiredFieldPainter::disable);
        }

        if (hasConnection()) {
            connection.setSshTunnel(useSshCheck.isSelected());
            storeJdbcProperties();
        }
    }

    private void useNewApiCheckTriggered() {

        DatabaseDriver selectedDriver = (DatabaseDriver) driverCombo.getSelectedItem();
        int majorVersion = selectedDriver != null ? selectedDriver.getMajorVersion() : -1;

        if (majorVersion < 4) {
            if (useNewApiCheck.isSelected()) {
                GUIUtilities.displayWarningMessage(bundleString("warning.useNewAPI.driver"));
                useNewApiCheck.setSelected(false);
            }
            useNewApiCheck.setEnabled(false);

        } else if (isPureJavaConnectionSelected()) {
            if (useNewApiCheck.isSelected()) {
                GUIUtilities.displayWarningMessage(bundleString("warning.useNewAPI.connType"));
                useNewApiCheck.setSelected(false);
            }
            useNewApiCheck.setEnabled(false);

        } else
            useNewApiCheck.setEnabled(true);

        if (hasConnection()) {
            connection.setUseNewAPI(useNewApiCheck.isSelected());
            storeJdbcProperties();
        }
    }

    private void namesToUpperCheckTriggered() {
        enableApplyButton();
        if (hasConnection()) {
            connection.setNamesToUpperCase(namesToUpperCheck.isSelected());
            storeJdbcProperties();
        }
    }

    private void storeSshPasswordCheckTriggered() {
        enableApplyButton();
        if (hasConnection()) {
            connection.setSshPasswordStored(storeSshPasswordCheck.isSelected());
            storeJdbcProperties();
        }
    }

    private void roleChanged() {
        enableApplyButton();
        if (hasConnection()) {
            connection.setRole(roleField.getText().trim());
            storeJdbcProperties();
        }
    }

    private void sshHostChanged() {
        enableApplyButton();
        if (hasConnection()) {
            connection.setSshHost(sshHostField.getText().trim());
            storeJdbcProperties();
        }
    }

    private void sshPortChanged() {
        enableApplyButton();
        if (hasConnection()) {
            connection.setSshPort(sshPortField.getValue());
            storeJdbcProperties();
        }
    }

    private void sshUserChanged() {
        enableApplyButton();
        if (hasConnection()) {
            connection.setSshUserName(sshUserField.getText().trim());
            storeJdbcProperties();
        }
    }

    private void sshPasswordChanged() {
        enableApplyButton();
        if (hasConnection()) {
            connection.setSshPassword(sshPasswordField.getPassword());
            storeJdbcProperties();
        }
    }

    // --- ssh ---

    private void setSshValues(DatabaseConnection dc) {

        sshHostField.setText(dc.getSshHost());
        sshUserField.setText(dc.getSshUserName());
        sshPortField.setText(String.valueOf(dc.getSshPort()));
        storeSshPasswordCheck.setSelected(dc.isSshPasswordStored());
        sshPasswordField.setPassword(dc.getUnencryptedSshPassword());

        if (dc.getSshPort() < 1)
            sshPortField.setText("22");
    }

    // --- host panel impl ---

    public void connectionNameChanged(String name) {
        nameField.setText(name);
        populateConnectionObject();
    }

    /**
     * Informed by a tree selection, this readies the form for
     * a new connection object and value change.
     */
    protected void selectionChanging() {
        if (connection != null)
            populateConnectionObject();
    }

    /**
     * Sets the connection fields on this panel to the
     * values as held within the specified connection
     * properties object.
     *
     * @param host connection to set the fields to
     */
    public void setConnectionValue(DatabaseHost host) {

        if (connection != null)
            populateConnectionObject();

        this.host = host;

        populateConnectionFields(host.getDatabaseConnection());
        saveConnection();
        focusNameField();
        updateConnectionState();

        EventMediator.fireEvent(new DefaultConnectionRepositoryEvent(
                this,
                ConnectionRepositoryEvent.CONNECTION_MODIFIED,
                connection
        ));
    }

    /**
     * Indicates the panel is being de-selected in the pane
     */
    public boolean tabViewDeselected() {
        return populateAndSave();
    }

    /**
     * Indicates the panel is being selected in the pane
     */
    public boolean tabViewSelected() {
        if (connection != null)
            enableFields(connection.isConnected());

        return true;
    }

    /**
     * Indicates a connection has been established.
     *
     * @param dc connection properties object
     */
    public void connected(DatabaseConnection dc) {
        populateConnectionFields(dc);
    }

    /**
     * Indicates a connection has been closed.
     */
    public void disconnected() {
        enableFields(false);
    }

    // --- connect handlers ---

    private void toggleConnection() {
        try {
            connectButton.setEnabled(false);

            if (connection.isConnected())
                disconnect();
            else
                connect();

        } finally {
            updateConnectionState();
        }
    }

    private void updateConnectionState() {
        boolean connected = connection != null && connection.isConnected();

        connectButton.setText(Bundles.getCommon(connected ? "disconnect.button" : "connect.button"));
        connectButton.setEnabled(true);
        connectButton.setPreferredSize(new Dimension(
                connected ? disconnectButtonWidth : connectButtonWidth,
                connectButton.getPreferredSize().height
        ));

        testButton.setEnabled(!connected);
    }

    /**
     * Action implementation on selection of the Connect button.
     */
    private void connect() {

        if (!canConnect())
            return;

        try {
            saveConnection();
            GUIUtilities.showWaitCursor();
            System.setProperty("java.security.auth.login.config", "config/gss.login.conf");

            boolean connected = host.connect();
            if (connected)
                propertiesPanel.applyTransactionLevel(false);

        } catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("error.connect", e.getMessage()), e, this.getClass());

        } finally {
            GUIUtilities.showNormalCursor();
        }
    }

    /**
     * Action implementation on selection of the Disconnect button.
     */
    private void disconnect() {
        try {
            host.disconnect();

        } catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("error.disconnect", e.getMessage()), e, this.getClass());

        } finally {
            setApplyButtonEnabled(false);
        }
    }

    private void testConnection() {

        if (!canConnect())
            return;

        try {
            saveConnection();
            GUIUtilities.showWaitCursor();

            if (ConnectionMediator.getInstance().test(connection))
                GUIUtilities.displayInformationMessage(bundleString("test.success"));

        } catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("error.connect", e.getMessage()), e, this.getClass());

        } finally {
            GUIUtilities.showNormalCursor();
            setApplyButtonEnabled(false);
        }
    }

    private void saveConnection() {
        setApplyButtonEnabled(false);
        populateAndSave();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean canConnect() {

        if (connection.isConnected())
            return false;

        if (MiscUtils.isNull(nameField.getText().trim()))
            return false;

        if (anyRequiredFieldEmpty())
            return false;

        if (connectionNameExists()) {
            focusNameField();
            return false;
        }

        return true;
    }

    // ---

    private boolean isEmbeddedConnectionSelected() {
        return useEmbeddedCheck.isSelected();
    }

    private boolean isPureJavaConnectionSelected() {
        ConnectionType connectionType = ConnectionType.getConnType(useNativeCheck.isSelected(), isEmbeddedConnectionSelected());
        return Objects.equals(connectionType.name(), ConnectionType.PURE_JAVA.name());
    }

    private void enableFields(boolean enable) {
        updateConnectionState();
        propertiesPanel.setTransactionEnabled(enable);
        setEncryptPassword(new ActionEvent(encryptPasswordCheck, -1, null));
    }

    private boolean populateAndSave() {
        populateConnectionObject();

        EventMediator.fireEvent(new DefaultConnectionRepositoryEvent(
                this,
                ConnectionRepositoryEvent.CONNECTION_MODIFIED,
                (DatabaseConnection) null
        ));

        return true;
    }

    /**
     * Populates the values of the fields with the values of
     * the specified connection.
     */
    private void populateConnectionFields(DatabaseConnection dc) {
        connection = dc;

        String host = dc.getHost();
        if (host.isEmpty())
            host = "localhost";

        String port = dc.getPort();
        if (port.isEmpty())
            port = "3050";

        portField.setText(port);
        hostField.setText(host);
        nameField.setText(dc.getName());
        roleField.setText(dc.getRole());
        userField.setText(dc.getUserName());
        fileField.setText(dc.getSourceName());
        certField.setText(dc.getCertificate());
        useSshCheck.setSelected(dc.isSshTunnel());
        useNewApiCheck.setSelected(dc.useNewAPI());
        authCombo.setSelectedItem(dc.getAuthMethod());
        charsetsCombo.setSelectedItem(dc.getCharset());
        storePasswordCheck.setSelected(dc.isPasswordStored());
        namesToUpperCheck.setSelected(dc.isNamesToUpperCase());
        contPasswordField.setPassword(dc.getContainerPassword());
        verifyCertCheck.setSelected(dc.isVerifyServerCertCheck());
        userPasswordField.setPassword(dc.getUnencryptedPassword());
        encryptPasswordCheck.setSelected(dc.isPasswordEncrypted());
        storeContPasswordCheck.setSelected(dc.isContainerPasswordStored());
        serverCombo.setSelectedItem(dc.getAuthMethodMode() != null ? dc.getAuthMethodMode() : NEW_SERVER);

        ConnectionType connType = ConnectionType.contains(dc.getConnType()) ?
                ConnectionType.valueOf(dc.getConnType()) :
                ConnectionType.PURE_JAVA;
        useNativeCheck.setSelected(Objects.equals(connType, ConnectionType.NATIVE));
        useEmbeddedCheck.setSelected(Objects.equals(connType, ConnectionType.EMBEDDED));

        selectActualDriver();
        useSshCheckTriggered();
        enableFields(connection.isConnected());
        propertiesPanel.setConnection(connection);
        updateVisibleComponents();
        serverChanged();
    }

    public void addTab(String title, Component component) {
        tabPane.addTab(title, component);
    }

    private void enableApplyButton() {
        setApplyButtonEnabled(true);
    }

    private void setApplyButtonEnabled(boolean value) {
        value &= hasConnection() && !connection.isConnected();
        saveButton.setEnabled(value);
    }

    // ---

    private class FocusHandler implements FocusListener {
        boolean block;

        @Override
        public void focusGained(FocusEvent e) {
            // do nothing
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (!block)
                checkNameUpdate();

            unblock();
        }

        public void block() {
            this.block = true;
        }

        public void unblock() {
            this.block = false;
        }

    } // FocusHandler class

}

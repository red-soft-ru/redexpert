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
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.event.*;
import org.executequery.gui.WidgetFactory;
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

    private List<RequiredFieldPainter> sshRequired;

    // ---  gui components ---

    private JTextField roleField;
    private JTextField sshHostField;
    private JTextField sshUserField;

    private NumberTextField sshPortField;
    private ViewablePasswordField sshPasswordField;

    private JCheckBox useSshCheck;
    private JCheckBox useNewApiCheck;
    private JCheckBox namesToUpperCheck;
    private JCheckBox storeSshPasswordCheck;

    private JButton testButton;
    private JButton connectButton;

    private JPanel sshPanel;

    // ---

    public ConnectionPanel(BrowserController controller) {
        super(controller);

        updateConnectionState();
        useSshCheckTriggered(null);
    }

    @Override
    protected void init() {
        super.init();

        roleField = WidgetFactory.createTextField("roleField");
        sshHostField = WidgetFactory.createTextField("sshHostField");
        sshUserField = WidgetFactory.createTextField("sshUserField");
        sshPortField = WidgetFactory.createNumberTextField("sshPortField");
        sshPasswordField = WidgetFactory.createViewablePasswordField("sshPasswordField");

        useSshCheck = WidgetFactory.createCheckBox("useSshCheck", bundleString("useSshCheck"));
        useNewApiCheck = WidgetFactory.createCheckBox("useNewApiCheck", bundleString("useNewApiCheck"));
        storeSshPasswordCheck = WidgetFactory.createCheckBox("storeSshPasswordCheck", bundleString("StorePassword"));
        namesToUpperCheck = WidgetFactory.createCheckBox("namesToUpperCheck", bundleString("namesToUpperCheck"), true);

        testButton = WidgetFactory.createButton("testButton", Bundles.get("common.test.button"), e -> testConnection());
        connectButton = WidgetFactory.createButton("connectButton", Bundles.get("common.connect.button"), e -> toggleConnection());
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

        gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        checkPanel.add(useNewApiCheck, gbh.get());
        checkPanel.add(useSshCheck, gbh.nextCol().leftGap(5).spanX().get());
        checkPanel.add(namesToUpperCheck, gbh.nextRowFirstCol().leftGap(-3).topGap(5).get());

        // --- buttons panel ---

        JPanel buttonsPanel = WidgetFactory.createPanel("buttonsPanel");

        gbh = new GridBagHelper().fillHorizontally();
        buttonsPanel.add(connectButton, gbh.get());
        buttonsPanel.add(testButton, gbh.nextCol().leftGap(5).get());
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
        leftPanel.add(checkPanel, gbh.nextRow().previousCol().leftGap(2).get());

        // --- right panel ---

        JPanel rightPanel = WidgetFactory.createPanel("rightPanel");

        gbh = new GridBagHelper().leftGap(5).setMinWeightX().anchorNorthWest().fillHorizontally();
        rightPanel.add(WidgetFactory.createLabel(bundleString("driverCombo")), gbh.get());
        rightPanel.add(WidgetFactory.createLabel(bundleString("authCombo")), gbh.nextRow().topGap(5).get());
        rightPanel.add(WidgetFactory.createLabel(bundleString("roleField")), gbh.nextRow().get());
        rightPanel.add(WidgetFactory.createLabel(bundleString("userField")), gbh.nextRow().get());
        rightPanel.add(WidgetFactory.createLabel(bundleString("passwordField")), gbh.nextRow().get());

        gbh.setY(0).nextCol().topGap(0).setMaxWeightX();
        rightPanel.add(driverCombo, gbh.get());
        rightPanel.add(addDriverButton, gbh.nextCol().setMinWeightX().get());
        rightPanel.add(authCombo, gbh.nextRow().previousCol().topGap(5).spanX().get());
        rightPanel.add(roleField, gbh.nextRow().get());
        rightPanel.add(userField, gbh.nextRow().get());
        rightPanel.add(passwordField, gbh.nextRow().get());
        rightPanel.add(checkPwdPanel, gbh.nextRow().nextCol().leftGap(2).get());
        rightPanel.add(new JPanel(), gbh.nextRow().setMaxWeightY().spanY().get());

        // --- main panel ---

        JPanel mainPanel = WidgetFactory.createPanel("mainPanel");

        gbh = new GridBagHelper().anchorNorthWest().setInsets(5, 5, 5, 5).fillBoth();
        mainPanel.add(leftPanel, gbh.setMaxWeightX().get());
        mainPanel.add(rightPanel, gbh.nextCol().spanX().get());
        mainPanel.add(multifactorPanel, gbh.nextRowFirstCol().leftGap(5).get());
        mainPanel.add(sshPanel, gbh.nextRow().get());
        mainPanel.add(buttonsPanel, gbh.nextRow().fillNone().get());
        mainPanel.add(new JPanel(), gbh.nextRow().setMaxWeightY().fillBoth().spanY().get());

        // --- tab pane ---

        tabPane.setTabPlacement(JTabbedPane.BOTTOM);
        tabPane.addTab(bundleString("Basic"), new JScrollPane(mainPanel));
        tabPane.addTab(bundleString("Advanced"), propertiesPanel);

        // --- base ---

        gbh = new GridBagHelper().fillBoth().spanX().spanY();
        add(tabPane, gbh.get());
    }

    @Override
    protected void initComponentsLists() {
        basicAuthComponents.addAll(Arrays.asList(
                roleField,
                userField,
                passwordField,
                storePasswordCheck,
                encryptPasswordCheck,
                getNearComponent(roleField, -6),
                getNearComponent(userField, -6),
                getNearComponent(passwordField, -6)
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

        KeyListener keyListener = new KeyHandler();
        roleField.addKeyListener(keyListener);
        sshHostField.addKeyListener(keyListener);
        sshPortField.addKeyListener(keyListener);
        sshUserField.addKeyListener(keyListener);
        sshPasswordField.addKeyListener(keyListener);

        namesToUpperCheck.addActionListener(this::handleEvent);
        useSshCheck.addActionListener(this::useSshCheckTriggered);
        storeSshPasswordCheck.addActionListener(this::handleEvent);
        useNewApiCheck.addActionListener(this::useNewApiCheckTriggered);
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
        if (e.getStateChange() == ItemEvent.SELECTED)
            useNewApiCheckTriggered(new ActionEvent(useNewApiCheck, -1, null));
    }

    @Override
    protected void handleEvent(AWTEvent event) {

        boolean processed = false;
        if (event == null || connection == null)
            return;

        Object source = event.getSource();
        if (Objects.equals(source, roleField)) {
            connection.setRole(roleField.getText().trim());
            processed = true;

        } else if (Objects.equals(source, namesToUpperCheck)) {
            connection.setNamesToUpperCase(namesToUpperCheck.isSelected());
            processed = true;

        } else if (Objects.equals(source, useSshCheck)) {
            connection.setSshTunnel(useSshCheck.isSelected());
            processed = true;

        } else if (Objects.equals(source, useNewApiCheck)) {
            connection.setUseNewAPI(useNewApiCheck.isSelected());
            processed = true;

        } else if (Objects.equals(source, sshHostField)) {
            connection.setSshHost(sshHostField.getText().trim());
            processed = true;

        } else if (Objects.equals(source, sshPortField)) {
            connection.setSshPort(sshPortField.getValue());
            processed = true;

        } else if (Objects.equals(source, sshUserField)) {
            connection.setSshUserName(sshUserField.getText().trim());
            processed = true;

        } else if (Objects.equals(source, sshPasswordField)) {
            connection.setSshPassword(MiscUtils.charsToString(sshPasswordField.getPassword()));
            processed = true;

        } else if (Objects.equals(source, storeSshPasswordCheck)) {
            connection.setSshPasswordStored(storeSshPasswordCheck.isSelected());
            processed = true;
        }

        if (processed) {
            storeJdbcProperties();
            return;
        }

        super.handleEvent(event);
    }

    @Override
    protected void populateConnectionObject() {

        if (connection == null)
            return;

        // --- basic ---

        connection.setRole(roleField.getText());
        connection.setUseNewAPI(useNewApiCheck.isSelected());
        connection.setNamesToUpperCase(namesToUpperCheck.isSelected());

        // --- ssh ---

        connection.setSshHost(sshHostField.getText());
        connection.setSshPort(sshPortField.getValue());
        connection.setSshUserName(sshUserField.getText());
        connection.setSshTunnel(useSshCheck.isSelected());
        connection.setSshPasswordStored(storeSshPasswordCheck.isSelected());
        connection.setSshPassword(MiscUtils.charsToString(sshPasswordField.getPassword()));

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
        useNewApiCheckTriggered(new ActionEvent(useNewApiCheck, -1, null));
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

    // --- handlers ---

    private void useSshCheckTriggered(ActionEvent e) {

        boolean enabled = useSshCheck.isSelected();
        if (enabled) {
            populateConnectionObject();
            setSshValues(connection);

            sshPanel.setVisible(true);
            sshRequired.forEach(RequiredFieldPainter::enable);

        } else {
            sshPanel.setVisible(false);
            sshRequired.forEach(RequiredFieldPainter::disable);
        }

        handleEvent(e);
    }

    private void useNewApiCheckTriggered(ActionEvent e) {
        DatabaseDriver selectedDriver = (DatabaseDriver) driverCombo.getSelectedItem();
        int majorVersion = selectedDriver != null ? selectedDriver.getMajorVersion() : -1;

        if (majorVersion < 4 && useNewApiCheck.isSelected()) {
            GUIUtilities.displayWarningMessage(bundleString("warning.useNewAPI"));
            useNewApiCheck.setSelected(false);
        }

        handleEvent(e);
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
        populateAndSave();
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
    }

    /**
     * Action implementation on selection of the Connect button.
     */
    private void connect() {

        if (!canConnect())
            return;

        try {
            populateAndSave();
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
        }
    }

    private void testConnection() {

        if (!canConnect())
            return;

        try {
            populateAndSave();
            GUIUtilities.showWaitCursor();

            if (ConnectionMediator.getInstance().test(connection))
                GUIUtilities.displayInformationMessage(bundleString("test.success"));

        } catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("error.connect", e.getMessage()), e, this.getClass());

        } finally {
            GUIUtilities.showNormalCursor();
        }
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
        passwordField.setPassword(dc.getUnencryptedPassword());
        verifyCertCheck.setSelected(dc.isVerifyServerCertCheck());
        encryptPasswordCheck.setSelected(dc.isPasswordEncrypted());
        containerPasswordField.setPassword(dc.getContainerPassword());
        portField.setText(dc.getPort().isEmpty() ? "3050" : dc.getPort());
        storeContPasswordCheck.setSelected(dc.isContainerPasswordStored());
        hostField.setText(dc.getHost().isEmpty() ? "localhost" : dc.getHost());

        selectActualDriver();
        useSshCheckTriggered(null);
        enableFields(connection.isConnected());
        propertiesPanel.setConnection(connection);
    }

    public void addTab(String title, Component component) {
        tabPane.addTab(title, component);
    }

    // ---

    private class FocusHandler implements FocusListener {
        boolean block;

        @Override
        public void focusGained(FocusEvent e) {
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

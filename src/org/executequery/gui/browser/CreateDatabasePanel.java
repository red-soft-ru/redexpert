package org.executequery.gui.browser;

import biz.redsoft.IFBCreateDatabase;
import biz.redsoft.IFBCryptoPluginInit;
import org.executequery.*;
import org.executequery.databasemediators.ConnectionType;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseConnectionFactory;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.databasemediators.spi.DatabaseConnectionFactoryImpl;
import org.executequery.event.*;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.DynamicLibraryLoader;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.Objects;

/**
 * @author Vasiliy Yashkov
 * @since 10.07.2015
 */
public class CreateDatabasePanel extends AbstractConnectionPanel {

    public static final String TITLE = Bundles.get(AbstractConnectionPanel.class, "CreateDatabase");
    public static final String FRAME_ICON = "icon_create_db";

    // --- gui components ---

    private JButton createButton;
    private JComboBox<String> pageSizeCombo;

    // ---

    public CreateDatabasePanel(BrowserController controller) {
        super(controller);
    }

    @Override
    protected void init() {
        super.init();

        createButton = WidgetFactory.createButton("createButton", Bundles.get("common.create.button"), e -> create());

        pageSizeCombo = WidgetFactory.createComboBox("pageSizeCombo", new String[]{"4096", "8192", "16384"});
        pageSizeCombo.setSelectedItem("8192");
        pageSizeCombo.setEditable(false);
    }

    @Override
    protected void arrange() {
        super.arrange();

        GridBagHelper gbh;

        // --- check panel ---

        JPanel checkPanel = WidgetFactory.createPanel("checkPanel");

        gbh = new GridBagHelper().fillHorizontally();
        checkPanel.add(storePasswordCheck, gbh.get());
        checkPanel.add(encryptPasswordCheck, gbh.nextCol().leftGap(5).get());
        checkPanel.add(new JPanel(), gbh.nextCol().setMaxWeightX().spanX().get());

        // --- left panel ---

        JPanel leftPanel = WidgetFactory.createPanel("leftPanel");

        gbh = new GridBagHelper().setMinWeightX().anchorNorthWest().fillHorizontally();
        leftPanel.add(WidgetFactory.createLabel(bundleString("nameField")), gbh.get());
        leftPanel.add(WidgetFactory.createLabel(bundleString("connTypeCombo")), gbh.nextRow().topGap(5).get());
        leftPanel.add(WidgetFactory.createLabel(bundleString("hostField")), gbh.nextRow().get());
        leftPanel.add(WidgetFactory.createLabel(bundleString("portField")), gbh.nextRow().get());
        leftPanel.add(WidgetFactory.createLabel(bundleString("fileField")), gbh.nextRow().get());
        leftPanel.add(WidgetFactory.createLabel(bundleString("charsetsCombo")), gbh.nextRow().get());
        leftPanel.add(WidgetFactory.createLabel(bundleString("pageSizeCombo")), gbh.nextRow().get());

        gbh.setY(0).nextCol().leftGap(5).topGap(0).setMaxWeightX().spanX();
        leftPanel.add(nameField, gbh.get());
        leftPanel.add(connTypeCombo, gbh.nextRow().topGap(5).get());
        leftPanel.add(hostField, gbh.nextRow().get());
        leftPanel.add(portField, gbh.nextRow().get());
        leftPanel.add(fileField, gbh.nextRow().setWidth(1).get());
        leftPanel.add(browseFileButton, gbh.nextCol().setMinWeightX().get());
        leftPanel.add(charsetsCombo, gbh.nextRow().previousCol().spanX().get());
        leftPanel.add(pageSizeCombo, gbh.nextRow().get());

        // --- right panel ---

        JPanel rightPanel = WidgetFactory.createPanel("rightPanel");

        gbh = new GridBagHelper().setMinWeightX().anchorNorthWest().fillHorizontally();
        rightPanel.add(WidgetFactory.createLabel(bundleString("driverCombo")), gbh.get());
        rightPanel.add(WidgetFactory.createLabel(bundleString("serverCombo")), gbh.nextRow().topGap(5).get());
        rightPanel.add(WidgetFactory.createLabel(bundleString("authCombo")), gbh.nextRow().get());
        rightPanel.add(WidgetFactory.createLabel(bundleString("userField")), gbh.nextRow().get());
        rightPanel.add(WidgetFactory.createLabel(bundleString("passwordField")), gbh.nextRow().get());

        gbh.setY(0).nextCol().leftGap(5).topGap(0).setMaxWeightX();
        rightPanel.add(driverCombo, gbh.get());
        rightPanel.add(addDriverButton, gbh.nextCol().setMinWeightX().get());
        rightPanel.add(serverCombo, gbh.nextRow().previousCol().topGap(5).spanX().get());
        rightPanel.add(authCombo, gbh.nextRow().get());
        rightPanel.add(userField, gbh.nextRow().get());
        rightPanel.add(userPasswordField, gbh.nextRow().get());
        rightPanel.add(checkPanel, gbh.nextRow().nextCol().leftGap(2).get());
        rightPanel.add(new JPanel(), gbh.nextRow().setMaxWeightY().spanY().get());

        // --- main panel ---

        JPanel mainPanel = WidgetFactory.createPanel("mainPanel");

        gbh = new GridBagHelper().anchorNorthWest().setInsets(5, 5, 5, 5).fillBoth();
        mainPanel.add(leftPanel, gbh.setMaxWeightX().get());
        mainPanel.add(rightPanel, gbh.nextCol().get());
        mainPanel.add(multifactorPanel, gbh.nextRowFirstCol().spanX().get());
        mainPanel.add(createButton, gbh.nextRowFirstCol().setMinWeightX().fillNone().get());
        mainPanel.add(new JPanel(), gbh.nextRow().fillBoth().setMaxWeightY().spanX().spanY().get());

        // --- tab pane ---

        tabPane.setTabPlacement(JTabbedPane.BOTTOM);
        tabPane.addTab(bundleString("Basic"), mainPanel);
        tabPane.addTab(bundleString("Advanced"), propertiesPanel);

        // --- base ---

        gbh = new GridBagHelper().fillBoth().spanX().spanY();
        add(tabPane, gbh.get());
    }

    // ---

    public void create() {

        if (anyRequiredFieldEmpty())
            return;

        String connectionName = nameField.getText().trim();
        if (connectionNameExists(connectionName)) {
            focusNameField();
            return;
        }

        populateAndSave();
        GUIUtilities.showWaitCursor();

        DatabaseDriver databaseDriver = (DatabaseDriver) driverCombo.getSelectedItem();
        if (databaseDriver == null || !databaseDriver.getClassName().contains("FBDriver")) {
            GUIUtilities.displayWarningMessage(bundleString("driverNotSupported"));
            return;
        }

        createDatabase(databaseDriver, connectionName);
    }

    private void createDatabase(DatabaseDriver databaseDriver, String connectionName) {

        Object dbObject = null;
        try (URLClassLoader classLoader = new URLClassLoader(MiscUtils.loadURLs(databaseDriver.getPath()))) {

            Class<?> driverClass = classLoader.loadClass(databaseDriver.getClassName());
            Driver driver = (Driver) driverClass.newInstance();
            int driverVersion = driver.getMajorVersion();

            Log.info("Database creation via jaybird");
            Log.info("Driver version: " + driverVersion + "." + driver.getMinorVersion());

            if (driverVersion < 3) {
                GUIUtilities.displayWarningMessage(bundleString("driverVersionNotSupported"));
                return;
            }

            try {
                Object cryptoPluginObject = DynamicLibraryLoader.loadingObjectFromClassLoader(driverVersion, driver, "FBCryptoPluginInitImpl");
                IFBCryptoPluginInit cryptoPlugin = (IFBCryptoPluginInit) cryptoPluginObject;
                cryptoPlugin.init();

            } catch (NoSuchMethodError | Exception | UnsatisfiedLinkError | NoClassDefFoundError e) {
                Log.warning("Unable to initialize cryptographic plugin");
                Log.warning("Authentication using cryptographic mechanisms will not be available.");
                Log.warning("Please install the crypto pro library to enable cryptographic modules.");
            }

            dbObject = DynamicLibraryLoader.loadingObjectFromClassLoader(driverVersion, driver, "FBCreateDatabaseImpl");

        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }

        IFBCreateDatabase createDatabase = (IFBCreateDatabase) dbObject;
        if (createDatabase == null)
            throw new NullPointerException("DynamicLibraryLoader::loadingObjectFromClassLoader return null");

        String user = userField.getText();
        String path = fileField.getText();
        String server = hostField.getText();
        String password = userPasswordField.getPassword();
        String charset = (String) charsetsCombo.getSelectedItem();
        int port = portField.getValue() != 0 ? portField.getValue() : 3050;
        int pageSize = Integer.parseInt(Objects.requireNonNull((String) pageSizeCombo.getSelectedItem()));

        createDatabase.setPort(port);
        createDatabase.setUser(user);
        createDatabase.setServer(server);
        createDatabase.setEncoding(charset);
        createDatabase.setPassword(password);
        createDatabase.setDatabaseName(path);
        createDatabase.setPageSize(pageSize);
        storeJdbcProperties(createDatabase);

        try {
            createDatabase.exec();

        } catch (UnsatisfiedLinkError e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("fbclientNotFound"), e, this.getClass());
            return;

        } catch (Throwable e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("connectionNotEstablished"), e, this.getClass());
            return;

        } finally {
            GUIUtilities.showNormalCursor();
            System.gc();
        }

        int result = GUIUtilities.displayYesNoDialog(bundleString("registration.message"), bundleString("registration"));
        if (result == JOptionPane.YES_OPTION) {

            DatabaseConnectionFactory databaseConnectionFactory = new DatabaseConnectionFactoryImpl();
            DatabaseConnection databaseConnection = databaseConnectionFactory.create(connectionName);

            databaseConnection.setHost(server);
            databaseConnection.setUserName(user);
            databaseConnection.setCharset(charset);
            storeJdbcProperties(databaseConnection);
            databaseConnection.setPassword(password);
            databaseConnection.setPort(portField.getText());
            databaseConnection.setJDBCDriver(databaseDriver);
            databaseConnection.setCertificate(certField.getText());
            databaseConnection.setDriverId(databaseDriver.getId());
            databaseConnection.setDriverName(databaseDriver.getName());
            databaseConnection.setPasswordStored(storePasswordCheck.isSelected());
            databaseConnection.setAuthMethod((String) authCombo.getSelectedItem());
            databaseConnection.setContainerPassword(contPasswordField.getPassword());
            databaseConnection.setVerifyServerCertCheck(verifyCertCheck.isSelected());
            databaseConnection.setAuthMethodMode((String) serverCombo.getSelectedItem());
            databaseConnection.setPasswordEncrypted(encryptPasswordCheck.isSelected());
            databaseConnection.setSourceName(path.replace("\\", "/"));
            databaseConnection.setContainerPasswordStored(storeContPasswordCheck.isSelected());

            ConnectionType connectionType = (ConnectionType) connTypeCombo.getSelectedItem();
            databaseConnection.setConnType(connectionType != null ? connectionType.name() : null);

            JPanel tabComponent = GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY);
            if (tabComponent instanceof ConnectionsTreePanel)
                ((ConnectionsTreePanel) tabComponent).newConnection(databaseConnection);
        }

        GUIUtilities.closeSelectedCentralPane();
    }

    // ---

    private void populateAndSave() {
        populateConnectionObject();
        EventMediator.fireEvent(new DefaultConnectionRepositoryEvent(
                this, ConnectionRepositoryEvent.CONNECTION_MODIFIED, (DatabaseConnection) null
        ));
    }

}

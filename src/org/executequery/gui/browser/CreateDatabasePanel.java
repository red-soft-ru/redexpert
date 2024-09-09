package org.executequery.gui.browser;

import biz.redsoft.IFBCreateDatabase;
import biz.redsoft.IFBCryptoPluginInit;
import org.executequery.*;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseConnectionFactory;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.databasemediators.spi.DatabaseConnectionFactoryImpl;
import org.executequery.event.*;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.Repository;
import org.executequery.repository.RepositoryCache;
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
        leftPanel.add(WidgetFactory.createLabel(bundleString("hostField")), gbh.nextRow().topGap(5).get());
        leftPanel.add(WidgetFactory.createLabel(bundleString("portField")), gbh.nextRow().get());
        leftPanel.add(WidgetFactory.createLabel(bundleString("fileField")), gbh.nextRow().get());
        leftPanel.add(WidgetFactory.createLabel(bundleString("charsetsCombo")), gbh.nextRow().get());
        leftPanel.add(WidgetFactory.createLabel(bundleString("pageSizeCombo")), gbh.nextRow().get());

        gbh.setY(0).nextCol().leftGap(5).topGap(0).setMaxWeightX().spanX();
        leftPanel.add(nameField, gbh.get());
        leftPanel.add(hostField, gbh.nextRow().topGap(5).get());
        leftPanel.add(portField, gbh.nextRow().get());
        leftPanel.add(fileField, gbh.nextRow().setWidth(1).get());
        leftPanel.add(browseFileButton, gbh.nextCol().setMinWeightX().get());
        leftPanel.add(charsetsCombo, gbh.nextRow().previousCol().spanX().get());
        leftPanel.add(pageSizeCombo, gbh.nextRow().get());

        // --- right panel ---

        JPanel rightPanel = WidgetFactory.createPanel("rightPanel");

        gbh = new GridBagHelper().setMinWeightX().anchorNorthWest().fillHorizontally();
        rightPanel.add(WidgetFactory.createLabel(bundleString("driverCombo")), gbh.get());
        rightPanel.add(WidgetFactory.createLabel(bundleString("authCombo")), gbh.nextRow().topGap(5).get());
        rightPanel.add(WidgetFactory.createLabel(bundleString("userField")), gbh.nextRow().get());
        rightPanel.add(WidgetFactory.createLabel(bundleString("passwordField")), gbh.nextRow().get());

        gbh.setY(0).nextCol().leftGap(5).topGap(0).setMaxWeightX();
        rightPanel.add(driverCombo, gbh.get());
        rightPanel.add(addDriverButton, gbh.nextCol().setMinWeightX().get());
        rightPanel.add(authCombo, gbh.nextRow().previousCol().topGap(5).spanX().get());
        rightPanel.add(userField, gbh.nextRow().get());
        rightPanel.add(passwordField, gbh.nextRow().get());
        rightPanel.add(checkPanel, gbh.nextRow().nextCol().leftGap(2).get());
        rightPanel.add(new JPanel(), gbh.nextRow().setMaxWeightY().spanY().get());

        // --- base ---

        gbh = new GridBagHelper().anchorNorthWest().setInsets(5, 5, 5, 5).fillBoth();
        add(leftPanel, gbh.setMaxWeightX().get());
        add(rightPanel, gbh.nextCol().get());
        add(multifactorPanel, gbh.nextRowFirstCol().spanX().get());
        add(createButton, gbh.nextRowFirstCol().setMinWeightX().fillNone().get());
        add(new JPanel(), gbh.nextRow().fillBoth().setMaxWeightY().spanX().spanY().get());
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
        String charset = (String) charsetsCombo.getSelectedItem();
        int port = portField.getValue() != 0 ? portField.getValue() : 3050;
        String password = MiscUtils.charsToString(passwordField.getPassword());
        int pageSize = Integer.parseInt(Objects.requireNonNull((String) pageSizeCombo.getSelectedItem()));

        createDatabase.setPort(port);
        createDatabase.setUser(user);
        createDatabase.setServer(server);
        storeJdbcProperties(createDatabase);
        createDatabase.setEncoding(charset);
        createDatabase.setPassword(password);
        createDatabase.setDatabaseName(path);
        createDatabase.setPageSize(pageSize);

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
            databaseConnection.setVerifyServerCertCheck(verifyCertCheck.isSelected());
            databaseConnection.setPasswordEncrypted(encryptPasswordCheck.isSelected());
            databaseConnection.setSourceName(path.replace("\\", "/"));
            databaseConnection.setContainerPasswordStored(storeContPasswordCheck.isSelected());
            databaseConnection.setContainerPassword(MiscUtils.charsToString(containerPasswordField.getPassword()));

            JPanel tabComponent = GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY);
            if (tabComponent instanceof ConnectionsTreePanel)
                ((ConnectionsTreePanel) tabComponent).newConnection(databaseConnection);
        }

        GUIUtilities.closeSelectedCentralPane();
    }

    private boolean connectionNameExists(String connectionName) {

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

    // ---

    private void populateAndSave() {
        populateConnectionObject();
        EventMediator.fireEvent(new DefaultConnectionRepositoryEvent(
                this, ConnectionRepositoryEvent.CONNECTION_MODIFIED, (DatabaseConnection) null
        ));
    }

    /**
     * Populates the values of the selected connection
     * properties object with the field values.
     */
    private void populateConnectionObject() {

        if (connection == null)
            return;

        connection.setHost(hostField.getText());
        connection.setPort(portField.getText());
        connection.setUserName(userField.getText());
        connection.setCertificate(certField.getText());
        connection.setPasswordStored(storePasswordCheck.isSelected());
        connection.setAuthMethod((String) authCombo.getSelectedItem());
        connection.setCharset((String) charsetsCombo.getSelectedItem());
        connection.setVerifyServerCertCheck(verifyCertCheck.isSelected());
        connection.setPasswordEncrypted(encryptPasswordCheck.isSelected());
        connection.setContainerPasswordStored(storeContPasswordCheck.isSelected());
        connection.setPassword(MiscUtils.charsToString(passwordField.getPassword()));
        connection.setContainerPassword(MiscUtils.charsToString(containerPasswordField.getPassword()));

        DatabaseDriver driver = (DatabaseDriver) driverCombo.getSelectedItem();
        if (driver != null) {
            connection.setJDBCDriver(driver);
            connection.setDriverId(driver.getId());
            connection.setDriverName(driver.getName());
            connection.setDatabaseType(Integer.toString(driver.getType()));

        } else {
            connection.setDriverId(0);
            connection.setJDBCDriver(null);
            connection.setDriverName(null);
            connection.setDatabaseType(null);
        }

        storeJdbcProperties();
        propertiesPanel.getTransactionIsolationLevel();
        checkNameUpdate();
    }

    /**
     * Checks the current selection for a name change
     * to be propagated back to the tree view.
     */
    private void checkNameUpdate() {

        String newName = nameField.getText().trim();
        if (connectionNameExists(newName)) {
            focusNameField();
            return;
        }

        String oldName = connection.getName();
        if (!oldName.equals(newName)) {
            connection.setName(newName);
            controller.nodeNameValueChanged(host);
        }
    }

    // ---

    private void focusNameField() {
        nameField.requestFocusInWindow();
        nameField.selectAll();
    }

}

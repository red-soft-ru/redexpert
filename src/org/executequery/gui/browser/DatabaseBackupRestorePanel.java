package org.executequery.gui.browser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.AbstractDockedTabPanel;
import org.executequery.gui.LoggingOutputPanel;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.backup.DatabaseBackupPanel;
import org.executequery.gui.browser.backup.DatabaseRestorePanel;
import org.executequery.gui.browser.backup.FileBrowser;
import org.executequery.gui.browser.backup.InvalidBackupFileException;
import org.executequery.listeners.SimpleDocumentListener;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.ConnectionsComboBox;
import org.underworldlabs.swing.ViewablePasswordField;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.ParameterSaver;

/**
 * A panel that provides backup and restore functionality for a database. Users can select a connection, and then either
 * perform a backup or a restore operation. Logging options are also available.
 *
 * @author Maxim Kozhinov
 */
public class DatabaseBackupRestorePanel extends AbstractDockedTabPanel {
    public static final String TITLE = bundleString("title");
    public static final String BACKUP_ICON = "icon_backup_restore";
    public static final String BACKUP_FILE = "lastBackupFilePath";

    private ParameterSaver parameterSaver;

    // --- gui components ---

    private DatabaseBackupPanel backupHelper;
    private DatabaseRestorePanel restoreHelper;

    private ConnectionsComboBox connectionCombo;
    private ViewablePasswordField passwordField;
    private JComboBox<String> charsetsCombo;
    private JTextField databaseFileField;
    private JTextField hostField;
    private JTextField portField;
    private JTextField userField;

    protected JButton fileLogButton;
    protected JButton browseDatabaseButton;

    protected transient FileOutputStream fileLog;
    protected JTextField fileLogField;
    protected JCheckBox logToFileBox;

    private LoggingOutputPanel loggingOutputPanel;

    // ---

    public DatabaseBackupRestorePanel() {
        init();
        arrange();
        initParameterSaver();
        changeDatabaseConnection();

        parameterSaver.restore();
        logToFileBoxTriggered(null);
    }

    /**
     * Initializes the components used in the panel, such as connection fields, log components, etc.
     */
    private void init() {
        parameterSaver = new ParameterSaver(DatabaseBackupRestorePanel.class.getName());

        backupHelper = new DatabaseBackupPanel(parameterSaver);
        backupHelper.getBackupButton().addActionListener(e -> runDaemon("backup", this::performBackup));
        restoreHelper = new DatabaseRestorePanel(parameterSaver);
        restoreHelper.getRestoreButton().addActionListener(e -> runDaemon("restore", this::performRestore));

        // Initialize connection-related fields
        connectionCombo = WidgetFactory.createConnectionComboBox("connectionCombo", false);
        charsetsCombo = WidgetFactory.createComboBox("charsetsCombo", loadCharsets());
        passwordField = WidgetFactory.createViewablePasswordField("passwordField");
        databaseFileField = WidgetFactory.createTextField("databaseFileField");
        hostField = WidgetFactory.createTextField("hostField");
        portField = WidgetFactory.createTextField("portField");
        userField = WidgetFactory.createTextField("userField");

        fileLogButton = WidgetFactory.createButton("fileLogButtonBackup", "...");
        fileLogButton.addActionListener(this::chooseLoggingFile);
        logToFileBox = WidgetFactory.createCheckBox("logToFileBoxBackup", bundleString("LogToFile"));
        logToFileBox.addActionListener(this::logToFileBoxTriggered);
        fileLogField = WidgetFactory.createTextField("fileLogFieldBackup");

        loggingOutputPanel = new LoggingOutputPanel();
        loggingOutputPanel.setBorder(BorderFactory.createTitledBorder(bundleString("loggingOutput")));
        loggingOutputPanel.clear();

        browseDatabaseButton = WidgetFactory.createButton("browseDatabaseButton", "...");
        browseDatabaseButton.addActionListener(e -> browseDatabase());

        connectionCombo.addActionListener(e -> changeDatabaseConnection());
        hostField.getDocument().addDocumentListener(new SimpleDocumentListener(this::hostChanged));
    }

    private void initParameterSaver() {
        Map<String, Component> components = new HashMap<>();
        components.put(logToFileBox.getName(), logToFileBox);
        components.put(fileLogField.getName(), fileLogField);

        parameterSaver.add(components);
    }

    /// Triggered when the <code>hostField</code> value changed.<br>Enables or disables browse file buttons.
    private void hostChanged() {
        String host = hostField.getText().trim();
        boolean isLocalhost = Objects.equals(host, "localhost") || Objects.equals(host, "127.0.0.1");

        browseDatabaseButton.setEnabled(isLocalhost);
        backupHelper.getBrowseBackupFileButton().setEnabled(isLocalhost);
        restoreHelper.getBrowseBackupFileButton().setEnabled(isLocalhost);
    }

    /**
     * Triggered when the "Log to file" checkbox is selected or deselected. Enables or disables file logging.
     *
     * @param event The action event from the checkbox.
     */
    private void logToFileBoxTriggered(ActionEvent event) {
        boolean enabled = logToFileBox.isSelected();
        fileLogField.setEnabled(enabled);
        fileLogButton.setEnabled(enabled);
        closeFileLog();
    }

    /**
     * Helper method to choose a log file.
     *
     * @param event ActionEvent from the button press.
     */
    private void chooseLoggingFile(ActionEvent event) {
        final FileChooserDialog fileChooser = new FileChooserDialog();
        int returnVal = fileChooser.showSaveDialog(fileLogButton);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            closeFileLog();  // Close any previously opened file log
            File file = fileChooser.getSelectedFile();
            fileLogField.setText(file.getAbsolutePath());
            try {
                fileLog = new FileOutputStream(file, false);
                fileLog.close();  // Immediately close after initializing
                fileLog = null;
            } catch (IOException e) {
                Log.error("Error occurred while handling the log file", e);
            }
        }
    }

    /**
     * Opens a file chooser dialog for selecting a database file.
     */
    private void browseDatabase() {

        String defaultFileName = databaseFileField.getText();
        if (MiscUtils.isNull(defaultFileName))
            defaultFileName = "database.fdb";

        FileNameExtensionFilter fbkFilter = new FileNameExtensionFilter(Bundles.get("common.fdb.files"), "fdb");
        FileBrowser fileBrowser = new FileBrowser(bundleString("databaseFileSelection"), fbkFilter, defaultFileName);

        String filePath = fileBrowser.getChosenFilePath();
        if (filePath != null) {
            String originalExtension = FilenameUtils.getExtension(filePath);
            if (MiscUtils.isNull(originalExtension))
                filePath += ".fdb";

            databaseFileField.setText(filePath);
        }
    }

    /**
     * Arrange the layout of the panel, dividing into a common section for connection and logging and a tabbed pane for
     * backup and restore operations.
     */
    private void arrange() {
        setLayout(new GridBagLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(bundleString("Backup"), backupHelper.arrange());
        tabbedPane.addTab(bundleString("Restore"), restoreHelper.arrange());
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1)
                restoreHelper.updateBackupFilePath();
        });

        JPanel commonPanel = createCommonPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, commonPanel, tabbedPane);
        splitPane.setResizeWeight(0.5);

        GridBagHelper gbh = new GridBagHelper().setInsets(5, 0, 5, 0).fillBoth().spanX().spanY();
        add(splitPane, gbh.get());
    }

    /**
     * Creates the common panel on the left side for connection fields, logging options, and logs.
     *
     * @return JPanel for the common fields.
     */
    private JPanel createCommonPanel() {
        JPanel commonPanel = WidgetFactory.createPanel("commonPanel");
        GridBagHelper gbh = new GridBagHelper().setInsets(0, 5, 5, 0).anchorNorthWest().fillHorizontally();

        addFieldWithLabel(commonPanel, gbh, bundleString("connections"), connectionCombo);
        commonPanel.add(WidgetFactory.createLabel(bundleString("database")), gbh.nextRowFirstCol().setWidth(1).setMinWeightX().get());
        commonPanel.add(databaseFileField, gbh.nextCol().setMaxWeightX().get());
        commonPanel.add(browseDatabaseButton, gbh.nextCol().setMinWeightX().get());
        addFieldWithLabel(commonPanel, gbh, bundleString("host"), hostField);
        addFieldWithLabel(commonPanel, gbh, bundleString("port"), portField);
        addFieldWithLabel(commonPanel, gbh, bundleString("username"), userField);
        addFieldWithLabel(commonPanel, gbh, bundleString("password"), passwordField);
        addFieldWithLabel(commonPanel, gbh, bundleString("charset"), charsetsCombo);

        commonPanel.add(logToFileBox, gbh.nextRowFirstCol().setWidth(1).setMinWeightX().get());
        commonPanel.add(fileLogField, gbh.nextCol().setMaxWeightX().get());
        commonPanel.add(fileLogButton, gbh.nextCol().setMinWeightX().get());

        gbh.setMaxWeightX().setMaxWeightY().fillBoth().spanX().spanY();
        commonPanel.add(loggingOutputPanel, gbh.nextRowFirstCol().bottomGap(5).get());

        return commonPanel;
    }

    /**
     * Helper method to add a field and its label to the common panel.
     *
     * @param panel The panel to which the field is added.
     * @param gbh   GridBagHelper instance for layout.
     * @param label The label for the field.
     * @param field The JTextField to be added.
     */
    private void addFieldWithLabel(JPanel panel, GridBagHelper gbh, String label, JComponent field) {
        panel.add(WidgetFactory.createLabel(label), gbh.nextRowFirstCol().setWidth(1).setMinWeightX().get());
        panel.add(field, gbh.nextCol().setMaxWeightX().spanX().get());
    }

    /**
     * Switches the UI fields based on the selected database connection.
     */
    private void changeDatabaseConnection() {
        DatabaseConnection dc = (DatabaseConnection) connectionCombo.getSelectedItem();
        if (dc != null) {
            passwordField.setPassword(dc.getUnencryptedPassword());
            databaseFileField.setText(dc.getSourceName());
            charsetsCombo.setSelectedItem(dc.getCharset());
            userField.setText(dc.getUserName());
            hostField.setText(dc.getHost());
            portField.setText(dc.getPort());
        } else {
            resetConnectionFields();
        }
    }

    /**
     * Resets all connection fields to their default empty state.
     */
    private void resetConnectionFields() {
        charsetsCombo.setSelectedItem("NONE");
        databaseFileField.setText("");
        passwordField.setPassword("");
        userField.setText("");
        hostField.setText("");
        portField.setText("3050");  // Default port
    }

    /**
     * Performs the backup operation, using the backupHelper to manage the backup process.
     */
    private void performBackup() {
        try (LoggingOutputPanel.LoggingStream loggingStream = loggingOutputPanel.getLoggingStream()) {
            loggingStream.setLogFilePath(logToFileBox.isSelected() ? fileLogField.getText() : null);

            if (backupHelper.performBackup(getDatabaseConnection(), loggingStream))
                GUIUtilities.displayInformationMessage(bundleString("backupSucceed"));

        } catch (InvalidBackupFileException e) {
            GUIUtilities.displayWarningMessage(e.getMessage());

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("backupFailed", e.getMessage()), e, getClass());
        }
    }

    /**
     * Performs the restore operation, using the restoreHelper to manage the restore process.
     */
    private void performRestore() {
        try (LoggingOutputPanel.LoggingStream loggingStream = loggingOutputPanel.getLoggingStream()) {
            loggingStream.setLogFilePath(logToFileBox.isSelected() ? fileLogField.getText() : null);

            if (restoreHelper.performRestore(getDatabaseConnection(), loggingStream))
                GUIUtilities.displayInformationMessage(bundleString("restoreSucceed"));

        } catch (InvalidBackupFileException e) {
            GUIUtilities.displayWarningMessage(e.getMessage());

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("restoreFailed", e.getMessage()), e, getClass());
        }
    }

    /**
     * Closes the file log stream if it is open.
     */
    private void closeFileLog() {
        if (fileLog != null) {
            try {
                fileLog.close();
                fileLog = null;
            } catch (IOException e) {
                Log.error(e.getMessage(), e);
            }
        }
    }

    /// Loads character sets from the <code>charsets.properties</code> file
    private List<String> loadCharsets() {
        List<String> charsets;

        try {
            String resource = FileUtils.loadResource("org/executequery/charsets.properties");
            charsets = Arrays.stream(resource.split("\n"))
                    .filter(s -> !MiscUtils.isNull(s))
                    .filter(s -> !s.startsWith("#"))
                    .sorted().collect(Collectors.toList());
            charsets.add(0, "NONE");

        } catch (Exception e) {
            Log.error(e.getMessage(), e);
            charsets = Collections.singletonList("NONE");
        }

        return charsets;
    }

    /**
     * Returns copy of the selected <code>DatabaseConnection</code>
     * with the properties from the connection fields
     */
    private DatabaseConnection getDatabaseConnection() {
        DatabaseConnection dc = connectionCombo.getSelectedConnection().copy();
        dc.setCharset(charsetsCombo.getSelectedIndex() != 0 ? (String) charsetsCombo.getSelectedItem() : "UTF8");
        dc.setPassword(String.valueOf(passwordField.getPassword()));
        dc.setSourceName(databaseFileField.getText());
        dc.setUserName(userField.getText());
        dc.setHost(hostField.getText());
        dc.setPort(portField.getText());

        return dc;
    }

    /// Returns last used backup file path or default (<code>~/backup.fbk</code>)
    public static String getLastBackupFilePath(ParameterSaver parameterSaver) {

        String lastBackupFilePath = parameterSaver.getProperties().get(BACKUP_FILE);
        if (MiscUtils.isNull(lastBackupFilePath)) {
            lastBackupFilePath = System.getProperty("user.home") + FileSystems.getDefault().getSeparator() + "backup.fbk";
            parameterSaver.getProperties().put(BACKUP_FILE, lastBackupFilePath).save();
        }

        return lastBackupFilePath;
    }

    /// Executes <code>Runnable</code> as background process via <code>SwingWorker</code>
    private void runDaemon(String action, Runnable r) {
        DatabaseConnection dc = getDatabaseConnection();
        SwingWorker.run(String.format("Performing '%s' %s", dc.getSourceName(), action), r);
    }

    /**
     * Utility method to retrieve localized strings.
     *
     * @param key The key for the string.
     * @return The localized string.
     */
    public static String bundleString(String key, Object... args) {
        return Bundles.get(DatabaseBackupRestorePanel.class, key, args);
    }

    // --- TabView impl ---

    @Override
    public boolean tabViewClosing() {
        parameterSaver.save();
        return true;
    }

    // --- DockedTabView impl ---

    @Override
    public String getPropertyKey() {
        return null;
    }

    @Override
    public String getMenuItemKey() {
        return "database-backup-restore-command";
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

}
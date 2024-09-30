package org.executequery.gui.browser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import javax.swing.*;

import org.executequery.components.FileChooserDialog;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.LoggingOutputPanel;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.backup.DatabaseBackupPanel;
import org.executequery.gui.browser.backup.DatabaseRestorePanel;
import org.executequery.gui.browser.backup.InvalidBackupFileException;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.ConnectionsComboBox;
import org.underworldlabs.swing.layouts.GridBagHelper;

/**
 * A panel that provides backup and restore functionality for a database. Users can select a connection, and then either
 * perform a backup or a restore operation. Logging options are also available.
 * @author Maxim Kozhinov
 */
public class DatabaseBackupRestorePanel extends JPanel {

    public static final String TITLE = bundleString("title");
    public static final String BACKUP_ICON = "icon_backup_restore";
    private static final String ERROR_TITLE = bundleString("Error");

    private DatabaseBackupPanel backupHelper;
    private DatabaseRestorePanel restoreHelper;

    private ConnectionsComboBox dbMasterComboBox;
    private JTextField databaseFileField;
    private JTextField hostField;
    private JTextField portField;
    private JTextField userField;
    private JPasswordField passwordField;

    protected transient FileOutputStream fileLog;
    protected JButton fileLogButton;
    protected JTextField fileLogField;
    protected JCheckBox logToFileBox;

    private LoggingOutputPanel loggingOutputPanel;

    public DatabaseBackupRestorePanel() {
        init();
        arrange();
        logToFileBoxTriggered(null);
    }

    /**
     * Initializes the components used in the panel, such as connection fields, log components, etc.
     */
    private void init() {
        backupHelper = new DatabaseBackupPanel();
        backupHelper.getBackupButton().addActionListener(e -> performBackup());
        restoreHelper = new DatabaseRestorePanel();
        restoreHelper.getRestoreButton().addActionListener(e -> performRestore());

        // Initialize connection-related fields
        dbMasterComboBox = WidgetFactory.createConnectionComboBox("dbMasterComboBox", false);
        databaseFileField = WidgetFactory.createTextField("databaseFileField");
        hostField = WidgetFactory.createTextField("hostField");
        portField = WidgetFactory.createTextField("portField");
        userField = WidgetFactory.createTextField("userField");
        passwordField = WidgetFactory.createPasswordField("passwordField");

        fileLogButton = WidgetFactory.createButton("fileLogButtonBackup", "...");
        fileLogButton.addActionListener(this::chooseLoggingFile);
        logToFileBox = WidgetFactory.createCheckBox("logToFileBoxBackup", bundleString("LogToFile"));
        logToFileBox.addActionListener(this::logToFileBoxTriggered);
        fileLogField = WidgetFactory.createTextField("fileLogFieldBackup");

        loggingOutputPanel = new LoggingOutputPanel();
        loggingOutputPanel.setBorder(BorderFactory.createTitledBorder(bundleString("loggingOutput")));
        loggingOutputPanel.clear();

        dbMasterComboBox.addActionListener(e -> changeDatabaseConnection());
    }

    /**
     * Triggered when the "Log to file" checkbox is selected or deselected. Enables or disables file logging.
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
     * Arrange the layout of the panel, dividing into a common section for connection and logging and a tabbed pane for
     * backup and restore operations.
     */
    private void arrange() {
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(bundleString("Backup"), backupHelper.arrange());
        tabbedPane.addTab(bundleString("Restore"), restoreHelper.arrange());

        JPanel commonPanel = createCommonPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, commonPanel, tabbedPane);
        splitPane.setResizeWeight(0.5);

        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Creates the common panel on the left side for connection fields, logging options, and logs.
     * @return JPanel for the common fields.
     */
    private JPanel createCommonPanel() {
        JPanel commonPanel = WidgetFactory.createPanel("commonPanel");
        GridBagHelper gbh = new GridBagHelper().bottomGap(5).anchorNorthWest().fillHorizontally();

        commonPanel.add(WidgetFactory.createLabel(bundleString("connections")), gbh.setMinWeightX().leftGap(0).get());
        commonPanel.add(dbMasterComboBox, gbh.nextCol().setMaxWeightX().leftGap(5).spanX().get());

        addFieldWithLabel(commonPanel, gbh, bundleString("database"), databaseFileField);
        addFieldWithLabel(commonPanel, gbh, bundleString("host"), hostField);
        addFieldWithLabel(commonPanel, gbh, bundleString("port"), portField);
        addFieldWithLabel(commonPanel, gbh, bundleString("username"), userField);
        addFieldWithLabel(commonPanel, gbh, bundleString("password"), passwordField);

        commonPanel.add(logToFileBox, gbh.nextRowFirstCol().setWidth(1).setMinWeightX().get());
        commonPanel.add(fileLogField, gbh.nextCol().setMaxWeightX().leftGap(5).get());
        commonPanel.add(fileLogButton, gbh.nextCol().setMinWeightX().get());

        commonPanel.add(loggingOutputPanel,
                        gbh.nextRowFirstCol().setMaxWeightX().setMaxWeightY().fillBoth().spanX().spanY().get());

        return commonPanel;
    }

    /**
     * Helper method to add a field and its label to the common panel.
     * @param panel The panel to which the field is added.
     * @param gbh   GridBagHelper instance for layout.
     * @param label The label for the field.
     * @param field The JTextField to be added.
     */
    private void addFieldWithLabel(JPanel panel, GridBagHelper gbh, String label, JTextField field) {
        panel.add(WidgetFactory.createLabel(label), gbh.nextRowFirstCol().setWidth(1).setMinWeightX().get());
        panel.add(field, gbh.nextCol().setMaxWeightX().leftGap(5).spanX().get());
    }

    /**
     * Switches the UI fields based on the selected database connection.
     */
    private void changeDatabaseConnection() {
        DatabaseConnection dc = (DatabaseConnection) dbMasterComboBox.getSelectedItem();
        if (dc != null) {
            databaseFileField.setText(dc.getSourceName());
            userField.setText(dc.getUserName());
            passwordField.setText(dc.getUnencryptedPassword());
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
        databaseFileField.setText("");
        userField.setText("");
        passwordField.setText("");
        hostField.setText("");
        portField.setText("3050");  // Default port
    }

    /**
     * Performs the backup operation, using the backupHelper to manage the backup process.
     */
    private void performBackup() {
        try (ByteArrayOutputStream backupOutputStream = new ByteArrayOutputStream()) {
            backupHelper.performBackup(dbMasterComboBox.getSelectedConnection(), backupOutputStream);
            JOptionPane.showMessageDialog(this, bundleString("backupSucceed"), bundleString("Success"),
                                          JOptionPane.INFORMATION_MESSAGE);
            populateLogs(backupOutputStream);
        } catch (InvalidBackupFileException e) {
            showErrorDialog(e.getMessage());
        } catch (Exception ex) {
            backupHelper.getProgressBar().setValue(0);
            showErrorDialog(bundleString("backupFailed") + " " + ex.getMessage());
        }
    }

    /**
     * Performs the restore operation, using the restoreHelper to manage the restore process.
     */
    private void performRestore() {
        try (ByteArrayOutputStream restoreOutputStream = new ByteArrayOutputStream()) {
            restoreHelper.performRestore(dbMasterComboBox.getSelectedConnection(), restoreOutputStream);
            JOptionPane.showMessageDialog(this, bundleString("restoreSucceed"), bundleString("Success"),
                                          JOptionPane.INFORMATION_MESSAGE);
            populateLogs(restoreOutputStream);
        } catch (InvalidBackupFileException e) {
            showErrorDialog(e.getMessage());
        } catch (Exception ex) {
            restoreHelper.getProgressBar().setValue(0);
            showErrorDialog(bundleString("restoreFailed") + " " + ex.getMessage());
        }
    }

    /**
     * Populates the logs into the logging panel and optionally writes them to the file.
     * @param os ByteArrayOutputStream containing the logs.
     */
    private void populateLogs(ByteArrayOutputStream os) {
        try {
            String logs = os.toString();
            loggingOutputPanel.append(logs);
            if (logToFileBox.isSelected()) {
                String logFilePath = fileLogField.getText();
                Path path = Paths.get(logFilePath);
                if (Files.notExists(path.getParent())) {
                    Files.createDirectories(path.getParent());
                }
                Files.write(path, logs.getBytes(), StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (IOException e) {
            Log.error(e.getMessage(), e);
            showErrorDialog(e.getMessage());
        }
    }

    /**
     * Displays an error dialog with the specified message.
     * @param message The error message to display.
     */
    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
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

    /**
     * Utility method to retrieve localized strings.
     * @param key The key for the string.
     * @return The localized string.
     */
    public static String bundleString(String key) {
        return Bundles.get(DatabaseBackupRestorePanel.class, key);
    }
}
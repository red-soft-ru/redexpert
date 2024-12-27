package org.executequery.gui.browser.backup;

import biz.redsoft.IFBBackupManager;

import java.awt.*;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.DatabaseBackupRestorePanel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.ParameterSaver;

import static org.executequery.gui.browser.DatabaseBackupRestorePanel.BACKUP_FILE;

/**
 * This class is responsible for creating the user interface panel for database backup operations. It provides options
 * for customizing the backup process, such as ignoring checksums, disabling garbage collection, and using
 * non-transportable backups. The panel also includes input fields for specifying the backup file and the number of
 * parallel workers.
 *
 * @author Maxim Kozhinov
 */
public class DatabaseBackupPanel implements Serializable {
    private final transient ParameterSaver parameterSaver;

    private JTextField backupFileField;
    private JButton browseBackupFileButton;
    private JButton backupButton;
    private JCheckBox ignoreChecksumsCheckBox;
    private JCheckBox noGarbageCollectCheckBox;
    private JCheckBox metadataOnlyCheckBox;
    private JCheckBox nonTransportableCheckBox;
    private JSpinner workersSpinner;

    /**
     * Constructs a new DatabaseBackupPanel and initializes the UI components.
     */
    public DatabaseBackupPanel(ParameterSaver parameterSaver) {
        this.parameterSaver = parameterSaver;
        init();
    }

    /**
     * Initializes the UI components used in the panel, including buttons, text fields, checkboxes, and the progress
     * bar.
     */
    private void init() {
        createFileChooserComponents();
        createBackupOptions();
        initParameterSaver();
    }

    /**
     * Creates and initializes components for file selection and backup buttons.
     */
    private void createFileChooserComponents() {
        browseBackupFileButton = WidgetFactory.createButton("browseBackupFileButton", "...");
        browseBackupFileButton.addActionListener(e -> browseBackupFile());
        backupButton = WidgetFactory.createButton("backupButton", bundleString("backupButton"));

        backupFileField = WidgetFactory.createTextField("backupFileField", DatabaseBackupRestorePanel.getLastBackupFilePath(parameterSaver));
    }

    /**
     * Creates and initializes components for backup options like checkboxes and parallel workers field.
     */
    private void createBackupOptions() {
        ignoreChecksumsCheckBox = WidgetFactory.createCheckBox("ignoreChecksumsCheckBox",
                bundleString("ignoreChecksumsCheckBox"));
        noGarbageCollectCheckBox = WidgetFactory.createCheckBox("noGarbageCollectCheckBox",
                bundleString("noGarbageCollectCheckBox"));
        metadataOnlyCheckBox = WidgetFactory.createCheckBox("metadataOnlyCheckBox",
                bundleString("metadataOnlyCheckBox"));
        nonTransportableCheckBox = WidgetFactory.createCheckBox("nonTransportableCheckBox",
                bundleString("nonTransportableCheckBox"));

        workersSpinner = WidgetFactory.createSpinner("workersSpinner", 1024, SwingConstants.LEFT);
        ((JSpinner.NumberEditor) workersSpinner.getEditor()).getFormat().setGroupingUsed(false);
    }

    private void initParameterSaver() {
        Map<String, Component> components = new HashMap<>();
        components.put(buildName(noGarbageCollectCheckBox), noGarbageCollectCheckBox);
        components.put(buildName(nonTransportableCheckBox), nonTransportableCheckBox);
        components.put(buildName(ignoreChecksumsCheckBox), ignoreChecksumsCheckBox);
        components.put(buildName(metadataOnlyCheckBox), metadataOnlyCheckBox);
        components.put(buildName(workersSpinner), workersSpinner);

        parameterSaver.add(components);
    }

    /**
     * Arranges the components within the panel using a grid layout.
     *
     * @return JPanel containing the UI components for backup configuration.
     */
    public JPanel arrange() {
        JPanel backupPanel = WidgetFactory.createPanel("backupPanel");
        GridBagHelper gbh;

        JPanel checkBoxPanel = createCheckBoxPanel();
        JPanel textOptionsPanel = createTextOptionsPanel();
        JPanel buttonPanel = createButtonPanel();

        // Main panel layout
        JPanel mainPanel = WidgetFactory.createPanel("mainPanel");
        gbh = new GridBagHelper().setInsets(0, 5, 0, 15).anchorNorthWest().fillBoth();
        mainPanel.add(textOptionsPanel, gbh.setMinWeightY().spanX().get());
        mainPanel.add(checkBoxPanel, gbh.nextRowFirstCol().topGap(0).get());
        mainPanel.add(buttonPanel, gbh.nextRowFirstCol().get());
        mainPanel.add(new JPanel(), gbh.nextRowFirstCol().setMaxWeightY().get());

        gbh = new GridBagHelper().anchorNorthWest().fillBoth();
        backupPanel.add(mainPanel, gbh.setMaxWeightX().setMaxWeightY().get());

        return backupPanel;
    }

    /**
     * Creates the panel for the backup options checkboxes.
     *
     * @return JPanel containing the checkboxes.
     */
    private JPanel createCheckBoxPanel() {
        JPanel checkBoxPanel = WidgetFactory.createPanel("checkBoxPanel");
        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        checkBoxPanel.add(ignoreChecksumsCheckBox, gbh.get());
        checkBoxPanel.add(noGarbageCollectCheckBox, gbh.nextCol().leftGap(5).get());
        checkBoxPanel.add(metadataOnlyCheckBox, gbh.nextRowFirstCol().leftGap(0).get());
        checkBoxPanel.add(nonTransportableCheckBox, gbh.nextCol().leftGap(5).get());
        checkBoxPanel.add(new JPanel(), gbh.nextCol().setMaxWeightX().spanX().get());
        return checkBoxPanel;
    }

    /**
     * Creates the panel for the text options (parallel workers and backup file fields).
     *
     * @return JPanel containing the text input fields.
     */
    private JPanel createTextOptionsPanel() {

        JPanel textOptionsPanel = WidgetFactory.createPanel("textOptionsPanel");
        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();

        textOptionsPanel.add(WidgetFactory.createLabel(bundleString("backupFile")), gbh.setMinWeightX().get());
        textOptionsPanel.add(backupFileField, gbh.nextCol().leftGap(5).setMaxWeightX().get());
        textOptionsPanel.add(browseBackupFileButton, gbh.nextCol().setMinWeightX().get());

        textOptionsPanel.add(WidgetFactory.createLabel(bundleString("parallelWorkersAmount")), gbh.nextRowFirstCol().leftGap(0).topGap(5).setWidth(1).setMinWeightX().get());
        textOptionsPanel.add(workersSpinner, gbh.nextCol().setMaxWeightX().leftGap(5).spanX().get());

        return textOptionsPanel;
    }

    /**
     * Creates the panel for the backup action button.
     *
     * @return JPanel containing the backup button.
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = WidgetFactory.createPanel("buttonPanel");
        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        buttonPanel.add(backupButton, gbh.setMinWeightX().get());
        buttonPanel.add(new JPanel(), gbh.nextCol().setMaxWeightX().spanX().get());
        return buttonPanel;
    }

    /**
     * Initiates the backup process using the selected database connection and writes the output to the provided output
     * stream.
     *
     * @param dc The database connection to be backed up.
     * @param os The output stream where the backup will be written.
     * @throws InvalidBackupFileException If the backup file name is invalid.
     */
    public boolean performBackup(DatabaseConnection dc, OutputStream os)
            throws InvalidBackupFileException, SQLException, ClassNotFoundException {

        String backupFileName = getNewFileName();
        if (FileUtils.fileExists(backupFileName)) {

            GUIUtilities.displayWarningMessage(bundleString("backupOverrideNotSupport"));
            return false;

            /* todo add when backup file override will be available
            (method available in jaybird 3.0.37+, 4.0.31+, 5.0.22+)

            if (!supportsBackupOverride(dc) || GUIUtilities.displayYesNoDialog() != JOptionPane.YES_OPTION)
                return false;

            // BackupManager#setBackupReplace method call
             */
        }

        int workersCount = (int) workersSpinner.getValue();
        int options = getCheckBoxOptions();

        parameterSaver.getProperties().put(BACKUP_FILE, backupFileName).save();
        DatabaseBackupRestoreService.backupDatabase(dc, backupFileName, options, workersCount, os);
        return true;
    }

    /**
     * Retrieves the options selected by the user via checkboxes and returns them as an integer.
     *
     * @return The combined options as an integer.
     */
    private int getCheckBoxOptions() {
        int options = 0;
        if (ignoreChecksumsCheckBox.isSelected()) {
            options |= IFBBackupManager.BACKUP_IGNORE_CHECKSUMS;
            Log.info("Ignore bad checksums for backup");
        }
        if (noGarbageCollectCheckBox.isSelected()) {
            options |= IFBBackupManager.BACKUP_NO_GARBAGE_COLLECT;
            Log.info("Garbage collection is disabled for backup");
        }
        if (metadataOnlyCheckBox.isSelected()) {
            options |= IFBBackupManager.BACKUP_METADATA_ONLY;
            Log.info("Backup will only contain metadata");
        }
        if (nonTransportableCheckBox.isSelected()) {
            options |= IFBBackupManager.BACKUP_NON_TRANSPORTABLE;
            Log.info("Backup will be non-transportable to other platforms");
        }
        return options;
    }

    /**
     * Opens a file chooser dialog for selecting a backup file and sets the chosen path in the backupFileField.
     */
    private void browseBackupFile() {

        String defaultFileName = backupFileField.getText();
        if (MiscUtils.isNull(defaultFileName))
            defaultFileName = DatabaseBackupRestorePanel.getLastBackupFilePath(parameterSaver);

        FileNameExtensionFilter fbkFilter = new FileNameExtensionFilter(Bundles.get("common.fbk.files"), "fbk");
        FileBrowser fileBrowser = new FileBrowser(bundleString("backupFileSelection"), fbkFilter, defaultFileName);

        String filePath = fileBrowser.getChosenFilePath();
        if (filePath == null)
            return;

        String originalExtension = FilenameUtils.getExtension(filePath);
        if (MiscUtils.isNull(originalExtension))
            filePath += ".fbk";

        backupFileField.setText(filePath);
    }

    /**
     * Checks if the given database connection supports backup override functionality.
     * This feature is supported in RedDatabase 5.0.1 and above, but not in Firebird.
     *
     * @param dc The DatabaseConnection object to check for backup override support.
     * @return true if the connected database supports backup override, false otherwise.
     */
    private boolean supportsBackupOverride(DatabaseConnection dc) {

        // backup override supports since RDB 5.0.1 (FB doesn't support)
        String serverName = new DefaultDatabaseHost(dc).getDatabaseProductName();
        boolean supportsBackupOverride = !MiscUtils.isNull(serverName)
                && serverName.toLowerCase().contains("reddatabase")
                && dc.getMajorServerVersion() > 4
                && dc.getMinorServerVersion() > 0;

        if (!supportsBackupOverride)
            GUIUtilities.displayWarningMessage(bundleString("backupOverrideNotSupport"));

        return supportsBackupOverride;
    }

    /**
     * Retrieves and validates the backup file name entered by the user.
     *
     * @return The validated backup file name.
     * @throws InvalidBackupFileException If the backup file name is invalid or empty.
     */
    private String getNewFileName() throws InvalidBackupFileException {
        String fileName = backupFileField.getText();
        FileValidator.createValidator(fileName)
                .notEmpty()
                .hasExtension(".fbk");
        return fileName;
    }

    /**
     * Returns the backup button, allowing external classes to trigger the backup process.
     *
     * @return The JButton used for creating backups.
     */
    public JButton getBackupButton() {
        return backupButton;
    }

    public JButton getBrowseBackupFileButton() {
        return browseBackupFileButton;
    }

    private String buildName(Component component) {
        return component.getName() + ".backup";
    }

    /**
     * Utility method to retrieve localized strings.
     *
     * @param key The key for the string.
     * @return The localized string.
     */
    public static String bundleString(String key) {
        return Bundles.get(DatabaseBackupRestorePanel.class, key);
    }
}
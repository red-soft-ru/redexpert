package org.executequery.gui.browser.backup;

import biz.redsoft.IFBBackupManager;

import java.io.OutputStream;
import java.io.Serializable;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;

/**
 * This class is responsible for creating the user interface panel for database restore operations. It provides options
 * for customizing the restore process, such as deactivating indices, disabling shadow tables, and restoring metadata
 * only. The panel also includes input fields for specifying the restore and backup file names, page size, and the
 * number of parallel workers.
 *
 * @author Maxim Kozhinov
 */
public class DatabaseRestorePanel implements Serializable {

    private JTextField restoredFileField;
    private JTextField backupFileField;
    private JButton restoreButton;
    private JButton browseBackupFileButton;
    private JButton browseRestoreFileButton;
    private JProgressBar progressBar;
    private JCheckBox deactivateIdxCheckBox;
    private JCheckBox noShadowCheckBox;
    private JCheckBox noValidityCheckBox;
    private JCheckBox metadataOnlyCheckBox;
    private JCheckBox oneAtATimeCheckBox;
    private NumberTextField pageSizeField;
    private NumberTextField parallelWorkersField;

    /**
     * Constructs a new DatabaseRestorePanel and initializes the UI components.
     */
    public DatabaseRestorePanel() {
        init();
    }

    /**
     * Initializes the UI components used in the panel, including buttons, text fields, checkboxes, and the progress
     * bar.
     */
    private void init() {
        createFileChooserComponents();
        createRestoreOptions();
        createProgressComponents();
    }

    /**
     * Creates and initializes components for file selection and restore buttons.
     */
    private void createFileChooserComponents() {
        browseBackupFileButton = WidgetFactory.createButton("browseBackupFileButton", "...");
        browseBackupFileButton.addActionListener(e -> browseBackupFile());

        browseRestoreFileButton = WidgetFactory.createButton("browseRestoreFileButton", "...");
        browseRestoreFileButton.addActionListener(e -> browseRestoreFile());

        backupFileField = WidgetFactory.createTextField("backupFileField");
        restoredFileField = WidgetFactory.createTextField("restoredFileField");
        restoreButton = WidgetFactory.createButton("restoreButton", bundleString("restoreButton"));
    }

    /**
     * Creates and initializes components for restore options like checkboxes and page size/parallel workers fields.
     */
    private void createRestoreOptions() {
        deactivateIdxCheckBox = WidgetFactory.createCheckBox("deactivateIdxCheckBox",
                bundleString("deactivateIdxCheckBox"));
        noShadowCheckBox = WidgetFactory.createCheckBox("noShadowCheckBox", bundleString("noShadowCheckBox"));
        noValidityCheckBox = WidgetFactory.createCheckBox("noValidityCheckBox", bundleString("noValidityCheckBox"));
        metadataOnlyCheckBox = WidgetFactory.createCheckBox("metadataOnlyCheckBox",
                bundleString("metadataOnlyCheckBox"));
        oneAtATimeCheckBox = WidgetFactory.createCheckBox("oneAtATimeCheckBox", bundleString("oneAtATimeCheckBox"));
        pageSizeField = WidgetFactory.createNumberTextField("pageSizeRestore", "8192");
        parallelWorkersField = WidgetFactory.createNumberTextField("parallelWorkersRestore", "1");
    }

    /**
     * Creates and initializes progress bar components for restore progress.
     */
    private void createProgressComponents() {
        progressBar = WidgetFactory.createProgressBar("restoreProgressBar");
        progressBar.setStringPainted(true);
    }

    /**
     * Arranges the components within the panel using a grid layout.
     *
     * @return JPanel containing the UI components for restore configuration.
     */
    public JPanel arrange() {
        JPanel restorePanel = WidgetFactory.createPanel("restorePanel");
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
        mainPanel.add(progressBar, gbh.nextRowFirstCol().bottomGap(6).setMinWeightY().get());

        gbh = new GridBagHelper().anchorNorthWest().fillBoth();
        restorePanel.add(mainPanel, gbh.setMaxWeightX().setMaxWeightY().get());

        return restorePanel;
    }

    /**
     * Creates the panel for the restore options checkboxes.
     *
     * @return JPanel containing the checkboxes.
     */
    private JPanel createCheckBoxPanel() {
        JPanel checkBoxPanel = WidgetFactory.createPanel("checkBoxPanel");
        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        checkBoxPanel.add(deactivateIdxCheckBox, gbh.get());
        checkBoxPanel.add(metadataOnlyCheckBox, gbh.nextCol().leftGap(5).get());
        checkBoxPanel.add(noShadowCheckBox, gbh.nextCol().get());
        checkBoxPanel.add(noValidityCheckBox, gbh.nextRowFirstCol().leftGap(0).topGap(5).get());
        checkBoxPanel.add(oneAtATimeCheckBox, gbh.nextCol().setWidth(2).leftGap(5).get());
        checkBoxPanel.add(new JPanel(), gbh.nextCol().setMaxWeightX().spanX().get());
        return checkBoxPanel;
    }

    /**
     * Creates the panel for the text options (page size, parallel workers, and file fields).
     *
     * @return JPanel containing the text input fields.
     */
    private JPanel createTextOptionsPanel() {

        JPanel textOptionsPanel = WidgetFactory.createPanel("textOptionsPanel");
        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();

        textOptionsPanel.add(WidgetFactory.createLabel(bundleString("backupFileField")), gbh.setMinWeightX().get());
        textOptionsPanel.add(backupFileField, gbh.nextCol().leftGap(5).setMaxWeightX().get());
        textOptionsPanel.add(browseBackupFileButton, gbh.nextCol().setMinWeightX().get());

        textOptionsPanel.add(WidgetFactory.createLabel(bundleString("restoredFileField")), gbh.nextRowFirstCol().leftGap(0).topGap(5).setWidth(1).setMinWeightX().get());
        textOptionsPanel.add(restoredFileField, gbh.nextCol().leftGap(5).setMaxWeightX().get());
        textOptionsPanel.add(browseRestoreFileButton, gbh.nextCol().setMinWeightX().get());

        textOptionsPanel.add(WidgetFactory.createLabel(bundleString("pageSizeField")), gbh.nextRowFirstCol().leftGap(0).setWidth(1).setMinWeightX().get());
        textOptionsPanel.add(pageSizeField, gbh.nextCol().leftGap(5).setMaxWeightX().spanX().get());

        textOptionsPanel.add(WidgetFactory.createLabel(bundleString("parallelWorkersField")), gbh.nextRowFirstCol().leftGap(0).setWidth(1).setMinWeightX().get());
        textOptionsPanel.add(parallelWorkersField, gbh.nextCol().leftGap(5).setMaxWeightX().spanX().get());

        return textOptionsPanel;
    }

    /**
     * Creates the panel for the restore action button.
     *
     * @return JPanel containing the restore button.
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = WidgetFactory.createPanel("buttonPanel");
        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        buttonPanel.add(restoreButton, gbh.setMinWeightX().get());
        buttonPanel.add(new JPanel(), gbh.nextCol().setMaxWeightX().spanX().get());
        return buttonPanel;
    }

    /**
     * Initiates the restore process using the selected database connection and writes the output to the provided output
     * stream.
     *
     * @param dc The database connection to be restored.
     * @param os The output stream where the restore will be written.
     * @throws InvalidBackupFileException If the backup or restore file name is invalid.
     */
    public void performRestore(DatabaseConnection dc, OutputStream os) throws InvalidBackupFileException {
        String fromFile = getBackupFileName();
        String toFile = getRestoreFileName();
        int pageSize = pageSizeField.getValue();
        int options = getCheckBoxOptions();
        int parallelWorkersCount = parallelWorkersField.getValue();

        DatabaseBackupRestoreService.restoreDatabase(dc, fromFile, toFile, options, pageSize, parallelWorkersCount, os);
        progressBar.setValue(100);
    }

    /**
     * Retrieves the options selected by the user via checkboxes and returns them as an integer.
     *
     * @return The combined options as an integer.
     */
    private int getCheckBoxOptions() {
        int options = 0;
        if (deactivateIdxCheckBox.isSelected()) {
            options |= IFBBackupManager.RESTORE_DEACTIVATE_IDX;
            Log.info("Indexes are deactivated during restore");
        }
        if (noShadowCheckBox.isSelected()) {
            options |= IFBBackupManager.RESTORE_NO_SHADOW;
            Log.info("No shadow tables will be recreated during restore");
        }
        if (noValidityCheckBox.isSelected()) {
            options |= IFBBackupManager.RESTORE_NO_VALIDITY;
            Log.info("Constraints validation is disabled during restore");
        }
        if (metadataOnlyCheckBox.isSelected()) {
            options |= IFBBackupManager.BACKUP_METADATA_ONLY;
            Log.info("Only metadata will be restored");
        }
        if (oneAtATimeCheckBox.isSelected()) {
            options |= IFBBackupManager.RESTORE_ONE_AT_A_TIME;
            Log.info("Restoring one table at a time is enabled");
        }
        return options;
    }

    /**
     * Opens a file chooser dialog for selecting a backup file and sets the chosen path in the backupFileField.
     */
    private void browseBackupFile() {

        String defaultFileName = backupFileField.getText();
        if (MiscUtils.isNull(defaultFileName))
            defaultFileName = "backup.fbk";

        FileNameExtensionFilter fbkFilter = new FileNameExtensionFilter(Bundles.get("common.fbk.files"), "fbk");
        FileBrowser fileBrowser = new FileBrowser(bundleString("backupFileSelection"), fbkFilter, defaultFileName);

        String filePath = fileBrowser.getChosenFilePath();
        if (filePath != null) {
            String originalExtension = FilenameUtils.getExtension(filePath);
            if (MiscUtils.isNull(originalExtension))
                filePath += ".fbk";

            backupFileField.setText(filePath);
        }
    }

    /**
     * Opens a file chooser dialog for selecting a restore file and sets the chosen path in the restoredFileField.
     */
    private void browseRestoreFile() {

        String defaultFileName = restoredFileField.getText();
        if (MiscUtils.isNull(defaultFileName))
            defaultFileName = "restored.fdb";

        FileNameExtensionFilter fdbFilter = new FileNameExtensionFilter(Bundles.get("common.fdb.files"), "fdb");
        FileBrowser fileBrowser = new FileBrowser(bundleString("restoreFileSelection"), fdbFilter, defaultFileName);

        String filePath = fileBrowser.getChosenFilePath();
        if (filePath != null) {
            String originalExtension = FilenameUtils.getExtension(filePath);
            if (MiscUtils.isNull(originalExtension))
                filePath += ".fdb";

            restoredFileField.setText(filePath);
        }
    }

    /**
     * Retrieves and validates the backup file name entered by the user.
     *
     * @return The validated backup file name.
     * @throws InvalidBackupFileException If the backup file name is invalid or empty.
     */
    private String getBackupFileName() throws InvalidBackupFileException {
        String fileName = backupFileField.getText();
        FileValidator.createValidator(fileName)
                .notEmpty()
                .hasExtension(".fbk");
        return fileName;
    }

    /**
     * Retrieves and validates the restore file name entered by the user.
     *
     * @return The validated restore file name.
     * @throws InvalidBackupFileException If the restore file name is invalid or empty.
     */
    private String getRestoreFileName() throws InvalidBackupFileException {
        String fileName = restoredFileField.getText();
        FileValidator.createValidator(fileName)
                .notEmpty()
                .hasExtension(".fdb");
        return fileName;
    }

    /**
     * Returns the restore button, allowing external classes to trigger the restore process.
     *
     * @return The JButton used for restoring databases.
     */
    public JButton getRestoreButton() {
        return restoreButton;
    }

    /**
     * Returns the progress bar used to track the progress of the restore process.
     *
     * @return The JProgressBar used to display restore progress.
     */
    public JProgressBar getProgressBar() {
        return progressBar;
    }

    /**
     * Utility method to retrieve localized strings.
     *
     * @param key The key for the string.
     * @return The localized string.
     */
    public static String bundleString(String key) {
        return Bundles.get(DatabaseRestorePanel.class, key);
    }
}
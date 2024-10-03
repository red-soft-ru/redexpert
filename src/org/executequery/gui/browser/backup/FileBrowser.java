package org.executequery.gui.browser.backup;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Utility class for handling file browsing operations with customized settings. Provides methods for selecting files
 * via a JFileChooser dialog.
 */
public class FileBrowser {

    private final JFileChooser fileChooser;

    /**
     * Constructs a FileBrowser with the specified title and file filter.
     * @param title      the title of the file chooser dialog
     * @param fileFilter the file filter to restrict file types in the chooser
     */
    public FileBrowser(String title, FileNameExtensionFilter fileFilter) {
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setDialogTitle(title);
        fileChooser.setFileFilter(fileFilter);
    }

    /**
     * Opens a file chooser dialog for selecting a file and returns the absolute file path.
     * @return the absolute path of the selected file, or null if no file is selected
     */
    public String getChosenFilePath() {
        return getChosenFilePath(JFileChooser.OPEN_DIALOG);
    }

    /**
     * Opens a file chooser dialog for selecting or saving a file, based on the specified mode.
     * @param dialogType the dialog type, either JFileChooser.OPEN_DIALOG or JFileChooser.SAVE_DIALOG
     * @return the absolute path of the selected file, or null if no file is selected
     */
    public String getChosenFilePath(int dialogType) {
        int result;

        if (dialogType == JFileChooser.SAVE_DIALOG) {
            result = fileChooser.showSaveDialog(null);
        } else {
            result = fileChooser.showOpenDialog(null);
        }

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            return selectedFile.getAbsolutePath();
        } else {
            return null;
        }
    }
}
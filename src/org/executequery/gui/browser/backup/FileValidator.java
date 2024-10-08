package org.executequery.gui.browser.backup;

import org.executequery.localization.Bundles;

import java.io.File;

/**
 * This class provides validation methods for file names. It follows a builder pattern, allowing for method chaining to
 * apply multiple validation rules.
 *
 * @author Maxim Kozhinov
 */
@SuppressWarnings("UnusedReturnValue")
public class FileValidator {

    private final String fileName;

    /**
     * Private constructor to initialize the validator with the file name to be validated.
     *
     * @param fileName The name of the file to validate.
     */
    private FileValidator(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Static factory method to create a new instance of FileValidator.
     *
     * @param fileName The file name to validate.
     * @return A new instance of FileValidator.
     */
    public static FileValidator createValidator(String fileName) {
        return new FileValidator(fileName);
    }

    /**
     * Validates that the file name is not empty or null.
     *
     * @return The current instance of FileValidator for method chaining.
     * @throws InvalidBackupFileException if the file name is null or empty.
     */
    public FileValidator notEmpty() throws InvalidBackupFileException {
        if (fileName == null || fileName.trim().isEmpty())
            throw new InvalidBackupFileException(bundleString("nameEmpty"));
        return this;
    }

    /**
     * Validates that the file has the specified extension.
     *
     * @param extension The required file extension (e.g., ".fbk").
     * @return The current instance of FileValidator for method chaining.
     * @throws InvalidBackupFileException if the file does not have the specified extension.
     */
    public FileValidator hasExtension(String extension) throws InvalidBackupFileException {
        if (!fileName.endsWith(extension))
            throw new InvalidBackupFileException(bundleString("extensionEmpty", extension));
        return this;
    }

    /**
     * Validates that the file exists in the file system.
     *
     * @return The current instance of FileValidator for method chaining.
     * @throws InvalidBackupFileException if the file does not exist.
     */
    public FileValidator exists() throws InvalidBackupFileException {
        if (!new File(fileName).exists())
            throw new InvalidBackupFileException(bundleString("fileNotExists"));
        return this;
    }

    private static String bundleString(String key, Object... args) {
        return Bundles.get(FileValidator.class, key, args);
    }

}
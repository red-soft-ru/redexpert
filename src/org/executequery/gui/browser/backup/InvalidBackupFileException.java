package org.executequery.gui.browser.backup;

/**
 * Exception thrown when a backup file validation fails. This can be due to reasons such as an empty file name,
 * incorrect file extension, or file not existing.
 * @author Maxim Kozhinov
 */
public class InvalidBackupFileException extends Exception {

    /**
     * Constructs a new InvalidBackupFileException with the specified detail message.
     * @param message The detail message explaining the reason for the exception.
     */
    public InvalidBackupFileException(String message) {
        super(message);
    }
}
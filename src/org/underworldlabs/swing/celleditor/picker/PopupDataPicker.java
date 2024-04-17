package org.underworldlabs.swing.celleditor.picker;

/**
 * Default DataPicker interface based on the
 * <code>com.github.lgooddatepicker.zinternaltools.CustomPopup</code> dependency
 * for custom data picker classes implementation
 *
 * @author Alexey Kozlov
 */
public interface PopupDataPicker {

    /**
     * Open the data picker popup panel
     */
    void openPopup();

    /**
     * Close the data picker popup panel
     */
    void closePopup();

    /**
     * Restores popup panel to null
     */
    void disposePopup();

    /**
     * Show if the data picker popup panel is active
     *
     * @return <code>true</code> if popup panel is active
     */
    boolean isPopupOpen();

    /**
     * Set up the new selected <code>String</code> value
     *
     * @param text value to set up
     */
    void setText(String text);

    /**
     * Get last selected value converted to the <code>String</code>
     *
     * @return last selected value
     */
    String getText();

    /**
     * Revert the selected value to default
     */
    void clear();

}

package org.underworldlabs.swing.celleditor.picker;

import com.github.lgooddatepicker.zinternaltools.CustomPopup;

/**
 * Default DataPicker interface based on the
 * <code>com.github.lgooddatepicker.zinternaltools.CustomPopup</code> dependency
 * for custom data picker classes implementation
 *
 * @author Alexey Kozlov
 */
public interface DataPicker extends CustomPopup.CustomPopupCloseListener {

    /**
     * Open the data picker popup panel
     */
    void openPopup();

    /**
     * Close the data picker popup panel
     */
    void closePopup();

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

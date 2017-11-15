/*
 * ImportExportMonitor.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.gui.importexport;

/** 
 *
 * @author   Takis Diakoumis
 */
public interface ImportExportMonitor {

    /**
     * Enables or disables the stop button as specified.
     *
     * @param true | false
     */
    void setStopButtonEnabled(final boolean enable);

    /**
     * Resets the progress bar to zero and clears the output text area.
     */
    void reset();

    /**
     * Returns the text currently displayed in the output pane.
     */
    String getText();

    /** 
     * Sets the progress bar's position during the process.
     *
     * @param the new process status
     */
    void setProgressStatus(int status);

    /** 
     * Retrieves the progress bar's maximum value.
     *
     * @param the progress bar's maximum value
     */
    int getMaximum();

    /** 
     * Sets the progress bar to track indeterminate values - action of
     * unknown length is taking place.
     */
    void setIndeterminate(boolean indeterminate);

    /** 
     * Sets the text to be appended within the progress info text area.
     *
     * @param the text to append
     */
    void appendProgressText(final String t);

    /** 
     * Sets the text to be appended within the
     * progress info text area as an error message.
     *
     * @param the text to append
     */
    void appendProgressErrorText(final String t);

    /** 
     * Sets the text to be appended within the progress info 
     * text area as a warning message.
     *
     * @param the text to append
     */
    void appendProgressWarningText(final String t);

    /** 
     * Sets the progress bar's minimum value to the specified value.
     *
     * @param the minimum value
     */
    void setMinimum(int min);

    /** 
     * Sets the progress bar's maximum value to the specified value.
     *
     * @param the maximum value
     */
    void setMaximum(int max);

}








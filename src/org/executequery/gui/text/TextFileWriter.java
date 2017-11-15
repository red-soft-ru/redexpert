/*
 * TextFileWriter.java
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

package org.executequery.gui.text;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.lang.StringUtils;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.event.DefaultFileIOEvent;
import org.executequery.event.FileIOEvent;
import org.executequery.gui.SaveFunction;
import org.executequery.io.SimpleTextFileWriter;
import org.underworldlabs.swing.FileSelector;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

/**
 *
 * @author   Takis Diakoumis
 */
public class TextFileWriter {
    
    /** The text to save */
    private String text;
    
    /** The path to save to */
    private String path;
    
    /** The selected file */
    private File selectedFile;

    private boolean showDialog;
    
    /** 
     *  Creates a new object with the specified text to be saved to the specified path.
     *
     *  @param the text to be saved
     *  @param the path to save to
     */
    public TextFileWriter(String text, File selectedFile) {
        this.text = text;
        this.selectedFile = selectedFile;
    }
    
    /** <p>Creates a new object with the specified text to
     *  be saved to the specified path.
     *
     *  @param the text to be saved
     *  @param the path to save to
     */
    public TextFileWriter(String text, String path) {
        this.text = text;
        this.path = path;
    }

    /** <p>Creates a new object with the specified text to
     *  be saved to the specified path.
     *
     *  @param the text to be saved
     *  @param the path to save to
     *  @param whether to force show the save dialog or not
     */
    public TextFileWriter(String text, String path, boolean showDialog) {
        this.text = text;
        this.path = path;
        this.showDialog = showDialog;
    }

    /** <p>Creates a new object with the specified text to save.
     *  A call to <code>write()</code> will open a file selector
     *  dialog to retrieve the path.
     *
     *  @param the text to be saved
     */
    public TextFileWriter(String text) {
        this.text = text;
    }
    
    /**
     * Writes the content to file.
     *
     * @return the result of the process:
     *         SaveFunction.SAVE_INVALID,
     *         SaveFunction.SAVE_FAILED,
     *         SaveFunction.SAVE_CANCELLED,
     *         SaveFunction.SAVE_COMPLETE
     */
    private int writeFile() {
        
        if (path == null || path.length() == 0) {
            
            return SaveFunction.SAVE_INVALID;
        }
        
        try {

            SimpleTextFileWriter writer = new SimpleTextFileWriter();
            writer.write(path, text, lineSeparator());

            fireFileOpened(path);

            return SaveFunction.SAVE_COMPLETE;

        } catch (IOException e) {

            String message = String.format("An error occurred saving to file." +
            		"\n\nThe system returned:\n%s", e.getMessage());

            GUIUtilities.displayExceptionErrorDialog(message, e);
            
            path = null;
            
            return SaveFunction.SAVE_FAILED;

        }

    }

    private LineSeparator lineSeparator() {

        return LineSeparator.valueForIndex(
                SystemProperties.getIntProperty("user", "general.line.separator"));
    }

    public int write() {

        if (showSaveDialog()) {

            int result = writeFile();

            String message = null;
            switch (result) {
                case SaveFunction.SAVE_COMPLETE:
                    message = "I/O process complete";
                    break;
                case SaveFunction.SAVE_FAILED:
                    message = "I/O process failed";
                    break;

                case SaveFunction.SAVE_INVALID:
                    message = "I/O process invalid";
                    break;
            }

            if (message != null) {
                
                setStatusText(message);
            }

            return result;

        } else {

            setStatusText("I/O process cancelled");
            return SaveFunction.SAVE_CANCELLED;
        }

    }
    
    private void setStatusText(String message) {
        
        GUIUtilities.getStatusBar().setSecondLabelText(message);
    }
    
    public File getSavedFile() {
        
        return new File(path);
    }
    
    private boolean showSaveDialog() {
        
        if (!showDialog && !MiscUtils.isNull(path)) { // already have path

            return true;
        }
        
        FileSelector textFiles = new FileSelector(new String[] {"txt"}, "Text files");
        FileSelector sqlFiles = new FileSelector(new String[] {"sql"}, "SQL files");

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.addChoosableFileFilter(textFiles);
        fileChooser.addChoosableFileFilter(sqlFiles);
        
        if (selectedFile != null) {
            
            fileChooser.setSelectedFile(selectedFile);
        }

        int result = fileChooser.showSaveDialog(GUIUtilities.getInFocusDialogOrWindow());
        if (result == JFileChooser.CANCEL_OPTION) {
            
            return false;
        }
        
        if (fileChooser.getSelectedFile() != null) {
            
            path = fileChooser.getSelectedFile().getAbsolutePath();
        }
        
        String extension = null;
        FileFilter filter = fileChooser.getFileFilter();
        if (filter == textFiles) {
            
            extension = ".txt";

        } else if (filter == sqlFiles) {
          
            extension = ".sql";
        }

        if (StringUtils.isNotBlank(extension) && !path.endsWith(extension)) {
            
            path += extension;
        }
        
        return true;
    }
    
    private void fireFileOpened(String file) {

        EventMediator.fireEvent(
                new DefaultFileIOEvent(this, FileIOEvent.OUTPUT_COMPLETE, file));
    }

}







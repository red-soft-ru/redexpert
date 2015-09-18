/*
 * ImportExportProgressPanel.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.executequery.components.LoggingOutputPane;
import org.executequery.sql.SqlMessages;

/** 
 * The progress display during an import/export
 * process. This is standard across all import/export
 * types - XML or delimited.
 *
 * @deprecated use NewImportExportProgressPanel
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ImportExportProgressPanel extends JPanel
                                       implements ActionListener {
    
    /** The stop button */
    private JButton stopButton;
    
    /** The progress bar tracking the process */
    private JProgressBar progressBar;
    
    /** The text area displaying process info */
    private LoggingOutputPane output;
    
    /** the parent process object */
    private ImportExportProcess parent;
    
    /** Constructs a new instance with the specified
     *  worker as the initiater and controller, and the
     *  whether the process is an export process.
     *
     *  @param The worker for this process
     *  @param Whether this is a export process
     */
    public ImportExportProgressPanel(ImportExportProcess parent) {
        super(new GridBagLayout());
        try {
            this.parent = parent;
            jbInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /** Initializes the state of this instance. */
    private void jbInit() throws Exception {
        
        JLabel transferLabel = new JLabel("Importing Data...");        
        if (parent.getTransferType() == ImportExportProcess.EXPORT) {
            transferLabel.setText("Exporting Data...");
        }

        output = new LoggingOutputPane();
        output.setBackground(getBackground());
        
        progressBar = new JProgressBar(0, 100);
        stopButton = new JButton("Stop");
        stopButton.addActionListener(this);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        add(transferLabel, gbc);
        gbc.gridy++;
        gbc.insets.top = 0;
        gbc.insets.right = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(progressBar, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.insets.right = 5;
        gbc.fill = GridBagConstraints.NONE;
        add(stopButton, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weighty = 1.0;
        gbc.insets.top = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        add(new JScrollPane(output), gbc);
    }

    /**
     * Enables or disables the stop button as specified.
     *
     * @param true | false
     */
    public void setStopButtonEnabled(final boolean enable) {
        Runnable updateStop = new Runnable() {
            public void run() {
                stopButton.setEnabled(enable);
            }
        };
        SwingUtilities.invokeLater(updateStop);
    }
    
    /**
     * Resets the progress bar to zero and clears the output text area.
     */
    public void reset() {
        progressBar.setValue(0);
        output.setText("");
        stopButton.setEnabled(true);
    }

    /**
     * Invoked when the stop button is pushed.
     */
    public void actionPerformed(ActionEvent e) {
        parent.stopTransfer();
        stopButton.setEnabled(false);
    }
    
    /**
     * Returns the text currently displayed in the output pane.
     */
    public String getText() {
        return output.getText();
    }
    
    /** 
     * Sets the progress bar's position during the process.
     *
     * @param the new process status
     */
    public void setProgressStatus(int status) {
        final int value = (status > 0 ? status : progressBar.getMaximum());
        Runnable setProgressBar = new Runnable() {
            public void run() {
                progressBar.setValue(value);
            }
        };
        SwingUtilities.invokeLater(setProgressBar);
    }
    
    /** 
     * Retrieves the progress bar's maximum value.
     *
     * @param the progress bar's maximum value
     */
    public int getMaximum() {
        return progressBar.getMaximum();
    }
    
    /** 
     * Sets the progress bar to track indeterminate values - action of
     * unknown length is taking place.
     */
    public void setIndeterminate(boolean indeterminate) {
        progressBar.setIndeterminate(indeterminate);
    }
    
    /** 
     * Sets the text to be appended within the progress info text area.
     *
     * @param the text to append
     */
    public void appendProgressText(final String t) {
        Runnable setProgressText = new Runnable() {
            public void run() {
                output.append(SqlMessages.PLAIN_MESSAGE_PREFORMAT, t);
            }
        };
        SwingUtilities.invokeLater(setProgressText);
    }

    /** 
     * Sets the text to be appended within the
     * progress info text area as an error message.
     *
     * @param the text to append
     */
    public void appendProgressErrorText(final String t) {
        Runnable setProgressText = new Runnable() {
            public void run() {
                output.append(SqlMessages.ERROR_MESSAGE_PREFORMAT, t);
            }
        };
        SwingUtilities.invokeLater(setProgressText);
    }

    /** 
     * Sets the text to be appended within the progress info 
     * text area as a warning message.
     *
     * @param the text to append
     */
    public void appendProgressWarningText(final String t) {
        Runnable setProgressText = new Runnable() {
            public void run() {
                output.append(SqlMessages.WARNING_MESSAGE_PREFORMAT, t);
            }
        };
        SwingUtilities.invokeLater(setProgressText);
    }

    /** 
     * Sets the progress bar's minimum value to the specified value.
     *
     * @param the minimum value
     */
    public void setMinimum(int min) {
        progressBar.setMaximum(min);
    }
    
    /** 
     * Sets the progress bar's maximum value to the specified value.
     *
     * @param the maximum value
     */
    public void setMaximum(int max) {
        progressBar.setMaximum(max);
    }
    
}







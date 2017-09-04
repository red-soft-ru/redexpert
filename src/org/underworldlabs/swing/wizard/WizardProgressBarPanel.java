/*
 * WizardProgressBarPanel.java
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

package org.underworldlabs.swing.wizard;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.StyledLogPane;

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 * Wizard progress bar panel rendering a progress bar and stop button.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class WizardProgressBarPanel extends JPanel 
                                    implements ActionListener {
    
    /** the stop button */
    private JButton stopButton;
    
    /** The progress bar tracking the process */
    private JProgressBar progressBar;

    /** the parent process */
    private InterruptibleWizardProcess parent;
    
    /** The text area displaying process info */
    private StyledLogPane output;

    
    /** Creates a new instance of WizardProgressBarPanel */
    public WizardProgressBarPanel(InterruptibleWizardProcess parent) {
        super(new GridBagLayout());
        this.parent = parent;
        
        output = new StyledLogPane();
        output.setBackground(getBackground());

        progressBar = new JProgressBar(0, 100);

        stopButton = new JButton("Stop");
        stopButton.addActionListener(this);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(progressBar, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.insets.left = 5;
        gbc.fill = GridBagConstraints.NONE;
        add(stopButton, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weighty = 1.0;
        gbc.insets.top = 10;
        gbc.insets.left = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        add(new JScrollPane(output), gbc);
    }

    /**
     * Sets the process as finished and disables the stop button.
     */
    public void finished() {
        setProgressStatus(-1);
        GUIUtils.invokeLater(new Runnable() {
            public void run() {
                stopButton.setEnabled(false);
            }
        });
    }
    
    /**
     * Resets the progress bar to zero and clears the output text area.
     */
    public void reset() {
        GUIUtils.invokeLater(new Runnable() {
            public void run() {
                progressBar.setValue(0);
                output.setText("");
                stopButton.setEnabled(true);
            }
        });
    }

    /**
     * Returns the text currently displayed in the output pane.
     */
    public String getText() {
        return output.getText();
    }
    
    /** 
     * Sets the text to be appended within the
     * progress info text area.
     *
     * @param the text to append
     */
    public void appendProgressText(final String t) {
        Runnable setProgressText = new Runnable() {
            public void run() {
                output.append(StyledLogPane.PLAIN_MESSAGE_PREFORMAT, t);
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
                output.append(StyledLogPane.ERROR_MESSAGE_PREFORMAT, t);
            }
        };
        SwingUtilities.invokeLater(setProgressText);
    }

    public void appendExceptionError(String message, Throwable e) {
        StringBuffer outputBuffer = new StringBuffer();
        if (message != null) {
            outputBuffer.append(message);
        }
        outputBuffer.append("\n[ ");

        String exceptionName = null;
        if (e.getCause() != null) {
            Throwable _e = e.getCause();
            exceptionName = _e.getClass().getName();
        } else {
            exceptionName = e.getClass().getName();
        }
        
        int index = exceptionName.lastIndexOf('.');
        if (index != -1) {
            exceptionName = exceptionName.substring(index+1);
        }
        outputBuffer.append(exceptionName);
        outputBuffer.append(" ] ");
        
        if (e instanceof DataSourceException) {
            outputBuffer.append(e.getMessage());
            outputBuffer.append(((DataSourceException)e).getExtendedMessage());
        }
        else if (e instanceof SQLException) {
            outputBuffer.append(e.getMessage());
            SQLException _e = (SQLException)e;
            outputBuffer.append("\nError Code: " + _e.getErrorCode());

            String state = _e.getSQLState();
            if (state != null) {
                outputBuffer.append("\nSQL State Code: " + state);
            }

        }
        else {
            outputBuffer.append(e.getMessage());
        }

        appendProgressErrorText(outputBuffer.toString());
    }


    /** 
     * Sets the progress bar's position during the process.
     * A value of -1 will set the progress bar to its set maximum.
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
     * Sets the progress bar's minimum value to the specified value.
     *
     * @param min - the minimum value
     */
    public void setMinimum(int min) {
        progressBar.setMinimum(min);
    }

    /** 
     * Sets the progress bar's maximum value to the specified value.
     *
     * @param max - the maximum value
     */
    public void setMaximum(int max) {
        progressBar.setMaximum(max);
    }

    /** 
     * Retrieves the progress bar's maximum value.
     *
     * @param the progress bar's maximum value
     */
    public int getMaximum() {
        return progressBar.getMaximum();
    }
    
    /** <p>Sets the progress bar to track
     *  indeterminate values - action of
     *  unknown length is taking place.
     */
    public void setIndeterminate(boolean indeterminate) {
        progressBar.setIndeterminate(indeterminate);
    }

    /**
     * Invoked when the stop button is pushed.
     */
    public void actionPerformed(ActionEvent e) {
        parent.stop();
        stopButton.setEnabled(false);
    }

}
















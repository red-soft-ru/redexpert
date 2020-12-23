/*
 * QueryEditorStatusBar.java
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

package org.executequery.gui.editor;

import org.executequery.localization.Bundles;
import org.underworldlabs.swing.AbstractStatusBarPanel;
import org.underworldlabs.swing.ProgressBar;
import org.underworldlabs.swing.ProgressBarFactory;

import javax.swing.*;

/**
 * Query Editor status bar panel.
 *
 * @author Takis Diakoumis
 */
public class QueryEditorStatusBar extends AbstractStatusBarPanel {

    /**
     * The buffer containing constantly changing values
     */
    private StringBuffer caretBuffer;

    /**
     * the progress bar
     */
    private ProgressBar progressBar;

    /**
     * the status bar panel fixed height
     */
    private static final int HEIGHT = 26;

    public QueryEditorStatusBar() {
        super(HEIGHT);
        init();
    }

    private void init() {

        caretBuffer = new StringBuffer();

        // setup the progress bar
        progressBar = ProgressBarFactory.create(false, true);

        addLabel(0, 100, true); // activity label
        addComponent(((JComponent) progressBar), 1, 120, false); // progress bar
        addLabel(2, 90, false); // execution time
        addLabel(3, 35, false); // insert mode
        addLabel(4, 60, false); // caret position
        addLabel(5, 40, true); // commit mode

        // set some labels to center alignment
        getLabel(3).setHorizontalAlignment(JLabel.CENTER);
        getLabel(4).setHorizontalAlignment(JLabel.CENTER);
    }

    /**
     * Cleanup code to ensure the progress thread is dead.
     */
    public void cleanup() {
        progressBar.cleanup();
        progressBar = null;
    }

    /**
     * Starts the progress bar.
     */
    public void startProgressBar() {
        progressBar.start();
    }

    /**
     * Stops the progress bar.
     */
    public void stopProgressBar() {
        progressBar.stop();
    }

    /**
     * Sets the query execution time to that specified.
     */
    public void setExecutionTime(String text) {
        setLabelText(2, text);
    }

    /**
     * Sets the editor commit status to the text specified.
     */
    public void setCommitStatus(boolean autoCommit) {

        setLabelText(5, Bundles.get(QueryEditor.class, "autocommit", autoCommit));
    }

    /**
     * Sets the editor insert mode to that specified.
     */
    public void setInsertionMode(String text) {
        setLabelText(3, text);
    }

    /**
     * Sets the editor status to the text specified.
     */
    public void setStatus(String text) {
        setLabelText(0, text);
    }

    /**
     * Sets the caret position to be formatted.
     *
     * @param l - the line number
     * @param c - the column number
     */
    public void setCaretPosition(int l, int c) {
        caretBuffer.append(" ").append(l).append(':').append(c);
        setLabelText(4, caretBuffer.toString());
        caretBuffer.setLength(0);
    }

}



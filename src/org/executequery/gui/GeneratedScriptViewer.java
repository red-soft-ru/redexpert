/*
 * GeneratedScriptViewer.java
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

package org.executequery.gui;

import org.executequery.ActiveComponent;
import org.executequery.Constants;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.base.TabView;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.gui.text.TextFileWriter;
import org.underworldlabs.util.FileUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * The Generated SQL Script Viewer.<br>
 * Used to display generated SQL CREATE TABLE statements.
 *
 * @author Takis Diakoumis
 */
public class GeneratedScriptViewer extends SimpleSqlTextPanel
        implements ActiveComponent,
        TabView {

    public static final String TITLE = "Generated SQL Script - ";

    public static final String FRAME_ICON = "DBImage16.png";

    /**
     * The text length
     */
    private int textLength;

    /**
     * The script file
     */
    private File file;

    public GeneratedScriptViewer(String text, String path) {
        this(text, new File(path));
    }

    /**
     * Constructs a new instance.
     */
    public GeneratedScriptViewer(String text, final File file) {
        this.file = file;

        if (text == null) {

            // load from file
            if (file != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        GUIUtilities.showWaitCursor();
                        try {
                            setContent(FileUtils.loadFile(file));
                        } catch (IOException e) {
                            GUIUtilities.displayErrorMessage(
                                    "Error reading from file " + file.getName() +
                                            "\nThe system returned: " + e.getMessage());
                        } finally {
                            GUIUtilities.showNormalCursor();
                        }
                    }
                });
            }

        }

        try {
            jbInit();
            setContent(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the state of this instance.
     */
    private void jbInit() throws Exception {
        setDefaultBorder();
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        EventMediator.registerListener(this);
    }

    private void setContent(String text) {
        if (text != null) {
            try {
                GUIUtilities.showWaitCursor();
                textLength = text.length();
                setSQLText(text);
            } finally {
                GUIUtilities.showNormalCursor();
            }
        }
    }

    // --------------------------------------------
    // DockedTabView implementation
    // --------------------------------------------

    /**
     * Indicates the panel is being removed from the pane
     */
    public boolean tabViewClosing() {
        cleanup();
        return true;
    }

    /**
     * Indicates the panel is being selected in the pane
     */
    public boolean tabViewSelected() {
        textPane.requestFocus();
        return true;
    }

    /**
     * Indicates the panel is being de-selected in the pane
     */
    public boolean tabViewDeselected() {
        return true;
    }

    // --------------------------------------------


    /**
     * Releases database resources before closing.
     */
    public void cleanup() {
        EventMediator.deregisterListener(this);
    }

    public String getPrintJobName() {
        return "RedXpert - SQL scripts";
    }

    public int save() {
        return save(file);
    }

    public int save(boolean saveAs) {
        String text = textPane.getText();
        TextFileWriter writer = null;

        if (file != null && !saveAs) {
            writer = new TextFileWriter(text, file.getAbsolutePath());
        } else {
            writer = new TextFileWriter(text, Constants.EMPTY);
        }

        int result = writer.write();
        if (result == SaveFunction.SAVE_COMPLETE) {
            file = writer.getSavedFile();
            GUIUtilities.setTabTitleForComponent(this, getDisplayName());
        }

        return result;
    }

    public boolean contentCanBeSaved() {
        return textLength != getEditorText().length();
    }

    public String getDisplayName() {
        return toString();
    }

    public String toString() {
        return TITLE + (file != null ? file.getName() : Constants.EMPTY);
    }

}












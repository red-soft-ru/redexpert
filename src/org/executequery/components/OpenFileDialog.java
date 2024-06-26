/*
 * OpenFileDialog.java
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

package org.executequery.components;

import org.executequery.localization.Bundles;
import org.executequery.localization.LocaleManager;
import org.underworldlabs.swing.FileSelector;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Takis Diakoumis
 */
public class OpenFileDialog extends FileChooserDialog
        implements PropertyChangeListener {

    public static final int NEW_EDITOR = 0;
    public static final int OPEN_EDITOR = 1;
    public static final int SCRATCH_PAD = 2;
    public static final int ERD_PANEL = 3;

    /**
     * The use open editor check box
     */
    private JCheckBox openEditorCheck;

    /**
     * The use new editor check box
     */
    private JCheckBox newEditorCheck;

    /**
     * The open scratch pad check box
     */
    private JCheckBox scratchPadCheck;

    /**
     * The open ERD check box
     */
    private JCheckBox erdPanelCheck;

    /**
     * Filter for text files
     */
    private FileSelector textFiles;

    /**
     * Filter for sql files
     */
    private FileSelector sqlFiles;

    /**
     * Filter for erd files
     */
    private FileSelector eqFiles;

    public OpenFileDialog() {

        super();

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void jbInit() throws Exception {

        textFiles = new FileSelector(new String[]{"txt"}, bundleString("file-text"));
        sqlFiles = new FileSelector(new String[]{"sql"}, bundleString("file-sql"));
        eqFiles = new FileSelector(new String[]{"eqd"}, bundleString("file-RedExpertERD"));

        setFileSelectionMode(JFileChooser.FILES_ONLY);
        addChoosableFileFilter(textFiles);
        addChoosableFileFilter(eqFiles);
        addChoosableFileFilter(sqlFiles);

        openEditorCheck = new JCheckBox(bundleString("new-command.open-query-editor"));
        newEditorCheck = new JCheckBox(bundleString("new-command.new-query-editor"), true);
        scratchPadCheck = new JCheckBox(bundleString("new-command.new-scratch-pad"));
        erdPanelCheck = new JCheckBox(bundleString("new-command.new-erd"));

        openEditorCheck.setEnabled(false);

        ButtonGroup bg = new ButtonGroup();
        bg.add(openEditorCheck);
        bg.add(newEditorCheck);
        bg.add(erdPanelCheck);
        bg.add(scratchPadCheck);

        JPanel optionsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 5, 3);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        optionsPanel.add(newEditorCheck, gbc);
        gbc.gridx = 1;
        gbc.insets.left = 5;
        optionsPanel.add(openEditorCheck, gbc);
        gbc.gridy = 1;
        optionsPanel.add(erdPanelCheck, gbc);
        gbc.gridx = 0;
        gbc.insets.left = 0;
        gbc.insets.right = 5;
        optionsPanel.add(scratchPadCheck, gbc);

        optionsPanel.setBorder(BorderFactory.createTitledBorder(bundleString("new-command.open-with") + ':'));

        customPanel = new JPanel(new BorderLayout());
        customPanel.setBorder(BorderFactory.createEmptyBorder(0, 7, 7, 7));
        customPanel.add(optionsPanel, BorderLayout.CENTER);

        addPropertyChangeListener(this);
        fileFilterChanged();

    }

    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName() == JFileChooser.FILE_FILTER_CHANGED_PROPERTY) {
            fileFilterChanged();
        }
    }

    private void fileFilterChanged() {

        FileFilter filter = getFileFilter();
        if (filter == textFiles || filter == sqlFiles) {

            if (erdPanelCheck.isSelected()) {
                newEditorCheck.setSelected(true);
            }

            newEditorCheck.setEnabled(true);
            //openEditorCheck.setEnabled(true);
            scratchPadCheck.setEnabled(true);
            erdPanelCheck.setEnabled(false);
        } else if (filter == eqFiles) {
            erdPanelCheck.setEnabled(true);
            erdPanelCheck.setSelected(true);
            newEditorCheck.setEnabled(false);
            //openEditorCheck.setEnabled(false);
            scratchPadCheck.setEnabled(false);
        } else {
            erdPanelCheck.setEnabled(true);
            newEditorCheck.setEnabled(true);
            //openEditorCheck.setEnabled(true);
            scratchPadCheck.setEnabled(true);
            newEditorCheck.setSelected(true);
        }
    }

    public int getOpenWith() {

        if (newEditorCheck.isSelected()) {

            return NEW_EDITOR;

        } else if (openEditorCheck.isSelected()) {

            return OPEN_EDITOR;

        } else if (scratchPadCheck.isSelected()) {

            return SCRATCH_PAD;

        } else if (erdPanelCheck.isSelected()) {

            return ERD_PANEL;

        } else {

            return NEW_EDITOR;
        }
    }

}







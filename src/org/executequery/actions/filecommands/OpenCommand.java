/*
 * OpenCommand.java
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

package org.executequery.actions.filecommands;

import org.executequery.Constants;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.event.DefaultFileIOEvent;
import org.executequery.event.FileIOEvent;
import org.executequery.gui.editor.QueryEditor;
import org.executequery.gui.erd.ErdSaveFileFormat;
import org.executequery.gui.erd.ErdViewerPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.FileSelector;
import org.underworldlabs.swing.actions.BaseCommand;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * The open file command.
 *
 * @author Takis Diakoumis
 */
public class OpenCommand implements BaseCommand {

    private final FileSelector[] fileSelectors;

    public OpenCommand() {
        fileSelectors = new FileSelector[]{
                new FileSelector(new String[]{"txt"}, bundleString("file.text")),
                new FileSelector(new String[]{"sql"}, bundleString("file.sql")),
                new FileSelector(new String[]{"eqd"}, bundleString("file.erd"))
        };
    }

    @Override
    public void execute(ActionEvent e) {

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        Arrays.stream(fileSelectors).forEach(fileChooser::addChoosableFileFilter);

        int result = fileChooser.showOpenDialog(GUIUtilities.getInFocusDialogOrWindow());
        if (result == JFileChooser.CANCEL_OPTION)
            return;

        SwingWorker worker = buildWorker(fileChooser.getSelectedFile());
        worker.start();
    }

    public void openFile(File file) {
        loadFile(file);
    }

    private SwingWorker buildWorker(File file) {
        return new SwingWorker(String.format("Loading file [%s]", file.getName())) {

            private boolean loadResult = false;

            @Override
            public Object construct() {
                GUIUtilities.showWaitCursor();
                GUIUtilities.getStatusBar().setSecondLabelText(String.format(bundleString("FileLoad"), file.getName()));
                loadResult = loadFile(file);

                return Constants.WORKER_SUCCESS;
            }

            @Override
            public void finished() {
                GUIUtilities.showNormalCursor();
                GUIUtilities.getStatusBar().setSecondLabelText(loadResult ?
                        bundleString("FileLoadSuccess") :
                        bundleString("FileLoadFail")
                );
            }
        };
    }

    private boolean loadFile(File file) {

        if (file == null || !file.exists()) {
            GUIUtilities.displayErrorMessage(bundleString("InvalidFileName"));
            return false;
        }

        String fileName = file.getName();
        try {

            // --- check for ERD file ---

            if (fileName.endsWith(".eqd")) {

                Object object = FileUtils.readObject(file);
                if (!(object instanceof ErdSaveFileFormat)) {
                    GUIUtilities.displayErrorMessage(bundleString("InvalidFileFormat"));
                    return false;
                }

                openNewErdEditor((ErdSaveFileFormat) object, file.getAbsolutePath());
                fireFileOpened(file);
                return true;
            }

            // --- check for existing query editor ---

            JPanel centralPane = GUIUtilities.getSelectedCentralPane();
            if (centralPane instanceof QueryEditor) {
                boolean openNewEditor;

                QueryEditor queryEditor = (QueryEditor) centralPane;
                if (MiscUtils.isNull(queryEditor.getEditorText())) {
                    openNewEditor = false;

                } else {
                    int result = GUIUtilities.displayYesNoDialog(bundleString("UseSelectedEditor"), Constants.EMPTY);
                    openNewEditor = result != JOptionPane.YES_OPTION;
                }

                if (!openNewEditor) {
                    String loadedText = FileUtils.loadFile(file);

                    queryEditor.loadText(loadedText);
                    queryEditor.setOpenFilePath(file.getAbsolutePath());
                    GUIUtilities.setTabTitleForComponent(centralPane, queryEditor.getDisplayName());

                    fireFileOpened(file);
                    return true;
                }
            }

            // --- open new query editor ---

            openNewQueryEditor(file, FileUtils.loadFile(file));
            fireFileOpened(file);
            return true;

        } catch (IOException e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("FileLoadError"), e, this.getClass());
            return false;
        }
    }

    private void openNewErdEditor(ErdSaveFileFormat erd, String absolutePath) {
        GUIUtilities.addCentralPane(
                ErdViewerPanel.TITLE + " - " + erd.getFileName(),
                ErdViewerPanel.FRAME_ICON,
                new ErdViewerPanel(erd, absolutePath),
                null,
                true
        );
    }

    private void openNewQueryEditor(File file, String contents) {
        GUIUtilities.addCentralPane(
                QueryEditor.TITLE,
                QueryEditor.FRAME_ICON,
                new QueryEditor(contents, file.getAbsolutePath(), -1),
                null,
                true
        );
    }

    private void fireFileOpened(File file) {
        EventMediator.fireEvent(new DefaultFileIOEvent(
                this,
                FileIOEvent.INPUT_COMPLETE,
                file.getAbsolutePath()
        ));
    }

    private static String bundleString(String key) {
        return Bundles.get(OpenCommand.class, key);
    }

}

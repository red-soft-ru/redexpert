/*
 * FileLoader.java
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

package org.executequery.util;

import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;

import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.components.OpenFileDialog;
import org.executequery.event.DefaultFileIOEvent;
import org.executequery.event.FileIOEvent;
import org.executequery.gui.ScratchPadPanel;
import org.executequery.gui.editor.QueryEditor;
import org.executequery.gui.erd.ErdSaveFileFormat;
import org.executequery.gui.erd.ErdViewerPanel;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.FileUtils;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class FileLoader {

    public void openFile(String file, int openWith) {

        openFile(new File(file), openWith);
    }

    public void openFile(final File file, final int openWith) {

        SwingWorker worker = new SwingWorker() {

            public Object construct() {

                GUIUtilities.getStatusBar().setSecondLabelText(
                        "Loading file: " + file.getName());
                GUIUtilities.showWaitCursor();

                load(file, openWith);

                return "done";
            }

            public void finished() {

                GUIUtilities.getStatusBar().
                        setSecondLabelText("I/O process complete");

                GUIUtilities.showNormalCursor();
            }

        };

        worker.start();
    }

    private void load(File file, int openWith) {

        if (file == null || !file.exists()) {

            GUIUtilities.displayErrorMessage("Invalid file name");
            return;
        }

        try {
            String fileName = file.getName();

            if (openWith == -1) {

                if (fileName.endsWith(".sql")) {

                    openWith = OpenFileDialog.NEW_EDITOR;

                } else {

                    openWith = OpenFileDialog.SCRATCH_PAD;
                }

            }

            if (fileName.endsWith(".eqd")) {

                Object object = FileUtils.readObject(file);
                if (!(object instanceof ErdSaveFileFormat)) {

                    GUIUtilities.displayErrorMessage("Invalid file format");
                    return;
                }

                ErdSaveFileFormat erd = (ErdSaveFileFormat)object;

                ErdViewerPanel erdPanel = new ErdViewerPanel(erd, file.getAbsolutePath());
                GUIUtilities.addCentralPane(ErdViewerPanel.TITLE,
                                            ErdViewerPanel.FRAME_ICON,
                                            erdPanel,
                                            null,
                                            true);
                erdPanel.setSavedErd(erd, file.getAbsolutePath());

                /*
                JPanel panel = GUIUtilities.getOpenFrame(ErdViewerPanel.TITLE);
                if (panel != null && panel instanceof ErdViewerPanel) {

                    ErdViewerPanel erdPanel = (ErdViewerPanel)panel;
                    erdPanel.setSavedErd(erd, file.getAbsolutePath());

                } else {

                    ErdViewerPanel erdPanel = new ErdViewerPanel(erd, file.getAbsolutePath());
                    GUIUtilities.addCentralPane(ErdViewerPanel.TITLE,
                                                ErdViewerPanel.FRAME_ICON,
                                                erdPanel,
                                                null,
                                                true);
                    erdPanel.setSavedErd(erd, file.getAbsolutePath());
                }
                */

                fireFileOpened(file);

                return;
            }

            String contents = FileUtils.loadFile(file);

            if (openWith == OpenFileDialog.NEW_EDITOR) {

                openNewEditor(file, contents);

            } else if (openWith == OpenFileDialog.OPEN_EDITOR) {

                JPanel panel = GUIUtilities.getSelectedCentralPane();

                if (panel != null && panel instanceof QueryEditor) {

                    QueryEditor editor = ((QueryEditor)panel);

                    //editor.setEditorText(contents, true);

                    editor.loadText(contents);
                    editor.setOpenFilePath(file.getAbsolutePath());

                    GUIUtilities.setTabTitleForComponent(
                                    panel, editor.getDisplayName());

                } else {

                    openNewEditor(file, contents);
                }

            } else if (openWith == OpenFileDialog.SCRATCH_PAD) {

                JPanel panel = GUIUtilities.getSelectedCentralPane();

                if (panel != null && panel instanceof ScratchPadPanel) {

                    ((ScratchPadPanel)panel).setEditorText(contents);

                } else {

                    openNewScratchPad(file, contents);
                }

            }

            fireFileOpened(file);

        } catch (IOException e) {

            e.printStackTrace();
            GUIUtilities.displayErrorMessage(
                    "I/O error opening selected file.\n" + e.getMessage());
        }

    }

    private void fireFileOpened(File file) {

        EventMediator.fireEvent(
                new DefaultFileIOEvent(this, FileIOEvent.INPUT_COMPLETE,
                        file.getAbsolutePath()));
    }

    private void openNewScratchPad(File file, String contents) {

        GUIUtilities.addCentralPane(ScratchPadPanel.TITLE,
                                    ScratchPadPanel.FRAME_ICON,
                                    new ScratchPadPanel(contents),
                                    null,
                                    true);
    }

    private void openNewEditor(File file, String contents) {

        QueryEditor editor = new QueryEditor(contents, file.getAbsolutePath());
        GUIUtilities.addCentralPane(QueryEditor.TITLE,
                                    QueryEditor.FRAME_ICON,
                                    editor,
                                    null,
                                    true);
    }

}






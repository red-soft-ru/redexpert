/*
 * FileChooserDialog.java
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

import org.executequery.GUIUtilities;
import org.executequery.localization.Bundles;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * This class provides a minor modification to the
 * <code>JFileChooser</code> class by setting the
 * location of the resultant dialog to the center of the
 * desktop pane as opposed to the default implementation
 * which is relative to the parent frame and centered to
 * the screen.
 *
 * @author Takis Diakoumis
 */
public class FileChooserDialog extends JFileChooser {

    private static final String LAST_OPEN_FILE_PATH = "last.open.file.path";

    protected JPanel customPanel;

    public FileChooserDialog() {
        super();
        setCurrentDirectory(new File(getLastOpenFilePath()));
    }

    public FileChooserDialog(String currentDirectoryPath) {
        super(currentDirectoryPath);
    }

    @Override
    public int showOpenDialog(Component parent) throws HeadlessException {
        return super.showOpenDialog(parent);
    }

    @Override
    public int showSaveDialog(Component parent) throws HeadlessException {
        int result = super.showSaveDialog(parent);
        File file = getSelectedFile();

        if (file == null || result == CANCEL_OPTION)
            return CANCEL_OPTION;

        if (!file.exists())
            return result;

        String confirmMessage = Bundles.get(FileChooserDialog.class, "new-command.overwrite-file");
        int confirmResult = GUIUtilities.displayConfirmCancelDialog(confirmMessage);
        if (confirmResult == JOptionPane.CANCEL_OPTION)
            return CANCEL_OPTION;

        if (confirmResult == JOptionPane.NO_OPTION)
            return showSaveDialog(parent);

        return result;
    }

    @Override
    public int showDialog(Component parent, String approveButtonText) throws HeadlessException {

        int result = super.showDialog(parent, approveButtonText);
        if (result != JFileChooser.CANCEL_OPTION)
            resetLastOpenFilePath();

        return result;
    }

    @Override
    protected JDialog createDialog(Component parent) throws HeadlessException {

        Frame frame = parent instanceof Frame ?
                (Frame) parent :
                (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parent);

        String title = getUI().getDialogTitle(this);
        JDialog dialog = new JDialog(frame, title, true);

        Container contentPane = dialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(this, BorderLayout.CENTER);
        setPreferredSize(new Dimension(700, getPreferredSize().height));

        if (customPanel != null)
            contentPane.add(customPanel, BorderLayout.SOUTH);

        if (JDialog.isDefaultLookAndFeelDecorated())
            if (UIManager.getLookAndFeel().getSupportsWindowDecorations())
                dialog.getRootPane().setWindowDecorationStyle(JRootPane.FILE_CHOOSER_DIALOG);

        dialog.pack();
        dialog.setLocation(GUIUtilities.getLocationForDialog(dialog.getSize()));
        return dialog;
    }

    private String getLastOpenFilePath() {

        String path = SystemProperties.getStringProperty("user", LAST_OPEN_FILE_PATH);
        if (MiscUtils.isNull(path))
            path = System.getProperty("user.home");

        return path;
    }

    private void resetLastOpenFilePath() {
        File file = getSelectedFile();

        String lastOpenFilePath = file.getPath();
        if (file.isFile() || !file.exists())
            lastOpenFilePath = file.getParent();

        SystemProperties.setStringProperty("user", LAST_OPEN_FILE_PATH, lastOpenFilePath);
    }

}

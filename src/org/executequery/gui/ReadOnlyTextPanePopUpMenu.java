/*
 * ReadOnlyTextPanePopUpMenu.java
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

import org.apache.commons.lang.StringUtils;
import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.localization.Bundles;
import org.executequery.repository.LogRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.actions.ReflectiveAction;
import org.underworldlabs.swing.menu.MenuItemFactory;
import org.underworldlabs.util.FileUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

/**
 * @author Takis Diakoumis
 */
public class ReadOnlyTextPanePopUpMenu extends JPopupMenu {

    private ReflectiveAction reflectiveAction;

    private ReadOnlyTextPane readOnlyTextArea;

    public ReadOnlyTextPanePopUpMenu(ReadOnlyTextPane readOnlyTextPane) {

        this.readOnlyTextArea = readOnlyTextPane;
        reflectiveAction = new ReflectiveAction(this);

        String[] actionCommands = {"copy", "selectAll", "saveToFile", "clear"};
        String[] menuLabels = getBundles(actionCommands, null);
        String[] toolTips = getBundles(actionCommands, "tooltip");

        for (int i = 0; i < menuLabels.length; i++) {

            add(createMenuItem(menuLabels[i], actionCommands[i], toolTips[i]));
        }

        readOnlyTextPane.getTextComponent().addKeyListener(new KeyListener() {

            public void keyReleased(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {

                    clear(null);
                }
            }

            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
            }

        });

    }

    public void saveToFile(ActionEvent e) {

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);

        fileChooser.setDialogTitle(bundleString("saveFileDialogTitle"));
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);

        int result = fileChooser.showDialog(GUIUtilities.getInFocusDialogOrWindow(), Bundles.get("common.select"));
        if (result == JFileChooser.CANCEL_OPTION) {

            return;
        }

        File file = fileChooser.getSelectedFile();
        if (file.exists()) {

            result = GUIUtilities.displayConfirmCancelDialog(bundleString("overwriteFile"));

            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.NO_OPTION) {

                saveToFile(e);
                return;
            }

        }

        try {

            FileUtils.writeFile(file.getAbsolutePath(), readOnlyTextArea.getText());

        } catch (IOException e1) {

            GUIUtilities.displayErrorMessage(bundleString("writingError")
                    + e1.getMessage());
        }
    }

    public void reset(ActionEvent e) {

        String message = bundleString("resetConfirm");
        if (GUIUtilities.displayConfirmDialog(message) == JOptionPane.YES_OPTION) {

            LogRepository logRepository = (LogRepository) RepositoryCache.load(LogRepository.REPOSITORY_ID);
            logRepository.reset(LogRepository.ACTIVITY);
            clear(e);
        }

    }

    public void clear(ActionEvent e) {
        readOnlyTextArea.clear();
    }

    public void selectAll(ActionEvent e) {
        readOnlyTextArea.selectAll();
    }

    public void copy(ActionEvent e) {
        readOnlyTextArea.copy();
    }

    protected JMenuItem createMenuItem(String text, String actionCommand, String toolTip) {

        JMenuItem menuItem = MenuItemFactory.createMenuItem(text);
        menuItem.setActionCommand(actionCommand);
        menuItem.addActionListener(reflectiveAction);

        if (StringUtils.isNotBlank(toolTip)) {

            menuItem.setToolTipText(toolTip);
        }

        return menuItem;
    }

    private static String[] getBundles(String[] keys, String suffix) {

        String[] result = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            String key = suffix != null ? keys[i] + "." + suffix : keys[i];
            result[i] = Bundles.get(ReadOnlyTextPanePopUpMenu.class, key);
        }

        return result;
    }

    private static String bundleString(String key) {
        return Bundles.get(ReadOnlyTextPanePopUpMenu.class, key);
    }

}

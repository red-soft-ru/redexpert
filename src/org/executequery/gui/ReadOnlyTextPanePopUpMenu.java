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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.apache.commons.lang.StringUtils;
import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.repository.LogRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.actions.ReflectiveAction;
import org.underworldlabs.swing.menu.MenuItemFactory;
import org.underworldlabs.util.FileUtils;

/**
 *
 * @author   Takis Diakoumis
 */
public class ReadOnlyTextPanePopUpMenu extends JPopupMenu {

    private ReflectiveAction reflectiveAction;

    private ReadOnlyTextPane readOnlyTextArea;

    public ReadOnlyTextPanePopUpMenu(ReadOnlyTextPane readOnlyTextPane) {

        this.readOnlyTextArea = readOnlyTextPane;
        reflectiveAction = new ReflectiveAction(this);

        String[] menuLabels = {"Copy", "Select All", "Save to File", "Clear"};
        String[] actionCommands = {"copy", "selectAll", "saveToFile", "clear"};
        String[] toolTips = {"", "", "Save the contents to file", "Clear the output pane"};

        for (int i = 0; i < menuLabels.length; i++) {

            add(createMenuItem(menuLabels[i], actionCommands[i], toolTips[i]));
        }
        
        readOnlyTextPane.getTextComponent().addKeyListener(new KeyListener() {
            
            public void keyReleased(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    
                    clear(null);
                }
            }
            public void keyTyped(KeyEvent e) {}
            public void keyPressed(KeyEvent e) {}
            
        });
        
    }

    public void saveToFile(ActionEvent e) {

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);

        fileChooser.setDialogTitle("Select Output File Path");
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);

        int result = fileChooser.showDialog(GUIUtilities.getInFocusDialogOrWindow(), "Select");
        if (result == JFileChooser.CANCEL_OPTION) {

            return;
        }

        File file = fileChooser.getSelectedFile();
        if (file.exists()) {

            result = GUIUtilities.displayConfirmCancelDialog("The selected file exists.\nOverwrite existing file?");

            if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.NO_OPTION) {

                saveToFile(e);
                return;
            }

        }

        try {

            FileUtils.writeFile(file.getAbsolutePath(), readOnlyTextArea.getText());

        } catch (IOException e1) {

            GUIUtilities.displayErrorMessage("Error writing output pane contents to file.\n"
                    + e1.getMessage());
        }
    }

    public void reset(ActionEvent e) {

        String message = "Are you sure you want to reset the system activity log?";
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

}






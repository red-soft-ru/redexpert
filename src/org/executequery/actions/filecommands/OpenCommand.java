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

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;

import org.executequery.GUIUtilities;
import org.executequery.components.OpenFileDialog;
import org.underworldlabs.swing.actions.BaseCommand;

import org.executequery.util.FileLoader;

/** 
 * The File | Open command.
 *
 * @author   Takis Diakoumis
 */
public class OpenCommand implements BaseCommand {
    
    public void execute(ActionEvent e) {
    
        OpenFileDialog fileChooser = new OpenFileDialog();

        int result = fileChooser.showOpenDialog(GUIUtilities.getInFocusDialogOrWindow());

        if (result == JFileChooser.CANCEL_OPTION) {
        
            return;
        }

        File file = fileChooser.getSelectedFile();
        FileLoader loader = new FileLoader();
        loader.openFile(file, fileChooser.getOpenWith());
    }

}
















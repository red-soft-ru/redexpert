/*
 * FileSelectionTableCell.java
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

package org.executequery.components.table;

import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;

import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;

/**
 * Table cell with file selection button.
 *
 * @author   Takis Diakoumis
 */
public class FileSelectionTableCell extends BrowsingCellEditor {
    
    /** Creates a new instance of ComboBoxCellRenderer */
    public FileSelectionTableCell() {}
    
    public void actionPerformed(ActionEvent e) {
        FileChooserDialog dialog = new FileChooserDialog();
        dialog.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = dialog.showOpenDialog(GUIUtilities.getInFocusDialogOrWindow());
        
        if (result == JFileChooser.CANCEL_OPTION) {
            fireEditingStopped();
            return;
        }

        if (dialog.getSelectedFile() != null) {
            String path = dialog.getSelectedFile().getAbsolutePath();
            setDelegateValue(path);
            fireEditingStopped();
        }

    }

}







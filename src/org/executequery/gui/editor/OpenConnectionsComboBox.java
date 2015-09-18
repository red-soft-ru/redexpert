/*
 * OpenConnectionsComboBox.java
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

package org.executequery.gui.editor;

import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JComboBox;

import org.executequery.databasemediators.DatabaseConnection;
import org.underworldlabs.swing.DynamicComboBoxModel;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
final class OpenConnectionsComboBox extends JComboBox { 
 
    private QueryEditor queryEditor;
    
    private DynamicComboBoxModel connectionsModel;
    
    OpenConnectionsComboBox(QueryEditor queryEditor, 
            Vector<DatabaseConnection> connections) {
        
        this.queryEditor = queryEditor;
        
        connectionsModel = new DynamicComboBoxModel(connections);
        setModel(connectionsModel);
        
        if (connectionsModel.getSize() == 0) {
            setEnabled(false);
        }

    }

    public boolean contains(DatabaseConnection databaseConnection) {
        
        return connectionsModel.contains(databaseConnection);
    }
    
    public void processKeyEvent(KeyEvent e) {

        super.processKeyEvent(e);

        if (isKeyForCaretReset(e.getKeyCode())) {

            queryEditor.resetCaretPositionToLast();
        }
    }
    
    private boolean isKeyForCaretReset(int keyCode) {
        return keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_ESCAPE;
    }
    
    protected void attemptToFocus() {
        requestFocus();
        setPopupVisible(true);
    }
    
    protected void removeElement(DatabaseConnection databaseConnection) {
        connectionsModel.removeElement(databaseConnection);
        if (connectionsModel.getSize() == 0) {
            setEnabled(false);
        }
    }

    protected void addElement(DatabaseConnection databaseConnection) {
        connectionsModel.addElement(databaseConnection);
        setEnabled(true);
    }
    
}





/*
 * ConnectionTreeCellEditor.java
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

package org.executequery.gui.browser.tree;

import java.util.EventObject;

import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.executequery.EventMediator;
import org.executequery.event.ConnectionRepositoryEvent;
import org.executequery.event.ConnectionsFolderRepositoryEvent;
import org.executequery.event.DefaultConnectionRepositoryEvent;
import org.executequery.event.DefaultConnectionsFolderRepositoryEvent;
import org.executequery.gui.browser.nodes.ConnectionsFolderNode;
import org.executequery.gui.browser.nodes.DatabaseHostNode;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;

/**
 * @author   Takis Diakoumis
 */
public class ConnectionTreeCellEditor extends DefaultTreeCellEditor {
    
    private final SchemaTree schemaTree;

    public ConnectionTreeCellEditor(SchemaTree tree, DefaultTreeCellRenderer renderer) {
        
        super(tree, renderer);
        schemaTree = tree;
    }

    public boolean isCellEditable(EventObject event) {
        
        Object object = tree.getSelectionPath().getLastPathComponent();
        if (object instanceof DatabaseObjectNode) {
            
            return ((DatabaseObjectNode) object).isNameEditable();
        }

        return false;        
    }
    
    public Object getCellEditorValue() {

        Object value = super.getCellEditorValue();
        Object lastPathComponent = tree.getSelectionPath().getLastPathComponent();
        if (lastPathComponent instanceof ConnectionsFolderNode) {
            
            ConnectionsFolderNode node = (ConnectionsFolderNode) lastPathComponent;
            node.setName(value.toString());
            
            EventMediator.fireEvent(new DefaultConnectionsFolderRepositoryEvent(
                    this, ConnectionsFolderRepositoryEvent.FOLDER_MODIFIED, node.getConnectionsFolder()));
        
        } else if (lastPathComponent instanceof DatabaseHostNode) {
            
            DatabaseHostNode node = (DatabaseHostNode) lastPathComponent;
            schemaTree.connectionNameChanged(value.toString());
            EventMediator.fireEvent(new DefaultConnectionRepositoryEvent(
                    this, ConnectionRepositoryEvent.CONNECTION_MODIFIED, node.getDatabaseConnection()));            
        }
        
        return value;
    }
}






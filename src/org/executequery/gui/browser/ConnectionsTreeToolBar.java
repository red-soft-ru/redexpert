/*
 * ConnectionsTreeToolBar.java
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

package org.executequery.gui.browser;

import javax.swing.JButton;

import org.executequery.GUIUtilities;
import org.executequery.util.ThreadUtils;
import org.underworldlabs.swing.toolbar.PanelToolBar;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
class ConnectionsTreeToolBar extends PanelToolBar {

    /** move connection up button */
    private JButton upButton;
    
    /** move connection down button */
    private JButton downButton;

    /** the reload node button */
    private JButton reloadButton;
    
    /** new connection button */
    @SuppressWarnings("unused")
    private JButton newConnectionButton;

    /** delete connection button */
    private JButton deleteConnectionButton;

    private ConnectionsTreePanel treePanel;
    
    public ConnectionsTreeToolBar(ConnectionsTreePanel treePanel) {
        
        this.treePanel = treePanel;

        init();
    }
    
    private void init() {
        
        newConnectionButton = addButton(
                treePanel, "newConnection", 
                GUIUtilities.getAbsoluteIconPath("NewConnection16.png"), 
                "New connection");

        addButton(
                treePanel, "newFolder", 
                GUIUtilities.getAbsoluteIconPath("NewFolder16.png"), 
                "New folder");
        
        deleteConnectionButton = addButton(
                treePanel, "deleteConnection", 
                GUIUtilities.getAbsoluteIconPath("Delete16.png"),
                "Delete");

        upButton = addButton(
                treePanel, "moveConnectionUp", 
                GUIUtilities.getAbsoluteIconPath("Up16.png"),
                "Move connection up");

        downButton = addButton(
                treePanel, "moveConnectionDown", 
                GUIUtilities.getAbsoluteIconPath("Down16.png"), 
                "Move connection down");

        reloadButton = addButton(
                treePanel, "reloadSelection", 
//                GUIUtilities.getAbsoluteIconPath("Reload16.png"), 
                GUIUtilities.getAbsoluteIconPath("Refresh16.png"), 
                "Reload the currently selected node");

        addButton(
                treePanel, "sortConnections", 
                GUIUtilities.getAbsoluteIconPath("SortAtoZ16.png"), 
                "Sort connections");

        addButton(treePanel.getTreeFindAction());

        addButton(
                treePanel, "collapseAll", 
                GUIUtilities.getAbsoluteIconPath("Collapse16.png"), 
                "Collapse all");

    }

    protected void enableButtons(final boolean enableUpButton, 
                                 final boolean enableDownButton,
                                 final boolean enableReloadButton,
                                 final boolean enableDeleteButton) {

        ThreadUtils.invokeLater(new Runnable() {

            public void run() {

                upButton.setEnabled(enableUpButton);
                downButton.setEnabled(enableDownButton);
                reloadButton.setEnabled(enableReloadButton);
                deleteConnectionButton.setEnabled(enableDeleteButton);
            }

        });
    }
    
}






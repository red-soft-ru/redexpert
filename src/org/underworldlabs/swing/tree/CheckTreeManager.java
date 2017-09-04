/*
 * CheckTreeManager.java
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

package org.underworldlabs.swing.tree;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

//some rather good work from here:
//http://www.jroller.com/santhosh/entry/jtree_with_checkboxes

//@author Santhosh Kumar T - santhosh@in.fiorano.com 
public class CheckTreeManager extends MouseAdapter implements TreeSelectionListener { 
    
    private CheckTreeSelectionModel selectionModel; 
    private JTree tree = new JTree(); 
    int hotspot = new JCheckBox().getPreferredSize().width; 
 
    public CheckTreeManager(JTree tree) {

        this.tree = tree; 
        selectionModel = new CheckTreeSelectionModel(tree.getModel()); 
        tree.setCellRenderer(new CheckTreeCellRenderer(tree.getCellRenderer(), selectionModel)); 
        tree.addMouseListener(this); 
        selectionModel.addTreeSelectionListener(this); 
    } 
 
    public void mouseClicked(MouseEvent me) { 

        TreePath path = tree.getPathForLocation(me.getX(), me.getY()); 
        if (path == null) 
            return; 
        if (me.getX() > tree.getPathBounds(path).x + hotspot) 
            return; 
 
        boolean selected = selectionModel.isPathSelected(path, true); 
        selectionModel.removeTreeSelectionListener(this); 
 
        try { 
            if (selected) 
                selectionModel.removeSelectionPath(path); 
            else 
                selectionModel.addSelectionPath(path); 
        } finally { 
            selectionModel.addTreeSelectionListener(this); 
            tree.treeDidChange(); 
        } 
    } 
 
    public CheckTreeSelectionModel getSelectionModel() { 
        return selectionModel; 
    } 
 
    public void valueChanged(TreeSelectionEvent e) { 
        tree.treeDidChange(); 
    } 
    

}


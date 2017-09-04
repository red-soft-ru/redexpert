/*
 * CheckTreeCellRenderer.java
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

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

//some rather good work from here:
//http://www.jroller.com/santhosh/entry/jtree_with_checkboxes

//@author Santhosh Kumar T - santhosh@in.fiorano.com 
public class CheckTreeCellRenderer extends JPanel implements TreeCellRenderer {
    
    private CheckTreeSelectionModel selectionModel;
    private TreeCellRenderer delegate;

//    private TristateCheckBox checkBox = new TristateCheckBox();
    
    private JCheckBox checkBox = new JCheckBox();

    public CheckTreeCellRenderer(TreeCellRenderer delegate, CheckTreeSelectionModel selectionModel) {

        this.delegate = delegate;
        this.selectionModel = selectionModel;
        setLayout(new BorderLayout());
        setOpaque(false);
        checkBox.setOpaque(false);
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {

        Component renderer = delegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row,
                hasFocus);

        TreePath path = tree.getPathForRow(row);
        if (path != null) {
        
            if (selectionModel.isPathSelected(path, true)) {
        
                checkBox.setSelected(Boolean.TRUE);

            } else {
            
                checkBox.setSelected(Boolean.FALSE);
                
//                checkBox.setState(selectionModel.isPartiallySelected(path) ? null : Boolean.FALSE);
            }

        }

        removeAll();
        add(checkBox, BorderLayout.WEST);
        add(renderer, BorderLayout.CENTER);
        
        return this;
    }

}


/*
 * AbstractTreeCellRenderer.java
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

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;


/**
 * Abstract tree cell renderer with custom paint method.
 *
 * @author   Takis Diakoumis
 */
public abstract class AbstractTreeCellRenderer extends DefaultTreeCellRenderer {

    /**
     * Sets the value of the current tree cell to value. If 
     * selected is true, the cell will be drawn as if selected. 
     * If expanded is true the node is currently expanded and if 
     * leaf is true the node represets a leaf and if hasFocus 
     * is true the node currently has focus. tree is the JTree 
     * the receiver is being configured for. Returns the Component 
     * that the renderer uses to draw the value.
     *
     * @return the Component that the renderer uses to draw the value
     */
    public abstract Component getTreeCellRendererComponent(
                                                  JTree tree, 
                                                  Object value,
                                                  boolean isSelected, 
                                                  boolean isExpanded,
                                                  boolean isLeaf, 
                                                  int row, 
                                                  boolean hasFocus);

    public void paintComponent(Graphics g) {
        
        if (selected) {
            
            int imageOffset = getLabelX();

            // paint the background
            if (backgroundSelectionColor != null) {
            
                g.setColor(backgroundSelectionColor);
                g.fillRect(imageOffset, 0, getWidth() - 1 - imageOffset, getHeight());
            }

            // paint the border
            if (borderSelectionColor != null) {
                
                g.setColor(borderSelectionColor);
                g.drawRect(imageOffset, 0, getWidth() - 1 - imageOffset, getHeight() - 1);
            }
        }
        
        super.paintComponent(g);
    }

    protected int getLabelX() {
        Icon currentI = getIcon();
        if(currentI != null && getText() != null) {
            return currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1);
        }
        return 0;
    }
    
    
}






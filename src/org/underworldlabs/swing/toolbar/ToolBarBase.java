/*
 * ToolBarBase.java
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

package org.underworldlabs.swing.toolbar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1766 $
 * @date     $Date: 2017-08-14 23:34:37 +1000 (Mon, 14 Aug 2017) $
 */
public class ToolBarBase extends JLayeredPane
                         implements PropertyChangeListener {
    
    private Color highlight;
    private ToolBarLayout toolBarLayout;
    
    public ToolBarBase(int initialRowSize) {
        super();
        highlight = UIManager.getColor("ToolBar.highlight");
        toolBarLayout = new ToolBarLayout(initialRowSize);
        setLayout(toolBarLayout);
    }
    
    public void addToolBar(ToolBar toolBar, int row, int position) {
        add(toolBar, new ToolBarConstraints(row, position));
        toolBar.addPropertyChangeListener(this);
    }
    
    public void addToolBar(ToolBar toolBar, int row, int position, int minimumWidth) {
        add(toolBar, new ToolBarConstraints(row, position, minimumWidth));
        toolBar.addPropertyChangeListener(this);
    }
    
    public void addToolBar(ToolBar toolBar, ToolBarConstraints tbc) {
        add(toolBar, tbc);
        toolBar.addPropertyChangeListener(this);
    }
    
    public void addToolBar(ToolBar toolBar, int row, int position,
    int minimumWidth, int preferredWidth) {
        add(toolBar, new ToolBarConstraints(row, position, minimumWidth, preferredWidth));
        toolBar.addPropertyChangeListener(this);
    }
    
    public void setRows(int rows) {
        toolBarLayout.setRows(rows);
    }
    
    public void toolBarMoved(ToolBar toolBar) {
        Point point = toolBar.getLocation();
        toolBarMoved(toolBar, point.x, point.y);
    }
    
    public boolean rowAdded(ToolBar toolBar) {
        boolean added = toolBarLayout.maybeAddRow(toolBar);
        
        if (added) {
            repaint();
            revalidate();
        }
        
        return added;
    }
    
    public void toolBarResized(ToolBar toolBar, int locX) {
        toolBarLayout.componentResized(toolBar, locX);
    }
    
    public void toolBarMoved(ToolBar toolBar, int locX, int locY) {
        toolBarLayout.componentMoved(toolBar, locX, locY);
        repaint();
        revalidate();
    }
    
    public void removeAll() {
        toolBarLayout.removeComponents();
        super.removeAll();
    }
    
    public void propertyChange(PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();
        
        if (propertyName == ToolBar.TOOL_BAR_BEGIN_MOVING) {
            ToolBar toolBar = null;
            Component[] components = getComponents();
            
            for (int i = 0; i < components.length; i++) {
                toolBar = (ToolBar)components[i];
                toolBar.enableButtonsSelection(false);
            }
            
            setLayer((ToolBar)e.getSource(), JLayeredPane.DRAG_LAYER.intValue());
        }
        
        else if (propertyName == ToolBar.TOOL_BAR_MOVING)
            rowAdded((ToolBar)e.getSource());
        
        else if (propertyName == ToolBar.TOOL_BAR_MOVED) {
            ToolBar toolBar = (ToolBar)e.getSource();
            setLayer(toolBar, JLayeredPane.DEFAULT_LAYER.intValue());
            toolBarMoved(toolBar);
        }
        
        else if (propertyName == ToolBar.TOOL_BAR_RESIZING) {
            ToolBar toolBar = (ToolBar)e.getSource();
            toolBarResized(toolBar, Integer.parseInt(e.getNewValue().toString()));
            validate();
        }
        
        else if (propertyName == ToolBar.TOOL_BAR_DESELECTED) {
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ToolBar toolBar = null;
                    Component[] components = getComponents();
                    
                    for (int i = 0; i < components.length; i++) {
                        toolBar = (ToolBar)components[i];
                        toolBar.enableButtonsSelection(true);
                        ToolBarProperties.setToolBarConstraints(toolBar.getName(),
                        toolBarLayout.getConstraint(toolBar));
                    }
                    
                    ToolBarProperties.saveTools();
                }
            });
            
        }
        
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        int height = getHeight();
        int width = getWidth();
        
        g.setColor(highlight);
        g.drawLine(0, height - 3, width, height - 3);
    }
    
}

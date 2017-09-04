/*
 * DragPanel.java
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

package org.executequery.base;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class DragPanel extends JPanel {
    
    private int tabX;
    private int tabWidth;
    private int tabHeight;

    /** the tab rectangle portion */
    private Rectangle tabRect;
    
    /** the border insets */
    private static Insets borderInsets;
    
    public DragPanel(Rectangle bounds) {
        this(null, bounds);
    }

    public DragPanel(Rectangle tabBounds, Rectangle bounds) {
        
        if (borderInsets == null) {
            borderInsets = new Insets(0,0,0,0);
        }

        init(tabBounds, bounds);
        setBorder(new PanelBorder());
    }

    protected Rectangle getTabRectangle() {
        return tabRect;
    }
    
    private void init(Rectangle tabBounds, Rectangle bounds) {
        if (tabBounds != null) {
            tabX = tabBounds.x;
            tabWidth = tabBounds.width;
            tabHeight = tabBounds.height;
        }
        tabRect = tabBounds;
        setBounds(bounds);
    }

    public void reset(Rectangle bounds) {
        reset(null, bounds);
    }

    public void reset(Rectangle tabBounds, Rectangle bounds) {
        init(tabBounds, bounds);
        repaint();
    }
    
    public boolean isOpaque() {
        return false;
    }
    
    class PanelBorder implements Border {
        
        /** the paint stroke */
        private BasicStroke stroke = new BasicStroke(2.0f);
        
        public void paintBorder(Component c,
                        Graphics g,
                        int x,
                        int y,
                        int width,
                        int height) {

            Graphics2D g2d = (Graphics2D)g;
            
            g2d.setStroke(stroke);
            g2d.setColor(Color.RED.brighter().brighter());
            
            if (tabRect == null) {
                g2d.drawRect(x + 1, y + 1, width - 2, height - 2);
                return;
            }
            
            // -------------------------
            // tab border 

            // tab top line
            g2d.drawLine(tabX + x + 1, y + 1, 
                         tabX + x + tabWidth - 2, y + 1);

            // tab left
            g2d.drawLine(tabX + x + 1, y, 
                         tabX + x + 1, y + tabHeight);

            // tab right
            g.drawLine(tabX + x + tabWidth - 2, y, 
                       tabX + x + tabWidth - 2, y + tabHeight);

            // ---------------------------
            // remaining border
            
            // top left side
            g2d.drawLine(x, y + tabHeight, 
                       x + tabX, y + tabHeight);

            // top right side
            g2d.drawLine(x + tabX + tabWidth, y + tabHeight,
                       x + width, y + tabHeight);

            // left side
            g2d.drawLine(x + 1, y + tabHeight, x + 1, y + height);

            // right side
            g2d.drawLine(x + width - 1, y + tabHeight, x + width - 1, y + height - 1);

            // bottom
            g2d.drawLine(x, y + height - 1, x + width - 1, y + height - 1);

        } 
     
        public Insets getBorderInsets(Component c) {
            return borderInsets;
        }
        
        public boolean isBorderOpaque() {
            return true;
        }
        
    }
    
}












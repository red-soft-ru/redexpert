/*
 * DockedTabMinimizeIcon.java
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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import javax.swing.SwingConstants;

/**
 * Simple icon drawing the close button
 * for a closeable tab on the CloseTabbedPane.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class DockedTabMinimizeIcon implements TabControlIcon,
                                              SwingConstants {
    
    /** The icons orientation */
    private int orientation;
    
    /** 
     * Creates a new instance of DockedTabMinimizeIcon 
     * with default orientation WEST. 
     */
    public DockedTabMinimizeIcon() {
        this(WEST);
    }

    /**
     * Creates a new instance of DockedTabMinimizeIcon 
     * at the specified orientation.
     *
     * @param the orientation - SwingConstants.WEST | EAST | CENTER
     */
    public DockedTabMinimizeIcon(int orientation) {
        this.orientation = orientation;
    }

    /**
     * Returns the icon's height.
     * 
     * @return the height of the icon
     */
    public int getIconHeight() {
        return ICON_HEIGHT;
    }

    /**
     * Returns the icon's width.
     * 
     * @return the width of the icon
     */
    public int getIconWidth() {
        return ICON_WIDTH;
    }

    /**
     * Draw the icon at the specified location.
     *
     * @param the component
     * @param the graphics context
     * @param x coordinate
     * @param y coordinate
     */
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D)g;
        AffineTransform oldTransform = g2.getTransform();
        
        g2.setColor(ICON_COLOR);

        if (orientation != WEST) {
            double theta = 0;
            double xOrigin = x + (ICON_WIDTH / 2);
            double yOrigin = y + (ICON_HEIGHT / 2);

            if (orientation == CENTER) {
                theta = Math.PI * 1.5;
            }
            else if (orientation == EAST) {
                theta = Math.PI;
            }
            g2.rotate(theta, xOrigin, yOrigin);
        }
        
        int x1 = x + ICON_WIDTH - 1;
        int y2 = y + ICON_HEIGHT - 1;
        g2.drawLine(x1, y, x + ICON_WIDTH - 1, y2);

        x1 = x + ICON_WIDTH - 3;
        g2.drawLine(x1, y, x1, y2);

        y2 = y + (ICON_HEIGHT / 2);
        g2.drawLine(x1 - 1, y, x, y2);
        g2.drawLine(x1 - 1, y + ICON_HEIGHT - 1, x, y2);

        g2.setTransform(oldTransform);
        
    }
    
}
















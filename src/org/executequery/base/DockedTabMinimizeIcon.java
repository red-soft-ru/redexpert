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

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Simple icon drawing the close button
 * for a closeable tab on the CloseTabbedPane.
 *
 * @author Takis Diakoumis
 */
public class DockedTabMinimizeIcon implements TabControlIcon,
        SwingConstants {

    /**
     * The icons orientation
     */
    private final int orientation;

    /**
     * Creates a new instance of DockedTabMinimizeIcon
     * at the specified orientation.
     *
     * @param orientation orientation - WEST | EAST | CENTER
     */
    public DockedTabMinimizeIcon(int orientation) {
        this.orientation = orientation;
    }

    /**
     * Returns the icon's height.
     *
     * @return the height of the icon
     */
    @Override
    public int getIconHeight() {
        return TabControlIcon.iconHeight();
    }

    /**
     * Returns the icon's width.
     *
     * @return the width of the icon
     */
    @Override
    public int getIconWidth() {
        return TabControlIcon.iconWidth();
    }

    /**
     * Draw the icon at the specified location.
     */
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform oldTransform = g2.getTransform();

        g2.setColor(TabControlIcon.iconColor());

        if (orientation != WEST) {
            double theta = 0;
            double xOrigin = x + ((double) getIconWidth() / 2);
            double yOrigin = y + ((double) getIconHeight() / 2);

            if (orientation == CENTER) {
                theta = Math.PI * 1.5;
            } else if (orientation == EAST) {
                theta = Math.PI;
            }
            g2.rotate(theta, xOrigin, yOrigin);
        }

        int x1 = x + getIconWidth() - 1;
        int y2 = y + getIconHeight() - 1;
        g2.drawLine(x1, y, x + getIconWidth() - 1, y2);

        x1 = x + getIconWidth() - 3;
        g2.drawLine(x1, y, x1, y2);

        y2 = y + (getIconHeight() / 2);
        g2.drawLine(x1 - 1, y, x, y2);
        g2.drawLine(x1 - 1, y + getIconHeight() - 1, x, y2);

        g2.setTransform(oldTransform);
    }

}

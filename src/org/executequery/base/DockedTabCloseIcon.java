/*
 * DockedTabCloseIcon.java
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

import java.awt.*;

/**
 * Simple icon drawing the close button for a closeable tab on the CloseTabbedPane.
 *
 * @author Takis Diakoumis
 */
public class DockedTabCloseIcon implements TabControlIcon {

    /**
     * Creates a new instance of TabCloseButtonIcon
     */
    public DockedTabCloseIcon() {
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
        g.setColor(TabControlIcon.iconColor());
        g.drawLine(x, y, x + getIconWidth() - 1, y + getIconHeight() - 1);
        g.drawLine(x + getIconWidth() - 1, y, x, y + getIconHeight() - 1);
    }

}

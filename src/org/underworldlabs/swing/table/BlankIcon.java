/*
 * BlankIcon.java
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

package org.underworldlabs.swing.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 *
 * @author   Takis Diakoumis
 */
public class BlankIcon implements Icon {
    
    private Color fillColor;
    private int size;
    
    public BlankIcon() {
        this(null, 11);
    }
    
    public BlankIcon(Color color, int size) {
        fillColor = color;
        this.size = size;
    }
    
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (fillColor != null) {
            g.setColor(fillColor);
            g.drawRect(x, y, size-1, size-1);
        }
    }
    
    public int getIconWidth() {
        return size;
    }
    
    public int getIconHeight() {
        return size;
    }
}








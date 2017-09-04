/*
 * ArrowIcon.java
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

import javax.swing.UIManager;
import javax.swing.Icon;

/** 
 * A simple arrow icon for all directions.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class ArrowIcon implements Icon {
    
    // direction constants
    public static final int UP = 0;
    public static final int DOWN  = 1;
    public static final int RIGHT = 2;
    public static final int LEFT = 2;
    
    private int size;
    private static final int DEFAULT_SIZE = 10;
    
    private int direction;
    private Color fillColour;
    
    public ArrowIcon(int direction) {
        this(UIManager.getColor("controlShadow"), direction, DEFAULT_SIZE);
    }

    public ArrowIcon(int direction, int size) {
        this(UIManager.getColor("controlShadow"), direction, size);
    }

    public ArrowIcon(Color fillColour, int direction) {
        this(fillColour, direction, DEFAULT_SIZE);
    }

    public ArrowIcon(Color fillColour, int direction, int size) {
        this.fillColour = fillColour;
        this.direction = direction;
        setSize(size);
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    public void paintIcon(Component c, Graphics g, int x, int y) {
        
        switch (direction) {
            
            case DOWN:
                drawDownArrow(g, x - 1, y);
                break;
                
            case UP:
                drawUpArrow(g, x - 1, y);
                break;
                
            case RIGHT:
                drawRightArrow(g, x - 1, y);
                break;
                
        }
        
    }
    
    public int getIconWidth() {
        return size;
    }
    
    public int getIconHeight() {
        return size;
    }
    
    private void drawRightArrow(Graphics g, int xo, int yo) {
        g.setColor(fillColour);
        
        int x = 0, y = 0;
        
        for (int i = 1; i <= size; i++) {
            
            y = yo + i + 1;
            
            if (i > size / 2) {
                
                for (int j = size - i; j >= 1; j--) {
                    x = xo + j;
                    g.drawLine(x, y, x, y);
                }
                
            }
            
            else {
                
                for (int j = 1; j <= i; j++) {
                    x = xo + j;
                    g.drawLine(x, y, x, y);
                }
                
            }
            
        }
    }
    
    private void drawDownArrow(Graphics g, int xo, int yo) {
        g.setColor(fillColour);
        
        int x = 0, y = 0;
        
        for (int i = 1; i <= size; i++) {
            
            y = yo + i + 2;
            
            for (int j = i; j <= size; j++) {
                
                if (j > size - i) {
                    break;
                }

                x = xo + j;
                g.drawLine(x, y, x, y);
                
            }
            
        }
    }
    
    private void drawUpArrow(Graphics g, int xo, int yo) {
        g.setColor(fillColour);
        
        int yOffset = yo + 2 + (size / 2);
        int x = 0, y = 0;
        
        for (int i = size; i >= 1; i--) {
            
            y = yOffset - i;
            
            for (int j = i; j <= size; j++) {
                
                if (j > size - i) {
                    break;
                }

                x = xo + j;
                g.drawLine(x, y, x, y);
                
            }
            
        }
        
    }
    
}









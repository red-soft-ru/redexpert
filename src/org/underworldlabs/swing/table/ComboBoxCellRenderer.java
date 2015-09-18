/*
 * ComboBoxCellRenderer.java
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

package org.underworldlabs.swing.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.underworldlabs.util.LabelValuePair;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ComboBoxCellRenderer extends JLabel
                                  implements TableCellRenderer {
    
    private static Color iconColor;
    
    static {
        iconColor = Color.DARK_GRAY.darker();
    }
    
    /** Creates a new instance of ComboBoxCellRenderer */
    public ComboBoxCellRenderer() {}

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean cellHasFocus,
                                                   int row, 
                                                   int col) {
        setFont(table.getFont());
        
        if (value == null) {

            setText("");

        } else {
            
            if (value instanceof LabelValuePair) {
            
                setText(((LabelValuePair) value).getLabel());
                
            } else {
             
                setText(value.toString());
            }
        }

        return this;
    }

    private int ICON_HEIGHT = 10;
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        int height = getHeight();
        int width = getWidth();

        int x = 0, y = 0;
        int xo = width - 15;
        int yo = (height - ICON_HEIGHT) / 2;

        g.setColor(iconColor);        
        for (int i = 1; i <= ICON_HEIGHT; i++) {
            
            y = yo + i + 2;
            
            for (int j = i; j <= ICON_HEIGHT; j++) {
                
                if (j > ICON_HEIGHT - i)
                    break;
                
                x = xo + j;
                g.drawLine(x, y, x, y);
                
            }
            
        }

    }

}















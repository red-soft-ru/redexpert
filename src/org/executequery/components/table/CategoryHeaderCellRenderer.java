/*
 * CategoryHeaderCellRenderer.java
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

package org.executequery.components.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.executequery.Constants;
import org.underworldlabs.swing.plaf.UIUtils;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class CategoryHeaderCellRenderer extends JLabel
                                        implements TableCellRenderer {
    
    private static Font font;
    private static Color background;
    private static Color foreground;
    
    /** Creates a new instance of CategoryHeaderCellRenderer */
    public CategoryHeaderCellRenderer() {}
    
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean cellHasFocus,
                                                   int row, 
                                                   int col) {

        if (background == null) {
            background = table.getGridColor();
            foreground = UIUtils.getColour("executequery.PreferencesHeader.foreground", background.darker().darker());
        }
        
        setBackground(background);

        if (col == 0 || col == 2) {
            setText(Constants.EMPTY);
            return this;
        }

        if (font == null) {
            Font _font = table.getFont();
            font = new Font(_font.getName(), Font.BOLD, _font.getSize() + 1);
        }

        setFont(font);
        setForeground(foreground);
        setText(value.toString());        
        return this;
    }

    public void paintComponent(Graphics g) {
        int height = getHeight();
        int width = getWidth();
        g.setColor(background);
        g.fillRect(0, 0, width, height);
        super.paintComponent(g);
    }
    
}



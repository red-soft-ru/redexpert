/*
 * ListLineRenderer.java
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

package org.underworldlabs.swing;

import java.awt.Graphics;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Component;

import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import java.util.StringTokenizer;

/**
 * The ListLineRenderer class provides a renderer for a JList where
 * the contents may be text with new line characters.
 * It removes the system new line characters (square symbols) 
 * from each statement for display.
 * 
 * @author   Takis Diakoumis
 */
public class ListLineRenderer extends JLabel
                              implements ListCellRenderer {
    
    protected static Border noFocusBorder;
    protected FontMetrics fMetrics = null;
    protected Insets insets = new Insets(0, 0, 0, 0);
    
    protected int defaultLineSpace = 1;
    
    public ListLineRenderer() {
        super();
        noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        setOpaque(true);
        setBorder(noFocusBorder);
    }
    
    public Component getListCellRendererComponent(JList list,
                                                  Object value, 
                                                  int index, 
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        setText(value.toString());
        setBackground(isSelected ?
                        list.getSelectionBackground() : list.getBackground());
        setForeground(isSelected ?
                        list.getSelectionForeground() : list.getForeground());

        setFont(list.getFont());
        setBorder((cellHasFocus) ?
                    UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
        
        return this;
    }
    
    public void setDefaultLineSpace(int defaultLine) {
        defaultLineSpace = defaultLine;
    }
    
    public int getDefaultLineSpace() {
        return defaultLineSpace;
    }
    
    public int getTab(int index) {
        return defaultLineSpace * index;
    }
    
    public void paint(Graphics g) {

        fMetrics = g.getFontMetrics();
        
        g.setColor(getBackground());
        
        g.fillRect(0, 0, getWidth(), getHeight());
        
        getBorder().paintBorder(this, g, 0, 0, getWidth(), getHeight());
        
        g.setColor(getForeground());
        g.setFont(getFont());

        insets = getInsets(insets);

        int x = insets.left;
        int y = insets.top + fMetrics.getAscent();
        
        StringTokenizer	st = new StringTokenizer(getText(), "\n");

        while (st.hasMoreTokens()) {
            
            String sNext = st.nextToken();
            g.drawString(sNext, x, y);
            x += fMetrics.stringWidth(sNext);
            
            if (!st.hasMoreTokens()) {
        
                break;
            }
            
            int index = 0;
            while (x >= getTab(index)) {
                
                index++;
            }
            
            x = getTab(index);
        }
    }
    
}


















/*
 * VerticalTextIcon.java
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.underworldlabs.swing.plaf.UIUtils;

/**
 * Vertical text icon derived from the original by
 * Santhosh Kumar http://jroller.com/pages/santhosh.
 *
 * @author Santhosh Kumar
 * @author   Takis Diakoumis
 */
public class VerticalTextIcon implements Icon, SwingConstants {
    
    private Font font = UIManager.getFont("Label.font");
    private FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
    
    private String text;
    private int width, height;
    private boolean clockwize;
    
    public VerticalTextIcon(String text, boolean clockwize){
        this.text = text;
        width = SwingUtilities.computeStringWidth(fm, text);
        height = fm.getHeight();
        this.clockwize = clockwize;
    }
    
    public void paintIcon(Component c, Graphics g, int x, int y) {

        UIUtils.antialias(g);
        
        Graphics2D g2 = (Graphics2D)g;
        Font oldFont = g.getFont();
        Color oldColor = g.getColor();
        AffineTransform oldTransform = g2.getTransform();
        
        g.setFont(font);
        g.setColor(Color.black);

        if (clockwize){
            g2.translate(x+getIconWidth(), y);
            g2.rotate(Math.PI/2);
        } 
        else {
            g2.translate(x, y+getIconHeight());
            g2.rotate(-Math.PI/2);
        }

        g.drawString(text, 0, fm.getLeading()+fm.getAscent());
        
        g.setFont(oldFont);
        g.setColor(oldColor);
        g2.setTransform(oldTransform);
    }
    
    public int getIconWidth(){
        return height;
    }
    
    public int getIconHeight(){
        return width;
    }
}






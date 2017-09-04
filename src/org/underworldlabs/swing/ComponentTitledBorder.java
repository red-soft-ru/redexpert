/*
 * ComponentTitledBorder.java
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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/** <p>This class provides a titled border with a component
 *  as the title the border.<br>
 *  Modified example from
 *  http://www2.gol.com/users/tame/swing/examples/BorderExamples1.html
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class ComponentTitledBorder extends TitledBorder {
    
    protected JComponent component;
    
    public ComponentTitledBorder(JComponent component) {
        this(null, component, LEFT, TOP);
    }
    
    public ComponentTitledBorder(Border border) {
        this(border, null, LEFT, TOP);
    }
    
    public ComponentTitledBorder(Border border, JComponent component) {
        this(border, component, LEFT, TOP);
    }
    
    public ComponentTitledBorder(Border border,
                                 JComponent component,
                                 int titleJustification,
                                 int titlePosition)      {
        super(border, null, titleJustification, titlePosition, null, null);
        this.component = component;
        if (border == null) {
            this.border = super.getBorder();
        }
    }
    
    
    public void paintBorder(Component c, Graphics g,
                            int x, int y, int width, int height) {
        
        Rectangle borderR = new Rectangle(x +  EDGE_SPACING,
                                          y +  EDGE_SPACING,
                                          width - (EDGE_SPACING * 2),
                                          height - (EDGE_SPACING * 2));
        Insets borderInsets;
        if (border != null) {
            borderInsets = border.getBorderInsets(c);
        } else {
            borderInsets = new Insets(0, 0, 0, 0);
        }
        
        Rectangle rect = new Rectangle(x, y, width, height);
        Insets insets = getBorderInsets(c);
        Rectangle compR = getComponentRect(rect, insets);

        int diff;
        switch (titlePosition) {
            case ABOVE_TOP:
                diff = compR.height + TEXT_SPACING;
                borderR.y += diff;
                borderR.height -= diff;
                break;
            case TOP:
            case DEFAULT_POSITION:
                diff = insets.top/2 - borderInsets.top - EDGE_SPACING;
                borderR.y += diff;
                borderR.height -= diff;
                break;
            case BELOW_TOP:
            case ABOVE_BOTTOM:
                break;
            case BOTTOM:
                diff = insets.bottom/2 - borderInsets.bottom - EDGE_SPACING;
                borderR.height -= diff;
                break;
            case BELOW_BOTTOM:
                diff = compR.height + TEXT_SPACING;
                borderR.height -= diff;
                break;
        }

        border.paintBorder(c, g, borderR.x,     borderR.y,
        borderR.width, borderR.height);

        Color col = g.getColor();
        g.setColor(c.getBackground());
        g.fillRect(compR.x - 2, compR.y, compR.width + 4, compR.height);
        g.setColor(col);
        
        if (component != null) {
            component.repaint();
        }
    }
    
    
    
    public Insets getBorderInsets(Component c, Insets insets) {
        Insets borderInsets;
        if (border != null) {
            borderInsets  = border.getBorderInsets(c);
        } else {
            borderInsets  = new Insets(0,0,0,0);
        }
        insets.top    = EDGE_SPACING + TEXT_SPACING + borderInsets.top;
        insets.right  = EDGE_SPACING + TEXT_SPACING + borderInsets.right;
        insets.bottom = EDGE_SPACING + TEXT_SPACING + borderInsets.bottom;
        insets.left   = EDGE_SPACING + TEXT_SPACING + borderInsets.left;
        
        if (c == null || component == null) {
            return insets;
        }
        
        int compHeight = 0;
        if (component != null) {
            compHeight = component.getPreferredSize().height;
        }
        
        switch (titlePosition) {
            case ABOVE_TOP:
                insets.top    += compHeight + TEXT_SPACING;
                break;
            case TOP:
            case DEFAULT_POSITION:
                insets.top    += Math.max(compHeight,borderInsets.top) - borderInsets.top;
                break;
            case BELOW_TOP:
                insets.top    += compHeight + TEXT_SPACING;
                break;
            case ABOVE_BOTTOM:
                insets.bottom += compHeight + TEXT_SPACING;
                break;
            case BOTTOM:
                insets.bottom += Math.max(compHeight,borderInsets.bottom) - borderInsets.bottom;
                break;
            case BELOW_BOTTOM:
                insets.bottom += compHeight + TEXT_SPACING;
                break;
        }
        return insets;
    }
    
    public JComponent getTitleComponent() {
        return component;
    }
    
    public void setTitleComponent(JComponent component) {
        this.component = component;
    }

    public Rectangle getComponentRect(Rectangle rect,Insets borderInsets) {
        if (component == null) {
            return new Rectangle(0,0,0,0);
        }

        Dimension compD = component.getPreferredSize();
        Rectangle compR = new Rectangle(0,0,compD.width,compD.height);
        switch (titlePosition) {
            case ABOVE_TOP:
                compR.y = EDGE_SPACING;
                break;
            case TOP:
            case DEFAULT_POSITION:
                compR.y = EDGE_SPACING +
                (borderInsets.top -EDGE_SPACING -TEXT_SPACING -compD.height)/2;
                break;
            case BELOW_TOP:
                compR.y = borderInsets.top - compD.height - TEXT_SPACING;
                break;
            case ABOVE_BOTTOM:
                compR.y = rect.height - borderInsets.bottom + TEXT_SPACING;
                break;
            case BOTTOM:
                compR.y = rect.height - borderInsets.bottom + TEXT_SPACING +
                (borderInsets.bottom -EDGE_SPACING -TEXT_SPACING -compD.height)/2;
                break;
            case BELOW_BOTTOM:
                compR.y = rect.height - compD.height - EDGE_SPACING;
                break;
        }
        switch (titleJustification) {
            case LEFT:
            case DEFAULT_JUSTIFICATION:
                compR.x = TEXT_INSET_H + borderInsets.left;
                break;
            case RIGHT:
                compR.x = rect.width - borderInsets.right -TEXT_INSET_H -compR.width;
                break;
            case CENTER:
                compR.x = (rect.width - compR.width) / 2;
                break;
        }
        return compR;
    }
    
}






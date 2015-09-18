/*
 * ThinInternalFrameBorder.java
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

package org.underworldlabs.swing.plaf;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.JInternalFrame;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

/* ----------------------------------------------------------------------------------
 * Modified from Java class javax.swing.plaf.metal.MetalBorders.InternalFrameBorder.
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *----------------------------------------------------------------------------------
 */

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ThinInternalFrameBorder extends AbstractBorder
        implements UIResource {
    
    private static final Insets insets = new Insets(3, 3, 3, 3);
    
    private static final int corner = 14;
    
    public void paintBorder(Component c, Graphics g,
            int x, int y, int w, int h) {
        
        Color background;
        Color highlight;
        Color shadow;
        
        if (c instanceof JInternalFrame && ((JInternalFrame)c).isSelected()) {
            background = MetalLookAndFeel.getPrimaryControlDarkShadow();
            highlight = MetalLookAndFeel.getPrimaryControlShadow();
            shadow = MetalLookAndFeel.getPrimaryControlInfo();
        }
        
        else {
            background = MetalLookAndFeel.getControlDarkShadow();
            highlight = MetalLookAndFeel.getControlShadow();
            shadow = MetalLookAndFeel.getControlInfo();
        }
        
        g.setColor(background);
        // Draw outermost lines
        g.drawLine( 1, 0, w-2, 0);
        g.drawLine( 0, 1, 0, h-2);
        g.drawLine( w-1, 1, w-1, h-2);
        g.drawLine( 1, h-1, w-2, h-1);
        
        // Draw the bulk of the border
        for (int i = 1; i < 5; i++) {
            g.drawRect(x+i,y+i,w-(i*2)-1, h-(i*2)-1);
        }
        
        if (c instanceof JInternalFrame && ((JInternalFrame)c).isResizable()) {
            g.setColor(highlight);
            // Draw the Long highlight lines
            g.drawLine( corner+1, 3, w-corner, 3);
            g.drawLine( 3, corner+1, 3, h-corner);
            g.drawLine( w-2, corner+1, w-2, h-corner);
            g.drawLine( corner+1, h-2, w-corner, h-2);
            
            g.setColor(shadow);
            // Draw the Long shadow lines
            g.drawLine( corner, 2, w-corner-1, 2);
            g.drawLine( 2, corner, 2, h-corner-1);
            g.drawLine( w-3, corner, w-3, h-corner-1);
            g.drawLine( corner, h-3, w-corner-1, h-3);
        }
        
    }
    
    public Insets getBorderInsets(Component c) {
        return insets;
    }
    
    public Insets getBorderInsets(Component c, Insets newInsets) {
        newInsets.top = insets.top;
        newInsets.left = insets.left;
        newInsets.bottom = insets.bottom;
        newInsets.right = insets.right;
        return newInsets;
    }
    
} // class


















/*
 * SmoothGradientUtils.java
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

package org.underworldlabs.swing.plaf.smoothgradient;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 *
 * @author   Takis Diakoumis
 */
public final class SmoothGradientUtils {
    
    static void drawDark3DBorder(Graphics g, int x, int y, int w, int h) {
        drawFlush3DBorder(g, x, y, w, h);
        g.setColor(SmoothGradientLookAndFeel.getControl());
        g.drawLine(x+1, y+1, 1, h - 3);
        g.drawLine(y+1, y+1, w - 3, 1);
    }
    
    static void drawDisabledBorder(Graphics g, int x, int y, int w, int h) {
        g.setColor(MetalLookAndFeel.getControlShadow());
        drawRect(g, x, y, w - 1, h - 1);
    }
    
    /**
     * Unlike <code>MetalUtils</code> we first draw with highlight then dark shadow
     */
    static void drawFlush3DBorder(Graphics g, int x, int y, int w, int h) {
        g.translate(x, y);
        g.setColor(SmoothGradientLookAndFeel.getControlHighlight());
        drawRect(g, 1, 1, w - 2, h - 2);
        g.drawLine(0, h - 1, 0, h - 1);
        g.drawLine(w - 1, 0, w - 1, 0);
        g.setColor(SmoothGradientLookAndFeel.getControlDarkShadow());
        drawRect(g, 0, 0, w - 2, h - 2);
        g.translate(-x, -y);
    }
    
    /**
     * Copied from <code>MetalUtils</code>.
     */
    static void drawPressed3DBorder(Graphics g, int x, int y, int w, int h) {
        g.translate(x, y);
        drawFlush3DBorder(g, 0, 0, w, h);
        g.setColor(MetalLookAndFeel.getControlShadow());
        g.drawLine(1, 1, 1, h - 3);
        g.drawLine(1, 1, w - 3, 1);
        g.translate(-x, -y);
    }
    
    /**
     * Copied from <code>MetalUtils</code>.
     */
    static void drawButtonBorder(Graphics g, int x, int y, int w, int h, boolean active) {
        if (active) {
            drawActiveButtonBorder(g, x, y, w, h);
        } else {
            drawFlush3DBorder(g, x, y, w, h);
        }
    }
    
    /**
     * Copied from <code>MetalUtils</code>.
     */
    static void drawActiveButtonBorder(Graphics g, int x, int y, int w, int h) {
        drawFlush3DBorder(g, x, y, w, h);
        g.setColor(SmoothGradientLookAndFeel.getPrimaryControl());
        g.drawLine( x+1, y+1, x+1, h-3 );
        g.drawLine( x+1, y+1, w-3, x+1 );
        g.setColor(SmoothGradientLookAndFeel.getPrimaryControlDarkShadow());
        g.drawLine( x+2, h-2, w-2, h-2 );
        g.drawLine( w-2, y+2, w-2, h-2 );
    }
    
    /**
     * Modified edges.
     */
    static void drawDefaultButtonBorder(Graphics g, int x, int y, int w, int h, boolean active) {
        drawButtonBorder(g, x+1, y+1, w-1, h-1, active);
        g.translate(x, y);
        g.setColor(SmoothGradientLookAndFeel.getControlDarkShadow() );
        drawRect(g, 0, 0, w-3, h-3 );
        g.drawLine(w-2, 0, w-2, 0);
        g.drawLine(0, h-2, 0, h-2);
        g.setColor(SmoothGradientLookAndFeel.getControl());
        g.drawLine(w-1, 0, w-1, 0);
        g.drawLine(0, h-1, 0, h-1);
        g.translate(-x, -y);
    }
    
    static void drawDefaultButtonPressedBorder(Graphics g, int x, int y, int w, int h) {
        drawPressed3DBorder(g, x + 1, y + 1, w - 1, h - 1);
        g.translate(x, y);
        g.setColor(SmoothGradientLookAndFeel.getControlDarkShadow());
        drawRect(g, 0, 0, w - 3, h - 3);
        g.drawLine(w - 2, 0, w - 2, 0);
        g.drawLine(0, h - 2, 0, h - 2);
        g.setColor(SmoothGradientLookAndFeel.getControl());
        g.drawLine(w - 1, 0, w - 1, 0);
        g.drawLine(0, h - 1, 0, h - 1);
        g.translate(-x, -y);
    }
    
    static void drawThinFlush3DBorder(Graphics g, int x, int y, int w, int h) {
        g.translate(x, y);
        g.setColor(SmoothGradientLookAndFeel.getControlHighlight());
        g.drawLine(0, 0, w - 2, 0);
        g.drawLine(0, 0, 0, h - 2);
        g.setColor(SmoothGradientLookAndFeel.getControlDarkShadow());
        g.drawLine(w - 1, 0, w - 1, h - 1);
        g.drawLine(0, h - 1, w - 1, h - 1);
        g.translate(-x, -y);
    }
    
    
    static void drawThinPressed3DBorder(Graphics g, int x, int y, int w, int h) {
        g.translate(x, y);
        g.setColor(SmoothGradientLookAndFeel.getControlDarkShadow());
        g.drawLine(0, 0, w - 2, 0);
        g.drawLine(0, 0, 0, h - 2);
        g.setColor(SmoothGradientLookAndFeel.getControlHighlight());
        g.drawLine(w - 1, 0, w - 1, h - 1);
        g.drawLine(0, h - 1, w - 1, h - 1);
        g.translate(-x, -y);
    }
    
    /*
     * Convenience function for determining ComponentOrientation.  Helps us
     * avoid having Munge directives throughout the code.
     */
    public static boolean isLeftToRight(Component c) {
        return c.getComponentOrientation().isLeftToRight();
    }
    
    
    // 3D Effects ***********************************************************************
    
    /**
     * Checks and answers if the specified component type has 3D effects.
     */
    static boolean is3D(String key) {
//        return true;
        		Object value = UIManager.get(key + "is3DEnabled");
        		return Boolean.TRUE.equals(value);
    }
    
    
    /**
     * Checks and answers if we have a custom hint that forces the 3D mode.
     *
     * @see #forceFlat
     */
    static boolean force3D(JComponent c) {
        Object value = c.getClientProperty(SmoothGradientLookAndFeel.IS_3D_KEY);
        return Boolean.TRUE.equals(value);
    }
    
    public static int getInt(Object key, int defaultValue) {
        Object value = UIManager.get(key);
        
        if (value instanceof Integer) {
            return ((Integer)value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String)value);
            } catch (NumberFormatException nfe) {}
        }
        return defaultValue;
    }
    
    
    /**
     * Checks and answers if we have a custom hint that forces the 3D mode.
     *
     * @see #forceFlat
     */
    static boolean forceFlat(JComponent c) {
        Object value = c.getClientProperty(SmoothGradientLookAndFeel.IS_3D_KEY);
        return Boolean.FALSE.equals(value);
    }
    
    
    // Painting 3D Effects *************************************************************
    
    private static float FRACTION_3D = 0.5f;
    
    
    private static void add3DEffekt(Graphics g, Rectangle r, boolean isHorizontal,
            Color startC0, Color stopC0, Color startC1, Color stopC1) {
        
        Graphics2D g2 = (Graphics2D) g;
        int xb0, yb0, xb1, yb1, xd0, yd0, xd1, yd1, width, height;
        if (isHorizontal) {
            width = r.width;
            height = (int) (r.height * FRACTION_3D);
            xb0 = r.x;
            yb0 = r.y;
            xb1 = xb0;
            yb1 = yb0 + height;
            xd0 = xb1;
            yd0 = yb1;
            xd1 = xd0;
            yd1 = r.y + r.height;
        } else {
            width = (int) (r.width * FRACTION_3D);
            height = r.height;
            xb0 = r.x;
            yb0 = r.y;
            xb1 = xb0 + width;
            yb1 = yb0;
            xd0 = xb1;
            yd0 = yb0;
            xd1 = r.x + r.width;
            yd1 = yd0;
        }
        g2.setPaint(new GradientPaint(xb0, yb0, stopC0, xb1, yb1, startC0));
        g2.fillRect(r.x, r.y, width, height);
        g2.setPaint(new GradientPaint(xd0, yd0, startC1, xd1, yd1, stopC1));
        g2.fillRect(xd0, yd0, width, height);
    }
    
    
    public static void add3DEffekt(Graphics g, Rectangle r) {
        Color brightenStop = UIManager.getColor("Plastic.brightenStop");
        if (null == brightenStop)
            brightenStop = SmoothGradientLookAndFeel.BRIGHTEN_STOP;
        
        // Add round sides
        Graphics2D g2 = (Graphics2D) g;
        int border = 10;
        g2.setPaint(new GradientPaint(r.x, r.y, brightenStop, r.x + border, r.y, SmoothGradientLookAndFeel.BRIGHTEN_START));
        g2.fillRect(r.x, r.y, border, r.height);
        int x = r.x + r.width -border;
        int y = r.y;
        g2.setPaint(new GradientPaint(x, y, SmoothGradientLookAndFeel.DARKEN_START, x + border, y, SmoothGradientLookAndFeel.LT_DARKEN_STOP));
        g2.fillRect(x, y, border, r.height);
        
        add3DEffekt(g, r, true, SmoothGradientLookAndFeel.BRIGHTEN_START, brightenStop, SmoothGradientLookAndFeel.DARKEN_START, SmoothGradientLookAndFeel.LT_DARKEN_STOP);
    }
    
    
    public static void addLight3DEffekt(Graphics g, Rectangle r, boolean isHorizontal) {
        Color ltBrightenStop = UIManager.getColor("Plastic.ltBrightenStop");
        if (null == ltBrightenStop)
            ltBrightenStop = SmoothGradientLookAndFeel.LT_BRIGHTEN_STOP;
        
        add3DEffekt(g, r, isHorizontal, SmoothGradientLookAndFeel.BRIGHTEN_START, ltBrightenStop, SmoothGradientLookAndFeel.DARKEN_START, SmoothGradientLookAndFeel.LT_DARKEN_STOP);
    }
    
    
    public static void addLight3DEffekt(Graphics g, Rectangle r) {
        Color ltBrightenStop = UIManager.getColor("Plastic.ltBrightenStop");
        if (null == ltBrightenStop)
            ltBrightenStop = SmoothGradientLookAndFeel.LT_BRIGHTEN_STOP;
        
        add3DEffekt(g, r, true, SmoothGradientLookAndFeel.DARKEN_START, SmoothGradientLookAndFeel.LT_DARKEN_STOP, SmoothGradientLookAndFeel.BRIGHTEN_START, ltBrightenStop);
    }
    
    
    // Low level graphics ***************************************************
    
    /**
     * An optimized version of Graphics.drawRect.
     */
    private static void drawRect(Graphics g, int x, int y, int w, int h) {
        g.fillRect(x,   y,   w+1, 1);
        g.fillRect(x,   y+1, 1,   h);
        g.fillRect(x+1, y+h, w,   1);
        g.fillRect(x+w, y+1, 1,   h);
    }
    
    
}
















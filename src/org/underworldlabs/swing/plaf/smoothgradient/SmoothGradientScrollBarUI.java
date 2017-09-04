/*
 * SmoothGradientScrollBarUI.java
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

import java.awt.Adjustable;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalScrollBarUI;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public final class SmoothGradientScrollBarUI extends MetalScrollBarUI {
    
    private static final String PROPERTY_PREFIX	  = "ScrollBar.";
    public  static final String MAX_BUMPS_WIDTH_KEY = PROPERTY_PREFIX + "maxBumpsWidth";
    
    private static Color shadowColor;
    private static Color highlightColor;
    private static Color darkShadowColor;
    private static Color thumbColor;
    private static Color thumbShadow;
    private static Color thumbHighlightColor;
    
    private static Color centerLineHighlight;
    private static Color centerLineShadow;
    
    // private SmoothGradientBumps bumps;
    
    public static ComponentUI createUI(JComponent b) {
        return new SmoothGradientScrollBarUI();
    }
    
    protected void installDefaults() {
        super.installDefaults();
        //bumps = new SmoothGradientBumps(10, 10, thumbHighlightColor, thumbShadow, thumbColor);
    }
    
    protected JButton createDecreaseButton(int orientation) {
        decreaseButton = new SmoothGradientArrowButton(orientation, scrollBarWidth, isFreeStanding);
        return decreaseButton;
    }
    
    
    protected JButton createIncreaseButton(int orientation) {
        increaseButton = new SmoothGradientArrowButton(orientation, scrollBarWidth, isFreeStanding);
        return increaseButton;
    }
    
    protected void configureScrollBarColors() {
        super.configureScrollBarColors();
        shadowColor         = UIManager.getColor(PROPERTY_PREFIX + "shadow");
        highlightColor      = UIManager.getColor(PROPERTY_PREFIX + "highlight");
        darkShadowColor     = UIManager.getColor(PROPERTY_PREFIX + "darkShadow");
        thumbColor          = UIManager.getColor(PROPERTY_PREFIX + "thumb");
        thumbShadow         = UIManager.getColor(PROPERTY_PREFIX + "thumbShadow");
        thumbHighlightColor = UIManager.getColor(PROPERTY_PREFIX + "thumbHighlight");
        
        centerLineHighlight = new Color(198,198,229);
        centerLineShadow = new Color(109,109,177);
        
    }
    
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        g.translate(trackBounds.x, trackBounds.y);
        
        boolean leftToRight = SmoothGradientUtils.isLeftToRight(c);
        
        if (scrollbar.getOrientation() == Adjustable.VERTICAL) {
            if (!isFreeStanding) {
                if (!leftToRight) {
                    trackBounds.width += 1;
                    g.translate(-1, 0);
                } else {
                    trackBounds.width += 2;
                }
            }
            
            if (c.isEnabled()) {
                g.setColor(darkShadowColor);
                g.drawLine(0, 0, 0, trackBounds.height - 1);
                g.drawLine(trackBounds.width - 2, 0, trackBounds.width - 2, trackBounds.height - 1);
                g.drawLine(1, trackBounds.height - 1, trackBounds.width - 1, trackBounds.height - 1);
                g.drawLine(1, 0, trackBounds.width - 2, 0);
                
                g.setColor(shadowColor);
//                	g.setColor( Color.red);
                g.drawLine(1, 1, 1, trackBounds.height - 2);
                g.drawLine(1, 1, trackBounds.width - 3, 1);
                if (scrollbar.getValue() != scrollbar.getMaximum()) { // thumb shadow
                    int y = thumbRect.y + thumbRect.height - trackBounds.y;
                    g.drawLine(1, y, trackBounds.width - 1, y);
                }
                g.setColor(highlightColor);
                g.drawLine(trackBounds.width - 1, 0, trackBounds.width - 1, trackBounds.height - 1);
            } else {
                SmoothGradientUtils.drawDisabledBorder(g, 0, 0, trackBounds.width, trackBounds.height);
            }
            
            if (!isFreeStanding) {
                if (!leftToRight) {
                    trackBounds.width -= 1;
                    g.translate(1, 0);
                } else {
                    trackBounds.width -= 2;
                }
            }
        } else { // HORIZONTAL
            if (!isFreeStanding) {
                trackBounds.height += 2;
            }
            
            if (c.isEnabled()) {
                g.setColor(darkShadowColor);
                g.drawLine(0, 0, trackBounds.width - 1, 0); // top
                g.drawLine(0, 1, 0, trackBounds.height - 2); // left
                g.drawLine(0, trackBounds.height - 2, trackBounds.width - 1, trackBounds.height - 2);
                // bottom
                g.drawLine(trackBounds.width - 1, 1, trackBounds.width - 1, trackBounds.height - 1);
                
                // right
                g.setColor(shadowColor);
                //	g.setColor( Color.red);
                g.drawLine(1, 1, trackBounds.width - 2, 1); // top
                g.drawLine(1, 1, 1, trackBounds.height - 3); // left
                g.drawLine(0, trackBounds.height - 1, trackBounds.width - 1, trackBounds.height - 1);
                // bottom
                if (scrollbar.getValue() != scrollbar.getMaximum()) { // thumb shadow
                    int x = thumbRect.x + thumbRect.width - trackBounds.x;
                    g.drawLine(x, 1, x, trackBounds.height - 1);
                }
            } else {
                SmoothGradientUtils.drawDisabledBorder(g, 0, 0, trackBounds.width, trackBounds.height);
            }
            
            if (!isFreeStanding) {
                trackBounds.height -= 2;
            }
        }
        g.translate(-trackBounds.x, -trackBounds.y);
    }
    
    
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        
        if (!c.isEnabled()) {
            return;
        }
        
        boolean leftToRight = SmoothGradientUtils.isLeftToRight(c);
        
        g.translate(thumbBounds.x, thumbBounds.y);
        
        if (scrollbar.getOrientation() == Adjustable.VERTICAL) {
            
            if (!isFreeStanding) {
                if (!leftToRight) {
                    thumbBounds.width += 1;
                    g.translate(-1, 0);
                } else {
                    thumbBounds.width += 2;
                }
                
            }
            
            g.setColor(thumbColor);
            g.fillRect(0, 0, thumbBounds.width - 2, thumbBounds.height - 1);
            
            g.setColor(thumbShadow);
            g.drawRect(0, 0, thumbBounds.width - 2, thumbBounds.height - 1);
            
            g.setColor(thumbHighlightColor);
            g.drawLine(1, 1, thumbBounds.width - 3, 1);
            g.drawLine(1, 1, 1, thumbBounds.height - 2);
            
            //			paintBumps(g, c, 3, 4, thumbBounds.width - 6, thumbBounds.height - 7);

            if (is3D()) {
            
                // draw the center lines
                int lineY = 4 + (thumbBounds.height - 13) / 2;
                g.setColor(centerLineHighlight);
                g.drawLine(4, lineY, 11, lineY);
                g.drawLine(4, lineY+3, 11, lineY+3);
                g.drawLine(4, lineY+6, 11, lineY+6);
                g.setColor(centerLineShadow);
                g.drawLine(5, lineY+1, 12, lineY+1);
                g.drawLine(5, lineY+4, 12, lineY+4);
                g.drawLine(5, lineY+7, 12, lineY+7);
            }

            if (!isFreeStanding) {
                if (!leftToRight) {
                    thumbBounds.width -= 1;
                    g.translate(1, 0);
                } else {
                    thumbBounds.width -= 2;
                }
            }
        } else { // HORIZONTAL
            
            if (!isFreeStanding) {
                thumbBounds.height += 2;
            }
            
            g.setColor(thumbColor);
            g.fillRect(0, 0, thumbBounds.width - 1, thumbBounds.height - 2);
            
            g.setColor(thumbShadow);
            g.drawRect(0, 0, thumbBounds.width - 1, thumbBounds.height - 2);
            
            g.setColor(thumbHighlightColor);
            g.drawLine(1, 1, thumbBounds.width - 2, 1);
            g.drawLine(1, 1, 1, thumbBounds.height - 3);
            
            //			paintBumps(g, c, 4, 3, thumbBounds.width - 7, thumbBounds.height - 6);
            
            // draw the center lines

            if (is3D()) {
            
                int lineX = (thumbBounds.width - 7) / 2;
                
                g.setColor(centerLineShadow);
                g.drawLine(lineX, 4, lineX, 11);
                g.drawLine(lineX+3, 4, lineX+3, 11);
                g.drawLine(lineX+6, 4, lineX+6, 11);
                g.setColor(centerLineHighlight);
                g.drawLine(lineX+1, 5, lineX+1, 12);
                g.drawLine(lineX+4, 5, lineX+4, 12);
                g.drawLine(lineX+7, 5, lineX+7, 12);
            }

            if (!isFreeStanding) {
                thumbBounds.height -= 2;
            }
            
        }
        
        g.translate(-thumbBounds.x, -thumbBounds.y);
        
        if (is3D())
            paintThumb3D(g, thumbBounds);
        
    }

    private boolean is3D() {
        return SmoothGradientUtils.is3D(PROPERTY_PREFIX);
    }
    
    /*
    private void paintBumps(Graphics g, JComponent c, int x, int y, int width, int height) {
//		if (!useNarrowBumps()) {
//			bumps.setBumpArea(width, height);
//			bumps.paintIcon(c, g, x, y);
//		} else {
            int MAX_WIDTH= UIManager.getInt(MAX_BUMPS_WIDTH_KEY);
            int myWidth  = Math.min(MAX_WIDTH, width);
            int myHeight = Math.min(MAX_WIDTH, height);
            int myX      = x + (width  - myWidth) / 2;
            int myY      = y + (height - myHeight) / 2;
            bumps.setBumpArea(myWidth, myHeight);
            bumps.paintIcon(c, g, myX, myY);
//		}
    }
     
     */
    private void paintThumb3D(Graphics g, Rectangle thumbBounds) {
        boolean isHorizontal = scrollbar.getOrientation() == Adjustable.HORIZONTAL;
        int width   = thumbBounds.width  - (isHorizontal ? 3 : 1);
        int height  = thumbBounds.height - (isHorizontal ? 1 : 3);
        Rectangle r = new Rectangle(thumbBounds.x + 2, thumbBounds.y + 2, width, height);
        SmoothGradientUtils.addLight3DEffekt(g, r, isHorizontal);
    }
    
    
    // Accessing Special Client Properties **********************************************
    
    /*
    private boolean useNarrowBumps() {
        Object value = UIManager.get(MAX_BUMPS_WIDTH_KEY);
        return value != null && value instanceof Integer;
    }
    */

}








/*
 * FlatSplitPaneDivider.java
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
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JButton;
import javax.swing.JSplitPane;

import javax.swing.border.Border;

import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import javax.swing.plaf.metal.MetalLookAndFeel;

/*
 * @(#)MetalSplitPaneUI.java	1.9 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 * Modified metal split pane divider
 */
/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class FlatSplitPaneDivider extends BasicSplitPaneDivider {
    
    private int inset = 2;
    
    public FlatSplitPaneDivider(BasicSplitPaneUI ui) {
        super(ui);
        setLayout(new FlapSplitPaneDividerLayout());
    }
    
    // --------------------------------------------
    // the main thing we have done here is to remove
    // the metal bumps from the divider. all else remains
    // the same as in MetalSplitPaneUI. the aim was to
    // remove the bumps but retain the small buttons.
    // --------------------------------------------
    
    public void paint(Graphics g) {
        super.paint(g);
    }
    
    /**
     * Creates and return an instance of JButton that can be used to
     * collapse the left component in the metal split pane.
     */
    protected JButton createLeftOneTouchButton() {
        JButton b = new JButton() {
            // Sprite buffer for the arrow image of the left button
            int[][]     buffer = {{0, 0, 0, 2, 2, 0, 0, 0, 0},
            {0, 0, 2, 1, 1, 1, 0, 0, 0},
            {0, 2, 1, 1, 1, 1, 1, 0, 0},
            {2, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 3, 3, 3, 3, 3, 3, 3, 3}};
            
            public void setBorder(Border b) {}
            
            public void paint(Graphics g) {
                JSplitPane splitPane = getSplitPaneFromSuper();
                if(splitPane != null) {
                    int         oneTouchSize = getOneTouchSizeFromSuper();
                    int         orientation = getOrientationFromSuper();
                    int         blockSize = Math.min(getDividerSize(),
                    oneTouchSize);
                    
                    // Initialize the color array
                    Color[]     colors = {
                        this.getBackground(),
                        MetalLookAndFeel.getPrimaryControlDarkShadow(),
                        MetalLookAndFeel.getPrimaryControlInfo(),
                        MetalLookAndFeel.getPrimaryControlHighlight()};
                        
                        // Fill the background first ...
                        g.setColor(this.getBackground());
                        g.fillRect(0, 0, this.getWidth(),
                        this.getHeight());
                        
                        // ... then draw the arrow.
                        if (getModel().isPressed()) {
                            // Adjust color mapping for pressed button state
                            colors[1] = colors[2];
                        }
                        if(orientation == JSplitPane.VERTICAL_SPLIT) {
                            // Draw the image for a vertical split
                            for (int i=1; i<=buffer[0].length; i++) {
                                for (int j=1; j<blockSize; j++) {
                                    if (buffer[j-1][i-1] == 0) {
                                        continue;
                                    }
                                    else {
                                        g.setColor(
                                        colors[buffer[j-1][i-1]]);
                                    }
                                    g.drawLine(i, j, i, j);
                                }
                            }
                        }
                        else {
                            // Draw the image for a horizontal split
                            // by simply swaping the i and j axis.
                            // Except the drawLine() call this code is
                            // identical to the code block above. This was done
                            // in order to remove the additional orientation
                            // check for each pixel.
                            for (int i=1; i<=buffer[0].length; i++) {
                                for (int j=1; j<blockSize; j++) {
                                    if (buffer[j-1][i-1] == 0) {
                                        // Nothing needs
                                        // to be drawn
                                        continue;
                                    }
                                    else {
                                        // Set the color from the
                                        // color map
                                        g.setColor(
                                        colors[buffer[j-1][i-1]]);
                                    }
                                    // Draw a pixel
                                    g.drawLine(j, i, j, i);
                                }
                            }
                        }
                }
            }
            
            // Don't want the button to participate in focus traversable.
            public boolean isFocusTraversable() {
                return false;
            }
        };
        
        b.setRequestFocusEnabled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        return b;
    }
    
    /**
     * Creates and return an instance of JButton that can be used to
     * collapse the right component in the metal split pane.
     */
    protected JButton createRightOneTouchButton() {
        JButton b = new JButton() {
            // Sprite buffer for the arrow image of the right button
            int[][]     buffer = {{2, 2, 2, 2, 2, 2, 2, 2},
            {0, 1, 1, 1, 1, 1, 1, 3},
            {0, 0, 1, 1, 1, 1, 3, 0},
            {0, 0, 0, 1, 1, 3, 0, 0},
            {0, 0, 0, 0, 3, 0, 0, 0}};
            
            public void setBorder(Border border) {}
            
            public void paint(Graphics g) {
                JSplitPane splitPane = getSplitPaneFromSuper();
                if(splitPane != null) {
                    int         oneTouchSize = getOneTouchSizeFromSuper();
                    int         orientation = getOrientationFromSuper();
                    int         blockSize = Math.min(getDividerSize(),
                    oneTouchSize);
                    
                    // Initialize the color array
                    Color[]     colors = {
                        this.getBackground(),
                        MetalLookAndFeel.getPrimaryControlDarkShadow(),
                        MetalLookAndFeel.getPrimaryControlInfo(),
                        MetalLookAndFeel.getPrimaryControlHighlight()};
                        
                        // Fill the background first ...
                        g.setColor(this.getBackground());
                        g.fillRect(0, 0, this.getWidth(),
                        this.getHeight());
                        
                        // ... then draw the arrow.
                        if (getModel().isPressed()) {
                            // Adjust color mapping for pressed button state
                            colors[1] = colors[2];
                        }
                        if(orientation == JSplitPane.VERTICAL_SPLIT) {
                            // Draw the image for a vertical split
                            for (int i=1; i<=buffer[0].length; i++) {
                                for (int j=1; j<blockSize; j++) {
                                    if (buffer[j-1][i-1] == 0) {
                                        continue;
                                    }
                                    else {
                                        g.setColor(
                                        colors[buffer[j-1][i-1]]);
                                    }
                                    g.drawLine(i, j, i, j);
                                }
                            }
                        }
                        else {
                            // Draw the image for a horizontal split
                            // by simply swaping the i and j axis.
                            // Except the drawLine() call this code is
                            // identical to the code block above. This was done
                            // in order to remove the additional orientation
                            // check for each pixel.
                            for (int i=1; i<=buffer[0].length; i++) {
                                for (int j=1; j<blockSize; j++) {
                                    if (buffer[j-1][i-1] == 0) {
                                        // Nothing needs
                                        // to be drawn
                                        continue;
                                    }
                                    else {
                                        // Set the color from the
                                        // color map
                                        g.setColor(
                                        colors[buffer[j-1][i-1]]);
                                    }
                                    // Draw a pixel
                                    g.drawLine(j, i, j, i);
                                }
                            }
                        }
                }
            }
            
            // Don't want the button to participate in focus traversable.
            public boolean isFocusTraversable() {
                return false;
            }
        };
        
        b.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setRequestFocusEnabled(false);
        return b;
    }
    
    /**
     * Used to layout a MetalSplitPaneDivider. Layout for the divider
     * involves appropriately moving the left/right buttons around.
     * <p>
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of MetalSplitPaneDivider.
     */
    public class FlapSplitPaneDividerLayout implements LayoutManager {
        
        public void layoutContainer(Container c) {
            
            JButton     leftButton = getLeftButtonFromSuper();
            JButton     rightButton = getRightButtonFromSuper();
            JSplitPane  splitPane = getSplitPaneFromSuper();
            int         orientation = getOrientationFromSuper();
            int         oneTouchSize = getOneTouchSizeFromSuper();
            int         oneTouchOffset = getOneTouchOffsetFromSuper();
            Insets      insets = getInsets();
            
            // This layout differs from the one used in BasicSplitPaneDivider.
            // It does not center justify the oneTouchExpadable buttons.
            // This was necessary in order to meet the spec of the Metal
            // splitpane divider.
            if (leftButton != null && rightButton != null &&
            c == FlatSplitPaneDivider.this) {
                
                if (splitPane.isOneTouchExpandable()) {
                    
                    if (orientation == JSplitPane.VERTICAL_SPLIT) {
                        int extraY = (insets != null) ? insets.top : 0;
                        int blockSize = getDividerSize();
                        
                        if (insets != null) {
                            blockSize -= (insets.top + insets.bottom);
                        }
                        extraY = 2;
                        blockSize = Math.min(blockSize, oneTouchSize);
                        leftButton.setBounds(oneTouchOffset, extraY,
                        blockSize * 2, blockSize);
                        rightButton.setBounds(oneTouchOffset +
                        oneTouchSize * 2, extraY,
                        blockSize * 2, blockSize);
                    }
                    
                    else {
                        int blockSize = getDividerSize();
                        int extraX = (insets != null) ? insets.left : 0;
                        
                        if (insets != null) {
                            blockSize -= (insets.left + insets.right);
                        }
                        
                        blockSize = Math.min(blockSize, oneTouchSize);
                        leftButton.setBounds(extraX, oneTouchOffset,
                        blockSize, blockSize * 2);
                        rightButton.setBounds(extraX, oneTouchOffset +
                        oneTouchSize * 2, blockSize,
                        blockSize * 2);
                    }
                    
                }
                
                else {
                    leftButton.setBounds(-5, -5, 1, 1);
                    rightButton.setBounds(-5, -5, 1, 1);
                }
            }
            
        }
        
        public Dimension minimumLayoutSize(Container c) {
            return new Dimension(0,0);
        }
        
        public Dimension preferredLayoutSize(Container c) {
            return new Dimension(0, 0);
        }
        
        public void removeLayoutComponent(Component c) {}
        
        public void addLayoutComponent(String string, Component c) {}
    }
    
  /*
   * The following methods only exist in order to be able to access protected
   * members in the superclass, because these are otherwise not available
   * in any inner class.
   */
    
    int getOneTouchSizeFromSuper() {
        return super.ONE_TOUCH_SIZE;
    }
    
    int getOneTouchOffsetFromSuper() {
        return super.ONE_TOUCH_OFFSET;
    }
    
    int getOrientationFromSuper() {
        return super.orientation;
    }
    
    JSplitPane getSplitPaneFromSuper() {
        return super.splitPane;
    }
    
    JButton getLeftButtonFromSuper() {
        return super.leftButton;
    }
    
    JButton getRightButtonFromSuper() {
        return super.rightButton;
    }
}















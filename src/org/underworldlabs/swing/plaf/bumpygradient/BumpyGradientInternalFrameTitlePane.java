/*
 * BumpyGradientInternalFrameTitlePane.java
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

package org.underworldlabs.swing.plaf.bumpygradient;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalInternalFrameTitlePane;
import org.underworldlabs.swing.plaf.smoothgradient.SmoothGradientLookAndFeel;
import org.underworldlabs.swing.plaf.smoothgradient.SmoothGradientUtils;

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public final class BumpyGradientInternalFrameTitlePane
        extends MetalInternalFrameTitlePane {
    
    private BumpyGradientBumps paletteBumps;
    
    private final BumpyGradientBumps activeBumps =
            new BumpyGradientBumps(
            0,
            0,
            BumpyGradientLookAndFeel.getInternalFrameBumpsHighlight(),
            BumpyGradientLookAndFeel.getPrimaryControlDarkShadow(),
            BumpyGradientLookAndFeel.getPrimaryControl());
    
    private final BumpyGradientBumps inactiveBumps =
            new BumpyGradientBumps(
            0,
            0,
            BumpyGradientLookAndFeel.getControlHighlight(),
            BumpyGradientLookAndFeel.getControlDarkShadow(),
            BumpyGradientLookAndFeel.getControl());
    
    public BumpyGradientInternalFrameTitlePane(JInternalFrame frame) {
        super(frame);
    }
    
    public void paintPalette(Graphics g) {
        boolean leftToRight = SmoothGradientUtils.isLeftToRight(frame);
        
        int width = getWidth();
        int height = getHeight();
        
        if (paletteBumps == null) {
            paletteBumps =
                    new BumpyGradientBumps(
                    0,
                    0,
                    BumpyGradientLookAndFeel.getPrimaryControlHighlight(),
                    BumpyGradientLookAndFeel.getPrimaryControlInfo(),
                    BumpyGradientLookAndFeel.getPrimaryControlShadow());
        }
        
        Color background = BumpyGradientLookAndFeel.getPrimaryControlShadow();
        Color darkShadow = BumpyGradientLookAndFeel.getControlDarkShadow();
        
        g.setColor(background);
        g.fillRect(0, 0, width, height);
        
        g.setColor(darkShadow);
        g.drawLine(0, height - 1, width, height - 1);
        
        int buttonsWidth = getButtonsWidth();
        int xOffset = leftToRight ? 4 : buttonsWidth + 4;
        int bumpLength = width - buttonsWidth - 2 * 4;
        int bumpHeight = getHeight() - 4;
        paletteBumps.setBumpArea(bumpLength, bumpHeight);
        paletteBumps.paintIcon(this, g, xOffset, 2);
    }
    
    protected LayoutManager createLayout() {
        return new PolishedTitlePaneLayout();
    }
    
    public void paintComponent(Graphics g) {
        
        if (isPalette) {
            paintPalette(g);
            return;
        }
        
        boolean leftToRight = SmoothGradientUtils.isLeftToRight(frame);
        boolean isSelected = frame.isSelected();
        
        int width = getWidth();
        int height = getHeight();
        
        Color background = null;
        Color foreground = null;
        Color shadow = null;
        
        BumpyGradientBumps bumps;
        
        if (isSelected) {
            background = SmoothGradientLookAndFeel.getWindowTitleBackground();
            foreground = SmoothGradientLookAndFeel.getWindowTitleForeground();
            bumps = activeBumps;
        } else {
            background = SmoothGradientLookAndFeel.getWindowTitleInactiveBackground();
            foreground = SmoothGradientLookAndFeel.getWindowTitleInactiveForeground();
            bumps = inactiveBumps;
        }
        
        shadow = SmoothGradientLookAndFeel.getControlDarkShadow();
        g.setColor(background);
        g.fillRect(0, 0, width, height);
        
        g.setColor(shadow);
        g.drawLine(0, height - 1, width, height - 1);
        g.drawLine(0, 0, 0, 0);
        g.drawLine(width - 1, 0, width - 1, 0);
        
        int titleLength = 0;
        int xOffset = leftToRight ? 5 : width - 5;
        String frameTitle = frame.getTitle();
        
        Icon icon = frame.getFrameIcon();
        
        if (icon != null) {
            
            if (!leftToRight)
                xOffset -= icon.getIconWidth();
            
            int iconY = ((height / 2) - (icon.getIconHeight() / 2));
            icon.paintIcon(frame, g, xOffset, iconY);
            xOffset += leftToRight ? icon.getIconWidth() + 5 : -5;
            
        }
        
        boolean iconifiable = frame.isIconifiable();
        boolean maximizable = frame.isMaximizable();
        
        if (frameTitle != null) {
            Font f = getFont();
            g.setFont(f);
            FontMetrics fm = g.getFontMetrics();
            //int fHeight = fm.getHeight();
            
            g.setColor(foreground);
            
            int yOffset = ((height - fm.getHeight()) / 2) + fm.getAscent();
            Rectangle rect = new Rectangle(0, 0, 0, 0);
            
            if (iconifiable)
                rect = iconButton.getBounds();
            
            else if (maximizable)
                rect = maxButton.getBounds();
            
            else if (frame.isClosable())
                rect = closeButton.getBounds();
            
            int titleW;
            
            if (leftToRight) {
                
                if (rect.x == 0)
                    rect.x = frame.getWidth() - frame.getInsets().right - 2;
                
                titleW = rect.x - xOffset - 4;
                frameTitle = getTitle(frameTitle, fm, titleW);
                
            }
            
            else {
                titleW = xOffset - rect.x - rect.width - 4;
                frameTitle = getTitle(frameTitle, fm, titleW);
                xOffset -= SwingUtilities.computeStringWidth(fm, frameTitle);
            }
            
            titleLength = SwingUtilities.computeStringWidth(fm, frameTitle);
            g.drawString(frameTitle, xOffset, yOffset);
            xOffset += leftToRight ? titleLength + 5 : -5;
            
        }
        
        int bumpXOffset;
        int bumpLength;
        int buttonsWidth = getButtonsWidth();
        
        if (leftToRight) {
            
            if (!iconifiable && !maximizable)
                bumpLength = width - buttonsWidth - xOffset - 4;
            else
                bumpLength = width - buttonsWidth - xOffset + 4;
            
            bumpXOffset = xOffset;
            
        }
        
        else {
            
            if (!iconifiable && !maximizable)
                bumpLength = width - buttonsWidth - xOffset - 4;
            else
                bumpLength = width - buttonsWidth - xOffset + 4;
            
            bumpXOffset = buttonsWidth + 5;
            
        }
        
        int bumpYOffset = 3;
        int bumpHeight = getHeight() - (2 * bumpYOffset);
        bumps.setBumpArea(bumpLength, bumpHeight);
        bumps.paintIcon(this, g, bumpXOffset, bumpYOffset);
        
        // draw the gradient
        Rectangle r = new Rectangle(1,0,width,height);
        SmoothGradientUtils.addLight3DEffekt(g, r, true);
        
    }
    
    protected String getTitle(
            String text,
            FontMetrics fm,
            int availTextWidth) {
        if ((text == null) || (text.equals("")))
            return "";
        int textWidth = SwingUtilities.computeStringWidth(fm, text);
        String clipString = "...";
        if (textWidth > availTextWidth) {
            int totalWidth = SwingUtilities.computeStringWidth(fm, clipString);
            int nChars;
            for (nChars = 0; nChars < text.length(); nChars++) {
                totalWidth += fm.charWidth(text.charAt(nChars));
                if (totalWidth > availTextWidth) {
                    break;
                }
            }
            text = text.substring(0, nChars) + clipString;
        }
        return text;
    }
    
    private int getButtonsWidth() {
        boolean leftToRight = SmoothGradientUtils.isLeftToRight(frame);
        
        int w = getWidth();
        int x = leftToRight ? w : 0;
        int spacing;
        
        // assumes all buttons have the same dimensions
        // these dimensions include the borders
        int buttonWidth = closeButton.getIcon().getIconWidth();
        
        if (frame.isClosable()) {
            if (isPalette) {
                spacing = 3;
                x += leftToRight ? -spacing - (buttonWidth + 2) : spacing;
                if (!leftToRight)
                    x += (buttonWidth + 2);
            } else {
                spacing = 4;
                x += leftToRight ? -spacing - buttonWidth : spacing;
                if (!leftToRight)
                    x += buttonWidth;
            }
        }
        
        if (frame.isMaximizable() && !isPalette) {
            spacing = frame.isClosable() ? 10 : 4;
            x += leftToRight ? -spacing - buttonWidth : spacing;
            if (!leftToRight)
                x += buttonWidth;
        }
        
        if (frame.isIconifiable() && !isPalette) {
            spacing =
                    frame.isMaximizable() ? 2 : (frame.isClosable() ? 10 : 4);
            x += leftToRight ? -spacing - buttonWidth : spacing;
            if (!leftToRight)
                x += buttonWidth;
        }
        
        return leftToRight ? w - x : x;
    }
    
    class PolishedTitlePaneLayout extends TitlePaneLayout {
        public void addLayoutComponent(String name, Component c) {}
        public void removeLayoutComponent(Component c) {}
        public Dimension preferredLayoutSize(Container c)  {
            return minimumLayoutSize(c);
        }
        
        public Dimension minimumLayoutSize(Container c) {
            // Compute width.
            int width = 30;
            if (frame.isClosable()) {
                width += 21;
            }
            if (frame.isMaximizable()) {
                width += 16 + (frame.isClosable() ? 10 : 4);
            }
            if (frame.isIconifiable()) {
                width += 16 + (frame.isMaximizable() ? 2 :
                    (frame.isClosable() ? 10 : 4));
            }
            FontMetrics fm = getFontMetrics(getFont());
            String frameTitle = frame.getTitle();
            int title_w = frameTitle != null ? fm.stringWidth(frameTitle) : 0;
            int title_length = frameTitle != null ? frameTitle.length() : 0;
            
            if (title_length > 2) {
                int subtitle_w =
                        fm.stringWidth(frame.getTitle().substring(0, 2) + "...");
                width += (title_w < subtitle_w) ? title_w : subtitle_w;
            } else {
                width += title_w;
            }
            
            // Compute height.
            int height = 0;
            if (isPalette) {
                height = paletteTitleHeight;
            } else {
                int fontHeight = fm.getHeight();
                fontHeight += 7;
                Icon icon = frame.getFrameIcon();
                int iconHeight = 0;
                if (icon != null) {
                    // SystemMenuBar forces the icon to be 16x16 or less.
                    iconHeight = Math.min(icon.getIconHeight(), 16);
                }
                iconHeight += 5;
                height = Math.max(fontHeight, iconHeight);
                
            }
            
            return new Dimension(width, height);
        }
        
        public void layoutContainer(Container c) {
            boolean leftToRight = SmoothGradientUtils.isLeftToRight(frame);
            
            int w = getWidth();
            int x = leftToRight ? w : 0;
            int y = 3;
            int spacing;
            
            // assumes all buttons have the same dimensions
            // these dimensions include the borders
            int buttonHeight = closeButton.getIcon().getIconHeight();
            int buttonWidth = closeButton.getIcon().getIconWidth();
            
            if(frame.isClosable()) {
                if (isPalette) {
                    spacing = 3;
                    x += leftToRight ? -spacing -(buttonWidth+2) : spacing;
                    closeButton.setBounds(x, y, buttonWidth+2, getHeight()-4);
                    if( !leftToRight ) x += (buttonWidth+2);
                } else {
                    spacing = 4;
                    x += leftToRight ? -spacing -buttonWidth : spacing;
                    closeButton.setBounds(x, y, buttonWidth, buttonHeight);
                    if( !leftToRight ) x += buttonWidth;
                }
            }
            
            if(frame.isMaximizable() && !isPalette ) {
                spacing = 1;
                x += leftToRight ? -spacing -buttonWidth : spacing;
                maxButton.setBounds(x, y, buttonWidth, buttonHeight);
                if( !leftToRight ) x += buttonWidth;
            }
            
            if(frame.isIconifiable() && !isPalette ) {
                spacing = 1;
                x += leftToRight ? -spacing -buttonWidth : spacing;
                iconButton.setBounds(x, y, buttonWidth, buttonHeight);
                if( !leftToRight ) x += buttonWidth;
            }
            
        }
        
    } // class PolishedTitlePaneLayout
    
}
















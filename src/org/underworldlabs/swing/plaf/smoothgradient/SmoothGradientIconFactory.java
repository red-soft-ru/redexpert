/*
 * SmoothGradientIconFactory.java
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
import java.awt.Paint;

import java.io.Serializable;

import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
final class SmoothGradientIconFactory {
    
    public static Icon getInternalFrameMinimizeIcon(int size) {
        return new InternalFrameMinimizeIcon(size);
    }
    
    private static class InternalFrameMinimizeIcon implements Icon, UIResource, Serializable {
        int iconSize = 16;
        
        public InternalFrameMinimizeIcon(int size) {
            iconSize = size;
        }
        
        public void paintIcon(Component c, Graphics g, int x, int y) {
            JButton parentButton = (JButton)c;
            ButtonModel buttonModel = parentButton.getModel();
            
            Color mainItemColor = SmoothGradientLookAndFeel.getPrimaryControlDarkShadow();
            Color darkHighlightColor = MetalLookAndFeel.getBlack();
            
            // background gradients
            Color bgStart = SmoothGradientLookAndFeel.FRAME_BUTTON_START_ACTIVE;
            Color bgStop = SmoothGradientLookAndFeel.FRAME_BUTTON_STOP_ACTIVE;
            
            // if the internal frame is inactive
            if (parentButton.getClientProperty("paintActive") != Boolean.TRUE) {
                mainItemColor = SmoothGradientLookAndFeel.getControlDarkShadow();
                bgStart = SmoothGradientLookAndFeel.FRAME_BUTTON_START_INACTIVE;
                bgStop = SmoothGradientLookAndFeel.FRAME_BUTTON_STOP_INACTIVE;
                
                // if inactive and pressed
                if (buttonModel.isPressed() && buttonModel.isArmed())
                    mainItemColor = darkHighlightColor;
                
            }
            
            // if the button is pressed and the mouse is over it
            else if (buttonModel.isPressed() && buttonModel.isArmed()) {
                mainItemColor = darkHighlightColor;
            }
            
            Graphics2D g2d = (Graphics2D)g;
            g2d.translate(x, y);
            
            g2d.setPaint(new GradientPaint(0, iconSize, bgStart, 0, 0, bgStop));
            
            g2d.fillRect(0, 0, iconSize, iconSize);
            
            g2d.setColor(mainItemColor);
            g2d.drawLine(4, 9, 12, 9);
            g2d.drawLine(3, 10, 13, 10);
            g2d.drawLine(3, 11, 13, 11);
            g2d.drawLine(4, 12, 12, 12);
            
            g.translate(-x, -y);
        }
        
        public int getIconWidth() {
            return iconSize;
        }
        
        public int getIconHeight() {
            return iconSize;
        }
        
    }  // class InternalFrameMinimizeIcon
    
    public static Icon getInternalFrameMaximizeIcon(int size) {
        return new InternalFrameMaximizeIcon(size);
    }
    
    private static class InternalFrameMaximizeIcon implements Icon, UIResource, Serializable {
        int iconSize = 16;
        
        public InternalFrameMaximizeIcon(int size) {
            iconSize = size;
        }
        
        public void paintIcon(Component c, Graphics g, int x, int y) {
            JButton parentButton = (JButton)c;
            ButtonModel buttonModel = parentButton.getModel();
            
            Color mainItemColor = SmoothGradientLookAndFeel.getPrimaryControlDarkShadow();
            Color darkHighlightColor = MetalLookAndFeel.getBlack();
            
            // background gradients
            Color bgStart = SmoothGradientLookAndFeel.FRAME_BUTTON_START_ACTIVE;
            Color bgStop = SmoothGradientLookAndFeel.FRAME_BUTTON_STOP_ACTIVE;
            
            // if the internal frame is inactive
            if (parentButton.getClientProperty("paintActive") != Boolean.TRUE) {
                mainItemColor = SmoothGradientLookAndFeel.getControlDarkShadow();
                bgStart = SmoothGradientLookAndFeel.FRAME_BUTTON_START_INACTIVE;
                bgStop = SmoothGradientLookAndFeel.FRAME_BUTTON_STOP_INACTIVE;
                
                // if inactive and pressed
                if (buttonModel.isPressed() && buttonModel.isArmed())
                    mainItemColor = darkHighlightColor;
                
            }
            
            // if the button is pressed and the mouse is over it
            else if (buttonModel.isPressed() && buttonModel.isArmed()) {
                mainItemColor = darkHighlightColor;
            }
            
            Graphics2D g2d = (Graphics2D)g;
            g2d.translate(x, y);
            
            Paint _paint = g2d.getPaint();
            // fill the background
            g2d.setPaint(new GradientPaint(0, iconSize, bgStart, 0, 0, bgStop));
            g2d.fillRect(0, 0, iconSize, iconSize);
            
            g2d.setPaint(_paint);
            // paint the image
            g2d.setColor(mainItemColor);
            g2d.drawLine(4, 3, 11, 3);
            g2d.fillRect(3, 4, 10, 3);
            g2d.drawLine(3, 7, 3, 11);
            g2d.drawLine(12, 7, 12, 11);
            g2d.drawLine(4, 12, 11, 12);
            
            g.translate(-x, -y);
        }
        
        public int getIconWidth() {
            return iconSize;
        }
        
        public int getIconHeight() {
            return iconSize;
        }
        
    }  // class InternalFrameMaximizeIcon
    
    public static Icon getInternalFrameAltMaximizeIcon(int size) {
        return new InternalFrameAltMaximizeIcon(size);
    }
    
    private static class InternalFrameAltMaximizeIcon implements Icon, UIResource, Serializable {
        int iconSize = 16;
        
        public InternalFrameAltMaximizeIcon(int size) {
            iconSize = size;
        }
        
        public void paintIcon(Component c, Graphics g, int x, int y) {
            JButton parentButton = (JButton)c;
            ButtonModel buttonModel = parentButton.getModel();
            
            Color mainItemColor = SmoothGradientLookAndFeel.getPrimaryControlDarkShadow();
            Color darkHighlightColor = MetalLookAndFeel.getBlack();
            
            // background gradients
            Color bgStart = SmoothGradientLookAndFeel.FRAME_BUTTON_START_ACTIVE;
            Color bgStop = SmoothGradientLookAndFeel.FRAME_BUTTON_STOP_ACTIVE;
            
            // if the internal frame is inactive
            if (parentButton.getClientProperty("paintActive") != Boolean.TRUE) {
                mainItemColor = SmoothGradientLookAndFeel.getControlDarkShadow();
                bgStart = SmoothGradientLookAndFeel.FRAME_BUTTON_START_INACTIVE;
                bgStop = SmoothGradientLookAndFeel.FRAME_BUTTON_STOP_INACTIVE;
                
                // if inactive and pressed
                if (buttonModel.isPressed() && buttonModel.isArmed()) {
                    mainItemColor = darkHighlightColor;
                }
                
            }
            
            // if the button is pressed and the mouse is over it
            else if (buttonModel.isPressed() && buttonModel.isArmed()) {
                mainItemColor = darkHighlightColor;
            }
            
            Graphics2D g2d = (Graphics2D)g;
            g2d.translate(x, y);
            
            Paint _paint = g2d.getPaint();
            // fill the background
            g2d.setPaint(new GradientPaint(0, iconSize, bgStart, 0, 0, bgStop));
            g2d.fillRect(0, 0, iconSize, iconSize);
            
            g2d.setPaint(_paint);
            // paint the image
            g2d.setColor(mainItemColor);
            g2d.drawLine(6, 3, 11, 3);
            g2d.drawLine(5, 4, 12, 4);
            g2d.drawLine(12, 5, 12, 10);
            g2d.fillRect(3, 6, 8, 3);
            g2d.drawLine(3, 9, 3, 12);
            g2d.drawLine(4, 12, 10, 12);
            g2d.drawLine(10, 9, 10, 12);
            
            g.translate(-x, -y);
        }
        
        public int getIconWidth() {
            return iconSize;
        }
        
        public int getIconHeight() {
            return iconSize;
        }
        
    }  // class InternalFrameAltMaximizeIcon
    
    public static Icon getInternalFrameCloseIcon(int size) {
        return new InternalFrameCloseIcon(size);
    }
    
    private static class InternalFrameCloseIcon implements Icon, UIResource, Serializable {
        int iconSize = 16;
        
        public InternalFrameCloseIcon(int size) {
            iconSize = size;
        }
        
        public void paintIcon(Component c, Graphics g, int x, int y) {
            JButton parentButton = (JButton)c;
            ButtonModel buttonModel = parentButton.getModel();
            
            Color mainItemColor = SmoothGradientLookAndFeel.getPrimaryControlDarkShadow();
            Color darkHighlightColor = MetalLookAndFeel.getBlack();
            
            // background gradients
            Color bgStart = SmoothGradientLookAndFeel.FRAME_BUTTON_START_ACTIVE;
            Color bgStop = SmoothGradientLookAndFeel.FRAME_BUTTON_STOP_ACTIVE;
            
            // if the internal frame is inactive
            if (parentButton.getClientProperty("paintActive") != Boolean.TRUE) {
                mainItemColor = SmoothGradientLookAndFeel.getControlDarkShadow();
                bgStart = SmoothGradientLookAndFeel.FRAME_BUTTON_START_INACTIVE;
                bgStop = SmoothGradientLookAndFeel.FRAME_BUTTON_STOP_INACTIVE;
                
                // if inactive and pressed
                if (buttonModel.isPressed() && buttonModel.isArmed())
                    mainItemColor = darkHighlightColor;
                
            }
            
            // if the button is pressed and the mouse is over it
            else if (buttonModel.isPressed() && buttonModel.isArmed()) {
                mainItemColor = darkHighlightColor;
            }
            
            Graphics2D g2d = (Graphics2D)g;
            g2d.translate(x, y);
            
            Paint _paint = g2d.getPaint();
            // fill the background
            g2d.setPaint(new GradientPaint(0, iconSize, bgStart, 0, 0, bgStop));
            g2d.fillRect(0, 0, iconSize, iconSize);
            
            g2d.setPaint(_paint);
            // paint the image
            g2d.setColor(mainItemColor);
            g2d.fillRect(5, 5, 6, 6);
            g2d.fillRect(3, 4, 4, 2);
            g2d.fillRect(4, 3, 2, 4);
            g2d.fillRect(10, 3, 2, 4);
            g2d.fillRect(9, 4, 4, 2);
            g2d.fillRect(4, 9, 2, 4);
            g2d.fillRect(3, 10, 4, 2);
            g2d.fillRect(10, 9, 2, 4);
            g2d.fillRect(9, 10, 4, 2);
            g.translate(-x, -y);
        }
        
        public int getIconWidth() {
            return iconSize;
        }
        
        public int getIconHeight() {
            return iconSize;
        }
        
    }  // class InternalFrameCloseIcon
    
    
    // Helper method utilized by the CheckBoxIcon and the CheckBoxMenuItemIcon.
    private static void drawCheck(Graphics g, int x, int y) {
        g.translate(x, y);
        g.drawLine(3, 5, 3, 5);
        g.fillRect(3, 6, 2, 2);
        g.drawLine(4, 8, 9, 3);
        g.drawLine(5, 8, 9, 4);
        g.drawLine(5, 9, 9, 5);
        g.translate(-x, -y);
    }
    
    
    private static class CheckBoxIcon implements Icon, UIResource, Serializable {
        
        private static final int SIZE = 13;
        
        public int getIconWidth()	{ return SIZE; }
        public int getIconHeight() { return SIZE; }
        
        public void paintIcon(Component c, Graphics g, int x, int y) {
            JCheckBox cb = (JCheckBox) c;
            ButtonModel model = cb.getModel();
            
            if (model.isEnabled()) {
                if (cb.isBorderPaintedFlat()) {
                    g.setColor(SmoothGradientLookAndFeel.getControlDarkShadow());
                    g.drawRect(x, y, SIZE - 2, SIZE - 2);
                    // inside box
                    g.setColor(SmoothGradientLookAndFeel.getControlHighlight());
                    g.fillRect(x+1, y+1, SIZE-3, SIZE-3);
                } else if (model.isPressed() && model.isArmed()) {
                    g.setColor(MetalLookAndFeel.getControlShadow());
                    g.fillRect(x, y, SIZE - 1, SIZE - 1);
                    SmoothGradientUtils.drawPressed3DBorder(g, x, y, SIZE, SIZE);
                } else {
                    SmoothGradientUtils.drawFlush3DBorder(g, x, y, SIZE, SIZE);
                }
                g.setColor(MetalLookAndFeel.getControlInfo());
            } else {
                g.setColor(MetalLookAndFeel.getControlShadow());
                g.drawRect(x, y, SIZE - 2, SIZE - 2);
            }
            
            if (model.isSelected()) {
                drawCheck(g, x, y);
            }
        }
        
    }
    
    
    private static class CheckBoxMenuItemIcon implements Icon, UIResource, Serializable {
        
        private static final int SIZE = 13;
        
        public int getIconWidth()	{ return SIZE; }
        public int getIconHeight() { return SIZE; }
        
        public void paintIcon(Component c, Graphics g, int x, int y) {
            JMenuItem b = (JMenuItem) c;
            if (b.isSelected()) {
                drawCheck(g, x, y + 1);
            }
        }
    }
    
    
    private static class RadioButtonMenuItemIcon implements Icon, UIResource, Serializable {
        
        private static final int SIZE = 13;
        
        public int getIconWidth()	{ return SIZE; }
        public int getIconHeight() { return SIZE; }
        
        public void paintIcon(Component c, Graphics g, int x, int y) {
            JMenuItem b = (JMenuItem) c;
            if (b.isSelected()) {
                drawDot(g, x, y);
            }
        }
        
        private void drawDot(Graphics g, int x, int y) {
            g.translate(x, y);
            g.drawLine(5, 4, 8, 4);
            g.fillRect(4, 5, 6, 4);
            g.drawLine(5, 9, 8, 9);
            g.translate(-x, -y);
        }
    }
    
    
    private static class MenuArrowIcon implements Icon, UIResource, Serializable  {
        
        private static final int WIDTH  = 4;
        private static final int HEIGHT = 8;
        
        public void paintIcon( Component c, Graphics g, int x, int y ) {
            JMenuItem b = (JMenuItem) c;
            
            g.translate( x, y );
            if( SmoothGradientUtils.isLeftToRight(b) ) {
                g.drawLine( 0, 0, 0, 7 );
                g.drawLine( 1, 1, 1, 6 );
                g.drawLine( 2, 2, 2, 5 );
                g.drawLine( 3, 3, 3, 4 );
            } else {
                g.drawLine( 4, 0, 4, 7 );
                g.drawLine( 3, 1, 3, 6 );
                g.drawLine( 2, 2, 2, 5 );
                g.drawLine( 1, 3, 1, 4 );
            }
            g.translate( -x, -y );
        }
        
        public int getIconWidth()	{ return WIDTH; }
        public int getIconHeight() { return HEIGHT; }
        
    }
    
    
    /**
     * The minus sign button icon used in trees
     */
    private static class ExpandedTreeIcon implements Icon, Serializable {
        
        protected static final int SIZE      = 9;
        protected static final int HALF_SIZE = 4;
        
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Color backgroundColor = c.getBackground();
            
            g.setColor(backgroundColor != null ? backgroundColor : Color.white);
            g.fillRect(x, y, SIZE - 1, SIZE - 1);
            g.setColor(Color.gray);
            g.drawRect(x, y, SIZE - 1, SIZE - 1);
            g.setColor(Color.gray);
            g.drawLine(x + 2, y + HALF_SIZE, x + (SIZE - 3), y + HALF_SIZE);
        }
        
        public int getIconWidth()	{ return SIZE; }
        public int getIconHeight() { return SIZE; }
    }
    
    
    /**
     * The plus sign button icon used in trees.
     */
    private static class CollapsedTreeIcon extends ExpandedTreeIcon {
        public void paintIcon(Component c, Graphics g, int x, int y) {
            super.paintIcon(c, g, x, y);
            g.drawLine(x + HALF_SIZE, y + 2, x + HALF_SIZE, y + (SIZE - 3));
        }
    }
    
    
    /**
     * The arrow button used in comboboxes.
     */
    private static class ComboBoxButtonIcon implements Icon, Serializable {
        
        public void paintIcon(Component c, Graphics g, int x, int y) {
            JComponent component = (JComponent)c;
            int iconWidth = getIconWidth();
            
            g.translate(x, y);
            
            g.setColor( component.isEnabled()
            ? MetalLookAndFeel.getControlInfo()
            : MetalLookAndFeel.getControlShadow() );
            g.drawLine( 0, 0, iconWidth - 1, 0 );
            g.drawLine( 1, 1, 1 + (iconWidth - 3), 1 );
            g.drawLine( 2, 2, 2 + (iconWidth - 5), 2 );
            g.drawLine( 3, 3, 3 + (iconWidth - 7), 3 );
            
/*
        int startY = (((h + 1) - arrowHeight) / 2) + arrowHeight - 1;
        int startX = (w / 2);
 
        //	    System.out.println( "startX2 :" + startX + " startY2 :"+startY);
 
        for (int line = 0; line < arrowHeight; line++) {
            g.drawLine(
                startX - line,
                startY - line,
                startX + line + 1,
                startY - line);
        }*/
            g.translate( -x, -y );
        }
        
        public int getIconWidth()  { return 8; }
        public int getIconHeight() { return 4; }
    }
    
    
    // Cached Access to Icons ***********************************************************
    
    private static Icon checkBoxIcon;
    private static Icon checkBoxMenuItemIcon;
    private static Icon radioButtonMenuItemIcon;
    private static Icon menuArrowIcon;
    private static Icon expandedTreeIcon;
    private static Icon collapsedTreeIcon;
    
    
    /**
     * Answers an <code>Icon</code> used for <code>JCheckBox</code>es.
     */
    static Icon getCheckBoxIcon() {
        if (checkBoxIcon == null) {
            checkBoxIcon = new CheckBoxIcon();
        }
        return checkBoxIcon;
    }
    
    
    /**
     * Answers an <code>Icon</code> used for <code>JCheckButtonMenuItem</code>s.
     */
    static Icon getCheckBoxMenuItemIcon() {
        if (checkBoxMenuItemIcon == null) {
            checkBoxMenuItemIcon = new CheckBoxMenuItemIcon();
        }
        return checkBoxMenuItemIcon;
    }
    
    
    /**
     * Answers an <code>Icon</code> used for <code>JRadioButtonMenuItem</code>s.
     */
    static Icon getRadioButtonMenuItemIcon() {
        if (radioButtonMenuItemIcon == null) {
            radioButtonMenuItemIcon = new RadioButtonMenuItemIcon();
        }
        return radioButtonMenuItemIcon;
    }
    
    
    /**
     * Answers an <code>Icon</code> used for arrows in <code>JMenu</code>s.
     */
    static Icon getMenuArrowIcon() {
        if (menuArrowIcon == null) {
            menuArrowIcon = new MenuArrowIcon();
        }
        return menuArrowIcon;
    }
    
    
    /**
     * Answers an <code>Icon</code> used in <code>JTree</code>s.
     */
    static Icon getExpandedTreeIcon() {
        if (expandedTreeIcon == null) {
            expandedTreeIcon = new ExpandedTreeIcon();
        }
        return expandedTreeIcon;
    }
    
    /**
     * Answers an <code>Icon</code> used in <code>JTree</code>s.
     */
    static Icon getCollapsedTreeIcon() {
        if (collapsedTreeIcon == null) {
            collapsedTreeIcon = new CollapsedTreeIcon();
        }
        return collapsedTreeIcon;
    }
    
    /**
     * Answers an <code>Icon</code> used in <code>JComboBox</code>es.
     */
    static Icon getComboBoxButtonIcon() {
        return new ComboBoxButtonIcon();
    }
    
    
}
















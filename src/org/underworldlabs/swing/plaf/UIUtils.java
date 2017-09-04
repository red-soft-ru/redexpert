/*
 * UIUtils.java
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

package org.underworldlabs.swing.plaf;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

import org.underworldlabs.swing.plaf.smoothgradient.SmoothGradientLookAndFeel;

/**
 * User interface utilities. Allows for central determination of
 * colours, icons etc.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class UIUtils {
           
    /** the active colour for text */
    private static Color defaultActiveTextColour;

    /** the active colour for backgrounds */
    private static Color defaultActiveBackgroundColour;

    /** the inactive colour for backgrounds */
    private static Color defaultInactiveBackgroundColour;

    /** True if checked for windows yet. */
    private static boolean checkedWindows;
    
    /** True if running on Windows. */
    private static boolean isWindows;

    /** True if checked for mac yet. */
    private static boolean checkedMac;

    /** True if running on Mac. */
    private static boolean isMac;

    /**
     * Convenience method for consistent border colour.
     *
     * @return the system default border colour
     */
    public static Color getDefaultBorderColour() {
        
        return getColour("executequery.Border.colour", "controlDkShadow");
    }

    public static String asHexColor(String key) {

        return toHexString(UIManager.getColor(key));
    }
    
    public static String toHexString(Color color) {

        return String.format("#%06X", (0xFFFFFF & color.getRGB()));
    }
    
    public static Color getColour(String key, String defaultKey) {
        
        Color color = UIManager.getColor(key);
        if (color != null) {
            
            return color;

        } else {
            
            return UIManager.getColor(defaultKey);       
        }

    }
    
    public static Color getColour(String key, Color defaultColour) {
        
        Color color = UIManager.getColor(key);
        if (color != null) {
            
            return color;
            
        } else {
            
            return defaultColour;       
        }        
        
    }
    
    public static Border getDefaultLineBorder() {
        
        return BorderFactory.createLineBorder(getDefaultBorderColour());
    }
    
    
    /**
     * Returns true if running on Mac.
     */
    public static boolean isMac() {
        if (!checkedMac) {
            String osName = System.getProperty ("os.name");
            if (osName != null && osName.indexOf("Mac") != -1) {
                isMac = true;
            }
            checkedMac = true;
        }
        return isMac;
    }    
    
    public static boolean isNativeMacLookAndFeel() {
        
        if (!isMac()) {
            
            return false;
        }
        
        String laf = UIManager.getLookAndFeel().getClass().getName();
        return (laf.equals(UIManager.getSystemLookAndFeelClassName()));
    }
    
    /**
     * Returns true if running on Windows.
     */
    public static boolean isWindows() {
        if (!checkedWindows) {
            String osName = System.getProperty ("os.name");
            if (osName != null && osName.indexOf("Windows") != -1) {
                isWindows = true;
            }
            checkedWindows = true;
        }
        return isWindows;
    }

    /**
     * Returns whether the current applied look and feel is
     * the SmoothGradientLookAndFeel default look and feel.
     *
     * @return true | false
     */
    public static boolean isDefaultLookAndFeel() {
        return (UIManager.getLookAndFeel() instanceof SmoothGradientLookAndFeel);
                //|| usingOcean();
    }

    /**
     * Returns whether the current applied look and feel is
     * the SmoothGradientLookAndFeel default look and feel.
     *
     * @return true | false
     */
    public static boolean is3DLookAndFeel() {
        LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
        return (lookAndFeel instanceof SmoothGradientLookAndFeel && !(lookAndFeel instanceof UnderworldLabsFlatLookAndFeel));
    }
    
    public static boolean isDarkLookAndFeel() {
        
        LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
        return (lookAndFeel instanceof UnderworldLabsDarkFlatLookAndFeel);
    }
    
    /**
     * Returns whether the current applied look and feel is 
     * an instance of GTK look and feel.
     *
     * @return true | false
     */
    public static boolean isGtkLookAndFeel() {
        return isLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
    }
    
    /**
     * Returns whether the current applied look and feel is
     * the MetalLookAndFeel
     *
     * @return true | false
     */
    public static boolean isMetalLookAndFeel() {
        return UIManager.getLookAndFeel() instanceof MetalLookAndFeel;
    }

    /**
     * Returns whether the current applied look and feel is
     * the MotifLookAndFeel
     *
     * @return true | false
     */
    public static boolean isMotifLookAndFeel() {
        return isLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
    }

    /**
     * Returns whether the current applied look and feel is
     * the WindowsLookAndFeel
     *
     * @return true | false
     */
    public static boolean isWindowsLookAndFeel() {
        return isLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    }

    private static boolean isLookAndFeel(String name) {
        return UIManager.getLookAndFeel().getClass().getName().equals(name);
    }
    
    /**
     * Returns true if we're using the Ocean Theme under the
     * MetalLookAndFeel.
     */
    public static boolean usingOcean() {
        if (isMetalLookAndFeel()) {
            return (MetalLookAndFeel.getCurrentTheme() instanceof OceanTheme);
        }
        return false;
    }

    public static Color getDefaultInactiveBackgroundColour() {
        if (defaultInactiveBackgroundColour == null) {
            defaultInactiveBackgroundColour = UIManager.getColor("control");
        }
        return defaultInactiveBackgroundColour;
    }

    public static Color getInverse(Color colour) {
        int red = 255 - colour.getRed();
        int green = 255 - colour.getGreen();
        int blue = 255 - colour.getBlue();
        return new Color(red, green, blue);
    }
    
    public static Color getDefaultActiveBackgroundColour() {
        if (defaultActiveBackgroundColour == null) {
            if (!isWindowsLookAndFeel()) {
                Color color = UIManager.getColor("activeCaptionBorder");
                if (color == null) {
                    color = UIManager.getColor("controlShadow");
                }
                
                defaultActiveBackgroundColour = getBrighter(color, 0.85);
            
            } else {

                defaultActiveBackgroundColour = 
                        UIManager.getColor("controlLtHighlight");
            }
        }
        return defaultActiveBackgroundColour;        
    }
    
    public static Color getDefaultActiveTextColour() {
        if (defaultActiveTextColour == null) {
            if (!isWindowsLookAndFeel()) {
                defaultActiveTextColour = UIManager.getColor("activeCaptionText");
                if (defaultActiveTextColour == null) {
                    // default to black text
                    defaultActiveTextColour = Color.BLACK;
                }
            }
            else {
                defaultActiveTextColour = UIManager.getColor("controlText");
            }
        }
        return defaultActiveTextColour;
    }
    
    public static Color getDarker(Color color, double factor) {
	return new Color(Math.max((int)(color.getRed()   * factor), 0), 
			 Math.max((int)(color.getGreen() * factor), 0),
			 Math.max((int)(color.getBlue()  * factor), 0));
    }

    public static Color getBrighter(Color color, double factor) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        int i = (int)(1.0/(1.0-factor));
        if ( r == 0 && g == 0 && b == 0) {
           return new Color(i, i, i);
        }
        if ( r > 0 && r < i ) r = i;
        if ( g > 0 && g < i ) g = i;
        if ( b > 0 && b < i ) b = i;

        return new Color(Math.min((int)(r/factor), 255),
                         Math.min((int)(g/factor), 255),
                         Math.min((int)(b/factor), 255));
    }

    // Cached Access to Icons ***********************************************************
    
//    private static Icon checkBoxIcon;
    private static Icon checkBoxMenuItemIcon;
//    private static Icon radioButtonMenuItemIcon;
//    private static Icon menuArrowIcon;
    private static Icon expandedTreeIcon;
    private static Icon collapsedTreeIcon;
    
    
    /**
     * Answers an <code>Icon</code> used for <code>JCheckBox</code>es.
     */
    /*
    static Icon getCheckBoxIcon() {
        if (checkBoxIcon == null) {
            checkBoxIcon = new CheckBoxIcon();
        }
        return checkBoxIcon;
    }
    */
    
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
    /*
    static Icon getRadioButtonMenuItemIcon() {
        if (radioButtonMenuItemIcon == null) {
            radioButtonMenuItemIcon = new RadioButtonMenuItemIcon();
        }
        return radioButtonMenuItemIcon;
    }
    */
    
    /**
     * Answers an <code>Icon</code> used for arrows in <code>JMenu</code>s.
     */
    /*
    static Icon getMenuArrowIcon() {
        if (menuArrowIcon == null) {
            menuArrowIcon = new MenuArrowIcon();
        }
        return menuArrowIcon;
    }
    */
    
    /**
     * Answers an <code>Icon</code> used in <code>JTree</code>s.
     */
    public static Icon getExpandedTreeIcon() {
        if (expandedTreeIcon == null) {
            expandedTreeIcon = new ExpandedTreeIcon();
        }
        return expandedTreeIcon;
    }
    
    /**
     * Answers an <code>Icon</code> used in <code>JTree</code>s.
     */
    public static Icon getCollapsedTreeIcon() {
        if (collapsedTreeIcon == null) {
            collapsedTreeIcon = new CollapsedTreeIcon();
        }
        return collapsedTreeIcon;
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
            g.setColor(Color.black);
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

    public static void antialias(Graphics g) {
    	Graphics2D g2d = (Graphics2D)g;
    	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    	g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }
    
    private UIUtils() {}
}





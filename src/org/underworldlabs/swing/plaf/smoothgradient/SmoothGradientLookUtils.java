/*
 * SmoothGradientLookUtils.java
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
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.AbstractButton;
import javax.swing.UIManager;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.UIResource;

/**
 *
 * @author   Takis Diakoumis
 */
public final class SmoothGradientLookUtils {

    // Properties and Keys for Internal Use Only - May Change without Notice 

    public static final boolean IS_BEFORE_14    = isBefore14();
    public static final boolean IS_140          = is140();
    public static final boolean IS_142_OR_LATER = is142orLater();
    public static final boolean HAS_XP_LAF      = IS_142_OR_LATER;

    public static boolean isLowRes = isLowResolution();
    
    private static boolean loggingEnabled;

    // Override default constructor;
    private SmoothGradientLookUtils() {}

    /**
     * Checks and answers whether we have a true color system.
     */
    public static boolean isTrueColor(Component c) {
        return c.getToolkit().getColorModel().getPixelSize() >= 24;
    }
    
    /**
     * Tries to look up the System property for the given key.
     * In untrusted environments this may throw a SecurityException.
     * In this case, we catch the exception and answer an empty string. 
     * 
     * @param key   the name of the system property
     * @return the system property's String value, or null if there's
     *     no such value, or an empty String when
     *     a SecurityException has been catched
     */
    public static String getSystemProperty(String key) {
        try {
            return System.getProperty(key);
        } catch (SecurityException e) {
            log("Can't read the System property " + key + ".");
            return "";
        }
    }

    /**
     * Tries to look up the System property for the given key.
     * In untrusted environments this may throw a SecurityException.
     * In this case, we catch the exception and answer an empty string. 
     * 
     * @param key          the name of the system property
     * @param defaultValue the default value if no property exists.
     * @return the system property's String value, or the defaultValue 
     *     if there's no such value, or an empty String when
     *     a SecurityException has been catched
     */
    public static String getSystemProperty(String key, String defaultValue) {
        try {
            return System.getProperty(key, defaultValue);
        } catch (SecurityException e) {
            log("Can't read the System property " + key + ".");
            return "";
        }
    }

    /**
     * Checks and answers whether we're on Windows.
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    /**
     * Checks and answers if we are on Windows 95 or NT.
     */
    public static boolean isClassicWindows() {
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        return osName.startsWith("Windows") && osVersion.startsWith("4.0");
    }

    /**
     * Checks and answers if we are on Windows 98/ME/2000/XP.
     */
    public static boolean isModernWindows() {
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        return osName.startsWith("Windows") && !osVersion.startsWith("4.0");
    }

    /**
     * Checks and answers if we are on Windows XP.
     */
    public static boolean isWindowsXP() {
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        return osName.startsWith("Windows") && osVersion.startsWith("5.1");
    }

    // Working with Button Margins ******************************************

    /**
     * Installs a narrow margin, if property <code>isNarrow</code> has been set.
     */
    public static void installNarrowMargin(
        AbstractButton b,
        String propertyPrefix) {
        Object value = b.getClientProperty(Options.IS_NARROW_KEY);
        boolean isNarrow = Boolean.TRUE.equals(value);
        String defaultsKey =
            propertyPrefix + (isNarrow ? "narrowMargin" : "margin");
        Insets insets = b.getMargin();
        if (insets == null || insets instanceof UIResource) {
            b.setMargin(UIManager.getInsets(defaultsKey));
        }
    }

    /**
     * Creates and answers the margin used by <code>JButton</code>
     * and <code>JToggleButton</code>. Honors the screen resolution 
     * and the global <code>isNarrowButtonsEnabled</code> property.<p>
     * 
     * Sun's L&F implementations use wide button margins.
     * @see Options#getUseNarrowButtons()
     */
    public static Insets createButtonMargin(boolean narrow) {
        int pad = 4;//narrow || Options.getUseNarrowButtons() ? 4 : 14;
        return isLowRes
            ? (IS_BEFORE_14
                ? new InsetsUIResource(0, pad, 1, pad)
                : new InsetsUIResource(2, pad, 1, pad))
            : (IS_BEFORE_14
                ? new InsetsUIResource(2, pad, 2, pad)
                : new InsetsUIResource(3, pad, 3, pad));
    }

    // Color Modifications **************************************************

    /**
     * Computes and answers a <code>Color</code> that is slightly brighter
     * than the specified <code>Color</code>. Required for 1.3 only.
     * 
     * @param color   the color used as basis for the brightened color
     * @return a slightly brighter color
     */
    public static Color getSlightlyBrighter(Color color) {
        return getSlightlyBrighter(color, 1.1f);
    }

    /**
     * Computes and answers a <code>Color</code> that is slightly brighter
     * than the specified <code>Color</code>. Required for 1.3 only.
     * 
     * @param color   the color used as basis for the brightened color
     * @param factor  the factor used to compute the brightness
     * @return a slightly brighter color
     */
    public static Color getSlightlyBrighter(Color color, float factor) {
        float[] hsbValues = new float[3];
        Color.RGBtoHSB(
            color.getRed(),
            color.getGreen(),
            color.getBlue(),
            hsbValues);
        float hue = hsbValues[0];
        float saturation = hsbValues[1];
        float brightness = hsbValues[2];
        float newBrightness = Math.min(brightness * factor, 1.0f);
        return Color.getHSBColor(hue, saturation, newBrightness);
    }

    // Minimal logging ******************************************************

    /**
     * Enables or disables the logging.
     */
    public static void setLoggingEnabled(boolean enabled) {
        loggingEnabled = enabled;
    }

    /**
     * Prints a new line to the console if logging is enabled.
     */
    public static void log() {
        if (loggingEnabled) {
            System.out.println();
        }
    }

    /**
     * Prints the given message to the console if logging is enabled.
     * 
     * @param message  the message to print
     */
    public static void log(String message) {
        if (loggingEnabled) {
            System.out.println("JGoodies Looks: " + message);
        }
    }

    // Private Helper Methods ***********************************************

    /**
     * Checks and answers if we are on a 1.2 or 1.3 runtime environment.
     */
    private static boolean isBefore14() {
        String version = System.getProperty("java.version");
        return version.startsWith("1.2") || version.startsWith("1.3");
    }

    /**
     * Checks and answers if we are on a J2SE 1.4.2 or later.
     */
    private static boolean is142orLater() {
        String version = System.getProperty("java.version");
        return !version.startsWith("1.2") 
             && !version.startsWith("1.3")
             && !version.startsWith("1.4.0")
             && !version.startsWith("1.4.1");
    }

    /**
     * Checks and answers whether this is a J2RE 1.4.0x
     */
    private static boolean is140() {
        return System.getProperty("java.version").startsWith("1.4.0");
    }

    /**
     * Checks and answers whether the screen resolution is low or high.
     */
    private static boolean isLowResolution() {
        return Toolkit.getDefaultToolkit().getScreenResolution() < 120;
    }


}
















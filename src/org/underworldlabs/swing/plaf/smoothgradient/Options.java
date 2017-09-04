/*
 * Options.java
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

import java.awt.Dimension;
//import java.util.HashMap;
//import java.util.Map;

import javax.swing.UIManager;

//import com.jgoodies.clearlook.ClearLookMode;

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 * Provides access to several optional properties for the 
 * JGoodies L&amp;Fs, either by a key to the <code>UIDefaults</code> table
 * or via a method or both.
 * 
 * @author  Karsten Lentzsch
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */

public final class Options {

    // Look & Feel Names ****************************************************
/*
    public static final String EXT_MOTIF_NAME =
        "com.jgoodies.plaf.motif.ExtMotifLookAndFeel";
        
    public static final String PLASTIC_NAME =
        "com.jgoodies.plaf.plastic.PlasticLookAndFeel";
        
    public static final String PLASTIC3D_NAME =
        "com.jgoodies.plaf.plastic.Plastic3DLookAndFeel";
        
    public static final String PLASTICXP_NAME =
        "com.jgoodies.plaf.plastic.PlasticXPLookAndFeel";
        
    public static final String EXT_WINDOWS_NAME =
        "com.jgoodies.plaf.windows.ExtWindowsLookAndFeel";
        
    public static final String DEFAULT_LOOK_NAME = 
        PLASTIC3D_NAME;
*/
    /**
     * Holds a <code>Map</code> that enables the look&amp;feel replacement
     * mechanism to replace one look by another. 
     * Maps the original class names to their replacement class names.
     */ /*
    private static final Map LAF_REPLACEMENTS;
    static {
        LAF_REPLACEMENTS = new HashMap();
        initializeDefaultReplacements();
    }
    */

    // Keys for Overriding Font Settings ************************************

/*    public static final String MENU_FONT_KEY = 
        "jgoodies.menuFont";
        
    public static final String CONTROL_FONT_KEY = 
        "jgoodies.controlFont";
*/        
    public static final String FONT_SIZE_HINTS_KEY = 
        "jgoodies.fontSizeHints";
        
    public static final String USE_SYSTEM_FONTS_KEY =
        "swing.useSystemFontSettings";
        
    public static final String USE_SYSTEM_FONTS_APP_KEY =
        "Application.useSystemFontSettings";


    // Optional Global User Properties **************************************

    public static final String DEFAULT_ICON_SIZE_KEY =
        "jgoodies.defaultIconSize";
/*        
    public static final String USE_NARROW_BUTTONS_KEY =
        "jgoodies.useNarrowButtons";
        
    public static final String TAB_ICONS_ENABLED_KEY =
        "jgoodies.tabIconsEnabled";
        

    // ClearLook Properties *************************************************

    public static final String CLEAR_LOOK_MODE_KEY = 
        "ClearLook.mode";
        
    public static final String CLEAR_LOOK_POLICY_KEY = 
        "ClearLook.policy";
        
    public static final String CLEAR_LOOK_OFF = 
        ClearLookMode.OFF.getName();
        
    public static final String CLEAR_LOOK_ON = 
        ClearLookMode.ON.getName();
        
    public static final String CLEAR_LOOK_VERBOSE =
        ClearLookMode.VERBOSE.getName();
        
    public static final String CLEAR_LOOK_DEBUG = 
        ClearLookMode.DEBUG.getName();
*/

    // Optional Client Properties *******************************************

    /** 
     * Hint that the button margin should be narrow.                   
     */
    public static final String IS_NARROW_KEY = "jgoodies.isNarrow";

    /** 
     * Hint that the scroll pane border should be etched.				
     */
    public static final String IS_ETCHED_KEY = "jgoodies.isEtched";

    /** 
     * Hint for the style: Single or Both, see <code>HeaderStyle</code>.
     */
    public static final String HEADER_STYLE_KEY = "jgoodies.headerStyle";

    /** 
     * Hint that the menu items in the menu have no icons.				
     */
    public static final String NO_ICONS_KEY = "jgoodies.noIcons";

    /** 
     * A client property key for <code>JTree</code>s.
     * Used with the angled and none style values.
     */
    public static final String TREE_LINE_STYLE_KEY = 
        "JTree.lineStyle";

    /** 
     * A client property value for <code>JTree</code>s
     * that indicates that lines shall be drawn.
     */
    public static final String TREE_LINE_STYLE_ANGLED_VALUE = 
        "Angled";

    /** 
     * A client property value for <code>JTree</code>s
     * that indicates that lines shall be hidden.
     */
    public static final String TREE_LINE_STYLE_NONE_VALUE   = 
        "None";

    /** 
     * A client property key for <code>JTabbedPane</code>s that indicates 
     * that no content border shall be painted. 
     * Supported by the Plastic look and feel family.
     * This effect will be achieved also if the EMBEDDED property is true.
     */
    public static final String NO_CONTENT_BORDER_KEY =
        "jgoodies.noContentBorder";

    /**
     * A client property key for <code>JTabbedPane</code>s that indicates
     * that tabs are painted with a special embedded appearance. 
     * Supported by the Plastic look and feel family.
     * This effect will be achieved also if the EMBEDDED property is true.
     */
    public static final String EMBEDDED_TABS_KEY = 
        "jgoodies.embeddedTabs";


    // Private ****************************************************************

    private static final Dimension DEFAULT_ICON_SIZE = 
        new Dimension(20, 20);

    // Override default constructor;
    private Options() {
    }


    // Accessing Options ******************************************************

    /**
     * Answers whether a hint is set in the <code>UIManager</code> 
     * that indicates, that a look&amp;feel may use the native system fonts.
     */
    public static boolean getUseSystemFonts() {
        return UIManager.get(USE_SYSTEM_FONTS_APP_KEY).equals(Boolean.TRUE);
    }

    /**
     * Sets a value in the <code>UIManager</code> to indicate, 
     * that a look&amp;feel may use the native system fonts.
     */
    public static void setUseSystemFonts(boolean useSystemFonts) {
        UIManager.put(USE_SYSTEM_FONTS_APP_KEY, Boolean.valueOf(useSystemFonts));
    }

    /**
     * Answers the default icon size.
     */
    public static Dimension getDefaultIconSize() {
        Dimension size = UIManager.getDimension(DEFAULT_ICON_SIZE_KEY);
        return size == null ? DEFAULT_ICON_SIZE : size;
    }

    /**
     * Sets the default icon size.
     */
    public static void setDefaultIconSize(Dimension defaultIconSize) {
        UIManager.put(DEFAULT_ICON_SIZE_KEY, defaultIconSize);
    }

    /**
     * Answers the global <code>FontSizeHints</code>, can be overriden 
     * by look specific setting.
     */
    public static FontSizeHints getGlobalFontSizeHints() {
        Object value = UIManager.get(FONT_SIZE_HINTS_KEY);
        if (value != null)
            return (FontSizeHints) value;

        String name = SmoothGradientLookUtils.getSystemProperty(FONT_SIZE_HINTS_KEY, "");
        try {
            return FontSizeHints.valueOf(name);
        } catch (IllegalArgumentException e) {
            return FontSizeHints.DEFAULT;
        }
    }

    /**
     * Sets the global <code>FontSizeHints</code>.
     */
    public static void setGlobalFontSizeHints(FontSizeHints hints) {
        UIManager.put(FONT_SIZE_HINTS_KEY, hints);
    }

    /**
     * Checks and answers if we shall use narrow button margins of 4 pixels.
     * Sun's L&F implementations use a much wider button margin of 14 pixels, 
     * which leads to good button minimum width in the typical case.<p>
     * 
     * Using narrow button margins can potentially cause compatibility issues, 
     * so this feature must be switched on programmatically.<p>
     * 
     * If you use narrow margin, you should take care of minimum button width,
     * either by the layout management or appropriate ButtonUI minimum widths.
     */ /*
    public static boolean getUseNarrowButtons() {
        Object value = UIManager.get(USE_NARROW_BUTTONS_KEY);
        return Boolean.TRUE.equals(value);
    }
*/
    /**
     * Sets if we use narrow or standard button margins.
     * 
     * @see #getUseNarrowButtons
     */ /*
    public static void setUseNarrowButtons(boolean b) {
        UIManager.put(USE_NARROW_BUTTONS_KEY, Boolean.valueOf(b));
    }
*/
    /**
     * Detects and answers if we shall use icons in <code>JTabbedPanes</code>.
     * This has an effect only inside NetBeans, it will answer 'yes'
     * if we are outside NetBeans.<p>
     * 
     * If the user has set a system property, we log a message 
     * about the choosen style.
     */ /*
    public static boolean isTabIconsEnabled() {
//        if (!LookUtils.IS_NETBEANS)
//            return true;

        String userMode = LookUtils.getSystemProperty(TAB_ICONS_ENABLED_KEY, "");
        boolean overridden = userMode.length() > 0;
        Object value =
            overridden ? userMode : UIManager.get(TAB_ICONS_ENABLED_KEY);

        boolean result =
            overridden
                ? userMode.equalsIgnoreCase("true")
                : value instanceof Boolean
                && Boolean.TRUE.equals(value);

        if (overridden) {
            LookUtils.log(
                "You have "
                    + (result ? "en" : "dis")
                    + "abled icons in tabbed panes.");
        }
        return result;
    }
*/
    /**
     * Enables or disables the use of icons in <code>JTabbedPane</code>s.
     * 
     * @see #isTabIconsEnabled
     */ /*
    public static void setTabIconsEnabled(boolean b) {
        UIManager.put(TAB_ICONS_ENABLED_KEY, Boolean.valueOf(b));
    }
*/
    // Look And Feel Replacements *******************************************

    /**
     * Puts a replacement name for a given <code>LookAndFeel</code> 
     * class name in the list of all look and feel replacements.
     */ /*
    public static void putLookAndFeelReplacement(
        String original,
        String replacement) {
        LAF_REPLACEMENTS.put(original, replacement);
    }
*/
    /**
     * Removes a replacement name for a given <code>LookAndFeel</code> 
     * class name from the list of all look and feel replacements.
     */ /*
    public static void removeLookAndFeelReplacement(String original) {
        LAF_REPLACEMENTS.remove(original);
    }
*/
    /**
     * Initializes some default class name replacements, that replace
     * Sun's Java look and feel, and Sun's Windows look and feel by
     * the appropriate JGoodies replacements.
     */ /*
    public static void initializeDefaultReplacements() {
        putLookAndFeelReplacement(
            "javax.swing.plaf.metal.MetalLookAndFeel",
            PLASTIC3D_NAME);
        putLookAndFeelReplacement(
            "com.sun.java.swing.plaf.windows.WindowsLookAndFeel",
            EXT_WINDOWS_NAME);
        putLookAndFeelReplacement(
            "com.sun.java.swing.plaf.motif.MotifLookAndFeel",
            EXT_MOTIF_NAME);
    }
*/
    /**
     * Answers the class name that can be used to replace the specified
     * <code>LookAndFeel</code> class name.
     */ /*
    public static String getReplacementClassNameFor(String className) {
        String replacement = (String) LAF_REPLACEMENTS.get(className);
        return replacement == null ? className : replacement;
    }
*/
    /**
     * Answers the class name for a cross-platform <code>LookAndFeel</code>.
     */ /*
    public static String getCrossPlatformLookAndFeelClassName() {
        return PLASTIC3D_NAME;
    }
*/
    /**
     * Answers the class name for a system specific <code>LookAndFeel</code>.
     */ /*
    public static String getSystemLookAndFeelClassName() {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows"))
            return Options.EXT_WINDOWS_NAME;
        else if (osName.startsWith("Mac"))
            return UIManager.getSystemLookAndFeelClassName();
        else
            return getCrossPlatformLookAndFeelClassName();
    }
*/
}
















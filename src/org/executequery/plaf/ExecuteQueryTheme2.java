/*
 * ExecuteQueryTheme2.java
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

package org.executequery.plaf;

import org.underworldlabs.swing.plaf.UIUtils;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.OceanTheme;
import java.awt.*;
import java.util.Arrays;

/**
 * Simple theme applied to the Java Metal look and feel.
 *
 * @author Takis Diakoumis
 */
public class ExecuteQueryTheme2 extends OceanTheme {

    // ---------------------------------
    // --------- System Fonts ----------
    // ---------------------------------
    private FontUIResource menuFont;
    private FontUIResource controlFont;
    private FontUIResource systemFont;
    private FontUIResource userFont;
    private FontUIResource smallFont;

    // --------------------------------------------
    // ------ Primary and Secondary Colours -------
    // --------------------------------------------
    // ********************************************
    // --------------------------------------------
    // ------------ Java Look and Feel ------------
    // --------------------------------------------
    // -------- primary 1:     102,102,153 --------
    // -------- primary 2:     153,153,204 --------
    // -------- primary 3:     204,204,255 --------
    // -------- secondary 1:   102,102,102 --------
    // -------- secondary 2:   153,153,153 --------
    // -------- secondary 3:   204,204,204 --------
    // --------------------------------------------
    // ********************************************

    // --------------------------------------------
    // ------------ Execute Query Theme -----------
    // --------------------------------------------

    // --- active internal frame borders ---
    private final ColorUIResource primary1 = new ColorUIResource(102, 102, 153);

    // --- scroll bars, highlights, menu selection etc ---
    private final ColorUIResource primary2 = new ColorUIResource(145, 145, 207);

    // --- active internal frame headers ---
    private final ColorUIResource primary3 = new ColorUIResource(169, 169, 242);

    // --- dark border for 3D for eg buttons ---
    private final ColorUIResource secondary1 = new ColorUIResource(102, 102, 102);

    // --- inactive internal frame borders, dimmed button borders ---
    private final ColorUIResource secondary2 = new ColorUIResource(153, 153, 153);

    // --- panel/frame, normal background ---
    private final ColorUIResource secondary3 = new ColorUIResource(240, 240, 240);

    private static final ColorUIResource CONTROL_TEXT_COLOR = new ColorUIResource(Color.BLACK);

    private static final ColorUIResource THEME_BLACK = new ColorUIResource(Color.BLACK);

    public ExecuteQueryTheme2() {
        // ------------------------------
        // add some further l&f defaults
        // ------------------------------

        ColorUIResource black = new ColorUIResource(0, 0, 0);

        // black text for labels
        UIManager.put("Label.foreground", black);
        // black text for title border
        UIManager.put("TitledBorder.titleColor", black);
        // toggle button selected colour to primary3
        UIManager.put("ToggleButton.select", primary3);


    }

    public static final Color BRIGHTEN_START = new Color(255, 255, 255, 0);
    public static final Color BRIGHTEN_STOP = new Color(255, 255, 255, 128);
    public static final Color LT_BRIGHTEN_STOP = new Color(255, 255, 255, 64);

    public static final Color DARKEN_START = new Color(0, 0, 0, 0);
    public static final Color DARKEN_STOP = new Color(0, 0, 0, 64);
    public static final Color LT_DARKEN_STOP = new Color(0, 0, 0, 32);

    /**
     * Add this theme's custom entries to the defaults table.
     *
     * @param table the defaults table, non-null
     * @throws NullPointerException if the parameter is null
     */
    public void addCustomEntriesToTable(UIDefaults table) {
        super.addCustomEntriesToTable(table);

        int treeFontSize = table.getFont("Tree.font").getSize();
        Integer rowHeight = Integer.valueOf(treeFontSize + 5);
        Object treeExpandedIcon = UIUtils.getExpandedTreeIcon();
        Object treeCollapsedIcon = UIUtils.getCollapsedTreeIcon();
        ColorUIResource gray = new ColorUIResource(Color.gray);

//        Color dadada = new ColorUIResource(0xDADADA);
        Color cccccc = new ColorUIResource(0xCCCCCC);

        java.util.List buttonGradient = Arrays.asList(
                //new Object[] {new Float(.15f), new Float(0.05f),
                new Object[]{new Float(.15f), new Float(-2.0f),
                        new ColorUIResource(BRIGHTEN_START),
                        new ColorUIResource(BRIGHTEN_STOP),
                        getSecondary3()});

        buttonGradient = Arrays.asList(
                new Object[]{new Float(0.15f), new Float(-2.0f),
                        new ColorUIResource(Color.BLUE),
                        new ColorUIResource(Color.RED), getSecondary3()});

/*
        java.util.List buttonGradient = Arrays.asList(
                 new Object[] {new Float(0.15f), new Float(-.0f),
                 new ColorUIResource(0xF0F0F0), getWhite(), getSecondary3() });
*/

        java.util.List scrollGradient = Arrays.asList(
                new Object[]{new Float(0.005f), new Float(-2.5f),
                        new ColorUIResource(BRIGHTEN_START),
                        new ColorUIResource(BRIGHTEN_STOP),
                        getPrimary1()});

        scrollGradient = Arrays.asList(
                new Object[]{new Float(0.15f), new Float(-1.5f),
                        getWhite(),
                        new ColorUIResource(LT_BRIGHTEN_STOP),
                        getPrimary1().brighter()});

        Object[] defaults = new Object[]{
                "Button.gradient", buttonGradient,

                "ScrollBar.gradient", scrollGradient,
                "ScrollBar.darkShadow", cccccc,

                "Button.rollover", Boolean.FALSE,
                //"ButtonUI", "org.executequery.plaf.smoothgradient.SmoothGradientButtonUI",

                //"ScrollBarUI", "org.executequery.plaf.smoothgradient.SmoothGradientScrollBarUI",

                "CheckBox.gradient", null,
                "CheckBoxMenuItem.gradient", null,
                "RadioButton.gradient", null,
                "RadioButtonMenuItem.gradient", null,

                //"ComboBox.selectionBackground", secondary3,

                "TabbedPane.borderHightlightColor", secondary1.brighter(),
                "TabbedPane.contentAreaColor", secondary3,
                "TabbedPane.contentBorderInsets", new Insets(2, 2, 3, 3),
                "TabbedPane.selected", secondary3,
                "TabbedPane.tabAreaBackground", secondary3,
                "TabbedPane.tabAreaInsets", new Insets(4, 2, 0, 6),
                "TabbedPane.unselectedBackground", cccccc,

                "Tree.expandedIcon", treeExpandedIcon,
                "Tree.collapsedIcon", treeCollapsedIcon,
                "Tree.line", gray,
                "Tree.hash", gray,
                "Tree.rowHeight", rowHeight,
/*
            "Menu.opaque", Boolean.FALSE,
            "MenuBar.gradient", Arrays.asList(new Object[] {
                     new Float(1f), new Float(0f),
                     getWhite(), dadada, 
                     new ColorUIResource(dadada) }),
            "MenuBar.borderColor", cccccc,
            "MenuBarUI", "javax.swing.plaf.metal.MetalMenuBarUI"           
*/
        };

        table.putDefaults(defaults);

    }

    public String getName() {
        return "RedXpert";
    }

    private static final int DEFAULT_FONT_SIZE = 11;

    public FontUIResource getControlTextFont() {

        if (controlFont == null) {

            try {
                controlFont = new FontUIResource(
                        Font.getFont("swing.plaf.metal.controlFont",
                                new Font("Dialog", Font.PLAIN, DEFAULT_FONT_SIZE)));
            } catch (Exception e) {
                controlFont = new FontUIResource("Dialog", Font.BOLD, DEFAULT_FONT_SIZE);
            }

        }

        return controlFont;

    }

    public FontUIResource getSystemTextFont() {

        if (systemFont == null) {

            try {
                systemFont = new FontUIResource(
                        Font.getFont("swing.plaf.metal.systemFont",
                                new Font("Dialog", Font.PLAIN, DEFAULT_FONT_SIZE)));
            } catch (Exception e) {
                systemFont = new FontUIResource("Dialog", Font.PLAIN, DEFAULT_FONT_SIZE);
            }

        }

        return systemFont;

    }

    public FontUIResource getUserTextFont() {

        if (userFont == null) {

            try {
                userFont = new FontUIResource(
                        Font.getFont("swing.plaf.metal.userFont",
                                new Font("Dialog", Font.PLAIN, DEFAULT_FONT_SIZE)));
            } catch (Exception e) {
                userFont = new FontUIResource("Dialog", Font.PLAIN, DEFAULT_FONT_SIZE);
            }

        }

        return userFont;
    }

    public FontUIResource getMenuTextFont() {

        if (menuFont == null) {

            try {
                menuFont = new FontUIResource(
                        Font.getFont("swing.plaf.metal.menuFont",
                                new Font("Dialog", Font.PLAIN, DEFAULT_FONT_SIZE)));
            } catch (Exception e) {
                menuFont = new FontUIResource("Dialog", Font.PLAIN, DEFAULT_FONT_SIZE);
            }

        }

        return menuFont;

    }

    public FontUIResource getWindowTitleFont() {

        if (controlFont == null) {

            try {
                controlFont = new FontUIResource(
                        Font.getFont("swing.plaf.metal.controlFont",
                                new Font("Dialog", Font.PLAIN, DEFAULT_FONT_SIZE)));
            } catch (Exception e) {
                controlFont = new FontUIResource("Dialog", Font.BOLD, DEFAULT_FONT_SIZE);
            }

        }

        return controlFont;

    }

    public FontUIResource getSubTextFont() {

        if (smallFont == null) {

            try {
                smallFont = new FontUIResource(Font.getFont("swing.plaf.metal.smallFont",
                        new Font("Dialog", Font.PLAIN, 10)));
            } catch (Exception e) {
                smallFont = new FontUIResource("Dialog", Font.PLAIN, 10);
            }

        }

        return smallFont;

    }

    protected ColorUIResource getPrimary1() {
        return primary1;
    }

    protected ColorUIResource getPrimary2() {
        return primary2;
    }

    protected ColorUIResource getPrimary3() {
        return primary3;
    }

    protected ColorUIResource getSecondary1() {
        return secondary1;
    }

    protected ColorUIResource getSecondary2() {
        return secondary2;
    }

    protected ColorUIResource getSecondary3() {
        return secondary3;
    }

    /**
     * Return the color that the EQ Look and Feel should use as the default
     * color for controls. The Look and Feel will use this color
     * in painting as it sees fit.
     *
     * @return the "Control Text" color.
     */
    public ColorUIResource getControlTextColor() {
        return CONTROL_TEXT_COLOR;
    }

    /**
     * Return the color that the EQ Look and Feel should use
     * as "Black". The Look and Feel will use this color
     * in painting as it sees fit. This color does not necessarily
     * synch up with the typical concept of black, nor is
     * it necessarily used for all black items.
     *
     * @return the "Black" color.
     */
    protected ColorUIResource getBlack() {
        return THEME_BLACK;
    }

}
















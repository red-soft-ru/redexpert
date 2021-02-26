/*
 * ExecuteQueryTheme.java
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

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import java.awt.*;
import java.util.Arrays;

/**
 * <p>Simple theme applied to the Java Metal look and feel.
 *
 * @author Takis Diakoumis
 */
public class ExecuteQueryTheme extends DefaultMetalTheme {

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
//    private final ColorUIResource primary2 = new ColorUIResource(145, 145, 207);
    private final ColorUIResource primary2 = new ColorUIResource(107, 148, 200);

    // --- active internal frame headers ---
    private final ColorUIResource primary3 = new ColorUIResource(139, 175, 220);

    // --- dark border for 3D for eg buttons ---
    private final ColorUIResource secondary1 = new ColorUIResource(102, 102, 102);

    // --- inactive internal frame borders, dimmed button borders ---
    private final ColorUIResource secondary2 = new ColorUIResource(153, 153, 153);

    // --- panel/frame, normal background ---
    private final ColorUIResource secondary3 = new ColorUIResource(240, 240, 240);

    public ExecuteQueryTheme() {
        // ------------------------------
        // add some further l&f settings
        // ------------------------------

        // black text for labels
        UIManager.put("Label.foreground", new ColorUIResource(0, 0, 0));
        // black text for title border
        UIManager.put("TitledBorder.titleColor", new ColorUIResource(0, 0, 0));
        // toggle button selected colour to primary3
        UIManager.put("ToggleButton.select", primary3);

    }

    /**
     * Add this theme's custom entries to the defaults table.
     *
     * @param table the defaults table, non-null
     * @throws NullPointerException if the parameter is null
     */
    public void addCustomEntriesToTable(UIDefaults table) {
        super.addCustomEntriesToTable(table);

        Color dadada = new ColorUIResource(0xDADADA);
        Color cccccc = new ColorUIResource(0xCCCCCC);

        Object[] defaults = new Object[]{

                "TabbedPane.borderHightlightColor", secondary1,
                "TabbedPane.contentAreaColor", secondary3,
                "TabbedPane.contentBorderInsets", new Insets(2, 2, 3, 3),
                "TabbedPane.selected", secondary3,
                "TabbedPane.tabAreaBackground", secondary3,
                "TabbedPane.tabAreaInsets", new Insets(4, 2, 0, 6),
                "TabbedPane.unselectedBackground", cccccc,

                "Menu.opaque", Boolean.FALSE,
                "MenuBar.gradient", Arrays.asList(new Object[]{
                new Float(1f), new Float(0f),
                getWhite(), dadada,
                new ColorUIResource(dadada)}),
                "MenuBar.borderColor", cccccc,
                "MenuBarUI", "javax.swing.plaf.metal.MetalMenuBarUI"

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

}








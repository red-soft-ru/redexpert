/*
 * PlainMetalTheme.java
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
import java.awt.Font;
import java.awt.Insets;

import javax.swing.UIDefaults;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;

public class PlainMetalTheme extends DefaultMetalTheme {

    // ---------------------------------
    // --------- System Fonts ----------
    // ---------------------------------
    private FontUIResource menuFont;
    private FontUIResource controlFont;
    private FontUIResource systemFont;
    private FontUIResource userFont;
    private FontUIResource smallFont;

    /**
     * Add this theme's custom entries to the defaults table.
     *
     * @param table the defaults table, non-null
     * @throws NullPointerException if the parameter is null
     */
    public void addCustomEntriesToTable(UIDefaults table) {
        super.addCustomEntriesToTable(table);

        Color cccccc = new ColorUIResource(0xCCCCCC);

        Object[] defaults = new Object[] {
            
            "TabbedPane.borderHightlightColor", getSecondary1(),
            "TabbedPane.contentAreaColor", getSecondary3(),
            "TabbedPane.contentBorderInsets", new Insets(2, 2, 3, 3),
            "TabbedPane.selected", getSecondary3(),
            "TabbedPane.tabAreaBackground", getSecondary3(),
            "TabbedPane.tabAreaInsets", new Insets(4, 2, 0, 6),
            "TabbedPane.unselectedBackground", cccccc,
            "MenuBarUI", "javax.swing.plaf.metal.MetalMenuBarUI"

        };
        table.putDefaults(defaults);
    }

    private static final int DEFAULT_FONT_SIZE = 12;

    public int getDefaultFontSize() {
        return DEFAULT_FONT_SIZE;
    }
    
    public FontUIResource getControlTextFont() {
        
        if (controlFont == null) {
            
            try {
                controlFont = new FontUIResource(
                                    Font.getFont("swing.plaf.metal.controlFont",
                                    new Font("Dialog", Font.PLAIN, getDefaultFontSize())));
            }
            catch (Exception e) {
                controlFont = new FontUIResource("Dialog", Font.BOLD, getDefaultFontSize());
            } 
            
        } 
        
        return controlFont;
        
    }
    
    public FontUIResource getSystemTextFont() {
        
        if (systemFont == null) {
            
            try {
                systemFont = new FontUIResource(
                                    Font.getFont("swing.plaf.metal.systemFont",
                                    new Font("Dialog", Font.PLAIN, getDefaultFontSize())));
            }
            catch (Exception e) {
                systemFont =  new FontUIResource("Dialog", Font.PLAIN, getDefaultFontSize());
            } 
            
        } 

        return systemFont;
        
    }
    
    public FontUIResource getUserTextFont() {
        
        if (userFont == null) {
            
            try {
                userFont = new FontUIResource(
                                    Font.getFont("swing.plaf.metal.userFont",
                                    new Font("Dialog", Font.PLAIN, getDefaultFontSize())));
            }             
            catch (Exception e) {
                userFont =  new FontUIResource("Dialog", Font.PLAIN, getDefaultFontSize());
            } 
            
        } 
        
        return userFont;
    }
    
    public FontUIResource getMenuTextFont() {
        
        if (menuFont == null) {
            
            try {
                menuFont = new FontUIResource(
                                    Font.getFont("swing.plaf.metal.menuFont",
                                    new Font("Dialog", Font.PLAIN, getDefaultFontSize())));
            }             
            catch (Exception e) {
                menuFont = new FontUIResource("Dialog", Font.PLAIN, getDefaultFontSize());
            } 
            
        } 
        
        return menuFont;
        
    }
    
    public FontUIResource getWindowTitleFont() {
        
        if (controlFont == null) {
            
            try {
                controlFont = new FontUIResource(
                                        Font.getFont("swing.plaf.metal.controlFont",
                                        new Font("Dialog", Font.PLAIN, getDefaultFontSize())));
            } 
            catch (Exception e) {
                controlFont = new FontUIResource("Dialog", Font.BOLD, getDefaultFontSize());
            } 
            
        } 
        
        return controlFont;
        
    }
    
    public FontUIResource getSubTextFont() {
        
        if (smallFont == null) {
            
            try {
                smallFont = new FontUIResource(Font.getFont("swing.plaf.metal.smallFont",
                                               new Font("Dialog", Font.PLAIN, 10)));
            }
            catch (Exception e) {
                smallFont = new FontUIResource("Dialog", Font.PLAIN, 10);
            } 
            
        } 
        
        return smallFont;
        
    }

}







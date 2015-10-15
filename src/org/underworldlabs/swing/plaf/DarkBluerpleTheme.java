/*
 * DarkBluerpleTheme.java
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
import java.awt.Font;
import java.awt.Insets;

import javax.swing.UIDefaults;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class DarkBluerpleTheme extends DefaultMetalTheme {
    
    // ---------------------------------
    // --------- System Fonts ----------
    // ---------------------------------
    private FontUIResource menuFont;
    private FontUIResource controlFont;
    private FontUIResource systemFont;
    private FontUIResource userFont;
    private FontUIResource smallFont;

    private final ColorUIResource myControlHighlightColor = new ColorUIResource(108, 111, 113);
    private final ColorUIResource myControlDarkShadowColor = new ColorUIResource(39, 42, 44);
    private final ColorUIResource myControlColor = new ColorUIResource(0x3c3f41);
    private static final ColorUIResource white = new ColorUIResource(128, 128, 128);
    private static final ColorUIResource darkBlue = new ColorUIResource(0, 44, 63);
    private static final ColorUIResource lightGray = new ColorUIResource(109, 109, 109);
    private final ColorUIResource mySeparatorForeground = new ColorUIResource(53, 56, 58);

    // --- active internal frame borders ---
    private final ColorUIResource primary1 = new ColorUIResource(53, 56, 58);
    
    // --- scroll bars, highlights, menu selection etc ---
    private final ColorUIResource primary2 = new ColorUIResource(50, 66, 114);
    
    // --- active internal frame headers ---
    private final ColorUIResource primary3 = new ColorUIResource(53, 69, 91);
    
    // --- dark border for 3D for eg buttons ---
    private final ColorUIResource secondary1 = new ColorUIResource(102, 102, 102);
    
    // --- inactive internal frame borders, dimmed button borders ---
    private final ColorUIResource secondary2 = new ColorUIResource(153, 153, 153);

    // --- panel/frame, normal background ---
    private final ColorUIResource secondary3 = new ColorUIResource(60, 63, 65);
    
    /**
     * Add this theme's custom entries to the defaults table.
     *
     * @param table the defaults table, non-null
     * @throws NullPointerException if the parameter is null
     */
    public void addCustomEntriesToTable(UIDefaults table) {

        super.addCustomEntriesToTable(table);

        Object[] defaults = new Object[] {

                "Button.background", new ColorUIResource(60,63,65),
                "Button.darkShadow", new ColorUIResource(39,42,44),
                "Button.disabledText", new ColorUIResource(119,119,119),
                "Button.focus", new ColorUIResource(0,0,0),
                "Button.foreground", new ColorUIResource(187,187,187),
                "Button.highlight", new ColorUIResource(108,111,113),
                "Button.light", new ColorUIResource(108,111,113),
                "Button.select", new ColorUIResource(108,111,113),
                "Button.shadow", new ColorUIResource(153,153,153),
                "CheckBox.background", new ColorUIResource(60,63,65),
                "CheckBox.disabledText", new ColorUIResource(153,153,153),
                "CheckBox.focus", new ColorUIResource(0,0,0),
                "CheckBox.foreground", new ColorUIResource(187,187,187),
                "CheckBoxMenuItem.acceleratorForeground", new ColorUIResource(53,56,58),
                "CheckBoxMenuItem.acceleratorSelectionForeground", new ColorUIResource(128,128,128),
                "CheckBoxMenuItem.background", new ColorUIResource(60,63,65),
                "CheckBoxMenuItem.disabledForeground", new ColorUIResource(153,153,153),
                "CheckBoxMenuItem.foreground", new ColorUIResource(187,187,187),
                "CheckBoxMenuItem.selectionBackground", new ColorUIResource(47,101,202),
                "CheckBoxMenuItem.selectionForeground", new ColorUIResource(187,187,187),
                "Checkbox.select", new ColorUIResource(153,153,153),
                "ColorChooser.background", new ColorUIResource(60,63,65),
                "ColorChooser.foreground", new ColorUIResource(187,187,187),
                "ColorChooser.swatchesDefaultRecentColor", new ColorUIResource(60,63,65),
                "ComboBox.background", new ColorUIResource(60,63,65),
                "ComboBox.buttonBackground", new ColorUIResource(60,63,65),
                "ComboBox.buttonDarkShadow", new ColorUIResource(39,42,44),
                "ComboBox.buttonHighlight", new ColorUIResource(108,111,113),
                "ComboBox.buttonShadow", new ColorUIResource(153,153,153),
                "ComboBox.disabledBackground", new ColorUIResource(60,63,65),
                "ComboBox.disabledForeground", new ColorUIResource(153,153,153),
                "ComboBox.foreground", new ColorUIResource(187,187,187),
                "ComboBox.selectionBackground", new ColorUIResource(47,101,202),
                "ComboBox.selectionForeground", new ColorUIResource(187,187,187),
                "Desktop.background", new ColorUIResource(60,63,65),
                "DesktopIcon.background", new ColorUIResource(60,63,65),
                "DesktopIcon.foreground", new ColorUIResource(187,187,187),
                "EditorPane.background", new ColorUIResource(60,63,65),
                "EditorPane.caretForeground", new ColorUIResource(187,187,187),
                "EditorPane.foreground", new ColorUIResource(187,187,187),
                "EditorPane.inactiveBackground", new ColorUIResource(69,73,74),
                "EditorPane.inactiveForeground", new ColorUIResource(187,187,187),
                "EditorPane.selectionBackground", new ColorUIResource(47,101,202),
                "EditorPane.selectionForeground", new ColorUIResource(187,187,187),
                "Focus.color", new ColorUIResource(255,0,0),
                "FormattedTextField.background", new ColorUIResource(69,73,74),
                "FormattedTextField.caretForeground", new ColorUIResource(187,187,187),
                "FormattedTextField.foreground", new ColorUIResource(187,187,187),
                "FormattedTextField.inactiveBackground", new ColorUIResource(60,63,65),
                "FormattedTextField.inactiveForeground", new ColorUIResource(153,153,153),
                "FormattedTextField.selectionBackground", new ColorUIResource(47,101,202),
                "FormattedTextField.selectionForeground", new ColorUIResource(187,187,187),
                "Hyperlink.linkColor", new ColorUIResource(88,157,246),
                "InternalFrame.activeTitleBackground", new ColorUIResource(53,69,91),
                "InternalFrame.activeTitleForeground", new ColorUIResource(0,0,0),
                "InternalFrame.borderColor", new ColorUIResource(60,63,65),
                "InternalFrame.borderDarkShadow", new ColorUIResource(39,42,44),
                "InternalFrame.borderHighlight", new ColorUIResource(108,111,113),
                "InternalFrame.borderLight", new ColorUIResource(108,111,113),
                "InternalFrame.borderShadow", new ColorUIResource(153,153,153),
                "InternalFrame.inactiveTitleBackground", new ColorUIResource(204,204,204),
                "InternalFrame.inactiveTitleForeground", new ColorUIResource(0,0,0),
                "Label.background", new ColorUIResource(60,63,65),
                "Label.disabledForeground", new ColorUIResource(39,42,44),
                "Label.disabledShadow", new ColorUIResource(153,153,153),
                "Label.foreground", new ColorUIResource(187,187,187),
                "List.background", new ColorUIResource(60,63,65),
                "List.dropLineColor", new ColorUIResource(153,153,153),
                "List.foreground", new ColorUIResource(187,187,187),
                "List.selectionBackground", new ColorUIResource(47,101,202),
                "List.selectionForeground", new ColorUIResource(187,187,187),
                "Menu.acceleratorForeground", new ColorUIResource(53,56,58),
                "Menu.acceleratorSelectionForeground", new ColorUIResource(128,128,128),
                "Menu.background", new ColorUIResource(60,63,65),
                "Menu.disabledForeground", new ColorUIResource(153,153,153),
                "Menu.foreground", new ColorUIResource(187,187,187),
                "Menu.selectionBackground", new ColorUIResource(47,101,202),
                "Menu.selectionForeground", new ColorUIResource(187,187,187),
                "MenuBar.background", new ColorUIResource(60,63,65),
                "MenuBar.disabledBackground", new ColorUIResource(60,63,65),
                "MenuBar.foreground", new ColorUIResource(187,187,187),
                "MenuBar.highlight", new ColorUIResource(108,111,113),
                "MenuBar.shadow", new ColorUIResource(60,63,65),
                "MenuItem.acceleratorForeground", new ColorUIResource(238,238,238),
                "MenuItem.acceleratorSelectionForeground", new ColorUIResource(128,128,128),
                "MenuItem.background", new ColorUIResource(60,63,65),
                "MenuItem.disabledForeground", new ColorUIResource(153,153,153),
                "MenuItem.foreground", new ColorUIResource(187,187,187),
                "MenuItem.selectionBackground", new ColorUIResource(47,101,202),
                "MenuItem.selectionForeground", new ColorUIResource(187,187,187),
                "OptionPane.background", new ColorUIResource(60,63,65),
                "OptionPane.errorDialog.border.background", new ColorUIResource(60,63,65),
                "OptionPane.errorDialog.titlePane.background", new ColorUIResource(60,63,65),
                "OptionPane.errorDialog.titlePane.foreground", new ColorUIResource(187,187,187),
                "OptionPane.errorDialog.titlePane.shadow", new ColorUIResource(204,102,102),
                "OptionPane.foreground", new ColorUIResource(187,187,187),
                "OptionPane.messageForeground", new ColorUIResource(187,187,187),
                "OptionPane.questionDialog.border.background", new ColorUIResource(60,63,65),
                "OptionPane.questionDialog.titlePane.background", new ColorUIResource(60,63,65),
                "OptionPane.questionDialog.titlePane.foreground", new ColorUIResource(187,187,187),
                "OptionPane.questionDialog.titlePane.shadow", new ColorUIResource(102,153,102),
                "OptionPane.warningDialog.border.background", new ColorUIResource(60,63,65),
                "OptionPane.warningDialog.titlePane.background", new ColorUIResource(60,63,65),
                "OptionPane.warningDialog.titlePane.foreground", new ColorUIResource(187,187,187),
                "OptionPane.warningDialog.titlePane.shadow", new ColorUIResource(204,153,102),
                "Panel.background", new ColorUIResource(60,63,65),
                "Panel.foreground", new ColorUIResource(187,187,187),
                "PasswordField.background", new ColorUIResource(69,73,74),
                "PasswordField.caretForeground", new ColorUIResource(187,187,187),
                "PasswordField.foreground", new ColorUIResource(187,187,187),
                "PasswordField.inactiveBackground", new ColorUIResource(60,63,65),
                "PasswordField.inactiveForeground", new ColorUIResource(153,153,153),
                "PasswordField.selectionBackground", new ColorUIResource(47,101,202),
                "PasswordField.selectionForeground", new ColorUIResource(187,187,187),
                "PopupMenu.background", new ColorUIResource(60,63,65),
                "PopupMenu.foreground", new ColorUIResource(187,187,187),
                "PopupMenu.translucentBackground", new ColorUIResource(60,63,65),
                "ProgressBar.background", new ColorUIResource(60,63,65),
                "ProgressBar.foreground", new ColorUIResource(128,128,128),
                "ProgressBar.selectionBackground", new ColorUIResource(47,101,202),
                "ProgressBar.selectionForeground", new ColorUIResource(187,187,187),
                "RadioButton.background", new ColorUIResource(60,63,65),
                "RadioButton.darkShadow", new ColorUIResource(39,42,44),
                "RadioButton.disabledText", new ColorUIResource(153,153,153),
                "RadioButton.focus", new ColorUIResource(0,0,0),
                "RadioButton.foreground", new ColorUIResource(187,187,187),
                "RadioButton.highlight", new ColorUIResource(108,111,113),
                "RadioButton.light", new ColorUIResource(108,111,113),
                "RadioButton.select", new ColorUIResource(153,153,153),
                "RadioButton.shadow", new ColorUIResource(153,153,153),
                "RadioButtonMenuItem.acceleratorForeground", new ColorUIResource(53,56,58),
                "RadioButtonMenuItem.acceleratorSelectionForeground", new ColorUIResource(128,128,128),
                "RadioButtonMenuItem.background", new ColorUIResource(60,63,65),
                "RadioButtonMenuItem.disabledForeground", new ColorUIResource(153,153,153),
                "RadioButtonMenuItem.foreground", new ColorUIResource(187,187,187),
                "RadioButtonMenuItem.selectionBackground", new ColorUIResource(47,101,202),
                "RadioButtonMenuItem.selectionForeground", new ColorUIResource(187,187,187),
                "ScrollBar.background", new ColorUIResource(60,63,65),
                "ScrollBar.darkShadow", new ColorUIResource(39,42,44),
                "ScrollBar.foreground", new ColorUIResource(187,187,187),
//                "ScrollBar.highlight", new ColorUIResource(108,111,113),
                "ScrollBar.highlight", new ColorUIResource(39,42,44),
//                "ScrollBar.shadow", new ColorUIResource(153,153,153),
                "ScrollBar.shadow", new ColorUIResource(39,42,44),
                "ScrollBar.thumb", new ColorUIResource(50,66,114),
                "ScrollBar.thumbDarkShadow", new ColorUIResource(39,42,44),
                "ScrollBar.thumbHighlight", new ColorUIResource(53,69,91),
                "ScrollBar.thumbShadow", new ColorUIResource(53,56,58),
                "ScrollBar.track", new ColorUIResource(60,63,65),
                "ScrollBar.trackHighlight", new ColorUIResource(39,42,44),
                "ScrollPane.background", new ColorUIResource(60,63,65),
//                "ScrollPane.foreground", new ColorUIResource(187,187,187),
                "ScrollPane.foreground", new ColorUIResource(Color.RED),
                "Separator.background", new ColorUIResource(60,63,65),
                "Separator.foreground", new ColorUIResource(45,45,45),
                "Separator.highlight", new ColorUIResource(108,111,113),
                "Separator.shadow", new ColorUIResource(153,153,153),
                "Slider.background", new ColorUIResource(60,63,65),
                "Slider.focus", new ColorUIResource(0,0,0),
                "Slider.foreground", new ColorUIResource(187,187,187),
                "Slider.highlight", new ColorUIResource(108,111,113),
                "Slider.shadow", new ColorUIResource(153,153,153),
                "Spinner.background", new ColorUIResource(60,63,65),
                "Spinner.foreground", new ColorUIResource(187,187,187),
                "SplitPane.background", new ColorUIResource(60,63,65),
                "SplitPane.darkShadow", new ColorUIResource(39,42,44),
                "SplitPane.dividerFocusColor", new ColorUIResource(53,69,91),
                "SplitPane.highlight", new ColorUIResource(60,63,65),
                "SplitPane.shadow", new ColorUIResource(153,153,153),
                "SplitPaneDivider.draggingColor", new ColorUIResource(64,64,64),
                "StatusBar.bottomColor", new ColorUIResource(44,44,44),
                "StatusBar.top2Color", new ColorUIResource(44,44,44),
                "StatusBar.topColor", new ColorUIResource(44,44,44),
                "TabbedPane.background", new ColorUIResource(60,63,65),
                "TabbedPane.darkShadow", new ColorUIResource(41,43,45),
                "TabbedPane.focus", new ColorUIResource(53,56,58),
                "TabbedPane.foreground", new ColorUIResource(187,187,187),
                "TabbedPane.highlight", new ColorUIResource(41,43,45),
                "TabbedPane.light", new ColorUIResource(68,68,68),
                "TabbedPane.selectHighlight", new ColorUIResource(60,63,65),
                "TabbedPane.selected", new ColorUIResource(65,81,109),
                "TabbedPane.shadow", new ColorUIResource(60,63,65),
                "TabbedPane.tabAreaBackground", new ColorUIResource(60,63,65),
                "Table.background", new ColorUIResource(60,63,65),
                "Table.dropLineColor", new ColorUIResource(0,0,0),
                "Table.dropLineShortColor", new ColorUIResource(53,56,58),
                "Table.focusCellBackground", new ColorUIResource(255,255,255),
                "Table.focusCellForeground", new ColorUIResource(0,0,0),
                "Table.foreground", new ColorUIResource(187,187,187),
                "Table.gridColor", new ColorUIResource(44,44,44),
                "Table.selectionBackground", new ColorUIResource(47,101,202),
                "Table.selectionForeground", new ColorUIResource(187,187,187),
                "Table.sortIconColor", new ColorUIResource(153,153,153),
                "TableHeader.background", new ColorUIResource(60,63,65),
                "TableHeader.focusCellBackground", new ColorUIResource(255,255,255),
                "TableHeader.foreground", new ColorUIResource(187,187,187),
                "TextArea.background", new ColorUIResource(69,73,74),
                "TextArea.caretForeground", new ColorUIResource(187,187,187),
                "TextArea.foreground", new ColorUIResource(187,187,187),
                "TextArea.inactiveForeground", new ColorUIResource(153,153,153),
                "TextArea.selectionBackground", new ColorUIResource(47,101,202),
                "TextArea.selectionForeground", new ColorUIResource(187,187,187),
                "TextField.background", new ColorUIResource(69,73,74),
                "TextField.caretForeground", new ColorUIResource(187,187,187),
                "TextField.darkShadow", new ColorUIResource(39,42,44),
                "TextField.foreground", new ColorUIResource(187,187,187),
                "TextField.highlight", new ColorUIResource(108,111,113),
                "TextField.inactiveBackground", new ColorUIResource(60,63,65),
                "TextField.inactiveForeground", new ColorUIResource(153,153,153),
                "TextField.light", new ColorUIResource(108,111,113),
                "TextField.selectionBackground", new ColorUIResource(47,101,202),
                "TextField.selectionForeground", new ColorUIResource(187,187,187),
                "TextField.shadow", new ColorUIResource(153,153,153),
                "TextPane.background", new ColorUIResource(60,63,65),
                "TextPane.caretForeground", new ColorUIResource(187,187,187),
                "TextPane.foreground", new ColorUIResource(187,187,187),
                "TextPane.inactiveForeground", new ColorUIResource(153,153,153),
                "TextPane.selectionBackground", new ColorUIResource(47,101,202),
                "TextPane.selectionForeground", new ColorUIResource(187,187,187),
                "TitledBorder.titleColor", new ColorUIResource(187,187,187),
                "ToggleButton.background", new ColorUIResource(60,63,65),
                "ToggleButton.darkShadow", new ColorUIResource(39,42,44),
                "ToggleButton.disabledText", new ColorUIResource(153,153,153),
                "ToggleButton.focus", new ColorUIResource(0,0,0),
                "ToggleButton.foreground", new ColorUIResource(187,187,187),
                "ToggleButton.highlight", new ColorUIResource(108,111,113),
                "ToggleButton.light", new ColorUIResource(108,111,113),
                "ToggleButton.select", new ColorUIResource(153,153,153), // primary3
                "ToggleButton.shadow", new ColorUIResource(153,153,153),
                "ToolBar.background", new ColorUIResource(60,63,65),
                "ToolBar.darkShadow", new ColorUIResource(39,42,44),
                "ToolBar.dockingBackground", new ColorUIResource(109,109,109),
                "ToolBar.dockingForeground", new ColorUIResource(53,56,58),
                "ToolBar.floatingBackground", new ColorUIResource(109,109,109),
                "ToolBar.floatingForeground", new ColorUIResource(53,69,91),
                "ToolBar.foreground", new ColorUIResource(187,187,187),
                "ToolBar.highlight", new ColorUIResource(108,111,113),
                "ToolBar.light", new ColorUIResource(108,111,113),
                "ToolBar.shadow", new ColorUIResource(153,153,153),
                "ToolTip.background", new ColorUIResource(92,92,66),
                "ToolTip.backgroundInactive", new ColorUIResource(60,63,65),
                "ToolTip.foreground", new ColorUIResource(187,187,187),
                "ToolTip.foregroundInactive", new ColorUIResource(39,42,44),
                "Tree.background", new ColorUIResource(60,63,65),
                "Tree.dropLineColor", new ColorUIResource(153,153,153),
                "Tree.foreground", new ColorUIResource(187,187,187),
                "Tree.hash", new ColorUIResource(53,69,91),
//                "Tree.line", new ColorUIResource(53,69,91),
                "Tree.line", new ColorUIResource(44,44,44),
                "Tree.selectionBackground", new ColorUIResource(47,101,202),
//                "Tree.selectionBorderColor", new ColorUIResource(0,0,0),
                "Tree.selectionBorderColor", new ColorUIResource(44,44,44),
                "Tree.selectionForeground", new ColorUIResource(187,187,187),
                "Tree.textBackground", new ColorUIResource(60,63,65),
                "Tree.textForeground", new ColorUIResource(187,187,187),
                "Viewport.background", new ColorUIResource(60,63,65),
                "Viewport.foreground", new ColorUIResource(187,187,187),
                "activeCaption", new ColorUIResource(53,69,91),
                "activeCaptionBorder", new ColorUIResource(50,66,114),
                "activeCaptionText", new ColorUIResource(0,0,0),
                "control", new ColorUIResource(60,63,65),
                "controlDkShadow", new ColorUIResource(39,42,44),
                "controlHighlight", new ColorUIResource(108,111,113),
                "controlLtHighlight", new ColorUIResource(108,111,113),
                "controlShadow", new ColorUIResource(153,153,153),
                "controlText", new ColorUIResource(0,0,0),
                "desktop", new ColorUIResource(50,66,114),
                "inactiveCaption", new ColorUIResource(204,204,204),
                "inactiveCaptionBorder", new ColorUIResource(153,153,153),
                "inactiveCaptionText", new ColorUIResource(0,0,0),
                "info", new ColorUIResource(53,69,91),
                "infoText", new ColorUIResource(187,187,187),
                "link.foreground", new ColorUIResource(88,157,246),
                "menu", new ColorUIResource(109,109,109),
                "menuText", new ColorUIResource(0,0,0),
                "scrollbar", new ColorUIResource(60,63,65),
                "text", new ColorUIResource(187,187,187),
                "textHighlight", new ColorUIResource(53,69,91),
                "textHighlightText", new ColorUIResource(0,0,0),
                "textInactiveText", new ColorUIResource(153,153,153),
                "textText", new ColorUIResource(187,187,187),
                "window", new ColorUIResource(60,63,65),
                "windowBorder", new ColorUIResource(60,63,65),
                "windowText", new ColorUIResource(0,0,0),
                
                "executequery.TabbedPane.border", new ColorUIResource(39,42,44),
                "executequery.TabbedPane.icon", new ColorUIResource(29,31,32),
                "executequery.GradientLabel.foreground", new ColorUIResource(153,153,153),
                "executequery.Border.colour", new ColorUIResource(39,42,44),
                "executequery.LinkButton.foreground", new ColorUIResource(63,126,211),
                "executequery.Erd.background", new ColorUIResource(60,63,65), 
                "executequery.Erd.grid", new ColorUIResource(0x555657), 
                "executequery.Erd.tableBackground", new ColorUIResource(0xDEDEDE), 
                "executequery.LoggingOutputPanel.plain", new ColorUIResource(187,187,187), 
                "executequery.LoggingOutputPanel.error", new ColorUIResource(0xCC0033), 
                "executequery.LoggingOutputPanel.warning", new ColorUIResource(0xffbf26), 
                "executequery.LoggingOutputPanel.action", new ColorUIResource(0x1290c3), 
                "executequery.PreferencesHeader.foreground", new ColorUIResource(153,153,153),
                "executequery.QueryEditor.queryTooltipBackground", new ColorUIResource(0x212020),
//                "executequery.ScrollingTabPane.closeButtonFocus", new ColorUIResource(0x212020),

                "ComboBoxUI", "javax.swing.plaf.metal.MetalComboBoxUI",

                "MenuItem.border", new org.underworldlabs.swing.plaf.SimpleMenuItemBorder(),
                "CheckBoxMenuItem.border", new org.underworldlabs.swing.plaf.SimpleMenuItemBorder(),
                "Menu.border", new org.underworldlabs.swing.plaf.SimpleMenuItemBorder(),
                "PopupMenu.border", new org.underworldlabs.swing.plaf.SimpleMenuBorder(new ColorUIResource(39,42,44)),

                
//            "TabbedPane.borderHightlightColor", getSecondary1(),
//            "TabbedPane.contentAreaColor", getSecondary3(),
            "TabbedPane.contentBorderInsets", new Insets(2, 2, 3, 3),
//            "TabbedPane.selected", new ColorUIResource(58, 77, 134),
//            "TabbedPane.tabAreaBackground", secondary3,
//            "TabbedPane.tabAreaBackground", getSecondary3(),
            "TabbedPane.tabAreaInsets", new Insets(4, 2, 0, 6),
//            "TabbedPane.unselectedBackground", cccccc,
            
//            "ScrollPane.background", secondary3,
//            "Tree.background", secondary3,
//            "Tree.textBackground", secondary3,
            
            "ToolTip.border", new BluerpleBorder(),
            "TextField.border", new BluerpleBorder(new Color(39,42,44)),
            "PasswordField.border", new BluerpleBorder(new Color(39,42,44)),
            "Button.border", new BluerpleBorder(new Color(39,42,44)),
            
            "MenuBarUI", "javax.swing.plaf.metal.MetalMenuBarUI",

        };
        table.putDefaults(defaults);
    }
    
    public String getName() {
        return "Dark Bluerple";
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
                                               new Font("Dialog", Font.PLAIN, 11)));
            }
            catch (Exception e) {
                smallFont = new FontUIResource("Dialog", Font.PLAIN, 11);
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

    public ColorUIResource getControl() {
        return myControlColor;
      }

      @Override
      public ColorUIResource getControlHighlight() {
        return myControlHighlightColor;
      }
    
      @Override
      public ColorUIResource getControlDarkShadow() {
        return myControlDarkShadowColor;
      }
    
      public ColorUIResource getSeparatorBackground() {
        return getControl();
      }
    
      public ColorUIResource getSeparatorForeground() {
        return mySeparatorForeground;
      }
    
      public ColorUIResource getMenuBackground() {
        return lightGray;
      }
    
      public ColorUIResource getMenuSelectedBackground() {
        return darkBlue;
      }
    
      public ColorUIResource getMenuSelectedForeground() {
        return white;
      }
    
      public ColorUIResource getAcceleratorSelectedForeground() {
        return white;
      }
    
      public ColorUIResource getFocusColor() {
        return new ColorUIResource(Color.black);
      }

}

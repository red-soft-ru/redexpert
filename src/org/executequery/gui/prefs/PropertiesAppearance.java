/*
 * PropertiesAppearance.java
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

package org.executequery.gui.prefs;

import org.executequery.GUIUtilities;
import org.executequery.plaf.LookAndFeelType;
import org.underworldlabs.util.LabelValuePair;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * System preferences appearance panel.
 *
 * @author Takis Diakoumis
 */
public class PropertiesAppearance extends AbstractPropertiesBasePanel {

    private SimplePreferencesPanel preferencesPanel;
    private boolean lafChangeWarningShown = false;

    public PropertiesAppearance() {
        init();
    }

    private void init() {

        String key;
        List<UserPreference> list = new ArrayList<>();

        list.add(new UserPreference(
                UserPreference.CATEGORY_TYPE,
                null,
                bundledStaticString("General"),
                null
        ));

        key = "startup.display.splash";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("DisplaySplashScreenAtStartup"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "startup.window.maximized";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("MaximiseWindowOnStartup"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "system.display.statusbar";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("StatusBar"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "system.display.connections";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("Connections"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        list.add(new UserPreference(
                UserPreference.CATEGORY_TYPE,
                null,
                bundledStaticString("Appearance"),
                null
        ));

        key = "startup.display.lookandfeel";
        list.add(new UserPreference(
                UserPreference.ENUM_TYPE,
                key,
                bundledStaticString("LookAndFeel"),
                LookAndFeelType.valueOf(stringUserProperty(key)),
                lookAndFeelValuePairs()
        ));

        key = "display.aa.fonts";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("UseAnti-aliasFonts"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "desktop.background.custom.colour";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                bundledStaticString("DesktopBackground"),
                SystemProperties.getColourProperty("user", key)
        ));

        key = "decorate.dialog.look";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("DecorateDialogs"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "decorate.frame.look";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("DecorateFrame"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        preferencesPanel = new SimplePreferencesPanel(list.toArray(new UserPreference[0]));
        addContent(preferencesPanel);
        lookAndFeelCombBox().addActionListener(e -> itemStateChanged());
    }

    @SuppressWarnings("rawtypes")
    private JComboBox lookAndFeelCombBox() {
        return (JComboBox) preferencesPanel.getComponentEditorForKey("startup.display.lookandfeel");
    }

    public void itemStateChanged() {

        if (!lafChangeWarningShown) {
            GUIUtilities.displayInformationMessage(bundledString("ChangingTheme.Information"));
            lafChangeWarningShown = true;
        }

        AbstractPropertiesColours.setSelectedLookAndFeel(getCurrentlySelectedLookAndFeel());

        PropertiesEditorColours editorColours = new PropertiesEditorColours();
        editorColours.restoreDefaults();
        editorColours.save();

        PropertiesResultSetTableColours resultsetColours = new PropertiesResultSetTableColours();
        resultsetColours.restoreDefaults();
        resultsetColours.save();
    }

    private Object[] lookAndFeelValuePairs() {

        LookAndFeelType[] lookAndFeelTypes = LookAndFeelType.values();
        LabelValuePair[] values = new LabelValuePair[lookAndFeelTypes.length];
        for (int i = 0; i < lookAndFeelTypes.length; i++) {
            LookAndFeelType lookAndFeelType = lookAndFeelTypes[i];
            values[i] = new LabelValuePair(lookAndFeelType, lookAndFeelType.getDescription());
        }

        return values;
    }

    private LookAndFeelType getCurrentlySelectedLookAndFeel() {

        LabelValuePair selectedValue = (LabelValuePair) lookAndFeelCombBox().getSelectedItem();
        if (selectedValue == null)
            return null;

        return (LookAndFeelType) (selectedValue).getValue();
    }

    @Override
    public void restoreDefaults() {
        preferencesPanel.savePreferences();
        apply();
    }

    @Override
    public void save() {
        preferencesPanel.savePreferences();
        apply();
    }

    private void apply() {
        GUIUtilities.displayStatusBar(SystemProperties.getBooleanProperty("user", "system.display.statusbar"));
    }

}

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
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the state of this instance.
     */
    private void init() throws Exception {

        List<UserPreference> list = new ArrayList<UserPreference>();

        list.add(new UserPreference(
                UserPreference.CATEGORY_TYPE,
                null,
                bundledString("General"),
                null));

        String key = "system.display.statusbar";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("StatusBar"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "system.display.connections";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("Connections"),
                Boolean.valueOf(stringUserProperty(key))));

        list.add(new UserPreference(
                UserPreference.CATEGORY_TYPE,
                null,
                bundledString("Appearance"),
                null));

        key = "startup.display.lookandfeel";
        list.add(new UserPreference(
                UserPreference.ENUM_TYPE,
                key,
                bundledString("LookAndFeel"),
                LookAndFeelType.valueOf(stringUserProperty(key)),
                lookAndFeelValuePairs()));

        key = "display.aa.fonts";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("UseAnti-aliasFonts"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "desktop.background.custom.colour";
        list.add(new UserPreference(
                UserPreference.COLOUR_TYPE,
                key,
                bundledString("DesktopBackground"),
                SystemProperties.getColourProperty("user", key)));

        key = "decorate.dialog.look";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("DecorateDialogs"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "decorate.frame.look";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("DecorateFrame"),
                Boolean.valueOf(stringUserProperty(key))));

        UserPreference[] preferences =
                list.toArray(new UserPreference[list.size()]);
        preferencesPanel = new SimplePreferencesPanel(preferences);
        addContent(preferencesPanel);

        lookAndFeelCombBox().addActionListener(e -> itemStateChanged());
    }

    @SuppressWarnings("rawtypes")
    private JComboBox lookAndFeelCombBox() {
        return (JComboBox) preferencesPanel.getComponentEditorForKey("startup.display.lookandfeel");
    }

    public void itemStateChanged() {

        if (!lafChangeWarningShown) {
            GUIUtilities.displayInformationMessage(bundleString("ChangingTheme.Information"));
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

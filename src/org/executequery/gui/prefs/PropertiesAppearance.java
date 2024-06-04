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
import org.executequery.localization.InterfaceLanguage;
import org.executequery.plaf.LookAndFeelType;
import org.underworldlabs.util.LabelValuePair;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * System preferences appearance panel.
 *
 * @author Takis Diakoumis
 */
public class PropertiesAppearance extends AbstractPropertiesBasePanel {

    private SimplePreferencesPanel preferencesPanel;
    private CustomLafSelectionPanel lafSelectionPanel;
    private boolean lafChangeWarningShown = false;

    public PropertiesAppearance(PropertiesPanel parent) {
        super(parent);
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

        key = "startup.display.language";
        list.add(new UserPreference(
                UserPreference.ENUM_TYPE,
                key,
                bundledStaticString("language"),
                InterfaceLanguage.valueOf(stringUserProperty(key)),
                languageValuePairs()
        ));

        key = "display.aa.fonts";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledStaticString("UseAnti-aliasFonts"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        lafSelectionPanel = new CustomLafSelectionPanel();
        lafSelectionPanel.setVisible(Objects.equals(
                LookAndFeelType.PLUGIN,
                LookAndFeelType.valueOf(stringUserProperty("startup.display.lookandfeel"))
        ));

        preferencesPanel = new SimplePreferencesPanel(list.toArray(new UserPreference[0]));
        preferencesPanel.add(lafSelectionPanel, BorderLayout.SOUTH);

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

        LookAndFeelType selectedLaf = getCurrentlySelectedLookAndFeel();
        AbstractPropertiesColours.setSelectedLookAndFeel(selectedLaf);
        lafSelectionPanel.setVisible(LookAndFeelType.PLUGIN.equals(selectedLaf));

        PropertiesEditorColours editorColours = new PropertiesEditorColours(null);
        editorColours.restoreDefaults();
        editorColours.save();

        PropertiesResultSetTableColours resultSetColours = new PropertiesResultSetTableColours(null);
        resultSetColours.restoreDefaults();
        resultSetColours.save();
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

    private LabelValuePair[] languageValuePairs() {

        InterfaceLanguage[] languages = InterfaceLanguage.values();

        LabelValuePair[] values = new LabelValuePair[languages.length];
        for (int i = 0; i < languages.length; i++)
            values[i] = new LabelValuePair(languages[i], languages[i].getLabel());

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
        lafSelectionPanel.restore();
        preferencesPanel.savePreferences();
        apply();
    }

    @Override
    public void save() {
        lafSelectionPanel.save();
        preferencesPanel.savePreferences();
        apply();
    }

    private void apply() {
        GUIUtilities.displayStatusBar(SystemProperties.getBooleanProperty("user", "system.display.statusbar"));
    }

}

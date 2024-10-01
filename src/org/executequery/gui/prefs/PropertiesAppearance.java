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

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.localization.InterfaceLanguage;
import org.executequery.plaf.LookAndFeelType;
import org.underworldlabs.util.LabelValuePair;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Arrays;
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
                getSelectedInterfaceLanguage(),
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
        lookAndFeelCombBox().addItemListener(this::lookAndFeelChanged);
    }

    @SuppressWarnings("rawtypes")
    private JComboBox lookAndFeelCombBox() {
        return (JComboBox) preferencesPanel.getComponentEditorForKey("startup.display.lookandfeel");
    }

    private synchronized void lookAndFeelChanged(ItemEvent e) {

        if (e.getStateChange() != ItemEvent.SELECTED)
            return;

        LookAndFeelType selectedLaf = getCurrentlySelectedLookAndFeel();
        AbstractPropertiesColours.setSelectedLookAndFeel(selectedLaf);
        lafSelectionPanel.setVisible(LookAndFeelType.PLUGIN.equals(selectedLaf));

        restoreAndSaveDefaults(PropertyTypes.EDITOR_COLOURS);
        restoreAndSaveDefaults(PropertyTypes.RESULT_SET_COLOURS);
    }

    private Object[] lookAndFeelValuePairs() {

        List<LabelValuePair> values = new ArrayList<>();
        for (LookAndFeelType lafType : LookAndFeelType.values())
            if (lafType != LookAndFeelType.LACKEY)
                values.add(new LabelValuePair(lafType, lafType.getDescription()));

        return values.toArray();
    }

    private Object[] languageValuePairs() {

        List<LabelValuePair> values = new ArrayList<>();
        for (InterfaceLanguage lafType : InterfaceLanguage.values())
            values.add(new LabelValuePair(lafType, lafType.getLabel()));

        return values.toArray();
    }

    private LookAndFeelType getCurrentlySelectedLookAndFeel() {

        LabelValuePair selectedValue = (LabelValuePair) lookAndFeelCombBox().getSelectedItem();
        if (selectedValue == null)
            return null;

        return (LookAndFeelType) (selectedValue).getValue();
    }

    private InterfaceLanguage getSelectedInterfaceLanguage() {

        String interfaceLanguage = SystemProperties.getProperty(Constants.USER_PROPERTIES_KEY, "startup.display.language");
        if (Arrays.stream(InterfaceLanguage.values()).anyMatch(value -> Objects.equals(value.getKey(), interfaceLanguage)))
            return InterfaceLanguage.valueOf(interfaceLanguage);

        SystemProperties.setStringProperty(Constants.USER_PROPERTIES_KEY, "startup.display.language", "en");
        return InterfaceLanguage.en;
    }

    @Override
    public void restoreDefaults() {
        lafSelectionPanel.restore();
        preferencesPanel.restoreDefaults();
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

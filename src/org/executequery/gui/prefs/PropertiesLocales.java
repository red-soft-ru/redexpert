/*
 * PropertiesLocales.java
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

import org.underworldlabs.swing.DisabledField;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.util.StringSorter;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Takis Diakoumis
 */
public class PropertiesLocales extends AbstractPropertiesBasePanel
        implements ListSelectionListener {

    private Locale[] locales;
    private JList<String> localeList;
    private JList<String> timezoneList;

    private DisabledField selectedLocaleField;
    private DisabledField selectedTimeZoneField;

    public PropertiesLocales() {
        init();
    }

    private void init() {

        locales = Locale.getAvailableLocales();
        Arrays.sort(locales, Comparator.comparing(Locale::getDisplayName));

        String[] timezones = TimeZone.getAvailableIDs();
        Arrays.sort(timezones, new StringSorter());

        String country = SystemProperties.getProperty("user", "locale.country");
        String language = SystemProperties.getProperty("user", "locale.language");

        boolean selectedFound = false;
        String selectedLocValue = null;

        String[] locValues = new String[locales.length];
        for (int i = 0; i < locValues.length; i++) {
            locValues[i] = locales[i].getDisplayName();

            if (!selectedFound && country != null && language != null) {
                if (country.compareTo(locales[i].getCountry()) == 0 && language.compareTo(locales[i].getLanguage()) == 0) {
                    selectedLocValue = locValues[i];
                    selectedFound = true;
                }
            }
        }

        localeList = new JList<>(locValues);
        timezoneList = new JList<>(timezones);

        localeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        timezoneList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        selectedLocaleField = new DisabledField();
        selectedTimeZoneField = new DisabledField();

        GridBagHelper gbh = new GridBagHelper()
                .setInsets(5, 5, 5, 5)
                .anchorNorthWest()
                .fillBoth();

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.add(new JLabel(bundledString("TimeZones")), gbh.setMinWeightX().setMinWeightY().get());
        mainPanel.add(selectedTimeZoneField, gbh.nextCol().setMinWeightX().get());
        mainPanel.add(new JLabel(bundledString("LanguageLocales")), gbh.nextCol().setMinWeightX().get());
        mainPanel.add(selectedLocaleField, gbh.nextCol().setMinWeightX().spanX().get());
        mainPanel.add(new JScrollPane(timezoneList), gbh.nextRowFirstCol().setWidth(2).setMaxWeightY().spanY().get());
        mainPanel.add(new JScrollPane(localeList), gbh.nextCol().spanX().get());

        localeList.setSelectedValue(selectedLocValue, true);
        timezoneList.setSelectedValue(SystemProperties.getProperty("user", "locale.timezone"), true);

        selectedLocaleField.setText(selectedLocValue);
        selectedTimeZoneField.setText(SystemProperties.getProperty("user", "locale.timezone"));

        add(mainPanel, new GridBagHelper().fillBoth().setMaxWeightY().spanX().get());
        localeList.addListSelectionListener(this);
        timezoneList.addListSelectionListener(this);
    }

    @Override
    public void save() {
        Locale loc = locales[localeList.getSelectedIndex()];
        SystemProperties.setProperty("user", "locale.country", loc.getCountry());
        SystemProperties.setProperty("user", "locale.language", loc.getLanguage());
        SystemProperties.setProperty("user", "locale.timezone", selectedTimeZoneField.getText());

        System.setProperty("user.country", loc.getCountry());
        System.setProperty("user.language", loc.getLanguage());
        System.setProperty("user.timezone", selectedTimeZoneField.getText());
    }

    @Override
    public void restoreDefaults() {
        timezoneList.setSelectedValue(SystemProperties.getProperty("defaults", "locale.timezone"), true);
        localeList.setSelectedValue(SystemProperties.getProperty("defaults", "locale.country"), true);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        selectedTimeZoneField.setText(timezoneList.getSelectedValue());
        selectedLocaleField.setText(locales[localeList.getSelectedIndex()].getDisplayName());
        PropertiesPanel.setRestartNeed(true);
    }

}

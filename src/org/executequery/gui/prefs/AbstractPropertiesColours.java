/*
 * AbstractPropertiesColours.java
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

import org.executequery.ApplicationException;
import org.executequery.Constants;
import org.executequery.plaf.LookAndFeelType;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.SystemProperties;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.executequery.Constants.*;

public abstract class AbstractPropertiesColours extends AbstractPropertiesBasePanel {

    private static final String LOOK_AND_FEEL_KEY = "startup.display.lookandfeel";

    private static LookAndFeelType selectedLookAndFeel;

    protected AbstractPropertiesColours(PropertiesPanel parent) {
        super(parent);
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent e) {

        if (LOOK_AND_FEEL_KEY.equals(e.getKey())) {

            LookAndFeelType lastSelectedLookAndFeel = selectedLookAndFeel;
            selectedLookAndFeel = (LookAndFeelType) e.getValue();

            if (lastSelectedLookAndFeel != selectedLookAndFeel)
                restoreDefaults();
        }

        super.preferenceChange(e);
    }

    protected Properties defaultsForTheme() {
        return defaultsForTheme(selectedLookAndFeel());
    }

    protected static Properties defaultsForTheme(LookAndFeelType lookAndFeel) {

        String resourcePath;
        if (lookAndFeel.isDefaultTheme()) {
            resourcePath = lookAndFeel.isDarkTheme() ?
                    "org/executequery/gui/editor/resource/sql-syntax.default.dark.profile" :
                    "org/executequery/gui/editor/resource/sql-syntax.default.light.profile";
        } else {
            resourcePath = lookAndFeel.isDarkTheme() ?
                    "org/executequery/gui/editor/resource/sql-syntax.classic.dark.profile" :
                    "org/executequery/gui/editor/resource/sql-syntax.classic.light.profile";
        }

        try {
            return FileUtils.loadPropertiesResource(resourcePath);

        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    private LookAndFeelType selectedLookAndFeel() {

        if (selectedLookAndFeel == null)
            selectedLookAndFeel = currentlySavedLookAndFeel();

        return selectedLookAndFeel;
    }

    private LookAndFeelType currentlySavedLookAndFeel() {

        String lookAndFeel = SystemProperties.getProperty(Constants.USER_PROPERTIES_KEY, "startup.display.lookandfeel");
        return LookAndFeelType.valueOf(lookAndFeel);
    }

    protected static Color asColour(String rgb) {
        return new Color(Integer.parseInt(rgb));
    }

    public static void setSelectedLookAndFeel(LookAndFeelType selectedLookAndFeel) {
        AbstractPropertiesColours.selectedLookAndFeel = selectedLookAndFeel;
    }

    public static void resetLookAndFeelColors(LookAndFeelType selectedLookAndFeel) {
        Properties themeDefaultsProperty = defaultsForTheme(selectedLookAndFeel);

        List<String> colorPropertiesKeys = new ArrayList<>();
        colorPropertiesKeys.addAll(Arrays.asList(OUTPUT_PANEL_COLOR_KEYS));
        colorPropertiesKeys.addAll(Arrays.asList(QUERY_EDITOR_COLOR_KEYS));
        colorPropertiesKeys.addAll(Arrays.asList(SQL_SYNTAX_COLOR_KEYS));

        for (String key : colorPropertiesKeys) {
            Color color = asColour(themeDefaultsProperty.getProperty(key));
            SystemProperties.setColourProperty("user", key, color);
        }
    }

}

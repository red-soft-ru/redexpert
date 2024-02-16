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
import java.util.Properties;

public abstract class AbstractPropertiesColours extends AbstractPropertiesBasePanel {

    private static final String LOOK_AND_FEEL_KEY = "startup.display.lookandfeel";

    private static LookAndFeelType selectedLookAndFeel;

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

        try {

            Properties defaults = FileUtils.loadPropertiesResource("org/executequery/gui/editor/resource/sql-syntax.default.profile");
            if (selectedLookAndFeel().isDarkTheme())
                defaults = FileUtils.loadPropertiesResource("org/executequery/gui/editor/resource/sql-syntax.dark.profile");

            return defaults;

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

    protected Color asColour(String rgb) {

        return new Color(Integer.parseInt(rgb));
    }

    public static void setSelectedLookAndFeel(LookAndFeelType selectedLookAndFeel) {
        AbstractPropertiesColours.selectedLookAndFeel = selectedLookAndFeel;
    }

}

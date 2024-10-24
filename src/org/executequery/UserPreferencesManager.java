/*
 * UserPreferencesManager.java
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

package org.executequery;

import org.executequery.event.DefaultUserPreferenceEvent;
import org.executequery.event.UserPreferenceEvent;
import org.underworldlabs.util.SystemProperties;

import java.awt.*;

/**
 * Proposed user preferences manager util for one-stop access.
 *
 * @author Takis Diakoumis
 */
public final class UserPreferencesManager {

    public static void fireUserPreferencesChanged() {
        fireUserPreferencesChanged(UserPreferenceEvent.ALL);
    }

    public static void fireUserPreferencesChanged(int eventType) {
        EventMediator.fireEvent(new DefaultUserPreferenceEvent(UserPreferencesManager.class, null, eventType));
    }

    public static Color getOutputPaneBackground() {
        return SystemProperties.getColourProperty(
                Constants.USER_PROPERTIES_KEY,
                "editor.output.background"
        );
    }

    public static boolean isTransposingSingleRowResultSets() {
        return SystemProperties.getBooleanProperty(
                Constants.USER_PROPERTIES_KEY,
                "results.table.single.row.transpose"
        );
    }

    public static boolean isResultSetTabSingle() {
        return SystemProperties.getBooleanProperty(
                Constants.USER_PROPERTIES_KEY,
                "editor.results.tabs.single"
        );
    }

}

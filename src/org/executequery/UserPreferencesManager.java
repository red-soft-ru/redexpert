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

import java.awt.Color;

import org.executequery.event.DefaultUserPreferenceEvent;
import org.executequery.event.UserPreferenceEvent;
import org.underworldlabs.util.SystemProperties;

/** 
 * Proposed user preferences manager util for one-stop access.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public final class UserPreferencesManager {

    private UserPreferencesManager() {}

    public static void fireUserPreferencesChanged() {
        
        EventMediator.fireEvent(
                new DefaultUserPreferenceEvent(
                        UserPreferencesManager.class, null, UserPreferenceEvent.ALL));
    }
    
    public static Color getOutputPaneBackground() {

        return SystemProperties.getColourProperty(
                Constants.USER_PROPERTIES_KEY, "editor.results.background.colour");
    }
    
    public static boolean isTransposingSingleRowResultSets() {
        
        return SystemProperties.getBooleanProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.single.row.transpose");
    }
    
    public static boolean isResultSetTabSingle() {
        
        return SystemProperties.getBooleanProperty(
                Constants.USER_PROPERTIES_KEY, "editor.results.tabs.single");
    }
    
    public static boolean doubleClickOpenItemView() {

        return SystemProperties.getBooleanProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.double-click.record.dialog"); 
    }
    
}







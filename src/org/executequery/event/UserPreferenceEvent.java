/*
 * UserPreferenceEvent.java
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

package org.executequery.event;

/**
 * @author Takis Diakoumis
 */
public interface UserPreferenceEvent extends ApplicationEvent {

    /**
     * Method name for preferences changed event
     */
    String PREFERENCES_CHANGED = "preferencesChanged";

    int ALL = 0;
    int QUERY_EDITOR = ALL + 1;
    int TOOL_BAR = QUERY_EDITOR + 1;
    int LOG = TOOL_BAR + 1;
    int PROXY = LOG + 1;
    int KEYBOARD_SHORTCUTS = PROXY + 1;
    int DOCKED_COMPONENT_CLOSED = KEYBOARD_SHORTCUTS + 1;
    int DOCKED_COMPONENT_OPENED = DOCKED_COMPONENT_CLOSED + 1;
    int RESULT_SET_POPUP = DOCKED_COMPONENT_OPENED + 1;

    int getEventType();

    String getKey();

}

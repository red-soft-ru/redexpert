/*
 * PropertyTypes.java
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

public interface PropertyTypes {

    int SYSTEM = 0;
    int GENERAL = SYSTEM + 1;
    int LOCALE = GENERAL + 1;
    int CONNECTIONS = LOCALE + 1;
    int LOOK_PLUGIN = CONNECTIONS + 1;
    int APPEARANCE = LOOK_PLUGIN + 1;
    int SHORTCUTS = APPEARANCE + 1;
    int SQL_SHORTCUTS = SHORTCUTS + 1;
    int KEYWORDS = SQL_SHORTCUTS + 1;
    int EDITOR = KEYWORDS + 1;
    int RESULT_SET = EDITOR + 1;

    int TOOLBAR_GENERAL = RESULT_SET + 1;
    int TOOLBAR_DATABASE = TOOLBAR_GENERAL + 1;
    int TOOLBAR_APPLICATION = TOOLBAR_DATABASE + 1;
    int TOOLBAR_QUERY_EDITOR = TOOLBAR_APPLICATION + 1;
    int TOOLBAR_SYSTEM = TOOLBAR_QUERY_EDITOR + 1;

    int FONTS_GENERAL = TOOLBAR_SYSTEM + 1;
    int EDITOR_FONTS = FONTS_GENERAL + 1;
    int CONNECTIONS_TREE_FONTS = EDITOR_FONTS + 1;
    int CONSOLE_FONTS = CONNECTIONS_TREE_FONTS + 1;

    int COLORS_GENERAL = CONSOLE_FONTS + 1;
    int EDITOR_COLOURS = COLORS_GENERAL + 1;
    int RESULT_SET_COLOURS = EDITOR_COLOURS + 1;

}

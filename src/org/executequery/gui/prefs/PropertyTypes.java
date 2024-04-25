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
    int VIEW = GENERAL + 1;
    int LOCALE = VIEW + 1;
    int CONNECTIONS = LOCALE + 1;
    int LOOK_PLUGIN = CONNECTIONS + 1;
    int APPEARANCE = LOOK_PLUGIN + 1;
    int SHORTCUTS = APPEARANCE + 1;

    int TOOLBAR_GENERAL = SHORTCUTS + 1;
    int TOOLBAR_BROWSER = TOOLBAR_GENERAL + 1;
    int TOOLBAR_IMPORT_EXPORT = TOOLBAR_BROWSER + 1;
    int TOOLBAR_DATABASE = TOOLBAR_IMPORT_EXPORT + 1;
    int TOOLBAR_SYSTEM = TOOLBAR_DATABASE + 1;

    int EDITOR_GENERAL = TOOLBAR_SYSTEM + 1;
    int EDITOR_FONTS = EDITOR_GENERAL + 1;
    int EDITOR_COLOURS = EDITOR_FONTS + 1;

    int BROWSER_GENERAL = EDITOR_COLOURS + 1;
    int BROWSER_DATA_TAB = BROWSER_GENERAL + 1;

    int TREE_CONNECTIONS_GENERAL = BROWSER_DATA_TAB + 1;
    int TREE_CONNECTIONS_FONTS = TREE_CONNECTIONS_GENERAL + 1;

    int RESULTS = TREE_CONNECTIONS_FONTS + 1;
    int RESULT_SET_CELL_COLOURS = RESULTS + 1;

    int OUTPUT_CONSOLE = RESULT_SET_CELL_COLOURS + 1;
    int CONSOLE_FONTS = OUTPUT_CONSOLE + 1;

}

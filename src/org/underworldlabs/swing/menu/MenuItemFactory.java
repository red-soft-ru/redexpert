/*
 * MenuItemFactory.java
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

package org.underworldlabs.swing.menu;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;


public final class MenuItemFactory {

    public static JMenu createMenu(String text) {
        
        return new MainMenu(text);
    }
    
    public static JMenuItem createMenuItem() {
        
        return new MainMenuItem();
    }

    public static JMenuItem createMenuItem(Action action) {
        
        return new MainMenuItem(action);
    }    

    public static JMenuItem createMenuItem(String text) {
        
        return new MainMenuItem(text);
    }

    public static JMenuItem createMenuItem(String text, Icon icon) {

        return new MainMenuItem(text, icon);
    }

    public static JCheckBoxMenuItem createCheckBoxMenuItem(String text) {
        
        return new MainCheckBoxMenuItem(text);
    }

    public static JCheckBoxMenuItem createCheckBoxMenuItem(Action action) {

        return new MainCheckBoxMenuItem(action);
    }

    public static JCheckBoxMenuItem createCheckBoxMenuItem(String text, boolean selected) {

        return new MainCheckBoxMenuItem(text, selected);
    }

}


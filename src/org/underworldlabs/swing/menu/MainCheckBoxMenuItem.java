/*
 * MainCheckBoxMenuItem.java
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

import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;

import org.executequery.gui.GUIConstants;

public class MainCheckBoxMenuItem extends JCheckBoxMenuItem {
    
    public MainCheckBoxMenuItem() {

        super();
    }
    
    public MainCheckBoxMenuItem(String text) {
        
        super(text);
    }

    public MainCheckBoxMenuItem(Action action) {

        super(action);
    }

    public MainCheckBoxMenuItem(String text, boolean selected) {

        super(text, selected);
    }

    public Dimension getPreferredSize() {

        Dimension preferredSize = super.getPreferredSize();
        preferredSize.height = Math.max(getHeight(), GUIConstants.DEFAULT_MENU_HEIGHT);

        return preferredSize;
    }

}


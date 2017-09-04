/*
 * MainMenu.java
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
import java.awt.Insets;

import javax.swing.JMenu;

import org.executequery.gui.GUIConstants;

public class MainMenu extends JMenu {

    public MainMenu() {
    
        super();
    }
    
    public MainMenu(String text) {
        
        super(text);
    }

    @Override
    public Insets getInsets() {

        return new Insets(0, 0, 0, 10);
    }
    
    public Dimension getPreferredSize() {

        Dimension preferredSize = super.getPreferredSize();
        preferredSize.height = Math.max(getHeight(), GUIConstants.DEFAULT_MENU_HEIGHT);

        return preferredSize;
    }
    
}


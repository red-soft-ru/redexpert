/*
 * UnderworldLabsDarkFlatLookAndFeel.java
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

package org.underworldlabs.swing.plaf;

import javax.swing.UIDefaults;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision$
 * @date     $Date$
 */
public class UnderworldLabsDarkFlatLookAndFeel extends UnderworldLabsFlatLookAndFeel {
    
    public UnderworldLabsDarkFlatLookAndFeel() {
        
        setCurrentTheme(darkBluerpleTheme());  
    }

    private DarkBluerpleTheme darkBluerpleTheme() {
        DarkBluerpleTheme theme = new DarkBluerpleTheme() {
            @Override
            public void addCustomEntriesToTable(UIDefaults table) {
                
                super.addCustomEntriesToTable(table);
            }
            @Override
            public int getDefaultFontSize() {
                
                return 13;
            }
        };
        return theme;
    }

    public String getName() {
        
        return "Dark UnderworldLabs Flat Look and Feel";
    }
    
    public String getDescription() {
        
        return "Themed extension to Smooth Gradient Look and Feel - modified from " +
                "The JGoodies Plastic Look and Feel and inspired by the Darcula theme";
    }
    
    
}



/*
 * AbstractPropertiesColours.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

import java.awt.Color;
import java.io.IOException;
import java.util.Properties;

import org.executequery.ApplicationException;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.util.FileUtils;

public abstract class AbstractPropertiesColours extends PropertiesBasePanel {

    protected Properties defaultsForTheme() {

        try {
        
            Properties defaults = FileUtils.loadPropertiesResource("org/executequery/gui/editor/resource/sql-syntax.default.profile");
            if (UIUtils.isDarkLookAndFeel()) {
   
                // catering only for the built-in dark theme
                
                defaults = FileUtils.loadPropertiesResource("org/executequery/gui/editor/resource/sql-syntax.dark.profile");
            }
            return defaults;

        } catch (IOException e) {
            
            throw new ApplicationException(e);
        }
    }

    protected Color asColour(String rgb) {
        
        return new Color(Integer.parseInt(rgb));
    }
    
}

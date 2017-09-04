/*
 * DefaultFieldLabel.java
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

package org.underworldlabs.swing;

import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JLabel;

import org.executequery.gui.GUIConstants;

public class DefaultFieldLabel extends JLabel {

    public DefaultFieldLabel() {
        
        super();
    }

    public DefaultFieldLabel(Icon image, int horizontalAlignment) {
        
        super(image, horizontalAlignment);
    }

    public DefaultFieldLabel(Icon image) {
        
        super(image);
    }

    public DefaultFieldLabel(String text, Icon icon, int horizontalAlignment) {
        
        super(text, icon, horizontalAlignment);
    }

    public DefaultFieldLabel(String text, int horizontalAlignment) {
       
        super(text, horizontalAlignment);
    }

    public DefaultFieldLabel(String text) {
        
        super(text);
    }

    public Insets getMargin() {

        return GUIConstants.DEFAULT_FIELD_MARGIN;
    }
    
    public int getHeight() {

        return Math.max(super.getHeight(), GUIConstants.DEFAULT_FIELD_HEIGHT);
    }
    
}


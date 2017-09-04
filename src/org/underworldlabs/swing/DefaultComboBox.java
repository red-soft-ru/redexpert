/*
 * DefaultComboBox.java
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

import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

import org.executequery.gui.GUIConstants;

public class DefaultComboBox extends JComboBox {

    public DefaultComboBox() {
        
        super();
    }

    public DefaultComboBox(ComboBoxModel aModel) {

        super(aModel);
    }

    public DefaultComboBox(Object[] items) {

        super(items);
    }

    public DefaultComboBox(Vector<?> items) {

        super(items);
    }

    public int getHeight() {

        return Math.max(super.getHeight(), GUIConstants.DEFAULT_FIELD_HEIGHT); 
    }
    
}







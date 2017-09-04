/*
 * DefaultInlineFieldButton.java
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

package org.executequery.gui.browser;

import javax.swing.Action;
import javax.swing.Icon;

import org.executequery.gui.GUIConstants;
import org.underworldlabs.swing.DefaultButton;

public class DefaultInlineFieldButton extends DefaultButton {

    public DefaultInlineFieldButton() {
        super();
    }

    public DefaultInlineFieldButton(Action a) {
        super(a);
    }

    public DefaultInlineFieldButton(Icon icon) {
        super(icon);
    }

    public DefaultInlineFieldButton(String text, Icon icon) {
        super(text, icon);
    }

    public DefaultInlineFieldButton(String text) {
        super(text);
    }

    public int getHeight() {        
        return GUIConstants.DEFAULT_FIELD_HEIGHT + 1;
    }
    
}







/*
 * DefaultToolbarButton.java
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
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.Icon;

public class DefaultToolbarButton extends DefaultButton {

    public DefaultToolbarButton() {
        super();
    }

    public DefaultToolbarButton(Action a) {
        super(a);
    }

    public DefaultToolbarButton(ActionListener actionListener, String text, String actionCommand) {
        super(actionListener, text, actionCommand);
    }

    public DefaultToolbarButton(Icon icon) {
        super(icon);
    }

    public DefaultToolbarButton(String text, Icon icon) {
        super(text, icon);
    }

    public DefaultToolbarButton(String text) {
        super(text);
    }

    @Override
    public Insets getInsets() {
        
        return new Insets(5, 5, 5, 5);
    }
    
}


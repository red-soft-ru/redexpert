/*
 * DefaultButton.java
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

package org.underworldlabs.swing;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

import org.executequery.gui.GUIConstants;

public class DefaultButton extends JButton {

    public DefaultButton() {

        super();
    }

    public DefaultButton(Action a) {

        super(a);
    }

    public DefaultButton(Icon icon) {

        super(icon);
    }

    public DefaultButton(String text, Icon icon) {

        super(text, icon);
    }

    public DefaultButton(String text) {

        super(text);
    }

    public DefaultButton(ActionListener actionListener, String text, String actionCommand) {

        super(text);
        addActionListener(actionListener);
        setActionCommand(actionCommand);
    }

    @Override
    public Dimension getPreferredSize() {

        Dimension preferredSize = super.getPreferredSize();
        preferredSize.height = getHeight();

        return preferredSize;
    }

    @Override
    public Insets getInsets() {
        
        return GUIConstants.DEFAULT_BUTTON_INSETS;
    }

    public int getHeight() {

        return Math.max(super.getHeight(), GUIConstants.DEFAULT_BUTTON_HEIGHT); 
    }
    
}

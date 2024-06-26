/*
 * DefaultNumberTextField.java
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

package org.executequery.gui;

import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.menu.SimpleTextComponentPopUpMenu;

import java.awt.*;

import java.awt.*;

public class DefaultNumberTextField extends NumberTextField {

    public DefaultNumberTextField() {

        super();
        addPopupMenu();
    }

    public DefaultNumberTextField(int digits) {

        super(digits);
        addPopupMenu();
    }

    private void addPopupMenu() {

        new SimpleTextComponentPopUpMenu(this);
    }

    public Insets getMargin() {

        return GUIConstants.DEFAULT_FIELD_MARGIN;
    }

    public int getHeight() {

        return super.getHeight() < GUIConstants.DEFAULT_FIELD_HEIGHT ?
                GUIConstants.DEFAULT_FIELD_HEIGHT : super.getHeight();
    }

}







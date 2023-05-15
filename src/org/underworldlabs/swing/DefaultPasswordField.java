/*
 * DefaultPasswordField.java
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

import org.executequery.gui.GUIConstants;
import org.underworldlabs.swing.menu.PasswordTextComponentPopUpMenu;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;

public class DefaultPasswordField extends JPasswordField {

    public DefaultPasswordField() {

        super();
        addPopupMenu();

    }

    public DefaultPasswordField(Document doc, String txt, int columns) {

        super(doc, txt, columns);
        addPopupMenu();
    }

    public DefaultPasswordField(int columns) {

        super(columns);
        addPopupMenu();
    }

    public DefaultPasswordField(String text, int columns) {

        super(text, columns);
        addPopupMenu();
    }

    public DefaultPasswordField(String text) {

        super(text);
        addPopupMenu();
    }

    public Insets getMargin() {

        return GUIConstants.DEFAULT_FIELD_MARGIN;
    }

    public int getHeight() {

        return Math.max(super.getHeight(), GUIConstants.DEFAULT_FIELD_HEIGHT);
    }
    private void addPopupMenu() {

        new PasswordTextComponentPopUpMenu(this);
    }

}







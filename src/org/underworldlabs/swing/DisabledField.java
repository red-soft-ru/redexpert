/*
 * DisabledField.java
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

import javax.swing.*;
import java.awt.*;

/**
 * A convenience class providing a simple component
 * to display text within a rectangle achieving the same
 * effect as displayed when disabling a <code>JTextField</code>
 * under the Metal L&F. This will provide a common 'disabled'
 * component across all L&Fs that remains lightweight and does
 * not feature or require those methods as would be available
 * using a <code>JTextField</code>.
 * <p>
 * <p>Some limitations have been deliberately introduced. The
 * component height will always be 24px. The width is determined
 * as specified or by the layout manager. In any case, This
 * component's size should not require any modification given its
 * limited design and purpose.
 *
 * @author Takis Diakoumis
 */
public class DisabledField extends JTextField {

    private static final int HEIGHT = 28;

    protected static final Insets INSETS = new Insets(5, 3, 5, 3);

    public DisabledField() {
        this("");
    }

    public DisabledField(String text) {

        super(text);
        setBackground(UIManager.getColor("Label.background"));
        setForeground(UIManager.getColor("Label.foreground"));
    }

    @Override
    public Insets getInsets() {

        return INSETS;
    }

    @Override
    public boolean isEditable() {

        return false;
    }


    
/*
    class DisabledBorder extends LineBorder {

        public DisabledBorder(Color color) {
            super(color, 1, false);
        }

        public Insets getBorderInsets() {
            return DisabledField.insets;
        }

        public Insets getBorderInsets(Component c, Insets insets) {
            return DisabledField.insets;
        }

    } // class DisabledBorder
*/

}







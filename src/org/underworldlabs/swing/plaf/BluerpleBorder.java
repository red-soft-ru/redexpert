/*
 * BluerpleBorder.java
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;

import javax.swing.border.LineBorder;

public class BluerpleBorder extends LineBorder {

    private static final Insets INSETS = new Insets(5, 5, 5, 5);
    
    public BluerpleBorder() {

        this(Color.GRAY);
    }

    public BluerpleBorder(Color colour) {
        
        super(colour);
    }
    
    @Override
    public Insets getBorderInsets(Component c) {

        return INSETS;
    }
    
    @Override
    public Insets getBorderInsets(Component c, Insets insets) {

        insets.top = INSETS.top;
        insets.left = INSETS.left;
        insets.bottom = INSETS.bottom;
        insets.right = INSETS.right;
        return insets;
    }
    
}


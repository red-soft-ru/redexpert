/*
 * OutlineDragPanel.java
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

package org.executequery.components;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * @author Takis Diakoumis
 */
public class OutlineDragPanel extends JPanel {

    public OutlineDragPanel(Rectangle bounds) {
        this(bounds, BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
    }

    public OutlineDragPanel(Rectangle bounds, Border border) {
        setOpaque(false);
        setBounds(bounds);
        setBorder(border);
    }

}












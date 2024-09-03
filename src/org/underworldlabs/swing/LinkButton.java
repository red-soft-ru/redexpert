/*
 * LinkButton.java
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

import org.underworldlabs.swing.listener.PaintMouseHandler;

import javax.swing.*;
import java.awt.*;

/**
 * Simple button behaving/looking like a hyperlink item.
 *
 * @author Takis Diakoumis
 */
public class LinkButton extends JButton {

    public LinkButton(String text) {
        super(text);
        init();
    }

    public LinkButton(Action action) {
        super(action);
        init();
    }

    private void init() {
        setFocusPainted(false);
        setContentAreaFilled(false);
        setRequestFocusEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new PaintMouseHandler(this, PaintMouseHandler.Preset.COLORED_UNDERLINE));
    }

}

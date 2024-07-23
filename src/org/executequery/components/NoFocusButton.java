/*
 * NoFocusButton.java
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

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.gui.IconManager;
import org.underworldlabs.swing.GUIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class NoFocusButton extends JButton {

    private Color borderColour;
    private Color rolloverColour;
    private boolean rollover;

    public NoFocusButton(String icon, String pressedIcon) {

        borderColour = GUIUtils.getDefaultBorderColour();
        rolloverColour = borderColour.darker();
        setFocusPainted(false);
        setBorderPainted(false);
        setMargin(Constants.EMPTY_INSETS);
        setIcon(IconManager.getIcon(icon));
        setPressedIcon(IconManager.getIcon(pressedIcon));

        try {
            setUI(new javax.swing.plaf.basic.BasicButtonUI());
        } catch (NullPointerException nullExc) {
        }

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                rollover = true;
                repaint();
            }

            public void mouseExited(MouseEvent e) {
                rollover = false;
                repaint();
            }
        });

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int height = getHeight();
        int width = getWidth();

        if (rollover) {
            g.setColor(rolloverColour);
            g.drawRect(1, 0, width - 2, height - 1);
        } else {
            g.setColor(borderColour);
            g.drawLine(1, 0, 1, height - 1);
        }

    }

    public boolean isFocusTraversable() {
        return false;
    }

    public void requestFocus() {
    }

    ;

}






/*
 * ColourChooserButton.java
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

package org.executequery.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import org.executequery.Constants;

import org.executequery.GUIUtilities;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ColourChooserButton extends JButton
                                 implements ActionListener {
    
    /** The colour border */
    private static final Color borderColour = Color.GRAY;
    /** The colour to display */
    private Color buttonColour;
    
    public ColourChooserButton() {
        super();
        buttonColour = getBackground();
        addActionListener(this);        
    }

    public ColourChooserButton(Color buttonColour) {
        this();
        this.buttonColour = buttonColour;
    }
    
    public void actionPerformed(ActionEvent e) {
        Color _buttonColour = JColorChooser.showDialog(
                                                GUIUtilities.getInFocusDialogOrWindow(),
                                                "Select Colour", buttonColour);

        if (_buttonColour != null) {
            firePropertyChange(Constants.COLOUR_PREFERENCE, buttonColour, _buttonColour);
            buttonColour = _buttonColour;
        }
        
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        g.setColor(borderColour);
        g.drawRect(5, 5, getWidth() - 12, getHeight() - 12);
        
        g.setColor(buttonColour);
        g.fillRect(6, 6, getWidth() - 13, getHeight() - 13);
    }
    
    public void setColour(Color _buttonColour) {
        buttonColour = _buttonColour;
        repaint();
    }
    
    public Color getColour() {
        return buttonColour;
    }
    
}















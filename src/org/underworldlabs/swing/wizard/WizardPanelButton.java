/*
 * WizardPanelButton.java
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

package org.underworldlabs.swing.wizard;

import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JButton;

public class WizardPanelButton extends JButton {

    public static final int DEFAULT_WIDTH = 75;
    
    public static final int DEFAULT_HEIGHT = 30;
    
    public static final Insets DEFAULT_INSETS = new Insets(2, 2, 2, 2);
    
    public WizardPanelButton(String text) {
        super(text);
        prepare();
    }

    @Override
    public Dimension getPreferredSize() {

        if (!isPreferredSizeSet()) {

            Dimension preferredSizeUI = getUI().getPreferredSize(this);
            
            Dimension size = new Dimension(
                    Math.max(preferredSizeUI.width, DEFAULT_WIDTH), 
                    Math.max(preferredSizeUI.height, DEFAULT_HEIGHT));
    
            setPreferredSize(size);
            setMinimumSize(size);
        }

        return super.getPreferredSize();
    }
    
    private void prepare() {
        
        setMargin(DEFAULT_INSETS);
    }

}








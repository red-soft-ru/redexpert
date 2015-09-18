/*
 * DefaultPanelButton.java
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

package org.executequery.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

public class DefaultPanelButton extends JButton {

    private static final int DEFAULT_WIDTH = 75;
    
    private static final int DEFAULT_HEIGHT = GUIConstants.DEFAULT_BUTTON_HEIGHT;
    
    private static final Insets DEFAULT_INSETS = new Insets(2, 2, 2, 2);
    
    private int defaultWidth;
    
    private int defaultHeight;
    
    public DefaultPanelButton() {
        super();
    }

    public DefaultPanelButton(Action a) {
        super(a);
    }

    public DefaultPanelButton(Icon icon) {
        super(icon);
    }

    public DefaultPanelButton(String text, Icon icon) {
        super(text, icon);
    }

    public DefaultPanelButton(String text) {
        super(text);
    }

    public DefaultPanelButton(String text, String actionCommand) {
        super(text);
        setActionCommand(actionCommand);
    }    
    
    public DefaultPanelButton(ActionListener actionListener, String text, String actionCommand) {
        super(text);
        addActionListener(actionListener);
        setActionCommand(actionCommand);
    }    
    
    public Dimension getPreferredSize() {

        validateDimension();
        return super.getPreferredSize();
    }

    private void validateDimension() {

        if (!isPreferredSizeSet()) {

            Dimension preferredSizeUI = getUI().getPreferredSize(this);
            
            Dimension size = new Dimension(
                    Math.max(preferredSizeUI.width, getDefaultWidth()), 
                    Math.max(preferredSizeUI.height, getDefaultHeight()));
    
            setPreferredSize(size);
            setMinimumSize(size);
        }

    }
    
    public Insets getMargin() {

        return DEFAULT_INSETS;
    }
    
    public int getDefaultWidth() {

        if (defaultWidth == 0) {
            
            defaultWidth = DEFAULT_WIDTH;
        }

        return defaultWidth;
    }

    public void setDefaultWidth(int defaultWidth) {
        this.defaultWidth = defaultWidth;
    }

    public int getDefaultHeight() {
        
        if (defaultHeight == 0) {
            
            defaultHeight = DEFAULT_HEIGHT;
        }
        
        return defaultHeight;
    }

    public void setDefaultHeight(int defaultHeight) {
        this.defaultHeight = defaultHeight;
    }

}







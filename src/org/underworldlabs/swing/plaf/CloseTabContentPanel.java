/*
 * CloseTabContentPanel.java
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

package org.underworldlabs.swing.plaf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class CloseTabContentPanel extends JPanel {
    
    /** the highlight border width */
    private static final int BORDER_WIDTH = 4;
    
    /** The added tab panel border */
    private static Border border;

    /** the active color for the tab border */
    private static Color activeColor;
    
    /** the displayed component */
    private Component component;
    
    /** the associated menu item */
    private TabMenuItem tabMenuItem;
    
    public CloseTabContentPanel(int tabPlacement, Component component) {
        this(tabPlacement, component, BORDER_WIDTH);
    }
    
    /** Creates a new instance of CloseTabContentPanel */
    public CloseTabContentPanel(int tabPlacement, Component component, int borderWidth) {
        super(new BorderLayout());
        
        this.component = component;
        
        if (activeColor == null) {
            activeColor = UIManager.getColor("InternalFrame.activeTitleBackground");
        }
        
        if (border == null) {
            switch (tabPlacement) {
                case JTabbedPane.TOP:
                    border = BorderFactory.createMatteBorder(
                            borderWidth, 0, 0, 0, activeColor);
                    break;
                case JTabbedPane.BOTTOM:
                    border = BorderFactory.createMatteBorder(
                            0, 0, borderWidth, 0, activeColor);
                    break;
            }
        }
        add(component, BorderLayout.CENTER);
        setBorder(border);
    }

    public Component getDisplayComponent() {
        return component;
    }
    
    public TabMenuItem getTabMenuItem() {
        return tabMenuItem;
    }

    public void setTabMenuItem(TabMenuItem tabMenuItem) {
        this.tabMenuItem = tabMenuItem;
    }

}







/*
 * ActionPanel.java
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

import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import javax.swing.JPanel;

/**
 * Base panel with default action listener implementation using reflection.
 *
 * @author   Takis Diakoumis
 */
public abstract class ActionPanel extends JPanel
                                  implements ActionListener {
    
    private Object[] args;
    private Class<?>[] argTypes;
    
    public ActionPanel() {
        super();
    }
    
    public ActionPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
    }
    
    public ActionPanel(LayoutManager layout) {
        super(layout);
    }
    
    public ActionPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }
    
    public void actionPerformed(ActionEvent e) {

        String command = e.getActionCommand();
        
        try {

            if (argTypes == null) {
                
                argTypes = new Class[0];
            }

            Method method = getClass().getMethod(command, argTypes);
            
            if (args == null) {
        
                args = new Object[0];
            }

            method.invoke(this, args);
        
        } catch (Exception ex) {
          
            ex.printStackTrace();
        }
        
    }
    
}


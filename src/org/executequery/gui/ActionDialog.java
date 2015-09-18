/*
 * ActionDialog.java
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;

import javax.swing.JPanel;

/**
 * Extension to base dialog implementing ActionListener for 
 * reflective use of action commands on components (similar to
 * org.underworldlabs.swing.ActionPanel)
 *  
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ActionDialog extends BaseDialog 
                          implements ActionListener {
    
    private static Object[] args;
    private static Class<?>[] argTypes;

    public ActionDialog(String name, boolean modal) {
        super(name, modal);
    }

    public ActionDialog(String name, boolean modal, boolean resizeable) {
        super(name, modal, resizeable);
    }

    public ActionDialog(String name, boolean modal, JPanel panel) {
        super(name, modal, panel);
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







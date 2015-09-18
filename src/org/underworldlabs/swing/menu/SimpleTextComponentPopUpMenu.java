/*
 * SimpleTextComponentPopUpMenu.java
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

package org.underworldlabs.swing.menu;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import org.underworldlabs.swing.actions.ActionBuilder;
import org.underworldlabs.swing.actions.ReflectiveAction;

/** 
 * The text utilities popup menu function
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class SimpleTextComponentPopUpMenu extends JPopupMenu {
    
    /** the action listener */
    private ReflectiveAction reflectiveAction;

    /** the text component this popup belongs to */
    private JTextComponent textComponent;
    
    public SimpleTextComponentPopUpMenu(JTextComponent textComponent) {
        
        // create the listener
        reflectiveAction = new ReflectiveAction(this);

        this.textComponent = textComponent;
        textComponent.addMouseListener(new PopupListener(this));
        
        // the menu label text
        String[] menuLabels = {"Cut", "Copy", "Paste"};
        
        // cached actions from which to retrieve common accels and mnemonics
        String[] actionNames = {"cut-command", "copy-command", "paste-command"};
        
        // action command settings to map to method names in this class
        String[] actionCommands = {"cut", "copy", "paste"};
        
        for (int i = 0; i < menuLabels.length; i++) {

            add(createMenuItem(menuLabels[i], actionNames[i], actionCommands[i]));
        }

    }

    /**
     * Executes the cut action on the registered text component.
     */
    public void cut(ActionEvent e) {
        textComponent.cut();
    }

    /**
     * Executes the copy action on the registered text component.
     */
    public void copy(ActionEvent e) {
        textComponent.copy();
    }

    /**
     * Executes the paste action on the registered text component.
     */
    public void paste(ActionEvent e) {
        textComponent.paste();
    }

    private JMenuItem createMenuItem(String text, 
                                     String actionName, 
                                     String actionCommand) {

        JMenuItem menuItem = MenuItemFactory.createMenuItem(text);
        Action action = ActionBuilder.get(actionName);
        Object object = action.getValue(Action.ACCELERATOR_KEY);
        if (object != null) {
            menuItem.setAccelerator((KeyStroke)object);
        }
        
        object = action.getValue(Action.MNEMONIC_KEY);
        if (object != null) {
            menuItem.setMnemonic(((Integer)object).intValue());
        }
        
        menuItem.setActionCommand(actionCommand);
        menuItem.addActionListener(reflectiveAction);
        return menuItem;
    }
    
    
    class PopupListener extends MouseAdapter {
        
        private JPopupMenu popup;
        
        public PopupListener(JPopupMenu popup) {
            this.popup = popup;
        }
        
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
        
    } // class PopupListener
    
}







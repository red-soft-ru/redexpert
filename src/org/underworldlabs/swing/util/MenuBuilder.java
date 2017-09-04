/*
 * MenuBuilder.java
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

package org.underworldlabs.swing.util;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.ImageIcon;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.Action;

/**
 * A helper class for creating menu items.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class MenuBuilder {
    
    /** A <code>JMenuItem</code> menu item */
    public static final int ITEM_PLAIN = 0;
    
    /** A <code>JCheckBoxMenuItem</code> menu item */
    public static final int ITEM_CHECK = 1;
    
    /** A <code>JRadioButtonMenuItem</code> menu item */
    public static final int ITEM_RADIO = 2;
    
    /**
     * Creates a new <code>MenuBuilder</code> object
     */
    public MenuBuilder() {}
    
    public JMenuItem createMenuItem(JMenu menu, 
                                    int iType, 
                                    String sText,
                                    ImageIcon image, 
                                    int acceleratorKey,
                                    String sToolTip, 
                                    Action a) {
        return createMenuItem(menu, 
                              iType, 
                              sText, 
                              image, 
                              acceleratorKey, 
                              sToolTip,
                              a,
                              null);
    }
    /**
     * Creates the required menu item.<br><p>
     * The menu item is associated with a <code>JMenu</code>,
     * a name, an image icon, a key mnemonic and a tool tip.
     *
     * @param menu The <code>JMenu</code> that this menu item
     *             will be associated with
     * @param iType The type of menu item to be constructed
     *              (see public fields)
     * @param sText The name of this menu item
     * @param image The image icon to be displayed with this menu item
     * @param acceleratorKey The keyboard mnemonic associated with the
     *                       menu item
     * @param sToolTip The tool tip associated with the menu item
     *
     * @return The constructed <code>JMenuItem</code>
     */
    public JMenuItem createMenuItem(JMenu menu, 
                                    int iType, 
                                    String sText,
                                    ImageIcon image, 
                                    int acceleratorKey,
                                    String sToolTip, 
                                    Action a, 
                                    String actionCommand) {

        JMenuItem menuItem = createMenuItem(iType); 
        
        if(a != null) {
            
            menuItem.setAction(a);
        }
        
        if(acceleratorKey > 0) {
            
            menuItem.setMnemonic(acceleratorKey);
        }
        
        if(sToolTip != null) {
            
            menuItem.setToolTipText(sToolTip);
        }

        if (actionCommand != null) {

            menuItem.setActionCommand(actionCommand);
        }
        
        menuItem.setIcon(image);
        menuItem.setText(sText);
        
        if (menu != null) {

            menu.add(menuItem);
        }

        return menuItem;
    }

    private JMenuItem createMenuItem(int iType) {

        switch(iType) {

            case ITEM_RADIO:                
                return new JRadioButtonMenuItem();

            case ITEM_CHECK:
                return new JCheckBoxMenuItem();

            default:
                return new JMenuItem();

        }

    }
    
    public JMenuItem createMenuItem(JMenu menu, int iType, Action action) {
        return createMenuItem(
                    menu, 
                    iType, 
                    (String)action.getValue(Action.NAME), 
                    null,
                    ((Integer)action.getValue(Action.MNEMONIC_KEY)).intValue(),
                    (String)action.getValue(Action.SHORT_DESCRIPTION), 
                    action, 
                    null);
    }
    
    public JMenuItem createMenuItem(JMenu menu, 
                                    String sText,
                                    int iType, 
                                    String sToolTip) {
        return createMenuItem(menu, 
                              iType, 
                              sText, 
                              null, 
                              -1, 
                              sToolTip, 
                              null);
    }
    
    public JMenuItem createMenuItem(JMenu menu, 
                                    int iType, 
                                    int acceleratorKey,
                                    String sToolTip, 
                                    Action a) {
        return createMenuItem(menu, 
                              iType, 
                              null, 
                              null, 
                              acceleratorKey, 
                              sToolTip, 
                              a,
                              null);
    }
    
    public JMenuItem createMenuItem(JMenu menu, int iType,
                                    String sToolTip, Action a) {
        return createMenuItem(menu, 
                              iType, 
                              null, 
                              null, 
                              0, 
                              sToolTip, 
                              a,
                              null);
    }
    
    /**
     * Creates the required menu.<br>
     * The menu item is initialised with a name and
     * key mnemonic.
     *
     * @param sText The name of this menu
     * @param acceleratorKey The keyboard mnemonic associated
     *                       with this menu
     *
     * @return The constructed <code>JMenu</code>
     */
    public JMenu createMenu(String sText, int acceleratorKey) {
        JMenu menu = new JMenu(sText);
        //    menu.setOpaque(true);
        if(acceleratorKey > 0) {
            menu.setMnemonic(acceleratorKey);
        }        
        return menu;
    }
    
}




















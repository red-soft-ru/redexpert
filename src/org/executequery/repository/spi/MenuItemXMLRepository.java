/*
 * MenuItemXMLRepository.java
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

package org.executequery.repository.spi;

import java.util.ArrayList;
import java.util.List;

import org.executequery.ExecuteQuerySystemError;
import org.executequery.gui.menu.MenuItem;
import org.executequery.log.Log;
import org.executequery.repository.MenuItemRepository;
import org.executequery.repository.RepositoryException;
import org.xml.sax.Attributes;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class MenuItemXMLRepository extends AbstractXMLRepository<MenuItem>
                                   implements MenuItemRepository {

    private static final String RESOURCE_PATH = "org/executequery/menus.xml";
    
    public List<MenuItem> getMenuItems() {

        try {
     
            return (List<MenuItem>)readResource(resourcePath(), new MenuItemHandler());

        } catch (RepositoryException e) {

            Log.error("Error loading menu items from file.", e);
            
            throw new ExecuteQuerySystemError(e.getMessage());
        }

    }

    private String resourcePath() {
        return RESOURCE_PATH;
    }

    class MenuItemHandler 
        extends AbstractXMLRepositoryHandler<MenuItem> {
    
        private List<MenuItem> menuItems;
    
        private MenuItem lastParent;
        
        MenuItemHandler() {
            
            menuItems = new ArrayList<MenuItem>();
        }
    
        public void startElement(String nameSpaceURI, String localName,
                                 String qName, Attributes attrs) {
    
            contents().reset();
            
            MenuItem menuItem = new MenuItem();
            menuItem.setParent(lastParent);

            menuItem.setMnemonic(attrs.getValue(MNEMONIC));
            menuItem.setName(attrs.getValue(NAME));

            if (attrs.getValue(INDEX) != null) {
                
                menuItem.setIndex(Integer.parseInt(attrs.getValue(INDEX)));
            }
            
            if (localName.equals(MENU_ITEM)) {
    
                menuItem.setId(attrs.getValue(ID));
                menuItem.setAcceleratorKey(attrs.getValue(ACCEL_KEY));
                menuItem.setActionCommand(attrs.getValue(ACTION_COMMAND));
                menuItem.setImplementingClass(attrs.getValue(CLASS));
                menuItem.setPropertyKey(attrs.getValue(PROPERTY_KEY));
                menuItem.setToolTip(attrs.getValue(TOOL_TIP));

                lastParent.add(menuItem);

            } else if (localName.equals(MENU)) {
                
                menuItem.setImplementingClass(attrs.getValue(CLASS));
                
                if (lastParent != null) {
                    
                    lastParent.add(menuItem);
                }

                lastParent = menuItem;

            }
    
        }
        
        public void endElement(String nameSpaceURI, String localName,
                String qName) {

            if (localName.equals(MENU)) {
            
                if (lastParent.hasParent()) {

                    lastParent = lastParent.getParent();
                    
                } else {
                    
                    menuItems.add(lastParent);

                    lastParent = null;
                }
                
            }

        }

        public List<MenuItem> getRepositoryItemsList() {

            return menuItems;
        }
    
        private static final String MENU = "menu";
        private static final String MENU_ITEM = "menu-item";
        private static final String NAME = "name";
        private static final String CLASS = "class";
        private static final String ID = "id";
        private static final String INDEX = "index";
        private static final String MNEMONIC = "mnemonic";
        private static final String TOOL_TIP = "tool-tip";
        private static final String ACTION_COMMAND = "action-command";
        private static final String ACCEL_KEY = "accel-key";
        private static final String PROPERTY_KEY = "property-key";

    } // MenuItemHandler


}











/*
 * ConnectionsMenu.java
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

package org.executequery.gui.menu;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenuItem;

import org.executequery.EventMediator;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionRepositoryEvent;
import org.executequery.event.ConnectionRepositoryListener;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.actions.ActionBuilder;
import org.underworldlabs.swing.menu.MainMenu;
import org.underworldlabs.swing.menu.MainMenuItem;
import org.underworldlabs.swing.util.MenuScroller;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1775 $
 * @date     $Date: 2017-08-27 22:51:41 +1000 (Sun, 27 Aug 2017) $
 */
public class ConnectionsMenu extends MainMenu 
                             implements ConnectionRepositoryListener {

    private List<JMenuItem> connectionMenuItemList;
    
    public ConnectionsMenu() {
        
        super();
        
        MenuScroller.setScrollerFor(this, 20, 100, 3, 0);
        
        createConnectionMenu();
        EventMediator.registerListener(this);
    }

    public boolean canHandleEvent(ApplicationEvent event) {
        
        return (event instanceof ConnectionRepositoryEvent);
    }

    public void connectionModified(
            ConnectionRepositoryEvent connectionRepositoryEvent) {

        reloadConnectionMenu();
    }
    
    public void connectionAdded(
            ConnectionRepositoryEvent connectionRepositoryEvent) {

        reloadConnectionMenu();
    }

    public void connectionRemoved(
            ConnectionRepositoryEvent connectionRepositoryEvent) {

        reloadConnectionMenu();
    }

    private void reloadConnectionMenu() {

        createConnectionMenu();
    }

    private void createConnectionMenu() {

        if (connectionMenuItemList != null) {
        
            for (JMenuItem menuItem : connectionMenuItemList) {

                remove(menuItem);
            }
            
        }

        resetConnectionMenuItemList();

        for (DatabaseConnection connection : connections()) {

            String connectionName = connection.getName();

            JMenuItem menuItem = createSavedConnectionMenuItem();
            menuItem.setText(connectionName);
            menuItem.setActionCommand(connectionName);

            add(menuItem);
            
            connectionMenuItemList.add(menuItem);
        }

    }

    private List<DatabaseConnection> connections() {
        
        return ((DatabaseConnectionRepository)RepositoryCache.load(
                DatabaseConnectionRepository.REPOSITORY_ID)).findAll();
    }
    
    private JMenuItem createSavedConnectionMenuItem() {

        JMenuItem menuItem = new MainMenuItem(loadAction());
        
        menuItem.setIcon(null);
        menuItem.setMnemonic(0);
        menuItem.setAccelerator(null);
        
        return menuItem;
    }

    private void resetConnectionMenuItemList() {

        if (connectionMenuItemList == null) {
            
            connectionMenuItemList = new ArrayList<JMenuItem>();
            
        } else {
            
            connectionMenuItemList.clear();
        }
    }
    
    private Action loadAction() {

        return ActionBuilder.get("connect-command");
    }

}


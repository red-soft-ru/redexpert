/*
 * ConnectCommand.java
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

package org.executequery.actions.databasecommands;

import java.awt.event.ActionEvent;

import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.actions.BaseCommand;
import org.underworldlabs.util.MiscUtils;

/** <p>Executes the Database | Connect... | New Connection command.
 *
 *  @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ConnectCommand extends OpenFrameCommand implements BaseCommand {
    
    public void execute(ActionEvent e) {
        
        GUIUtilities.ensureDockedTabVisible(ConnectionsTreePanel.PROPERTY_KEY);        
        ConnectionsTreePanel panel = connectionsPanel();

        String command = e.getActionCommand();
        if (MiscUtils.isNull(command) || "New Connection".equals(command)) {
            
            panel.newConnection();

        } else {

            DatabaseConnection dc = loadConnection(command);
            panel.setSelectedConnection(dc);
        }

    }

    private DatabaseConnection loadConnection(String name) {

        return databaseConnectionRepository().findByName(name);
    }

    private DatabaseConnectionRepository databaseConnectionRepository() {

        return (DatabaseConnectionRepository)RepositoryCache.load(
                    DatabaseConnectionRepository.REPOSITORY_ID);        
    }

    private ConnectionsTreePanel connectionsPanel() {

        return (ConnectionsTreePanel)GUIUtilities.
            getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY);
    }
    
}


/*
 * AbstractImportExportPanelOne.java
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

package org.executequery.gui.importexport;

import java.awt.LayoutManager;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.WidgetFactory;
import org.underworldlabs.swing.DynamicComboBoxModel;

public abstract class AbstractImportExportPanelOne extends AbstractImportExportPanel {

    private JComboBox connectionsCombo;

    public AbstractImportExportPanelOne(LayoutManager layout) {
        
        super(layout, null);
    }

    protected final JComboBox connectionsCombo() {

        if (connectionsCombo == null) {
            Vector<DatabaseConnection> connections = ConnectionManager.getActiveConnections();
            ComboBoxModel connectionsModel = new DynamicComboBoxModel(connections);
            connectionsCombo = WidgetFactory.createComboBox(connectionsModel);            
        }
        
        return connectionsCombo;
    }

    /**
     * Sets the connection selection to that specified.
     *
     * @param dc - the connection to select
     */
    public void setDatabaseConnection(DatabaseConnection dc) {
        connectionsCombo.setSelectedItem(dc);
    }

    /**
     * Returns the selected database connection properties object.
     *
     * @return the connection properties object
     */
    public DatabaseConnection getDatabaseConnection() {
        return (DatabaseConnection)connectionsCombo.getSelectedItem();
    }
    
}








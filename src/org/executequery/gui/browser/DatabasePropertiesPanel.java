/*
 * DatabasePropertiesPanel.java
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

package org.executequery.gui.browser;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.executequery.gui.SortableColumnsTable;
import org.underworldlabs.swing.table.PropertyWrapperModel;

/**
 * Simple panel displaying database meta data properties.
 *
 * @author   Takis Diakoumis
 */
public class DatabasePropertiesPanel extends ConnectionPropertiesPanel {
    
    /** table model */
    private PropertyWrapperModel model;
    
    /** the table */
    private JTable table;
    
    public DatabasePropertiesPanel() {
        
        super(new GridBagLayout());
        init();
    }
    
    private void init() {

        model = new PropertyWrapperModel(PropertyWrapperModel.SORT_BY_KEY);

        table = new SortableColumnsTable(model);
        setTableProperties(table);

        GridBagConstraints gbc = new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                                        GridBagConstraints.SOUTHEAST, 
                                        GridBagConstraints.BOTH,
                                        new Insets(5, 5, 5, 5), 0, 0);

        add(new JScrollPane(table), gbc); 
    }
    
    public void setDatabaseProperties(Map<Object, Object> properties) {

        model.setValues(properties, true);
    }

    public JTable getTable() {
        
        return table;
    }
    
}






/*
 * DataTypesPanel.java
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

package org.executequery.gui.browser;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.ResultSet;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.executequery.gui.SortableColumnsTable;
import org.executequery.gui.resultset.ResultSetTableModel;

/**
 * Displays data types from the current connection.
 * 
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class DataTypesPanel extends ConnectionPropertiesPanel {
    
    private JTable table;
    
    /** the data type table model from the result set */
    private ResultSetTableModel model;
    
    public DataTypesPanel() {

        super(new GridBagLayout());

        init();
    }
    
    private void init() {
        
        model = new ResultSetTableModel();
        table = new SortableColumnsTable(model);
        
        setTableProperties(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        GridBagConstraints gbc = new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                                        GridBagConstraints.SOUTHEAST, 
                                        GridBagConstraints.BOTH,
                                        new Insets(5, 5, 5, 5), 0, 0);

        add(new JScrollPane(table), gbc); 
    }
    
    public void setDataTypes(ResultSet rs) {

        model.createTable(rs);
        model.fireTableStructureChanged();
    }
    
}











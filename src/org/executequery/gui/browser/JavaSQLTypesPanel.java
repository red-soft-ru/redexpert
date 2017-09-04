/*
 * JavaSQLTypesPanel.java
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
import java.lang.reflect.Field;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.executequery.gui.SortableColumnsTable;
import org.underworldlabs.swing.table.AbstractSortableTableModel;
import org.underworldlabs.swing.table.SortableTableModel;

/**
 * Displays java.sql.Types in a table with full properties
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class JavaSQLTypesPanel extends ConnectionPropertiesPanel {
    
    private JTable table;
    
    /** Creates a new instance of JavaSQLTypesPanel */
    public JavaSQLTypesPanel() {

        super(new GridBagLayout());

        init();
    }
    
    private void init() {

        SortableTableModel model = createModel();

        if (model == null) {

            add(new JLabel("Not Available"));
            return;
        }
        
        table = new SortableColumnsTable(model);
        setTableProperties(table);

        GridBagConstraints gbc = new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                                        GridBagConstraints.SOUTHEAST, 
                                        GridBagConstraints.BOTH,
                                        new Insets(5, 5, 5, 5), 0, 0);

        add(new JScrollPane(table), gbc); 
    }

    private SortableTableModel createModel() {

        Field[] fields = java.sql.Types.class.getDeclaredFields();

        String[][] values = new String[fields.length][2];
        
        try {

            for (int i = 0; i < fields.length; i++) {

                String fieldName = fields[i].getName();

                values[i][0] = fieldName;
                values[i][1] = Integer.toString(fields[i].getInt(fieldName));
            }

            return new JavaSQLTypesModel(values);
            
        } catch (IllegalAccessException e) {

            e.printStackTrace();
        }

        return null;
    }

    class JavaSQLTypesModel extends AbstractSortableTableModel {

        private String[][] values;
        
        private String[] header = new String[]{"Name", "Value"};
        
        JavaSQLTypesModel(String[][] values) {

            this.values = values;
        }
        
        public int getRowCount() {
            
            if (hasValues()) {

                return values.length;
            }

            return 0;
        }
        
        public int getColumnCount() {
            
            return header.length;
        }
        
        public String getColumnName(int col) {

            return header[col];
        }
        
        public Object getValueAt(int row, int col) {
            
            if (hasValues()) {

                return values[row][col];
            }
            
            return null; 
        }
        
        public boolean isCellEditable() {

            return false;
        }
        
        private boolean hasValues() {

            return values != null;
        }

    }

}







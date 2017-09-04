/*
 * SystemPropertiesPanel.java
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

package org.executequery.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.underworldlabs.swing.table.PropertyWrapperModel;

/**
 * Simple system properties panel displaying a table
 * with the values from <code>System.getProperties()</code>.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class SystemPropertiesPanel extends JPanel {
    
    private SortableColumnsTable table;

    public SystemPropertiesPanel() {

        super(new GridBagLayout());
        PropertyWrapperModel model = new PropertyWrapperModel(
                                            System.getProperties(), 
                                            PropertyWrapperModel.SORT_BY_KEY);
        
        table = new SortableColumnsTable(model);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setCellRenderer(new PropertiesTableCellRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new PropertiesTableCellRenderer());
        
        JScrollPane scroller = new JScrollPane(table);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets.top = 1;
        gbc.insets.bottom = 3;
        gbc.insets.left = 3;
        gbc.insets.right = 3;
        gbc.insets.top = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        add(scroller, gbc);
    }
 
    public void reload() {
        
        table.setModel(new PropertyWrapperModel(
                                            System.getProperties(), 
                                            PropertyWrapperModel.SORT_BY_KEY));
        table.resetSorter();
    }
    
    
    private class PropertiesTableCellRenderer extends DefaultTableCellRenderer {

        public PropertiesTableCellRenderer() {}
        
        public Component getTableCellRendererComponent(JTable table, 
                                                       Object value, 
                                                       boolean isSelected, 
                                                       boolean hasFocus, 
                                                       int row, 
                                                       int column) {
            
            if (value != null) {

                setToolTipText(value.toString());
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
    
}






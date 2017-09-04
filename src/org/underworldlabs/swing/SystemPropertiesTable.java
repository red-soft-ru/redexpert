/*
 * SystemPropertiesTable.java
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

package org.underworldlabs.swing;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.table.DefaultTableCellRenderer;
import org.underworldlabs.swing.table.DefaultTableHeaderRenderer;
import org.underworldlabs.swing.table.PropertyWrapperModel;

/**
 * Simple system properties table with the values 
 * from <code>System.getProperties()</code>.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class SystemPropertiesTable extends JTable {
    
    /** table model */
    private PropertyWrapperModel model;
    
    /** Creates a new instance of SystemPropertiesTable */
    public SystemPropertiesTable() {
        if (UIManager.getLookAndFeel() instanceof MetalLookAndFeel) {
            getTableHeader().setDefaultRenderer(new DefaultTableHeaderRenderer());
        }

        model = new PropertyWrapperModel(System.getProperties(), 
                                         PropertyWrapperModel.SORT_BY_KEY);
        setModel(model);
        
        getTableHeader().setReorderingAllowed(false);
        getColumnModel().getColumn(0).setCellRenderer(new PropertiesTableCellRenderer());
        getColumnModel().getColumn(1).setCellRenderer(new PropertiesTableCellRenderer());
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
                String toolTip = value.toString();
                setToolTipText(toolTip);
            }
            return super.getTableCellRendererComponent(
                            table, value, isSelected, hasFocus, row, column);
        }
    }
    
}













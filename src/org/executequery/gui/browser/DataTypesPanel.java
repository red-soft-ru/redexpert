/*
 * DataTypesPanel.java
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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.ResultSet;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.executequery.gui.SortableColumnsTable;
import org.executequery.gui.resultset.ResultSetTableModel;
import org.underworldlabs.swing.plaf.UIUtils;

/**
 * Displays data types from the current connection.
 * 
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class DataTypesPanel extends ConnectionPropertiesPanel {
    
    private ResultSetTableModel model;

    private JScrollPane scrollPane;
    
    public DataTypesPanel() {

        super(new GridBagLayout());
        init();
    }
    
    private void init() {
        
        model = new ResultSetTableModel();
        JTable table = new SortableColumnsTable(model);
        
        setTableProperties(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        scrollPane = new JScrollPane(table);
        addScrollPane();
    }

    public void setDataTypeError(String message) {
        
        remove(scrollPane);

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><p><center>Error populating data types from the current connection.");
        sb.append("</center></p><p><center>");
        sb.append(message);
        sb.append("</center></p></body></html>");

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 20, 10, 20);
        panel.add(new JLabel(sb.toString()), gbc);
        panel.setBorder(UIUtils.getDefaultLineBorder());

        add(panel, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0));
        
        validate();
        repaint();
    }
    
    public void setDataTypes(ResultSet rs) {

        addScrollPane();
        model.createTable(rs);
        model.fireTableStructureChanged();
        
        validate();
        repaint();
    }
    
    private void addScrollPane() {
        
        if (!contains(scrollPane)) {

            removeAll();
            add(scrollPane, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                    GridBagConstraints.SOUTHEAST, 
                    GridBagConstraints.BOTH,
                    new Insets(5, 5, 5, 5), 0, 0));
        }
    
    }

    private boolean contains(Component component) {

        for (Component c : getComponents()) {
            
            if (c == component) {
                
                return true;
            }
        }
        
        return false;
    }
    
}



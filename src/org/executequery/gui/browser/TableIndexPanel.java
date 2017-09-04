/*
 * TableIndexPanel.java
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

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.executequery.gui.DefaultTable;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class TableIndexPanel extends JPanel {
    
    /** the table display model */
    private ColumnIndexTableModel model;
    
    /** the table display */
    private JTable table;
    
    public TableIndexPanel() {
        super(new BorderLayout());
        try {
            init();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void init() throws Exception {
        model = new ColumnIndexTableModel();
        table = new DefaultTable(model);
        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.addMouseListener(new MouseHandler());

        initColumnProperties();

        add(new JScrollPane(table), BorderLayout.CENTER);
    }
    
    private void initColumnProperties() {
        TableColumnModel tcm = table.getColumnModel();
        TableColumn col = tcm.getColumn(0);
        col.setPreferredWidth(25);
        col = tcm.getColumn(2);
        col.setPreferredWidth(150);
        col = tcm.getColumn(3);
        col.setPreferredWidth(90);        
    }
    
    public void insertBefore() {}
    public void insertAfter() {}
    public void deleteRow() {}
    public void setSQLText() {}
    public void setSQLText(String values, int type) {}
    
    /** 
     * Retrieves the currently selected/created table name.
     *
     * @return the table name
     */
    public String getTableName() {
        return null;
    }

    public void moveColumnUp() {}
    public void moveColumnDown() {}

    
    
    private class MouseHandler extends MouseAdapter {
        public MouseHandler() {}
        public void mouseClicked(MouseEvent e) {
            int mouseX = e.getX();
            int mouseY = e.getY();
            
            int col = table.columnAtPoint(new Point(mouseX, mouseY));
            // if we haven't clicked on column 0 - bail
            if (col != 0) {
                return;
            }

            int row = table.rowAtPoint(new Point(mouseX, mouseY));
            Object object = model.getValueAt(row, col);
            if (object == null) {
                return;
            }

            ColumnIndex index = (ColumnIndex)object;            
            // if this constraint is marked to be dropped, unmark it
            if (index.isMarkedDeleted()) {
                index.setMarkedDeleted(false);
                //tempSqlText.remove(DROP_CONSTRAINT + row);
                model.fireTableRowsUpdated(row, row);
                //generateSQL();
                //creator.setSQLText();                
            }

        }
    }

}





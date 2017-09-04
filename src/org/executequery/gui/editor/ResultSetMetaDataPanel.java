/*
 * ResultSetMetaDataPanel.java
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

package org.executequery.gui.editor;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.executequery.gui.DefaultTable;
import org.executequery.gui.resultset.ResultSetMetaDataTableModel;
import org.executequery.util.UserProperties;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class ResultSetMetaDataPanel extends JPanel {
    
    public static final String TITLE = "Result Set Meta Data";
    
    private JTable table;
    
    public ResultSetMetaDataPanel() {

        super(new BorderLayout());
        
        try {

            init();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void init() {

        table = new DefaultTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.setRowHeight(rowHeight());

        JScrollPane scroller = new JScrollPane(table);
        scroller.setBorder(null);

        add(scroller, BorderLayout.CENTER);
    }

    private int rowHeight() {

        return UserProperties.getInstance().getIntProperty(
                "results.table.column.height");
    }
    
    public void setMetaData(ResultSetMetaDataTableModel model) {
        
        table.setModel(model);
    }

}










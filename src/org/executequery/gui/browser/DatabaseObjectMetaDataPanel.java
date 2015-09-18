/*
 * DatabaseObjectMetaDataPanel.java
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

import java.awt.BorderLayout;
import java.sql.ResultSet;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import org.executequery.gui.DefaultTable;
import org.executequery.gui.editor.ResultSetTableContainer;
import org.executequery.gui.resultset.ResultSetTableModel;
import org.underworldlabs.swing.table.TableSorter;

public class DatabaseObjectMetaDataPanel extends JPanel implements ResultSetTableContainer {

    private JTable table;
    private ResultSetTableModel tableModel;

    public DatabaseObjectMetaDataPanel() {

        super(new BorderLayout());
        
        table = new DefaultTable();
        tableModel = new ResultSetTableModel();
        table.setModel(new TableSorter(tableModel, table.getTableHeader()));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        setBorder(BorderFactory.createTitledBorder("Database object Meta Data"));
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void setData(ResultSet resultSet) {

        tableModel.createTable(resultSet);
    }
    
    public JTable getTable() {
        
        return table;
    }
    
    public boolean isTransposeAvailable() {

        return false;
    }

    public void transposeRow(TableModel tableModel, int row) {}
    
}




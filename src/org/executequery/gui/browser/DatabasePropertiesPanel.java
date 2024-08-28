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

import org.executequery.gui.SortableColumnsTable;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.table.PropertyWrapperModel;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Simple panel displaying database meta data properties.
 *
 * @author Takis Diakoumis
 */
public class DatabasePropertiesPanel extends ConnectionPropertiesPanel {

    private PropertyWrapperModel model;
    private JTable table;

    public DatabasePropertiesPanel() {
        super(new GridBagLayout());
        init();
    }

    private void init() {

        model = new PropertyWrapperModel();

        table = new SortableColumnsTable(model);
        table.setEnabled(false);
        setTableProperties(table);

        add(new JScrollPane(table), new GridBagHelper().fillBoth().spanX().spanY().get());
    }

    public void setDatabaseProperties(Map<Object, Object> properties) {
        setDatabaseProperties(properties, true);
    }

    public void setDatabaseProperties(Map<Object, Object> properties, boolean sort) {
        model.setValues(properties, sort);
    }

    public JTable getTable() {
        return table;
    }

    public void setHeaders(List<String> headers) {
        JTableHeader tableHeader = table.getTableHeader();
        TableColumnModel columnModel = tableHeader.getColumnModel();
        for (int col = 0; col < headers.size() && col < columnModel.getColumnCount(); col++)
            columnModel.getColumn(col).setHeaderValue(headers.get(col));

        tableHeader.repaint();
    }

    public void restoreHeaders() {
        setHeaders(Arrays.asList(PropertyWrapperModel.DEFAULT_HEADERS));
    }

}

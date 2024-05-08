/*
 * PropertyWrapperModel.java
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

package org.underworldlabs.swing.table;

import org.executequery.localization.Bundles;
import org.underworldlabs.util.KeyValuePair;

import java.util.*;

/**
 * Simple wrapper class for key/value property values
 * providing table model and sorting by key or value.
 *
 * @author Takis Diakoumis
 */
public class PropertyWrapperModel extends AbstractSortableTableModel {

    private static final String[] HEADER = Bundles.get(PropertyWrapperModel.class, new String[]{
            "property",
            "value"
    });

    private KeyValuePair[] valuePairs;

    public PropertyWrapperModel() {
    }

    public PropertyWrapperModel(Properties values) {
        setValues(values.entrySet(), values.size());
    }

    public void setValues(Map<Object, Object> values, boolean sort) {
        setValues(values.entrySet(), values.size());
        if (sort)
            sort();
    }

    private void setValues(Set<Map.Entry<Object, Object>> entrySet, int setSize) {
        int count = 0;
        valuePairs = new KeyValuePair[setSize];
        for (Map.Entry<Object, Object> entry : entrySet)
            valuePairs[count++] = new KeyValuePair(entry.getKey().toString(), entry.getValue().toString());

        fireTableDataChanged();
    }

    public void sort() {

        if (valuePairs == null || valuePairs.length < 2)
            return;

        Arrays.sort(valuePairs, (pair1, pair2) -> {
            String value1 = pair1.getKey().toString().toUpperCase();
            String value2 = pair2.getKey().toString().toUpperCase();
            return value1.compareTo(value2);
        });

        fireTableDataChanged();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public int getRowCount() {
        return valuePairs != null ? valuePairs.length : 0;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return true;
    }

    @Override
    public Object getValueAt(int row, int col) {
        return col == 0 ? valuePairs[row].getKey() : valuePairs[row].getValue();
    }

    @Override
    public String getColumnName(int col) {
        return HEADER[col];
    }

}

/*
 * DynamicComboBoxModel.java
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

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * Simple combo box model that allows complete removal
 * and resetting of all values.
 *
 * @author Takis Diakoumis
 */
public class DynamicComboBoxModel<E> extends DefaultComboBoxModel<E> {
    private boolean blocked;

    public DynamicComboBoxModel() {
        super();
    }

    public DynamicComboBoxModel(E[] items) {
        super(items);
    }

    public DynamicComboBoxModel(Vector<E> items) {
        super(items);
    }

    public boolean contains(E item) {
        return getIndexOf(item) != -1;
    }

    public void setElements(E[] items) {
        setElements(Arrays.asList(items));
    }

    public void setElements(List<E> items) {

        if (getSize() > 0)
            removeAllElements();

        if (items == null)
            return;

        try {
            blocked = true;
            items.forEach(this::addElement);

        } finally {
            blocked = false;
        }

        fireIntervalAdded(this, 0, items.size() - 1);
        setSelectedItem(getElementAt(0));
    }

    // --- DefaultComboBoxModel impl ---

    @Override
    public void setSelectedItem(Object anObject) {
        if (!blocked)
            super.setSelectedItem(anObject);
    }

    @Override
    protected void fireIntervalAdded(Object source, int index0, int index1) {
        if (!blocked)
            super.fireIntervalAdded(source, index0, index1);
    }

}

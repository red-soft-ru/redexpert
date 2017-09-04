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

import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;

/**
 * Simple combo box model that allows complete removal 
 * and resetting of all values.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class DynamicComboBoxModel extends DefaultComboBoxModel {
    
    public DynamicComboBoxModel() {

        super();
    }

    public DynamicComboBoxModel(Object[] items) {
     
        super(items);
    }

    public DynamicComboBoxModel(Vector<?> items) {

        super(items);
    }

    public void contentsChanged() {

        fireContentsChanged(this,-1, -1);
    }

    public boolean contains(Object item) {
        
        return (getIndexOf(item) != -1); 
    }
    
    private boolean rebuilding;
    
    public void setElements(Object[] items) {

        if (getSize() > 0) {

            removeAllElements();
        }

        if (items != null && items.length > 0) {

            try {
            
                rebuilding = true;
                
                for (Object item : items) {
                    
                    addElement(item);
                }

            } finally {
                
                rebuilding = false;                    
            }

            fireIntervalAdded(this, 0, items.length - 1);
            setSelectedItem(getElementAt(0));
        
        }

    }
    
    @Override
    public void setSelectedItem(Object anObject) {

        if (!rebuilding) {

            super.setSelectedItem(anObject);
        }

    }
    
    @Override
    protected void fireIntervalAdded(Object source, int index0, int index1) {

        if (!rebuilding) {
        
            super.fireIntervalAdded(source, index0, index1);
        }

    }
    
    public void setElements(List<?> elements) {

        if (elements != null) {
        
            setElements(elements.toArray(new Object[elements.size()]));
            
        } else {
            
            removeAllElements();
        }
    }

}








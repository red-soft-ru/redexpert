/*
 * VetoableSingleSelectionModel.java
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

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

import javax.swing.DefaultSingleSelectionModel;

public class VetoableSingleSelectionModel extends DefaultSingleSelectionModel {

    private VetoableChangeSupport vetoableChangeSupport;

    @Override
    public void setSelectedIndex(int index) {

        if (getSelectedIndex() == index) {
         
            return;
        }
        
        try {
        
            fireVetoableChange(getSelectedIndex(), index);

        } catch (PropertyVetoException e) {
        
            return;
        }
        super.setSelectedIndex(index);
    }

    private void fireVetoableChange(int oldSelectionIndex, int newSelectionIndex) throws PropertyVetoException {

        if (!isVetoable()) {
         
            return;
        }

        vetoableChangeSupport.fireVetoableChange("selectedIndex", oldSelectionIndex, newSelectionIndex);
    }

    private boolean isVetoable() {
        
        if (vetoableChangeSupport == null) {
         
            return false;
        }
        return vetoableChangeSupport.hasListeners(null);
    }

    public void addVetoableChangeListener(VetoableChangeListener l) {

        if (vetoableChangeSupport == null) {

            vetoableChangeSupport = new VetoableChangeSupport(this);
        }
        vetoableChangeSupport.addVetoableChangeListener(l);
    }

    public void removeVetoableChangeListener(VetoableChangeListener l) {

        if (vetoableChangeSupport == null) {
         
            return;
        }
        vetoableChangeSupport.removeVetoableChangeListener(l);
    }

}



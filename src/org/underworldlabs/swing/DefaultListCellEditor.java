/*
 * DefaultListCellEditor.java
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

package org.underworldlabs.swing;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;

// from http://www.jroller.com/santhosh/date/20050607
public class DefaultListCellEditor extends DefaultCellEditor implements ListCellEditor { 

    public DefaultListCellEditor(final JCheckBox checkBox){ 
        
        super(checkBox); 
    } 
 
    public DefaultListCellEditor(final JComboBox comboBox){ 
        
        super(comboBox); 
    } 
 
    public DefaultListCellEditor(final JTextField textField){ 
        
        super(textField); 
    } 
 
    public Component getListCellEditorComponent(JList list, Object value, boolean isSelected, int index){ 
        
        delegate.setValue(value);

        if (getComponent() instanceof JTextField) {
            
            ((JTextField)getComponent()).selectAll();
        }
        
        return editorComponent; 
    }
    
}











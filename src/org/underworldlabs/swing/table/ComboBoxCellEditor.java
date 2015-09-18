/*
 * ComboBoxCellEditor.java
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

package org.underworldlabs.swing.table;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.EventListenerList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

import java.util.EventObject;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ComboBoxCellEditor extends JComboBox
                                implements TableCellEditor {
    
    protected EventListenerList listenerList = new EventListenerList();
    protected ChangeEvent changeEvent = new ChangeEvent(this);
    
    public static final int INTEGER = 0;
    public static final int STRING = 1;
    
    private int d_type;

    public ComboBoxCellEditor() {
        super();
        d_type = STRING;
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                fireEditingStopped();
            }
        });
    }

    public ComboBoxCellEditor(String[] values) {
        super(values);
        d_type = STRING;
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                fireEditingStopped();
            }
        });
    }
    
    public ComboBoxCellEditor(Object[] values) {
        super(values);
        d_type = STRING;
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                fireEditingStopped();
            }
        });
    }
    
    public ComboBoxCellEditor(Vector<?> values) {
        super(values);
        d_type = STRING;
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                fireEditingStopped();
            }
        });
    }
    
    public ComboBoxCellEditor(Vector<?> values, int type) {
        super(values);
        d_type = type;
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                fireEditingStopped();
            }
        });
    }
    
    public void addCellEditorListener(CellEditorListener listener) {
        listenerList.add(CellEditorListener.class, listener);
    }
    
    public void removeCellEditorListener(CellEditorListener listener) {
        listenerList.remove(CellEditorListener.class, listener);
    }
    
    protected void fireEditingStopped() {
        CellEditorListener listener;
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] == CellEditorListener.class) {
                listener = (CellEditorListener)listeners[i + 1];
                listener.editingStopped(changeEvent);
            }
        }
    }
    
    protected void fireEditingCanceled() {
        CellEditorListener listener;
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] == CellEditorListener.class) {
                listener = (CellEditorListener)listeners[i + 1];
                listener.editingCanceled(changeEvent);
            }
        }
    }
    
    public void cancelCellEditing() {
        fireEditingCanceled();
    }
    
    public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }
    
    public boolean isCellEditable(EventObject event) {
        return true;
    }
    
    public boolean shouldSelectCell(EventObject event) {
        return true;
    }
    
    public Object getCellEditorValue() {
        return getSelectedItem();
    }
    
    public void setSelectionValues(Object[] values) {
        DefaultComboBoxModel model = (DefaultComboBoxModel)getModel();
        model.removeAllElements();
        if (values != null && values.length > 0) {
            for (int i = 0; i < values.length; i++) {
                model.addElement(values[i]);
            }
            // select the first item
            model.setSelectedItem(values[0]);
        }
    }
    
    public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected, int row, int column) {
        if (d_type == STRING) {
            String type = (String)value;
            setSelectedItem(type);
        } else if (d_type == INTEGER) {
            Integer type = (Integer)value;
            setSelectedItem(type);
        }
        return this;
    }
    
    public void setSelection(int i) {
        setSelectedIndex(i);
    }
    
}






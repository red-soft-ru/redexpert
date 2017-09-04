/*
 * LabelCellEditor.java
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

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.EventListenerList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

import java.util.EventObject;

/**
 * A Class class.
 * <P>
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class LabelCellEditor extends JLabel
                             implements TableCellEditor {
    
    protected EventListenerList listenerList = new EventListenerList();
    protected ChangeEvent changeEvent = new ChangeEvent(this);
    
    public LabelCellEditor(int align) {
        super();
        
        setHorizontalAlignment(align);
        setBorder(null);
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
        return getText();
    }
    
    public Component getTableCellEditorComponent(JTable table,
    Object value, boolean isSelected, int row, int column) {
        String type = (String)value;
        setText(type);
        return this;
    }
   
}









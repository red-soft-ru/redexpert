/*
 * MutableValueJList.java
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

import java.applet.Applet;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventObject;

import javax.swing.AbstractAction;
import javax.swing.CellEditor;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

//from http://www.jroller.com/santhosh/date/20050607
public class MutableValueJList extends JList implements CellEditorListener {

    protected Component editorComp = null; 
    protected int editingIndex = -1; 
    protected ListCellEditor editor = null; 
    private PropertyChangeListener editorRemover = null; 
 
    private static final int DEFAULT_ROW_HEIGHT = 20;
    
    public MutableValueJList(ListModel dataModel) {

        super(dataModel); 
        init(); 
    } 
 
    private void init() { 
        
        getActionMap().put("startEditing", new StartEditingAction());                                                             //NOI18N 
        getActionMap().put("cancel", new CancelEditingAction());                                                                  //NOI18N 
        
        addMouseListener(new MouseListener()); 
        
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "startEditing");                                             //NOI18N 
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");  //NOI18N 
        
        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);                                                              //NOI18N 
        
        setListCellEditor(new DefaultListCellEditor(new JTextField()));
        setFixedCellHeight(DEFAULT_ROW_HEIGHT);
    } 
 
    public void setListCellEditor(ListCellEditor editor) { 
        this.editor = editor; 
    }
 
    public ListCellEditor getListCellEditor(){ 
        return editor; 
    } 
 
    public boolean isEditing() { 
        return (editorComp == null)? false : true; 
    } 
 
    public Component getEditorComponent() { 
        return editorComp; 
    } 
 
    public int getEditingIndex() { 
        return editingIndex; 
    } 
 
    public Component prepareEditor(int index) { 

        Object value = getModel().getElementAt(index); 
        
        boolean isSelected = isSelectedIndex(index); 
        
        Component comp = editor.getListCellEditorComponent(
                this, value, isSelected, index); 
        
        if (comp instanceof JComponent) { 

            JComponent jComp = (JComponent)comp;

            if (jComp.getNextFocusableComponent() == null) { 
                jComp.setNextFocusableComponent(this); 
            } 

        } 

        return comp; 
    } 
 
    public void removeEditor() { 
        KeyboardFocusManager.getCurrentKeyboardFocusManager(). 
                removePropertyChangeListener("permanentFocusOwner", editorRemover);   //NOI18N 

        editorRemover = null; 
 
        if(editor != null) { 

            editor.removeCellEditorListener(this); 
 
            if (editorComp != null) { 

                remove(editorComp); 
            } 
 
            Rectangle cellRect = getCellBounds(editingIndex, editingIndex); 
 
            editingIndex = -1; 
            editorComp = null; 
 
            repaint(cellRect); 
        } 
    } 

    public boolean editCellAt(int index, EventObject e) { 

        if (editor != null && !editor.stopCellEditing()) { 
            
            return false; 
        }
 
        if (index < 0 || index >= getModel().getSize()) {
         
            return false; 
        }
 
        if (!isCellEditable(index)) {
            
            return false; 
        }
 
        if (editorRemover == null) { 
            KeyboardFocusManager fm = KeyboardFocusManager.getCurrentKeyboardFocusManager(); 
            editorRemover = new CellEditorRemover(fm); 
            fm.addPropertyChangeListener("permanentFocusOwner", editorRemover);    //NOI18N 
        } 
 
        if (editor != null && editor.isCellEditable(e)) { 
            editorComp = prepareEditor(index); 
            if (editorComp == null) { 
                removeEditor(); 
                return false; 
            } 
            editorComp.setBounds(getCellBounds(index, index)); 
            add(editorComp); 
            editorComp.validate(); 
 
            editingIndex = index; 
            editor.addCellEditorListener(this); 
 
            return true; 
        } 
        return false; 
    } 
 
    public void removeNotify() { 
        KeyboardFocusManager.getCurrentKeyboardFocusManager(). 
            removePropertyChangeListener("permanentFocusOwner", editorRemover);   //NOI18N 
        super.removeNotify(); 
    } 
 
    // This class tracks changes in the keyboard focus state. It is used 
    // when the XList is editing to determine when to cancel the edit. 
    // If focus switches to a component outside of the XList, but in the 
    // same window, this will cancel editing. 
    class CellEditorRemover implements PropertyChangeListener { 
        KeyboardFocusManager focusManager; 
 
        public CellEditorRemover(KeyboardFocusManager fm) { 
            this.focusManager = fm; 
        } 
 
        public void propertyChange(PropertyChangeEvent ev) { 
            if (!isEditing() || getClientProperty("terminateEditOnFocusLost") != Boolean.TRUE) {   //NOI18N 
                return; 
            } 
 
            Component c = focusManager.getPermanentFocusOwner(); 
            while (c != null) { 
                if (c == MutableValueJList.this) { 
                    // focus remains inside the table 
                    return; 
                } else if ((c instanceof Window) || 
                           (c instanceof Applet && c.getParent() == null)) { 
                    if (c == SwingUtilities.getRoot(MutableValueJList.this)) { 
                        if (!getListCellEditor().stopCellEditing()) { 
                            getListCellEditor().cancelCellEditing(); 
                        } 
                    } 
                    break; 
                } 
                c = c.getParent(); 
            } 
        } 
    } 
 
    /*-------------------------------------------------[ Model Support ]---------------------------------------------------*/ 
 
    public boolean isCellEditable(int index) { 
        
        if (getModel() instanceof MutableListModel) {

            return ((MutableListModel)getModel()).isCellEditable(index);
        }

        return false; 
    } 
 
    public void setValueAt(Object value, int index){ 
        ((MutableListModel)getModel()).setValueAt(value, index); 
    } 
 
    /*-------------------------------------------------[ CellEditorListener ]---------------------------------------------------*/ 
 
    public void editingStopped(ChangeEvent e) { 
        if (editor != null) { 
            Object value = editor.getCellEditorValue(); 
            setValueAt(value, editingIndex); 
            removeEditor(); 
        } 
    } 
 
    public void editingCanceled(ChangeEvent e) { 
        removeEditor(); 
    } 
 
    /*-------------------------------------------------[ Editing Actions]---------------------------------------------------*/ 
 
    private static class StartEditingAction extends AbstractAction { 
        public void actionPerformed(ActionEvent e) { 
            
            MutableValueJList list = (MutableValueJList)e.getSource(); 
            if (!list.hasFocus()) {
                
                CellEditor cellEditor = list.getListCellEditor(); 
                if (cellEditor != null && !cellEditor.stopCellEditing()) { 
                    return; 
                } 

                list.requestFocus(); 
                return; 
            } 
            
            ListSelectionModel rsm = list.getSelectionModel(); 
            int anchorRow = rsm.getAnchorSelectionIndex();

            list.editCellAt(anchorRow, null); 
            Component editorComp = list.getEditorComponent();

            if (editorComp != null) {
                editorComp.requestFocus(); 
            }

        } 
    } 
 
    private class CancelEditingAction extends AbstractAction { 
        public void actionPerformed(ActionEvent e) { 
            MutableValueJList list = (MutableValueJList)e.getSource(); 
            list.removeEditor(); 
        } 
 
        public boolean isEnabled(){ 
            return isEditing(); 
        } 
    } 
 
    private class MouseListener extends MouseAdapter{ 
        private Component dispatchComponent; 
 
        private void setDispatchComponent(MouseEvent e) { 
            Component editorComponent = getEditorComponent(); 
            Point p = e.getPoint(); 
            Point p2 = SwingUtilities.convertPoint(MutableValueJList.this, p, editorComponent); 
            dispatchComponent = SwingUtilities.getDeepestComponentAt(editorComponent, 
                                                                 p2.x, p2.y); 
        } 
 
        private boolean repostEvent(MouseEvent e) { 
            // Check for isEditing() in case another event has 
            // caused the editor to be removed. See bug #4306499. 
            if (dispatchComponent == null || !isEditing()) { 
                return false; 
            } 
            MouseEvent e2 = SwingUtilities.convertMouseEvent(MutableValueJList.this, e, dispatchComponent); 
            dispatchComponent.dispatchEvent(e2); 
            return true; 
        } 
 
        private boolean shouldIgnore(MouseEvent e) { 
            return e.isConsumed() || 
                (!(SwingUtilities.isLeftMouseButton(e) && isEnabled())); 
        } 
 
        public void mouseClicked(MouseEvent e){ 
            
            if(shouldIgnore(e)) { 
                return; 
            }
            
            Point p = e.getPoint(); 
            int index = locationToIndex(p);

            // The autoscroller can generate drag events outside the Table's range. 
            if(index == -1) { 
                return;
            }
 
            if(editCellAt(index, e)) {

                setDispatchComponent(e); 
                repostEvent(e);

            } else if (isRequestFocusEnabled()) { 
                
                requestFocus(); 
            }
            
        }

    } 

}











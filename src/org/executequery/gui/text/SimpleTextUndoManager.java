/*
 * TextUndoManager.java
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

package org.executequery.gui.text;

import org.apache.commons.lang.StringUtils;
import org.executequery.GUIUtilities;
import org.executequery.gui.UndoableComponent;
import org.executequery.log.Log;
import org.underworldlabs.swing.actions.ActionBuilder;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentEvent.EventType;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Undo manager for text components. 
 *
 * @author   Takis Diakoumis
 */
public class SimpleTextUndoManager extends UndoManager implements FocusListener {
    
    /** the text component this manager is assigned to */
    private JTextComponent textComponent;
    
    /** the text component's document */
    private Document document;
    
    /** the current compound edit */
    private CompoundEdit compoundEdit;
    
    /** The undo command */
    private Action undoCommand;
    
    /** The redo command */
    private Action redoCommand;
    
    private boolean addNextInsert;

    private static final String[] WHITESPACE = {"\t", " ", "\n", "\r"};

    /** Creates a new instance of TextUndoManager */
    public SimpleTextUndoManager(JTextComponent textComponent) {

        this.textComponent = textComponent;
        document = textComponent.getDocument();
        document.addUndoableEditListener(this);
        
        // add the focus listener
        textComponent.addFocusListener(this);
        
        // retrieve the undo/redo actions from the cache
        undoCommand = ActionBuilder.get("undo-command");
        redoCommand = ActionBuilder.get("redo-command");
        
        // initialise the compound edit
        compoundEdit = new CompoundEdit();
        
        // Java 9 seems to have removed edit detailed info so adding
        // a simple document listener to listen for breaks used for 
        // compound edits
        document.addDocumentListener(new SimpleDocumentListenerForJava9());

    }

    /**
     * Updates the state of undo/redo on a focus gain.
     */
    public void focusGained(FocusEvent e) {
        // register this as an undo/redo component
        if (textComponent instanceof UndoableComponent) {
            GUIUtilities.registerUndoRedoComponent((UndoableComponent)textComponent);
        }
        updateControls();
    }

    /**
     * Updates the state of undo/redo on a focus lost.
     */
    public void focusLost(FocusEvent e) {
        if (undoCommand != null) {
            undoCommand.setEnabled(false);
        }        
        if (redoCommand != null) {
            redoCommand.setEnabled(false);
        }
        // deregister this as an undo/redo component
        if (textComponent instanceof UndoableComponent) {
            GUIUtilities.registerUndoRedoComponent(null);
        }
    }

    /**
     * Updates the state of the undo/redo actions.
     */
    private void updateControls() {

        undoCommand.setEnabled(canUndo());
        redoCommand.setEnabled(canRedo());
    }
    
    public void undoableEditHappened(UndoableEditEvent undoableEditEvent) {

        UndoableEdit edit = undoableEditEvent.getEdit();
        
        if (edit instanceof AbstractDocument.DefaultDocumentEvent) {
            
            AbstractDocument.DefaultDocumentEvent event = (AbstractDocument.DefaultDocumentEvent) edit;
            
            EventType eventType = event.getType();
            if (eventType == EventType.INSERT) {
                
                insert(edit);
                
            } else if (eventType == EventType.REMOVE) {
    
                remove(edit);
                
            }

        } else {
            
            // Java 9 ... no insight into the edit info now... why?
            // some notes here from issue notes from Netbeans:
            // http://statistics.netbeans.org/exceptions/detail.do?id=223463

            if (addNextInsert) {
                
                add();
            }
   
            change(edit);            
        }
        
        redoCommand.setEnabled(false);
        undoCommand.setEnabled(true);
    }

    private void change(UndoableEdit edit) {
        
        compoundEdit.addEdit(edit);
    }

    private void remove(UndoableEdit edit) {
        
        add();
        change(edit);
        add();
    }

    private void insert(UndoableEdit edit) {

        try {

            AbstractDocument.DefaultDocumentEvent event = (AbstractDocument.DefaultDocumentEvent) edit;
   
            if (addNextInsert) {
   
                add();
            }
   
            change(edit);
            
            int start = event.getOffset();
            int length = event.getLength();
   
            String text = event.getDocument().getText(start, length);
            if (StringUtils.endsWithAny(text, WHITESPACE)) {
   
                addNextInsert = true;
            }
            
        } catch (BadLocationException e) {
            
            Log.debug(e);
        }
    }

    private void add() {

        addNextInsert = false;
        compoundEdit.end();
        addEdit(compoundEdit);
        compoundEdit = new CompoundEdit();
    }
    
    /**
     * Ensures the component regains focus and actions are updated.
     */
    public void undo() {

        try {

            if (!canRedo()) {
        
                add();
            }
            super.undo();

        } catch (CannotUndoException e) {

            return;
        }
        updateControls();

        if (!textComponent.hasFocus()) {

            textComponent.requestFocus();
        }

    }

    /**
     * Ensures the component regains focus and actions are updated.
     */
    public void redo() {

        try {

            super.redo();

        } catch (CannotUndoException e) {

            return;
        }
        
        // always enable the undo command
        undoCommand.setEnabled(true);
        redoCommand.setEnabled(canRedo());

        if (!textComponent.hasFocus()) {

            textComponent.requestFocus();
        }

    }

    /**
     * Suspends this manager by removing itself from the document.
     */
    public void suspend() {

        document.removeUndoableEditListener(this);
    }
    
    /**
     * Suspends this manager by removing itself from the document.
     */
    public void reinstate() {

        document.addUndoableEditListener(this);
    }

    public void reset() {
        
        discardAllEdits();
    }
    
    
    class SimpleDocumentListenerForJava9 implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent event) {

            try {

                String text = document.getText(event.getOffset(), event.getLength());
                if (StringUtils.endsWithAny(text, WHITESPACE)) {
                    
                    addNextInsert = true;
                }

            } catch (BadLocationException e) {}
            
        }

        @Override
        public void removeUpdate(DocumentEvent event) {}

        @Override
        public void changedUpdate(DocumentEvent event) {}

    }
    
}

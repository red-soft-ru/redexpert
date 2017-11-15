/*
 * TextEditor.java
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

import javax.swing.text.JTextComponent;

import org.executequery.print.PrintFunction;
import org.executequery.gui.SaveFunction;

/**
 * Defines a panel with a text component that may
 * be manipulated - print, cut, copy, change case etc.
 *
 * @author   Takis Diakoumis
 */
public interface TextEditor extends PrintFunction, SaveFunction {
    
    /**
     * Returns the text component's text.
     *
     * @return the text component text
     */
    String getEditorText();

    /**
     * Returns the actual text component.
     *
     * @return the text component
     */
    JTextComponent getEditorTextComponent();
    
    /**
     * Cuts the selected text from the text component.
     */
    void cut();

    /**
     * Copies the selected text from the text component.
     */
    void copy();
    
    /**
     * Pastes text into the text component at the cursor position.
     */
    void paste();
    
    /**
     * Disables/enables updates on the text component. This is designed
     * to add remove some of the heavier listeners such as document 
     * change and caret listeners.
     *
     * @param disable - true | false
     */
    void disableUpdates(boolean disable);
    
    /**
     * Return whether the text component defined by this interface
     * may be text searched.
     *
     * @return true | false
     */
    boolean canSearch();

    void changeSelectionCase(boolean upper);
    
    void changeSelectionToCamelCase();
    
    void changeSelectionToUnderscore();
    
    void deleteLine();
    
    void deleteWord();
    
    void deleteSelection();
    
    void insertFromFile();
    
    void insertLineAfter();
    
    void insertLineBefore();
    
    void selectAll();
    
    void selectNone();

}




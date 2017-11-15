/*
 * TabPane.java
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

package org.executequery.base;

/**
 * Defines a tab pane with some simple focus methods.
 *
 * @author   Takis Diakoumis
 */
public interface TabPane {

    /**
     * Selects the next tab from the current selection.
     */
    void selectNextTab();

    /**
     * Selects the previous tab from the current selection.
     */
    void selectPreviousTab();

    /**
     * Indicates whether this tab pane has focus.
     *
     * @return true | false
     */
    boolean isFocused();
    
    /**
     * Indicates a focus gain.
     */
    void focusGained();

    /**
     * Indicates a focus loss.
     */
    void focusLost();

    
	int getSelectedIndex();
    
	void removeIndex(int index);
	
	void removeSelectedTab();

    void removeAllTabs();
	
}
















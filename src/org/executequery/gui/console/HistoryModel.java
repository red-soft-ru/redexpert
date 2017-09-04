/*
 * HistoryModel.java
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

package org.executequery.gui.console;

import java.util.*;

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class HistoryModel {
    
    // Private members
    private int max;
    private Vector data;
    
    /** Creates a new history model, seizing it
     *  according to the specified size.
     *
     *  @param max The maximum numbers of items this history can hold
     */
    public HistoryModel(int max) {
        this.max = max;
        data = new Vector(max);
    }
    
    /** When the user validate a new entry, we add it to the
     *  history.
     *
     *  @param text The String to be added to the history
     */
    public void addItem(String text) {
        
        if (text == null || text.length() == 0)
            return;
        
        int index = data.indexOf(text);
        
        if (index != -1)
            data.removeElementAt(index);
        
        data.insertElementAt(text, 0);
        
        if (getSize() > max)
            data.removeElementAt(getSize() - 1);
        
    }
    
    /** When user press UP or DOWN, we need to get
     *  a previous typed String, stored in the Vector.
     *
     *  @param index The index of the String to get
     *  @return A String corresponding to a previous entry
     */
    public String getItem(int index) {
        return (String)data.elementAt(index);
    }
    
    /** As the user can use arrows to get up and down
     *  in the list, we need to know its max capacity.
     *
     *  @return Maximum capacity of the history
     */
    public int getSize() {
        return data.size();
    }
    
    /** We can need to add an item directly at the end of
     *  the list.
     *
     *  @param item The String to add at the end
     */
    private void addItemToEnd(String item) {
        data.addElement(item);
    }
    
    
    public void cleanup() {
        data.clear();
        data = null;
    }
    
} // class
















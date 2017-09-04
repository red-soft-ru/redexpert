/*
 * TabRolloverEvent.java
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

package org.underworldlabs.swing.plaf;

import java.util.EventObject;

/**
 * Defines a tab rectangle rollover event.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class TabRolloverEvent extends EventObject {
    
    /** the tab index of the rollover */
    private int index;
    
    /** the x-coord */
    private int x;
    
    /** the y-coord */
    private int y;    

    /** 
     * Creates a new instance of TabRolloverEvent with the
     * specified object as the source of this event.
     *
     * @param the source object
     */
    public TabRolloverEvent(Object source, int index) {
        this(source, index, -1, -1);
    }

    /** 
     * Creates a new instance of TabRolloverEvent with the
     * specified object as the source of this event.
     *
     * @param the source object
     */
    public TabRolloverEvent(Object source, int index, int x, int y) {
        super(source);
        this.index = index;
        this.x = x;
        this.y = y;
    }

    /** 
     * Returns the tab index where this event originated.
     *
     * @return the tab index
     */
    public int getIndex() {
        return index;
    }

    /**
     * The x-coord of the underlying mouse event.
     *
     * @return the x-coord
     */
    public int getX() {
        return x;
    }

    /**
     * The y-coord of the underlying mouse event.
     *
     * @return the y-coord
     */
    public int getY() {
        return y;
    }
    
}








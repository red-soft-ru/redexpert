/*
 * DockedTabDragListener.java
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

import java.util.EventListener;

/**
 * Defines a listener implementation for a docked tab 
 * pane. The aim here is to provide event notification on 
 * tab dragging events.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public interface DockedTabDragListener extends EventListener {

    /**
     * Invoked when a mouse button is pressed on a tab and then dragged.
     *
     * @param the encapsulating event object
     */
    public void dockedTabDragged(DockedDragEvent e);
 
    /**
     *  Invoked when a mouse button has been released on a tab.
     *
     * @param the encapsulating event object
     */
    public void dockedTabReleased(DockedDragEvent e);
    
}
















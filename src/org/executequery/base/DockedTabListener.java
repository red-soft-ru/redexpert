/*
 * DockedTabListener.java
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
 * Listener interface for tab events.
 *
 * @author   Takis Diakoumis
 */
public interface DockedTabListener {
    
    /**
     * Indicates a tab minimised event.
     *
     * @param the event 
     */
    public void tabMinimised(DockedTabEvent e);

    /**
     * Indicates a tab restored from minimised event.
     *
     * @param the event 
     */
    public void tabRestored(DockedTabEvent e);

    /**
     * Indicates a tab selected event.
     *
     * @param the event 
     */
    public void tabSelected(DockedTabEvent e);

    /**
     * Indicates a tab deselected event.
     *
     * @param the event 
     */
    public void tabDeselected(DockedTabEvent e);
    
    /**
     * Indicates a tab closed event.
     *
     * @param the event 
     */
    public void tabClosed(DockedTabEvent e);
    
}
















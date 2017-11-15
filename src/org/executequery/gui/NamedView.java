/*
 * NamedView.java
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

package org.executequery.gui;

/**
 * Defines a panel view that can have multiple instances
 * displayed at the one time. This mainly allows for non-standard
 * names - ie. the title plus an incremented number as in 'Query Editor 1' etc.
 *
 * @author   Takis Diakoumis
 */
public interface NamedView {
    
    /**
     * Returns the display name for this view.
     *
     * @return the display name
     */
    public String getDisplayName();
    
}







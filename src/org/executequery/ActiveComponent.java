/*
 * ActiveComponent.java
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

package org.executequery;

/** 
 * Defines those objects with resources attached
 * that need to be closed before the panel or internal
 * frame is disposed. These resources usually include
 * database connections, threads, timers etc. This method
 * will be called from respective internal frame's dispose
 * method if required.
 *
 * @author   Takis Diakoumis
 */
public interface ActiveComponent {

    /**
     * Performs any cleanup functions that may be required
     * before this component is closed/disposed of.
     */
    public void cleanup();
    
}












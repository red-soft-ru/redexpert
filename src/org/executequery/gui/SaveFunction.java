/*
 * SaveFunction.java
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

package org.executequery.gui;

/**
 * Defines those panel views where a save to file is available.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public interface SaveFunction extends NamedView {
    
    /** Indicates a save has been successful. */
    int SAVE_COMPLETE = 0;
    
    /** Indicates a save has failed. */
    int SAVE_FAILED = 1;
    
    /** Indicates a save has been cancelled. */
    int SAVE_CANCELLED = 2;
    
    /** Indicates a save has been invalid. */
    int SAVE_INVALID = 3;
    
    /**
     * Performs a save on this panel view.
     *
     * @param whether to invoke a save as
     */
    int save(boolean saveAs);

    /**
     * Returns whether to display a save prompt as this view is being closed.
     *
     * @return true | false
     */
    boolean contentCanBeSaved();
    
}






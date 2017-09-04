/*
 * PrintFunction.java
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

package org.executequery.print;

import java.awt.print.Printable;

/** 
 * Defines those objects that have printing support.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public interface PrintFunction {
    
    /** 
     * Returns whether the object (panel) is in a printable state.
     *
     * @return Whether the object can print
     */
    boolean canPrint();
    
    /** 
     * Returns the <code>Printable</code> object.
     */
    Printable getPrintable();
    
    /** 
     * The name for this print job.
     *
     * @return the print job's name
     */
    String getPrintJobName();
    
}



















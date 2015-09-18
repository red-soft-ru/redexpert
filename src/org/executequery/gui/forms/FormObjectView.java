/*
 * FormObjectView.java
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

package org.executequery.gui.forms;

import java.awt.print.Printable;

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public interface FormObjectView {
    
    /** 
     * Performs some cleanup and releases resources before being closed.
     */
    public void cleanup();
    
    /** 
     * Refreshes the data and clears the cache 
     */
    public void refresh();
    
    /** 
     * Returns the print object - if any 
     */
    public Printable getPrintable();
    
    /** 
     * Returns the name of this panel 
     */
    public String getLayoutName();
    
    public void validate();
    public void repaint();
    
}















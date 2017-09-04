/*
 * BrowserObjectView.java
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

package org.executequery.gui.browser;

import java.awt.print.Printable;

/**
 * Defines those components with a browser view.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public interface BrowserObjectView {
    
    /** Performs some cleanup and releases resources before being closed. */
    public void cleanup();
    
    /** Refreshes the data and clears the cache */
    public void refresh();
    
    /** Returns the print object - if any */
    public Printable getPrintable();
    
    /** Returns the name of this panel */
    public String getLayoutName();
    
    /** Propagates the call to the relevant component. */
    public void validate();

    /** Propagates the call to the relevant component. */
    public void repaint();
    
}












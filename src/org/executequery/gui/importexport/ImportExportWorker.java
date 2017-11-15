/*
 * ImportExportWorker.java
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

package org.executequery.gui.importexport;

/**
 * <p>Interface defining an import or export worker
 * process. This interface will be implemented by those
 * classes handling the actual data transfer tasks after
 * all required details have been completed by the user.
 *
 * @author   Takis Diakoumis
 * @date   16 April 2003
 */
public interface ImportExportWorker {
    
    /** 
     * Cancels the current data transfer process. 
     */
    public void cancelTransfer();
    
    /**
     * Indicates a data transfer process has completed
     * and clean-up can be performed.
     */
    public void finished();
    
}



















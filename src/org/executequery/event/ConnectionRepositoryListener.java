/*
 * ConnectionRepositoryListener.java
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

package org.executequery.event;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public interface ConnectionRepositoryListener extends ApplicationEventListener {

    /**
     * Indicates a connection has been added to the repository.
     * 
     * @param the encapsulating event
     */
    public void connectionAdded(ConnectionRepositoryEvent connectionRepositoryEvent);

    /**
     * Indicates a connection has been modified to the repository.
     * 
     * @param the encapsulating event
     */
    public void connectionModified(ConnectionRepositoryEvent connectionRepositoryEvent);

    /**
     * Indicates a connection has removed from the repository.
     * 
     * @param the encapsulating event
     */
    public void connectionRemoved(ConnectionRepositoryEvent connectionRepositoryEvent);

    
}












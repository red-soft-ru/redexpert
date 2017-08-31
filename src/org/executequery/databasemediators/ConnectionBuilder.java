/*
 * ConnectionBuilder.java
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

package org.executequery.databasemediators;

import org.underworldlabs.jdbc.DataSourceException;

/**
 * 
 * @author Takis Diakoumis
 * @version $Revision: 1776 $
 * @date $Date: 2017-08-29 22:59:07 +1000 (Tue, 29 Aug 2017) $
 */
public interface ConnectionBuilder {

    void connect();

    void cancel();

    boolean isCancelled();
    
    boolean isConnected();
    
    String getConnectionName();

    DataSourceException getException();
    
    String getErrorMessage();
    
}

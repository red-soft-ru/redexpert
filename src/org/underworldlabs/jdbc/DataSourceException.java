/*
 * DataSourceException.java
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

package org.underworldlabs.jdbc;

import java.sql.SQLException;

/**
 * Generic exception thrown by data source related methods/classes.
 * Note: this was changed to extend RuntimeException (12 March 2008)
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class DataSourceException extends RuntimeException {
    
    /** closed connection indictaor value */
    private boolean connectionClosed;
    
    /** underlying dump cause */
    private Throwable cause;
    
    public DataSourceException() {
        super();
    }
    
    public DataSourceException(String message) {
        super(message);
    }

    public DataSourceException(String message, boolean connectionClosed) {
        super(message);
        this.connectionClosed = connectionClosed;
    }

    public DataSourceException(Throwable cause) {
        super(cause);
        this.cause = cause;
    }
    
    public DataSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public Throwable getCause() {
        return cause;
    }
    
    public boolean wasConnectionClosed() {
        return connectionClosed;
    }
    
    public String getExtendedMessage() {
        if (cause == null) {
            return getMessage() == null ? "" : getMessage();
        }

        StringBuilder sb = new StringBuilder();
        String message = cause.getMessage();
        if (message != null) {
            sb.append(message);
        } else {
            sb.append(cause);
        }

        if (cause instanceof SQLException) {            
            SQLException sqlCause = (SQLException)cause;
            sb.append("\nError Code: " + sqlCause.getErrorCode());

            String state = sqlCause.getSQLState();
            if (state != null) {
                sb.append("\nSQL State Code: " + state);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static final long serialVersionUID = 1L;
    
}








/*
 * SqlStatementResult.java
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

package org.executequery.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import org.executequery.Constants;

/**
 *
 * @author   Takis Diakoumis
 */
public class SqlStatementResult {
    
    private int type;
    
    private int updateCount;
    
    private int statementCount;
    
    private String message;
    
    private String otherErrorMessage;
    
    private ResultSet resultSet;
    
    private SQLException sqlException;
    
    private SQLWarning sqlWarning;
    
    private Throwable otherException;
    
    private Object otherResult;
    
    public SqlStatementResult() {}

    public SqlStatementResult(ResultSet resultSet, 
                              SQLException sqlException,
                              SQLWarning sqlWarning) {
        this.resultSet = resultSet;
        this.sqlException = sqlException;
        this.sqlWarning = sqlWarning;
    }

    public void reset(ResultSet resultSet, 
                      SQLException sqlException,
                      SQLWarning sqlWarning) {
        updateCount = -1;
        this.resultSet = resultSet;
        this.sqlException = sqlException;
        this.sqlWarning = sqlWarning;
    }

    public void reset() {
        updateCount = -1;
        message = Constants.EMPTY;
        resultSet = null;
        sqlException = null;
        sqlWarning = null;
        otherResult = null;
        otherErrorMessage = null;
    }
    
    public ResultSet getResultSet() {
        return resultSet;
    }

    public String getErrorMessage() {
        
        if (sqlException == null && otherErrorMessage == null) {

            return message;

        } else if (otherErrorMessage != null) {

            return otherErrorMessage;

        } else if (sqlException == null && otherException != null) {
            
            return otherException.getMessage();
        }

        String text = sqlException.getMessage();

        if (text != null) {

            int errorCode = 0;
            
            StringBuilder message = new StringBuilder();
            SQLException sqlExc = sqlException;
            
            while (true) {

                if (sqlExc == null) {

                    break;
                }

                String _message = sqlExc.getMessage();
                message.append(_message);

                if (!_message.endsWith("\n")) {

                    message.append("\n");
                }

                errorCode = sqlException.getErrorCode();
                if (errorCode > 0) {

                    message.append("[Error Code: ").
                            append(errorCode).
                            append("]\n");
                }

                text = sqlException.getSQLState();
                if (text != null) {

                    message.append("[SQL State: ").
                            append(text).
                            append("]\n");
                }

                sqlExc = sqlExc.getNextException();
            }

            return message.toString();

        } else {
          
            return "An indeterminate error has occurred";
        }

    }
    
    public boolean isResultSet() {
        return resultSet != null;
    }
    
    public void setResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public boolean isException() {
        return sqlException != null || otherException != null;
    }
    
    public boolean isInterrupted() {
        return otherException instanceof InterruptedException 
                || otherException instanceof org.underworldlabs.util.InterruptedException;
    }
    
    public SQLException getSqlException() {
        return sqlException;
    }

    public void setSqlException(SQLException sqlException) {
        this.sqlException = sqlException;
    }

    public SQLWarning getSqlWarning() {
        return sqlWarning;
    }

    public void setSqlWarning(SQLWarning sqlWarning) {
        this.sqlWarning = sqlWarning;
    }

    public int getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(int updateCount) {
        this.updateCount = updateCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getOtherResult() {
        return otherResult;
    }

    public void setOtherResult(Object otherResult) {
        this.otherResult = otherResult;
    }

    public String getOtherErrorMessage() {
        return otherErrorMessage;
    }

    public void setOtherErrorMessage(String otherErrorMessage) {
        this.otherErrorMessage = otherErrorMessage;
    }

    public int getStatementCount() {
        return statementCount;
    }

    public void setStatementCount(int statementCount) {
        this.statementCount = statementCount;
    }

    public Throwable getOtherException() {
        return otherException;
    }

    public void setOtherException(Throwable otherException) {
        this.otherException = otherException;
    }

}







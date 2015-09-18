/*
 * ProcedureParameter.java
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

package org.executequery.databaseobjects;

import java.sql.DatabaseMetaData;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ProcedureParameter {
    
    private String name;
    private int type;
    private int dataType;
    private String sqlType;
    private int size;
    private String value;
    
    private static final String RESULT_STORE = "< Result Store >";
    private static final String RETURN_VALUE = "< Return Value >";
    private static final String UNKNOWN = "< Unknown >";
    
    public ProcedureParameter(String name, int type, int dataType,
                              String sqlType, int size) {
        this.name = name;
        this.type = type;
        this.dataType = dataType;
        this.sqlType = sqlType;
        this.size = size;
    }
    
    public void setDataType(int dataType) {
        this.dataType = dataType;
    }
    
    public int getDataType() {
        return dataType;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }
    
    public String getSqlType() {
        return sqlType;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public int getType() {
        return type;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        
        if (name == null) {
            
            if (type == DatabaseMetaData.procedureColumnResult)
                return RESULT_STORE;
            
            else if (type == DatabaseMetaData.procedureColumnReturn)
                return RETURN_VALUE;
            
            else
                return UNKNOWN;
            
        } 
        
        return name;
    }
    
    public String toString() {
        return getName();
    }
    
}


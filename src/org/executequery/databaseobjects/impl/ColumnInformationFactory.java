/*
 * ColumnInformationFactory.java
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

package org.executequery.databaseobjects.impl;

import java.sql.Types;

public class ColumnInformationFactory {

    public ColumnInformation build(String tableName, String columnName, String typeName, 
            int typeInt, int size, int scale, boolean isNotNullable) {
        
        String typeString = typeName == null ? "" : typeName;

        StringBuilder buffer = new StringBuilder();

        buffer.append(columnName);
        buffer.append(" ");
        buffer.append(typeString);

        // if the type doesn't end with a digit or it
        // is a char type then add the size - attempt
        // here to avoid int4, int8 etc. type values

        if (!typeString.matches("\\b\\D+\\d+\\b") ||
                (typeInt == Types.CHAR ||
                 typeInt == Types.VARCHAR ||
                 typeInt == Types.LONGVARCHAR)) {

            if (size > 0 && !isDateDataType(typeInt) 
                                    && !isNonPrecisionType(typeInt)) {

                buffer.append("(");
                buffer.append(size);

                if (scale > 0) {

                    buffer.append(",");
                    buffer.append(scale);
                }
                buffer.append(")");
            }

        }

        if (isNotNullable) {
            
            buffer.append(" NOT NULL");
        }

        buffer.append("  [");
        buffer.append(tableName);
        buffer.append("]");

        return new ColumnInformation(columnName, buffer.toString());
    }
    
    private boolean isNonPrecisionType(int typeInt) {

        return typeInt == Types.BIT;
    }

    private boolean isDateDataType(int typeInt) {

        return typeInt == Types.DATE ||
                typeInt == Types.TIME ||
                typeInt == Types.TIMESTAMP;
    }

    
}






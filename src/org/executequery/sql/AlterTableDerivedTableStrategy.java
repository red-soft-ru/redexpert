/*
 * AlterTableDerivedTableStrategy.java
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

public class AlterTableDerivedTableStrategy extends AbstractDerivedTableStrategy {

    @Override
    public String extractTablesAndAliases(String query) {

        String tables = null;

        int index = query.indexOf(ALTER_TABLE);
        if (index != -1) {
            
            String portion = query.substring(index + ALTER_TABLE.length());
            
            char[] chars = portion.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                
                if (i > 0 && Character.isWhitespace(chars[i])) {
                    
                    tables = portion.substring(0, i).trim();
                }
                
            }
            
        }
        
        return tables;
    }

}







/*
 * DeleteDerivedTableStrategy.java
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

public class DeleteDerivedTableStrategy extends AbstractDerivedTableStrategy {

    @Override
    public String extractTablesAndAliases(String query) {

        String tables = null;
        int fromIndex = query.indexOf(FROM);
        int whereIndex = query.indexOf(WHERE);

        if (whereIndex != -1 && fromIndex != -1) {
            
            if (whereIndex != -1) {
            
                tables = query.substring(fromIndex + FROM.length(), whereIndex);
                
            } else {
            
                tables = query.substring(fromIndex + FROM.length());
            }
        
        }
        
        return tables;
    }

}







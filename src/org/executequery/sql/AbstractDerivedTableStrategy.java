/*
 * AbstractDerivedTableStrategy.java
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

package org.executequery.sql;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public abstract class AbstractDerivedTableStrategy implements DerivedTableStrategy {

    public List<QueryTable> deriveTables(String query) {

        List<QueryTable> queryTables = new ArrayList<QueryTable>();
        String tables = extractTablesAndAliases(query.toUpperCase());
        
        if (StringUtils.isNotBlank(tables)) {
            
            String[] namesAndAliases = StringUtils.split(tables, ",");                
            for (String nameAndAlias : namesAndAliases) {
                
                String[] strings = StringUtils.split(nameAndAlias, " ");                
                queryTables.add(new QueryTable(strings[0].trim(), strings.length > 1 ? strings[1].trim() : null));
            }

        }

        return queryTables;
    }

    abstract String extractTablesAndAliases(String query);

}






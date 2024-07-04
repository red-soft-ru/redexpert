/*
 * QueryTable.java
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

public class QueryTable {

    private final String name;
    private final String alias;

    public QueryTable(String name, String alias) {
        this.name = name;
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public String getName() {
        return name;
    }

    public String getCompareName() {
        if (name.contains("."))
            return name.substring(name.indexOf('.') + 1);
        return name;
    }

}

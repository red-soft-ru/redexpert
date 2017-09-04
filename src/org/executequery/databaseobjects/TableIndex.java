/*
 * TableIndex.java
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

package org.executequery.databaseobjects;

import java.util.List;



public interface TableIndex extends NamedObject {

    int NORMAL_INDEX = 0;
    int BITMAP_INDEX = 1;
    int UNSORTED_INDEX = 2;
    int UNIQUE_INDEX = 3;
    
    List<DatabaseColumn> getColumns();

    void setColumns(List<DatabaseColumn> columns);

    void addColumn(DatabaseColumn column);

    int getIndexType();

    void setIndexType(int indexType);

    String getCreateSQLText();
    
    DatabaseTable getTable();

    void clearColumns();
    
}












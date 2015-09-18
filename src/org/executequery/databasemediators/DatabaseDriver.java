/*
 * DatabaseDriver.java
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

import java.io.Serializable;

public interface DatabaseDriver extends Serializable {

    long SUN_ODBC_ID = 9999999999999l;
    
    String SUN_ODBC_DRIVER = "sun.jdbc.odbc.JdbcOdbcDriver";

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    String getURL();

    void setURL(String url);

    int getType();

    void setDatabaseType(int type);

    String getPath();

    void setPath(String path);

    String getClassName();

    void setClassName(String className);

    long getId();

    void setId(long id);

    boolean isDefaultSunOdbc();

    boolean isIdValid();

    boolean isDatabaseTypeValid();

}











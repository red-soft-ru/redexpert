/*
 * ProcedureParameter.java
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

/**
 * @author Takis Diakoumis
 */
public class ProcedureParameter extends Parameter {


    public ProcedureParameter(String name, int type, int dataType,
                              String sqlType, int size, int nullable) {
        this(name, type);
        this.dataType = dataType;
        this.sqlType = sqlType;
        this.size = size;
        this.nullable = nullable;
    }

    public ProcedureParameter(String name, int type) {
        this.name = name;
        this.type = type;
    }
}



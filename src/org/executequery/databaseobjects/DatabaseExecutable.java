/*
 * DatabaseExecutable.java
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

import org.underworldlabs.jdbc.DataSourceException;

import java.util.List;

/**
 * Defines a database executable object such as a function or
 * stored database procedure.
 *
 * @author Takis Diakoumis
 */
public interface DatabaseExecutable extends DatabaseObject {

    /**
     * Adds the specified values as a single parameter to this object.
     */
    ProcedureParameter addParameter(String name, int type, int dataType, String sqlType, int size, int nullable);

    /**
     * Returns this object's parameters.
     */
    List<ProcedureParameter> getParameters() throws DataSourceException;

    /**
     * Returns this object's parameters as an array.
     */
    ProcedureParameter[] getParametersArray() throws DataSourceException;

    String getSourceCode();
}

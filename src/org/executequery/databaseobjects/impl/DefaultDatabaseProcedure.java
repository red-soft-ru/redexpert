/*
 * DefaultDatabaseProcedure.java
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

package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;
import org.executequery.databaseobjects.ProcedureParameter;

import java.sql.DatabaseMetaData;
import java.sql.Types;
import java.util.List;

/**
 * Default database procedure implementation.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class DefaultDatabaseProcedure extends DefaultDatabaseExecutable 
                                      implements DatabaseProcedure {
    
    /**
     * Creates a new instance of DefaultDatabaseProcedure.
     */
    public DefaultDatabaseProcedure() {}

    /**
     * Creates a new instance of DefaultDatabaseProcedure
     */
    public DefaultDatabaseProcedure(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
    }

    /**
     * Creates a new instance of DefaultDatabaseProcedure with
     * the specified values.
     */
    public DefaultDatabaseProcedure(String schema, String name) {
        setName(name);
        setSchemaName(schema);
    }
    
    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    public int getType() {
        return PROCEDURE;
    }

    /**
     * Returns the meta data key name of this object.
     *
     * @return the meta data key name.
     */
    public String getMetaDataKey() {
        return META_TYPES[PROCEDURE];
    }

    public String getCreateSQLText() {

        StringBuilder sbSQL = new StringBuilder();
        StringBuilder sbInput = new StringBuilder();
        StringBuilder sbOutput = new StringBuilder();

        sbSQL.append("CREATE OR ALTER PROCEDURE \n");
        sbSQL.append(getName());
        sbSQL.append("\n");

        sbInput.append("( \n");

        sbOutput.append("( \n");

        List<ProcedureParameter> parameters = getParameters();

        for (ProcedureParameter parameter : parameters) {
            if (parameter.getType() == DatabaseMetaData.procedureColumnIn) {
                sbInput.append("\t");
                sbInput.append(parameter.getName());
                sbInput.append(" ");
                sbInput.append(parameter.getSqlType());
                if (parameter.getDataType() == Types.CHAR
                        || parameter.getDataType() == Types.VARCHAR
                        || parameter.getDataType() == Types.NVARCHAR
                        || parameter.getDataType() == Types.VARBINARY) {
                    sbInput.append("(");
                    sbInput.append(parameter.getSize());
                    sbInput.append("),\n");
                } else {
                    sbInput.append(",\n");
                }
            } else if (parameter.getType() == DatabaseMetaData.procedureColumnOut) {
                sbOutput.append("\t");
                sbOutput.append(parameter.getName());
                sbOutput.append(" ");
                sbOutput.append(parameter.getSqlType());
                if (parameter.getDataType() == Types.CHAR
                        || parameter.getDataType() == Types.VARCHAR
                        || parameter.getDataType() == Types.NVARCHAR
                        || parameter.getDataType() == Types.VARBINARY) {
                    sbOutput.append("(");
                    sbOutput.append(parameter.getSize());
                    sbOutput.append("),\n");
                } else {
                    sbOutput.append(",\n");
                }
            }
        }

        String input = null;
        if (sbInput.length() > 3) {
            input = sbInput.substring(0, sbInput.length() - 2);
            input += "\n) \n";
        }

        if (input != null) {
            sbSQL.append(input);
            sbSQL.append("\n");
        }

        String output = null;
        if (sbOutput.length() > 3) {
            output = sbOutput.substring(0, sbOutput.length() - 2);
            output += "\n) \n";
        }

        if (output != null) {
            sbSQL.append(output);
            sbSQL.append("\n");
        }


        sbSQL.append("AS");
        sbSQL.append("\n");

        sbSQL.append(getProcedureSourceCode());

        sbSQL.append(";\n");

        return sbSQL.toString();
    }
}
/*
 * DefaultDatabaseProcedure.java
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

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;
import org.executequery.databaseobjects.ProcedureParameter;
import org.executequery.gui.browser.ColumnData;

import java.sql.DatabaseMetaData;
import java.sql.Types;
import java.util.List;

/**
 * Default database procedure implementation.
 *
 * @author Takis Diakoumis
 */
public class DefaultDatabaseProcedure extends DefaultDatabaseExecutable
        implements DatabaseProcedure {

    /**
     * Creates a new instance of DefaultDatabaseProcedure.
     */
    public DefaultDatabaseProcedure() {
    }

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
                if (parameter.isTypeOf()) {
                    sbInput.append(" type of ");
                    if (parameter.getTypeOfFrom() == ColumnData.TYPE_OF_FROM_DOMAIN)
                        sbInput.append(parameter.getDomain());
                    else {
                        sbInput.append("column ");
                        sbInput.append(parameter.getRelation_name());
                        sbInput.append(".");
                        sbInput.append(parameter.getField_name());
                    }
                } else {
                    if (parameter.getDomain() != null) {
                        sbInput.append(parameter.getDomain());
                    } else {
                        if (parameter.getSqlType().contains("SUB_TYPE")) {
                            sbInput.append(parameter.getSqlType().replace("<0", String.valueOf(parameter.getSubtype())));
                            sbInput.append(" segment size ");
                            sbInput.append(parameter.getSize());
                        } else {
                            sbInput.append(parameter.getSqlType());

                            if (parameter.getDataType() == Types.CHAR
                                    || parameter.getDataType() == Types.BINARY
                                    || parameter.getDataType() == Types.VARCHAR
                                    || parameter.getDataType() == Types.NVARCHAR
                                    || parameter.getDataType() == Types.VARBINARY) {
                                sbInput.append("(");
                                sbInput.append(parameter.getSize());
                                sbInput.append(")");
                            }
                        }
                    }
                }
                if (parameter.getNullable() == 1)
                    sbInput.append(" not null ");
                sbInput.append(",\n");
            } else if (parameter.getType() == DatabaseMetaData.procedureColumnOut) {
                sbOutput.append("\t");
                sbOutput.append(parameter.getName());
                sbOutput.append(" ");
                if (parameter.isTypeOf()) {
                    sbOutput.append("type of ");
                    if (parameter.getTypeOfFrom() == ColumnData.TYPE_OF_FROM_DOMAIN)
                        sbOutput.append(parameter.getDomain());
                    else {
                        sbOutput.append("column ");
                        sbOutput.append(parameter.getRelation_name());
                        sbOutput.append(".");
                        sbOutput.append(parameter.getField_name());
                    }
                } else {
                    if (parameter.getDomain() != null) {
                        sbOutput.append(parameter.getDomain());
                    } else {
                        if (parameter.getSqlType().contains("SUB_TYPE")) {
                            sbOutput.append(parameter.getSqlType().replace("<0", String.valueOf(parameter.getSubtype())));
                            sbOutput.append(" segment size ");
                            sbOutput.append(parameter.getSize());
                        } else {
                            sbOutput.append(parameter.getSqlType());

                            if (parameter.getDataType() == Types.CHAR
                                    || parameter.getDataType() == Types.BINARY
                                    || parameter.getDataType() == Types.VARCHAR
                                    || parameter.getDataType() == Types.NVARCHAR
                                    || parameter.getDataType() == Types.VARBINARY) {
                                sbOutput.append("(");
                                sbOutput.append(parameter.getSize());
                                sbOutput.append(")");
                            }
                        }
                    }
                }
                if (parameter.getNullable() == 1)
                    sbOutput.append(" not null ");
                sbOutput.append(",\n");
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
            sbSQL.append("RETURNS \n");
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
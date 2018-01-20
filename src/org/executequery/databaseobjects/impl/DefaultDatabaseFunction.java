/*
 * DefaultDatabaseFunction.java
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

import org.executequery.databaseobjects.DatabaseFunction;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.FunctionParameter;
import org.underworldlabs.jdbc.DataSourceException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Default database function implementation.
 *
 * @author Takis Diakoumis
 */
public class DefaultDatabaseFunction extends DefaultDatabaseExecutable
        implements DatabaseFunction {

    /**
     * function parameters
     */
    private ArrayList<FunctionParameter> parameters;

    /**
     * function sql
     */
    private String functionSourceCode;

    /**
     * Creates a new instance of DefaultDatabaseFunction
     */
    public DefaultDatabaseFunction(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
    }

    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    public int getType() {
        return FUNCTION;
    }

    /**
     * Returns the meta data key name of this object.
     *
     * @return the meta data key name.
     */
    public String getMetaDataKey() {
        return META_TYPES[getType()];
    }

    /**
     * Indicates whether this executable object has any parameters.
     *
     * @return true | false
     */
    public boolean hasParameters() {
        List<FunctionParameter> _parameters = getFunctionParameters();
        return _parameters != null && !_parameters.isEmpty();
    }

    /**
     * Adds the specified values as a single parameter to this object.
     */
    public FunctionParameter addFunctionParameter(String name, int dataType, int size, int precision, int scale, int subType, int position, int type_of, String relation, String field) {
        if (parameters == null) {

            parameters = new ArrayList<FunctionParameter>();
        }

        FunctionParameter parameter = new FunctionParameter(name, dataType, size, precision, scale, subType, position, type_of, relation, field);
        parameters.add(parameter);

        return parameter;
    }

    /**
     * Returns this object's parameters.
     */
    public List<FunctionParameter> getFunctionParameters() throws DataSourceException {

        if (!isMarkedForReload() && parameters != null) {

            return parameters;
        }

        ResultSet rs = null;
        try {

            DatabaseMetaData dmd = getMetaTagParent().getHost().getDatabaseMetaData();
            parameters = new ArrayList<FunctionParameter>();


            rs = getFunctionArguments(getName());

            while (rs.next()) {

                parameters.add(new FunctionParameter(rs.getString(4),
                        rs.getInt(6),
                        rs.getInt(7),
                        rs.getInt(18),
                        rs.getInt(8),
                        rs.getInt(9),
                        rs.getInt(14),
                        rs.getInt("AM"),
                        rs.getString("RN"),
                        rs.getString("FN")
                        ));
                if (functionSourceCode == null || functionSourceCode.isEmpty())
                    functionSourceCode = rs.getString(2);
            }

            return parameters;

        } catch (SQLException e) {

            throw new DataSourceException(e);

        } finally {

            releaseResources(rs);
            setMarkedForReload(false);
        }
    }

    /**
     * Returns this object's parameters as an array.
     */
    public FunctionParameter[] getFunctionParametersArray() throws DataSourceException {
        if (parameters == null) {
            getFunctionParameters();
        }
        return parameters.toArray(new
                FunctionParameter[parameters.size()]);
    }

    /**
     * Returns the function arguments.
     *
     * @return the result set
     */
    private ResultSet getFunctionArguments(String name) throws SQLException {

        Connection connection = this.getMetaTagParent().getHost().getConnection();
        Statement statement = connection.createStatement();

        String sql = "select fnc.rdb$function_name,\n" +
                "fnc.rdb$function_source,\n" +
                "fnc.rdb$description,\n" +
                "fa.rdb$argument_name,\n" +
                "fs.rdb$field_name,\n" +
                "fs.rdb$field_type,\n" +
                "fs.rdb$field_length,\n" +
                "fs.rdb$field_scale,\n" +
                "fs.rdb$field_sub_type,\n" +
                "fs.rdb$segment_length,\n" +
                "fs.rdb$dimensions,\n" +
                "cr.rdb$character_set_name,\n" +
                "co.rdb$collation_name,\n" +
                "fa.rdb$argument_position,\n" +
                "fs.rdb$character_length,\n" +
                "fa.rdb$description,\n" +
                "fa.rdb$default_source,\n" +
                "fs.rdb$field_precision,\n" +
                "fa.rdb$argument_mechanism as AM,\n" +
                "fa.rdb$field_source,\n" +
                "fs.rdb$default_source,\n" +
                "fa.rdb$null_flag,\n" +
                "fa.rdb$relation_name as RN,\n" +
                "fa.rdb$field_name as FN,\n" +
                "co2.rdb$collation_name,\n" +
                "cr.rdb$default_collate_name,\n" +
                "fnc.rdb$return_argument,\n" +
                "fa.rdb$argument_position,\n" +
                "fnc.rdb$deterministic_flag,\n" +
                "fnc.rdb$engine_name,\n" +
                "fnc.rdb$entrypoint\n" +
                "from rdb$functions fnc\n" +
                "left join rdb$function_arguments fa on fa.rdb$function_name = fnc.rdb$function_name\n" +
                "and (fa.rdb$package_name is null)\n" +
                "left join rdb$fields fs on fs.rdb$field_name = fa.rdb$field_source\n" +
                "left join rdb$character_sets cr on fs.rdb$character_set_id = cr.rdb$character_set_id\n" +
                "left join rdb$collations co on ((fs.rdb$collation_id = co.rdb$collation_id) and (fs.rdb$character_set_id = co.rdb$character_set_id))\n" +
                "left join rdb$collations co2 on ((fa.rdb$collation_id = co2.rdb$collation_id) and (fs.rdb$character_set_id = co2.rdb$character_set_id))\n" +
                "where fnc.rdb$function_name = '" + name +"'\n" +
                "and (fnc.rdb$package_name is null)\n" +
                "order by fa.rdb$argument_position";

        return statement.executeQuery(sql);
    }

    public String getFunctionSourceCode() {
        return functionSourceCode;
    }

    public String getCreateSQLText() {

        StringBuilder sb = new StringBuilder();
        StringBuilder sbInput = new StringBuilder();
        StringBuilder sbOutput = new StringBuilder();

        sb.append("set term ^ ;");
        sb.append("\n\n");
        sb.append("create or alter function \n");
        sb.append(getName());
        sb.append("\n");

        sbInput.append("( \n");
        
        List<FunctionParameter> parameters = getFunctionParameters();

        for (FunctionParameter parameter : parameters) {
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
            } else if (parameter.getType() == DatabaseMetaData.procedureColumnReturn) {
                sbOutput.append("\t");
                sbOutput.append(" ");
                sbOutput.append(parameter.getSqlType());
                if (parameter.getDataType() == Types.CHAR
                        || parameter.getDataType() == Types.VARCHAR
                        || parameter.getDataType() == Types.NVARCHAR
                        || parameter.getDataType() == Types.VARBINARY) {

                    sbOutput.append(parameter.getSize());
                    sbOutput.append("\n");
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
            sb.append(input);
            sb.append("\n");
        }

        String output = null;
        if (sbOutput.length() > 3) {
            output = sbOutput.substring(0, sbOutput.length() - 2);
        }

        if (output != null) {
            sb.append("returns ");
            sb.append(output);
            sb.append("\n");
        }


        sb.append("as");
        sb.append("\n");

        sb.append(getFunctionSourceCode());

        sb.append("\n\n");
        sb.append("set term ; ^");


        return sb.toString();
    }
}













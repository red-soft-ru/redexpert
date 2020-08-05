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

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.DatabaseFunction;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseTypeConverter;
import org.executequery.databaseobjects.FunctionArgument;
import org.executequery.gui.browser.ColumnData;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;

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
     * function arguments
     */
    private ArrayList<FunctionArgument> arguments;

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
        if (isSystem()) {
            return SYSTEM_FUNCTION;
        } else return FUNCTION;
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
     * Indicates whether this executable object has any arguments.
     *
     * @return true | false
     */
    public boolean hasParameters() {
        List<FunctionArgument> arguments = getFunctionArguments();
        return arguments != null && !arguments.isEmpty();
    }

    /**
     * Returns this object's arguments.
     */
    public List<FunctionArgument> getFunctionArguments() throws DataSourceException {

        checkOnReload(arguments);
        return arguments;
    }

    void loadFunctionArguments() {
        ResultSet rs = null;
        try {

            DatabaseMetaData dmd = getMetaTagParent().getHost().getDatabaseMetaData();
            arguments = new ArrayList<>();


            rs = getFunctionArguments(getName());

            while (rs.next()) {
                FunctionArgument fp = new FunctionArgument(rs.getString(4),
                        DatabaseTypeConverter.getSqlTypeFromRDBType(rs.getInt(6), rs.getInt(9)),
                        rs.getInt(7),
                        rs.getInt(18),
                        rs.getInt(8),
                        rs.getInt(9),
                        rs.getInt(14),
                        rs.getInt("AM"),
                        rs.getString("RN"),
                        rs.getString("FN")
                );
                String domain = rs.getString("FS");
                if (domain != null && !domain.startsWith("RDB$"))
                    fp.setDomain(domain.trim());
                fp.setNullable(rs.getInt("null_flag"));
                if (fp.getDataType() == Types.LONGVARBINARY ||
                        fp.getDataType() == Types.LONGVARCHAR ||
                        fp.getDataType() == Types.BLOB) {
                    fp.setSize(rs.getInt("segment_length"));
                }
                String characterSet = rs.getString("character_set_name");
                if (characterSet != null && !characterSet.isEmpty() && !characterSet.contains("NONE"))
                    fp.setEncoding(characterSet.trim());
                fp.setSqlType(DatabaseTypeConverter.getDataTypeName(rs.getInt(6), fp.getSubType(), fp.getScale()));
                fp.setDefaultValue(rs.getString("DEFAULT_SOURCE"));
                arguments.add(fp);
                if (functionSourceCode == null || functionSourceCode.isEmpty())
                    functionSourceCode = rs.getString(2);
            }

        } catch (SQLException e) {

            throw new DataSourceException(e);

        } finally {

            releaseResources(rs, this.getMetaTagParent().getHost().getConnection());
        }
    }
    /**
     * Returns this object's arguments as an array.
     */
    public FunctionArgument[] getFunctionArgumentsArray() throws DataSourceException {
        if (arguments == null) {
            getFunctionArguments();
        }
        return arguments.toArray(new
                FunctionArgument[arguments.size()]);
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
                "fs.rdb$segment_length as segment_length,\n" +
                "fs.rdb$dimensions,\n" +
                "cr.rdb$character_set_name as character_set_name,\n" +
                "co.rdb$collation_name,\n" +
                "fa.rdb$argument_position,\n" +
                "fs.rdb$character_length,\n" +
                "fa.rdb$description,\n" +
                "fa.rdb$default_source as DEFAULT_SOURCE,\n" +
                "fs.rdb$field_precision,\n" +
                "fa.rdb$argument_mechanism as AM,\n" +
                "fa.rdb$field_source as FS,\n" +
                "fs.rdb$default_source,\n" +
                "fa.rdb$null_flag as null_flag,\n" +
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
        
        List<FunctionArgument> arguments = getFunctionArguments();

        for (FunctionArgument argument : arguments) {
            if (argument.getType() == DatabaseMetaData.procedureColumnIn) {
                sbInput.append("\t");
                sbInput.append(argument.getName());
                sbInput.append(" ");
                if (argument.isTypeOf()) {
                    sbInput.append(" type of ");
                    if (argument.getTypeOfFrom() == ColumnData.TYPE_OF_FROM_DOMAIN)
                        sbInput.append(argument.getDomain());
                    else {
                        sbInput.append("column ");
                        sbInput.append(argument.getRelationName());
                        sbInput.append(".");
                        sbInput.append(argument.getFieldName()).append(" ");
                    }
                    if (argument.getNullable() == 1)
                        sbInput.append(" not null ");
                    if (!MiscUtils.isNull(argument.getDefaultValue()))
                        sbInput.append(argument.getDefaultValue());
                    sbInput.append(",\n");
                } else {
                    if (argument.getDomain() != null) {
                        sbInput.append(argument.getDomain());
                    } else {
                        if (argument.getSqlType().contains("SUB_TYPE")) {
                            sbInput.append(argument.getSqlType().replace("<0", String.valueOf(argument.getSubType())));
                            sbInput.append(" segment size ");
                            sbInput.append(argument.getSize());
                        } else {
                            sbInput.append(argument.getSqlType());
                            if (argument.getDataType() == Types.CHAR
                                    || argument.getDataType() == Types.VARCHAR
                                    || argument.getDataType() == Types.NVARCHAR
                                    || argument.getDataType() == Types.VARBINARY) {
                                sbInput.append("(");
                                sbInput.append(argument.getSize());
                                sbInput.append(")");
                            }
                        }
                    }
                    if (argument.getEncoding() != null) {
                        sbInput.append(" character set ");
                        sbInput.append(argument.getEncoding()).append(" ");
                    }
                    if (argument.getNullable() == 1)
                        sbInput.append(" not null ");
                    if (!MiscUtils.isNull(argument.getDefaultValue()))
                        sbInput.append(" ").append(argument.getDefaultValue());
                    sbInput.append(",\n");
                }
            } else if (argument.getType() == DatabaseMetaData.procedureColumnReturn) {
                sbOutput.append(" ");
                if (argument.isTypeOf()) {
                    sbOutput.append("type of ");
                    if (argument.getTypeOfFrom() == ColumnData.TYPE_OF_FROM_DOMAIN)
                        sbOutput.append(argument.getDomain());
                    else {
                        sbOutput.append("column ");
                        sbOutput.append(argument.getRelationName());
                        sbOutput.append(".");
                        sbOutput.append(argument.getFieldName());
                    }
                    if (argument.getNullable() == 1)
                        sbOutput.append(" not null,\n");
                    else
                        sbOutput.append(",\n");
                } else {
                    if (argument.getDomain() != null) {
                        sbOutput.append(argument.getDomain());
                    } else {
                        if (argument.getSqlType().contains("SUB_TYPE")) {
                            sbOutput.append(argument.getSqlType().replace("<0", String.valueOf(argument.getSubType())));
                            sbOutput.append(" segment size ");
                            sbOutput.append(argument.getSize());
                        } else {
                            sbOutput.append(argument.getSqlType());
                            if (argument.getDataType() == Types.CHAR
                                    || argument.getDataType() == Types.VARCHAR
                                    || argument.getDataType() == Types.NVARCHAR
                                    || argument.getDataType() == Types.VARBINARY) {

                                sbOutput.append(argument.getSize());
                            }
                        }
                    }
                    if (argument.getEncoding() != null) {
                        sbOutput.append(" character set ");
                        sbOutput.append(argument.getEncoding());
                    }
                    if (argument.getNullable() == 1)
                        sbOutput.append(" not null,\n");
                    else
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

    protected void getObjectInfo() {
        try {
            loadFunctionArguments();
        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog("Error loading info about Function", e);
        } finally {
            setMarkedForReload(false);
        }
    }
}













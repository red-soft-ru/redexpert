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
import org.executequery.databaseobjects.DatabaseTypeConverter;
import org.executequery.databaseobjects.FunctionArgument;
import org.executequery.sql.sqlbuilder.*;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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

    private boolean deterministic;

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
     * Returns the metadata key name of this object.
     *
     * @return the metadata key name.
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
    @Override
    public String getCreateSQLText() {
        return SQLUtils.generateCreateFunction(
                getName(), getFunctionArguments(), getSourceCode(),
                getEntryPoint(), getEngine(), getSqlSecurity(), getRemarks(),
                false, true, isDeterministic(), getHost().getDatabaseConnection());
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("FUNCTION", getName());
    }

    protected static final String FA = "FA_";
    protected static final String PROCEDURE_SOURCE = "FUNCTION_SOURCE";
    protected static final String PARAMETER_NAME = "ARGUMENT_NAME";
    protected static final String PARAMETER_NUMBER = "ARGUMENT_POSITION";
    protected static final String PARAMETER_MECHANISM = "ARGUMENT_MECHANISM";
    protected static final String DETERMINISTIC_FLAG = "DETERMINISTIC_FLAG";
    protected static final String RETURN_ARGUMENT = "RETURN_ARGUMENT";

    @Override
    protected String queryForInfo() {
        SelectBuilder sb = SelectBuilder.createSelectBuilder();
        Table functions = Table.createTable("RDB$FUNCTIONS", "FNC");
        Table arguments = Table.createTable("RDB$FUNCTION_ARGUMENTS", "FA");
        Table fields = Table.createTable("RDB$FIELDS", "F");
        Table charsets = Table.createTable("RDB$CHARACTER_SETS", "CR");
        Table collations1 = Table.createTable("RDB$COLLATIONS", "CO1");
        Table collations2 = Table.createTable("RDB$COLLATIONS", "CO2");
        sb.appendFields(functions, PROCEDURE_SOURCE, DESCRIPTION, DETERMINISTIC_FLAG, RETURN_ARGUMENT);
        sb.appendFields(functions, !externalCheck(), ENGINE_NAME, ENTRYPOINT);
        sb.appendField(buildSqlSecurityField(functions));
        sb.appendFields(FA, arguments, PARAMETER_NAME, PARAMETER_NUMBER, FIELD_SOURCE, DESCRIPTION, PARAMETER_MECHANISM, DEFAULT_SOURCE, RELATION_NAME, FIELD_NAME, NULL_FLAG);
        sb.appendFields(fields, FIELD_NAME, FIELD_TYPE, FIELD_LENGTH, FIELD_SCALE, FIELD_SUB_TYPE, SEGMENT_LENGTH, DIMENSIONS, FIELD_PRECISION, DEFAULT_SOURCE);
        sb.appendFields(charsets, CHARACTER_SET_NAME, DEFAULT_COLLATE_NAME);
        sb.appendField(Field.createField(fields, "CHARACTER_LENGTH").setAlias(CHARACTER_LENGTH));
        sb.appendField(Field.createField(collations1, COLLATION_NAME).setAlias("CO1_" + COLLATION_NAME));
        sb.appendField(Field.createField(collations2, COLLATION_NAME).setAlias("CO2_" + COLLATION_NAME));

        sb.appendJoin(LeftJoin.createLeftJoin().appendFields(Field.createField(functions, "FUNCTION_NAME"),
                Field.createField(arguments, "FUNCTION_NAME")).setCondition(Condition.createCondition(Field.createField(arguments, "PACKAGE_NAME"), "IS", "NULL")));
        sb.appendJoin(LeftJoin.createLeftJoin().appendFields(Field.createField(arguments, FIELD_SOURCE), Field.createField(fields, FIELD_NAME)));
        sb.appendJoin(LeftJoin.createLeftJoin().appendFields(Field.createField(fields, CHARACTER_SET_ID), Field.createField(charsets, CHARACTER_SET_ID)));
        sb.appendJoin(LeftJoin.createLeftJoin().appendFields(Field.createField(fields, "COLLATION_ID"), Field.createField(collations1, "COLLATION_ID"))
                .appendFields(Field.createField(fields, CHARACTER_SET_ID), Field.createField(charsets, CHARACTER_SET_ID)));
        sb.appendJoin(LeftJoin.createLeftJoin().appendFields(Field.createField(arguments, "COLLATION_ID"), Field.createField(collations2, "COLLATION_ID"))
                .appendFields(Field.createField(fields, CHARACTER_SET_ID), Field.createField(collations2, CHARACTER_SET_ID)));
        sb.appendCondition(buildNameCondition(functions, "FUNCTION_NAME"));
        sb.appendCondition(Condition.createCondition(Field.createField(functions, "PACKAGE_NAME"), "IS", "NULL"));
        sb.setOrdering(Field.createField(arguments, PARAMETER_NUMBER).getFieldTable());
        String sql = sb.getSQLQuery();
                /*"select fnc.rdb$function_name,\n" +
                "fnc.rdb$function_source as SOURCE_CODE,\n" +
                "fnc.rdb$description as DESCRIPTION,\n" +
                "fa.rdb$argument_name,\n" +
                "fs.rdb$field_name,\n" +
                "fs.rdb$field_type,\n" +
                "fs.rdb$field_length,\n" +
                "fs.rdb$field_scale,\n" +
                "fs.rdb$field_sub_type as field_subtype,\n" +
                "fs.rdb$segment_length as segment_length,\n" +
                "fs.rdb$dimensions,\n" +
                "cr.rdb$character_set_name as character_set_name,\n" +
                "co.rdb$collation_name,\n" +
                "fa.rdb$argument_position,\n" +
                "fs.rdb$character_length AS CHAR_LEN,\n" +
                "fa.rdb$description as argument_description,\n" +
                "fa.rdb$default_source as DEFAULT_SOURCE,\n" +
                "fs.rdb$field_precision as FIELD_PRECISION,\n" +
                "fa.rdb$argument_mechanism as AM,\n" +
                "fa.rdb$field_source as FS,\n" +
                "fs.rdb$default_source,\n" +
                "fa.rdb$null_flag as null_flag,\n" +
                "fa.rdb$relation_name as RN,\n" +
                "fa.rdb$field_name as FN,\n" +
                "co2.rdb$collation_name,\n" +
                "cr.rdb$default_collate_name,\n" +
                "fnc.rdb$return_argument as RETURN_ARGUMENT,\n" +
                "fa.rdb$argument_position as argument_position,\n" +
                "fnc.rdb$deterministic_flag as DETERMINISTIC_FLAG,\n" +
                "fnc.rdb$engine_name as ENGINE,\n" +
                "fnc.rdb$entrypoint as ENTRY_POINT,\n" +
                "IIF(fnc.rdb$sql_security is null,null,IIF(fnc.rdb$sql_security,'DEFINER','INVOKER')) as SQL_SECURITY\n" +
                "from rdb$functions fnc\n" +
                "left join rdb$function_arguments fa on fa.rdb$function_name = fnc.rdb$function_name\n" +
                "and (fa.rdb$package_name is null)\n" +
                "left join rdb$fields fs on fs.rdb$field_name = fa.rdb$field_source\n" +
                "left join rdb$character_sets cr on fs.rdb$character_set_id = cr.rdb$character_set_id\n" +
                "left join rdb$collations co on ((fs.rdb$collation_id = co.rdb$collation_id) and (fs.rdb$character_set_id = co.rdb$character_set_id))\n" +
                "left join rdb$collations co2 on ((fa.rdb$collation_id = co2.rdb$collation_id) and (fs.rdb$character_set_id = co2.rdb$character_set_id))\n" +
                "where fnc.rdb$function_name = ?\n" +
                "and (fnc.rdb$package_name is null)\n" +
                "order by fa.rdb$argument_position";*/
        return sql;
    }

    @Override
    protected void setInfoFromResultSet(ResultSet rs) {

        try {
            boolean first = true;
            arguments = new ArrayList<>();
            while (rs.next()) {
                String parameterName = rs.getString(FA + PARAMETER_NAME);
                if (parameterName != null)
                    parameterName = parameterName.trim();
                FunctionArgument fp = new FunctionArgument(parameterName,
                        DatabaseTypeConverter.getSqlTypeFromRDBType(rs.getInt(FIELD_TYPE), rs.getInt(FIELD_SUB_TYPE)),
                        rs.getInt(FIELD_LENGTH),
                        rs.getInt(FIELD_PRECISION),
                        rs.getInt(FIELD_SCALE),
                        rs.getInt(FIELD_SUB_TYPE),
                        rs.getInt(FA + PARAMETER_NUMBER),
                        rs.getInt(FA + PARAMETER_MECHANISM),
                        rs.getString(FA + RELATION_NAME),
                        rs.getString(FA + FIELD_NAME)
                );
                int return_arg = rs.getInt(RETURN_ARGUMENT);
                if (return_arg == fp.getPosition())
                    fp.setType(DatabaseMetaData.procedureColumnReturn);
                else fp.setType(DatabaseMetaData.procedureColumnIn);
                String domain = rs.getString(FIELD_NAME);
                if (domain != null && !domain.startsWith("RDB$"))
                    fp.setDomain(domain.trim());
                fp.setNullable(rs.getInt(FA + NULL_FLAG) == 1 ? 0 : 1);
                if (rs.getInt(FIELD_PRECISION) != 0)
                    fp.setSize(rs.getInt(FIELD_PRECISION));
                if (rs.getInt(CHARACTER_LENGTH) != 0)
                    fp.setSize(rs.getInt(CHARACTER_LENGTH));
                if (fp.getDataType() == Types.LONGVARBINARY ||
                        fp.getDataType() == Types.LONGVARCHAR ||
                        fp.getDataType() == Types.BLOB) {
                    fp.setSize(rs.getInt(SEGMENT_LENGTH));
                }
                String characterSet = rs.getString(CHARACTER_SET_NAME);
                if (!MiscUtils.isNull(characterSet))
                    fp.setEncoding(characterSet.trim());
                fp.setSqlType(DatabaseTypeConverter.getDataTypeName(rs.getInt(FIELD_TYPE), fp.getSubType(), fp.getScale()));
                fp.setDefaultValue(rs.getString(FA + DEFAULT_SOURCE));
                fp.setDescription(rs.getString(FA + DESCRIPTION));
                arguments.add(fp);
                if (first) {
                    sourceCode = getFromResultSet(rs, PROCEDURE_SOURCE);
                    entryPoint = getFromResultSet(rs, ENTRYPOINT);
                    engine = getFromResultSet(rs, ENGINE_NAME);
                    setRemarks(getFromResultSet(rs, DESCRIPTION));
                    setSqlSecurity(getFromResultSet(rs, SQL_SECURITY));
                    setDeterministic(rs.getInt(DETERMINISTIC_FLAG) == 1);
                    first = false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public boolean isDeterministic() {
        return deterministic;
    }

    public void setDeterministic(boolean deterministic) {
        this.deterministic = deterministic;
    }

}













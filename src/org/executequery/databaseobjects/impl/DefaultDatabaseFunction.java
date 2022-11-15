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
import org.executequery.gui.browser.comparer.Comparer;
import org.underworldlabs.jdbc.DataSourceException;
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

    public String getCreateSQLText() {
        return SQLUtils.generateCreateFunction(getName(), getFunctionArguments(), getSourceCode(),
                getEntryPoint(), getEngine(), getRemarks(), true, getHost().getDatabaseConnection());
    }

    @Override
    protected String queryForInfo() {
        String sql = "select fnc.rdb$function_name,\n" +
                "fnc.rdb$function_source as SOURCE_CODE,\n" +
                "fnc.rdb$description as DESCRIPTION,\n" +
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
                "fnc.rdb$return_argument as RETURN_ARGUMENT,\n" +
                "fa.rdb$argument_position,\n" +
                "fnc.rdb$deterministic_flag,\n" +
                "fnc.rdb$engine_name as ENGINE,\n" +
                "fnc.rdb$entrypoint as ENTRY_POINT,\n" +
                "fnc.rdb$sql_security as SQL_SECURITY\n" +
                "from rdb$functions fnc\n" +
                "left join rdb$function_arguments fa on fa.rdb$function_name = fnc.rdb$function_name\n" +
                "and (fa.rdb$package_name is null)\n" +
                "left join rdb$fields fs on fs.rdb$field_name = fa.rdb$field_source\n" +
                "left join rdb$character_sets cr on fs.rdb$character_set_id = cr.rdb$character_set_id\n" +
                "left join rdb$collations co on ((fs.rdb$collation_id = co.rdb$collation_id) and (fs.rdb$character_set_id = co.rdb$character_set_id))\n" +
                "left join rdb$collations co2 on ((fa.rdb$collation_id = co2.rdb$collation_id) and (fs.rdb$character_set_id = co2.rdb$character_set_id))\n" +
                "where fnc.rdb$function_name = '" + getName() + "'\n" +
                "and (fnc.rdb$package_name is null)\n" +
                "order by fa.rdb$argument_position";
        return sql;
    }

    @Override
    protected void setInfoFromResultSet(ResultSet rs) {

        try {
            boolean first = true;
            arguments = new ArrayList<>();
            while (rs.next()) {
                String parameterName = rs.getString(4);
                if (parameterName != null) {
                    FunctionArgument fp = new FunctionArgument(parameterName.trim(),
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
                    int return_arg = rs.getInt("RETURN_ARGUMENT");
                    if (return_arg == fp.getPosition())
                        fp.setType(DatabaseMetaData.procedureColumnReturn);
                    else fp.setType(DatabaseMetaData.procedureColumnIn);
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
                }
                if (first) {
                    sourceCode = getFromResultSet(rs, "SOURCE_CODE");
                    entryPoint = getFromResultSet(rs, "ENTRY_POINT");
                    engine = getFromResultSet(rs, "ENGINE");
                    setRemarks(getFromResultSet(rs, "DESCRIPTION"));
                    first = false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {
        return SQLUtils.generateCreateFunction(getName(), getFunctionArguments(), getSourceCode(),
                getEntryPoint(), getEngine(), getRemarks(), Comparer.isCommentsNeed(), getHost().getDatabaseConnection());
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropRequest("FUNCTION", getName());
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {
        return databaseObject.getCompareCreateSQL().
                replaceFirst("CREATE OR ", "").
                replaceFirst("CREATE", "ALTER");
    }

    @Override
    public String getFillSQL() throws DataSourceException {
        return null;
    }

}













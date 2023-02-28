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
import org.executequery.databaseobjects.DatabaseTypeConverter;
import org.executequery.databaseobjects.ProcedureParameter;
import org.executequery.gui.browser.ColumnData;
import org.underworldlabs.util.SQLUtils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;

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
     * @return the meta data key name
     */


    public String getMetaDataKey() {
        return META_TYPES[PROCEDURE];
    }

    public String getCreateSQLText() {

        return SQLUtils.generateCreateProcedure(getName(), getEntryPoint(), getEngine(), getParameters(), getSqlSecurity(), getAuthid(), getSourceCode(), getRemarks(), getHost().getDatabaseConnection());
    }

    @Override
    protected String queryForInfo() {
        String sql;
        if (getDatabaseMajorVersion() >= 3)
            sql = "select prc.rdb$procedure_name,\n" +
                    "prc.rdb$procedure_source as PROCEDURE_SOURCE,\n" +
                    "prc.rdb$description AS DESCRIPTION, \n" +
                    "pp.rdb$parameter_name,\n" +
                    "pp.rdb$parameter_type,\n" +
                    "fs.rdb$field_name, \n" +
                    "fs.rdb$field_type, \n" +
                    "fs.rdb$field_length, \n" +
                    "fs.rdb$field_scale, \n" +
                    "fs.rdb$field_sub_type, \n" +
                    "fs.rdb$segment_length as segment_length, \n" +
                    "fs.rdb$dimensions, \n" +
                    "cr.rdb$character_set_name as character_set_name, \n" +
                    "co.rdb$collation_name, \n" +
                    "pp.rdb$parameter_number,\n" +
                    "fs.rdb$character_length AS CHAR_LEN, \n" +
                    "pp.rdb$description,\n" +
                    "pp.rdb$default_source as default_source,\n" +
                    "fs.rdb$field_precision as FIELD_PRECISION, \n" +
                    "pp.rdb$parameter_mechanism as AM,\n" +
                    "pp.rdb$field_source as FS,\n" +
                    "fs.rdb$default_source as FS_DEFAULT_SOURCE, \n" +
                    "pp.rdb$null_flag as null_flag,\n" +
                    "pp.rdb$relation_name as RN,\n" +
                    "pp.rdb$field_name as FN,\n" +
                    "co2.rdb$collation_name, \n" +
                    "cr.rdb$default_collate_name, \n" +
                    "prc.rdb$engine_name as ENGINE,\n" +
                    "prc.rdb$entrypoint as ENTRY_POINT,\n" +
                    "IIF(prc.rdb$sql_security is null,null,IIF(prc.rdb$sql_security,'DEFINER','INVOKER')) as SQL_SECURITY,\n" +
                    "null as AUTHID\n" +
                    "from rdb$procedures prc\n" +
                    "left join rdb$procedure_parameters pp on pp.rdb$procedure_name = prc.rdb$procedure_name\n" +
                    "and (pp.rdb$package_name is null)\n" +
                    "left join rdb$fields fs on fs.rdb$field_name = pp.rdb$field_source\n" +
                    "left join rdb$character_sets cr on fs.rdb$character_set_id = cr.rdb$character_set_id \n" +
                    "left join rdb$collations co on ((fs.rdb$collation_id = co.rdb$collation_id) and (fs.rdb$character_set_id = co.rdb$character_set_id)) \n" +
                    "left join rdb$collations co2 on ((pp.rdb$collation_id = co2.rdb$collation_id) and (fs.rdb$character_set_id = co2.rdb$character_set_id))\n" +
                    "where prc.rdb$procedure_name = ?\n" +
                    "and (prc.rdb$package_name is null) \n" +
                    "order by pp.rdb$parameter_number";
        else if (getDatabaseMinorVersion() >= 5)
            sql = "select prc.rdb$procedure_name,\n" +
                    "prc.rdb$procedure_source as PROCEDURE_SOURCE,\n" +
                    "prc.rdb$description AS DESCRIPTION, \n" +
                    "pp.rdb$parameter_name,\n" +
                    "pp.rdb$parameter_type,\n" +
                    "fs.rdb$field_name, \n" +
                    "fs.rdb$field_type, \n" +
                    "fs.rdb$field_length, \n" +
                    "fs.rdb$field_scale, \n" +
                    "fs.rdb$field_sub_type, \n" +
                    "fs.rdb$segment_length as segment_length, \n" +
                    "fs.rdb$dimensions, \n" +
                    "cr.rdb$character_set_name as character_set_name, \n" +
                    "co.rdb$collation_name, \n" +
                    "pp.rdb$parameter_number,\n" +
                    "fs.rdb$character_length AS CHAR_LEN, \n" +
                    "pp.rdb$description,\n" +
                    "pp.rdb$default_source as default_source,\n" +
                    "fs.rdb$field_precision as FIELD_PRECISION, \n" +
                    "pp.rdb$parameter_mechanism as AM,\n" +
                    "pp.rdb$field_source as FS,\n" +
                    "fs.rdb$default_source as FS_DEFAULT_SOURCE, \n" +
                    "pp.rdb$null_flag as null_flag,\n" +
                    "pp.rdb$relation_name as RN,\n" +
                    "pp.rdb$field_name as FN,\n" +
                    "co2.rdb$collation_name, \n" +
                    "cr.rdb$default_collate_name, \n" +
                    "prc.rdb$language as ENGINE,\n" +
                    "prc.rdb$external_name as ENTRY_POINT,\n" +
                    "null as SQL_SECURITY,\n" +
                    "IIF(prc.rdb$procedure_context=1,'CALLER','OWNER') as AUTHID\n" +
                    "from rdb$procedures prc\n" +
                    "left join rdb$procedure_parameters pp on pp.rdb$procedure_name = prc.rdb$procedure_name\n" +
                    "left join rdb$fields fs on fs.rdb$field_name = pp.rdb$field_source\n" +
                    "left join rdb$character_sets cr on fs.rdb$character_set_id = cr.rdb$character_set_id \n" +
                    "left join rdb$collations co on ((fs.rdb$collation_id = co.rdb$collation_id) and (fs.rdb$character_set_id = co.rdb$character_set_id)) \n" +
                    "left join rdb$collations co2 on ((pp.rdb$collation_id = co2.rdb$collation_id) and (fs.rdb$character_set_id = co2.rdb$character_set_id))\n" +
                    "where prc.rdb$procedure_name = ?\n" +
                    "order by pp.rdb$parameter_number";
        else
            sql = "select prc.rdb$procedure_name,\n" +
                    "prc.rdb$procedure_source as PROCEDURE_SOURCE,\n" +
                    "prc.rdb$description as DESCRIPTION, \n" +
                    "pp.rdb$parameter_name,\n" +
                    "pp.rdb$parameter_type,\n" +
                    "fs.rdb$field_name, \n" +
                    "fs.rdb$field_type, \n" +
                    "fs.rdb$field_length, \n" +
                    "fs.rdb$field_scale, \n" +
                    "fs.rdb$field_sub_type, \n" +
                    "fs.rdb$segment_length as segment_length, \n" +
                    "fs.rdb$dimensions, \n" +
                    "cr.rdb$character_set_name as character_set_name, \n" +
                    "co.rdb$collation_name, \n" +
                    "pp.rdb$parameter_number,\n" +
                    "fs.rdb$character_length AS CHAR_LEN, \n" +
                    "pp.rdb$description,\n" +
                    "null as default_source,\n" +
                    "fs.rdb$field_precision as FIELD_PRECISION, \n" +
                    "null as AM,\n" +
                    "pp.rdb$field_source as FS,\n" +
                    "fs.rdb$default_source as FS_DEFAULT_SOURCE, \n" +
                    "null as null_flag,\n" +
                    "null as RN,\n" +
                    "null as FN,\n" +
                    "co2.rdb$collation_name, \n" +
                    "cr.rdb$default_collate_name, \n" +
                    "null as ENGINE,\n" +
                    "null as ENTRY_POINT,\n" +
                    "null as SQL_SECURITY,\n" +
                    "null as AUTHID\n" +
                    "from rdb$procedures prc\n" +
                    "left join rdb$procedure_parameters pp on pp.rdb$procedure_name = prc.rdb$procedure_name\n" +
                    "left join rdb$fields fs on fs.rdb$field_name = pp.rdb$field_source\n" +
                    "left join rdb$character_sets cr on fs.rdb$character_set_id = cr.rdb$character_set_id \n" +
                    "left join rdb$collations co on ((fs.rdb$collation_id = co.rdb$collation_id) and (fs.rdb$character_set_id = co.rdb$character_set_id)) \n" +
                    "left join rdb$collations co2 on (fs.rdb$character_set_id = co2.rdb$character_set_id)\n" +
                    "where prc.rdb$procedure_name = ?\n" +
                    "order by pp.rdb$parameter_number";
        return sql;
    }

    @Override
    protected void setInfoFromResultSet(ResultSet rs) {
        try {
            parameters = new ArrayList<ProcedureParameter>();
            procedureInputParameters = new ArrayList<ProcedureParameter>();
            procedureOutputParameters = new ArrayList<ProcedureParameter>();
            boolean first = true;
            while (rs.next()) {
                String parameterName = rs.getString(4);
                if (parameterName != null) {
                    ProcedureParameter pp = new ProcedureParameter(parameterName.trim(),
                            rs.getInt(5) == 0 ? DatabaseMetaData.procedureColumnIn : DatabaseMetaData.procedureColumnOut,
                            DatabaseTypeConverter.getSqlTypeFromRDBType(rs.getInt(7), rs.getInt(10)),
                            DatabaseTypeConverter.getDataTypeName(rs.getInt(7), rs.getInt(10), rs.getInt(9)),
                            rs.getInt(8),
                            1 - rs.getInt("null_flag"));
                    if (rs.getInt("FIELD_PRECISION") != 0)
                        pp.setSize(rs.getInt("FIELD_PRECISION"));
                    if (rs.getInt("CHAR_LEN") != 0)
                        pp.setSize(rs.getInt("CHAR_LEN"));
                    if (pp.getDataType() == Types.LONGVARBINARY ||
                            pp.getDataType() == Types.LONGVARCHAR ||
                            pp.getDataType() == Types.BLOB) {
                        pp.setSubType(rs.getInt(10));
                        pp.setSize(rs.getInt("segment_length"));
                    }

                    String domain = rs.getString(6);
                    if (domain != null && !domain.startsWith("RDB$"))
                        pp.setDomain(domain.trim());
                    pp.setTypeOf(rs.getInt("AM") == 1);
                    String relationName = rs.getString("RN");
                    if (relationName != null)
                        pp.setRelationName(relationName.trim());
                    String fieldName = rs.getString("FN");
                    if (fieldName != null)
                        pp.setFieldName(fieldName.trim());

                    if (pp.getRelationName() != null && pp.getFieldName() != null)
                        pp.setTypeOfFrom(ColumnData.TYPE_OF_FROM_COLUMN);
                    String characterSet = rs.getString("character_set_name");
                    if (characterSet != null && !characterSet.isEmpty() && !characterSet.contains("NONE"))
                        pp.setEncoding(characterSet.trim());
                    pp.setDefaultValue(rs.getString("default_source"));
                    if (pp.getDefaultValue() == null)
                        pp.setDefaultValue(rs.getString("FS_DEFAULT_SOURCE"));
                    if (pp.getType() == DatabaseMetaData.procedureColumnIn)
                        procedureInputParameters.add(pp);
                    else if (pp.getType() == DatabaseMetaData.procedureColumnOut)
                        procedureOutputParameters.add(pp);
                    parameters.add(pp);
                }
                if (first) {
                    sourceCode = getFromResultSet(rs, "PROCEDURE_SOURCE");
                    entryPoint = getFromResultSet(rs, "ENTRY_POINT");
                    engine = getFromResultSet(rs, "ENGINE");
                    setRemarks(getFromResultSet(rs, "DESCRIPTION"));
                    setSqlSecurity(getFromResultSet(rs, "SQL_SECURITY"));
                    setAuthid(getFromResultSet(rs, "AUTHID"));
                    first = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
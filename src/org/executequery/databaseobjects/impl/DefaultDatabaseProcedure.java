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
import org.executequery.gui.browser.comparer.Comparer;
import org.executequery.sql.sqlbuilder.*;
import org.underworldlabs.jdbc.DataSourceException;
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
     * Returns the metadata key name of this object.
     *
     * @return the metadata key name
     */


    public String getMetaDataKey() {
        return META_TYPES[PROCEDURE];
    }

    public String getCreateSQLText() {
        return SQLUtils.generateCreateProcedure(
                getName(), getEntryPoint(), getEngine(), getParameters(), getSqlSecurity(), getAuthid(),
                getSourceCode(), getRemarks(), getHost().getDatabaseConnection(), false, true);
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("PROCEDURE", getName());
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {
        return SQLUtils.generateCreateProcedure(
                getName(), getEntryPoint(), getEngine(), getParameters(), getSqlSecurity(), getAuthid(), getSourceCode(),
                getRemarks(), getHost().getDatabaseConnection(), true, Comparer.isCommentsNeed());
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {
        return (!this.getCompareCreateSQL().equals(databaseObject.getCompareCreateSQL())) ?
                databaseObject.getCompareCreateSQL() : "/* there are no changes */";
    }

    protected static final String PP = "PP_";
    protected static final String PROCEDURE_SOURCE = "PROCEDURE_SOURCE";
    protected static final String PROCEDURE_CONTEXT = "PROCEDURE_CONTEXT";
    protected static final String AUTHID = "AUTHID";
    protected static final String PARAMETER_NAME = "PARAMETER_NAME";
    protected static final String PARAMETER_TYPE = "PARAMETER_TYPE";
    protected static final String PARAMETER_NUMBER = "PARAMETER_NUMBER";
    protected static final String PARAMETER_MECHANISM = "PARAMETER_MECHANISM";

    @Override
    protected String queryForInfo() {
        SelectBuilder sb = SelectBuilder.createSelectBuilder();
        Table procedures = Table.createTable("RDB$PROCEDURES", "PRC");
        Table parameters = Table.createTable("RDB$PROCEDURE_PARAMETERS", "PP");
        Table fields = Table.createTable("RDB$FIELDS", "F");
        Table charsets = Table.createTable("RDB$CHARACTER_SETS", "CR");
        Table collations1 = Table.createTable("RDB$COLLATIONS", "CO1");
        Table collations2 = Table.createTable("RDB$COLLATIONS", "CO2");
        sb.appendFields(procedures, PROCEDURE_SOURCE, DESCRIPTION);
        sb.appendFields(procedures, !externalCheck(), ENGINE_NAME, ENTRYPOINT);
        sb.appendField(buildSqlSecurityField(procedures));
        Field authid = Field.createField(procedures, PROCEDURE_CONTEXT);
        authid.setStatement(Function.createFunction("IIF")
                .appendArgument(authid.getFieldTable() + " IS NULL").appendArgument("NULL").appendArgument(Function.createFunction().setName("IIF")
                        .appendArgument(authid.getFieldTable() + "=1").appendArgument("'CALLER'").appendArgument("'OWNER'").getStatement()).getStatement());
        authid.setNull(sqlSecurityCheck() || getDatabaseMajorVersion() < 3 && getDatabaseMinorVersion() < 5 || !isRDB());
        authid.setAlias(AUTHID);
        sb.appendField(authid);
        sb.appendFields(PP, parameters, PARAMETER_NAME, PARAMETER_TYPE, PARAMETER_NUMBER, FIELD_SOURCE, DESCRIPTION);
        sb.appendFields(fields, FIELD_NAME, FIELD_TYPE, FIELD_LENGTH, FIELD_SCALE, FIELD_SUB_TYPE, SEGMENT_LENGTH, DIMENSIONS, FIELD_PRECISION, DEFAULT_SOURCE);
        sb.appendFields(charsets, CHARACTER_SET_NAME, DEFAULT_COLLATE_NAME);
        sb.appendField(Field.createField(fields, "CHARACTER_LENGTH").setAlias(CHARACTER_LENGTH));
        sb.appendField(Field.createField(collations1, COLLATION_NAME).setAlias("CO1_" + COLLATION_NAME));
        sb.appendField(Field.createField(collations2, COLLATION_NAME).setAlias("CO2_" + COLLATION_NAME));
        sb.appendFields(PP, parameters, !moreOrEqualsVersionCheck(2, 5), PARAMETER_MECHANISM, DEFAULT_SOURCE, RELATION_NAME, FIELD_NAME, NULL_FLAG);

        sb.appendJoin(LeftJoin.createLeftJoin().appendFields(Field.createField(procedures, "PROCEDURE_NAME"),
                Field.createField(parameters, "PROCEDURE_NAME")).setCondition(Condition.createCondition(Field.createField(parameters, "PACKAGE_NAME"), "IS", "NULL")));
        sb.appendJoin(LeftJoin.createLeftJoin().appendFields(Field.createField(parameters, FIELD_SOURCE), Field.createField(fields, FIELD_NAME)));
        sb.appendJoin(LeftJoin.createLeftJoin().appendFields(Field.createField(fields, CHARACTER_SET_ID), Field.createField(charsets, CHARACTER_SET_ID)));
        sb.appendJoin(LeftJoin.createLeftJoin().appendFields(Field.createField(fields, "COLLATION_ID"), Field.createField(collations1, "COLLATION_ID"))
                .appendFields(Field.createField(fields, CHARACTER_SET_ID), Field.createField(collations1, CHARACTER_SET_ID)));
        sb.appendJoin(LeftJoin.createLeftJoin().appendFields(Field.createField(parameters, "COLLATION_ID"), Field.createField(collations2, "COLLATION_ID"))
                .appendFields(Field.createField(fields, CHARACTER_SET_ID), Field.createField(collations2, CHARACTER_SET_ID)));
        sb.appendCondition(buildNameCondition(procedures, "PROCEDURE_NAME"));
        sb.appendCondition(Condition.createCondition(Field.createField(procedures, "PACKAGE_NAME"), "IS", "NULL"));
        sb.setOrdering(Field.createField(parameters, PARAMETER_NUMBER).getFieldTable());


        String sql = sb.getSQLQuery();
        /*if (getDatabaseMajorVersion() >= 3)
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
                    "order by pp.rdb$parameter_number";*/
        return sql;
    }

    @Override
    protected void setInfoFromResultSet(ResultSet rs) {
        try {
            parameters = new ArrayList<>();
            procedureInputParameters = new ArrayList<>();
            procedureOutputParameters = new ArrayList<>();
            boolean first = true;
            while (rs.next()) {
                String parameterName = rs.getString(PP + PARAMETER_NAME);
                if (parameterName != null) {
                    ProcedureParameter pp = new ProcedureParameter(parameterName.trim(),
                            rs.getInt(PP + PARAMETER_TYPE) == 0 ? DatabaseMetaData.procedureColumnIn : DatabaseMetaData.procedureColumnOut,
                            DatabaseTypeConverter.getSqlTypeFromRDBType(rs.getInt(FIELD_TYPE), rs.getInt(FIELD_SUB_TYPE)),
                            DatabaseTypeConverter.getDataTypeName(rs.getInt(FIELD_TYPE), rs.getInt(FIELD_SUB_TYPE), rs.getInt(FIELD_SCALE)),
                            rs.getInt(FIELD_LENGTH),
                            1 - rs.getInt(PP + NULL_FLAG));
                    if (rs.getInt(FIELD_PRECISION) != 0)
                        pp.setSize(rs.getInt(FIELD_PRECISION));
                    if (rs.getInt(CHARACTER_LENGTH) != 0)
                        pp.setSize(rs.getInt(CHARACTER_LENGTH));
                    if (pp.getDataType() == Types.LONGVARBINARY ||
                            pp.getDataType() == Types.LONGVARCHAR ||
                            pp.getDataType() == Types.BLOB) {
                        pp.setSubType(rs.getInt(FIELD_SUB_TYPE));
                        pp.setSize(rs.getInt(SEGMENT_LENGTH));
                    }

                    String domain = rs.getString(FIELD_NAME);
                    if (domain != null && !domain.startsWith("RDB$"))
                        pp.setDomain(domain.trim());
                    pp.setTypeOf(rs.getInt(PP + PARAMETER_MECHANISM) == 1);
                    String relationName = rs.getString(PP + RELATION_NAME);
                    if (relationName != null)
                        pp.setRelationName(relationName.trim());
                    String fieldName = rs.getString(PP + FIELD_NAME);
                    if (fieldName != null)
                        pp.setFieldName(fieldName.trim());

                    if (pp.getRelationName() != null && pp.getFieldName() != null)
                        pp.setTypeOfFrom(ColumnData.TYPE_OF_FROM_COLUMN);
                    String characterSet = rs.getString(CHARACTER_SET_NAME);
                    if (characterSet != null && !characterSet.isEmpty() && !characterSet.contains("NONE"))
                        pp.setEncoding(characterSet.trim());
                    pp.setDefaultValue(rs.getString(PP + DEFAULT_SOURCE));
                    if (pp.getDefaultValue() == null)
                        pp.setDefaultValue(rs.getString(DEFAULT_SOURCE));
                    if (pp.getType() == DatabaseMetaData.procedureColumnIn)
                        procedureInputParameters.add(pp);
                    else if (pp.getType() == DatabaseMetaData.procedureColumnOut)
                        procedureOutputParameters.add(pp);
                    parameters.add(pp);
                }
                if (first) {
                    sourceCode = getFromResultSet(rs, PROCEDURE_SOURCE);
                    entryPoint = getFromResultSet(rs, ENTRYPOINT);
                    engine = getFromResultSet(rs, ENGINE_NAME);
                    setRemarks(getFromResultSet(rs, DESCRIPTION));
                    setSqlSecurity(getFromResultSet(rs, SQL_SECURITY));
                    setAuthid(getFromResultSet(rs, AUTHID));
                    first = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
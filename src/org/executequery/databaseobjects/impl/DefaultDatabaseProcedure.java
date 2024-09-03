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
import org.executequery.gui.browser.comparer.Comparer;
import org.executequery.sql.sqlbuilder.*;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.SQLUtils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Default database procedure implementation.
 *
 * @author Takis Diakoumis
 */
public class DefaultDatabaseProcedure extends DefaultDatabaseExecutable
        implements DatabaseProcedure {

    public static final int SELECTABLE = 1;

    /**
     * Creates a new instance of DefaultDatabaseProcedure
     */
    public DefaultDatabaseProcedure(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
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

    @Override
    protected String prefixLabel() {
        return PP;
    }

    @Override
    protected String mechanismLabel() {
        return PARAMETER_MECHANISM;
    }

    @Override
    protected String positionLabel() {
        return PARAMETER_NUMBER;
    }

    @Override
    public String getCreateSQLText() {
        return SQLUtils.generateCreateProcedure(
                getName(),
                getEntryPoint(),
                getEngine(),
                getParameters(),
                getSqlSecurity(),
                getAuthid(),
                getSourceCode(),
                getRemarks(),
                getHost().getDatabaseConnection(),
                false,
                true
        );
    }

    @Override
    public String getCreateSQLTextWithoutComment() throws DataSourceException {
        return SQLUtils.generateCreateProcedure(
                getName(),
                getEntryPoint(),
                getEngine(),
                getParameters(),
                getSqlSecurity(),
                getAuthid(),
                getSourceCode(),
                null,
                getHost().getDatabaseConnection(),
                false,
                false
        );
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {
        return SQLUtils.generateCreateProcedure(
                getName(),
                getEntryPoint(),
                getEngine(),
                getParameters(),
                getSqlSecurity(),
                getAuthid(),
                getSourceCode(),
                Comparer.isCommentsNeed() ? getRemarks() : null,
                getHost().getDatabaseConnection(),
                true,
                Comparer.isCommentsNeed()
        );
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("PROCEDURE", getName(), getHost().getDatabaseConnection());
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {
        return (!this.getCompareCreateSQL().equals(databaseObject.getCompareCreateSQL())) ?
                databaseObject.getCompareCreateSQL() : SQLUtils.THERE_ARE_NO_CHANGES;
    }

    protected static final String PP = "PP_";
    protected static final String PROCEDURE_SOURCE = "PROCEDURE_SOURCE";
    protected static final String PROCEDURE_CONTEXT = "PROCEDURE_CONTEXT";
    protected static final String AUTHID = "AUTHID";
    protected static final String PARAMETER_NAME = "PARAMETER_NAME";
    protected static final String PARAMETER_TYPE = "PARAMETER_TYPE";
    protected static final String PARAMETER_NUMBER = "PARAMETER_NUMBER";
    protected static final String PARAMETER_MECHANISM = "PARAMETER_MECHANISM";
    public static final int EXECUTABLE = 2;
    protected static final String PROCEDURE_TYPE = "PROCEDURE_TYPE";
    private int procedureType;

    @Override
    protected String getFieldName() {
        return "PROCEDURE_NAME";
    }

    @Override
    protected Table getMainTable() {
        return Table.createTable("RDB$PROCEDURES", "PRC");
    }

    @Override
    protected SelectBuilder builderCommonQuery() {
        SelectBuilder sb = SelectBuilder.createSelectBuilder(getHost().getDatabaseConnection());
        Table procedures = getMainTable();
        Table parameters = Table.createTable("RDB$PROCEDURE_PARAMETERS", "PP");
        Table fields = Table.createTable("RDB$FIELDS", "F");
        Table charsets = Table.createTable("RDB$CHARACTER_SETS", "CR");
        Table collations1 = Table.createTable("RDB$COLLATIONS", "CO1");
        Table collations2 = Table.createTable("RDB$COLLATIONS", "CO2");
        sb.appendField(Field.createField(procedures, getFieldName()).setCast("VARCHAR(1024)"));
        sb.appendFields(procedures, PROCEDURE_SOURCE, PROCEDURE_TYPE, DESCRIPTION, VALID_BLR);
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
        sb.appendFields(charsets, CHARACTER_SET_NAME, DEFAULT_COLLATE_NAME, BYTES_PER_CHARACTER);
        sb.appendField(Field.createField(fields, "CHARACTER_LENGTH").setAlias(CHARACTER_LENGTH));
        sb.appendField(Field.createField(collations1, COLLATION_NAME).setAlias("CO1_" + COLLATION_NAME));
        sb.appendField(Field.createField(collations2, COLLATION_NAME).setAlias("CO2_" + COLLATION_NAME));
        sb.appendFields(PP, parameters, !moreOrEqualsVersionCheck(2, 5), PARAMETER_MECHANISM, DEFAULT_SOURCE, RELATION_NAME, FIELD_NAME, NULL_FLAG);
        Join procParamJoin = Join.createLeftJoin().appendFields(Field.createField(procedures, "PROCEDURE_NAME"),
                Field.createField(parameters, "PROCEDURE_NAME"));
        if (getDatabaseMajorVersion() > 2)
            procParamJoin.setCondition(Condition.createCondition(Field.createField(parameters, "PACKAGE_NAME"), "IS", "NULL"));
        sb.appendJoin(procParamJoin);
        sb.appendJoin(Join.createLeftJoin().appendFields(Field.createField(parameters, FIELD_SOURCE), Field.createField(fields, FIELD_NAME)));
        sb.appendJoin(Join.createLeftJoin().appendFields(Field.createField(fields, CHARACTER_SET_ID), Field.createField(charsets, CHARACTER_SET_ID)));
        sb.appendJoin(Join.createLeftJoin().appendFields(Field.createField(fields, "COLLATION_ID"), Field.createField(collations1, "COLLATION_ID"))
                .appendFields(Field.createField(fields, CHARACTER_SET_ID), Field.createField(collations1, CHARACTER_SET_ID)));
        sb.appendJoin(Join.createLeftJoin().appendFields(Field.createField(parameters, "COLLATION_ID"), Field.createField(collations2, "COLLATION_ID"))
                .appendFields(Field.createField(fields, CHARACTER_SET_ID), Field.createField(collations2, CHARACTER_SET_ID)));
        if (getDatabaseMajorVersion() > 2)
            sb.appendCondition(Condition.createCondition(Field.createField(procedures, "PACKAGE_NAME"), "IS", "NULL"));
        sb.setOrdering(getObjectField().getFieldTable() + ", " + Field.createField(parameters, PARAMETER_NUMBER).getFieldTable());
        return sb;
    }

    @Override
    public Object setInfoFromSingleRowResultSet(ResultSet rs, boolean first) throws SQLException {
        String parameterName = rs.getString(PP + PARAMETER_NAME);
        if (parameterName != null) {
            ProcedureParameter pp = new ProcedureParameter(parameterName.trim(), rs.getInt(PP + PARAMETER_TYPE) == 0 ? DatabaseMetaData.procedureColumnIn : DatabaseMetaData.procedureColumnOut);
            pp = (ProcedureParameter) fillParameter(pp, rs);
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
            setValid(rs.getInt(VALID_BLR) == 1);
            setProcedureType(rs.getInt(PROCEDURE_TYPE));
        }
        return null;
    }

    @Override
    public void prepareLoadingInfo() {
        parameters = new ArrayList<>();
        procedureInputParameters = new ArrayList<>();
        procedureOutputParameters = new ArrayList<>();
    }

    @Override
    public void finishLoadingInfo() {
        if (getProcedureType() == 0)
            setProcedureType(procedureOutputParameters.isEmpty() ? EXECUTABLE : SELECTABLE);
    }

    @Override
    public boolean isAnyRowsResultSet() {
        return true;
    }

    public int getProcedureType() {
        return procedureType;
    }

    public void setProcedureType(int procedureType) {
        this.procedureType = procedureType;
    }
}
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
import org.executequery.databaseobjects.FunctionArgument;
import org.executequery.databaseobjects.Parameter;
import org.executequery.gui.browser.comparer.Comparer;
import org.executequery.sql.sqlbuilder.*;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.SQLUtils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    FunctionArgument returnArgument;

    @Override
    protected String prefixLabel() {
        return FA;
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
                getName(),
                getFunctionArguments(),
                getSourceCode(),
                getEntryPoint(),
                getEngine(),
                getSqlSecurity(),
                getRemarks(),
                false,
                true,
                isDeterministic(),
                getHost().getDatabaseConnection()
        );
    }

    @Override
    public String getCreateSQLTextWithoutComment() {
        return SQLUtils.generateCreateFunction(
                getName(),
                getFunctionArguments(),
                getSourceCode(),
                getEntryPoint(),
                getEngine(),
                getSqlSecurity(),
                null,
                false,
                false,
                isDeterministic(),
                getHost().getDatabaseConnection()
        );
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {
        return SQLUtils.generateCreateFunction(
                getName(),
                getFunctionArguments(),
                getSourceCode(),
                getEntryPoint(),
                getEngine(),
                getSqlSecurity(),
                Comparer.isCommentsNeed() ? getRemarks() : null,
                true,
                Comparer.isCommentsNeed(),
                isDeterministic(),
                getHost().getDatabaseConnection()
        );
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("FUNCTION", getName(), getHost().getDatabaseConnection());
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {
        return (!this.getCompareCreateSQL().equals(databaseObject.getCompareCreateSQL())) ?
                databaseObject.getCompareCreateSQL() : SQLUtils.THERE_ARE_NO_CHANGES;
    }

    protected static final String FA = "FA_";
    protected static final String PROCEDURE_SOURCE = "FUNCTION_SOURCE";
    protected static final String PARAMETER_NAME = "ARGUMENT_NAME";
    protected static final String PARAMETER_NUMBER = "ARGUMENT_POSITION";
    protected static final String PARAMETER_MECHANISM = "ARGUMENT_MECHANISM";
    protected static final String DETERMINISTIC_FLAG = "DETERMINISTIC_FLAG";
    protected static final String RETURN_ARGUMENT = "RETURN_ARGUMENT";


    @Override
    protected String getFieldName() {
        return "FUNCTION_NAME";
    }

    @Override
    protected Table getMainTable() {
        return Table.createTable("RDB$FUNCTIONS", "FNC");
    }

    @Override
    protected SelectBuilder builderForInfoAllObjects(SelectBuilder commonBuilder) {
        SelectBuilder sb = super.builderForInfoAllObjects(commonBuilder);
        if (!(this instanceof DefaultDatabaseUDF))
            sb.appendCondition(Condition.createCondition(Field.createField(getMainTable(), "MODULE_NAME"), "IS", "NULL"));
        return sb;
    }

    @Override
    protected SelectBuilder builderCommonQuery() {
        SelectBuilder sb = SelectBuilder.createSelectBuilder(getHost().getDatabaseConnection());
        Table functions = getMainTable();
        Table arguments = Table.createTable("RDB$FUNCTION_ARGUMENTS", "FA");
        Table fields = Table.createTable("RDB$FIELDS", "F");
        Table charsets = Table.createTable("RDB$CHARACTER_SETS", "CR");
        Table collations1 = Table.createTable("RDB$COLLATIONS", "CO1");
        Table collations2 = Table.createTable("RDB$COLLATIONS", "CO2");
        sb.appendField(Field.createField(functions, getFieldName()).setCast("VARCHAR(1024)"));
        sb.appendFields(functions, PROCEDURE_SOURCE, DESCRIPTION, DETERMINISTIC_FLAG, RETURN_ARGUMENT, VALID_BLR);
        sb.appendFields(functions, !externalCheck(), ENGINE_NAME, ENTRYPOINT);
        sb.appendField(buildSqlSecurityField(functions));
        sb.appendFields(FA, arguments, PARAMETER_NAME, PARAMETER_NUMBER, FIELD_SOURCE, DESCRIPTION, PARAMETER_MECHANISM, DEFAULT_SOURCE, RELATION_NAME, FIELD_NAME, NULL_FLAG);
        sb.appendFields(fields, FIELD_NAME, FIELD_TYPE, FIELD_LENGTH, FIELD_SCALE, FIELD_SUB_TYPE, SEGMENT_LENGTH, DIMENSIONS, FIELD_PRECISION, DEFAULT_SOURCE);
        sb.appendFields(charsets, CHARACTER_SET_NAME, DEFAULT_COLLATE_NAME, BYTES_PER_CHARACTER);
        sb.appendField(Field.createField(fields, "CHARACTER_LENGTH").setAlias(CHARACTER_LENGTH));
        sb.appendField(Field.createField(collations1, COLLATION_NAME).setAlias("CO1_" + COLLATION_NAME));
        sb.appendField(Field.createField(collations2, COLLATION_NAME).setAlias("CO2_" + COLLATION_NAME));

        sb.appendJoin(Join.createLeftJoin().appendFields(Field.createField(functions, getFieldName()),
                Field.createField(arguments, getFieldName())).setCondition(Condition.createCondition(Field.createField(arguments, "PACKAGE_NAME"), "IS", "NULL")));
        sb.appendJoin(Join.createLeftJoin().appendFields(Field.createField(arguments, FIELD_SOURCE), Field.createField(fields, FIELD_NAME)));
        sb.appendJoin(Join.createLeftJoin().appendFields(Field.createField(fields, CHARACTER_SET_ID), Field.createField(charsets, CHARACTER_SET_ID)));
        sb.appendJoin(Join.createLeftJoin().appendFields(Field.createField(fields, "COLLATION_ID"), Field.createField(collations1, "COLLATION_ID"))
                .appendFields(Field.createField(fields, CHARACTER_SET_ID), Field.createField(collations1, CHARACTER_SET_ID)));
        sb.appendJoin(Join.createLeftJoin().appendFields(Field.createField(arguments, "COLLATION_ID"), Field.createField(collations2, "COLLATION_ID"))
                .appendFields(Field.createField(fields, CHARACTER_SET_ID), Field.createField(collations2, CHARACTER_SET_ID)));
        sb.appendCondition(Condition.createCondition(Field.createField(functions, "PACKAGE_NAME"), "IS", "NULL"));
        sb.setOrdering(getObjectField().getFieldTable() + ", " + Field.createField(arguments, PARAMETER_NUMBER).getFieldTable());
        return sb;
    }

    @Override
    protected String mechanismLabel() {
        return PARAMETER_MECHANISM;
    }

    @Override
    public Object setInfoFromSingleRowResultSet(ResultSet rs, boolean first) throws SQLException {
        String parameterName = rs.getString(FA + PARAMETER_NAME);
        if (parameterName != null)
            parameterName = parameterName.trim();
        FunctionArgument fp = new FunctionArgument(parameterName);
        fp = (FunctionArgument) fillParameter(fp, rs);
        arguments.add(fp);
        if (first) {
            sourceCode = getFromResultSet(rs, PROCEDURE_SOURCE);
            entryPoint = getFromResultSet(rs, ENTRYPOINT);
            engine = getFromResultSet(rs, ENGINE_NAME);
            setRemarks(getFromResultSet(rs, DESCRIPTION));
            setSqlSecurity(getFromResultSet(rs, SQL_SECURITY));
            setDeterministic(rs.getInt(DETERMINISTIC_FLAG) == 1);
            setValid(rs.getInt(VALID_BLR) == 1);
        }
        return null;
    }

    public FunctionArgument getReturnArgument() {
        checkOnReload(returnArgument);
        return returnArgument;
    }

    protected Parameter fillParameter(Parameter pp, ResultSet rs) throws SQLException {
        pp = super.fillParameter(pp, rs);
        int return_arg = rs.getInt(RETURN_ARGUMENT);
        if (return_arg == pp.getPosition()) {
            pp.setType(DatabaseMetaData.procedureColumnReturn);
            returnArgument = (FunctionArgument) pp;
        } else pp.setType(DatabaseMetaData.procedureColumnIn);
        return pp;
    }

    @Override
    public void prepareLoadingInfo() {
        arguments = new ArrayList<>();
    }

    @Override
    public void finishLoadingInfo() {

    }

    @Override
    public boolean isAnyRowsResultSet() {
        return true;
    }

    public boolean isDeterministic() {
        return deterministic;
    }

    public void setDeterministic(boolean deterministic) {
        this.deterministic = deterministic;
    }

    @Override
    protected String positionLabel() {
        return PARAMETER_NUMBER;
    }
}













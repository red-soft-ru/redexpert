package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;
import org.executequery.databaseobjects.UDFParameter;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.comparer.Comparer;
import org.executequery.sql.sqlbuilder.*;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.SQLUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vasiliy on 13.02.17.
 */
@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "unused"})
public class DefaultDatabaseUDF extends DefaultDatabaseFunction
        implements DatabaseProcedure {

    private final static String MODULE_NAME = "MODULE_NAME";
    private static final String PARAMETER_MECHANISM = "MECHANISM";
    private static final String BYTES_PER_CHARACTER = "BYTES_PER_CHARACTER";
    private static final String[] MECHANISMS = {
            "BY VALUE",
            "BY REFERENCE",
            "BY DESCRIPTOR",
            "BY BLOB DESCRIPTOR"
    };

    public static final int BY_VALUE = 0;
    public static final int BY_REFERENCE = BY_VALUE + 1;
    public static final int BY_DESCRIPTOR = BY_REFERENCE + 1;
    public static final int BY_BLOB_DESCRIPTOR = BY_DESCRIPTOR + 1;
    public static final int BY_SCALAR_ARRAY_DESCRIPTOR = BY_BLOB_DESCRIPTOR + 1;
    public static final int BY_REFERENCE_WITH_NULL = BY_SCALAR_ARRAY_DESCRIPTOR + 1;

    private int returnArg;
    private boolean freeIt;
    private String returns;
    private String moduleName;
    private String returnMechanism;
    private String inputParameters;
    private final List<UDFParameter> parameters;

    public DefaultDatabaseUDF(DatabaseMetaTag metaTagParent, String name, DatabaseHost host) {
        super(metaTagParent, name);

        freeIt = false;
        parameters = new ArrayList<>();

        returns = "";
        returnMechanism = "";
        inputParameters = "";

        setHost(host);
    }

    @Override
    public String getCreateSQLText() {
        return SQLUtils.generateCreateUDF(
                getName(),
                parameters,
                returnArg,
                getEntryPoint(),
                getModuleName(),
                freeIt,
                getRemarks(),
                true,
                getHost().getDatabaseConnection()
        );
    }

    @Override
    public String getCreateSQLTextWithoutComment() throws DataSourceException {
        return SQLUtils.generateCreateUDF(
                getName(),
                parameters,
                returnArg,
                getEntryPoint(),
                getModuleName(),
                freeIt,
                null,
                false,
                getHost().getDatabaseConnection()
        );
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("UDF", getName(), getHost().getDatabaseConnection());
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {
        return Comparer.isCommentsNeed() ?
                getCreateSQLText() :
                getCreateSQLTextWithoutComment();
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {
        DefaultDatabaseUDF comparingUDF = (DefaultDatabaseUDF) databaseObject;
        return SQLUtils.generateAlterUDF(this, comparingUDF, Comparer.isCommentsNeed());
    }

    @Override
    protected SelectBuilder builderCommonQuery() {
        SelectBuilder sb = new SelectBuilder(getHost().getDatabaseConnection());

        Table functions = getMainTable();
        Table arguments = Table.createTable("RDB$FUNCTION_ARGUMENTS", "FA");
        Table charsets = Table.createTable("RDB$CHARACTER_SETS", "CR");
        Table collations1 = Table.createTable("RDB$COLLATIONS", "CO1");
        Table collations2 = Table.createTable("RDB$COLLATIONS", "CO2");

        sb.appendFields(functions, getFieldName(), DESCRIPTION, RETURN_ARGUMENT, MODULE_NAME, ENTRYPOINT);
        sb.appendField(buildSqlSecurityField(functions));
        sb.appendFields(FA, arguments, getDatabaseMajorVersion() < 3 && !isRDB(), PARAMETER_NAME, DESCRIPTION, PARAMETER_MECHANISM, mechanismLabel(), DEFAULT_SOURCE, RELATION_NAME, FIELD_NAME);
        sb.appendFields(FA, arguments, PARAMETER_NUMBER);
        sb.appendFields(arguments, getDatabaseMajorVersion() < 3 && !isRDB(), DEFAULT_SOURCE, FIELD_NAME);
        sb.appendFields(arguments, FIELD_TYPE, FIELD_LENGTH, FIELD_SCALE, FIELD_SUB_TYPE, FIELD_PRECISION);
        sb.appendFields(charsets, CHARACTER_SET_NAME, DEFAULT_COLLATE_NAME);
        sb.appendField(Field.createField(arguments, "CHARACTER_LENGTH").setAlias(CHARACTER_LENGTH));
        sb.appendField(Field.createField(collations1, COLLATION_NAME).setAlias("CO1_" + COLLATION_NAME).setNull(getDatabaseMajorVersion() < 3 && !isRDB()));
        sb.appendField(Field.createField(collations2, COLLATION_NAME).setAlias("CO2_" + COLLATION_NAME).setNull(getDatabaseMajorVersion() < 3 && !isRDB()));
        sb.appendField(Field.createField(arguments, NULL_FLAG).setNull(getDatabaseMajorVersion() < 3).setAlias(prefixLabel() + NULL_FLAG));
        sb.appendFields(charsets, CHARACTER_SET_NAME, BYTES_PER_CHARACTER);

        sb.appendJoin(Join.createLeftJoin().appendFields(Field.createField(functions, getFieldName()), Field.createField(arguments, getFieldName())));
        sb.appendJoin(Join.createLeftJoin().appendFields(Field.createField(arguments, CHARACTER_SET_ID), Field.createField(charsets, CHARACTER_SET_ID)));

        if (isRDB() || getDatabaseMajorVersion() >= 3) {
            sb.appendJoin(Join.createLeftJoin()
                    .appendFields(Field.createField(arguments, "COLLATION_ID"), Field.createField(collations1, "COLLATION_ID"))
                    .appendFields(Field.createField(arguments, CHARACTER_SET_ID), Field.createField(collations1, CHARACTER_SET_ID))
            );
            sb.appendJoin(Join.createLeftJoin()
                    .appendFields(Field.createField(arguments, "COLLATION_ID"), Field.createField(collations2, "COLLATION_ID"))
                    .appendFields(Field.createField(arguments, CHARACTER_SET_ID), Field.createField(collations2, CHARACTER_SET_ID))
            );
        }

        sb.setOrdering(getObjectField().getFieldTable() + ", " + Field.createField(arguments, PARAMETER_NUMBER).getFieldTable());

        return sb;
    }

    @Override
    protected SelectBuilder builderForInfoAllObjects(SelectBuilder commonBuilder) {

        SelectBuilder sb = super.builderForInfoAllObjects(commonBuilder);
        if (getDatabaseMajorVersion() >= 3) {
            sb.appendCondition(Condition.createCondition(Field.createField(getMainTable(), "MODULE_NAME"), "IS", "NOT NULL"));
            sb.appendCondition(Condition.createCondition(Field.createField(getMainTable(), "LEGACY_FLAG"), "=", "1"));
        }

        return sb;
    }

    @Override
    public Object setInfoFromSingleRowResultSet(ResultSet rs, boolean first) throws SQLException {

        UDFParameter udfParameter = new UDFParameter(rs.getInt(FA + PARAMETER_MECHANISM), rs.getInt(FIELD_TYPE));
        udfParameter = (UDFParameter) fillParameter(udfParameter, rs);

        int nullFlag = rs.getInt(prefixLabel() + NULL_FLAG);
        if (rs.getInt(FA + PARAMETER_MECHANISM) != BY_REFERENCE_WITH_NULL) // already setup
            udfParameter.setNotNull(nullFlag != 0);

        parameters.add(udfParameter);
        if (first) {
            setRemarks(getFromResultSet(rs, DESCRIPTION));
            setReturnArg(rs.getInt(RETURN_ARGUMENT));
            setModuleName(getFromResultSet(rs, MODULE_NAME));
            setEntryPoint(getFromResultSet(rs, ENTRYPOINT));
        }

        return null;
    }

    @Override
    public void prepareLoadingInfo() {
        parameters.clear();
        inputParameters = "";
    }

    @Override
    public void finishLoadingInfo() {

        if (returnArg != 0) {
            returnMechanism = parameters.get(returnArg - 1).getStringMechanism();
            if (parameters.get(returnArg - 1).getMechanism() == -1)
                this.freeIt = true;

        } else {
            returnMechanism = parameters.get(0).getStringMechanism();
            if (parameters.get(0).getMechanism() == -1)
                this.freeIt = true;
        }

        Map<UDFParameter, ColumnData> mapParameters = new HashMap<>();
        for (UDFParameter parameter : parameters) {
            ColumnData cd = SQLUtils.columnDataFromProcedureParameter(parameter, getHost().getDatabaseConnection(), false);
            mapParameters.put(parameter, cd);
        }

        StringBuilder inputParamenersBuilder = new StringBuilder();
        for (int i = 0; i < parameters.size(); i++) {
            UDFParameter parameter = parameters.get(i);

            if (returnArg == 0 && i == 0)
                continue;

            inputParamenersBuilder.append(mapParameters.get(parameter).getFormattedDataType());
            if (!byReference(parameter) && !byValue(parameter))
                inputParamenersBuilder.append(" ").append(parameter.getStringMechanism());
            inputParamenersBuilder.append(", ");
        }
        if (!inputParamenersBuilder.toString().isEmpty())
            inputParameters = inputParamenersBuilder.substring(0, inputParamenersBuilder.length() - 2);

        if (returnArg != 0) {
            returns = "Parameter " + returnArg;

        } else {
            UDFParameter parameter = parameters.get(0);

            returns = mapParameters.get(parameter).getFormattedDataType();
            if (!byReference(parameter) && parameter.getMechanism() != -1)
                returns += " " + parameter.getStringMechanism();
        }
    }

    @Override
    public int getType() {
        return UDF;
    }

    // --- get/set methods ---

    public static String getMechanism(int mechanism) {

        if (mechanism >= 0 && mechanism < MECHANISMS.length)
            return MECHANISMS[mechanism];
        else if (mechanism == -1)
            return MECHANISMS[1];

        return "";
    }

    public String getModuleName() {
        checkOnReload(moduleName);
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public int getReturnArg() {
        return returnArg;
    }

    public void setReturnArg(int returnArg) {
        this.returnArg = returnArg;
    }

    public String getReturnMechanism() {
        checkOnReload(returnMechanism);
        return returnMechanism;
    }

    public String getReturns() {
        checkOnReload(returns);
        return returns;
    }

    public String getInputParameters() {
        return inputParameters;
    }

    public List<UDFParameter> getUDFParameters() {
        checkOnReload(parameters);
        return parameters;
    }

    public boolean getFreeIt() {
        return freeIt;
    }

    public static boolean byValue(UDFParameter parameter) {
        return parameter.getMechanism() == DefaultDatabaseUDF.BY_VALUE;
    }

    public static boolean byReference(UDFParameter parameter) {
        return parameter.getMechanism() == DefaultDatabaseUDF.BY_REFERENCE;
    }

    public static boolean byDescriptor(UDFParameter parameter) {
        return parameter.getMechanism() == DefaultDatabaseUDF.BY_DESCRIPTOR;
    }

    public static boolean byBlobDescriptor(UDFParameter parameter) {
        return parameter.getMechanism() == DefaultDatabaseUDF.BY_BLOB_DESCRIPTOR;
    }

    public static boolean byScalarArrayDescriptor(UDFParameter parameter) {
        return parameter.getMechanism() == DefaultDatabaseUDF.BY_SCALAR_ARRAY_DESCRIPTOR;
    }

    public static boolean byReferenceWithNull(UDFParameter parameter) {
        return parameter.getMechanism() == DefaultDatabaseUDF.BY_REFERENCE_WITH_NULL;
    }

}

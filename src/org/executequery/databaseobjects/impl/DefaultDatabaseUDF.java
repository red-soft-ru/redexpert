package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;
import org.executequery.databaseobjects.UDFParameter;
import org.executequery.sql.sqlbuilder.*;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.SQLUtils;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by vasiliy on 13.02.17.
 */

public class
DefaultDatabaseUDF extends DefaultDatabaseFunction
        implements DatabaseProcedure {
    public static final String[] mechanisms = {"BY VALUE", "BY REFERENCE", "BY DESCRIPTOR", "BY BLOB DESCRIPTOR"};
    public static final int BY_VALUE = 0;
    public static final int BY_REFERENCE = 1;
    public static final int BY_DESCRIPTOR = 2;
    public static final int BY_BLOB_DESCRIPTOR = 3;
    public static final int BY_SCALAR_ARRAY_DESCRIPTOR = 4;
    public static final int BY_REFERENCE_WITH_NULL = 5;

    // TODO why so many methods??? WHY???
    public static String getStringMechanismFromInt(int mechanism) {
        if (mechanism >= 0 && mechanism < mechanisms.length)
            return mechanisms[mechanism];
        else if (mechanism == -1) {
            return mechanisms[1];
        } else
            return "";
    }

    public static class UDFTableModel implements TableModel {

        private final Set<TableModelListener> listeners = new HashSet<>();

        private final List<DefaultDatabaseUDF> udfs;

        public UDFTableModel(List<DefaultDatabaseUDF> udf) {
            this.udfs = udf;
        }

        public void addTableModelListener(TableModelListener listener) {
            listeners.add(listener);
        }

        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        public int getColumnCount() {
            return 7;
        }

        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return "Name";
                case 1:
                    return "Library Name";
                case 2:
                    return "Entry Point";
                case 3:
                    return "Input Parameters";
                case 4:
                    return "Returns";
                case 5:
                    return "Return Mechanism";
                case 6:
                    return "Free it";
                case 7:
                    return "Description";
            }
            return "";
        }

        public int getRowCount() {
            return udfs.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            DefaultDatabaseUDF udf = udfs.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return udf.getName();
                case 1:
                    return udf.getModuleName();
                case 2:
                    return udf.getEntryPoint();
                case 3:
                    return udf.getInputParameters();
                case 4:
                    return udf.getReturns();
                case 5:
                    return udf.getReturnMechanism();
                case 6:
                    return udf.getFreeIt();
                case 7:
                    return udf.getRemarks();
            }
            return "";
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        public void removeTableModelListener(TableModelListener listener) {
            listeners.remove(listener);
        }

        public void setValueAt(Object value, int rowIndex, int columnIndex) {

        }

    }

    private String moduleName;
    private int returnArg;

    private String returnMechanism = "";
    private String returns = "";
    private String inputParameters = "";
    private Boolean freeIt = false;

    List<UDFParameter> parameters = new ArrayList<>();

    /**
     * Creates a new instance.
     */
    public DefaultDatabaseUDF(DatabaseMetaTag metaTagParent, String name, DatabaseHost host) {
        super(metaTagParent, name);
        setHost(host);
    }




    public String getModuleName() {
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
        return returnMechanism;
    }

    public String getReturns() {
        return returns;
    }

    public String getInputParameters() {
        return inputParameters;
    }

    public List<UDFParameter> getUDFParameters() {
        return this.parameters;
    }

    public int getType() {
        return UDF;
    }

    public Boolean getFreeIt() {
        return this.freeIt;
    }

    public String getCreateSQLText() {
        return SQLUtils.generateCreateUDF(getName(), parameters, returnArg, getEntryPoint(), getModuleName(), freeIt);
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("UDF", getName());
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {
        return getCreateSQLText();
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {
        DefaultDatabaseUDF comparingUDF = (DefaultDatabaseUDF) databaseObject;
        return SQLUtils.generateAlterUDF(this, comparingUDF);
    }


    protected final static String MODULE_NAME = "MODULE_NAME";
    protected static final String PARAMETER_MECHANISM = "MECHANISM";
    protected static final String BYTES_PER_CHARACTER = "BYTES_PER_CHARACTER";

    @Override
    protected SelectBuilder builderCommonQuery() {


        SelectBuilder sb = new SelectBuilder();
        Table functions = getMainTable();
        Table arguments = Table.createTable("RDB$FUNCTION_ARGUMENTS", "FA");
        Table charsets = Table.createTable("RDB$CHARACTER_SETS", "CR");
        sb.appendFields(functions, getFieldName(), DESCRIPTION, RETURN_ARGUMENT, MODULE_NAME, ENTRYPOINT);
        sb.appendFields(functions, !externalCheck(), ENGINE_NAME, ENTRYPOINT);
        sb.appendField(buildSqlSecurityField(functions));
        sb.appendFields(arguments, PARAMETER_NUMBER, FIELD_TYPE, FIELD_SCALE, FIELD_LENGTH, FIELD_SUB_TYPE, FIELD_PRECISION, PARAMETER_MECHANISM);
        sb.appendField(Field.createField(arguments, NULL_FLAG).setNull(getDatabaseMajorVersion() < 3));
        sb.appendFields(charsets, CHARACTER_SET_NAME, BYTES_PER_CHARACTER);

        sb.appendJoin(LeftJoin.createLeftJoin().appendFields(Field.createField(functions, getFieldName()),
                Field.createField(arguments, getFieldName())));
        sb.appendJoin(LeftJoin.createLeftJoin().appendFields(Field.createField(arguments, CHARACTER_SET_ID), Field.createField(charsets, CHARACTER_SET_ID)));
        sb.setOrdering(getObjectField().getFieldTable() + ", " + Field.createField(arguments, PARAMETER_NUMBER).getFieldTable());
        return sb;
    }

    @Override
    protected SelectBuilder builderForInfoAllObjects() {
        SelectBuilder sb = super.builderForInfoAllObjects();
        if (getDatabaseMajorVersion() > 3) {
            sb.appendCondition(Condition.createCondition(Field.createField(getMainTable(), "MODULE_NAME"), "IS", "NOT NULL"));
            sb.appendCondition(Condition.createCondition(Field.createField(getMainTable(), "LEGACY_FLAG"), "=", "1"));
        }
        return sb;
    }

    @Override
    public Object setInfoFromSingleRowResultSet(ResultSet rs, boolean first) throws SQLException {
        UDFParameter udfParameter = new UDFParameter(rs.getInt(PARAMETER_NUMBER),
                rs.getInt(PARAMETER_MECHANISM), rs.getInt(FIELD_TYPE), rs.getInt(FIELD_SCALE),
                rs.getInt(FIELD_LENGTH), rs.getInt(FIELD_SUB_TYPE), rs.getInt(FIELD_PRECISION));
        int nullFlag = rs.getInt(NULL_FLAG);
        if (rs.getInt(7) != BY_REFERENCE_WITH_NULL) // already setup
            udfParameter.setNotNull(nullFlag != 0);
        udfParameter.setEncoding(rs.getString(CHARACTER_SET_NAME));
        parameters.add(udfParameter);
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

        for (int i = 0; i < parameters.size(); i++) {
            if (returnArg == 0 && i == 0)
                continue;
            inputParameters += parameters.get(i).getFieldStringType();
            if (parameters.get(i).getMechanism() != BY_REFERENCE &&
                    parameters.get(i).getMechanism() != BY_VALUE) {
                inputParameters += " " + parameters.get(i).getStringMechanism();
            }
            inputParameters += ", ";
        }
        if (!inputParameters.isEmpty())
            inputParameters = inputParameters.substring(0, inputParameters.length() - 2);

        if (returnArg != 0)
            returns = "Parameter " + returnArg;
        else {
            returns = parameters.get(0).getFieldStringType();
            if (parameters.get(0).getMechanism() != BY_REFERENCE &&
                    parameters.get(0).getMechanism() != -1) {
                returns += " ";
                returns += parameters.get(0).getStringMechanism();
            }
        }
    }

    @Override
    public boolean isAnyRowsResultSet() {
        return true;
    }
}
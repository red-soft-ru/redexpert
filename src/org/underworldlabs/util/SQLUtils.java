package org.underworldlabs.util;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.MetaDataValues;
import org.executequery.databaseobjects.Parameter;
import org.executequery.databaseobjects.ProcedureParameter;
import org.executequery.gui.browser.ColumnConstraint;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.table.CreateTableSQLSyntax;
import org.executequery.gui.table.TableDefinitionPanel;

import java.sql.DatabaseMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import static org.executequery.databaseobjects.NamedObject.*;
import static org.executequery.gui.browser.ColumnConstraint.RESTRICT;
import static org.executequery.gui.browser.ColumnConstraint.RULES;
import static org.executequery.gui.table.CreateTableSQLSyntax.*;

public final class SQLUtils {
    public static String generateCreateTable(String name, List<ColumnData> columnDataList, List<ColumnConstraint> columnConstraintList, boolean existTable, boolean temporary, String typeTemporary, String externalFile, String adapter) {
        StringBuilder sqlText = new StringBuilder();
        StringBuilder sqlBuffer = new StringBuilder();
        List<String> descriptions = new ArrayList<>();
        if (temporary)
            sqlBuffer.append(CreateTableSQLSyntax.CREATE_GLOBAL_TEMPORARY_TABLE);
        else
            sqlBuffer.append(CreateTableSQLSyntax.CREATE_TABLE);
        StringBuilder primaryText = new StringBuilder();
        StringBuffer primary = new StringBuffer(50);
        primary.setLength(0);
        primary.append(",\nCONSTRAINT PK_");
        primary.append(name);
        primary.append(" PRIMARY KEY (");
        boolean primary_flag = false;
        String autoincrementSQLText="";
        for (int i = 0,k=columnDataList.size(); i < k; i++) {
            ColumnData cd = columnDataList.get(i);
            autoincrementSQLText+=cd.getAutoincrement().getSqlAutoincrement();
            if (cd.isPrimaryKey()) {
                if (primary_flag)
                    primaryText.append(", ");
                else primaryText.append(" ");
                primaryText.append(cd.getFormattedColumnName());
                primary_flag = true;
            }
            if (!MiscUtils.isNull(cd.getDescription())) {
                descriptions.add(cd.getFormattedColumnName() + " is '" + cd.getDescription() + "'");
            }
           sqlText.append(generateDefinitionColumn(cd));
            if (i != k - 1) {
                sqlText.append(COMMA);
            }

        }
        if (primary_flag)
            primary.append(primaryText.toString());
        primary.append(")");
        StringBuffer description = new StringBuffer(50);
        description.setLength(0);
        if (!descriptions.isEmpty())
            for (String d : descriptions) {
                description.append("\nCOMMENT ON COLUMN ");
                description.append(MiscUtils.getFormattedObject(name));
                description.append("." + d);
                description.append("^");

            }

        sqlBuffer.append(MiscUtils.getFormattedObject(name));
        if (externalFile != null)
            sqlBuffer.append(NEW_LINE).append("EXTERNAL FILE '").append(externalFile.trim()).append("'");
        if (adapter != null)
            sqlBuffer.append(SPACE).append(" ADAPTER '").append(adapter.trim()).append("'");
        sqlBuffer.append(SPACE).append(B_OPEN);
        sqlBuffer.append(sqlText.toString().replaceAll(TableDefinitionPanel.SUBSTITUTE_NAME, MiscUtils.getFormattedObject(name)));
        if (primary_flag && !existTable)
            sqlBuffer.append(primary);
        columnConstraintList = removeDuplicatesConstraints(columnConstraintList);
        for (int i = 0, n = columnConstraintList.size(); i < n; i++) {
            sqlBuffer.append(generateDefinitionColumnConstraint(columnConstraintList.get(i)).replaceAll(TableDefinitionPanel.SUBSTITUTE_NAME, MiscUtils.getFormattedObject(name)));

        }
        sqlBuffer.append(CreateTableSQLSyntax.B_CLOSE);
        if (temporary)
            sqlBuffer.append("\nON COMMIT ").append(typeTemporary);
        sqlBuffer.append(CreateTableSQLSyntax.SEMI_COLON);
        sqlBuffer.append("\n").append(description);
        if(autoincrementSQLText!=null)
            sqlBuffer.append(autoincrementSQLText.replace(TableDefinitionPanel.SUBSTITUTE_NAME, MiscUtils.getFormattedObject(name)));
        return sqlBuffer.toString();
    }



    public static String generateDefinitionColumn(ColumnData cd)
    {
        StringBuilder sqlText=new StringBuilder();
        sqlText.append(NEW_LINE_2).append(
                cd.getColumnName() == null ? CreateTableSQLSyntax.EMPTY : cd.getFormattedColumnName()).
                append(SPACE);
        if (MiscUtils.isNull(cd.getComputedBy())) {

            if (MiscUtils.isNull(cd.getDomain())||cd.getDomain().startsWith("RDB$")) {
                sqlText.append(cd.getFormattedDataType());
            } else {
                sqlText.append(cd.getFormattedDomain());
            }
            if (cd.isAutoincrement() && cd.getAutoincrement().isIdentity()) {
                sqlText.append(" GENERATED BY DEFAULT AS IDENTITY");
                if(cd.getAutoincrement().getStartValue()!=0)
                sqlText.append(" START WITH " + cd.getAutoincrement().getStartValue() + ")");
            }
            if (!MiscUtils.isNull(cd.getDefaultValue())) {
                String value = "";
                boolean str = false;
                int sqlType = cd.getSQLType();
                switch (sqlType) {

                    case Types.LONGVARCHAR:
                    case Types.LONGNVARCHAR:
                    case Types.CHAR:
                    case Types.NCHAR:
                    case Types.VARCHAR:
                    case Types.NVARCHAR:
                    case Types.CLOB:
                    case Types.DATE:
                    case Types.TIME:
                    case Types.TIMESTAMP:
                        value = "'";
                        str = true;
                        break;
                    default:
                        break;
                }
                value += cd.getDefaultValue();
                if (str) {
                    value += "'";
                }
                if (MiscUtils.checkKeyword(cd.getDefaultValue()) || cd.getDefaultValue().startsWith("'"))
                    value = cd.getDefaultValue();
                sqlText.append(" DEFAULT " + value);
            }
            sqlText.append(cd.isRequired() ? NOT_NULL : CreateTableSQLSyntax.EMPTY);
            if (!MiscUtils.isNull(cd.getCheck())) {
                sqlText.append(" CHECK ( " + cd.getCheck() + ")");
            }
            if (cd.getCollate() != null && !cd.getCollate().equals(CreateTableSQLSyntax.NONE)) {
                sqlText.append(" COLLATE ").append(cd.getCollate());
            }
        } else {
            sqlText.append("COMPUTED BY ( " + cd.getComputedBy() + ")");
        }
        return sqlText.toString();
    }
    public static String generateDefinitionColumnConstraint(ColumnConstraint cc)
    {
        StringBuilder sqlBuffer=new StringBuilder();
        String nameConstraint=null;
        boolean hasName;

        if (!MiscUtils.isNull(cc.getName())) {

            nameConstraint = cc.getName();
            hasName = true;

        } else {

            hasName = false;
        }

        if (hasName) {

            sqlBuffer.append(COMMA).append(NEW_LINE_2).append(CONSTRAINT);
            sqlBuffer.append(MiscUtils.getFormattedObject(nameConstraint)).append(SPACE);

            if (cc.getType() != -1) {
                if(cc.getType()==CHECK_KEY)
                {
                    sqlBuffer.append(cc.getCheck());
                }
                else if (cc.getType() == UNIQUE_KEY) {
                    sqlBuffer.append(ColumnConstraint.UNIQUE).append(SPACE).append(B_OPEN);
                    String formatted = "";
                    if (cc.getCountCols() > 1)
                        formatted = cc.getColumn();
                    else formatted = MiscUtils.getFormattedObject(cc.getColumn());
                    sqlBuffer.append(formatted).append(B_CLOSE);
                } else {
                    sqlBuffer.append(cc.getTypeName()).append(KEY).append(B_OPEN);
                    String formatted = "";
                    if (cc.getCountCols() > 1)
                        formatted = cc.getColumn();
                    else formatted = MiscUtils.getFormattedObject(cc.getColumn());
                    sqlBuffer.append(formatted);
                    sqlBuffer.append(B_CLOSE);

                    if (cc.getType() == FOREIGN_KEY) {
                        sqlBuffer.append(REFERENCES);
                        sqlBuffer.append(MiscUtils.getFormattedObject(cc.getRefTable())).append(SPACE).append(B_OPEN);
                        if (cc.getCountCols() > 1)
                            formatted = cc.getRefColumn();
                        else formatted = MiscUtils.getFormattedObject(cc.getRefColumn());
                        sqlBuffer.append(formatted).append(B_CLOSE);
                        if (cc.getUpdateRule() != null && !Objects.equals(cc.getUpdateRule(), RULES[RESTRICT]))
                            sqlBuffer.append(" ON UPDATE ").append(cc.getUpdateRule());
                        if (cc.getDeleteRule() != null && !Objects.equals(cc.getDeleteRule(), RULES[RESTRICT]))
                            sqlBuffer.append(" ON DELETE ").append(cc.getDeleteRule());
                    }

                }

            }

        }
        return sqlBuffer.toString();
    }
    public static String generateCreateProcedure(String name,Vector<ColumnData> inputParameters,Vector<ColumnData> outputParameters,Vector<ColumnData> variables, String procedureBody,String comment)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(formattedParameters(variables, true));
        sb.append(procedureBody);
        return generateCreateProcedure(name,inputParameters,outputParameters,sb.toString(),comment);
    }

    public static String generateCreateProcedure(String name,List<ProcedureParameter> parameters,String fullProcedureBody,String comment,DatabaseConnection dc) {
        Vector<ColumnData> inputs = new Vector<>();
        Vector<ColumnData> outputs = new Vector<>();
        for (ProcedureParameter parameter : parameters) {
            if (parameter.getType() == DatabaseMetaData.procedureColumnIn) {
                ColumnData cd = columnDataFromProcedureParameter(parameter,dc);
                inputs.add(cd);
            } else {
                ColumnData cd = columnDataFromProcedureParameter(parameter,dc);
                outputs.add(cd);
            }
        }
        return generateCreateProcedure(name,inputs,outputs,fullProcedureBody,comment);
    }

    public static String generateCreateProcedure(String name,Vector<ColumnData> inputParameters,Vector<ColumnData> outputParameters, String fullProcedureBody,String comment)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR ALTER PROCEDURE ");
        sb.append(MiscUtils.getFormattedObject(name));
        sb.append(" (");
        sb.append(formattedParameters(inputParameters, false));
        sb.append(")\n");
        String output = formattedParameters(outputParameters, false);
        if (!MiscUtils.isNull(output.trim())) {
            sb.append("RETURNS (");
            sb.append(output);
            sb.append(")\n");
        }
        sb.append("AS\n");
        sb.append(fullProcedureBody);
        sb.append("^\n");

        sb.append("\n");

        // add procedure description
        String text = comment;
        if (text != null && !text.isEmpty()) {
            sb.append("\n");
            sb.append("COMMENT ON PROCEDURE ");
            sb.append(MiscUtils.getFormattedObject(name));
            sb.append(" IS '");
            sb.append(text);
            sb.append("'");
            sb.append("^\n");
        }

        for (ColumnData cd :
                inputParameters) {
            String cdText = cd.getDescription();
            if (cdText != null && !cdText.isEmpty()) {
                sb.append("\n");
                sb.append("COMMENT ON PARAMETER ");
                sb.append(MiscUtils.getFormattedObject(name)).append(".");
                sb.append(cd.getFormattedColumnName());
                sb.append(" IS '");
                sb.append(cdText);
                sb.append("'\n");
                sb.append("^\n");
            }
        }

        for (ColumnData cd :
                outputParameters) {
            String cdText = cd.getDescription();
            if (cdText != null && !cdText.isEmpty()) {
                sb.append("\n");
                sb.append("COMMENT ON PARAMETER ");
                sb.append(MiscUtils.getFormattedObject(name)).append(".");
                sb.append(cd.getFormattedColumnName());
                sb.append(" IS '");
                sb.append(cdText);
                sb.append("'\n");
                sb.append("^\n");
            }
        }

        return sb.toString();
    }

    public static String formattedParameters(Vector<ColumnData> tableVector, boolean variable) {
        StringBuilder sqlText = new StringBuilder();
        sqlText.append("\n");
        for (int i = 0, k = tableVector.size(); i < k; i++) {
            ColumnData cd = tableVector.elementAt(i);
            if (!MiscUtils.isNull(cd.getColumnName())) {
                if (variable)
                    sqlText.append("DECLARE ");
                sqlText.append(formattedParameter(cd));
                if (variable) {
                    sqlText.append(";");
                    if (cd.getDescription() != null && !cd.getDescription().isEmpty()) {
                        if (cd.isDescriptionAsSingleComment()) {
                            sqlText.append(" --");
                            sqlText.append(cd.getDescription());
                        } else {
                            sqlText.append(" /*");
                            sqlText.append(cd.getDescription());
                            sqlText.append("*/");
                        }
                    }
                } else if (i != k - 1) {
                    sqlText.append(",");
                }
                sqlText.append("\n");
            }
        }
        return sqlText.toString();
    }
    public static String formattedParameter(ColumnData cd) {
        StringBuilder sb = new StringBuilder();
        sb.append(cd.getColumnName() == null ? CreateTableSQLSyntax.EMPTY : cd.getColumnName()).
                append(" ");
        if (MiscUtils.isNull(cd.getComputedBy())) {
            if (MiscUtils.isNull(cd.getDomain())) {
                if (cd.getColumnType() != null || cd.isTypeOf()) {
                    sb.append(cd.getFormattedDataType());
                }
            } else {
                if (cd.isTypeOf())
                    sb.append(cd.getFormattedDataType());
                else
                    sb.append(cd.getFormattedDomain());
            }
            sb.append(cd.isRequired() ? " NOT NULL" : CreateTableSQLSyntax.EMPTY);
            if (cd.getTypeParameter() != ColumnData.OUTPUT_PARAMETER && !MiscUtils.isNull(cd.getDefaultValue())) {
                String value = "";
                boolean str = false;
                int sqlType = cd.getSQLType();
                switch (sqlType) {

                    case Types.LONGVARCHAR:
                    case Types.LONGNVARCHAR:
                    case Types.CHAR:
                    case Types.NCHAR:
                    case Types.VARCHAR:
                    case Types.VARBINARY:
                    case Types.BINARY:
                    case Types.NVARCHAR:
                    case Types.CLOB:
                    case Types.DATE:
                    case Types.TIME:
                    case Types.TIMESTAMP:
                        value = "'";
                        str = true;
                        break;
                    default:
                        break;
                }
                value += cd.getDefaultValue();
                if (str) {
                    value += "'";
                }
                if(MiscUtils.checkKeyword(cd.getDefaultValue()))
                    value=cd.getDefaultValue();
                sb.append(" DEFAULT ").append(value);
            }
            if (!MiscUtils.isNull(cd.getCheck())) {
                sb.append(" CHECK ( ").append(cd.getCheck()).append(")");
            }
        } else {
            sb.append("COMPUTED BY ( ").append(cd.getComputedBy()).append(")");
        }
        return sb.toString();
    }

    public static ColumnData columnDataFromProcedureParameter(Parameter parameter, DatabaseConnection dc) {
        ColumnData cd = new ColumnData(true, dc);
        cd.setColumnName(parameter.getName());
        cd.setDomain(parameter.getDomain());
        cd.setColumnSubtype(parameter.getSubType());
        cd.setSQLType(parameter.getDataType());
        cd.setColumnSize(parameter.getSize());
        cd.setColumnType(parameter.getSqlType());
        cd.setColumnScale(parameter.getScale());
        cd.setColumnRequired(parameter.getNullable());
        cd.setCharset(parameter.getEncoding());
        cd.setDescription(parameter.getDescription());
        cd.setTypeOf(parameter.isTypeOf());
        cd.setTypeOfFrom(parameter.getTypeOfFrom());
        cd.setTable(parameter.getRelationName());
        cd.setColumnTable(parameter.getFieldName());
        cd.setDefaultValue(parameter.getDefaultValue(), true);
        cd.setDescriptionAsSingleComment(parameter.isDescriptionAsSingleComment());
        MetaDataValues metaData = new MetaDataValues(true);
        metaData.setDatabaseConnection(dc);
        String[] dataTypes = metaData.getDataTypesArray();
        int[] intDataTypes = metaData.getIntDataTypesArray();
        for (int i = 0; i < dataTypes.length; i++) {
            if (dataTypes[i].toLowerCase().equals(parameter.getSqlType().toLowerCase()))
                cd.setSQLType(intDataTypes[i]);
        }
        return cd;
    }

    public static List<ColumnConstraint> removeDuplicatesConstraints(List<ColumnConstraint> columnConstraintList) {
        List<String> cc_names = new ArrayList<>();
        List<ColumnConstraint> columnConstraints = new ArrayList<>();
        for (int i = 0, n = columnConstraintList.size(); i < n; i++) {
            int ind = cc_names.indexOf(columnConstraintList.get(i).getName());
            if (ind >= 0) {
                ColumnConstraint cc_rep = columnConstraintList.get(i);
                ColumnConstraint cc_origin = columnConstraints.get(ind);
                if (cc_rep.getColumn() != null) {
                    String cols = cc_origin.getColumn();
                    if (cc_origin.getCountCols() == 1)
                        cols = MiscUtils.getFormattedObject(cols);
                    cols += "," + MiscUtils.getFormattedObject(cc_rep.getColumn());
                    cc_origin.setColumn(cols);
                }
                if (cc_rep.getRefColumn() != null) {
                    String cols = cc_origin.getRefColumn();
                    if (cc_origin.getCountCols() == 1)
                        cols = MiscUtils.getFormattedObject(cols);
                    cols += "," + MiscUtils.getFormattedObject(cc_rep.getRefColumn());
                    cc_origin.setRefColumn(cols);
                }
                cc_origin.setCountCols(cc_origin.getCountCols() + 1);

            } else {
                cc_names.add(columnConstraintList.get(i).getName());
                columnConstraints.add(columnConstraintList.get(i));
            }
        }
        return columnConstraints;
    }

}


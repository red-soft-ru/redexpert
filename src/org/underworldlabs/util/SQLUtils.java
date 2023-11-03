package org.underworldlabs.util;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.*;
import org.executequery.databaseobjects.impl.*;
import org.executequery.gui.browser.ColumnConstraint;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.table.CreateTableSQLSyntax;
import org.executequery.gui.table.TableDefinitionPanel;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;

import java.sql.DatabaseMetaData;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.executequery.databaseobjects.NamedObject.*;
import static org.executequery.gui.browser.ColumnConstraint.RESTRICT;
import static org.executequery.gui.browser.ColumnConstraint.RULES;
import static org.executequery.gui.table.CreateTableSQLSyntax.*;

public final class SQLUtils {

    public static String generateCreateTable(
            String name, List<ColumnData> columnDataList, List<ColumnConstraint> columnConstraintList,
            boolean existTable, boolean temporary, boolean constraints, boolean computed, boolean setComment,
            String typeTemporary, String externalFile, String adapter, String sqlSecurity, String tablespace, String comment, String delimiter) {

        StringBuilder sb = new StringBuilder();
        StringBuilder sqlText = new StringBuilder();

        sb.append(temporary ?
                CreateTableSQLSyntax.CREATE_GLOBAL_TEMPORARY_TABLE :
                CreateTableSQLSyntax.CREATE_TABLE);
        DatabaseConnection dc = null;
        if (columnDataList.size() > 0)
            dc = columnDataList.get(0).getDatabaseConnection();
        sb.append(format(name, dc));

        if (!MiscUtils.isNull(externalFile))
            sb.append(NEW_LINE).append("EXTERNAL FILE '").append(externalFile.trim()).append("'");
        if (!MiscUtils.isNull(adapter))
            sb.append(SPACE).append(" ADAPTER '").append(adapter.trim()).append("'");
        sb.append(SPACE).append(B_OPEN);

        StringBuilder primary = new StringBuilder();
        primary.append(",\nCONSTRAINT ");
        primary.append(format("PK_" + name, dc));
        primary.append(" PRIMARY KEY (");

        boolean primary_flag = false;
        StringBuilder autoincrementSQLText = new StringBuilder();
        StringBuilder primaryText = new StringBuilder();

        for (ColumnData cd : columnDataList) {

            autoincrementSQLText.append(cd.getAutoincrement().getSqlAutoincrement());

            if (cd.isPrimaryKey()) {
                primaryText.append(primary_flag ? ", " : " ");
                primaryText.append(cd.getFormattedColumnName());
                primary_flag = true;
            }

            sqlText.append(generateDefinitionColumn(cd, computed, true, true));
        }
        if (sqlText.lastIndexOf(COMMA) > -1)
            sqlText.deleteCharAt(sqlText.lastIndexOf(COMMA));

        primary.append(primaryText).append(B_CLOSE);
        sb.append(sqlText.toString().replaceAll(TableDefinitionPanel.SUBSTITUTE_NAME, format(name, dc)));

        if (primary_flag && !existTable)
            sb.append(primary);
        columnConstraintList = removeDuplicatesConstraints(columnConstraintList, dc);

        if (constraints)
            for (ColumnConstraint columnConstraint : columnConstraintList)
                sb.append(generateDefinitionColumnConstraint(columnConstraint, existTable, true, dc)
                        .replaceAll(TableDefinitionPanel.SUBSTITUTE_NAME, format(name, dc)));

        sb.append(CreateTableSQLSyntax.B_CLOSE);

        if (!MiscUtils.isNull(tablespace))
            sb.append("\nTABLESPACE ").append(format(tablespace, dc));
        if (!MiscUtils.isNull(sqlSecurity))
            sb.append("\n" + SQL_SECURITY).append(sqlSecurity);
        if (temporary)
            sb.append("\n").append(typeTemporary);

        sb.append(delimiter).append("\n");

        if (autoincrementSQLText != null)
            sb.append(autoincrementSQLText.toString().replace(TableDefinitionPanel.SUBSTITUTE_NAME, format(name, dc))).append(NEW_LINE);

        if (setComment) {
            sb.append(generateCommentForColumns(name, columnDataList, "COLUMN", delimiter));
            if (!MiscUtils.isNull(comment) && !comment.equals(""))
                sb.append(generateComment(name, "TABLE", comment, delimiter, false, dc));
        }

        return sb.toString();
    }

    public static String generateDefinitionColumn(ColumnData cd, boolean computedNeed, boolean startWithNewLine, boolean setComma) {

        StringBuilder sb = new StringBuilder();

        if (startWithNewLine)
            sb.append(NEW_LINE_2);
        sb.append(cd.getColumnName() == null ? CreateTableSQLSyntax.EMPTY : cd.getFormattedColumnName()).append(SPACE);
        String checkValid = sb.toString();

        if (MiscUtils.isNull(cd.getComputedBy())) {

            if (MiscUtils.isNull(cd.getDomain()) || cd.getDomain().startsWith("RDB$"))
                sb.append(cd.getFormattedDataType());
            else
                sb.append(cd.getFormattedDomain());

            if (cd.isAutoincrement() && cd.getAutoincrement().isIdentity()) {
                sb.append(" GENERATED BY DEFAULT AS IDENTITY");
                if (cd.getAutoincrement().getStartValue() != 0)
                    sb.append(" (START WITH ").append(cd.getAutoincrement().getStartValue()).append(")");
            }

            if (!MiscUtils.isNull(cd.getDefaultValue().getValue()))
                sb.append(format(cd.getDefaultValue(), cd.getSQLType(), cd.getDatabaseConnection()));

            sb.append(cd.isRequired() ? NOT_NULL : CreateTableSQLSyntax.EMPTY);
            if (!MiscUtils.isNull(cd.getCheck()))
                sb.append(" CHECK ( ").append(cd.getCheck()).append(")");

            if (!MiscUtils.isNull(cd.getCollate()) && !cd.getCollate().equals(CreateTableSQLSyntax.NONE))
                sb.append(" COLLATE ").append(cd.getCollate());

        } else if (computedNeed)
            sb.append("COMPUTED BY ( ").append(cd.getComputedBy()).append(")");

        return (!sb.toString().equals(checkValid)) ? sb.append(setComma ? COMMA : "").toString() : "";

    }

    public static String generateDefinitionColumnConstraint(
            ColumnConstraint cc, boolean editing, boolean startWithNewLine, DatabaseConnection dc) {

        StringBuilder sb = new StringBuilder();

        String nameConstraint = null;
        boolean hasName = false;

        if (!MiscUtils.isNull(cc.getName())) {
            nameConstraint = cc.getName();
            hasName = true;
        }

        if (hasName) {

            if (startWithNewLine)
                sb.append(COMMA).append(NEW_LINE_2);
            sb.append(CreateTableSQLSyntax.CONSTRAINT);
            sb.append(format(nameConstraint, dc)).append(SPACE);

            if (cc.getType() != -1) {

                if (cc.getType() == CHECK_KEY) {
                    sb.append(cc.getCheck());

                } else {

                    String formatted = (cc.getCountCols() > 1) ? cc.getColumn() : format(cc.getColumn(), dc);
                    if (editing) {
                        List<String> columnList = new ArrayList<>();
                        for (String col : cc.getColumnDisplayList()) {
                            String column = format(col, dc);
                            columnList.add(column);
                        }
                        formatted = String.join(", ", columnList);
                    }

                    if (cc.getType() == UNIQUE_KEY) {
                        sb.append(ColumnConstraint.UNIQUE).append(SPACE).append(B_OPEN);
                        sb.append(formatted).append(B_CLOSE);

                    } else {
                        sb.append(cc.getTypeName()).append(KEY).append(B_OPEN);
                        sb.append(formatted).append(B_CLOSE);

                        if (cc.getType() == FOREIGN_KEY) {
                            sb.append(REFERENCES).append(format(cc.getRefTable(), dc));
                            sb.append(SPACE).append(B_OPEN);

                            formatted = (cc.getCountCols() > 1) ? cc.getRefColumn() : format(cc.getRefColumn(), dc);
                            if (editing) {
                                List<String> columnList = new ArrayList<>();
                                for (String col : cc.getRefColumnDisplayList()) {
                                    String column = format(col, dc);
                                    columnList.add(column);
                                }
                                formatted = String.join(", ", columnList);
                            }

                            sb.append(formatted).append(B_CLOSE);

                            if (cc.getUpdateRule() != null && !Objects.equals(cc.getUpdateRule(), RULES[RESTRICT]))
                                sb.append(" ON UPDATE ").append(cc.getUpdateRule());
                            if (cc.getDeleteRule() != null && !Objects.equals(cc.getDeleteRule(), RULES[RESTRICT]))
                                sb.append(" ON DELETE ").append(cc.getDeleteRule());
                        }
                    }

                    if (!MiscUtils.isNull(cc.getTablespace()))
                        sb.append(" TABLESPACE ").append(format(cc.getTablespace(), dc));
                }
            }
        }

        return sb.toString();
    }

    public static String generateCreateProcedure(
            String name, String entryPoint, String engine, Vector<ColumnData> inputParameters,
            Vector<ColumnData> outputParameters, Vector<ColumnData> variables, String sqlSecurity,
            String authid, String procedureBody, String comment, boolean setTerm, boolean setComment, DatabaseConnection dc) {

        String sb = formattedParameters(variables, true) +
                procedureBody;
        return generateCreateProcedure(name, entryPoint, engine, inputParameters, outputParameters,
                sqlSecurity, authid, sb, comment, setTerm, setComment, dc);
    }

    public static String generateCreateProcedure(
            String name, String entryPoint, String engine, List<ProcedureParameter> parameters,
            String sqlSecurity, String authid, String fullProcedureBody, String comment,
            DatabaseConnection dc, boolean setTerm, boolean setComment) {

        Vector<ColumnData> inputs = new Vector<>();
        Vector<ColumnData> outputs = new Vector<>();

        for (ProcedureParameter parameter : parameters) {
            ColumnData cd = columnDataFromProcedureParameter(parameter, dc, false);
            if (parameter.getType() == DatabaseMetaData.procedureColumnIn)
                inputs.add(cd);
            else
                outputs.add(cd);
        }
        return generateCreateProcedure(name, entryPoint, engine, inputs, outputs,
                sqlSecurity, authid, fullProcedureBody, comment, setTerm, setComment, dc);
    }

    public static String generateCreateProcedure(
            String name, String entryPoint, String engine, Vector<ColumnData> inputParameters,
            Vector<ColumnData> outputParameters, String sqlSecurity, String authid,
            String fullProcedureBody, String comment, boolean setTerm, boolean setComment, DatabaseConnection dc) {


        StringBuilder sb = new StringBuilder();

        if (setTerm)
            sb.append("SET TERM ^;\n");

        sb.append(generateCreateProcedureOrFunctionHeader(name, inputParameters, NamedObject.META_TYPES[PROCEDURE], authid, dc));
        String output = formattedParameters(outputParameters, false);
        if (!MiscUtils.isNull(output.trim())) {
            sb.append("\nRETURNS (\n");
            sb.append(output);
            sb.append(")");
        }

        if (!MiscUtils.isNull(sqlSecurity))
            sb.append("\n" + SQL_SECURITY).append(sqlSecurity);

        if (!MiscUtils.isNull(entryPoint)) {

            sb.append("\nEXTERNAL NAME '");
            sb.append(entryPoint).append("'");
            sb.append(" ENGINE ").append(engine);

        } else
            sb.append(generateSQLBody(fullProcedureBody));

        if (setTerm)
            sb.append("^\nSET TERM ;^");
        sb.append("\n");

        if (setComment) {
            sb.append(generateComment(name, NamedObject.META_TYPES[PROCEDURE], comment, "^", false, dc));
            sb.append(generateCommentForColumns(name, inputParameters, "PARAMETER", "^"));
            sb.append(generateCommentForColumns(name, outputParameters, "PARAMETER", "^"));
        }

        return sb.toString();
    }

    public static String generateCommentForColumns(
            String relationName, List<ColumnData> cols, String metaTag, String delimiter) {

        StringBuilder sb = new StringBuilder();

        for (ColumnData cd : cols) {
            String name = format(relationName, cd.getDatabaseConnection()) + "." + cd.getFormattedColumnName();
            sb.append(generateComment(name, metaTag, cd.getDescription(), delimiter, true, cd.getDatabaseConnection()));
        }

        return sb.toString();
    }

    public static String generateComment(
            String name, String metaTag, String comment, String delimiter, boolean nameAlreadyFormatted, DatabaseConnection dc) {
        if (metaTag != null && metaTag.contentEquals(NamedObject.META_TYPES[GLOBAL_TEMPORARY]))
            metaTag = NamedObject.META_TYPES[TABLE];
        if (metaTag != null && (metaTag.contentEquals(NamedObject.META_TYPES[DATABASE_TRIGGER]) || metaTag.contentEquals(NamedObject.META_TYPES[DDL_TRIGGER])))
            metaTag = NamedObject.META_TYPES[TRIGGER];
        StringBuilder sb = new StringBuilder();

        if (comment != null && !comment.isEmpty()) {

            if (comment.startsWith("'") && comment.endsWith("'"))
                comment = comment.substring(1, comment.length() - 1);

            comment = comment.replace("'", "''");

            sb.append("COMMENT ON ").append(metaTag).append(" ");
            sb.append(nameAlreadyFormatted ? name : format(name, dc));
            sb.append(" IS ");

            if (!comment.equals("NULL"))
                sb.append("'").append(comment).append("'");
            else
                sb.append("NULL");

            sb.append(delimiter);
            sb.append("\n");
        }

        return sb.toString();
    }

    public static String generateCreateProcedureOrFunctionHeader(
            String name, Vector<ColumnData> inputParameters, String metaTag, String authid, DatabaseConnection dc) {

        StringBuilder sb = new StringBuilder();

        sb.append("CREATE OR ALTER ").append(metaTag).append(" ");
        sb.append(format(name, dc));

        if (!MiscUtils.isNull(authid))
            sb.append("\nAUTHID ").append(authid).append("\n");

        if (inputParameters != null && inputParameters.size() > 0 &&
                (inputParameters.size() == 1 &&
                        !MiscUtils.isNull(inputParameters.get(0).getColumnName()) || inputParameters.size() > 1)) {

            sb.append(" (\n");
            sb.append(formattedParameters(inputParameters, false));
            sb.append(") ");
        }

        return sb.toString();
    }

    public static String generateSQLBody(String sqlBody) {
        return "\nAS\n" + sqlBody + "^\n";
    }

    public static String generateCreateFunction(
            String name, Vector<ColumnData> argumentList, Vector<ColumnData> variables, ColumnData returnType,
            String functionBody, String entryPoint, String engine, String sqlSecurity, String comment,
            boolean setTerm, boolean setComment, boolean deterministic, DatabaseConnection dc) {

        String sb = formattedParameters(variables, true) +
                functionBody;
        return generateCreateFunction(name, argumentList, returnType, sb, entryPoint,
                engine, sqlSecurity, comment, setTerm, setComment, deterministic, dc);
    }


    public static String generateCreateFunction(
            String name, List<FunctionArgument> argumentList, String fullFunctionBody,
            String entryPoint, String engine, String sqlSecurity, String comment,
            boolean setTerm, boolean setComment, boolean deterministic, DatabaseConnection dc) {

        Vector<ColumnData> inputs = new Vector<>();
        ColumnData returnType = null;

        for (FunctionArgument parameter : argumentList) {
            if (parameter.getType() == DatabaseMetaData.procedureColumnIn) {
                ColumnData cd = columnDataFromProcedureParameter(parameter, dc, false);
                inputs.add(cd);
            } else
                returnType = columnDataFromProcedureParameter(parameter, dc, false);
        }

        return generateCreateFunction(name, inputs, returnType, fullFunctionBody, entryPoint, engine,
                sqlSecurity, comment, setTerm, setComment, deterministic, dc);
    }

    public static String generateCreateFunction(
            String name, Vector<ColumnData> inputArguments, ColumnData returnType,
            String fullFunctionBody, String entryPoint, String engine, String sqlSecurity,
            String comment, boolean setTerm, boolean setComment, boolean deterministic, DatabaseConnection dc) {

        StringBuilder sb = new StringBuilder();

        if (setTerm)
            sb.append("SET TERM ^;\n");

        sb.append(generateCreateProcedureOrFunctionHeader(name, inputArguments, NamedObject.META_TYPES[FUNCTION], null, dc));
        sb.append("\nRETURNS ");

        if (returnType != null)
            sb.append(returnType.getFormattedDataType());

        if (deterministic)
            sb.append(" DETERMINISTIC");

        if (!MiscUtils.isNull(sqlSecurity))
            sb.append("\n" + SQL_SECURITY).append(sqlSecurity);

        if (!MiscUtils.isNull(entryPoint)) {
            sb.append("\nEXTERNAL NAME '");
            sb.append(entryPoint).append("'");
            sb.append(" ENGINE ").append(engine);
        } else
            sb.append(generateSQLBody(fullFunctionBody));

        if (setComment) {
            sb.append(generateComment(name, NamedObject.META_TYPES[FUNCTION], comment, "^", false, dc));
            sb.append(generateCommentForColumns(name, inputArguments, "PARAMETER", "^"));
        }

        if (setTerm)
            sb.append("SET TERM ;^\n");

        return sb.toString();
    }

    public static String generateAlterDefinitionColumn(
            DatabaseTableColumn column, ColumnData columnData, String tableName, String delimiter) {

        StringBuilder sb = new StringBuilder();
        StringBuilder alterTableTemplate = new StringBuilder()
                .append("ALTER TABLE ").append(format(tableName, columnData.getDatabaseConnection())).append("\n\tALTER COLUMN ");

        if (column.isNameChanged())
            sb.append(alterTableTemplate).append(format(column.getOriginalColumn().getName(), columnData.getDatabaseConnection()))
                    .append(" TO ").append(columnData.getFormattedColumnName())
                    .append(delimiter).append("\n");

        alterTableTemplate.append(columnData.getFormattedColumnName());

        if (column.isDataTypeChanged() && !column.isDomainChanged() && !column.isGenerated())
            sb.append(alterTableTemplate)
                    .append(" TYPE ").append(columnData.getFormattedDataType())
                    .append(delimiter).append("\n");

        if (column.isRequiredChanged())
            sb.append(alterTableTemplate)
                    .append(column.isRequired() ? " SET NOT NULL" : " DROP NOT NULL")
                    .append(delimiter).append("\n");

        if (column.isDefaultValueChanged())
            sb.append(alterTableTemplate)
                    .append(" SET ").append(format(columnData.getDefaultValue(), columnData.getSQLType(), columnData.getDatabaseConnection()))
                    .append(delimiter).append("\n");

        if (column.isComputedChanged())
            sb.append(alterTableTemplate)
                    .append(" COMPUTED BY ").append(column.getComputedSource())
                    .append(delimiter).append("\n");

        if (column.isDomainChanged() && !column.isGenerated())
            sb.append(alterTableTemplate)
                    .append(" TYPE ").append(columnData.getFormattedDomain())
                    .append(delimiter).append("\n");

        if (column.isDescriptionChanged())
            sb.append("COMMENT ON COLUMN ")
                    .append(format(tableName, columnData.getDatabaseConnection())).append(".").append(columnData.getFormattedColumnName())
                    .append(" IS '").append(column.getColumnDescription()).append("'")
                    .append(delimiter).append("\n");

        return sb.toString();
    }

    public static String generateAlterDefinitionColumn(ColumnData thisCD, ColumnData comparingCD, boolean computedNeed, boolean positionNeed) {

        StringBuilder sb = new StringBuilder();

        if (MiscUtils.isNull(thisCD.getComputedBy()) == MiscUtils.isNull(comparingCD.getComputedBy())) {
            if (thisCD.isAutoincrement() == comparingCD.isAutoincrement()) {

                String alterColumnTemplate = "\n\tALTER COLUMN " + format(thisCD.getColumnName(), thisCD.getDatabaseConnection());

                if (MiscUtils.isNull(thisCD.getComputedBy())) {
                    if (!comparingCD.isAutoincrement()) {

                        if (MiscUtils.isNull(comparingCD.getDomain()) || comparingCD.getDomain().startsWith("RDB$")) {
                            if ((!MiscUtils.isNull(thisCD.getDomain()) && !thisCD.getDomain().startsWith("RDB$")) ||
                                    !Objects.equals(thisCD.getFormattedDataType(), comparingCD.getFormattedDataType())) {

                                if (thisCD.getFormattedDataType().contains("BLOB") || comparingCD.getFormattedDataType().contains("BLOB")) {
                                    sb.append("\n\tDROP ").append(format(thisCD.getColumnName(), thisCD.getDatabaseConnection())).append(COMMA);
                                    sb.append("\n\tADD ").append(generateDefinitionColumn(comparingCD, computedNeed, false, true));
                                    return sb.toString();

                                } else
                                    sb.append(alterColumnTemplate).append(" TYPE ").append(comparingCD.getFormattedDataType()).append(COMMA);
                            }

                        } else if (MiscUtils.isNull(thisCD.getDomain()) || !Objects.equals(thisCD.getDomain(), comparingCD.getDomain()))
                            sb.append(alterColumnTemplate).append(" TYPE ").append(comparingCD.getDomain()).append(COMMA);

                        if (positionNeed && !Objects.equals(thisCD.getColumnPosition(), comparingCD.getColumnPosition()))
                            sb.append(alterColumnTemplate).append(" POSITION ").append(comparingCD.getColumnPosition()).append(COMMA);

                        if (!MiscUtils.isNull(thisCD.getDefaultValue().getValue()) && MiscUtils.isNull(comparingCD.getDefaultValue().getValue()))
                            sb.append(alterColumnTemplate).append(" DROP DEFAULT").append(COMMA);

                        else if (!Objects.equals(thisCD.getDefaultValue().getValue(), comparingCD.getDefaultValue().getValue()))
                            sb.append(alterColumnTemplate).append(" SET DEFAULT ").append(comparingCD.getDefaultValue().getValue()).append(COMMA);

                        if (thisCD.isRequired() && !comparingCD.isRequired())
                            sb.append(alterColumnTemplate).append(" DROP NOT NULL").append(COMMA);

                        else if (!thisCD.isRequired() && comparingCD.isRequired())
                            sb.append(alterColumnTemplate).append(" SET NOT NULL").append(COMMA);

                    } else if (!Objects.equals(thisCD.getAutoincrement().getStartValue(), comparingCD.getAutoincrement().getStartValue()))
                        sb.append(alterColumnTemplate).append(" RESTART WITH ")
                                .append(comparingCD.getAutoincrement().getStartValue()).append(COMMA);

                } else if (computedNeed && !Objects.equals(thisCD.getComputedBy().trim(), comparingCD.getComputedBy().trim())) {

                    sb.append(alterColumnTemplate);

                    if (!Objects.equals(thisCD.getColumnType(), comparingCD.getColumnType()))
                        sb.append(" TYPE ").append((MiscUtils.isNull(comparingCD.getDomain())) ?
                                comparingCD.getFormattedDataType() : comparingCD.getDomain()).append(COMMA);

                    sb.append(" COMPUTED BY (").append(comparingCD.getComputedBy()).append(")").append(COMMA);
                }

            } else {
                sb.append("\n\tDROP ").append(format(thisCD.getColumnName(), thisCD.getDatabaseConnection())).append(COMMA);
                sb.append("\n\tADD ").append(generateDefinitionColumn(comparingCD, computedNeed, false, true));
            }

        } else {
            sb.append("\n\tDROP ").append(format(thisCD.getColumnName(), thisCD.getDatabaseConnection())).append(COMMA);
            sb.append("\n\tADD ").append(generateDefinitionColumn(comparingCD, computedNeed, false, true));
        }

        return sb.toString();
    }

    public static String formattedParameters(Vector<ColumnData> tableVector, boolean variable) {

        StringBuilder sb = new StringBuilder();

        for (int i = 0, k = tableVector.size(); i < k; i++) {

            ColumnData cd = tableVector.elementAt(i);

            if (!MiscUtils.isNull(cd.getColumnName())) {

                if (variable)
                    sb.append("DECLARE ");

                if (cd.isCursor()) {

                    sb.append(cd.getColumnName()).append(" CURSOR FOR ");
                    if (cd.isScroll())
                        sb.append("SCROLL ");
                    sb.append("(").append(cd.getSelectOperator()).append(")");

                } else {

                    if (!variable)
                        sb.append("\t");
                    sb.append(formattedParameter(cd));

                }

                if (variable) {

                    sb.append(";");
                    if (cd.getDescription() != null && !cd.getDescription().isEmpty()) {
                        if (cd.isDescriptionAsSingleComment()) {
                            sb.append(" --");
                            sb.append(cd.getDescription());
                        } else {
                            sb.append(" /*");
                            sb.append(cd.getDescription());
                            sb.append("*/");
                        }
                    }

                } else if (i != k - 1)
                    sb.append(",");

                sb.append("\n");
            }
        }

        return sb.toString();
    }

    public static String formattedParameter(ColumnData cd) {
        StringBuilder sb = new StringBuilder();
        sb.append(cd.getColumnName() == null ? CreateTableSQLSyntax.EMPTY : format(cd.getColumnName(), cd.getDatabaseConnection())).
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
            if (cd.getTypeParameter() != ColumnData.OUTPUT_PARAMETER && !MiscUtils.isNull(cd.getDefaultValue().getValue()) && !cd.getDefaultValue().isDomain()) {
                sb.append(format(cd.getDefaultValue(), cd.getSQLType(), cd.getDatabaseConnection()));
            }
            if (!MiscUtils.isNull(cd.getCheck())) {
                sb.append(" CHECK ( ").append(cd.getCheck()).append(")");
            }
        } else {
            sb.append("COMPUTED BY ( ").append(cd.getComputedBy()).append(")");
        }
        return sb.toString();
    }

    public static ColumnData columnDataFromProcedureParameter(Parameter parameter, DatabaseConnection dc, boolean loadDomainInfo) {
        ColumnData cd = new ColumnData(true, dc);
        cd.setColumnName(parameter.getName());
        cd.setDomain(parameter.getDomain(), loadDomainInfo);
        cd.setColumnSubtype(parameter.getSubType());
        cd.setSQLType(parameter.getDataType());
        cd.setColumnSize(parameter.getSize());
        cd.setColumnType(parameter.getSqlType());
        cd.setColumnScale(parameter.getScale());
        cd.setNotNull(parameter.getNullable() == 0);
        cd.setCharset(parameter.getEncoding());
        cd.setDescription(parameter.getDescription());
        cd.setTypeOf(parameter.isTypeOf());
        cd.setTypeOfFrom(parameter.getTypeOfFrom());
        cd.setTable(parameter.getRelationName());
        cd.setColumnTable(parameter.getFieldName());
        cd.setDefaultValue(parameter.getDefaultValue(), true, parameter.isDefaultValueFromDomain());
        cd.setDescriptionAsSingleComment(parameter.isDescriptionAsSingleComment());
        String[] dataTypes = dc.getDataTypesArray();
        int[] intDataTypes = dc.getIntDataTypesArray();
        for (int i = 0; i < dataTypes.length; i++) {
            if (dataTypes[i].equalsIgnoreCase(parameter.getSqlType()))
                cd.setSQLType(intDataTypes[i]);
        }
        return cd;
    }

    public static String generateNameForDBObject(String type, DatabaseConnection databaseConnection) {
        String name = "NEW_" + type + "_";
        int int_number = 0;
        String number;
        List<NamedObject> keys = ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(databaseConnection).getDatabaseObjectsForMetaTag(type);
        if (keys != null)
            for (NamedObject key : keys) {
                if (!MiscUtils.isNull(key.getName()))
                    if (key.getName().contains(name)) {
                        number = key.getName().replace(name, "");
                        try {
                            if (Integer.parseInt(number) > int_number)
                                int_number = Integer.parseInt(number);
                        } catch (NumberFormatException e) {
                            Log.debug(e.getMessage());
                        }
                    }

            }
        number = String.valueOf(int_number + 1);
        return name + number;
    }

    private static String generateNameForConstraint(String type, List<ColumnConstraint> keys) {
        String name = "_<TABLE_NAME>";
        switch (type) {
            case org.executequery.databaseobjects.impl.ColumnConstraint.PRIMARY:
                name = "PK" + name;
                break;
            case org.executequery.databaseobjects.impl.ColumnConstraint.FOREIGN:
                name = "FK" + name;
                break;
            case org.executequery.databaseobjects.impl.ColumnConstraint.CHECK:
                name = "CHECK" + name;
                break;
            case org.executequery.databaseobjects.impl.ColumnConstraint.UNIQUE:
                name = "UQ" + name;
                break;
        }
        name = name + "_";
        int int_number = 0;
        String number;
        if (keys != null)
            for (ColumnConstraint key : keys) {
                if (!MiscUtils.isNull(key.getName()))
                    if (key.getName().contains(name)) {
                        number = key.getName().replace(name, "");
                        try {
                            if (Integer.parseInt(number) > int_number)
                                int_number = Integer.parseInt(number);
                        } catch (NumberFormatException e) {
                            Log.debug(e.getMessage());
                        }
                    }

            }
        number = String.valueOf(int_number + 1);
        return name + number;
    }

    public static List<ColumnConstraint> removeDuplicatesConstraints(List<ColumnConstraint> columnConstraintList, DatabaseConnection dc) {
        List<String> cc_names = new ArrayList<>();
        List<ColumnConstraint> columnConstraints = new ArrayList<>();
        for (int i = 0, n = columnConstraintList.size(); i < n; i++) {
            if (MiscUtils.isNull(columnConstraintList.get(i).getName()) && !MiscUtils.isNull(columnConstraintList.get(i).getTypeName())) {
                String colType = columnConstraintList.get(i).getTypeName();
                columnConstraintList.get(i).setName(generateNameForConstraint(colType, columnConstraintList));
                columnConstraintList.get(i).setGeneratedName(true);
            }
            int ind = cc_names.indexOf(columnConstraintList.get(i).getName());
            if (ind >= 0) {
                ColumnConstraint cc_rep = columnConstraintList.get(i);
                ColumnConstraint cc_origin = columnConstraints.get(ind);
                if (cc_rep.getColumn() != null) {
                    String cols = cc_origin.getColumn();
                    if (cc_origin.getCountCols() == 1)
                        cols = format(cols, dc);
                    cols += "," + format(cc_rep.getColumn(), dc);
                    cc_origin.setColumn(cols);
                }
                if (cc_rep.getRefColumn() != null) {
                    String cols = cc_origin.getRefColumn();
                    if (cc_origin.getCountCols() == 1)
                        cols = format(cols, dc);
                    cols += "," + format(cc_rep.getRefColumn(), dc);
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

    public static String generateDefaultDropQuery(String metaTag, String name, DatabaseConnection dc) {
        String sb = "DROP " + metaTag + " " +
                format(name, dc) + ";\n";
        return sb;
    }

    public static String generateCreateDomain(
            ColumnData columnData, String name, boolean useDomainType, boolean setComment) {

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE DOMAIN ").append(name).append(" AS ");

        if (useDomainType)
            sb.append(columnData.getFormattedDomainDataType());
        else
            sb.append(columnData.getFormattedDataType());

        sb.append("\n");
        if (!MiscUtils.isNull(columnData.getDefaultValue().getValue()))
            sb.append(format(columnData.getDefaultValue(), columnData.getSQLType(), columnData.getDatabaseConnection()));
        sb.append(columnData.isRequired() ? " NOT NULL" : "");
        if (!MiscUtils.isNull(columnData.getCheck()))
            sb.append(" CHECK (").append(columnData.getCheck()).append(")");
        if (columnData.getCollate() != null && !columnData.getCollate().trim().contentEquals("NONE")
                && !columnData.getCollate().trim().contentEquals(""))
            sb.append(" COLLATE ").append(columnData.getCollate());
        sb.append(";");

        if (setComment && !MiscUtils.isNull(columnData.getDescription()))
            sb.append("\nCOMMENT ON DOMAIN ").append(columnData.getFormattedColumnName()).append(" IS '")
                    .append(columnData.getDescription()).append("';");

        return sb.toString();
    }

    public static String generateCreateUser(DefaultDatabaseUser user, boolean setComment) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE");
        sb.append(" USER ").append(format(user.getName(), user.getHost().getDatabaseConnection()));
        if (!MiscUtils.isNull(user.getFirstName()))
            sb.append("\nFIRSTNAME '").append(user.getFirstName()).append("'");
        if (!MiscUtils.isNull(user.getMiddleName()))
            sb.append("\nMIDDLENAME '").append(user.getMiddleName()).append("'");
        if (!MiscUtils.isNull(user.getLastName()))
            sb.append("\nLASTNAME '").append(user.getLastName()).append("'");
        if (!MiscUtils.isNull(user.getPassword())) {
            sb.append("\nPASSWORD '").append(user.getPassword()).append("'");
        }
        if (user.getActive()) {
            sb.append("\nACTIVE");
        } else {
            sb.append("\nINACTIVE");
        }
        if (user.getAdministrator()) {
            sb.append("\nGRANT ADMIN ROLE");
        }
        if (!user.getPlugin().equals(""))
            sb.append("\nUSING PLUGIN ").append(user.getPlugin());
        Map<String, String> tags = user.getTags();
        if (tags.size() > 0) {
            sb.append("\nTAGS (");
            boolean first = true;
            for (String tag : tags.keySet()) {
                if (!first)
                    sb.append(", ");
                first = false;
                sb.append(tag).append(" = '").append(tags.get(tag)).append("'");
            }
            sb.append(" )");
        }
        sb.append(";\n");
        if (setComment && !MiscUtils.isNull(user.getComment()))
            sb.append("COMMENT ON USER ").append(format(user.getName(), user.getHost().getDatabaseConnection())).append(" is '").append(user.getComment()).append("'");
        return sb.toString();
    }

    public static String generateAlterDomain(ColumnData thisDomainData, ColumnData domainData) throws DataSourceException {

        StringBuilder sb = new StringBuilder();
        sb.append("ALTER DOMAIN ").append(thisDomainData.getFormattedColumnName()).append("\n");
        String noChangesCheckString = sb.toString();

        if (!thisDomainData.getColumnName().contentEquals(domainData.getColumnName()))
            sb.append("TO ").append(domainData.getFormattedColumnName()).append("\n");

        if (!Objects.equals(thisDomainData.getDefaultValue().getValue(), domainData.getDefaultValue().getValue())) {

            if (MiscUtils.isNull(domainData.getDefaultValue().getValue()))
                sb.append("DROP DEFAULT\n");

            else {

                sb.append("SET DEFAULT ");
                if (domainData.getDefaultValue().getValue().toUpperCase().trim().equals("NULL"))
                    sb.append("NULL");
                else
                    sb.append(MiscUtils.formattedSQLValue(domainData.getDefaultValue(), domainData.getSQLType(), domainData.getDatabaseConnection()));
                sb.append("\n");
            }
        }

        if (thisDomainData.isDomainNotNull() != domainData.isRequired()) {

            if (domainData.isRequired())
                sb.append("SET ");
            else
                sb.append("DROP ");

            sb.append("NOT NULL\n");

        }
        if (!Objects.equals(thisDomainData.getCheck(), domainData.getCheck())) {

            sb.append("DROP CONSTRAINT\n");
            if (!MiscUtils.isNull(domainData.getCheck()))
                sb.append("ADD CHECK (").append(domainData.getCheck()).append(")\n");

        }

        if (!Objects.equals(thisDomainData.getDomainTypeName(), domainData.getDomainTypeName()))
            sb.append("TYPE ").append(domainData.getDomainTypeName());

        if (noChangesCheckString.contentEquals(sb))
            sb = new StringBuilder();
        else
            sb.append(";\n");

        if (!Objects.equals(thisDomainData.getDescription(), domainData.getDescription())) {
            sb.append("COMMENT ON DOMAIN ").append(thisDomainData.getFormattedColumnName()).append(" IS ");
            if (!Objects.equals(domainData.getDescription(), "") && domainData.getDescription() != null)
                sb.append("'").append(domainData.getDescription()).append("'");
            else
                sb.append("NULL");
        }

        return !sb.toString().equals("") ? sb.toString() : "/* there are no changes */\n";
    }

    public static String generateAlterDomain(ColumnData columnData, String domainName) {

        StringBuilder sb = new StringBuilder();
        sb.append("ALTER DOMAIN ").append(format(domainName, columnData.getDatabaseConnection())).append("\n");
        String noChangesCheckString = sb.toString();

        if (columnData.isNameChanged())
            sb.append("TO ").append(columnData.getFormattedColumnName()).append("\n");

        if (columnData.isDefaultChanged())
            if (!MiscUtils.isNull(columnData.getDefaultValue().getValue()))
                sb.append("SET DEFAULT ").append(MiscUtils.formattedSQLValue(
                        columnData.getDefaultValue(), columnData.getSQLType(), columnData.getDatabaseConnection())).append("\n");
            else
                sb.append("DROP DEFAULT\n");

        if (columnData.isRequiredChanged())
            sb.append(columnData.isRequired() ? "SET" : "DROP").append(" NOT NULL\n");

        if (columnData.isCheckChanged()) {
            sb.append("DROP CONSTRAINT\n");
            if (!MiscUtils.isNull(columnData.getCheck()))
                sb.append("ADD CHECK (").append(columnData.getCheck()).append(")\n");
        }

        if (columnData.isTypeChanged())
            sb.append("TYPE ").append(columnData.getFormattedDataType());

        if (noChangesCheckString.contentEquals(sb))
            sb = new StringBuilder();
        else
            sb.append(";\n");

        if (columnData.isDescriptionChanged()) {
            sb.append("COMMENT ON DOMAIN ").append(columnData.getFormattedColumnName()).append(" IS ");
            if (columnData.getDescription() != null)
                sb.append("'").append(columnData.getDescription()).append("'");
            else
                sb.append("NULL");
        }

        return !sb.toString().equals("") ? sb.toString() : "/* there are no changes */\n";
    }


    public static String generateAlterTable(
            DefaultDatabaseTable thisTable, DefaultDatabaseTable comparingTable,
            boolean temporary, boolean[] constraints, boolean computed, boolean fieldsPositions) {

        StringBuilder sb = new StringBuilder();

        if (temporary)
            sb.append("ALTER GLOBAL TEMPORARY TABLE ");
        else
            sb.append("ALTER TABLE ");
        sb.append(format(thisTable.getName(), thisTable.getHost().getDatabaseConnection()));
        String noChangesCheckString = sb.toString();

        List<String> thisColumnsNames = thisTable.getColumnNames();
        List<String> comparingColumnsNames = comparingTable.getColumnNames();

        for (String thisColumn : thisColumnsNames) {

            int dropCheck = 0;

            //check for ALTER COLUMN
            for (String comparingColumn : comparingColumnsNames) {
                if (Objects.equals(thisColumn, comparingColumn)) {
                    sb.append(generateAlterDefinitionColumn(
                            new ColumnData(thisTable.getHost().getDatabaseConnection(), thisTable.getColumn(thisColumn), false),
                            new ColumnData(comparingTable.getHost().getDatabaseConnection(), comparingTable.getColumn(comparingColumn), false),
                            computed, fieldsPositions));
                    break;

                } else dropCheck++;
            }

            //check for DROP COLUMN
            if (dropCheck == comparingColumnsNames.size())
                sb.append("\n\tDROP ").append(format(thisColumn, thisTable.getHost().getDatabaseConnection())).append(COMMA);
        }

        //check for ADD COLUMN
        for (String comparingColumn : comparingColumnsNames)
            if (!thisColumnsNames.contains(comparingColumn))
                sb.append("\n\tADD ").append(generateDefinitionColumn(new ColumnData(
                                comparingTable.getHost().getDatabaseConnection(),
                                comparingTable.getColumn(comparingColumn), false), computed, false, false))
                        .append(COMMA);

        if (!Arrays.equals(constraints, new boolean[]{false, false, false, false})) {

            List<org.executequery.databaseobjects.impl.ColumnConstraint> thisConstraints = thisTable.getConstraints();
            List<org.executequery.databaseobjects.impl.ColumnConstraint> comparingConstraints = comparingTable.getConstraints();

            //check for DROP CONSTRAINT
            for (org.executequery.databaseobjects.impl.ColumnConstraint thisConstraint : thisConstraints) {

                if ((thisConstraint.getType() == PRIMARY_KEY && !constraints[0]) ||
                        (thisConstraint.getType() == FOREIGN_KEY && !constraints[1]) ||
                        (thisConstraint.getType() == UNIQUE_KEY && !constraints[2]) ||
                        (thisConstraint.getType() == CHECK_KEY && !constraints[3]))
                    continue;

                int dropCheck = 0;
                for (org.executequery.databaseobjects.impl.ColumnConstraint comparingConstraint : comparingConstraints)
                    if (!Objects.equals(thisConstraint.getName(), comparingConstraint.getName()))
                        dropCheck++;
                    else break;

                if (dropCheck == comparingConstraints.size())
                    sb.append("\n\tDROP CONSTRAINT ").append(format(thisConstraint.getName(), thisTable.getHost().getDatabaseConnection())).append(COMMA);
            }

            //check for ADD CONSTRAINT
            for (org.executequery.databaseobjects.impl.ColumnConstraint comparingConstraint : comparingConstraints) {

                if ((comparingConstraint.getType() == PRIMARY_KEY && !constraints[0]) ||
                        (comparingConstraint.getType() == FOREIGN_KEY && !constraints[1]) ||
                        (comparingConstraint.getType() == UNIQUE_KEY && !constraints[2]) ||
                        (comparingConstraint.getType() == CHECK_KEY && !constraints[3]))
                    continue;

                int addCheck = 0;
                for (org.executequery.databaseobjects.impl.ColumnConstraint thisConstraint : thisConstraints)
                    if (!Objects.equals(thisConstraint.getName(), comparingConstraint.getName()))
                        addCheck++;
                    else break;

                if (addCheck == thisConstraints.size())
                    sb.append("\n\tADD ").append(generateDefinitionColumnConstraint(
                                    new org.executequery.gui.browser.ColumnConstraint(false, comparingConstraint), true, false, thisTable.getHost().getDatabaseConnection()))
                            .append(COMMA);
            }

        }

        if (noChangesCheckString.contentEquals(sb))
            return "/* there are no changes */\n";
        return sb.deleteCharAt(sb.length() - 1).append(";\n").toString();
    }

    public static String generateAlterException(
            DefaultDatabaseException thisException, DefaultDatabaseException comparingException) {

        String comparingExceptionText = comparingException.getExceptionText();
        if (Objects.equals(thisException.getExceptionText(), comparingExceptionText))
            return "/* there are no changes */\n";

        String sb = "ALTER EXCEPTION " + format(thisException.getName(), thisException.getHost().getDatabaseConnection()) +
                SPACE + comparingExceptionText + ";\n";
        return sb;
    }

    public static String generateAlterSequence(
            DefaultDatabaseSequence thisSequence, DefaultDatabaseSequence comparingSequence, int version) {

        StringBuilder sb = new StringBuilder();
        sb.append("ALTER SEQUENCE ").append(format(thisSequence.getName(), thisSequence.getHost().getDatabaseConnection()));
        String noChangesCheckString = sb.toString();

        if (version > 2) {
            if (thisSequence.getSequenceFirstValue() != comparingSequence.getSequenceFirstValue())
                sb.append("\n\tRESTART WITH ").append(comparingSequence.getSequenceFirstValue());
            if (thisSequence.getIncrement() != comparingSequence.getIncrement())
                sb.append("\n\tINCREMENT BY ").append(comparingSequence.getIncrement());
        }

        if (noChangesCheckString.contentEquals(sb))
            return "/* there are no changes */\n";
        return sb.append(";\n").toString();
    }

    public static String generateAlterUDF(
            DefaultDatabaseUDF thisUDF, DefaultDatabaseUDF comparingUDF) {

        StringBuilder sb = new StringBuilder();
        sb.append("ALTER EXTERNAL FUNCTION ").append(format(thisUDF.getName(), thisUDF.getHost().getDatabaseConnection()));
        String noChangesCheckString = sb.toString();

        if (!Objects.equals(thisUDF.getEntryPoint(), comparingUDF.getEntryPoint()))
            sb.append("\nENTRY_POINT '").append(comparingUDF.getEntryPoint()).append("'");
        if (!Objects.equals(thisUDF.getModuleName(), comparingUDF.getModuleName()))
            sb.append("\nMODULE_NAME '").append(comparingUDF.getModuleName()).append("'");

        if (noChangesCheckString.contentEquals(sb))
            return "/* there are no changes */\n";
        return sb.append(";\n").toString();
    }

    public static String generateAlterIndex(
            DefaultDatabaseIndex thisIndex, DefaultDatabaseIndex comparingIndex) {

        StringBuilder sb = new StringBuilder();

        String comparedIndexCreateQuery = comparingIndex.getCreateSQLText();
        String thisIndexCreateQuery = thisIndex.getCreateSQLText();
        if (!thisIndexCreateQuery.equals(comparedIndexCreateQuery) &&
                (thisIndexCreateQuery.contains(" INACTIVE;") || comparedIndexCreateQuery.contains(" INACTIVE;"))) {

            sb.append("ALTER INDEX ").append(format(thisIndex.getName(), thisIndex.getHost().getDatabaseConnection()));
            sb.append(comparingIndex.isActive() ? " ACTIVE" : " INACTIVE").append(";\n");
            return sb.toString();
        }

        if (!thisIndexCreateQuery.equals(comparedIndexCreateQuery)) {
            sb.append(generateDefaultDropQuery("INDEX", thisIndex.getName(), thisIndex.getHost().getDatabaseConnection()));
            sb.append(comparedIndexCreateQuery);
            return sb.toString();
        }

        return "/* there are no changes */\n";
    }

    public static String generateAlterUser(DefaultDatabaseUser thisUser, DefaultDatabaseUser compareUser, boolean setComment) {

        StringBuilder sb = new StringBuilder();
        sb.append("ALTER USER ").append(format(thisUser.getName(), thisUser.getHost().getDatabaseConnection()));
        String noChangesCheckString = sb.toString();

        if (!Objects.equals(thisUser.getFirstName(), compareUser.getFirstName()))
            sb.append("\n\tFIRSTNAME '").append(compareUser.getFirstName()).append("'");

        if (!Objects.equals(thisUser.getMiddleName(), compareUser.getMiddleName()))
            sb.append("\n\tMIDDLENAME '").append(compareUser.getMiddleName()).append("'");

        if (!Objects.equals(thisUser.getLastName(), compareUser.getLastName()))
            sb.append("\n\tLASTNAME '").append(compareUser.getLastName()).append("'");

        if (!Objects.equals(thisUser.getPassword(), compareUser.getPassword()))
            if (!Objects.equals(compareUser.getPassword(), ""))
                sb.append("\n\tPASSWORD '").append(compareUser.getPassword()).append("'");

        if (thisUser.getActive() != compareUser.getActive())
            sb.append(compareUser.getActive() ? "\n\tACTIVE" : "\n\tINACTIVE");

        if (thisUser.getAdministrator() != compareUser.getAdministrator())
            sb.append(compareUser.getAdministrator() ? "\n\tGRANT ADMIN ROLE" : "\n\tREVOKE ADMIN ROLE");

        if (!Objects.equals(thisUser.getPlugin(), compareUser.getPlugin()))
            if (!Objects.equals(compareUser.getPlugin(), "") && compareUser.getPlugin() != null)
                sb.append("\nUSING PLUGIN ").append(compareUser.getPlugin());

        Map<String, String> thisTags = thisUser.getTags();
        Map<String, String> compareTags = compareUser.getTags();

        if (!thisTags.equals(compareTags)) {
            sb.append("\n\tTAGS (");

            for (String tag : thisTags.keySet())
                if (!compareTags.containsKey(tag))
                    sb.append("DROP ").append(tag).append(", ");

            for (String tag : compareTags.keySet())
                sb.append(tag).append(" = '").append(compareTags.get(tag)).append("', ");

            sb.deleteCharAt(sb.lastIndexOf(",")).append(" )");
        }

        if (noChangesCheckString.contentEquals(sb))
            sb = new StringBuilder();
        else
            sb.append(";\n");

        if (setComment && !Objects.equals(thisUser.getComment(), compareUser.getComment())) {
            sb.append("COMMENT ON USER ").append(format(thisUser.getName(), thisUser.getHost().getDatabaseConnection())).append(" IS ");
            if (!Objects.equals(compareUser.getComment(), "") && compareUser.getComment() != null)
                sb.append("'").append(compareUser.getComment()).append("'");
            else
                sb.append("NULL");
        }

        return !sb.toString().equals("") ? sb.toString() : "/* there are no changes */\n";
    }


    public static String generateAlterTablespace(
            DefaultDatabaseTablespace thisTablespace, DefaultDatabaseTablespace comparingTablespace) {

        String comparingFileName = comparingTablespace.getFileName();

        return !Objects.equals(thisTablespace.getFileName(), comparingFileName) ?
                "/* there are no changes */\n" :
                generateAlterTablespace(thisTablespace.getName(), comparingFileName, thisTablespace.getHost().getDatabaseConnection());
    }

    public static String generateAlterTablespace(String name, String file, DatabaseConnection dc) {
        String sb = "ALTER TABLESPACE " + format(name, dc) +
                " SET FILE '" + file + "';\n";
        return sb;
    }

    public static String generateCreateTablespace(String name, String file, DatabaseConnection dc) {
        String sb = "CREATE TABLESPACE " + format(name, dc) +
                " FILE '" + file + "';\n";
        return sb;
    }

    public static String generateCreateSequence(
            String name, long startValue, long increment, String description, int databaseVersion, boolean existed, DatabaseConnection dc) {

        StringBuilder sb = new StringBuilder();

        if (databaseVersion >= 3) {

            sb.append("CREATE OR ALTER SEQUENCE ").append(format(name, dc));
            sb.append(" START WITH ").append(startValue);
            sb.append(" INCREMENT BY ").append(increment);
            sb.append(";\n");

        } else {

            if (!existed)
                sb.append("CREATE SEQUENCE ").append(format(name, dc)).append(";\n");
            sb.append("ALTER SEQUENCE ").append(format(name, dc));
            sb.append(" RESTART WITH ").append(startValue + increment).append(";\n");

        }

        if (description != null && !description.trim().equals(""))
            sb.append(generateComment(name, "SEQUENCE", description.trim(), ";", false, dc));

        return sb.toString();
    }

    public static String generateCreateView(
            String name, String fields, String selectStatement, String description, int databaseVersion, boolean existed, boolean useNewSyntax, DatabaseConnection dc) {

        StringBuilder sb = new StringBuilder();

        if (databaseVersion >= 3 && useNewSyntax)
            sb.append("CREATE OR ALTER VIEW ").append(format(name, dc));
        else if (!existed)
            sb.append("CREATE VIEW ").append(format(name, dc));
        else
            sb.append("ALTER VIEW ").append(format(name, dc));

        if (fields != null && !fields.trim().isEmpty())
            sb.append(" (").append(fields.trim()).append(") ");
        sb.append("\nAS \n").append(selectStatement.trim());
        sb.append(sb.toString().endsWith(";") ? "\n" : ";\n");

        if (description != null && !description.trim().isEmpty())
            sb.append(generateComment(name, "VIEW", description.trim(), ";", false, dc));

        return sb.toString();
    }

    public static String generateDefaultUpdateStatement(String name, String settings, DatabaseConnection dc) {
        String sb = "UPDATE " + format(name.trim(), dc) +
                " SET " + settings.trim() + ";\n";
        return sb;
    }

    public static String generateDefaultInsertStatement(String name, String fields, String values, DatabaseConnection dc) {
        String sb = "INSERT INTO " + format(name.trim(), dc) +
                " (" + fields.trim() + ")" +
                " VALUES (" + values.trim() + ");\n";
        return sb;
    }

    public static String generateDefaultSelectStatement(String name, String fields, DatabaseConnection dc) {

        String sb = "SELECT " + fields.trim() +
                " FROM " + format(name.trim(), dc) + ";\n";

        return sb;
    }

    public static String generateCreateTriggerStatement(
            String name, String tableName, boolean active, String triggerType, int position,
            String sourceCode, String engine, String entryPoint, String sqlSecurity, String comment, boolean setTerm, DatabaseConnection dc) {
        StringBuilder sb = new StringBuilder();

        if (setTerm)
            sb.append("SET TERM ^;\n");

        sb.append("CREATE OR ALTER TRIGGER ").append(format(name, dc));
        if (!MiscUtils.isNull(tableName))
            sb.append(" FOR ").append(format(tableName, dc));

        sb.append("\n").append(active ? "ACTIVE" : "INACTIVE");
        sb.append(" ").append(triggerType);
        sb.append(" POSITION ").append(position);
        sb.append("\n");
        if (!MiscUtils.isNull(sqlSecurity)) {
            sb.append(SQL_SECURITY).append(sqlSecurity).append("\n");
        }
        if (!MiscUtils.isNull(entryPoint)) {

            sb.append("EXTERNAL NAME '").append(entryPoint).append("'");
            if (!MiscUtils.isNull(engine))
                sb.append("\n").append("ENGINE ").append(engine);

        } else if (!MiscUtils.isNull(sourceCode))
            sb.append(sourceCode);

        sb.append("^\n");

        if (!MiscUtils.isNull(comment) && !comment.equals("")) {
            comment = comment.replace("'", "''");
            sb.append("COMMENT ON TRIGGER ").append(format(name, dc)).append(" IS '").append(comment).append("'^");
        }

        if (setTerm)
            sb.append("SET TERM ;^\n");

        return sb.toString();
    }

    public static String generateCreateException(String name, String exceptionText, DatabaseConnection dc) {
        String sb = "CREATE EXCEPTION " + format(name, dc) +
                "\n'" + exceptionText + "';\n";
        return sb;
    }

    public static String generateCreatePackage(
            String name, String headerSource, String bodySource, String description, DatabaseConnection dc) {

        StringBuilder sb = new StringBuilder();

        sb.append("SET TERM ^ ;");
        sb.append("\n").append(headerSource);
        sb.append("^\n").append(bodySource);
        sb.append("^\n").append("SET TERM ; ^").append("\n");

        if (description != null && !description.isEmpty())
            sb.append(generateComment(name, "PACKAGE", description, ";", false, dc));

        return sb.toString();
    }

    public static String generateCreateUDF(
            String name, List<UDFParameter> parameters, int returnArg,
            String entryPoint, String moduleName, boolean freeIt, DatabaseConnection dc) {

        int BY_VALUE = 0;
        int BY_REFERENCE = 1;
        int BY_DESCRIPTOR = 2;

        StringBuilder sb = new StringBuilder();

        sb.append("DECLARE EXTERNAL FUNCTION ").append(format(name, dc)).append("\n");

        String args = "";
        Map<UDFParameter, ColumnData> mapParameters = new HashMap<>();

        for (UDFParameter parameter : parameters) {
            ColumnData cd = columnDataFromProcedureParameter(parameter, dc, false);
            mapParameters.put(parameter, cd);
        }
        for (int i = 0; i < parameters.size(); i++) {

            if (returnArg == 0 && i == 0)
                continue;

            args += "\t" + mapParameters.get(parameters.get(i)).getFormattedDataType();
            if (parameters.get(i).getMechanism() != BY_VALUE && parameters.get(i).getMechanism() != BY_REFERENCE)
                if (parameters.get(i).isNotNull() || parameters.get(i).getMechanism() == BY_DESCRIPTOR)
                    args += " " + parameters.get(i).getStringMechanism();

            if (!parameters.get(i).isNotNull() && parameters.get(i).getMechanism() != BY_DESCRIPTOR &&
                    parameters.get(i).getMechanism() != BY_REFERENCE && returnArg - 1 != i)
                args += " " + "NULL";

            args += ",\n";
        }

        if (!args.isEmpty())
            args = args.substring(0, args.length() - 2);

        sb.append(args).append("\nRETURNS\n");

        if (returnArg == 0) {

            sb.append(mapParameters.get(parameters.get(0)).getFormattedDataType());
            if (parameters.get(0).getMechanism() != BY_REFERENCE && parameters.get(0).getMechanism() != -1)
                sb.append(" ").append(parameters.get(0).getStringMechanism());

        } else
            sb.append("PARAMETER ").append(returnArg);

        if (freeIt)
            sb.append(" FREE_IT ");

        sb.append("\nENTRY_POINT '");
        if (!MiscUtils.isNull(entryPoint))
            sb.append(entryPoint.replace("'", "''"));

        sb.append("' MODULE_NAME '");
        if (!MiscUtils.isNull(moduleName))
            sb.append(moduleName.replace("'", "''"));
        sb.append("';\n");

        return sb.toString();
    }

    public static String generateCreateRole(String name, DatabaseConnection dc) {
        return "CREATE ROLE " + format(name, dc) + ";\n";
    }

    public static String generateCreateIndex(
            String name, int type, boolean isUnique, String tableName, String expression, String condition,
            List indexColumns, String tablespace, boolean isActive, String comment, DatabaseConnection dc) {

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE ");

        if (isUnique)
            sb.append("UNIQUE ");
        if (type == 1)
            sb.append("DESCENDING ");

        sb.append("INDEX ").append(format(name, dc));
        sb.append(" ON ").append(format(MiscUtils.trimEnd(tableName), dc)).append(SPACE);

        if (!MiscUtils.isNull(expression)) {
            sb.append("COMPUTED BY (").append(expression).append(B_CLOSE);

        } else {

            boolean first = true;
            int columnType = -1;
            StringBuilder fields = new StringBuilder();

            for (Object indexColumn : indexColumns) {

                if (first)
                    columnType = (indexColumn instanceof DefaultDatabaseIndex.DatabaseIndexColumn) ? 0 : 1;
                else
                    fields.append(COMMA);
                first = false;

                if (columnType == 0)
                    fields.append(format(((DefaultDatabaseIndex.DatabaseIndexColumn) indexColumn).getFieldName(), dc));
                else
                    fields.append(format((String) indexColumn, dc));
            }

            sb.append(B_OPEN).append(fields).append(B_CLOSE);
        }

        if (!MiscUtils.isNull(condition))
            sb.append("\nWHERE ").append(condition);
        if (!MiscUtils.isNull(tablespace))
            sb.append("\nTABLESPACE ").append(format(tablespace, dc));

        sb.append(";");

        if (!isActive)
            sb.append("ALTER INDEX ").append(format(name, dc)).append(" INACTIVE;");
        if (!MiscUtils.isNull(comment))
            sb.append("COMMENT ON INDEX ").append(format(name, dc)).append(" IS '").append(comment).append("';");

        return sb.toString();
    }

    public static String generateAlterIndex(String name, Boolean isActive, String tablespace, String comment, DatabaseConnection dc) {

        StringBuilder sb = new StringBuilder();

        if (isActive != null)
            sb.append("ALTER INDEX ").append(format(name, dc)).append(isActive ? " ACTIVE" : " INACTIVE").append(";\n");
        if (tablespace != null)
            sb.append("ALTER INDEX ").append(format(name, dc)).append(" SET TABLESPACE TO ").append(tablespace).append(";\n");
        if (comment != null)
            sb.append(generateComment(name, "INDEX", comment, ";", false, dc));

        return sb.toString();
    }

    public static String generateCreateDefaultStub(NamedObject object) {

        StringBuilder sb = new StringBuilder();
        sb.append("SET TERM ^;\n");

        switch (object.getType()) {

            case (FUNCTION):
                sb.append(generateCreateFunctionStub((DefaultDatabaseFunction) object));
                break;

            case (PROCEDURE):
                sb.append(generateCreateProcedureStub((DefaultDatabaseProcedure) object));
                break;

            case (TRIGGER):
            case (DDL_TRIGGER):
            case (DATABASE_TRIGGER):
                sb.append(generateCreateTriggerStub((DefaultDatabaseTrigger) object));
                break;

            case (JOB):
                sb.append(generateCreateJobStub((DefaultDatabaseJob) object));
                break;
        }

        sb.append("\nSET TERM ;^\n");
        return sb.toString();
    }

    public static String generateCreateFunctionStub(DefaultDatabaseFunction obj) {

        StringBuilder sb = new StringBuilder();

        Vector<ColumnData> inputParams = new Vector<>();
        ColumnData returnType = null;

        for (FunctionArgument param : obj.getFunctionArguments()) {

            if (param.getType() == DatabaseMetaData.procedureColumnIn) {
                ColumnData cd = columnDataFromProcedureParameter(
                        param, obj.getHost().getDatabaseConnection(), false);
                inputParams.add(cd);

            } else
                returnType = columnDataFromProcedureParameter(
                        param, obj.getHost().getDatabaseConnection(), false);
        }

        sb.append(generateCreateProcedureOrFunctionHeader(
                obj.getName(), inputParams, NamedObject.META_TYPES[FUNCTION], null, obj.getHost().getDatabaseConnection()));

        sb.append("RETURNS ");
        if (returnType != null)
            sb.append(returnType.getFormattedDataType());

        return sb.append("\nAS BEGIN return null; END^\n").toString();
    }

    public static String generateCreateProcedureStub(DefaultDatabaseProcedure obj) {

        StringBuilder sb = new StringBuilder();

        Vector<ColumnData> inputParams = new Vector<>();
        Vector<ColumnData> outputParams = new Vector<>();

        for (ProcedureParameter param : obj.getParameters()) {
            ColumnData cd = columnDataFromProcedureParameter(
                    param, obj.getHost().getDatabaseConnection(), false);

            if (param.getType() == DatabaseMetaData.procedureColumnIn)
                inputParams.add(cd);
            else
                outputParams.add(cd);
        }

        sb.append(generateCreateProcedureOrFunctionHeader(
                obj.getName(), inputParams, NamedObject.META_TYPES[PROCEDURE], null, obj.getHost().getDatabaseConnection()));

        String formattedOutputParams = formattedParameters(outputParams, false);
        if (!MiscUtils.isNull(formattedOutputParams.trim()))
            sb.append(String.format("RETURNS (\n%s)", formattedOutputParams));

        return sb.append("\nAS BEGIN END^\n").toString();
    }

    public static String generateCreateTriggerStub(DefaultDatabaseTrigger obj) {

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE OR ALTER TRIGGER ").append(format(obj.getName(), obj.getHost().getDatabaseConnection()));

        if (obj.getType() == TRIGGER) {
            sb.append("\n\tFOR ").append(format(obj.getTriggerTableName(), obj.getHost().getDatabaseConnection()));
            sb.append(" BEFORE INSERT");

        } else if (obj.getType() == DDL_TRIGGER)
            sb.append("\n\tBEFORE CREATE|ALTER|DROP TABLE");

        else
            sb.append(" ON CONNECT");

        return sb.append("\nAS BEGIN END^\n").toString();
    }

    public static String generateCreateJobStub(DefaultDatabaseJob obj) {

        String sb = "CREATE JOB " + format(obj.getName(), obj.getHost().getDatabaseConnection()) + "\n" +
                "\t'' COMMAND ''^";

        return sb;
    }

    public static String generateCreateCollation(
            String name, String charset, String baseCollation, String attributes, boolean padSpace,
            boolean caseSensitive, boolean accentSensitive, boolean isExternal) {

        StringBuilder sb = new StringBuilder();

        sb.append("CREATE COLLATION ").append(name);
        sb.append("\nFOR ").append(charset);

        if (!MiscUtils.isNull(baseCollation)) {
            sb.append("\nFROM ");
            if (isExternal)
                sb.append("EXTERNAL ('");
            sb.append(baseCollation);
            if (isExternal)
                sb.append("')");
        }

        sb.append("\n").append(padSpace ? "PAD SPACE" : "NO PAD");
        sb.append("\nCASE ").append(caseSensitive ? "SENSITIVE" : "INSENSITIVE");
        sb.append("\nACCENT ").append(accentSensitive ? "SENSITIVE" : "INSENSITIVE");

        if (!MiscUtils.isNull(attributes))
            sb.append("\n'").append(attributes).append("'");

        return sb.append(";\n").toString();
    }

    public static String generateCreateJob(
            String name, String cronSchedule, boolean active, LocalDateTime startDate, LocalDateTime endDate,
            int jobType, String source, String comment, boolean setTerm, DatabaseConnection dc) {

        StringBuilder sb = new StringBuilder();
        sb.append(setTerm ? "SET TERM ^;\n" : "");

        sb.append("CREATE JOB ").append(format(name, dc)).append("\n");
        sb.append("'").append(cronSchedule).append("'").append("\n");
        sb.append(active ? "ACTIVE" : "INACTIVE").append("\n");

        sb.append("START DATE ");
        if (startDate != null)
            sb.append("'").append(startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))).append("'\n");
        else
            sb.append("NULL\n");

        sb.append("END DATE ");
        if (endDate != null)
            sb.append("'").append(endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))).append("'\n");
        else
            sb.append("NULL\n");

        if (jobType == DefaultDatabaseJob.BASH_TYPE)
            sb.append("COMMAND '").append(source).append("'");
        else
            sb.append("AS\n").append(source);

        if (comment != null && !comment.isEmpty())
            sb.append("^").append(generateComment(name, "JOB", comment, "", false, dc));

        return sb.append(setTerm ? "^\nSET TERM ;^\n" : "^\n").toString();
    }

    public static String generateAlterJob(
            DefaultDatabaseJob job, String name, String cronSchedule, boolean active, LocalDateTime startDate,
            LocalDateTime endDate, int jobType, String source, String comment, boolean setTerm) {

        StringBuilder sb = new StringBuilder();
        sb.append(setTerm ? "SET TERM ^;\n" : "");
        sb.append("ALTER JOB ").append(format(name, job.getHost().getDatabaseConnection())).append("\n");

        if (!job.getCronSchedule().equals(cronSchedule))
            sb.append("'").append(cronSchedule).append("'").append("\n");

        if (job.isActive() != active)
            sb.append(active ? "ACTIVE" : "INACTIVE").append("\n");

        if (!Objects.equals(job.getStartDate(), startDate)) {
            sb.append("START DATE ");
            if (startDate != null)
                sb.append("'").append(startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))).append("'\n");
            else
                sb.append("NULL\n");
        }

        if (!Objects.equals(job.getEndDate(), endDate)) {
            sb.append("END DATE ");
            if (endDate != null)
                sb.append("'").append(endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))).append("'\n");
            else
                sb.append("NULL\n");
        }

        if (job.getJobType() != jobType || !job.getSource().equals(source)) {
            if (jobType == DefaultDatabaseJob.BASH_TYPE)
                sb.append("COMMAND '").append(source).append("'");
            else
                sb.append("AS\n").append(source);
        }

        if (!Objects.equals(job.getRemarks(), comment) && !comment.isEmpty())
            sb.append("^").append(generateComment(name, "JOB", comment, "", false, job.getHost().getDatabaseConnection()));

        return sb.append(setTerm ? "^\nSET TERM ;^\n" : "^\n").toString();
    }

    public static String generateAlterJob(DefaultDatabaseJob thisJob, DefaultDatabaseJob compareJob) {

        StringBuilder sb = new StringBuilder();
        sb.append("SET TERM ^;\n");
        sb.append("ALTER JOB ").append(format(thisJob.getName(), thisJob.getHost().getDatabaseConnection())).append("\n");

        String noChangesCheckString = sb.toString();

        if (!thisJob.getCronSchedule().equals(compareJob.getCronSchedule()))
            sb.append("'").append(compareJob.getCronSchedule()).append("'").append("\n");

        if (thisJob.isActive() != compareJob.isActive())
            sb.append(compareJob.isActive() ? "ACTIVE" : "INACTIVE").append("\n");

        if (!Objects.equals(thisJob.getStartDate(), compareJob.getStartDate())) {
            sb.append("START DATE ");
            if (compareJob.getStartDate() != null)
                sb.append("'").append(compareJob.getStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))).append("'\n");
            else
                sb.append("NULL\n");
        }

        if (!Objects.equals(thisJob.getEndDate(), compareJob.getEndDate())) {
            sb.append("END DATE ");
            if (compareJob.getEndDate() != null)
                sb.append("'").append(compareJob.getEndDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))).append("'\n");
            else
                sb.append("NULL\n");
        }

        if (thisJob.getJobType() != compareJob.getJobType() || !thisJob.getSource().equals(compareJob.getSource())) {
            if (compareJob.getJobType() == DefaultDatabaseJob.BASH_TYPE)
                sb.append("COMMAND '").append(compareJob.getSource()).append("'");
            else
                sb.append("AS\n").append(compareJob.getSource());
        }

        if (!Objects.equals(thisJob.getRemarks(), compareJob.getRemarks()) && !compareJob.getRemarks().isEmpty())
            sb.append("^").append(generateComment(thisJob.getName(), "JOB", compareJob.getRemarks(), "", false, thisJob.getHost().getDatabaseConnection()));

        if (noChangesCheckString.contentEquals(sb))
            return "/* there are no changes */\n";
        return sb.append("^\nSET TERM ;^\n").toString();
    }

    private static String format(String object, DatabaseConnection dc) {
        return MiscUtils.getFormattedObject(object, dc);
    }

    private static String format(ColumnData.DefaultValue defaultValue, int type, DatabaseConnection dc) {
        return MiscUtils.formattedDefaultValue(defaultValue, type, dc);
    }

}


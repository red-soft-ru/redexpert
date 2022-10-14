package org.underworldlabs.util;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.MetaDataValues;
import org.executequery.databaseobjects.*;
import org.executequery.databaseobjects.impl.DefaultDatabaseIndex;
import org.executequery.databaseobjects.impl.DefaultDatabaseUDF;
import org.executequery.databaseobjects.impl.DefaultDatabaseUser;
import org.executequery.gui.browser.ColumnConstraint;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.table.CreateTableSQLSyntax;
import org.executequery.gui.table.TableDefinitionPanel;
import org.underworldlabs.jdbc.DataSourceException;

import java.sql.DatabaseMetaData;
import java.sql.Types;
import java.util.*;

import static org.executequery.databaseobjects.NamedObject.*;
import static org.executequery.gui.browser.ColumnConstraint.RESTRICT;
import static org.executequery.gui.browser.ColumnConstraint.RULES;
import static org.executequery.gui.table.CreateTableSQLSyntax.*;

public final class SQLUtils {

    public static String generateCreateTable(
            String name, List<ColumnData> columnDataList, List<ColumnConstraint> columnConstraintList,
            boolean existTable, boolean temporary, boolean constraints, String typeTemporary, String externalFile,
            String adapter, String tablespace, String comment) {

        StringBuilder sb = new StringBuilder();
        StringBuilder sqlText = new StringBuilder();

        if (temporary)
            sb.append(CreateTableSQLSyntax.CREATE_GLOBAL_TEMPORARY_TABLE);
        else
            sb.append(CreateTableSQLSyntax.CREATE_TABLE);

        StringBuilder primaryText = new StringBuilder();
        StringBuilder primary = new StringBuilder(50);

        primary.setLength(0);
        primary.append(",\nCONSTRAINT PK_").append(name).append(" PRIMARY KEY (");

        boolean primary_flag = false;
        String autoincrementSQLText = "";

        for (int i = 0, k = columnDataList.size(); i < k; i++) {

            ColumnData cd = columnDataList.get(i);
            autoincrementSQLText += cd.getAutoincrement().getSqlAutoincrement();

            if (cd.isPrimaryKey()) {

                if (primary_flag)
                    primaryText.append(", ");
                else
                    primaryText.append(" ");

                primaryText.append(cd.getFormattedColumnName());
                primary_flag = true;
            }

//            if (!MiscUtils.isNull(cd.getDescription())) {
//                descriptions.add(cd.getFormattedColumnName() + " is '" + cd.getDescription() + "'");
//            }

            sqlText.append(generateDefinitionColumn(cd));
            if (i != k - 1)
                sqlText.append(COMMA);
        }

        if (primary_flag)
            primary.append(primaryText);

        primary.append(")");
        String description = generateCommentForColumns(name, columnDataList, "COLUMN", "^");

        sb.append(format(name));

        if (externalFile != null)
            sb.append(NEW_LINE).append("EXTERNAL FILE '").append(externalFile.trim()).append("'");
        if (adapter != null)
            sb.append(SPACE).append(" ADAPTER '").append(adapter.trim()).append("'");

        sb.append(SPACE).append(B_OPEN);
        sb.append(sqlText.toString().replaceAll(TableDefinitionPanel.SUBSTITUTE_NAME, format(name)));

        if (primary_flag && !existTable)
            sb.append(primary);

        columnConstraintList = removeDuplicatesConstraints(columnConstraintList);

        if (constraints)
            for (ColumnConstraint columnConstraint : columnConstraintList)
                sb.append(generateDefinitionColumnConstraint(columnConstraint).replaceAll(TableDefinitionPanel.SUBSTITUTE_NAME, format(name)));

        sb.append(CreateTableSQLSyntax.B_CLOSE);

        if (tablespace != null)
            sb.append("\nTABLESPACE ").append(format(tablespace));
        if (temporary)
            sb.append("\n").append(typeTemporary);

        sb.append(";\n").append(description);

        if (autoincrementSQLText != null)
            sb.append(autoincrementSQLText.replace(TableDefinitionPanel.SUBSTITUTE_NAME, format(name))).append(NEW_LINE);
        if (comment != null)
            sb.append("COMMENT ON TABLE ").append(name).append(" IS '").append(comment).append("';\n");

        return sb.toString();
    }

    public static String generateCreateView(
            String name, String fields, String selectStatement, String description, int databaseVersion, boolean existed) {

        StringBuilder sb = new StringBuilder();

        if (databaseVersion >= 3)
            sb.append("CREATE OR ALTER VIEW ").append(format(name));
        else if (!existed)
            sb.append("CREATE VIEW ").append(format(name));
        else
            sb.append("ALTER VIEW ").append(format(name));

        if (fields != null && !fields.trim().equals(""))
            sb.append(" (").append(fields.trim()).append(") ");
        sb.append("\nAS \n").append(selectStatement.trim()).append(";\n");

        if (description != null && !description.trim().equals(""))
            sb.append(generateComment(name, "VIEW", description.trim(), ";"));

        return sb.toString();
    }

    public static String generateCreateException(String name, String exceptionText) {

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE EXCEPTION ").append(format(name));
        sb.append("\n'").append(exceptionText).append("';\n");
        return sb.toString();
    }

    public static String generateCreatePackage(
            String name, String headerSource, String bodySource, String description) {

        StringBuilder sb = new StringBuilder();

        sb.append("SET TERM ^ ;");
        sb.append("\n").append(headerSource);
        sb.append("^\n").append(bodySource);
        sb.append("^\n").append("SET TERM ; ^").append("\n");

        if (description != null && !description.isEmpty())
            sb.append(generateComment(name, "PACKAGE", description, "^"));

        return sb.toString();
    }

    public static String generateCreateTrigger(
            String name, String tableName, boolean activity, String type, int sequence, String sourceCode) {

        StringBuilder sb = new StringBuilder();

        sb.append("CREATE OR ALTER TRIGGER ");
        sb.append(format(name));
        if (!tableName.isEmpty())
            sb.append(" FOR ").append(format(tableName));
        sb.append("\n").append(activity ? "ACTIVE" : "INACTIVE");
        sb.append(" ").append(type).append(" POSITION ").append(sequence).append("\n");
        if (sourceCode != null)
            sb.append(sourceCode);
        sb.append("\n");

        return sb.toString();
    }

    public static String generateCreateSequence(
            String name, long startValue, long increment, String description, int databaseVersion, boolean existed) {

        StringBuilder sb = new StringBuilder();

        if (databaseVersion >= 3) {

            sb.append("CREATE OR ALTER SEQUENCE ").append(format(name));
            sb.append(" START WITH ").append(startValue);
            sb.append(" INCREMENT BY ").append(increment);
            sb.append(";\n");

        } else {

            if (!existed)
                sb.append("CREATE SEQUENCE ").append(format(name)).append(";\n");
            sb.append("ALTER SEQUENCE ").append(format(name));
            sb.append(" RESTART WITH ").append(startValue + increment).append(";\n");

        }

        if (description != null && !description.trim().equals(""))
            sb.append(generateComment(name, "SEQUENCE", description.trim(), ";"));

        return sb.toString();
    }

    public static String generateCreateUDF(
            String name, List<DefaultDatabaseUDF.UDFParameter> parameters, int returnArg,
            String entryPoint, String moduleName, boolean freeIt) {

        int BY_VALUE = 0;
        int BY_REFERENCE = 1;
        int BY_DESCRIPTOR = 2;

        StringBuilder sb = new StringBuilder();

        sb.append("DECLARE EXTERNAL FUNCTION ").append(format(name)).append("\n");

        String args = "";
        for (int i = 0; i < parameters.size(); i++) {

            if (returnArg == 0 && i == 0)
                continue;

            args += "\t" + parameters.get(i).getFieldStringType();
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

            sb.append(parameters.get(0).getFieldStringType());
            if (parameters.get(0).getMechanism() != BY_REFERENCE && parameters.get(0).getMechanism() != -1)
                sb.append(" ").append(parameters.get(0).getStringMechanism());

        } else
            sb.append("PARAMETER ").append(returnArg);

        if (freeIt)
            sb.append(" FREE_IT ");

        sb.append("\nENTRY_POINT '");
        if (!MiscUtils.isNull(entryPoint))
            sb.append(entryPoint);

        sb.append("' MODULE_NAME '");
        if (!MiscUtils.isNull(moduleName))
            sb.append(moduleName);
        sb.append("';\n");

        return sb.toString();
    }

    public static String generateCreateRole(String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE ROLE ").append(format(name)).append(";\n");
        return sb.toString();
    }

    public static String generateCreateIndex(
            String name, int type, String namePrefix, String tableName,
            List<DatabaseColumn> databaseColumns, List<DefaultDatabaseIndex.DatabaseIndexColumn> databaseIndexColumns) {

        StringBuilder sb = new StringBuilder();

        String stringType = "";
        if (type == 1)
            stringType = "BITMAP ";
        if (type == 3)
            stringType = "UNIQUE ";

        sb.append("CREATE ").append(stringType);
        sb.append("INDEX ").append(format(name));
        sb.append("\n\tON ");

        if (namePrefix != null)
            sb.append(namePrefix).append(".");

        sb.append(format(tableName)).append(" (");

        if (databaseColumns != null) {
            for (int i = 0, n = databaseColumns.size(); i < n; i++) {
                sb.append(databaseColumns.get(i).getName());
                if (i < n - 1)
                    sb.append(", ");
            }
        }

        if (databaseIndexColumns != null) {
            for (int i = 0, n = databaseIndexColumns.size(); i < n; i++) {
                sb.append(databaseIndexColumns.get(i).getFieldName());
                if (i < n - 1)
                    sb.append(", ");
            }
        }

        sb.append(")");

        if (type == 2)
            sb.append(" NOSORT");

        sb.append(";\n");

        return sb.toString();
    }

    public static String generateCreateDomain(ColumnData columnData, String name, boolean useDomainType) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE DOMAIN ").append(name).append(" AS ");
        if (useDomainType)
            sb.append(columnData.getFormattedDomainDataType());
        else sb.append(columnData.getFormattedDataType());
        if (!MiscUtils.isNull(columnData.getDefaultValue())) {
            sb.append("\n DEFAULT ").append(MiscUtils.formattedSQLValue(columnData.getDefaultValue(), columnData.getSQLType()));
        }
        sb.append(columnData.isRequired() ? " NOT NULL" : "");
        if (!MiscUtils.isNull(columnData.getCheck())) {
            sb.append("\n CHECK (").append(columnData.getCheck()).append(")");
        }
        if (columnData.getCollate() != null && !columnData.getCollate().trim().contentEquals("NONE"))
            sb.append(" COLLATE ").append(columnData.getCollate());
        sb.append(";\n");
        if (!MiscUtils.isNull(columnData.getDescription())) {
            sb.append("COMMENT ON DOMAIN ").append(columnData.getFormattedColumnName()).append(" IS '")
                    .append(columnData.getDescription()).append("';\n");
        }
        return sb.toString();
    }

    public static String generateCreateUser(DefaultDatabaseUser user) {

        StringBuilder sb = new StringBuilder();

        sb.append("CREATE");
        sb.append(" USER ").append(format(user.getName()));

        if (!MiscUtils.isNull(user.getFirstName()))
            sb.append("\nFIRSTNAME '").append(user.getFirstName()).append("'");
        if (!MiscUtils.isNull(user.getMiddleName()))
            sb.append("\nMIDDLENAME '").append(user.getMiddleName()).append("'");
        if (!MiscUtils.isNull(user.getLastName()))
            sb.append("\nLASTNAME '").append(user.getLastName()).append("'");
        if (!MiscUtils.isNull(user.getPassword()))
            sb.append("\nPASSWORD '").append(user.getPassword()).append("'");

        if (user.getActive())
            sb.append("\nACTIVE");
        else
            sb.append("\nINACTIVE");

        if (user.getAdministrator())
            sb.append("\nGRANT ADMIN ROLE");
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
        if (!MiscUtils.isNull(user.getComment()))
            sb.append("COMMENT ON USER ").append(format(user.getName())).append(" is '").append(user.getComment()).append("'");

        return sb.toString();
    }

    public static String generateCreateTablespace(String name, String file) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLESPACE ").append(format(name));
        sb.append(" FILE '").append(file).append("';\n");
        return sb.toString();
    }

    public static String generateCreateProcedure(
            String name, Vector<ColumnData> inputParameters, Vector<ColumnData> outputParameters,
            Vector<ColumnData> variables, String procedureBody, String comment) {

        StringBuilder sb = new StringBuilder();
        sb.append(formattedParameters(variables, true));
        sb.append(procedureBody);
        return generateCreateProcedure(name, inputParameters, outputParameters, sb.toString(), comment);
    }
    public static String generateCreateProcedure(
            String name, List<ProcedureParameter> parameters, String fullProcedureBody, String comment, DatabaseConnection dc) {

        Vector<ColumnData> inputs = new Vector<>();
        Vector<ColumnData> outputs = new Vector<>();

        for (ProcedureParameter parameter : parameters) {
            ColumnData cd = columnDataFromProcedureParameter(parameter, dc);
            if (parameter.getType() == DatabaseMetaData.procedureColumnIn)
                inputs.add(cd);
            else
                outputs.add(cd);
        }

        return generateCreateProcedure(name, inputs, outputs, fullProcedureBody, comment);
    }
    public static String generateCreateProcedure(
            String name, Vector<ColumnData> inputParameters, Vector<ColumnData> outputParameters, String fullProcedureBody, String comment) {

        StringBuilder sb = new StringBuilder();

        sb.append(generateCreateProcedureOrFunctionHeader(name, inputParameters, NamedObject.META_TYPES[PROCEDURE]));

        String output = formattedParameters(outputParameters, false);
        if (!MiscUtils.isNull(output.trim())) {
            sb.append("\nRETURNS (");
            sb.append(output);
            sb.append(")\n");
        }
        sb.append(generateSQLBody(fullProcedureBody));

        sb.append("\n");

        // add procedure description
        sb.append(generateComment(name, NamedObject.META_TYPES[PROCEDURE], comment, "^"));
        sb.append(generateCommentForColumns(name, inputParameters, "PARAMETER", "^"));
        sb.append(generateCommentForColumns(name, outputParameters, "PARAMETER", "^"));

        return sb.toString();
    }

    public static String generateCreateFunction(
            String name, Vector<ColumnData> argumentList, Vector<ColumnData> variables,
            ColumnData returnType, String functionBody, String entryPoint, String engine, String comment) {

        StringBuilder sb = new StringBuilder();
        sb.append(formattedParameters(variables, true));
        sb.append(functionBody);
        return generateCreateFunction(name, argumentList, returnType, sb.toString(), entryPoint, engine, comment);
    }
    public static String generateCreateFunction(
            String name, List<FunctionArgument> argumentList, String fullFunctionBody,
            String entryPoint, String engine, String comment, DatabaseConnection dc) {

        Vector<ColumnData> inputs = new Vector<>();
        ColumnData returnType = null;

        for (FunctionArgument parameter : argumentList) {
            if (parameter.getType() == DatabaseMetaData.procedureColumnIn) {
                ColumnData cd = columnDataFromProcedureParameter(parameter, dc);
                inputs.add(cd);
            } else
                returnType = columnDataFromProcedureParameter(parameter, dc);
        }

        return generateCreateFunction(name, inputs, returnType, fullFunctionBody, entryPoint, engine, comment);
    }
    public static String generateCreateFunction(
            String name, Vector<ColumnData> inputArguments, ColumnData returnType,
            String fullFunctionBody, String entryPoint, String engine, String comment) {

        StringBuilder sb = new StringBuilder();

        sb.append(generateCreateProcedureOrFunctionHeader(name, inputArguments, NamedObject.META_TYPES[FUNCTION]));
        sb.append("RETURNS ");

        if (returnType != null)
            sb.append(returnType.getFormattedDataType());
        if (entryPoint != null) {
            sb.append("EXTERNAL NAME '");
            sb.append(entryPoint).append("'");
            sb.append(" ENGINE ").append(engine);
        } else
            sb.append(generateSQLBody(fullFunctionBody));

        sb.append(generateComment(name, NamedObject.META_TYPES[FUNCTION], comment, "^"));
        sb.append(generateCommentForColumns(name, inputArguments, "PARAMETER", "^"));

        return sb.toString();
    }

    public static String generateDefinitionColumnConstraint(ColumnConstraint cc) {

        StringBuilder sb = new StringBuilder();

        String nameConstraint = null;
        boolean hasName;

        if (!MiscUtils.isNull(cc.getName())) {
            nameConstraint = cc.getName();
            hasName = true;

        } else
            hasName = false;

        if (hasName) {

            sb.append(COMMA).append(NEW_LINE_2).append(CreateTableSQLSyntax.CONSTRAINT);
            sb.append(format(nameConstraint)).append(SPACE);

            if (cc.getType() != -1) {

                if (cc.getType() == CHECK_KEY) {
                    sb.append(cc.getCheck());

                } else {

                    if (cc.getType() == UNIQUE_KEY) {

                        sb.append(ColumnConstraint.UNIQUE).append(SPACE).append(B_OPEN);

                        String formatted;
                        if (cc.getCountCols() > 1)
                            formatted = cc.getColumn();
                        else
                            formatted = format(cc.getColumn());

                        sb.append(formatted).append(B_CLOSE);

                    } else {

                        sb.append(cc.getTypeName()).append(KEY).append(B_OPEN);

                        String formatted;
                        if (cc.getCountCols() > 1)
                            formatted = cc.getColumn();
                        else
                            formatted = format(cc.getColumn());

                        sb.append(formatted).append(B_CLOSE);

                        if (cc.getType() == FOREIGN_KEY) {

                            sb.append(REFERENCES).append(format(cc.getRefTable())).append(SPACE).append(B_OPEN);

                            if (cc.getCountCols() > 1)
                                formatted = cc.getRefColumn();
                            else
                                formatted = format(cc.getRefColumn());

                            sb.append(formatted).append(B_CLOSE);

                            if (cc.getUpdateRule() != null && !Objects.equals(cc.getUpdateRule(), RULES[RESTRICT]))
                                sb.append(" ON UPDATE ").append(cc.getUpdateRule());
                            if (cc.getDeleteRule() != null && !Objects.equals(cc.getDeleteRule(), RULES[RESTRICT]))
                                sb.append(" ON DELETE ").append(cc.getDeleteRule());
                        }
                    }

                    if (!MiscUtils.isNull(cc.getTablespace()))
                        sb.append(" TABLESPACE ").append(format(cc.getTablespace()));
                }
            }
        }

        return sb.toString();
    }
    public static String generateDefinitionColumn(ColumnData cd) {

        StringBuilder sqlText = new StringBuilder();

        sqlText.append(NEW_LINE_2).append(
                        cd.getColumnName() == null ? CreateTableSQLSyntax.EMPTY : cd.getFormattedColumnName()).
                append(SPACE);

        if (MiscUtils.isNull(cd.getComputedBy())) {

            if (MiscUtils.isNull(cd.getDomain()) || cd.getDomain().startsWith("RDB$"))
                sqlText.append(cd.getFormattedDataType());
            else
                sqlText.append(cd.getFormattedDomain());

            if (cd.isAutoincrement() && cd.getAutoincrement().isIdentity()) {
                sqlText.append(" GENERATED BY DEFAULT AS IDENTITY");
                if (cd.getAutoincrement().getStartValue() != 0)
                    sqlText.append(" START WITH ").append(cd.getAutoincrement().getStartValue()).append(")");
            }

//            if (!MiscUtils.isNull(cd.getDefaultValue().getValue()))
//                sqlText.append(MiscUtils.formattedDefaultValue(cd.getDefaultValue(), cd.getSQLType()));

            sqlText.append(cd.isRequired() ? NOT_NULL : CreateTableSQLSyntax.EMPTY);
            if (!MiscUtils.isNull(cd.getCheck()))
                sqlText.append(" CHECK ( ").append(cd.getCheck()).append(")");

            if (cd.getCollate() != null && !cd.getCollate().equals(CreateTableSQLSyntax.NONE))
                sqlText.append(" COLLATE ").append(cd.getCollate());

        } else
            sqlText.append("COMPUTED BY ( ").append(cd.getComputedBy()).append(")");

        return sqlText.toString();
    }

    public static String generateCommentForColumns(
            String relationName, List<ColumnData> cols, String metaTag, String delimiter) {

        StringBuilder sb = new StringBuilder();

        for (ColumnData cd : cols) {
            String name = format(relationName) + "." + cd.getFormattedColumnName();
            sb.append(generateComment(name, metaTag, cd.getDescription(), delimiter));
        }

        return sb.toString();
    }

    public static String generateComment(
            String name, String metaTag, String comment, String delimiter) {

        StringBuilder sb = new StringBuilder();

        if (comment != null && !comment.isEmpty()) {
            sb.append("\nCOMMENT ON ").append(metaTag).append(" ");
            sb.append(format(name)).append(" IS ");

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
            String name, Vector<ColumnData> inputParameters, String metaTag) {

        StringBuilder sb = new StringBuilder();

        sb.append("CREATE OR ALTER ").append(metaTag).append(" ");
        sb.append(format(name));

        if (inputParameters != null && inputParameters.size() > 0 && (inputParameters.size() == 1 && !MiscUtils.isNull(inputParameters.get(0).getColumnName()) || inputParameters.size() > 1)) {
            sb.append(" (");
            sb.append(formattedParameters(inputParameters, false));
            sb.append(")\n");
        }

        return sb.toString();
    }

    public static String generateSQLBody(String sqlBody) {
        StringBuilder sb = new StringBuilder();
        sb.append("\nAS\n").append(sqlBody).append(";\n");
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

                } else
                    sb.append(formattedParameter(cd));

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
                if (MiscUtils.checkKeyword(cd.getDefaultValue()))
                    value = cd.getDefaultValue();
                if (cd.getDefaultValue().trim().toLowerCase().contentEquals("= null")
                        || cd.getDefaultValue().trim().toLowerCase().contentEquals("=null"))
                    value = cd.getDefaultValue();
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
        number = "" + (int_number + 1);
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
        number = "" + (int_number + 1);
        return name + number;
    }

    public static List<ColumnConstraint> removeDuplicatesConstraints(List<ColumnConstraint> columnConstraintList) {
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
                        cols = format(cols);
                    cols += "," + format(cc_rep.getColumn());
                    cc_origin.setColumn(cols);
                }
                if (cc_rep.getRefColumn() != null) {
                    String cols = cc_origin.getRefColumn();
                    if (cc_origin.getCountCols() == 1)
                        cols = format(cols);
                    cols += "," + format(cc_rep.getRefColumn());
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

    public static String generateDefaultDropRequest(String metaTag, String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("DROP ").append(metaTag).append(" ");
        sb.append(format(name)).append(";\n");
        return sb.toString();
    }

    public static String generateAlterDomain(ColumnData thisDomainData, ColumnData domainData) throws DataSourceException {

        StringBuilder sb = new StringBuilder();
        sb.append("ALTER DOMAIN ").append(thisDomainData.getFormattedColumnName()).append("\n");
        String noChangesCheckString = sb.toString();

        if (!thisDomainData.getColumnName().contentEquals(domainData.getColumnName()))
            sb.append("TO ").append(domainData.getFormattedColumnName()).append("\n");

        if (!MiscUtils.compareStrings(thisDomainData.getDefaultValue(), domainData.getDefaultValue())) {

            if (MiscUtils.isNull(domainData.getDefaultValue()))
                sb.append("DROP DEFAULT\n");

            else {
                sb.append("SET DEFAULT ");
                if (domainData.getDefaultValue().toUpperCase().trim().equals("NULL"))
                    sb.append("NULL");
                else
                    sb.append(MiscUtils.formattedSQLValue(domainData.getDefaultValue(), domainData.getSQLType()));
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
        if (!MiscUtils.compareStrings(thisDomainData.getCheck(), domainData.getCheck())) {

            sb.append("DROP CONSTRAINT\n");
            if (!MiscUtils.isNull(domainData.getCheck()))
                sb.append("ADD CHECK (").append(domainData.getCheck()).append(")\n");

        }

        if (!MiscUtils.compareStrings(thisDomainData.getDomainTypeName(), domainData.getDomainTypeName()))
            sb.append("TYPE ").append(domainData.getDomainTypeName());

        if (noChangesCheckString.equals(sb.toString()))
            sb = new StringBuilder();
        else
            sb.append(";\n");

        return sb.toString();
    }

    public static String generateAlterTable() {
        return null;
    }

    public static String generateAlterUDF(String name, String newEntryPoint, String newModuleName) {

        StringBuilder sb = new StringBuilder();
        sb.append("ALTER EXTERNAL FUNCTION ").append(format(name));
        String noChangesCheckString = sb.toString();

        if (newEntryPoint != null)
            sb.append("\nENTRY_POINT '").append(newEntryPoint).append("'");
        if (newModuleName != null)
            sb.append("\nMODULE_NAME '").append(newModuleName).append("'");

        if (noChangesCheckString.equals(sb.toString()))
            sb = new StringBuilder();
        else
            sb.append(";\n");

        return sb.toString();
    }

    public static String generateAlterIndex(String name, boolean active) {
        StringBuilder sb = new StringBuilder();
        String activeString = active ? " ACTIVE" : " INACTIVE";
        sb.append("ALTER INDEX ").append(format(name));
        sb.append(activeString).append(";\n");
        return sb.toString();
    }

    public static String generateAlterTablespace(String name, String file) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLESPACE ").append(format(name));
        sb.append(" SET FILE '").append(file).append("';\n");
        return sb.toString();
    }

    private static String format(String object) {
        return MiscUtils.getFormattedObject(object);
    }

}


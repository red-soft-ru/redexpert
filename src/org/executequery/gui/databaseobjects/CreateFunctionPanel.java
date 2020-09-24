package org.executequery.gui.databaseobjects;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.FunctionArgument;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseFunction;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.datatype.DomainPanel;
import org.executequery.gui.datatype.SelectTypePanel;
import org.executequery.gui.procedure.CreateProcedureFunctionPanel;
import org.underworldlabs.jdbc.DataSourceException;

import javax.swing.*;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

/**
 * Panel for creating and editing function
 *
 * @since rdb3
 */
public class CreateFunctionPanel extends CreateProcedureFunctionPanel {

    public static final String CREATE_TITLE = "Create Function";
    public static final String EDIT_TITLE = "Edit Function";

    private SelectTypePanel selectTypePanel;
    private DomainPanel domainPanel;
    private JTabbedPane returnTypeTabPane;
    private ColumnData returnType;
    private DefaultDatabaseFunction function;

    /**
     * <p> Constructs a new instance.
     *
     * @param dc
     * @param dialog
     * @param procedure
     */
    public CreateFunctionPanel(DatabaseConnection dc, ActionContainer dialog,
                               String procedure, DefaultDatabaseFunction databaseFunction) {
        super(dc, dialog, procedure, new Object[]{databaseFunction});
        parametersTabs.remove(outputParametersPanel);
        parametersTabs.setTitleAt(0, "Arguments");
        selectTypePanel = new SelectTypePanel(metaData.getDataTypesArray(),
                metaData.getIntDataTypesArray(), returnType, true);
        returnType.setDomain(returnType.getDomain());
        selectTypePanel.refresh();
        domainPanel = new DomainPanel(returnType, returnType.getDomain());
        returnTypeTabPane = new JTabbedPane();
        returnTypeTabPane.add("Domain", domainPanel);
        returnTypeTabPane.add("Type", selectTypePanel);
        tabbedPane.insertTab("Returns type", null, returnTypeTabPane, null, 1);
    }

    public CreateFunctionPanel(DatabaseConnection dc, ActionContainer dialog) {
        this(dc, dialog, null, null);
    }

    @Override
    protected String queryGetDescription() {
        return "SELECT RDB$DESCRIPTION FROM RDB$FUNCTIONS WHERE RDB$FUNCTION_NAME = '" + procedure + "'";
    }

    @Override
    protected String getFullSourceBody() {

        String res = "";
        String query = "SELECT RDB$FUNCTION_SOURCE FROM RDB$FUNCTIONS WHERE RDB$FUNCTION_NAME = '" + procedure + "'";
        ResultSet rs;
        try {
            rs = sender.getResultSet(query).getResultSet();
            if (rs.next())
                res = rs.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            sender.releaseResources();
        }
        return res;
    }

    public static String getQueryFunctionArguments(String function) {
        return "select cast(PP.RDB$FUNCTION_NAME as varchar(63)) as PROCEDURE_NAME,\n" +
                "cast(PP.RDB$ARGUMENT_NAME as varchar(63)) as COLUMN_NAME,\n" +
                "PP.RDB$FIELD_TYPE as COLUMN_TYPE,\n" +
                "F.RDB$FIELD_TYPE as FIELD_TYPE,\n" +
                "F.RDB$FIELD_SUB_TYPE as FIELD_SUB_TYPE,\n" +
                "F.RDB$FIELD_PRECISION as FIELD_PRECISION,\n" +
                "F.RDB$FIELD_SCALE as FIELD_SCALE,\n" +
                "F.RDB$FIELD_LENGTH as FIELD_LENGTH,\n" +
                "F.RDB$NULL_FLAG as NULL_FLAG,\n" +
                "PP.RDB$DESCRIPTION as REMARKS,\n" +
                "F.RDB$CHARACTER_LENGTH AS CHAR_LEN,\n" +
                "PP.RDB$ARGUMENT_POSITION AS PARAMETER_NUMBER,\n" +
                "F.RDB$CHARACTER_SET_ID\n" +
                "from RDB$FUNCTION_ARGUMENTS PP,RDB$FIELDS F\n" +
                "where PP.RDB$FUNCTION_NAME = '" + function + "' AND PP.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME\n" +
                " order by PP.RDB$FUNCTION_NAME, PP.RDB$FIELD_TYPE desc, PP.RDB$ARGUMENT_POSITION";
    }

    @Override
    protected void loadParameters() {
        {
            // remove first empty row
            try {
                List<FunctionArgument> parameters = function.getFunctionArguments();
                if (parameters.size() > 1)
                    inputParametersPanel.deleteEmptyRow();
                for (FunctionArgument pp :
                        parameters) {

                    ResultSet resultSet = sender.getResultSet("select\n" +
                            "f.rdb$field_sub_type as field_subtype,\n" +
                            "f.rdb$segment_length as segment_length,\n" +
                            "pp.rdb$field_source as field_source,\n" +
                            "pp.rdb$null_flag as null_flag,\n" +
                            "cs.rdb$character_set_name as character_set,\n" +
                            "pp.rdb$description as description,\n" +
                            "pp.rdb$argument_mechanism as mechanism,\n" +
                            "pp.rdb$field_name as field_name,\n" +
                            "pp.rdb$relation_name as relation_name \n" +
                            "from rdb$function_arguments pp,\n" +
                            "rdb$fields f\n" +
                            "left join rdb$character_sets cs on cs.rdb$character_set_id = f.rdb$character_set_id\n" +
                            "where pp.rdb$argument_name = '" + pp.getName() + "'\n" +
                            "and pp.rdb$function_name = '" + this.procedure + "'\n" +
                            "and  pp.rdb$field_source = f.rdb$field_name").getResultSet();
                    try {
                        if (resultSet.next()) {
                            pp.setSubType(resultSet.getInt(1));
                            /*int size = resultSet.getInt(2);
                            if (size != 0)
                                pp.setSize(size);*/
                            pp.setNullable(resultSet.getInt(4) == 1 ? 0 : 1);
                            String domain = resultSet.getString(3);
                            if (!domain.contains("RDB$"))
                                pp.setDomain(domain.trim());
                            String characterSet = resultSet.getString(5);
                            if (characterSet != null && !characterSet.isEmpty() && !characterSet.contains("NONE"))
                                pp.setEncoding(characterSet.trim());
                            pp.setDescription(resultSet.getString(6));
                            if (resultSet.getInt(7) == 1) {
                                pp.setTypeOf(true);
                                pp.setTypeOfFrom(ColumnData.TYPE_OF_FROM_DOMAIN);
                                String fieldName = resultSet.getString(8);
                                String relationName = resultSet.getString(9);
                                if (fieldName != null && !fieldName.isEmpty()
                                        && relationName != null && !relationName.isEmpty()) {
                                    pp.setFieldName(fieldName.trim());
                                    pp.setRelationName(relationName.trim());
                                    pp.setTypeOfFrom(ColumnData.TYPE_OF_FROM_COLUMN);
                                }
                            }
                        }
                    } finally {
                        releaseResources(resultSet);
                    }
                    if (pp.getType() == DatabaseMetaData.functionColumnIn)
                        inputParametersPanel.addRow(pp);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected String getEmptySqlBody() {
        return "begin\n" +
                "  /* Function Text */\n" +
                "end";
    }

    @Override
    protected void generateScript() {
        StringBuilder sb = new StringBuilder();
        sb.append("create or alter function ");
        sb.append(getFormattedName());
        sb.append(" (");
        sb.append(formattedParameters(inputParametersPanel.getProcedureParameterModel().getTableVector(), false));
        sb.append(")\n");
        sb.append("returns ");
        sb.append(returnType.getFormattedDataType());
        sb.append("\n");
        sb.append("as");
        sb.append(formattedParameters(variablesPanel.getProcedureParameterModel().getTableVector(), true));
        sb.append(sqlBodyText.getSQLText());
        sb.append("^\n");

        sb.append("\n");

        // add procedure description
        String text = descriptionArea.getTextAreaComponent().getText();
        if (text != null && !text.isEmpty()) {
            sb.append("\n");
            sb.append("COMMENT ON FUNCTION ");
            sb.append(getFormattedName());
            sb.append(" IS '");
            sb.append(text);
            sb.append("'");
            sb.append("^\n");
        }

        for (ColumnData cd :
                inputParametersPanel.getProcedureParameterModel().getTableVector()) {
            String cdText = cd.getDescription();
            if (cdText != null && !cdText.isEmpty()) {
                sb.append("\n");
                sb.append("COMMENT ON PARAMETER ");
                sb.append(getFormattedName()).append(".");
                sb.append(cd.getFormattedColumnName());
                sb.append(" IS '");
                sb.append(cdText);
                sb.append("'\n");
                sb.append("^\n");
            }
        }

        ddlTextPanel.setSQLText(sb.toString());
    }

    @Override
    public Vector<String> getColumnNamesVector(String tableName, String schemaName) {
        try {
            return metaData.getColumnNamesVector(tableName, schemaName);
        } catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(
                    "Error retrieving the column names for the " +
                            "selected table.\n\nThe system returned:\n" +
                            e.getExtendedMessage(), e);
            return new Vector<>(0);
        }
    }

    @Override
    protected void initEdited() {
        super.initEditing();
    }

    @Override
    public void createObject() {
        try {
            String querys = getSQLText();
            displayExecuteQueryDialog(querys, "^");

        } catch (Exception exc) {
            GUIUtilities.displayExceptionErrorDialog("Error:\n" + exc.getMessage(), exc);
        }
    }

    @Override
    public String getCreateTitle() {
        return CREATE_TITLE;
    }

    @Override
    public String getEditTitle() {
        return EDIT_TITLE;
    }

    @Override
    public String getTypeObject() {
        return NamedObject.META_TYPES[NamedObject.FUNCTION];
    }

    @Override
    public void setDatabaseObject(Object databaseObject) {

        procedure = (String) databaseObject;
        returnType = new ColumnData(connection);
        if (procedure != null) {
            String query = "SELECT RDB$FUNCTION_ARGUMENTS.RDB$FIELD_SOURCE,\n" +
                    "RDB$FUNCTION_ARGUMENTS.RDB$ARGUMENT_MECHANISM,\n" +
                    "RDB$FUNCTION_ARGUMENTS.RDB$RELATION_NAME,\n" +
                    "RDB$FUNCTION_ARGUMENTS.RDB$FIELD_NAME\n" +
                    "FROM RDB$FUNCTIONS LEFT JOIN RDB$FUNCTION_ARGUMENTS ON \n" +
                    "RDB$FUNCTIONS.RDB$FUNCTION_NAME = RDB$FUNCTION_ARGUMENTS.RDB$FUNCTION_NAME AND\n" +
                    "RDB$FUNCTIONS.RDB$RETURN_ARGUMENT = RDB$FUNCTION_ARGUMENTS.RDB$ARGUMENT_POSITION \n" +
                    "WHERE RDB$FUNCTIONS.RDB$FUNCTION_NAME = '" + procedure + "'";
            try {
                ResultSet rs = sender.getResultSet(query).getResultSet();
                String domain = null;
                String table = null;
                String column = null;
                if (rs.next()) {
                    domain = rs.getString(1).trim();
                    returnType.setTypeOf(rs.getInt(2) == 1);
                    table = rs.getString(3);
                    column = rs.getString(4);
                }
                sender.releaseResources();
                if (domain != null)
                    returnType.setDomain(domain.trim());
                if (table != null)
                    returnType.setTable(table.trim());
                if (column != null)
                    returnType.setColumnTable(column.trim());
                if (returnType.getTable() != null && returnType.getColumnTable() != null)
                    returnType.setTypeOfFrom(ColumnData.TYPE_OF_FROM_COLUMN);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setParameters(Object[] params) {
        function = (DefaultDatabaseFunction) params[0];
    }
}

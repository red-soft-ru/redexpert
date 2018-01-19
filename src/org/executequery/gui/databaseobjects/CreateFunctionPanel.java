package org.executequery.gui.databaseobjects;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.ProcedureParameter;
import org.executequery.databaseobjects.impl.DatabaseObjectFactoryImpl;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class CreateFunctionPanel extends CreateProcedureFunctionPanel {


    public static final String CREATE_TITLE = "Create Function";
    public static final String EDIT_TITLE = "Edit Function";

    private SelectTypePanel selectTypePanel;
    private DomainPanel domainPanel;
    private JTabbedPane returnTypeTabPane;
    private ColumnData returnType;


    /**
     * <p> Constructs a new instance.
     *
     * @param dc
     * @param dialog
     * @param procedure
     */
    public CreateFunctionPanel(DatabaseConnection dc, ActionContainer dialog, String procedure) {
        super(dc, dialog, procedure);
        parametersTabs.remove(outputParametersPanel);
        returnType = new ColumnData(connection);
        selectTypePanel = new SelectTypePanel(metaData.getDataTypesArray(), metaData.getIntDataTypesArray(), returnType, true);
        domainPanel = new DomainPanel(returnType, returnType.getDomain());
        returnTypeTabPane = new JTabbedPane();
        returnTypeTabPane.add("Domain", domainPanel);
        returnTypeTabPane.add("Type", selectTypePanel);
        tabbedPane.insertTab("Returns type", null, returnTypeTabPane, null, 1);
    }

    public CreateFunctionPanel(DatabaseConnection dc, ActionContainer dialog) {
        this(dc, dialog, null);
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

    @Override
    protected void loadParameters() {
        {
            inputParametersPanel.deleteEmptyRow(); // remove first empty row
            DatabaseHost host = null;
            try {
                host = new DatabaseObjectFactoryImpl().createDatabaseHost(connection);
                DatabaseMetaData dmd = host.getDatabaseMetaData();
                List<ProcedureParameter> parameters = new ArrayList<>();
                ResultSet rs = dmd.getFunctionColumns(null, null, this.procedure, null);

                while (rs.next()) {
                    ProcedureParameter procedureParameter = new ProcedureParameter(rs.getString(4),
                            rs.getInt(5),
                            rs.getInt(6),
                            rs.getString(7),
                            rs.getInt(8),
                            0/*rs.getInt(12)*/);
                    procedureParameter.setScale(rs.getInt(10));
                    parameters.add(procedureParameter);
                }

                releaseResources(rs);

                for (ProcedureParameter pp :
                        parameters) {

                    ResultSet resultSet = sender.getResultSet("select\n" +
                            "f.rdb$field_sub_type as field_subtype,\n" +
                            "f.rdb$segment_length as segment_length,\n" +
                            "pp.rdb$field_source as field_source,\n" +
                            "pp.rdb$null_flag as null_flag,\n" +
                            "cs.rdb$character_set_name as character_set,\n" +
                            "pp.rdb$description as description\n" +
                            "from rdb$function_arguments pp,\n" +
                            "rdb$fields f\n" +
                            "left join rdb$character_sets cs on cs.rdb$character_set_id = f.rdb$character_set_id\n" +
                            "where pp.rdb$argument_name = '" + pp.getName() + "'\n" +
                            "and pp.rdb$function_name = '" + this.procedure + "'\n" +
                            "and  pp.rdb$field_source = f.rdb$field_name").getResultSet();
                    try {
                        if (resultSet.next()) {
                            pp.setSubtype(resultSet.getInt(1));
                            int size = resultSet.getInt(2);
                            if (size != 0)
                                pp.setSize(size);
                            pp.setNullable(resultSet.getInt(4) == 1 ? 0 : 1);
                            String domain = resultSet.getString(3);
                            if (!domain.contains("RDB$"))
                                pp.setDomain(domain.trim());
                            String characterSet = resultSet.getString(5);
                            if (characterSet != null && !characterSet.isEmpty() && !characterSet.contains("NONE"))
                                pp.setEncoding(characterSet.trim());
                            pp.setDescription(resultSet.getString(6));
                        }
                    } finally {
                        releaseResources(resultSet);
                    }
                    if (pp.getType() == DatabaseMetaData.functionColumnIn)
                        inputParametersPanel.addRow(pp);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (host != null)
                    host.close();
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
        sb.append(nameField.getText());
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
            sb.append(nameField.getText());
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
                sb.append(nameField.getText()).append(".");
                sb.append(cd.getColumnName());
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
    protected void init_edited() {
        super.initEditing();
    }

    @Override
    public void create_object() {
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
            String query = "SELECT RDB$FUNCTION_ARGUMENTS.RDB$FIELD_SOURCE " +
                    "FROM RDB$FUNCTIONS LEFT JOIN RDB$FUNCTION_ARGUMENTS ON \n" +
                    "RDB$FUNCTIONS.RDB$RETURN_ARGUMENT = RDB$FUNCTION_ARGUMENTS.RDB$ARGUMENT_POSITION" +
                    " WHERE RDB$FUNCTIONS.RDB$FUNCTION_NAME = '" + procedure + "'";
            try {
                ResultSet rs = sender.getResultSet(query).getResultSet();
                String domain = null;
                if (rs.next())
                    domain = rs.getString(1).trim();
                sender.releaseResources();
                returnType.setDomain(domain);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setParameters(Object[] params) {

    }
}

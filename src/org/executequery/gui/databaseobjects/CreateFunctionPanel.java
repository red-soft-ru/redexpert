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
import org.executequery.localization.Bundles;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel for creating and editing function
 *
 * @since rdb3
 */
public class CreateFunctionPanel extends CreateProcedureFunctionPanel {

    public static final String CREATE_TITLE = getCreateTitle(NamedObject.FUNCTION);
    public static final String EDIT_TITLE = getEditTitle(NamedObject.FUNCTION);

    private final SelectTypePanel selectTypePanel;
    private final DomainPanel domainPanel;
    private final JTabbedPane returnTypeTabPane;
    private ColumnData returnType;
    private DefaultDatabaseFunction function;

    protected JCheckBox deterministicBox;

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
        parametersTabs.setTitleAt(0, bundledString("Arguments"));
        selectTypePanel = new SelectTypePanel(connection.getDataTypesArray(),
                connection.getIntDataTypesArray(), returnType, true);
        returnType.setDomain(returnType.getDomain());
        selectTypePanel.refresh();
        domainPanel = new DomainPanel(returnType, returnType.getDomain());
        returnTypeTabPane = new JTabbedPane();
        returnTypeTabPane.add(bundledString("Domain"), domainPanel);
        returnTypeTabPane.add(bundledString("Type"), selectTypePanel);
        tabbedPane.insertTab(bundledString("ReturnsType"), null, returnTypeTabPane, null, 1);
        firstQuery = generateQuery();
    }

    public CreateFunctionPanel(DatabaseConnection dc, ActionContainer dialog) {
        this(dc, dialog, null, null);
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
            // remove first empty row
            try {
                inputParametersPanel.clearRows();
                List<FunctionArgument> parameters = function.getFunctionArguments();
                if (parameters.size() > 1)
                    inputParametersPanel.deleteEmptyRow();
                for (FunctionArgument pp :
                        parameters) {
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

    protected String generateQuery() {
        return SQLUtils.generateCreateFunction(nameField.getText(), inputParametersPanel.getProcedureParameterModel().getTableVector(),
                variablesPanel.getProcedureParameterModel().getTableVector(), returnType, sqlBodyText.getSQLText(),
                externalField.getText(), engineField.getText(), (String) sqlSecurityCombo.getSelectedItem(),
                simpleCommentPanel.getComment(), false, true, deterministicBox.isSelected());
    }

    @Override
    protected void generateScript() {
        ddlTextPanel.setSQLText(generateQuery());
    }

    @Override
    protected void init() {
        super.init();
        deterministicBox = new JCheckBox(bundleStaticString("deterministic"));
        topPanel.add(deterministicBox, topGbh.setLabelDefault().get());
        topGbh.nextCol();
    }

    @Override
    protected void initEdited() {
        super.initEditing();
        if (function != null)
            deterministicBox.setSelected(function.isDeterministic());
    }

    @Override
    public void createObject() {
        try {
            String queries = getSQLText();
            displayExecuteQueryDialog(queries, "^");

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

    public String bundledString(String key) {
        return Bundles.get(CreateFunctionPanel.class, key);
    }

    @Override
    public void setParameters(Object[] params) {
        function = (DefaultDatabaseFunction) params[0];
    }
}

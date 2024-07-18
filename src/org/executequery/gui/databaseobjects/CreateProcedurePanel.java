package org.executequery.gui.databaseobjects;

import biz.redsoft.IFBDatabaseMetadata;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.ProcedureParameter;
import org.executequery.databaseobjects.impl.DatabaseObjectFactoryImpl;
import org.executequery.databaseobjects.impl.DefaultDatabaseProcedure;
import org.executequery.datasource.DefaultDriverLoader;
import org.executequery.datasource.PooledDatabaseMetaData;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.procedure.CreateProcedureFunctionPanel;
import org.executequery.log.Log;
import org.underworldlabs.util.DynamicLibraryLoader;
import org.underworldlabs.util.SQLUtils;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

/**
 * @author Vasiliy Yashkov
 */
public class CreateProcedurePanel extends CreateProcedureFunctionPanel {

    public static final String TITLE = getCreateTitle(NamedObject.PROCEDURE);
    public static final String EDIT_TITLE = getEditTitle(NamedObject.PROCEDURE);

    public CreateProcedurePanel(DatabaseConnection dc, ActionContainer parent) {
        this(dc, parent, null);
    }

    public CreateProcedurePanel(DatabaseConnection connection, ActionContainer parent, String procedure) {
        super(connection, parent, procedure);
        setFocusComponent();
    }

    @Override
    protected void initEdited() {
        super.initEditing();
    }

    @Override
    protected String getFullSourceBody() {

        DatabaseHost host = null;
        String fullProcedureBody = null;

        try {
            DatabaseConnection connection = (DatabaseConnection) connectionsCombo.getSelectedItem();
            host = new DatabaseObjectFactoryImpl().createDatabaseHost(connection);
            DatabaseMetaData databaseMetadata = ((PooledDatabaseMetaData) host.getDatabaseMetaData()).getInner();

            IFBDatabaseMetadata db = (IFBDatabaseMetadata) DynamicLibraryLoader.loadingObjectFromClassLoader(
                    connection != null ? connection.getDriverMajorVersion() : DefaultDriverLoader.getDefaultDriver().getMajorVersion(),
                    databaseMetadata,
                    "FBDatabaseMetadataImpl"
            );

            fullProcedureBody = db.getProcedureSourceCode(databaseMetadata, this.procedureName);

        } catch (ClassNotFoundException | SQLException e) {
            Log.error(e.getMessage(), e);

        } finally {
            if (host != null)
                host.close();
        }

        return fullProcedureBody;
    }

    @Override
    protected void loadParameters() {
        inputParamsPanel.clearRows();// remove first empty row
        outputParamsPanel.clearRows(); // remove first empty row

        List<ProcedureParameter> parameters = ((DefaultDatabaseProcedure) ConnectionsTreePanel
                .getNamedObjectFromHost(connection, NamedObject.PROCEDURE, procedureName))
                .getParameters();

        for (ProcedureParameter pp : parameters) {
            if (pp.getType() == DatabaseMetaData.procedureColumnIn)
                inputParamsPanel.addRow(pp);
            else if (pp.getType() == DatabaseMetaData.procedureColumnOut)
                outputParamsPanel.addRow(pp);
        }
    }

    @Override
    protected String getEmptySqlBody() {
        return "BEGIN\n\t/* Procedure impl */\nEND";
    }

    protected String generateQuery() {

        if (isParseVariables()) {
            Vector<ColumnData> vars = new Vector<>();
            vars.addAll(variablesPanel.getProcedureParameterModel().getTableVector());
            vars.addAll(cursorsPanel.getCursorsVector());

            return SQLUtils.generateCreateProcedure(
                    nameField.getText(),
                    externalField.getText(),
                    engineField.getText(),
                    inputParamsPanel.getProcedureParameterModel().getTableVector(),
                    outputParamsPanel.getProcedureParameterModel().getTableVector(),
                    vars,
                    (String) securityCombo.getSelectedItem(),
                    (String) authidCombo.getSelectedItem(),
                    procedureBody,
                    simpleCommentPanel.getComment(),
                    false,
                    true,
                    getDatabaseConnection()
            );
        }

        return SQLUtils.generateCreateProcedure(
                nameField.getText(),
                externalField.getText(),
                engineField.getText(),
                inputParamsPanel.getProcedureParameterModel().getTableVector(),
                outputParamsPanel.getProcedureParameterModel().getTableVector(),
                (String) securityCombo.getSelectedItem(),
                (String) authidCombo.getSelectedItem(),
                procedureBody,
                simpleCommentPanel.getComment(),
                false,
                true,
                getDatabaseConnection()
        );
    }

    /**
     * Indicates that a [long-running] process has begun or ended
     * as specified. This may trigger the glass pane on or off
     * or set the cursor appropriately.
     *
     * @param inProcess - true | false
     */
    @Override
    public void setInProcess(boolean inProcess) {

        if (parent == null)
            return;

        if (inProcess)
            parent.block();
        else
            parent.unblock();
    }

    @Override
    public void createObject() {
        try {
            displayExecuteQueryDialog(getSQLText(), "^");

        } catch (Exception exc) {
            GUIUtilities.displayExceptionErrorDialog("Error:\n" + exc.getMessage(), exc, this.getClass());
        }
    }

    @Override
    public String getCreateTitle() {
        return TITLE;
    }

    @Override
    public String getEditTitle() {
        return EDIT_TITLE;
    }

    @Override
    public String getTypeObject() {
        return NamedObject.META_TYPES[NamedObject.PROCEDURE];
    }

    @Override
    public void setDatabaseObject(Object databaseObject) {
        procedureName = (String) databaseObject;
    }

    @Override
    public void setParameters(Object[] params) {
    }

}

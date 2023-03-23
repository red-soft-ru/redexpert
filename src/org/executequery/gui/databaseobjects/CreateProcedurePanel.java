package org.executequery.gui.databaseobjects;

import biz.redsoft.IFBDatabaseMetadata;
import org.executequery.ActiveComponent;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.components.BottomButtonPanel;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.ProcedureParameter;
import org.executequery.databaseobjects.impl.DatabaseObjectFactoryImpl;
import org.executequery.databaseobjects.impl.DefaultDatabaseProcedure;
import org.executequery.datasource.PooledDatabaseMetaData;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.DefaultKeywordEvent;
import org.executequery.event.KeywordEvent;
import org.executequery.event.KeywordListener;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.procedure.CreateProcedureFunctionPanel;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

/**
 * @author Vasiliy Yashkov
 */
public class CreateProcedurePanel extends CreateProcedureFunctionPanel
        implements ActionListener,
        KeywordListener,
        ActiveComponent {

    /**
     * This objects title as an internal frame
     */
    public static final String TITLE = getCreateTitle(NamedObject.PROCEDURE);

    public static final String EDIT_TITLE = getEditTitle(NamedObject.PROCEDURE);

    /**
     * This objects icon as an internal frame
     */
    public static final String FRAME_ICON = "NewProcedure16.png";


    /**
     * <p> Constructs a new instance.
     */

    public CreateProcedurePanel(DatabaseConnection dc, ActionContainer parent) {
        this(dc, parent, null);

    }

    public CreateProcedurePanel(DatabaseConnection connection, ActionContainer parent, String procedure) {
        super(connection, parent, procedure);
        setFocusComponent();
    }

    @Override
    protected String getFullSourceBody() {
        DatabaseHost host = null;
        String fullProcedureBody = null;
        try {
            DatabaseConnection connection =
                    (DatabaseConnection) connectionsCombo.getSelectedItem();
            host = new DatabaseObjectFactoryImpl().createDatabaseHost(connection);
            DatabaseMetaData dmd = host.getDatabaseMetaData();
            PooledDatabaseMetaData poolMetaData = (PooledDatabaseMetaData) dmd;
            DatabaseMetaData dMetaData = poolMetaData.getInner();
            URL[] urls;
            Class clazzdb;
            Object odb;
            urls = MiscUtils.loadURLs("./lib/fbplugin-impl.jar;../lib/fbplugin-impl.jar");
            ClassLoader cl = new URLClassLoader(urls, dMetaData.getClass().getClassLoader());
            clazzdb = cl.loadClass("biz.redsoft.FBDatabaseMetadataImpl");
            odb = clazzdb.newInstance();
            IFBDatabaseMetadata db = (IFBDatabaseMetadata) odb;

            fullProcedureBody = db.getProcedureSourceCode(dMetaData, this.procedure);


        } catch (IllegalAccessException | InstantiationException | MalformedURLException | ClassNotFoundException |
                 SQLException e) {
            e.printStackTrace();
        } finally {
            if (host != null)
                host.close();
        }

        return fullProcedureBody;
    }

    @Override
    protected void loadParameters() {
        inputParametersPanel.clearRows();// remove first empty row
        outputParametersPanel.clearRows(); // remove first empty row
        List<ProcedureParameter> parameters = ((DefaultDatabaseProcedure) ConnectionsTreePanel.getNamedObjectFromHost(connection, NamedObject.PROCEDURE, procedure)).getParameters();
        for (ProcedureParameter pp : parameters) {
            if (pp.getType() == DatabaseMetaData.procedureColumnIn)
                inputParametersPanel.addRow(pp);
            else if (pp.getType() == DatabaseMetaData.procedureColumnOut)
                outputParametersPanel.addRow(pp);
        }

    }

    @Override
    protected String getEmptySqlBody() {
        return "BEGIN\n" +
                "  /* PROCEDURE TEXT */\n" +
                "  SUSPEND;\n" +
                "END";
    }

    @Override
    protected void generateScript() {
        ddlTextPanel.setSQLText(generateQuery());

    }

    protected String generateQuery() {
        Vector<ColumnData> vars = new Vector<>();
        vars.addAll(variablesPanel.getProcedureParameterModel().getTableVector());
        vars.addAll(cursorsPanel.getProcedureParameterModel().getTableVector());
        return SQLUtils.generateCreateProcedure(
                nameField.getText(), externalField.getText(), engineField.getText(),
                inputParametersPanel.getProcedureParameterModel().getTableVector(),
                outputParametersPanel.getProcedureParameterModel().getTableVector(),
                vars, (String) sqlSecurityCombo.getSelectedItem(), (String) authidCombo.getSelectedItem(),
                sqlBodyText.getSQLText(), simpleCommentPanel.getComment(), false, true);
    }


    /**
     * <p>Initializes the state of this instance.
     */
    private void jbInit() {

        addButtonsPanel(new BottomButtonPanel(
                this, "Create", "create-procedure", parent.isDialog()));
        setPreferredSize(new Dimension(750, 820));
        EventMediator.registerListener(this);
    }

    private void alterInit() {

        addButtonsPanel(new BottomButtonPanel(
                this, "Alter", "create-procedure", parent.isDialog()));
        setPreferredSize(new Dimension(750, 820));
        EventMediator.registerListener(this);
    }

    /**
     * Indicates that a [long-running] process has begun or ended
     * as specified. This may trigger the glass pane on or off
     * or set the cursor appropriately.
     *
     * @param inProcess - true | false
     */
    public void setInProcess(boolean inProcess) {
        if (parent != null) {

            if (inProcess) {

                parent.block();

            } else {

                parent.unblock();
            }
        }
    }

    /**
     * Notification of a new keyword added to the list.
     */
    public void keywordsAdded(KeywordEvent e) {
        outSqlText.setSQLKeywords(true);
    }

    public boolean canHandleEvent(ApplicationEvent event) {
        return (event instanceof DefaultKeywordEvent);
    }

    /**
     * Notification of a keyword removed from the list.
     */
    public void keywordsRemoved(KeywordEvent e) {
        outSqlText.setSQLKeywords(true);
    }


    /**
     * Releases database resources before closing.
     */
    public void cleanup() {
        EventMediator.deregisterListener(this);
    }

    /**
     * Action listener implementation.<br>
     * Executes the create table script.
     *
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {

        if (connection == null) {
            GUIUtilities.displayErrorMessage(
                    "No database connection is available.");
            return;
        }
        createProcedure();
    }

    private void createProcedure() {
        try {
            String queries = getSQLText();
            displayExecuteQueryDialog(queries, "^");

        } catch (Exception exc) {
            GUIUtilities.displayExceptionErrorDialog("Error:\n" + exc.getMessage(), exc);
        }

    }

    public String toString() {
        return TITLE;
    }

    @Override
    protected void initEdited() {
        super.initEditing();
    }

    @Override
    public void createObject() {
        createProcedure();
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
        procedure = (String) databaseObject;
    }

    @Override
    public void setParameters(Object[] params) {

    }
}

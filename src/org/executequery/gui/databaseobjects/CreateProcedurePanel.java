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
import org.executequery.datasource.PooledDatabaseMetaData;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.DefaultKeywordEvent;
import org.executequery.event.KeywordEvent;
import org.executequery.event.KeywordListener;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.browser.ColumnData;
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
import java.util.ArrayList;
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
    public static final String TITLE = "Create Procedure";

    public static final String EDIT_TITLE = "Edit Procedure";

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
    public String queryGetDescription() {
        return "select\n" +
                "p.rdb$description \n" +
                "from rdb$procedures p\n" +
                "where p.rdb$procedure_name = '" +
                this.procedure +
                "'";
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
            List<ProcedureParameter> parameters = new ArrayList<>();


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


        } catch (IllegalAccessException | InstantiationException | MalformedURLException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            if (host != null)
                host.close();
        }

        return fullProcedureBody;
    }

    @Override
    protected void loadParameters() {
        {
            inputParametersPanel.deleteEmptyRow(); // remove first empty row
            outputParametersPanel.deleteEmptyRow(); // remove first empty row
            DatabaseHost host = null;
            try {
                host = new DatabaseObjectFactoryImpl().createDatabaseHost(connection);
                DatabaseMetaData dmd = host.getDatabaseMetaData();
                List<ProcedureParameter> parameters = new ArrayList<>();

                ResultSet rs = dmd.getProcedureColumns(null, null, this.procedure, null);

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
                            "pp.rdb$description as description,\n" +
                            "pp.rdb$parameter_mechanism as mechanism,\n" +
                            "pp.rdb$field_name as field_name,\n" +
                            "pp.rdb$relation_name as relation_name,\n" +
                            "pp.rdb$default_source as default_source\n" +
                            "from rdb$procedure_parameters pp,\n" +
                            "rdb$fields f\n" +
                            "left join rdb$character_sets cs on cs.rdb$character_set_id = f.rdb$character_set_id\n" +
                            "where pp.rdb$parameter_name = '" + pp.getName() + "'\n" +
                            "and pp.rdb$procedure_name = '" + this.procedure + "'\n" +
                            "and  pp.rdb$field_source = f.rdb$field_name").getResultSet();
                    try {
                        if (resultSet.next()) {
                            pp.setSubType(resultSet.getInt(1));
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
                            pp.setDefaultValue(resultSet.getString("default_source"));

                        }
                    } finally {
                        sender.releaseResources();
                    }
                    if (pp.getType() == DatabaseMetaData.procedureColumnIn)
                        inputParametersPanel.addRow(pp);
                    else if (pp.getType() == DatabaseMetaData.procedureColumnOut)
                        outputParametersPanel.addRow(pp);
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
                "  /* Procedure Text */\n" +
                "  suspend;\n" +
                "end";
    }

    @Override
    protected void generateScript() {
            ddlTextPanel.setSQLText(SQLUtils.generateCreateProcedure(nameField.getText(),inputParametersPanel.getProcedureParameterModel().getTableVector(),outputParametersPanel.getProcedureParameterModel().getTableVector(),variablesPanel.getProcedureParameterModel().getTableVector(),sqlBodyText.getSQLText(),descriptionArea.getTextAreaComponent().getText()));

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

    /**
     * Releases database resources before closing.
     */
    public void cleanup() {
        EventMediator.deregisterListener(this);
        metaData.closeConnection();
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
            String querys = getSQLText();
            displayExecuteQueryDialog(querys, "^");

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

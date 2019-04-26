/*
 * BrowserController.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.gui.browser;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.MetaDataValues;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.databaseobjects.*;
import org.executequery.databaseobjects.impl.*;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.gui.browser.nodes.RootDatabaseObjectNode;
import org.executequery.gui.forms.FormObjectView;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Performs SQL execution tasks from browser components.
 *
 * @author Takis Diakoumis
 */
public class BrowserController {

    public static final int UPDATE_CANCELLED = 99;

    /**
     * the meta data retrieval object
     */
    private final MetaDataValues metaData;

    /**
     * query sender object
     */
    private StatementExecutor querySender;

    /**
     * the connections tree panel
     */
    private ConnectionsTreePanel treePanel;

    /**
     * the databse viewer panel
     */
    private BrowserViewPanel viewPanel;

    /**
     * Creates a new instance of BorwserQueryExecuter
     */
    public BrowserController(ConnectionsTreePanel treePanel) {
        this.treePanel = treePanel;
        viewPanel = new BrowserViewPanel(this);
        metaData = new MetaDataValues(true);
    }

    /**
     * Connects the specified connection.
     *
     * @param dc - the conn to connect
     */
    protected void connect(DatabaseConnection dc) {
        try {

            ((DatabaseHost) treePanel.getHostNode(dc).getDatabaseObject()).connect();

        } catch (DataSourceException e) {

            GUIUtilities.displayExceptionErrorDialog(Bundles.getCommon("error.connection") + e.getExtendedMessage(), e);
        }
    }

    /**
     * Disconnects the specified connection.
     *
     * @param dc - the conn to disconnect
     */
    protected void disconnect(DatabaseConnection dc) {
        try {
            ((DatabaseHost) treePanel.
                    getHostNode(dc).getDatabaseObject()).disconnect();
        } catch (DataSourceException e) {
            Log.warning("Error on disconnection: " + e.getMessage());
            if (Log.isDebugEnabled()) {
                Log.error("Disconnection error: " + e);
            }
        }
    }

    /**
     * Performs the drop database object action.
     */
    protected void dropSelectedObject() {
        //try {
        // make sure we are not on a type parent object
        if (treePanel.isTypeParentSelected()) {
            return;
        }

        treePanel.removeTreeNode();
            /*
            NamedObject object = treePanel.getSelectedNamedObject();
            if (object == null) {
                return;
            }

            // display confirmation dialog
            int yesNo = GUIUtilities.displayConfirmDialog(
                               "Are you sure you want to drop " + object + "?");
            if (yesNo == JOptionPane.NO_OPTION) {
                return;
            }

            int result = dropObject(getDatabaseConnection(), object);
            if (result >= 0) {
                treePanel.removeSelectedNode();
            }
        }
        catch (SQLException e) {
            StringBuffer sb = new StringBuffer();
            sb.append("An error occurred removing the selected object.").
               append("\n\nThe system returned:\n").
               append(MiscUtils.formatSQLError(e));
            GUIUtilities.displayExceptionErrorDialog(sb.toString(), e);
        }        */
    }

    /**
     * Ensures we have a browser panel and that it is visible.
     */
    protected void checkBrowserPanel() {

        // check we have the browser view panel
//        if (viewPanel == null) {
//
//            viewPanel = new BrowserViewPanel(this);
//        }

        // check the panel is in the pane
        if (viewPanel == null)
            viewPanel = new BrowserViewPanel(this);
        String title = viewPanel.getNameObject();
        if (title == null)
            title = BrowserViewPanel.TITLE;
        JPanel _viewPanel = GUIUtilities.getCentralPane(title);

        if (_viewPanel == null) {

            GUIUtilities.addCentralPane(title,
                    BrowserViewPanel.FRAME_ICON,
                    viewPanel,
                    title,
                    true);
            ConnectionHistory.add(viewPanel.getCurrentView());

        } else {

            GUIUtilities.setSelectedCentralPane(title);
        }

    }

    /**
     * Informs the view panel of a pending change.
     */
    protected void selectionChanging() {

        if (viewPanel != null) {

            viewPanel.selectionChanging();
        }

    }

    /**
     * Sets the selected connection tree node to the
     * specified database connection.
     *
     * @param dc - the database connection to select
     */
    protected void setSelectedConnection(DatabaseConnection dc) {

        treePanel.setSelectedConnection(dc);
    }

    /**
     * Reloads the database properties meta data table panel.
     */
    protected void updateDatabaseProperties() {

        FormObjectView view = viewPanel.getFormObjectView(HostPanel.NAME);
        if (view != null) {

            HostPanel panel = (HostPanel) view;
            panel.updateDatabaseProperties();
        }

    }

    /**
     * Adds a new connection.
     */
    protected void addNewConnection() {

        treePanel.newConnection();
    }

    /**
     * Indicates that a node name has changed and fires a call
     * to repaint the tree display.
     */
    protected void nodeNameValueChanged(DatabaseHost host) {

        treePanel.nodeNameValueChanged(host);
    }

    /**
     * Indicates a change in the tree selection value.<br>
     * This will determine and builds the object view panel to be
     * displayed based on the specified host node connection object
     * and the selected node as specified.
     * <p>
     * //@param the connection host parent object
     * //@param the selected node
     */
    public synchronized void valueChanged_(DatabaseObjectNode node, DatabaseConnection connection) {

        treePanel.setInProcess(true);

        try {

            FormObjectView panel = buildPanelView(node);
            panel.setDatabaseObjectNode(node);
            String type = "";
            if (node.getType() < NamedObject.META_TYPES.length)
                type = NamedObject.META_TYPES[node.getType()];
            if (connection == null)
                connection = getDatabaseConnection();
            if (node.isHostNode() || node.getType() == NamedObject.CATALOG)
                panel.setObjectName(null);
            else panel.setObjectName(node.getShortName().trim() + ":" + type + ":" + connection.getName());
            panel.setDatabaseConnection(connection);
            if (panel != null) {

                viewPanel.setView(panel);
                checkBrowserPanel();
            }

        } finally {

            treePanel.setInProcess(false);
        }

    }

    /**
     * Determines and builds the object view panel to be
     * displayed based on the specified host node connection object
     * and the selected node as specified.
     *
     * @param //the selected node
     * @param// the connection host parent object
     */
    private FormObjectView buildPanelView(DatabaseObjectNode node) {
        try {

            NamedObject databaseObject = node.getDatabaseObject();
            if (databaseObject == null) {

                return null;
            }

//            System.out.println("selected object type: " + databaseObject.getClass().getName());
            viewPanel = new BrowserViewPanel(this);
            int type = node.getType();
            switch (type) {
                case NamedObject.HOST:
                    viewPanel = (BrowserViewPanel) GUIUtilities.getCentralPane(BrowserViewPanel.TITLE);
                    if (viewPanel == null)
                        viewPanel = new BrowserViewPanel(this);
                    HostPanel hostPanel = hostPanel();
                    hostPanel.setValues((DatabaseHost) databaseObject);

                    return hostPanel;

                // catalog node:
                // this will display the schema table list
                case NamedObject.CATALOG:
                    viewPanel = (BrowserViewPanel) GUIUtilities.getCentralPane(BrowserViewPanel.TITLE);
                    CatalogPanel catalogPanel = null;
                    if (!viewPanel.containsPanel(CatalogPanel.NAME)) {
                        catalogPanel = new CatalogPanel(this);
                        viewPanel.addToLayout(catalogPanel);
                    } else {
                        catalogPanel = (CatalogPanel) viewPanel.
                                getFormObjectView(CatalogPanel.NAME);
                    }

                    catalogPanel.setValues((DatabaseCatalog) databaseObject);
                    return catalogPanel;

                case NamedObject.SCHEMA:
                    viewPanel = (BrowserViewPanel) GUIUtilities.getCentralPane(BrowserViewPanel.TITLE);
                    SchemaPanel schemaPanel = null;
                    if (!viewPanel.containsPanel(SchemaPanel.NAME)) {
                        schemaPanel = new SchemaPanel(this);
                        viewPanel.addToLayout(schemaPanel);
                    } else {
                        schemaPanel = (SchemaPanel) viewPanel.
                                getFormObjectView(SchemaPanel.NAME);
                    }

                    schemaPanel.setValues((DatabaseSchema) databaseObject);
                    return schemaPanel;

                case NamedObject.META_TAG:
                case NamedObject.SYSTEM_STRING_FUNCTIONS:
                case NamedObject.SYSTEM_NUMERIC_FUNCTIONS:
                case NamedObject.SYSTEM_DATE_TIME_FUNCTIONS:
                    MetaKeyPanel metaKeyPanel = null;
                    if (!viewPanel.containsPanel(MetaKeyPanel.NAME)) {
                        metaKeyPanel = new MetaKeyPanel(this);
                        viewPanel.addToLayout(metaKeyPanel);
                    } else {
                        metaKeyPanel = (MetaKeyPanel) viewPanel.
                                getFormObjectView(MetaKeyPanel.NAME);
                    }

                    metaKeyPanel.setValues(databaseObject);
                    return metaKeyPanel;

                case NamedObject.FUNCTION: // Internal function of Red Database 3+
                    BrowserFunctionPanel functionPanel = null;
                    if (!viewPanel.containsPanel(BrowserFunctionPanel.NAME)) {
                        functionPanel = new BrowserFunctionPanel(this);
                        viewPanel.addToLayout(functionPanel);
                    } else {
                        functionPanel = (BrowserFunctionPanel) viewPanel.
                                getFormObjectView(BrowserFunctionPanel.NAME);
                    }

                    functionPanel.setValues((DefaultDatabaseFunction) databaseObject);
                    return functionPanel;
                case NamedObject.PROCEDURE:
                case NamedObject.SYSTEM_FUNCTION:
                    BrowserProcedurePanel procsPanel = null;
                    if (!viewPanel.containsPanel(BrowserProcedurePanel.NAME)) {
                        procsPanel = new BrowserProcedurePanel(this);
                        viewPanel.addToLayout(procsPanel);
                    } else {
                        procsPanel = (BrowserProcedurePanel) viewPanel.
                                getFormObjectView(BrowserProcedurePanel.NAME);
                    }

                    procsPanel.setValues((DatabaseExecutable) databaseObject);
                    return procsPanel;

                case NamedObject.TRIGGER:
                case NamedObject.SYSTEM_TRIGGER:
                case NamedObject.SYSTEM_DATABASE_TRIGGER:
                    BrowserTriggerPanel triggerPanel = null;
                    if (!viewPanel.containsPanel(BrowserTriggerPanel.NAME)) {
                        triggerPanel = new BrowserTriggerPanel(this);
                        viewPanel.addToLayout(triggerPanel);
                    } else {
                        triggerPanel = (BrowserTriggerPanel) viewPanel.
                                getFormObjectView(BrowserTriggerPanel.NAME);
                    }

                    triggerPanel.setValues((DefaultDatabaseTrigger) databaseObject);
                    return triggerPanel;

                case NamedObject.PACKAGE:
                    BrowserPackagePanel packagePanel = null;
                    if (!viewPanel.containsPanel(BrowserPackagePanel.NAME)) {
                        packagePanel = new BrowserPackagePanel(this);
                        viewPanel.addToLayout(packagePanel);
                    } else {
                        packagePanel = (BrowserPackagePanel) viewPanel.
                                getFormObjectView(BrowserPackagePanel.NAME);
                    }

                    packagePanel.setValues((DefaultDatabasePackage) databaseObject);
                    return packagePanel;

                case NamedObject.SEQUENCE:
                    BrowserSequencePanel sequencePanel = null;
                    if (!viewPanel.containsPanel(BrowserSequencePanel.NAME)) {
                        sequencePanel = new BrowserSequencePanel(this);
                        viewPanel.addToLayout(sequencePanel);
                    } else {
                        sequencePanel = (BrowserSequencePanel) viewPanel.
                                getFormObjectView(BrowserSequencePanel.NAME);
                    }

                    sequencePanel.setValues((DefaultDatabaseSequence) databaseObject);
                    return sequencePanel;

                case NamedObject.DOMAIN:
                case NamedObject.SYSTEM_DOMAIN:
                    BrowserDomainPanel domainPanel = null;
                    if (!viewPanel.containsPanel(BrowserDomainPanel.NAME)) {
                        domainPanel = new BrowserDomainPanel(this);
                        viewPanel.addToLayout(domainPanel);
                    } else {
                        domainPanel = (BrowserDomainPanel) viewPanel.
                                getFormObjectView(BrowserDomainPanel.NAME);
                    }

                    domainPanel.setValues((DefaultDatabaseDomain) databaseObject);
                    return domainPanel;
                case NamedObject.ROLE:
                    BrowserRolePanel rolePanel = null;
                    if (!viewPanel.containsPanel(BrowserRolePanel.NAME)) {
                        rolePanel = new BrowserRolePanel(this);
                        viewPanel.addToLayout(rolePanel);
                    } else {
                        rolePanel = (BrowserRolePanel) viewPanel.
                                getFormObjectView(BrowserRolePanel.NAME);
                    }
                    rolePanel.setValues((DefaultDatabaseRole) databaseObject, this);
                    return rolePanel;
                case NamedObject.EXCEPTION:
                    BrowserExceptionPanel exceptionPanel = null;
                    if (!viewPanel.containsPanel(BrowserExceptionPanel.NAME)) {
                        exceptionPanel = new BrowserExceptionPanel(this);
                        viewPanel.addToLayout(exceptionPanel);
                    } else {
                        exceptionPanel = (BrowserExceptionPanel) viewPanel.
                                getFormObjectView(BrowserExceptionPanel.NAME);
                    }

                    exceptionPanel.setValues((DefaultDatabaseException) databaseObject);
                    return exceptionPanel;

                case NamedObject.UDF:
                    BrowserUDFPanel browserUDFPanel = null;
                    if (!viewPanel.containsPanel(BrowserUDFPanel.NAME)) {
                        browserUDFPanel = new BrowserUDFPanel(this);
                        viewPanel.addToLayout(browserUDFPanel);
                    } else {
                        browserUDFPanel = (BrowserUDFPanel) viewPanel.
                                getFormObjectView(BrowserUDFPanel.NAME);
                    }

                    browserUDFPanel.setValues((DefaultDatabaseUDF) databaseObject);
                    return browserUDFPanel;

                case NamedObject.INDEX:
                case NamedObject.SYSTEM_INDEX:
                    BrowserIndexPanel browserIndexPanel = null;
                    if (!viewPanel.containsPanel(BrowserIndexPanel.NAME)) {
                        browserIndexPanel = new BrowserIndexPanel(this);
                        viewPanel.addToLayout(browserIndexPanel);
                    } else {
                        browserIndexPanel = (BrowserIndexPanel) viewPanel.
                                getFormObjectView(BrowserIndexPanel.NAME);
                    }

                    browserIndexPanel.setValues((DefaultDatabaseIndex) databaseObject);
                    return browserIndexPanel;

                case NamedObject.TABLE:
                case NamedObject.GLOBAL_TEMPORARY:
                    BrowserTableEditingPanel editingPanel = viewPanel.getEditingPanel();
                    editingPanel.setValues((DatabaseTable) databaseObject);
                    return editingPanel;
                case NamedObject.TABLE_COLUMN:
                    TableColumnPanel columnPanel = null;
                    if (!viewPanel.containsPanel(TableColumnPanel.NAME)) {
                        columnPanel = new TableColumnPanel(this);
                        viewPanel.addToLayout(columnPanel);
                    } else {
                        columnPanel =
                                (TableColumnPanel) viewPanel.getFormObjectView(TableColumnPanel.NAME);
                    }
                    columnPanel.setValues((DatabaseColumn) databaseObject);
                    return columnPanel;

                case NamedObject.TABLE_INDEX:
                case NamedObject.PRIMARY_KEY:
                case NamedObject.FOREIGN_KEY:
                case NamedObject.UNIQUE_KEY:
                    SimpleMetaDataPanel panel = null;
                    if (!viewPanel.containsPanel(SimpleMetaDataPanel.NAME)) {
                        panel = new SimpleMetaDataPanel(this);
                        viewPanel.addToLayout(panel);
                    } else {
                        panel = (SimpleMetaDataPanel) viewPanel.getFormObjectView(SimpleMetaDataPanel.NAME);
                    }
                    panel.setValues(databaseObject);
                    return panel;

                default:
                    ObjectDefinitionPanel objectDefnPanel = null;
                    if (!viewPanel.containsPanel(ObjectDefinitionPanel.NAME)) {
                        objectDefnPanel = new ObjectDefinitionPanel(this);
                        viewPanel.addToLayout(objectDefnPanel);
                    } else {
                        objectDefnPanel = (ObjectDefinitionPanel) viewPanel.
                                getFormObjectView(ObjectDefinitionPanel.NAME);
                    }
                    objectDefnPanel.setValues(
                            (org.executequery.databaseobjects.DatabaseObject) databaseObject);
                    return objectDefnPanel;

            }

        } catch (Exception e) {
            handleException(e);
            return null;
        }

    }

    private HostPanel hostPanel() {
        HostPanel hostPanel = null;
        if (!viewPanel.containsPanel(HostPanel.NAME)) {

            hostPanel = new HostPanel(this);
            viewPanel.addToLayout(hostPanel);

        } else {

            hostPanel = (HostPanel) viewPanel.getFormObjectView(HostPanel.NAME);
        }
        return hostPanel;
    }

    /**
     * Selects the node that matches the specified prefix forward
     * from the currently selected node.
     *
     * @param prefix - the prefix of the node to select
     */
    protected void selectBrowserNode(String prefix) {
        treePanel.selectBrowserNode(prefix);
    }

    /**
     * Displays the root main view panel.
     */
    protected void displayConnectionList(ConnectionsFolder folder) {
        viewPanel = (BrowserViewPanel) GUIUtilities.getCentralPane(BrowserViewPanel.TITLE);
        checkBrowserPanel();
        viewPanel.displayConnectionList(folder);
    }

    /**
     * Displays the root main view panel.
     */
    protected void displayConnectionList() {
        viewPanel = (BrowserViewPanel) GUIUtilities.getCentralPane(BrowserViewPanel.TITLE);
        checkBrowserPanel();
        viewPanel.displayConnectionList();
    }

    /**
     * Displays the root main view panel.
     */
    protected void displayRootPanel(RootDatabaseObjectNode node) {
        checkBrowserPanel();
        viewPanel.displayConnectionList();
    }

    /**
     * Applies the table alteration changes.
     */
    protected void applyTableChange(boolean valueChange) {

        BrowserTableEditingPanel editingPanel = viewPanel.getEditingPanel();

        // check we actually have something to apply
        if (!editingPanel.hasSQLText()) {
            return;
        }

        // retrieve the browser node
        BrowserTreeNode node = null;
        if (valueChange) {
            // if we are selecting a new node, get the previous selection
            node = treePanel.getOldBrowserNodeSelection();
        } else {
            // otherwise get the current selection
            node = treePanel.getSelectedBrowserNode();
        }

        try {
            treePanel.removeTreeSelectionListener();

            // if specified, ask the user again
            if (valueChange) {
                int yesNo = GUIUtilities.displayConfirmCancelDialog(
                        Bundles.get("common.message.apply-changes"));
                if (yesNo == JOptionPane.NO_OPTION) {
                    node = treePanel.getSelectedBrowserNode();
                    editingPanel.selectionChanged(node.getDatabaseUserObject(), true);
                    editingPanel.resetSQLText();
                    return;
                } else if (yesNo == JOptionPane.CANCEL_OPTION) {
                    treePanel.setNodeSelected(node);
                    return;
                }
            }

            // apply the changes to the database
            if (querySender == null) {
                querySender = new DefaultStatementExecutor();
            }
            querySender.setDatabaseConnection(getDatabaseConnection());

            SqlStatementResult result = null;
            StringTokenizer st = new StringTokenizer(
                    editingPanel.getSQLText().trim(), ";\n");

            try {
                while (st.hasMoreTokens()) {
                    result = querySender.updateRecords(st.nextToken());
                    if (result.getUpdateCount() < 0) {
                        editingPanel.setSQLText();
                        SQLException e = result.getSqlException();
                        if (e != null) {

                            GUIUtilities.displayExceptionErrorDialog(Bundles.get("common.error.apply-changes") + MiscUtils.formatSQLError(e), e);
                        } else {
                            GUIUtilities.displayErrorMessage(result.getErrorMessage());
                        }
                        treePanel.setNodeSelected(node);
                        return;
                    }
                }
                querySender.execute(QueryTypes.COMMIT, (String) null);
            } catch (SQLException e) {
                GUIUtilities.displayExceptionErrorDialog(Bundles.get("common.error.apply-changes") + MiscUtils.formatSQLError(e), e);
                treePanel.setNodeSelected(node);
                return;
            }

            // reset the current panel
            editingPanel.selectionChanged(node.getDatabaseUserObject(), true);
            editingPanel.resetSQLText();
            treePanel.setNodeSelected(node);
        } finally {
            treePanel.addTreeSelectionListener();
        }
    }

    /**
     * Returns whether a table alteration has occurred and
     * is actionable.
     *
     * @return true | false
     */
    protected boolean hasAlterTable() {
        if (viewPanel == null) {
            return false;
        }
        return viewPanel.getEditingPanel().hasSQLText()
                || viewPanel.getEditingPanel().getTableDataPanel().hasChanges();
    }

    // --------------------------------------------
    // Meta data propagation methods
    // --------------------------------------------

    /**
     * Generic exception handler.
     */
    protected void handleException(Throwable e) {
        if (Log.isDebugEnabled()) {
            e.printStackTrace();
            Log.debug(bundleString("error.handle.log"), e);
        }

        boolean isDataSourceException = (e instanceof DataSourceException);
        GUIUtilities.displayExceptionErrorDialog(
                bundleString("error.handle.exception") +
                        (isDataSourceException ? ((DataSourceException) e).getExtendedMessage() : e.getMessage()), e);


        if (isDataSourceException) {

            if (((DataSourceException) e).wasConnectionClosed()) {

//                connect(treePanel.getSelectedDatabaseConnection());
                disconnect(treePanel.getSelectedDatabaseConnection());
            }

        }

    }

    /**
     * Propagates the call to the meta data object.
     */
    protected Vector<String> getHostedSchemas(DatabaseConnection dc) {
        try {
            metaData.setDatabaseConnection(dc);
            return metaData.getHostedSchemasVector();
        } catch (DataSourceException e) {
            handleException(e);
            return new Vector<String>(0);
        }
    }

    /**
     * Propagates the call to the meta data object.
     */
    protected Vector<String> getColumnNamesVector(DatabaseConnection dc,
                                                  String table, String schema) {
        try {
            metaData.setDatabaseConnection(dc);
            return metaData.getColumnNamesVector(table, schema);
        } catch (DataSourceException e) {
            handleException(e);
            return new Vector<String>(0);
        }
    }

    /**
     * Propagates the call to the meta data object.
     */
    protected Vector<String> getColumnNamesVector(String table, String schema) {
        return getColumnNamesVector(getDatabaseConnection(), table, schema);
    }

    /**
     * Propagates the call to the meta data object.
     */
    protected Vector<String> getHostedSchemas() {
        return getHostedSchemas(getDatabaseConnection());
    }

    /**
     * Propagates the call to the meta data object.
     */
    protected Vector<String> getTables(String schema) {
        return getTables(getDatabaseConnection(), schema);
    }

    /**
     * Propagates the call to the meta data object.
     */
    protected Vector<String> getTables(DatabaseConnection dc, String schema) {
        try {
            metaData.setDatabaseConnection(dc);
            return metaData.getSchemaTables(schema);
        } catch (DataSourceException e) {
            handleException(e);
            return new Vector<String>(0);
        }
    }

    protected ColumnData[] getColumnData(String catalog, String schema, String name, DatabaseConnection connection) {
        try {
            metaData.setDatabaseConnection(connection);
            return metaData.getColumnMetaData(
                    isUsingCatalogs() ? catalog : null, schema, name);
        } catch (DataSourceException e) {
            handleException(e);
            return new ColumnData[0];
        }
    }

    /**
     * Recycles the specified connection object for the browser.
     *
     * @param dc - the connection to be recycled
     */
    protected void recycleConnection(DatabaseConnection dc) {
        try {
            metaData.recycleConnection(dc);
        } catch (DataSourceException e) {
            handleException(e);
        }
    }

    /**
     * Returns true if the currently selected connection is using
     * catalogs in meta data retrieval.
     *
     * @return true | false
     */
    protected boolean isUsingCatalogs() {

        return false;
    }

    /**
     * Retrieves the selected database connection properties object.
     */
    protected DatabaseConnection getDatabaseConnection() {

        return treePanel.getSelectedDatabaseConnection();
    }

    /**
     * Drops the specified database object.
     *
     * @param dc - the database connection
     * @param object - the object to be dropped
     */

    /*
    public int dropObject(DatabaseConnection dc, NamedObject object)
        throws SQLException {

        String queryStart = null;
        int type = object.getType();
        switch (type) {

            case NamedObject.CATALOG:
            case NamedObject.SCHEMA:
            case NamedObject.OTHER:
                GUIUtilities.displayErrorMessage(
                    "Dropping objects of this type is not currently supported");
                return UPDATE_CANCELLED;

            case NamedObject.FUNCTION:
                queryStart = "DROP FUNCTION ";
                break;

            case NamedObject.INDEX:
                queryStart = "DROP INDEX ";
                break;

            case NamedObject.PROCEDURE:
                queryStart = "DROP PROCEDURE ";
                break;

            case NamedObject.SEQUENCE:
                queryStart = "DROP SEQUENCE ";
                break;

            case NamedObject.SYNONYM:
                queryStart = "DROP SYNONYM ";
                break;

            case NamedObject.SYSTEM_TABLE:
            case BrowserConstants.TABLE_NODE:
                queryStart = "DROP TABLE ";
                break;

            case NamedObject.TRIGGER:
                queryStart = "DROP TRIGGER ";
                break;

            case NamedObject.VIEW:
                queryStart = "DROP VIEW ";
                break;

        }

        if (querySender == null) {
            querySender = new QuerySender(dc);
        } else {
            querySender.setDatabaseConnection(dc);
        }

        String name = object.getName();
        return querySender.updateRecords(queryStart + name).getUpdateCount();
    }
    */

    /**
     * Propagates the call to the meta data object.
     */
    protected void closeConnection() {

        if (metaData != null) {

            metaData.closeConnection();
        }
    }

    public void connectionNameChanged(String name) {

        hostPanel().connectionNameChanged(name);
    }

    private String bundleString(String key) {

        return Bundles.get(BrowserController.class, key);
    }

}






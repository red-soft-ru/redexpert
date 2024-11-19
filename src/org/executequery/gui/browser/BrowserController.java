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
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.ColumnConstraint;
import org.executequery.databaseobjects.impl.*;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.gui.databaseobjects.AbstractCreateObjectPanel;
import org.executequery.gui.databaseobjects.CreateIndexPanel;
import org.executequery.gui.forms.FormObjectView;
import org.executequery.gui.table.EditConstraintPanel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;

import java.util.Objects;
import java.util.Vector;

/**
 * Performs SQL execution tasks from browser components.
 *
 * @author Takis Diakoumis
 */
public class BrowserController {

    /**
     * the meta data retrieval object
     */
    private final MetaDataValues metaData;

    /**
     * the connections tree panel
     */
    private final ConnectionsTreePanel treePanel;

    /**
     * the database viewer panel
     */
    private BrowserViewPanel viewPanel;

    public BrowserController(ConnectionsTreePanel treePanel) {
        this.treePanel = treePanel;
        this.viewPanel = new BrowserViewPanel(this);
        this.metaData = new MetaDataValues(true);
    }

    /**
     * Connects the specified connection.
     *
     * @param dc the database connection to connect
     */
    protected void connect(DatabaseConnection dc) {
        try {
            ((DatabaseHost) treePanel.getHostNode(dc).getDatabaseObject()).connect();

        } catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(Bundles.getCommon("error.connection") + e.getExtendedMessage(), e, this.getClass());
        }
    }

    /**
     * Disconnects the specified connection.
     *
     * @param dc the database connection to disconnect
     */
    protected void disconnect(DatabaseConnection dc) {
        try {
            ((DatabaseHost) treePanel.getHostNode(dc).getDatabaseObject()).disconnect();

        } catch (DataSourceException e) {
            Log.error("Disconnection error: " + e);
        }
    }

    /**
     * Performs the drop database object action.
     */
    protected void dropSelectedObject() {
        if (!treePanel.isTypeParentSelected())
            treePanel.removeTreeNode();
    }

    /**
     * Ensures we have a browser panel and that it is visible.
     */
    protected void checkBrowserPanel() {

        if (viewPanel == null)
            viewPanel = new BrowserViewPanel(this);

        String title = (viewPanel.getNameObject() != null) ? viewPanel.getNameObject() : BrowserViewPanel.TITLE;

        if (GUIUtilities.getCentralPane(title) == null) {
            GUIUtilities.addCentralPane(title, BrowserViewPanel.FRAME_ICON, viewPanel, title, true);
            ConnectionHistory.add(viewPanel.getCurrentView());
        } else
            GUIUtilities.setSelectedCentralPane(title);
    }

    /**
     * Informs the view panel of a pending change.
     */
    protected void selectionChanging() {
        if (viewPanel != null)
            viewPanel.selectionChanging();
    }

    /**
     * Sets the selected connection tree node to the
     * specified database connection.
     *
     * @param dc the database connection to select
     */
    protected void setSelectedConnection(DatabaseConnection dc) {
        treePanel.setSelectedConnection(dc);
    }

    /**
     * Reloads the database properties meta data table panel.
     */
    public void updateDatabaseProperties() {

        FormObjectView view = viewPanel.getFormObjectView(HostPanel.NAME);
        if (view != null)
            ((HostPanel) view).updateDatabaseProperties();
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
     * This void has been moved in BrowserTreePopupMenuActionListener
     */
    public void valueChanged(DatabaseObjectNode node, DatabaseConnection connection) {
        valueChanged(node, connection, true);
    }

    /**
     * This void has been moved in BrowserTreePopupMenuActionListener
     */
    public void valueChanged(DatabaseObjectNode node, DatabaseConnection connection, boolean updatePropertiesPanel) {

        if (!isNodeObjectEditable(node))
            return;

        treePanel.setInProcess(true);
        try {

            FormObjectView panel = buildPanelView(node, updatePropertiesPanel);
            if (panel == null)
                return;

            panel.setDatabaseObjectNode(node);
            String type = "";

            int nodeType = node.getType();
            if (nodeType < NamedObject.META_TYPES.length)
                type = NamedObject.META_TYPES[node.getType()];

            if (connection == null)
                connection = getDatabaseConnection();

            String panelName = !node.isHostNode() && connection != null ? buildNodeName(node, type, connection) : null;
            panel.setObjectName(panelName);

            panel.setDatabaseConnection(connection);
            viewPanel.setView(panel);
            checkBrowserPanel();

            if (viewPanel.getCurrentView() != null)
                viewPanel = null;

        } finally {
            treePanel.setInProcess(false);
        }
    }

    private static String buildNodeName(DatabaseObjectNode node, String type, DatabaseConnection connection) {
        String name;

        if (node.getType() == NamedObject.TABLE_COLUMN) {
            name = MiscUtils.trimEnd(((DatabaseObjectNode) node.getParent()).getShortName() + "." + node.getShortName());
        } else if (node.getType() == NamedObject.USER) {
            name = MiscUtils.trimEnd(node.getShortName()) + ":" + ((DefaultDatabaseUser) node.getDatabaseObject()).getPlugin();
        } else
            name = MiscUtils.trimEnd(node.getShortName());

        name += ":" + type;
        if (connection != null)
            name += ":" + connection.getName();

        return name;
    }

    /**
     * Determines and builds the object view panel to be
     * displayed based on the specified host node connection object
     * and the selected node as specified.
     *
     * @param node the selected node
     */
    @SuppressWarnings("DataFlowIssue")
    private FormObjectView buildPanelView(DatabaseObjectNode node, boolean updatePropertiesPanel) {
        try {

            NamedObject databaseObject = node.getDatabaseObject();
            if (databaseObject == null)
                return null;

            DatabaseConnection connection = null;
            if (databaseObject instanceof AbstractDatabaseObject)
                connection = ((AbstractDatabaseObject) databaseObject).getHost().getDatabaseConnection();

            int type = node.getType();
            viewPanel = new BrowserViewPanel(this);
            switch (type) {

                case NamedObject.HOST: {

                    viewPanel = (BrowserViewPanel) GUIUtilities.getCentralPane(BrowserViewPanel.TITLE);
                    if (viewPanel == null)
                        viewPanel = new BrowserViewPanel(this);

                    HostPanel hostPanel = hostPanel();
                    hostPanel.setValues((DatabaseHost) databaseObject, updatePropertiesPanel);

                    return hostPanel;
                }

                case NamedObject.META_TAG:
                case NamedObject.SYSTEM_STRING_FUNCTIONS:
                case NamedObject.SYSTEM_NUMERIC_FUNCTIONS:
                case NamedObject.SYSTEM_DATE_TIME_FUNCTIONS: {

                    MetaKeyPanel metaKeyPanel;
                    if (!viewPanel.containsPanel(MetaKeyPanel.NAME)) {
                        metaKeyPanel = new MetaKeyPanel(this);
                        viewPanel.addToLayout(metaKeyPanel);

                    } else
                        metaKeyPanel = (MetaKeyPanel) viewPanel.getFormObjectView(MetaKeyPanel.NAME);

                    metaKeyPanel.setValues(databaseObject);
                    return metaKeyPanel;
                }

                case NamedObject.DOMAIN:
                case NamedObject.PROCEDURE:
                case NamedObject.FUNCTION:
                case NamedObject.TRIGGER:
                case NamedObject.DDL_TRIGGER:
                case NamedObject.DATABASE_TRIGGER:
                case NamedObject.SEQUENCE:
                case NamedObject.PACKAGE:
                case NamedObject.USER:
                case NamedObject.TABLESPACE:
                case NamedObject.JOB:
                case NamedObject.EXCEPTION:
                case NamedObject.UDF:
                case NamedObject.ROLE:
                case NamedObject.SYSTEM_DOMAIN:
                case NamedObject.SYSTEM_ROLE:
                case NamedObject.SYSTEM_PACKAGE:
                case NamedObject.SYSTEM_FUNCTION: {

                    AbstractCreateObjectPanel objectPanel = AbstractCreateObjectPanel
                            .getEditPanelFromType(type, connection, node.getDatabaseObject());

                    if (!viewPanel.containsPanel(Objects.requireNonNull(objectPanel).getLayoutName())) {
                        viewPanel.addToLayout(objectPanel);
                    } else
                        objectPanel = (AbstractCreateObjectPanel) viewPanel.getFormObjectView(objectPanel.getLayoutName());

                    objectPanel.setCurrentPath(node.getTreePath());
                    return objectPanel;
                }

                case NamedObject.SYSTEM_TRIGGER: {

                    BrowserTriggerPanel triggerPanel;
                    if (!viewPanel.containsPanel(BrowserTriggerPanel.NAME)) {
                        triggerPanel = new BrowserTriggerPanel(this);
                        viewPanel.addToLayout(triggerPanel);

                    } else
                        triggerPanel = (BrowserTriggerPanel) viewPanel.getFormObjectView(BrowserTriggerPanel.NAME);

                    triggerPanel.setValues((DefaultDatabaseTrigger) databaseObject);
                    return triggerPanel;
                }

                case NamedObject.SYSTEM_SEQUENCE: {

                    BrowserSequencePanel sequencePanel;
                    if (!viewPanel.containsPanel(BrowserSequencePanel.NAME)) {
                        sequencePanel = new BrowserSequencePanel(this);
                        viewPanel.addToLayout(sequencePanel);

                    } else
                        sequencePanel = (BrowserSequencePanel) viewPanel.getFormObjectView(BrowserSequencePanel.NAME);

                    sequencePanel.setValues((DefaultDatabaseSequence) databaseObject);
                    return sequencePanel;
                }

                case NamedObject.SYSTEM_INDEX: {
                    try {

                        GUIUtilities.showWaitCursor();
                        BaseDialog dialog = new BaseDialog(CreateIndexPanel.ALTER_TITLE, true);
                        CreateIndexPanel createObjectPanel = new CreateIndexPanel(connection, dialog, (DefaultDatabaseIndex) databaseObject, true);
                        showDialogCreateObject(createObjectPanel, dialog);

                    } finally {
                        GUIUtilities.showNormalCursor();
                    }
                    return null;
                }

                case NamedObject.TABLE_INDEX:
                case NamedObject.INDEX: {
                    try {

                        GUIUtilities.showWaitCursor();
                        BaseDialog dialog = new BaseDialog(CreateIndexPanel.ALTER_TITLE, true);
                        CreateIndexPanel createObjectPanel = new CreateIndexPanel(connection, dialog, (DefaultDatabaseIndex) databaseObject);
                        showDialogCreateObject(createObjectPanel, dialog);

                    } finally {
                        GUIUtilities.showNormalCursor();
                    }
                    return null;
                }

                case NamedObject.TABLE:
                case NamedObject.GLOBAL_TEMPORARY:
                case NamedObject.SYSTEM_TABLE: {
                    BrowserTableEditingPanel editingPanel = viewPanel.getEditingPanel();
                    editingPanel.setValues((DatabaseTable) databaseObject);
                    return editingPanel;
                }

                case NamedObject.VIEW: {

                    BrowserViewEditingPanel objectDefinitionPanel;
                    if (!viewPanel.containsPanel(ObjectDefinitionPanel.NAME)) {
                        objectDefinitionPanel = new BrowserViewEditingPanel(this);
                        viewPanel.addToLayout(objectDefinitionPanel);

                    } else
                        objectDefinitionPanel = (BrowserViewEditingPanel) viewPanel.getFormObjectView(ObjectDefinitionPanel.NAME);

                    objectDefinitionPanel.setValues((org.executequery.databaseobjects.DatabaseObject) databaseObject);
                    return objectDefinitionPanel;
                }


                case NamedObject.TABLE_COLUMN: {
                    AbstractCreateObjectPanel objectPanel = AbstractCreateObjectPanel
                            .getEditPanelFromType(type, connection, node.getDatabaseObject());

                    if (!viewPanel.containsPanel(objectPanel.getLayoutName())) {
                        viewPanel.addToLayout(objectPanel);
                    } else
                        objectPanel = (AbstractCreateObjectPanel) viewPanel.getFormObjectView(objectPanel.getLayoutName());

                    objectPanel.setCurrentPath(node.getTreePath());
                    return objectPanel;
                }

                case NamedObject.PRIMARY_KEY:
                case NamedObject.FOREIGN_KEY:
                case NamedObject.UNIQUE_KEY: {
                    try {
                        GUIUtilities.showWaitCursor();
                        ColumnConstraint constraint = (ColumnConstraint) databaseObject;

                        BaseDialog dialog = new BaseDialog(EditConstraintPanel.EDIT_TITLE, true);
                        EditConstraintPanel objectPanel = new EditConstraintPanel(constraint.getTable(), dialog, constraint);
                        showDialogCreateObject(objectPanel, dialog);

                    } finally {
                        GUIUtilities.showNormalCursor();
                    }
                    return null;
                }

                default: {

                    ObjectDefinitionPanel objectDefinitionPanel;
                    if (!viewPanel.containsPanel(ObjectDefinitionPanel.NAME)) {
                        objectDefinitionPanel = new ObjectDefinitionPanel(this);
                        viewPanel.addToLayout(objectDefinitionPanel);

                    } else
                        objectDefinitionPanel = (ObjectDefinitionPanel) viewPanel.getFormObjectView(ObjectDefinitionPanel.NAME);

                    objectDefinitionPanel.setValues((org.executequery.databaseobjects.DatabaseObject) databaseObject);
                    return objectDefinitionPanel;
                }
            }

        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    protected void showDialogCreateObject(AbstractCreateObjectPanel panel, BaseDialog dialog) {
        panel.setTreePanel(treePanel);
        panel.setCurrentPath(treePanel.getTreeSelectionPath());
        dialog.addDisplayComponentWithEmptyBorder(panel);
        dialog.display();
    }

    private HostPanel hostPanel() {

        HostPanel hostPanel;
        if (!viewPanel.containsPanel(HostPanel.NAME)) {
            hostPanel = new HostPanel(this);
            viewPanel.addToLayout(hostPanel);

        } else
            hostPanel = (HostPanel) viewPanel.getFormObjectView(HostPanel.NAME);

        return hostPanel;
    }

    private static boolean isNodeObjectEditable(DatabaseObjectNode node) {

        int nodeType = node.getType();
        if (NamedObject.isTableFolder(nodeType))
            return false;

        if (nodeType == NamedObject.TABLE_COLUMN) {
            int parentType = ((DatabaseObjectNode) node.getParent()).getType();
            return parentType != NamedObject.VIEW;
        }

        return true;
    }

    /**
     * Selects the node that matches the specified prefix forward
     * from the currently selected node.
     *
     * @param prefix the prefix of the node to select
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

        if (viewPanel.getCurrentView() != null)
            viewPanel = null;

    }

    /**
     * Displays the root main view panel.
     */
    protected void displayConnectionList() {

        viewPanel = (BrowserViewPanel) GUIUtilities.getCentralPane(BrowserViewPanel.TITLE);
        checkBrowserPanel();
        viewPanel.displayConnectionList();

        if (viewPanel.getCurrentView() != null)
            viewPanel = null;

    }

    // --------------------------------------------
    // Meta data propagation methods
    // --------------------------------------------

    /**
     * Generic exception handler.
     */
    protected void handleException(Throwable throwable) {

        if (Log.isDebugEnabled())
            Log.debug(bundleString("error.handle.log"), throwable);

        boolean isDataSourceException = (throwable instanceof DataSourceException);
        GUIUtilities.displayExceptionErrorDialog(
                String.format(
                        bundleString("error.handle.exception"),
                        isDataSourceException ? ((DataSourceException) throwable).getExtendedMessage() : throwable.getMessage()
                ),
                throwable,
                this.getClass());

        if (isDataSourceException && ((DataSourceException) throwable).wasConnectionClosed())
            disconnect(treePanel.getSelectedDatabaseConnection());
    }

    /**
     * Propagates the call to the metadata object.
     */
    protected Vector<String> getColumnNamesVector(DatabaseConnection dc, String table) {
        try {
            metaData.setDatabaseConnection(dc);
            return metaData.getColumnNamesVector(table);

        } catch (DataSourceException e) {
            handleException(e);
            return new Vector<>(0);
        }
    }

    /**
     * Propagates the call to the metadata object.
     */
    protected Vector<String> getColumnNamesVector(String table) {
        return getColumnNamesVector(getDatabaseConnection(), table);
    }

    /**
     * Propagates the call to the metadata object.
     */
    protected Vector<String> getTables() {
        return getTables(getDatabaseConnection());
    }

    /**
     * Propagates the call to the metadata object.
     */
    protected Vector<String> getTables(DatabaseConnection dc) {
        try {
            metaData.setDatabaseConnection(dc);
            return metaData.getAllTables();

        } catch (DataSourceException e) {
            handleException(e);
            return new Vector<>(0);
        }
    }

    /**
     * Retrieves the selected database connection properties object.
     */
    protected DatabaseConnection getDatabaseConnection() {
        return treePanel.getSelectedDatabaseConnection();
    }

    /**
     * Propagates the call to the metadata object.
     */
    protected void closeConnection() {
        if (metaData != null)
            metaData.closeConnection();
    }

    public void connectionNameChanged(String name) {
        hostPanel().connectionNameChanged(name);
    }

    public void setViewPanel(BrowserViewPanel viewPanel) {
        this.viewPanel = viewPanel;
    }

    private String bundleString(String key) {
        return Bundles.get(BrowserController.class, key);
    }
}

/*
 * ConnectionsTreePanel.java
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

import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.ConnectionMediator;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseConnectionFactory;
import org.executequery.databasemediators.spi.DatabaseConnectionFactoryImpl;
import org.executequery.databasemediators.spi.TemplateDatabaseConnection;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseObjectFactory;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DatabaseObjectFactoryImpl;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.event.*;
import org.executequery.gui.browser.nodes.ConnectionsFolderNode;
import org.executequery.gui.browser.nodes.DatabaseHostNode;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.gui.browser.nodes.RootDatabaseObjectNode;
import org.executequery.gui.browser.tree.SchemaTree;
import org.executequery.gui.browser.tree.TreePanel;
import org.executequery.localization.Bundles;
import org.executequery.repository.ConnectionFoldersRepository;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.toolbar.PanelToolBar;
import org.underworldlabs.swing.tree.DynamicTree;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

/**
 * @author Takis Diakoumis
 */
public class ConnectionsTreePanel extends TreePanel
        implements ConnectionListener,
        ConnectionRepositoryListener,
        UserPreferenceListener {

    public static final String TITLE = Bundles.get(ConnectionsTreePanel.class, "Connections");
    public static final String MENU_ITEM_KEY = "viewConnections";
    public static final String PROPERTY_KEY = "system.display.connections";

    private boolean rootSelectOnDisconnect;
    private boolean moveScroll = false;
    private boolean treeExpanding = false;
    private boolean moveScrollAfterExpansion = false;

    private SchemaTree tree;
    private BrowserController controller;
    private TreePath oldSelectionPath;

    private List<ConnectionsFolder> folders;
    private List<DatabaseConnection> connections;

    private TreeFindAction treeFindAction;
    private DatabaseHostNodeSorter hostNodeSorter;

    private JScrollPane scrollPane;
    private ConnectionsTreeToolBar toolBar;
    private BrowserTreePopupMenu popupMenu;
    private BrowserTreeRootPopupMenu rootPopupMenu;
    private BrowserTreeFolderPopupMenu folderPopupMenu;

    private DatabaseObjectFactoryImpl databaseObjectFactory;
    private DatabaseConnectionFactory databaseConnectionFactory;

    public ConnectionsTreePanel() {
        super(new BorderLayout());
        init();
    }

    private void init() {

        rootSelectOnDisconnect = false;

        controller = new BrowserController(this);
        tree = new SchemaTree(createTreeStructure(), this);

        MouseHandler mouseHandler = new MouseHandler();
        tree.addMouseListener(mouseHandler);

        treeFindAction = new TreeFindAction();
        treeFindAction.install(tree);

        add(createToolBar(), BorderLayout.NORTH);
        scrollPane = new JScrollPane(tree);
        add(scrollPane, BorderLayout.CENTER);

        EventMediator.registerListener(this);

        enableButtons(false, false, false, false, false);
        tree.setSelectionRow(0);
        tree.setToggleClickCount(-1);

    }

    private DefaultMutableTreeNode createTreeStructure() {

        RootDatabaseObjectNode root = new RootDatabaseObjectNode();
        try {
            folders = folders();
        } catch (Exception e) {
            folders = new ArrayList<>();
            e.printStackTrace();
        }

        List<DatabaseConnection> connectionsAdded = new ArrayList<>();
        DatabaseObjectFactory factory = databaseObjectFactory();

        int count = 0;
        try {
            connections = connections();
        } catch (Exception e) {
            connections = new ArrayList<>();
            e.printStackTrace();
        }

        for (ConnectionsFolder folder : folders) {

            ConnectionsFolderNode folderNode = new ConnectionsFolderNode(folder);
            for (DatabaseConnection connection : folder.getConnections()) {

                DatabaseHostNode child = createHostNode(factory, folderNode, connection);
                child.setOrder(count++);
                folderNode.add(child);
                connectionsAdded.add(connection);
            }

            root.add(folderNode);
        }

        for (DatabaseConnection connection : connections) {
            if (!connectionsAdded.contains(connection)) {
                DatabaseHostNode child = createHostNode(factory, null, connection);
                root.add(child);
            }
        }

        return root;
    }

    private DatabaseHostNode createHostNode(
            DatabaseObjectFactory factory, ConnectionsFolderNode folderNode, DatabaseConnection connection) {

        return new DatabaseHostNode(factory.createDatabaseHost(connection), folderNode);
    }

    public Action getTreeFindAction() {
        return treeFindAction;
    }

    private List<ConnectionsFolder> folders() {
        return ((ConnectionFoldersRepository) RepositoryCache.load(ConnectionFoldersRepository.REPOSITORY_ID)).findAll();
    }

    private List<DatabaseConnection> connections() {
        return ((DatabaseConnectionRepository) RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID)).findAll();
    }

    private DatabaseConnectionFactory databaseConnectionFactory() {
        if (databaseConnectionFactory == null)
            databaseConnectionFactory = new DatabaseConnectionFactoryImpl();
        return databaseConnectionFactory;
    }

    private DatabaseObjectFactory databaseObjectFactory() {
        if (databaseObjectFactory == null)
            databaseObjectFactory = new DatabaseObjectFactoryImpl();
        return databaseObjectFactory;
    }

    private PanelToolBar createToolBar() {
        toolBar = new ConnectionsTreeToolBar(this);
        return toolBar;
    }

    private void enableButtons(boolean enableUpButton, boolean enableDownButton, boolean enableReloadButton, boolean enableDeleteButton, boolean enableConnect) {
        toolBar.enableButtons(enableUpButton, enableDownButton, enableReloadButton, enableDeleteButton, enableConnect, enableReloadButton);
    }

    public void collapseAll() {

        for (Enumeration<TreeNode> i = tree.getRootNode().children(); i.hasMoreElements(); ) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) i.nextElement();
            tree.collapsePath(new TreePath(node.getPath()));
        }

    }

    /**
     * Moves the selected connection (host node) up in the list.
     */
    public void moveConnectionUp() {
        move(DynamicTree.MOVE_UP);
    }

    /**
     * Moves the selected connection (host node) down in the list.
     */
    public void moveConnectionDown() {
        move(DynamicTree.MOVE_DOWN);
    }

    private void move(int direction) {

        if (tree.canMoveSelection(direction)) {
            try {

                removeTreeSelectionListener();
                tree.moveSelection(direction);

                // adjust the position of the connection
                Object object = tree.getLastPathComponent();

                if (isADatabaseHostNode(object))
                    moveNode(connections, asDatabaseHostNode(object).getDatabaseConnection(), direction);
                else if (isAConnectionsFolderNode(object))
                    moveNode(folders, asConnectionsFolderNode(object).getConnectionsFolder(), direction);

            } finally {
                addTreeSelectionListener();
            }
        }
    }

    private ConnectionsFolderNode asConnectionsFolderNode(Object object) {
        return (ConnectionsFolderNode) object;
    }

    private DatabaseHostNode asDatabaseHostNode(Object object) {
        return (DatabaseHostNode) object;
    }

    @SuppressWarnings("rawtypes")
    public void sortConnections() {

        if (hostNodeSorter == null)
            hostNodeSorter = new DatabaseHostNodeSorter();

        boolean isRootNode = false;
        DefaultMutableTreeNode selectedNode = getSelectedFolderNode();
        if (selectedNode == null) {
            isRootNode = true;
            selectedNode = tree.getConnectionsBranchNode();
        }

        hostNodeSorter.sort(selectedNode);
        tree.nodeStructureChanged(selectedNode);

        int count = 0;
        for (Enumeration i = selectedNode.children(); i.hasMoreElements(); ) {

            Object object = i.nextElement();
            if (object instanceof DatabaseHostNode) {

                DatabaseHostNode node = asDatabaseHostNode(object);
                DatabaseConnection databaseConnection = node.getDatabaseConnection();
                connections.remove(databaseConnection);
                connections.add(count, databaseConnection);

                count++;
            }
        }

        if (isRootNode) {
            connectionModified((DatabaseConnection) null);

        } else {

            ConnectionsFolderNode folderNode = (ConnectionsFolderNode) selectedNode;
            ConnectionsFolder folder = folderNode.getConnectionsFolder();
            folder.empty();

            for (Enumeration<?> j = selectedNode.children(); j.hasMoreElements(); ) {

                Object object = j.nextElement();
                if (isADatabaseHostNode(object)) {

                    DatabaseHostNode child = asDatabaseHostNode(object);
                    DatabaseConnection databaseConnection = child.getDatabaseConnection();
                    folder.addConnection(databaseConnection.getId());
                    databaseConnection.setFolderId(folder.getId());
                }

            }

            folderModified(folder);

        }
    }

    public BrowserController getController() {
        return controller;
    }

    @Override
    public void rebuildConnectionsFromTree() {

        DefaultMutableTreeNode root = tree.getConnectionsBranchNode();
        connections.clear();
        folders.clear();

        for (Enumeration<?> i = root.children(); i.hasMoreElements(); ) {

            DefaultMutableTreeNode _node = (DefaultMutableTreeNode) i.nextElement();
            if (_node instanceof ConnectionsFolderNode) {

                ConnectionsFolderNode folderNode = (ConnectionsFolderNode) _node;
                ConnectionsFolder connectionsFolder = folderNode.getConnectionsFolder();
                connectionsFolder.empty();

                for (Enumeration<?> j = folderNode.children(); j.hasMoreElements(); ) {

                    Object object = j.nextElement();
                    if (isADatabaseHostNode(object)) {

                        DatabaseHostNode child = asDatabaseHostNode(object);
                        child.setParentFolder(folderNode);

                        DatabaseConnection databaseConnection = addConnectionFromNode(child);
                        connectionsFolder.addConnection(databaseConnection.getId());
                    }

                }

                folders.add(folderNode.getConnectionsFolder());

            } else if (_node instanceof DatabaseHostNode) {
                addConnectionFromNode(_node);
            }

        }

        folderModified(null);
    }

    private void connectionModified(DatabaseConnection databaseConnection) {
        EventMediator.fireEvent(new DefaultConnectionRepositoryEvent(this, ConnectionRepositoryEvent.CONNECTION_MODIFIED, databaseConnection));
    }

    private void connectionAdded(DatabaseConnection databaseConnection) {
        EventMediator.fireEvent(new DefaultConnectionRepositoryEvent(this, ConnectionRepositoryEvent.CONNECTION_ADDED, databaseConnection));
    }

    private void folderModified(ConnectionsFolder connectionsFolder) {
        EventMediator.fireEvent(new DefaultConnectionsFolderRepositoryEvent(this, ConnectionsFolderRepositoryEvent.FOLDER_MODIFIED, connectionsFolder));
    }

    private void folderAdded(ConnectionsFolder connectionsFolder) {
        EventMediator.fireEvent(new DefaultConnectionsFolderRepositoryEvent(this, ConnectionsFolderRepositoryEvent.FOLDER_ADDED, connectionsFolder));
    }

    private void folderRemoved(ConnectionsFolder connectionsFolder) {
        EventMediator.fireEvent(new DefaultConnectionsFolderRepositoryEvent(this, ConnectionsFolderRepositoryEvent.FOLDER_REMOVED, connectionsFolder));
    }


    private DatabaseConnection addConnectionFromNode(DefaultMutableTreeNode node) {

        Object userObject = node.getUserObject();
        DatabaseHost object = (DatabaseHost) userObject;

        DatabaseConnection databaseConnection = object.getDatabaseConnection();
        connections.add(databaseConnection);

        return databaseConnection;
    }

    /**
     * Selects and scrolls the tree to the specified path.
     *
     * @param path the tree path
     */
    protected void selectTreePath(TreePath path) {

        try {
            removeTreeSelectionListener();
            tree.scrollPathToVisible(path);
            tree.setSelectionPath(path);

        } finally {
            addTreeSelectionListener();
        }

    }

    /**
     * Sets the selected connection tree node to the
     * specified database connection.
     *
     * @param dc the database connection to select
     */
    public void setSelectedConnection(DatabaseConnection dc) {

        DefaultMutableTreeNode node = getHostNode(dc);
        if (node != null) {
            selectTreePath(new TreePath(node.getPath()));
            tree.getLastPathComponent();
        }

    }

    /**
     * Deletes the selected connection (host node) from the list.
     */
    public void deleteConnection() {

        Object object = tree.getLastPathComponent();

        if (isADatabaseHostNode(object))
            deleteConnection(asDatabaseHostNode(object));
        else if (isAConnectionsFolderNode(object))
            deleteFolder(asConnectionsFolderNode(object));

    }

    private void deleteFolder(ConnectionsFolderNode folder) {

        int result = GUIUtilities.displayConfirmCancelDialog(bundleString("message.confirm-delete-folder", folder));

        if (result != JOptionPane.YES_OPTION)
            return;

        if (folder.getChildCount() > 0)
            for (Enumeration<TreeNode> i = folder.children(); i.hasMoreElements(); )
                connections.remove(((DatabaseHostNode) i.nextElement()).getDatabaseConnection());

        ConnectionsFolder connectionsFolder = folder.getConnectionsFolder();
        folders.remove(connectionsFolder);
        tree.removeNode(folder);
        connectionModified((DatabaseConnection) null);
        folderRemoved(connectionsFolder);

    }

    /**
     * Deletes the specified connection (host node) from the list.
     *
     * @param node node representing the connection to be removed
     */
    public void deleteConnection(DatabaseHostNode node) {

        int result = GUIUtilities.displayYesNoDialog(bundleString("message.confirm-delete-connection", node), bundleString("title.confirm-delete-connection"));

        if (result != JOptionPane.YES_OPTION)
            return;

        DatabaseConnection dc = node.getDatabaseConnection();

        // check that we're not connected
        if (dc.isConnected()) {
            try {
                ConnectionMediator.getInstance().disconnect(dc);

            } catch (DataSourceException e) {
                e.printStackTrace();
            }
        }

        // the next selection index will be the index of
        // the one being removed - (index - 1)
        int index = connections.indexOf(dc);
        if (index == -1)
            return;

        DatabaseHostNode nextSelectableHostNode = nextSelectableHostNode(node);

        tree.removeNode(node);      // remove the node from the tree
        node.removeFromFolder();    // remove from folder
        connections.remove(index);  // remove from the connections

        connectionModified(dc);
        folderModified(null);

        if (nextSelectableHostNode != null) {
            setNodeSelected(nextSelectableHostNode);

        } else {

            GUIUtils.invokeLater(() -> {
                controller.selectionChanging();
                enableButtons(false, false,
                        false, false, false);
                tree.setSelectionRow(0);
            });

        }

    }

    private DatabaseHostNode nextSelectableHostNode(DatabaseHostNode node) {

        List<DatabaseHostNode> hostNodes = databaseHostNodes();

        int size = hostNodes.size();
        if (size > 1) {

            for (int i = 0; i < size; i++) {

                DatabaseHostNode databaseHostNode = hostNodes.get(i);
                if (databaseHostNode == node) {

                    if (i == 0) {

                        return hostNodes.get(i + 1);

                    } else {

                        return hostNodes.get(i - 1);
                    }

                }

            }
        }

        return null;
    }

    /**
     * Returns the database connection at the specified point.
     *
     * @return the connection properties object
     */
    protected DatabaseConnection getConnectionAt(Point point) {
        return getConnectionAt(tree.getPathForLocation(point.x, point.y));
    }

    /**
     * Returns the database connection associated with the specified path.
     *
     * @return the connection properties object
     */
    protected DatabaseConnection getConnectionAt(TreePath path) {
        if (path != null) {
            Object object = path.getLastPathComponent();
            if (isADatabaseObjectNode(object)) {
                return getDatabaseConnection((DatabaseObjectNode) object);
            }
        }
        return null;
    }

    /**
     * Removes the tree listener.
     */
    protected void removeTreeSelectionListener() {
        tree.removeTreeSelectionListener();
    }

    /**
     * Adds the tree listener.
     */
    protected void addTreeSelectionListener() {
        tree.addTreeSelectionListener();
    }

    /**
     * Selects the specified node.
     */
    protected void setNodeSelected(DefaultMutableTreeNode node) {
        if (node != null) {
            TreePath path = new TreePath(node.getPath());
            tree.setSelectionPath(path);
        }
    }


    /**
     * Removes the selected tree node (database object) from the tree.
     */
    public void removeSelectedNode() {

        int row = -1;
        try {

            removeTreeSelectionListener();                                  // remove the listener
            row = Objects.requireNonNull(tree.getSelectionRows())[0];       // store the current row
            tree.removeNode((BrowserTreeNode) tree.getLastPathComponent()); // retrieve the current selection node

        } finally {
            addTreeSelectionListener();
            if (row >= 0)
                tree.setSelectionRow((row != 0) ? row - 1 : 1);
        }
    }

    public void connectAll() {

        List<DatabaseHost> hosts = databaseHosts();
        for (DatabaseHost databaseHost : hosts)
            try {
                if (!databaseHost.isConnected())
                    databaseHost.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public void disconnectAll() {

        List<DatabaseHost> hosts = databaseHosts();
        for (DatabaseHost databaseHost : hosts)
            try {
                if (databaseHost.isConnected())
                    databaseHost.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

    }

    public void searchNodes() {
        getTreeFindAction().actionPerformed(new ActionEvent(this, 0, "searchNodes"));
    }

    private List<DatabaseHostNode> databaseHostNodes() {

        DefaultMutableTreeNode root = tree.getConnectionsBranchNode();
        List<DatabaseHostNode> hosts = new ArrayList<>();

        for (Enumeration<?> i = root.children(); i.hasMoreElements(); ) {

            Object object = i.nextElement();
            if (isAConnectionsFolderNode(object)) {
                ConnectionsFolderNode node = asConnectionsFolderNode(object);
                hosts.addAll(node.getDatabaseHostNodes());

            } else if (isADatabaseHostNode(object)) {
                hosts.add(asDatabaseHostNode(object));
            }

        }

        return hosts;
    }

    private List<DatabaseHost> databaseHosts() {

        DefaultMutableTreeNode parent = getSelectedFolderNode();
        if (parent == null)
            parent = tree.getConnectionsBranchNode();

        List<DatabaseHost> hosts = new ArrayList<>();
        for (Enumeration<?> i = parent.children(); i.hasMoreElements(); ) {

            Object object = i.nextElement();
            if (isAConnectionsFolderNode(object)) {

                ConnectionsFolderNode node = asConnectionsFolderNode(object);
                hosts.addAll(node.getDatabaseHosts());

            } else if (isADatabaseHostNode(object)) {

                DatabaseHostNode node = asDatabaseHostNode(object);
                DatabaseHost host = (DatabaseHost) node.getDatabaseObject();
                hosts.add(host);
            }
        }

        return hosts;
    }

    public ConnectionsFolder newFolder() {

        String name = GUIUtilities.displayInputMessage(bundleString("NewFolder"), bundleString("FolderName"));
        if (!JOptionPane.UNINITIALIZED_VALUE.equals(name)) {

            ConnectionsFolder folder = new ConnectionsFolder(name);
            folders.add(folder);

            ConnectionsFolderNode lastFolder = null;
            DefaultMutableTreeNode root = tree.getConnectionsBranchNode();
            for (Enumeration<?> i = root.children(); i.hasMoreElements(); ) {

                Object object = i.nextElement();
                if (isAConnectionsFolderNode(object))
                    lastFolder = asConnectionsFolderNode(object);
                else
                    break;
            }

            int insertIndex = 0;
            ConnectionsFolderNode folderNode = new ConnectionsFolderNode(folder);
            if (lastFolder != null)
                insertIndex = root.getIndex(lastFolder) + 1;

            root.insert(folderNode, insertIndex);
            tree.nodesWereInserted(root, new int[]{insertIndex});
            folderAdded(folder);

            return folder;
        }

        return null;
    }

    /**
     * Creates a new connection and adds it to the bottom of the list.
     */
    public void newConnection() {
        String name = buildConnectionName(Bundles.getCommon("newConnection.button"));
        newConnection(databaseConnectionFactory().create(name));
    }

    public void newConnection(String sourceName) {

        String username = SystemProperties.getProperty("user", "startup.default.connection.username");
        String password = SystemProperties.getProperty("user", "startup.default.connection.password");
        String charset = SystemProperties.getProperty("user", "startup.default.connection.charset");
        TemplateDatabaseConnection tdc = new TemplateDatabaseConnection(username, password, charset, true);
        String name = buildConnectionName(Bundles.getCommon("newConnection.button"));

        newConnection(databaseConnectionFactory().create(name, sourceName, tdc));
    }

    /**
     * Creates a new connection based on the specified connection.
     *
     * @param dc the connection the new one is to be based on
     */
    public void newConnection(DatabaseConnection dc) {

        DatabaseHost host = databaseObjectFactory().createDatabaseHost(dc);
        connections.add(dc);
        ConnectionsFolderNode folderNode = getSelectedFolderNode();

        if (folderNode != null) {

            final DatabaseHostNode hostNode = new DatabaseHostNode(host, folderNode);
            folderNode.addNewHostNode(hostNode);
            tree.nodesWereInserted(folderNode, new int[]{folderNode.getChildCount() - 1});
            tree.selectNode(hostNode);
            folderModified(folderNode.getConnectionsFolder());

        } else {
            DatabaseHostNode databaseHostNode = new DatabaseHostNode(host, null);
            tree.addToRoot(databaseHostNode);
            valueChanged(databaseHostNode, null);
        }

        connectionAdded(dc);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void moveNode(List list, Object object, int direction) {

        int currentIndex = list.indexOf(object);
        if (currentIndex == 0 && direction == DynamicTree.MOVE_UP)
            return;

        int newIndex;
        if (direction == DynamicTree.MOVE_UP) {
            newIndex = currentIndex - 1;

        } else {

            newIndex = currentIndex + 1;
            if (newIndex > (list.size() - 1))
                return;
        }

        list.remove(currentIndex);
        list.add(newIndex, object);
    }

    /**
     * Indicates that a node name has changed and fires a call
     * to repaint the tree display.
     */
    protected void nodeNameValueChanged(Object nodeObject) {
        TreeNode node = tree.getNodeFromRoot(nodeObject);
        if (node != null)
            tree.nodeChanged(node);
    }

    /**
     * Returns the currently selected node's user object where the
     * node is a BrowserTreeNode and the user object is a BaseDatabaseObject.
     * If the above is not met, null is returned.
     *
     * @return the user object of the selected node where the
     * user object is a DBaseDatabaseObject
     */
    protected BrowserTreeNode getSelectedBrowserNode() {

        if (tree.isSelectionEmpty())
            return null;

        Object object = tree.getLastPathComponent();
        if (!isABrowserTreeNode(object))
            return null;

        return (BrowserTreeNode) object;
    }

    /**
     * Returns whether the currently selected node's user object
     * is a parent type where the node is a BrowserTreeNode and the
     * user object is a BaseDatabaseObject. If the above is not met, false is
     * returned, otherwise the object is evaluated.
     *
     * @return true | false
     */
    protected boolean isTypeParentSelected() {

        if (tree.isSelectionEmpty())
            return false;

        Object object = tree.getLastPathComponent();
        if (!isABrowserTreeNode(object))
            return false;

        return ((BrowserTreeNode) object).isTypeParent();
    }

    /**
     * Returns the currently selected node's user object where the
     * node is a BrowserTreeNode and the user object is a BaseDatabaseObject.
     * If the above is not met, null is returned.
     *
     * @return the user object of the selected node where the
     * user object is a DBaseDatabaseObject
     */
    protected NamedObject getSelectedNamedObject() {

        if (tree.isSelectionEmpty())
            return null;

        Object object = tree.getLastPathComponent();
        if (!isADatabaseObjectNode(object))
            return null;

        return ((DatabaseObjectNode) object).getDatabaseObject();
    }

    protected List<ConnectionsFolderNode> getFolderNodes() {

        List<ConnectionsFolderNode> folders = new ArrayList<>();
        Enumeration<?> children = tree.getRootNode().children();

        while (children.hasMoreElements()) {
            Object child = children.nextElement();
            if (isAConnectionsFolderNode(child))
                folders.add((ConnectionsFolderNode) child);
        }

        return folders;
    }

    protected ConnectionsFolderNode getSelectedFolderNode() {

        if (tree.isSelectionEmpty())
            return null;

        Object object = tree.getLastPathComponent();
        if (isAConnectionsFolderNode(object))
            return asConnectionsFolderNode(object);
        else if (isADatabaseHostNode(object))
            return asDatabaseHostNode(object).getParentFolder();

        return null;
    }

    private boolean isRootNode(Object object) {
        return object instanceof RootDatabaseObjectNode;
    }

    private boolean isADatabaseHostNode(Object object) {
        return object instanceof DatabaseHostNode;
    }

    private boolean isADatabaseObjectNode(Object object) {
        return object instanceof DatabaseObjectNode;
    }

    private boolean isAConnectionsFolderNode(Object object) {
        return object instanceof ConnectionsFolderNode;
    }

    private boolean isABrowserTreeNode(Object object) {
        return object instanceof BrowserTreeNode;
    }

    /**
     * Returns the selected metaObject host node.
     *
     * @return the selected host node metaObject
     */
    protected DatabaseHost getSelectedMetaObject() {

        if (tree.isSelectionEmpty())
            return null;

        Object object = tree.getLastPathComponent();
        if (!isADatabaseObjectNode(object))
            return null;

        DatabaseObjectNode node = (DatabaseObjectNode) object;
        DatabaseObjectNode parent = getParentNode(node);
        if (parent == null)
            return null;

        return ((DatabaseHost) parent.getDatabaseObject());
    }

    /**
     * Returns the selected database connection.
     *
     * @return the selected connection properties object
     */
    public DatabaseConnection getSelectedDatabaseConnection() {

        DatabaseHost object = getSelectedMetaObject();
        if (object != null)
            return object.getDatabaseConnection();

        return null;
    }

    // ------------------------------------------
    // --- ConnectionListener implementation ---
    // ------------------------------------------

    /**
     * Indicates a connection has been established.
     *
     * @param connectionEvent encapsulating event
     */
    @Override
    public void connected(ConnectionEvent connectionEvent) {

        DatabaseConnection dc = connectionEvent.getDatabaseConnection();
        DatabaseObjectNode node = getHostNode(dc);

        // if the host node itself is selected - enable/disable buttons
        TreePath selectionPath = tree.getSelectionPath();
        if (selectionPath != null && selectionPath.getLastPathComponent() == node)
            enableButtons(true, true, true, false, true);

        nodeStructureChanged(node);
    }

    /**
     * Indicates a connection has been closed.
     *
     * @param connectionEvent encapsulating event
     */
    @Override
    public void disconnected(ConnectionEvent connectionEvent) {

        DatabaseConnection dc = connectionEvent.getDatabaseConnection();
        DatabaseHostNode host = (DatabaseHostNode) getHostNode(dc);
        host.disconnected();
        nodeStructureChanged(host);
        oldSelectionPath = null;

        if (rootSelectOnDisconnect) {
            tree.setSelectionRow(0);

        } else {

            if (tree.getSelectionPath() != null) {

                if (tree.getSelectionPath().getLastPathComponent() == host)
                    enableButtons(true, true, false, true, true);
                else
                    enableButtons(false, false, false, false, false);

            } else {
                enableButtons(false, false, false, false, false);
            }
        }

    }

    @Override
    public boolean canHandleEvent(ApplicationEvent event) {
        return (event instanceof ConnectionEvent) ||
                (event instanceof UserPreferenceEvent) ||
                (event instanceof ConnectionRepositoryEvent && "connectionImported".equals(event.getMethod()));
    }

    // ---

    /**
     * Returns the previously selected path before the current
     * selection.
     *
     * @return the previous path
     */
    protected TreePath getOldSelectionPath() {
        return oldSelectionPath;
    }

    /**
     * Returns the previously selected browse node before the
     * current selection.
     *
     * @return the previous node selection
     */
    protected BrowserTreeNode getOldBrowserNodeSelection() {

        Object object = getOldSelectionPath().getLastPathComponent();
        if (isABrowserTreeNode(object))
            return (BrowserTreeNode) object;

        return null;
    }

    protected ConnectionsFolderNode getFolderNode(ConnectionsFolder folder) {

        for (Enumeration<?> i = tree.getConnectionsBranchNode().children(); i.hasMoreElements(); ) {

            Object object = i.nextElement();
            if (isAConnectionsFolderNode(object)) {

                ConnectionsFolderNode folderNode = (ConnectionsFolderNode) object;
                if (folderNode.getConnectionsFolder() == folder)
                    return folderNode;
            }
        }

        return null;
    }

    public DefaultDatabaseHost getDefaultDatabaseHostFromConnection(DatabaseConnection dc) {
        return (DefaultDatabaseHost) getHostNode(dc).getDatabaseObject();
    }


    public DatabaseObjectNode getHostNode(DatabaseConnection dc) {

        for (Enumeration<?> i = tree.getConnectionsBranchNode().children(); i.hasMoreElements(); ) {

            Object object = i.nextElement();
            if (isAConnectionsFolderNode(object)) {

                ConnectionsFolderNode node = asConnectionsFolderNode(object);
                DatabaseObjectNode hostNode = node.getHostNode(dc);

                if (hostNode != null)
                    return hostNode;

            } else if (isADatabaseObjectNode(object)) {

                DatabaseObjectNode node = (DatabaseObjectNode) object;
                if (node.getType() == NamedObject.HOST) {

                    DatabaseHost host = (DatabaseHost) node.getDatabaseObject();
                    if (host.getDatabaseConnection() == dc)
                        return node;
                }
            }
        }

        return null;
    }

    /**
     * Notification that the currently selected node (a host)
     * has had their associated db connection closed.
     */
    protected void selectedNodeDisconnected() {

        Object object = tree.getLastPathComponent();
        if (!isADatabaseObjectNode(object))
            return;

        DatabaseObjectNode node = (DatabaseObjectNode) object;
        if (!(node.getUserObject() instanceof DatabaseHost))
            node = getParentNode(node);

        DatabaseObjectNode parent = getParentNode(node);
        parent.removeAllChildren();
        nodeStructureChanged(parent);

    }

    /**
     * Notification that the currently selected node (a host)
     * has had their associated db connection created.
     */
    protected void selectedNodeConnected() {

        if (tree.isSelectionEmpty())
            return;

        Object object = tree.getLastPathComponent();
        if (!isADatabaseObjectNode(object))
            return;

        // ensure node is expandable
        DatabaseObjectNode node = (DatabaseObjectNode) object;
        if (node.isLeaf() && node instanceof DatabaseHostNode) {
            pathExpanded(Objects.requireNonNull(tree.getSelectionPath()));
            nodeStructureChanged(node);
        }

    }

    public void pathChanged(TreePath newPath) {
        pathChanged(oldSelectionPath, newPath);
    }

    private boolean canProceedWithChangesApplied(Object selectedNode) {

        if (isADatabaseObjectNode(selectedNode)) {

            try {

                DatabaseObjectNode databaseObjectNode = (DatabaseObjectNode) selectedNode;
                boolean applyChanges = databaseObjectChangeProvider(databaseObjectNode.getDatabaseObject()).applyChanges(true);

                if (!applyChanges)
                    return false;

            } catch (DataSourceException e) {
                GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e);
                return false;
            }
        }

        return true;
    }

    private DatabaseObjectChangeProvider databaseObjectChangeProvider(NamedObject namedObject) {
        return new DatabaseObjectChangeProvider(namedObject);
    }

    private synchronized void doNodeExpansion(DatabaseObjectNode node) {
        try {

            if (node.getChildCount() == 0) {
                node.populateChildren();
                nodeStructureChanged(node);
            }

        } catch (DataSourceException e) {
            controller.handleException(e);
        }
    }

    @Override
    public synchronized void valueChanged(DatabaseObjectNode node) {
        if (node.getDatabaseObject().getParent().getType() != NamedObject.PACKAGE)
            controller.valueChanged_(node, null);
    }

    public synchronized void valueChanged(DatabaseObjectNode node, DatabaseConnection connection) {
        controller.valueChanged_(node, connection);
    }

    /**
     * Reloads the currently selected node.
     */
    public void reloadSelection() {
        reloadPath(tree.getSelectionPath());
    }

    /**
     * Reloads the specified tree path.
     */
    public void reloadPath(TreePath path) {

        try {

            if (treeExpanding || path == null)
                return;

            Object object = path.getLastPathComponent();
            if (!isADatabaseObjectNode(object))
                return;

            GUIUtilities.showWaitCursor();

            boolean expanded = tree.isExpanded(path);
            if (expanded)
                tree.collapsePath(path);

            DatabaseObjectNode node = (DatabaseObjectNode) object;
            node.reset();
            nodeStructureChanged(node);
            pathExpanded(path);

            if (expanded)
                tree.expandPath(path);

            pathChanged(oldSelectionPath, path);

        } finally {
            GUIUtilities.showNormalCursor();
        }
    }

    protected void nodeStructureChanged(TreeNode node) {
        tree.nodeStructureChanged(node);
    }

    public void reloadRowHeight() {
        tree.setRowHeight(Integer.parseInt(
                SystemProperties.getProperty("user", "treeconnection.row.height")));
    }

    @Override
    public void pathChanged(TreePath oldPath, TreePath newPath) {

        oldSelectionPath = oldPath; // store the last position

        if (oldSelectionPath != null) {

            Object lastObject = oldSelectionPath.getLastPathComponent();
            if (!canProceedWithChangesApplied(lastObject)) {

                try {
                    removeTreeSelectionListener();
                    tree.setSelectionPath(oldSelectionPath);
                    return;

                } finally {
                    addTreeSelectionListener();
                }
            }
        }

        Object object = newPath.getLastPathComponent();
        if (object == null)
            return;

        controller.selectionChanging();

        if (object == tree.getConnectionsBranchNode()) { // root node
            controller.displayConnectionList();
            enableButtons(false, false, false, false, false);
            return;
        }

        final DatabaseObjectNode node = (DatabaseObjectNode) object;
        if (node instanceof ConnectionsFolderNode) {

            controller.displayConnectionList(((ConnectionsFolderNode) node).getConnectionsFolder());
            enableButtons(true, true, false, true, false);
            return;

        } else if (node instanceof DatabaseHostNode) {

            DatabaseHostNode hostNode = (DatabaseHostNode) node;
            boolean hostConnected = hostNode.isConnected();
            enableButtons(true, true, hostConnected, !hostConnected, true);

        } else {
            enableButtons(false, false, true, false, true);
        }

        if (node.isHostNode()) {

            final ConnectionsTreePanel c = this;
            c.setInProcess(true);

            SwingWorker worker = new SwingWorker("loadingNode " + node.getName()) {

                @Override
                public Object construct() {

                    try {
                        tree.startLoadingNode();
                        treeExpanding = true;

                    } finally {
                        treeExpanding = false;
                    }

                    return null;
                }

                @Override
                public void finished() {
                    tree.finishedLoadingNode();
                    treeExpanding = false;
                    c.setInProcess(false);
                }

            };
            worker.start();
        }

        if (isMoveScroll()) {
            moveScrollToSelection();
            setMoveScroll(false);
        }

    }

    @Override
    public void pathExpanded(TreePath path) {

        Object object = path.getLastPathComponent();
        if (!isADatabaseObjectNode(object))
            return;

        final DatabaseObjectNode node = (DatabaseObjectNode) object;
        SwingWorker worker = new SwingWorker("nodeExpansion " + node.getName()) {

            @Override
            public Object construct() {
                GUIUtilities.showWaitCursor();
                doNodeExpansion(node);
                return null;
            }

            @Override
            public void finished() {
                if (isMoveScrollAfterExpansion()) {
                    moveScrollToSelection();
                    setMoveScrollAfterExpansion(false);
                }
                GUIUtilities.showNormalCursor();
            }

        };
        worker.start();
    }

    public boolean isMoveScroll() {
        return moveScroll;
    }

    public void setMoveScroll(boolean moveScroll) {
        this.moveScroll = moveScroll;
    }

    public boolean isMoveScrollAfterExpansion() {
        return moveScrollAfterExpansion;
    }

    public void setMoveScrollAfterExpansion(boolean moveScroll) {
        this.moveScrollAfterExpansion = moveScroll;
    }

    protected DatabaseObjectNode getParentNode(DatabaseObjectNode child) {

        if (child instanceof DatabaseHostNode)
            return child;

        TreeNode parent = child.getParent();
        while (parent != null) {

            if (parent instanceof DatabaseHostNode)
                return (DatabaseObjectNode) parent;

            parent = parent.getParent();
        }

        return null;
    }

    /**
     * Selects the node that matches the specified prefix forward
     * from the currently selected node.
     *
     * @param prefix the prefix of the node to select
     */
    protected void selectBrowserNode(final String prefix) {

        // make sure it has its children
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();

        if (node.getChildCount() == 0)
            doNodeExpansion((DatabaseObjectNode) node);

        tree.expandSelectedRow();
        tree.selectNextNode(prefix);

    }

    /**
     * Returns the connection properties object associated with
     * the specified child node.
     */
    protected DatabaseConnection getDatabaseConnection(DatabaseObjectNode child) {

        DatabaseHost databaseHost = getConnectionObject(child);
        if (databaseHost != null)
            return databaseHost.getDatabaseConnection();

        return null;
    }

    /**
     * Returns the DatabaseHost (host node) associated with
     * the specified child node.
     */
    protected DatabaseHost getConnectionObject(DatabaseObjectNode child) {

        DatabaseObjectNode parent = getParentNode(child);
        if (parent != null)
            return (DatabaseHost) parent.getDatabaseObject();

        return null;
    }

    /**
     * Removes the selected node.<p>
     * This will attempt to propagate the call to the connected
     * database object using a DROP statement.
     */
    public void removeTreeNode() {

        TreePath selection = tree.getSelectionPath();
        if (selection != null) {

            Object object = selection.getLastPathComponent();
            if (isADatabaseObjectNode(object)) {

                DatabaseObjectNode dbObject = (DatabaseObjectNode) object;
                if (!dbObject.isDroppable())
                    return;

                int yesNo = GUIUtilities.displayConfirmDialog(bundleString("message.confirm-delete-object"));
                if (yesNo == JOptionPane.NO_OPTION)
                    return;

                removeTreeSelectionListener();
                int row = tree.getSelectionRows()[0];

                try {
                    dbObject.drop(); // quoted cases
                    tree.removeNode(dbObject);

                } catch (DataSourceException e) {
                    GUIUtilities.displayExceptionErrorDialog(
                            bundleString("error.delete-object") + e.getExtendedMessage(), e);

                } finally {
                    addTreeSelectionListener();
                    if (row >= 0)
                        tree.setSelectionRow((row == 0) ? 1 : row - 1);
                }
            }
        }
    }

    /**
     * Returns the name of a new connection to be added where
     * the name of the connection may already exist.
     *
     * @param name the name of the connection
     */
    protected String buildConnectionName(String name) {

        int count = 1;
        String tempName = name;

        while (existConName(tempName)) {
            tempName = name + " " + count;
            count++;
        }

        return tempName;
    }

    private boolean existConName(String name) {
        return connections.stream().anyMatch(o -> name.equals(o.getName()));
    }

    public boolean isRootSelectOnDisconnect() {
        return rootSelectOnDisconnect;
    }

    public void setRootSelectOnDisconnect(boolean rootSelectOnDisconnect) {
        this.rootSelectOnDisconnect = rootSelectOnDisconnect;
    }

    // ----------------------------------------
    // DockedTabView Implementation
    // ----------------------------------------

    /**
     * Returns the display title for this view.
     *
     * @return the title displayed for this view
     */
    public String getTitle() {
        return TITLE;
    }

    /**
     * Returns the name defining the property name for this docked tab view.
     *
     * @return the key
     */
    public String getPropertyKey() {
        return PROPERTY_KEY;
    }

    /**
     * Returns the name defining the menu cache property
     * for this docked tab view.
     *
     * @return the preferences key
     */
    public String getMenuItemKey() {
        return MENU_ITEM_KEY;
    }


    protected void handleException(Throwable e) {
        controller.handleException(e);
    }

    protected void disconnect(DatabaseConnection dc) {

        if (canProceedWithChangesApplied(tree.getLastPathComponent())) {

            setSelectedConnection(dc);
            controller.disconnect(dc);
        }

    }

    protected void connect(DatabaseConnection dc) {
        controller.connect(dc);
    }

    public void connectDisconnect() {

        if (!getSelectedDatabaseConnection().isConnected()) connect(getSelectedDatabaseConnection());
        else GUIUtilities.closeSelectedConnection();
    }

    protected void setTreeSelectionPath(TreePath treePath) {
        tree.setSelectionPath(treePath);
    }

    public TreePath getTreeSelectionPath() {
        return tree.getSelectionPath();
    }

    public TreePath[] getTreeSelectionPaths() {
        return tree.getSelectionPaths();
    }

    private boolean selectedPathsOnlyThisTyped(int namedObject) {

        boolean flag = true;
        TreePath[] treePaths = tree.getSelectionPaths();

        if (treePaths == null)
            return false;

        DatabaseObjectNode Object;
        for (TreePath treePath : treePaths) {

            Object = (DatabaseObjectNode) treePath.getLastPathComponent();
            if (Object.getType() != namedObject)
                flag = false;
        }

        return flag;
    }

    private boolean checkPathForLocationInSelectedTree(TreePath treePathForLocation) {

        if (treePathForLocation == null)
            return false;

        boolean flag = false;
        try {

            TreePath[] treePaths = tree.getSelectionPaths();
            if (treePaths == null)
                return false;

            for (TreePath treePath : treePaths)
                if (treePath.getLastPathComponent() == treePathForLocation.getLastPathComponent())
                    flag = true;

        } catch (Exception e) {
            return false;
        }

        return flag;
    }

    private boolean checkShowActiveMenu(TreePath treePathForLocation) {
        return selectedTriggersOrIndexesOnly() && checkPathForLocationInSelectedTree(treePathForLocation);
    }

    private boolean selectedTriggersOrIndexesOnly() {
        return selectedPathsOnlyThisTyped(NamedObject.TRIGGER) || selectedPathsOnlyThisTyped(NamedObject.DDL_TRIGGER) || selectedPathsOnlyThisTyped(NamedObject.DATABASE_TRIGGER) || selectedPathsOnlyThisTyped(NamedObject.INDEX);
    }

    protected TreePath getTreePathForLocation(int x, int y) {
        return tree.getPathForLocation(x, y);
    }

    public BrowserTreePopupMenu getBrowserTreePopupMenu() {
        if (popupMenu == null) {
            popupMenu = new BrowserTreePopupMenu(new BrowserTreePopupMenuActionListener(this));
        }
        return popupMenu;
    }

    private BrowserTreeRootPopupMenu getBrowserRootTreePopupMenu() {
        if (rootPopupMenu == null) {
            rootPopupMenu = new BrowserTreeRootPopupMenu(this);
        }
        return rootPopupMenu;
    }

    private BrowserTreeFolderPopupMenu getBrowserTreeFolderPopupMenu() {
        if (folderPopupMenu == null) {
            folderPopupMenu = new BrowserTreeFolderPopupMenu(this);
        }
        return folderPopupMenu;
    }

    private boolean doubleClickHostToConnect() {
        return SystemProperties.getBooleanProperty("user", "browser.double-click.to.connect");
    }

    @Override
    public void preferencesChanged(UserPreferenceEvent event) {

        if (event.getEventType() == UserPreferenceEvent.ALL) {

            RootDatabaseObjectNode root = (RootDatabaseObjectNode) tree.getConnectionsBranchNode();
            List<DatabaseHostNode> hosts = root.getHostNodes();

            for (DatabaseHostNode host : hosts)
                host.applyUserPreferences();
        }

    }

    @Override
    public void connectionNameChanged(String name) {
        controller.connectionNameChanged(name);
    }

    public void moveToFolder(DatabaseConnection databaseConnection) {
        new MoveConnectionToFolderDialog(databaseConnection, this);
    }

    public void moveToFolder(DatabaseConnection databaseConnection, ConnectionsFolderNode folder) {

        ConnectionsFolder connectionsFolder = folder.getConnectionsFolder();
        databaseConnection.setFolderId(connectionsFolder.getId());
        connectionsFolder.addConnection(databaseConnection.getId());

        DatabaseObjectNode node = getHostNode(databaseConnection);
        tree.removeNode(node);
        folder.add(node);
        tree.nodeStructureChanged(folder);
        tree.expandPath(new TreePath(folder.getPath()));

        EventMediator.fireEvent(new DefaultConnectionRepositoryEvent(this, ConnectionRepositoryEvent.CONNECTION_MODIFIED, databaseConnection));
        EventMediator.fireEvent(new DefaultConnectionsFolderRepositoryEvent(this, ConnectionsFolderRepositoryEvent.FOLDER_MODIFIED, connectionsFolder));
    }

    public void moveScrollToSelection() {
        JScrollBar bar = scrollPane.getVerticalScrollBar();
        int max_bar = bar.getMaximum();
        int max_tree = tree.getRowCount();
        int value = tree.getMaxSelectionRow() * (max_bar / max_tree);
        bar.setValue(value);
    }

    public static ConnectionsTreePanel getPanelFromBrowser() {
        return (ConnectionsTreePanel) GUIUtilities.getDockedTabComponent(PROPERTY_KEY);
    }

    public static NamedObject getNamedObjectFromHost(DatabaseConnection dc, int type, String name) {
        return getPanelFromBrowser().getDefaultDatabaseHostFromConnection(dc).getDatabaseObjectFromTypeAndName(type, name);
    }

    public static NamedObject getTableOrViewFromHost(DatabaseConnection dc, String name) {
        List<Integer> list = new ArrayList<>();
        list.add(NamedObject.TABLE);
        list.add(NamedObject.GLOBAL_TEMPORARY);
        list.add(NamedObject.VIEW);
        list.add(NamedObject.SYSTEM_TABLE);
        list.add(NamedObject.SYSTEM_VIEW);
        for (Integer type : list) {
            NamedObject table = getPanelFromBrowser().getDefaultDatabaseHostFromConnection(dc).getDatabaseObjectFromTypeAndName(type, name);
            if (table != null)
                return table;
        }
        return null;
    }

    public static NamedObject getNamedObjectFromHost(DatabaseConnection dc, String metaTag, String name) {
        return getPanelFromBrowser().getDefaultDatabaseHostFromConnection(dc).getDatabaseObjectFromMetaTagAndName(metaTag, name);
    }

    public SchemaTree getTree() {
        return tree;
    }

    @Override
    public void connectionAdded(ConnectionRepositoryEvent connectionRepositoryEvent) {
        tree.reset(createTreeStructure());
    }

    @Override
    public void connectionImported(ConnectionRepositoryEvent connectionRepositoryEvent) {
    }

    @Override
    public void connectionModified(ConnectionRepositoryEvent connectionRepositoryEvent) {
    }

    @Override
    public void connectionRemoved(ConnectionRepositoryEvent connectionRepositoryEvent) {
    }

    @Override
    public String toString() {
        return TITLE;
    }

    private class MouseHandler extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() > 1)
                twoClicks(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getClickCount() < 2)
                maybeShowPopup(e);
            else
                twoClicks(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void twoClicks(MouseEvent e) {
            TreePath path = pathFromMouseEvent(e);
            if (path != null && path == getTreeSelectionPath())
                connectOnDoubleClick(path);
        }

        private void maybeShowPopup(MouseEvent e) {

            if (e.isPopupTrigger()) {

                Point point = new Point(e.getX(), e.getY());
                TreePath treePathForLocation = getTreePathForLocation(point.x, point.y);

                if (!checkShowActiveMenu(treePathForLocation)) {
                    try {
                        removeTreeSelectionListener();
                        setTreeSelectionPath(treePathForLocation);

                    } finally {
                        addTreeSelectionListener();
                    }
                }

                if (treePathForLocation != null) {

                    JPopupMenu popupMenu;
                    Object object = treePathForLocation.getLastPathComponent();

                    if (isAConnectionsFolderNode(object)) {
                        popupMenu = getBrowserTreeFolderPopupMenu();

                    } else if (isRootNode(object)) {
                        popupMenu = getBrowserRootTreePopupMenu();

                    } else {

                        popupMenu = getBrowserTreePopupMenu();
                        BrowserTreePopupMenu browserPopup = (BrowserTreePopupMenu) popupMenu;

                        if ((checkShowActiveMenu(treePathForLocation)) && tree.getSelectionPaths().length > 1) {
                            browserPopup.setTreePaths(tree.getSelectionPaths());
                            browserPopup.setSelectedSeveralPaths(true);

                        } else {
                            browserPopup.setCurrentPath(treePathForLocation);
                            browserPopup.setSelectedSeveralPaths(false);
                        }

                        DatabaseConnection connection = getConnectionAt(point);
                        if (connection == null)
                            return;

                        browserPopup.setCurrentSelection(connection);
                    }

                    popupMenu.show(e.getComponent(), point.x, point.y);
                }
            }
        }

        private TreePath pathFromMouseEvent(MouseEvent e) {
            return getTreePathForLocation(e.getX(), e.getY());
        }

        private void connectOnDoubleClick(TreePath path) {

            if (doubleClickHostToConnect()) {

                Object node = path.getLastPathComponent();
                if (node instanceof DatabaseHostNode) {

                    DatabaseHostNode hostNode = (DatabaseHostNode) node;
                    DatabaseConnection databaseConnection = hostNode.getDatabaseConnection();

                    if (!databaseConnection.isConnected())
                        connect(databaseConnection);
                    else
                        disconnect(databaseConnection);
                }
            }
        }

    }

}

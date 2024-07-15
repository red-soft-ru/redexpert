/*
 * DatabaseHostNode.java
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

package org.executequery.gui.browser.nodes;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.*;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.SystemProperties;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class DatabaseHostNode extends DatabaseObjectNode {

    private final boolean tableCatalogsEnabled;

    private int order;
    private List<DatabaseObjectNode> visibleChildren;
    private List<DatabaseObjectNode> allChildren;
    private ConnectionsFolderNode parentFolder;

    public DatabaseHostNode(DatabaseHost host, ConnectionsFolderNode parentFolder) {
        this(host, parentFolder, true);
    }

    public DatabaseHostNode(DatabaseHost host, ConnectionsFolderNode parentFolder, boolean tableCatalogsEnabled) {
        super(host);
        this.parentFolder = parentFolder;
        this.tableCatalogsEnabled = tableCatalogsEnabled;
        showAll();
    }

    @Override
    public DatabaseObjectNode copy() {
        return new DatabaseHostNode((DatabaseHost) getDatabaseObject().copy(), parentFolder);
    }

    @Override
    public DatabaseObjectNode newInstance() {
        return new DatabaseHostNode((DatabaseHost) getDatabaseObject(), parentFolder);
    }

    @Override
    public boolean isDraggable() {
        return true;
    }

    public void setParentFolder(ConnectionsFolderNode parentFolder) {
        this.parentFolder = parentFolder;
    }

    public ConnectionsFolderNode getParentFolder() {
        return parentFolder;
    }

    /**
     * Returns whether the host is connected.
     */
    public boolean isConnected() {
        return getDatabaseConnection().isConnected();
    }

    public DatabaseConnection getDatabaseConnection() {
        return ((DatabaseHost) getDatabaseObject()).getDatabaseConnection();
    }

    /**
     * Adds this object's children as expanded nodes.
     */
    public void populateChildren() throws DataSourceException {
        if (isConnected()) {
            super.populateChildren();
            showAll();
        }
    }

    /**
     * Indicates that this host has been disconnected.
     */
    public void disconnected() {
        reset();
    }

    /**
     * Indicates whether this node is a leaf node.
     *
     * @return true | false
     */
    public boolean isLeaf() {
        return !(isConnected());
    }

    /**
     * Override to return true.
     */
    public boolean allowsChildren() {
        return true;
    }

    /**
     * Returns the children associated with this node.
     *
     * @return a list of children for this node
     */
    public List<DatabaseObjectNode> getChildObjects() throws DataSourceException {

        if (visibleChildren != null) {

            return visibleChildren;
        }

        DatabaseHost host = (DatabaseHost) getDatabaseObject();
        List<?> metaObjects = host.getMetaObjects();
        if (metaObjects != null && !metaObjects.isEmpty()) {

            int count = metaObjects.size();
            visibleChildren = new ArrayList<>();
            allChildren = new ArrayList<>();
            for (int i = 0; i < count; i++) {

                DatabaseMetaTag metaTag = (DatabaseMetaTag) metaObjects.get(i);
                DatabaseObjectNode metaTagNode = new DatabaseObjectNode(metaTag, tableCatalogsEnabled);
                allChildren.add(metaTagNode);
                if ((!metaTag.getMetaDataKey().contains("SYSTEM")
                        || SystemProperties.getBooleanProperty("user", "browser.show.system.objects"))
                        && metaTag.getSubType() != NamedObject.COLLATION)
                    visibleChildren.add(metaTagNode);
            }

            return visibleChildren;
        }

        return null;
    }

    public List<DatabaseObjectNode> getAllChildren() {
        if (allChildren == null)
            getChildObjects();
        return allChildren;
    }

    /**
     * Clears out the children of this node.
     */
    @Override
    public void reset() {
        super.reset();
        visibleChildren = null;
        allChildren = null;
    }

    public void showAll() {

        if (visibleChildren == null)
            return;

        for (int i = 0; i < visibleChildren.size(); i++) {
            DatabaseObjectNode node = visibleChildren.get(i);
            if (!childExists(node))
                insert(node, i);
        }
    }

    private boolean childExists(TreeNode node) {

        for (Enumeration<?> i = children(); i.hasMoreElements(); ) {

            if (i.nextElement().equals(node)) {

                return true;
            }

        }

        return false;
    }

    public int getOrder() {

        return order;
    }

    public void setOrder(int order) {

        this.order = order;
    }

    public void removeFromFolder() {

        if (parentFolder != null) {

            parentFolder.removeNode(this);
        }
    }

    public List<DatabaseObjectNode> getAllDBObjects(String type) {
        List<DatabaseObjectNode> childs = getAllChildren();
        for (DatabaseObjectNode child : childs) {
            if (child.getMetaDataKey().contains(type))
                return child.getChildObjects();
        }
        return new ArrayList<>();
    }


}





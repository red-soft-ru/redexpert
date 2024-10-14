/*
 * DatabaseObjectNode.java
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

import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.AbstractTableObject;
import org.executequery.databaseobjects.impl.DatabaseTableColumn;
import org.executequery.databaseobjects.impl.DefaultDatabaseColumn;
import org.executequery.databaseobjects.impl.DefaultDatabaseView;
import org.executequery.gui.browser.nodes.tableNode.DatabaseTableNode;
import org.executequery.gui.browser.tree.RETreePath;
import org.executequery.localization.Bundles;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.SystemProperties;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * @author Takis Diakoumis
 */
public class DatabaseObjectNode extends DefaultMutableTreeNode {

    private boolean childrenRetrieved;
    private NamedObject databaseObject;
    private boolean tableCatalogsEnabled;
    private List<DatabaseObjectNode> childrenList;

    public DatabaseObjectNode() {
    }

    public DatabaseObjectNode(NamedObject databaseObject) {
        this(databaseObject, true);
    }

    public DatabaseObjectNode(NamedObject databaseObject, boolean tableCatalogsEnabled) {
        super(databaseObject);
        this.databaseObject = databaseObject;
        this.tableCatalogsEnabled = tableCatalogsEnabled;
    }

    public DatabaseObjectNode copy() {
        return new DatabaseObjectNode(this.databaseObject.copy());
    }

    public DatabaseObjectNode newInstance() {
        return new DatabaseObjectNode(this.databaseObject);
    }

    /**
     * Sets the user object to that specified.
     *
     * @param databaseObject the database object for this node
     */
    public void setDatabaseObject(NamedObject databaseObject) {
        setUserObject(databaseObject);
        this.databaseObject = databaseObject;
    }

    /**
     * Returns the database user object of this node.
     *
     * @return the database object
     */
    public NamedObject getDatabaseObject() {
        return databaseObject;
    }

    /**
     * Drops/deletes the object represented by this node.
     */
    public int drop() throws DataSourceException {
        NamedObject namedObject = getDatabaseObject();

        int result = namedObject.drop();
        if (result >= 0)
            namedObject.getParent().getObjects().remove(namedObject);

        return result;
    }

    public TreePath getTreePath() {

        TreeNode parent = getParent();
        if (parent instanceof DatabaseObjectNode) {
            TreePath treePath = ((DatabaseObjectNode) parent).getTreePath();
            return new RETreePath(treePath, this);
        }

        return new TreePath(this);
    }

    /**
     * Adds this object's children as expanded nodes.
     */
    public void populateChildren() throws DataSourceException {
        if (!childrenRetrieved) {
            for (DatabaseObjectNode child : getChildObjects())
                add(child);
            childrenRetrieved = true;
        }
    }

    /**
     * Returns the children associated with this node.
     *
     * @return a list of children for this node
     */
    public List<DatabaseObjectNode> getChildObjects() throws DataSourceException {

        if (childrenList != null)
            return childrenList;

        if (databaseObject == null)
            return new ArrayList<>();

        List<NamedObject> childObjects = databaseObject.getObjects();
        if (childObjects == null)
            return new ArrayList<>();

        childrenList = new ArrayList<>();
        for (NamedObject childObject : childObjects) {
            childrenList.add(isTableCatalog(childObject) ?
                    new DatabaseTableNode(childObject) :
                    new DatabaseObjectNode(childObject)
            );
        }

        return childrenList;
    }

    public Vector<TreeNode> getChildren() {
        return children;
    }

    /**
     * Indicates whether this node allows children attached.
     *
     * @return true | false
     */
    public boolean allowsChildren() {
        return databaseObject == null || databaseObject.allowsChildren();
    }

    /**
     * Indicates whether this node is a leaf node.
     *
     * @return true | false
     */
    @Override
    public boolean isLeaf() {

        if (getDatabaseObject() != null) {

            int type = getDatabaseObject().getType();
            if (type == NamedObject.TABLE_COLUMN
                    || type == NamedObject.FOREIGN_KEY
                    || type == NamedObject.PRIMARY_KEY
                    || type == NamedObject.UNIQUE_KEY
                    || type == NamedObject.TABLE_INDEX
                    || type == NamedObject.DOMAIN
            ) {
                return true;
            }

            if (type == NamedObject.META_TAG)
                return databaseObject.getObjects().isEmpty();
        }

        return !allowsChildren();
    }

    /**
     * Propagates the call to the underlying database object
     * and removes all children from this node.
     */
    public void reset() {
        databaseObject.reset();
        removeAllChildren();
        childrenRetrieved = false;
        childrenList = null;
    }

    /**
     * Propagates the call to the underlying database object.
     */
    public int getType() {
        return databaseObject.getType();
    }

    /**
     * Propagates the call to the underlying database object.
     */
    public String getName() {
        return databaseObject.getName();
    }

    /**
     * Propagates the call to the underlying database object.
     */
    public void setName(String name) {
        databaseObject.setName(name);
    }

    /**
     * Propagates the call to the underlying database object.
     */
    public String getDisplayName() {
        if (getType() == NamedObject.META_TAG)
            return databaseObject.getDescription();
        return getShortName();
    }

    public String getShortName() {
        return databaseObject.getShortName().trim();
    }

    /**
     * Propagates the call to the underlying database object.
     */
    public String getMetaDataKey() {
        return databaseObject.getMetaDataKey();
    }

    public boolean isSystem() {
        return databaseObject.isSystem();
    }

    public boolean isRootNode() {
        return false;
    }

    public boolean isHostNode() {
        return getType() == NamedObject.HOST;
    }

    private boolean isDatabaseTable(NamedObject namedObject) {
        return namedObject instanceof DatabaseTable;
    }

    private boolean isTableCatalog(NamedObject databaseObject) {
        return tableCatalogsEnabled
                && SystemProperties.getBooleanProperty("user", "browser.show.table.catalogs")
                && databaseObject instanceof AbstractTableObject
                && !(databaseObject instanceof DefaultDatabaseView);
    }

    public boolean isNameEditable() {
        return false;
    }

    public boolean isDraggable() {
        return getType() == NamedObject.TABLE || getType() == NamedObject.INDEX;
    }

    /**
     * Returns whether the object represented by this
     * node may be dropped/deleted.
     */
    public boolean isDroppable() {
        return isDatabaseTable(getDatabaseObject());
    }

    @Override
    public String toString() {

        String shortName = getShortName();
        String metaDataKey = getMetaDataKey();
        if (metaDataKey == null)
            return shortName;

        if (databaseObject instanceof DatabaseTableColumn)
            return ((DatabaseTableColumn) databaseObject).getTable().getName().trim() + "." + shortName + ":" + "COLUMN";

        if (databaseObject instanceof DefaultDatabaseColumn)
            return databaseObject.getParent().getName().trim() + "." + shortName + ":" + "COLUMN";

        return shortName + ":" + metaDataKey;
    }

    protected String bundleString(String key) {
        return Bundles.get(getClass(), key);
    }

}

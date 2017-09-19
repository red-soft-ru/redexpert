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

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.browser.DatabaseObjectChangeProvider;
import org.executequery.localization.Bundles;
import org.underworldlabs.jdbc.DataSourceException;

/** 
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1783 $
 * @date     $Date: 2017-09-19 00:04:44 +1000 (Tue, 19 Sep 2017) $
 */
public class DatabaseObjectNode extends DefaultMutableTreeNode {
    
    /** the underlying database object associated with this node */
    private NamedObject databaseObject;
    
    /** indicates that children have been retrieved */
    private boolean childrenRetrieved;

    /** Creates a new instance of DefaultDatabaseObjectNode */
    public DatabaseObjectNode() {}

    /** Creates a new instance of DefaultDatabaseObjectNode */
    public DatabaseObjectNode(NamedObject databaseObject) {

        super(databaseObject);
        this.databaseObject = databaseObject;
    }

    public DatabaseObjectNode copy() {
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
     * Returns whether any changes have been made to the
     * database object represented by this node within the 
     * view panel.
     */
    public boolean isObjectModified() throws DataSourceException {
        NamedObject namedObject = getDatabaseObject();
        if (isDatabaseTable(namedObject)) {
            return ((DatabaseTable)namedObject).isAltered();
        }
        return false;
    }

    /**
     * Reverts any changes on the underlying database object.
     */
    public void revert() {
        NamedObject namedObject = getDatabaseObject();
        if (isDatabaseTable(namedObject)) {
            ((DatabaseTable)namedObject).revert();
        }
    }

    public boolean isNameEditable() {
        return false;
    }
    
    public boolean isDraggable() {
        return false;
    }
    
    /**
     * Returns whether the object represented by this 
     * node may be dropped/deleted.
     */
    public boolean isDroppable() {
        NamedObject namedObject = getDatabaseObject();
        if (isDatabaseTable(namedObject)) {
            return true;
        }
        return false;
    }
    
    /**
     * Drops/deletes the object represented by this node.
     */
    public int drop() throws DataSourceException {

        NamedObject namedObject = getDatabaseObject();
        
        int result = namedObject.drop();

        if (result >= 0) {
        
            namedObject.getParent().getObjects().remove(namedObject);
        }
/*        
        // retrieve the parent node and check the meta type
        // to remove itself from the children
        TreeNode _parent = getParent();
        if (_parent instanceof DatabaseObjectNode) {
            DatabaseObjectNode parentNode = (DatabaseObjectNode)_parent;
            parentNode.getDatabaseObject().getObjects().remove(namedObject);
        }
*/
        return result;
    }
    
    /**
     * Applies any changes on the underlying database object.
     */
    public void applyChanges() throws DataSourceException {

//        NamedObject namedObject = getDatabaseObject();
        new DatabaseObjectChangeProvider(getDatabaseObject()).applyChanges(true);
        
//        if (isDatabaseTable(namedObject)) {
//
//            ((DatabaseTable)namedObject).applyChanges();
//        }

    }

    private boolean isDatabaseTable(NamedObject namedObject) {
        
        return (namedObject instanceof DatabaseTable);
    }
    
    /**
     * Adds this object's children as expanded nodes.
     */
    public void populateChildren() throws DataSourceException {
        
        if (!childrenRetrieved) {
       
            List<DatabaseObjectNode> children = getChildObjects();
            if (children != null) {
            
                for (int i = 0, n = children.size(); i < n; i++) {
                
                    add((MutableTreeNode)children.get(i));
                }

            }

            childrenRetrieved = true;
        }
    }
    
    /**
     * Returns the children associated with this node.
     *
     * @return a list of children for this node
     */
    public List<DatabaseObjectNode> getChildObjects() throws DataSourceException {
        
        NamedObject _namedObject = getDatabaseObject();
        if (_namedObject != null) {

            List<NamedObject> values = _namedObject.getObjects();
            if (values != null) {

                List<DatabaseObjectNode> nodes = new ArrayList<DatabaseObjectNode>();
                for (int i = 0, n = values.size(); i < n; i++) {
                
                    nodes.add(new DatabaseObjectNode(values.get(i)));
                }
                
                return nodes;
            }
        }

        return null;
    }
    
    /**
     * Indicates whether this node allows children attached.
     *
     * @return true | false
     */
    public boolean allowsChildren() {
        return true;
    }

    /**
     * Indicates whether this node is a leaf node.
     *
     * @return true | false
     */
    public boolean isLeaf() {

        if (getDatabaseObject() != null) { 
            
            int type = getDatabaseObject().getType();
            if (type == NamedObject.TABLE_COLUMN 
                    || type == NamedObject.FOREIGN_KEY
                    || type == NamedObject.PRIMARY_KEY
                    || type == NamedObject.UNIQUE_KEY
                    || type == NamedObject.TABLE_INDEX)

            return true;
        }

        return !(allowsChildren());
    }

    public boolean isHostNode() {
        
        return (getType() == NamedObject.HOST); 
    }
    
    /**
     * Propagates the call to the underlying database object 
     * and removes all children from this node.
     */
    public void reset() {        
        databaseObject.reset();
        removeAllChildren();
        childrenRetrieved = false;
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
        return databaseObject.getShortName();
    }
    
    /**
     * Propagates the call to the underlying database object.
     */
    public String getMetaDataKey() {
        return databaseObject.getMetaDataKey();
    }

    /**
     * Returns the display name.
     */
    public String toString() {
        return getDisplayName();
    }

    protected String bundleString(String key) {
    
        return Bundles.get(getClass(), key);
    }
    
}


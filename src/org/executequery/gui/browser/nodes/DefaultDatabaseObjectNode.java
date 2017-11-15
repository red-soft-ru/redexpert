/*
 * DefaultDatabaseObjectNode.java
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

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.executequery.databaseobjects.NamedObject;
import org.underworldlabs.jdbc.DataSourceException;

/** 
 *
 * @author   Takis Diakoumis
 */
public class DefaultDatabaseObjectNode extends DefaultMutableTreeNode {
                                       //implements DatabaseObjectNode {
    
    /** the underlying database object associated with this node */
    private NamedObject databaseObject;
    
    /** indicates that children have been retrieved */
    private boolean childrenRetrieved;

    /** Creates a new instance of DefaultDatabaseObjectNode */
    public DefaultDatabaseObjectNode(NamedObject databaseObject) {
        super(databaseObject);
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
        return null;
    }
    
    /**
     * Indicates whether this node is a leaf node.
     *
     * @return true | false
     */
    public boolean allowsChildren() {
        return false;
    }

    /**
     * Indicates whether this node is a leaf node.
     *
     * @return true | false
     */
    public boolean isLeaf() {
        return true;
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
    
}








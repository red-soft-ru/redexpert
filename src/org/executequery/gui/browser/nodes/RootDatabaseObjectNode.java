/*
 * RootDatabaseObjectNode.java
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

import javax.swing.tree.MutableTreeNode;

import org.executequery.databaseobjects.NamedObject;
import org.executequery.localization.Bundles;

/** 
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1783 $
 * @date     $Date: 2017-09-19 00:04:44 +1000 (Tue, 19 Sep 2017) $
 */
public class RootDatabaseObjectNode extends DatabaseObjectNode {
    
    private static final String NAME = Bundles.getCommon("database-connections");
    
    private List<DatabaseHostNode> hostNodes = new ArrayList<DatabaseHostNode>();

    /**
     * Propagates the call to the underlying database object 
     * and removes all children from this node.
     */
    public void reset() {}
    
    /**
     * Propagates the call to the underlying database object.
     */
    public int getType() {
        
        return NamedObject.ROOT;
    }

    /**
     * Returns the name of the root node.
     */
    public String getName() {
        
        return NAME;
    }

    /**
     * Does nothing. Name of the root node may not be changed.
     */
    public void setName(String name) {}
    
    /**
     * Propagates the call to the underlying database object.
     */
    public String getDisplayName() {
        
        return getName();
    }
    
    /**
     * Propagates the call to the underlying database object.
     */
    public String getMetaDataKey() {
        
        return null;
    }

    public List<DatabaseHostNode> getHostNodes() {
        
        return hostNodes;
    }

    public void add(MutableTreeNode newChild) {

        if (!(newChild instanceof DatabaseHostNode) && !(newChild instanceof ConnectionsFolderNode)) {

            throw new IllegalArgumentException("Node must be of type DatabaseHostNode");
        }

        if (newChild instanceof DatabaseHostNode) {
        
            hostNodes.add((DatabaseHostNode)newChild);
        }

        super.add(newChild);
    }
    
}





/*
 * DatabaseSchemaNode.java
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

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseSchema;
import org.underworldlabs.jdbc.DataSourceException;

/**
 *
 * @author   Takis Diakoumis
 */
public class DatabaseSchemaNode extends DatabaseObjectNode {
    
    /** the direct descendants of this object */
    private List<DatabaseObjectNode> children;
    
    /** Creates a new instance of DatabaseSchemaNode */
    public DatabaseSchemaNode(DatabaseSchema schema) {
        super(schema);
    }
    
    /**
     * Returns the children associated with this node.
     *
     * @return a list of children for this node
     */
    @SuppressWarnings("rawtypes")
    public List<DatabaseObjectNode> getChildObjects() throws DataSourceException {

        if (children != null) {
            return children;
        }
        
        DatabaseSchema schema = (DatabaseSchema)getDatabaseObject();
        
        // check for meta tags
        List _children = schema.getMetaObjects();
        if (_children != null && !_children.isEmpty()) {
            
            int count = _children.size();
            children = new ArrayList<DatabaseObjectNode>(count);
            for (int i = 0; i < count; i++) {
                DatabaseMetaTag metaTag = (DatabaseMetaTag)_children.get(i);
                children.add(new DatabaseMetaTagNode(metaTag));
            }
            return children;
        }

        return null;
    }

    /**
     * Override to return true.
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
        return false;
    }

    /**
     * Clears out the children of this node.
     */
    public void reset() {
        super.reset();
        children = null;
    }

}



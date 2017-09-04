/*
 * DatabaseMetaTagNode.java
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
import org.executequery.databaseobjects.NamedObject;
import org.underworldlabs.jdbc.DataSourceException;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class DatabaseMetaTagNode extends DatabaseObjectNode {
    
    /** Creates a new instance of DatabaseMetaTagNode */
    public DatabaseMetaTagNode(DatabaseMetaTag metaTag) {
        super(metaTag);
    }

    @Override
    public List<DatabaseObjectNode> getChildObjects() throws DataSourceException {

        if (((DatabaseMetaTag) getDatabaseObject()).getSubType() == NamedObject.TABLE) {

            List<NamedObject> values = getDatabaseObject().getObjects();
            if (values != null) {

                List<DatabaseObjectNode> nodes = new ArrayList<DatabaseObjectNode>();
                for (NamedObject namedObject : values) {
                
                    nodes.add(new DatabaseTableNode(namedObject));
                }
                
                return nodes;
            }
            
        }

        return super.getChildObjects();
    }
    
}



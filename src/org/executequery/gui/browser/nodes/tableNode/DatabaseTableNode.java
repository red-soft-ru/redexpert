/*
 * DatabaseTableNode.java
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

package org.executequery.gui.browser.nodes.tableNode;

import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.underworldlabs.jdbc.DataSourceException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseTableNode extends DatabaseObjectNode {
    private List<DatabaseObjectNode> childrenList;

    public DatabaseTableNode(NamedObject databaseObject) {
        super(databaseObject);
    }

    @Override
    public List<DatabaseObjectNode> getChildObjects() throws DataSourceException {

        if (childrenList == null) {
            childrenList = new ArrayList<>();
            childrenList.addAll(buildObjectNodes());
        }

        return childrenList;
    }

    private List<DatabaseObjectNode> buildObjectNodes() {
        DatabaseTable databaseTable = (DatabaseTable) getDatabaseObject();

        return Arrays.asList(
                new ColumnFolderNode(databaseTable),
                new PrimaryKeysFolderNode(databaseTable),
                new ForeignKeysFolderNode(databaseTable),
                new IndexesFolderNode(databaseTable),
                new TriggersFolderNode(databaseTable)
        );
    }

    @Override
    public void reset() {
        super.reset();
        childrenList = null;
    }

}

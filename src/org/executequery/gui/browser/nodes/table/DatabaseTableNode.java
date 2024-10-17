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

package org.executequery.gui.browser.nodes.table;

import org.executequery.EventMediator;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.event.DatabaseTableEvent;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.underworldlabs.jdbc.DataSourceException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseTableNode extends DatabaseObjectNode {
    private List<DatabaseObjectNode> childrenList;
    private final boolean displayTableCatalog;

    public DatabaseTableNode(NamedObject databaseObject, boolean displayTableCatalog) {
        super(databaseObject);
        this.displayTableCatalog = displayTableCatalog;
    }

    @Override
    public List<DatabaseObjectNode> getChildObjects() throws DataSourceException {

        if (!displayTableCatalog)
            return super.getChildObjects();

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
        EventMediator.fireEvent(new DatabaseTableEvent(this, DatabaseTableEvent.PROCESS_TABLE_RESET));
    }

}

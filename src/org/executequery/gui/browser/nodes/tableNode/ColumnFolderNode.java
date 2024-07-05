package org.executequery.gui.browser.nodes.tableNode;

import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.underworldlabs.jdbc.DataSourceException;

import java.util.List;

class ColumnFolderNode extends TableFolderNode {

    public ColumnFolderNode(DatabaseTable databaseTable) {
        super(databaseTable);
    }

    @Override
    public List<DatabaseObjectNode> getChildObjects() throws DataSourceException {
        return buildObjectNodes(databaseTable.getObjects());
    }

    @Override
    public String getName() {
        return bundleString("columns");
    }

    @Override
    public int getType() {
        return NamedObject.COLUMNS_FOLDER_NODE;
    }

}

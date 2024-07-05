package org.executequery.gui.browser.nodes.tableNode;

import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.underworldlabs.jdbc.DataSourceException;

import java.util.List;

class IndexesFolderNode extends TableFolderNode {

    public IndexesFolderNode(DatabaseTable databaseTable) {
        super(databaseTable);
    }

    @Override
    public List<DatabaseObjectNode> getChildObjects() throws DataSourceException {
        return buildObjectNodes(databaseTable.getIndexes());
    }

    @Override
    public String getName() {
        return bundleString("indexes");
    }

    @Override
    public int getType() {
        return NamedObject.INDEXES_FOLDER_NODE;
    }

}

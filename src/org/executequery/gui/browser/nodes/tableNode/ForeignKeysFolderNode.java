package org.executequery.gui.browser.nodes.tableNode;

import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.underworldlabs.jdbc.DataSourceException;

import java.util.List;

class ForeignKeysFolderNode extends TableFolderNode {

    public ForeignKeysFolderNode(DatabaseTable databaseTable) {
        super(databaseTable);
    }

    @Override
    public List<DatabaseObjectNode> getChildObjects() throws DataSourceException {
        return buildObjectNodes(databaseTable.getForeignKeys());
    }

    @Override
    public String getName() {
        return bundleString("foreign-keys");
    }

    @Override
    public int getType() {
        return NamedObject.FOREIGN_KEYS_FOLDER_NODE;
    }

}

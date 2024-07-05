package org.executequery.gui.browser.nodes.tableNode;

import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.underworldlabs.jdbc.DataSourceException;

import java.util.List;

class PrimaryKeysFolderNode extends TableFolderNode {

    public PrimaryKeysFolderNode(DatabaseTable databaseTable) {
        super(databaseTable);
    }

    @Override
    public List<DatabaseObjectNode> getChildObjects() throws DataSourceException {
        return buildObjectNodes(databaseTable.getPrimaryKeys());
    }

    @Override
    public String getName() {
        return bundleString("primary-keys");
    }

    @Override
    public int getType() {
        return NamedObject.PRIMARY_KEYS_FOLDER_NODE;
    }

}

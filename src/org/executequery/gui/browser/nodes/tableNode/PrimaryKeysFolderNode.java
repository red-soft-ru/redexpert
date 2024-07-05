package org.executequery.gui.browser.nodes.tableNode;

import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;

class PrimaryKeysFolderNode extends TableFolderNode {

    public PrimaryKeysFolderNode(DatabaseTable databaseTable) {
        super(databaseTable);
    }

    @Override
    protected void buildObjectNodes() {
        buildObjectNodes(databaseTable.getPrimaryKeys());
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

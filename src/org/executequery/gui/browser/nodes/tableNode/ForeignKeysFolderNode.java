package org.executequery.gui.browser.nodes.tableNode;

import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;

class ForeignKeysFolderNode extends TableFolderNode {

    public ForeignKeysFolderNode(DatabaseTable databaseTable) {
        super(databaseTable);
    }

    @Override
    protected void buildObjectNodes() {
        buildObjectNodes(databaseTable.getForeignKeys());
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

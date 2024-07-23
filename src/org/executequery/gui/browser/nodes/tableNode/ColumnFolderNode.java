package org.executequery.gui.browser.nodes.tableNode;

import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;

class ColumnFolderNode extends TableFolderNode {

    public ColumnFolderNode(DatabaseTable databaseTable) {
        super(databaseTable);
    }

    @Override
    protected void buildObjectNodes() {
        buildObjectNodes(databaseTable.getObjects());
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

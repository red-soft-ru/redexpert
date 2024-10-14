package org.executequery.gui.browser.nodes.tableNode;

import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class ColumnFolderNode extends TableFolderNode {

    public ColumnFolderNode(DatabaseTable databaseTable) {
        super(databaseTable);
    }

    @Override
    protected List<DatabaseObjectNode> buildObjectNodes() {

        List<NamedObject> values = databaseTable.getObjects();
        if (values == null)
            return new ArrayList<>();

        return values.stream().map(DatabaseObjectNode::new).collect(Collectors.toList());
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

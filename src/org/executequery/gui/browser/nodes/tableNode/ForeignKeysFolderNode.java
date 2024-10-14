package org.executequery.gui.browser.nodes.tableNode;

import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.ColumnConstraint;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class ForeignKeysFolderNode extends TableFolderNode {

    public ForeignKeysFolderNode(DatabaseTable databaseTable) {
        super(databaseTable);
    }

    @Override
    protected List<DatabaseObjectNode> buildObjectNodes() {

        List<ColumnConstraint> values = databaseTable.getForeignKeys();
        if (values == null)
            return new ArrayList<>();

        return values.stream().map(DatabaseObjectNode::new).collect(Collectors.toList());
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

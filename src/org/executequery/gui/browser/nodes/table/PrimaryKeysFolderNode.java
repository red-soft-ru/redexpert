package org.executequery.gui.browser.nodes.table;

import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.ColumnConstraint;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class PrimaryKeysFolderNode extends TableFolderNode {

    public PrimaryKeysFolderNode(DatabaseTable databaseTable) {
        super(databaseTable);
    }

    @Override
    protected List<DatabaseObjectNode> buildObjectNodes() {

        DatabaseTable databaseTable = getDatabaseObject();
        if (databaseTable == null)
            return new ArrayList<>();

        List<ColumnConstraint> values = databaseTable.getPrimaryKeys();
        if (values == null)
            return new ArrayList<>();

        return values.stream().map(DatabaseObjectNode::new).collect(Collectors.toList());
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

package org.executequery.gui.browser.nodes.tableNode;

import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.localization.Bundles;

import java.util.ArrayList;
import java.util.List;

abstract class TableFolderNode extends DatabaseObjectNode {
    protected DatabaseTable databaseTable;

    protected TableFolderNode(DatabaseTable databaseTable) {
        this.databaseTable = databaseTable;
    }

    protected List<DatabaseObjectNode> buildObjectNodes(List<? extends NamedObject> values) {

        if (values == null)
            return null;

        List<DatabaseObjectNode> nodes = new ArrayList<>();
        for (NamedObject value : values)
            nodes.add(new DatabaseObjectNode(value));

        return nodes;
    }

    @Override
    public NamedObject getDatabaseObject() {
        return databaseTable;
    }

    @Override
    public String getShortName() {
        return databaseTable.getShortName();
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    protected String bundleString(String key) {
        return Bundles.get(DatabaseTableNode.class, key);
    }

}

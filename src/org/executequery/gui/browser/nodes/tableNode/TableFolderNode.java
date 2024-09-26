package org.executequery.gui.browser.nodes.tableNode;

import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.localization.Bundles;
import org.underworldlabs.jdbc.DataSourceException;

import java.util.ArrayList;
import java.util.List;

public abstract class TableFolderNode extends DatabaseObjectNode {

    protected DatabaseTable databaseTable;
    protected List<DatabaseObjectNode> children;

    protected TableFolderNode(DatabaseTable databaseTable) {
        super(databaseTable);
        this.databaseTable = databaseTable;
        this.children = new ArrayList<>();
        buildObjectNodes();
    }

    protected abstract void buildObjectNodes();

    protected void buildObjectNodes(List<? extends NamedObject> values) {
        if (values != null)
            for (NamedObject value : values)
                children.add(new DatabaseObjectNode(value));
    }

    @Override
    public List<DatabaseObjectNode> getChildObjects() throws DataSourceException {
        return children;
    }

    @Override
    public boolean isLeaf() {
        return children.isEmpty();
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
        return getName() + String.format(" (%d)", children.size());
    }

    @Override
    protected String bundleString(String key) {
        return Bundles.get(DatabaseTableNode.class, key);
    }

}

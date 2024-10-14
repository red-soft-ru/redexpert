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
    private List<DatabaseObjectNode> childrenList;

    protected TableFolderNode(DatabaseTable databaseTable) {
        super(databaseTable);
        this.databaseTable = databaseTable;
        getChildObjects();
    }

    protected abstract List<DatabaseObjectNode> buildObjectNodes();

    private boolean hasChildrenList() {
        return childrenList != null;
    }

    @Override
    public List<DatabaseObjectNode> getChildObjects() throws DataSourceException {

        if (childrenList == null) {
            childrenList = new ArrayList<>();
            childrenList.addAll(buildObjectNodes());
        }

        return childrenList;
    }

    @Override
    public boolean isLeaf() {
        return hasChildrenList() && childrenList.isEmpty();
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
        return String.format("%s (%d)", getName(), (hasChildrenList() ? childrenList.size() : 0));
    }

    @Override
    public void reset() {
        super.reset();
        childrenList = null;
    }

    @Override
    protected String bundleString(String key) {
        return Bundles.get(DatabaseTableNode.class, key);
    }

}

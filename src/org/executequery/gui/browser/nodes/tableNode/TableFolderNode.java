package org.executequery.gui.browser.nodes.tableNode;

import org.executequery.EventMediator;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.event.DatabaseTableEvent;
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

    @Override
    public List<DatabaseObjectNode> getChildObjects() throws DataSourceException {

        if (childrenList == null) {
            childrenList = new ArrayList<>();
            childrenList.addAll(buildObjectNodes());
        }

        return childrenList;
    }

    @Override
    public boolean isTableFolder() {
        return true;
    }

    @Override
    public boolean isLeaf() {
        getChildObjects();
        return childrenList.isEmpty();
    }

    @Override
    public String getShortName() {
        return databaseTable.getShortName();
    }

    @Override
    public String getDisplayName() {
        getChildObjects();
        return String.format("%s (%d)", getName(), childrenList.size());
    }

    @Override
    public void reset() {
        super.reset();
        childrenList = null;
        EventMediator.fireEvent(new DatabaseTableEvent(this, DatabaseTableEvent.PROCESS_TABLE_RESET));
    }

    @Override
    protected String bundleString(String key) {
        return Bundles.get(DatabaseTableNode.class, key);
    }

}

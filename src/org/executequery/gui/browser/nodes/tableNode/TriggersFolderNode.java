package org.executequery.gui.browser.nodes.tableNode;

import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseMetaTag;
import org.executequery.databaseobjects.impl.DefaultDatabaseTrigger;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.underworldlabs.jdbc.DataSourceException;

import java.util.List;

import static org.executequery.databaseobjects.NamedObject.*;

class TriggersFolderNode extends TableFolderNode {

    public TriggersFolderNode(DatabaseTable databaseTable) {
        super(databaseTable);
    }

    @Override
    public List<DatabaseObjectNode> getChildObjects() throws DataSourceException {
        DefaultDatabaseMetaTag metaTag = new DefaultDatabaseMetaTag(databaseTable.getHost(), META_TYPES[TRIGGER]);

        List<DefaultDatabaseTrigger> indices = databaseTable.getTriggers();
        indices.forEach(index -> index.setParent(metaTag));
        return buildObjectNodes(indices);
    }

    @Override
    public String getName() {
        return bundleString("triggers");
    }

    @Override
    public int getType() {
        return NamedObject.TRIGGERS_FOLDER_NODE;
    }

}

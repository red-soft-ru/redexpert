package org.executequery.gui.browser.nodes.tableNode;

import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseMetaTag;
import org.executequery.databaseobjects.impl.DefaultDatabaseTrigger;

import java.util.List;

import static org.executequery.databaseobjects.NamedObject.*;

class TriggersFolderNode extends TableFolderNode {

    public TriggersFolderNode(DatabaseTable databaseTable) {
        super(databaseTable);
    }

    @Override
    protected void buildObjectNodes() {
        DefaultDatabaseMetaTag metaTag = new DefaultDatabaseMetaTag(databaseTable.getHost(), META_TYPES[TRIGGER]);

        List<DefaultDatabaseTrigger> indices = databaseTable.getTriggers();
        indices.forEach(index -> index.setParent(metaTag));
        buildObjectNodes(indices);
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

package org.executequery.gui.browser.nodes.tableNode;

import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseMetaTag;
import org.executequery.databaseobjects.impl.DefaultDatabaseTrigger;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.executequery.databaseobjects.NamedObject.*;

class TriggersFolderNode extends TableFolderNode {

    public TriggersFolderNode(DatabaseTable databaseTable) {
        super(databaseTable);
    }

    @Override
    protected List<DatabaseObjectNode> buildObjectNodes() {

        DatabaseTable databaseTable = getDatabaseObject();
        if (databaseTable == null)
            return new ArrayList<>();

        List<DefaultDatabaseTrigger> values = databaseTable.getTriggers();
        if (values == null)
            return new ArrayList<>();

        DefaultDatabaseMetaTag metaTag = new DefaultDatabaseMetaTag(databaseTable.getHost(), META_TYPES[TRIGGER]);
        values.stream().filter(Objects::nonNull).forEach(val -> val.setParent(metaTag));

        return values.stream().map(DatabaseObjectNode::new).collect(Collectors.toList());
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

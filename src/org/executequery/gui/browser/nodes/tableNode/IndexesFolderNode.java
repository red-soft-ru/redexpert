package org.executequery.gui.browser.nodes.tableNode;

import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseIndex;
import org.executequery.databaseobjects.impl.DefaultDatabaseMetaTag;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.executequery.databaseobjects.NamedObject.INDEX;
import static org.executequery.databaseobjects.NamedObject.META_TYPES;

class IndexesFolderNode extends TableFolderNode {

    public IndexesFolderNode(DatabaseTable databaseTable) {
        super(databaseTable);
    }

    @Override
    protected List<DatabaseObjectNode> buildObjectNodes() {

        DatabaseTable databaseTable = getDatabaseObject();
        if (databaseTable == null)
            return new ArrayList<>();

        List<DefaultDatabaseIndex> values = databaseTable.getIndexes();
        if (values == null)
            return new ArrayList<>();

        DefaultDatabaseMetaTag metaTag = new DefaultDatabaseMetaTag(databaseTable.getHost(), META_TYPES[INDEX]);
        values.stream().filter(Objects::nonNull).forEach(val -> val.setParent(metaTag));

        return values.stream().map(DatabaseObjectNode::new).collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return bundleString("indexes");
    }

    @Override
    public int getType() {
        return NamedObject.INDEXES_FOLDER_NODE;
    }

}

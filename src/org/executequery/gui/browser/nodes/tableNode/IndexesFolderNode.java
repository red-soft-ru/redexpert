package org.executequery.gui.browser.nodes.tableNode;

import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseIndex;
import org.executequery.databaseobjects.impl.DefaultDatabaseMetaTag;

import java.util.List;

import static org.executequery.databaseobjects.NamedObject.INDEX;
import static org.executequery.databaseobjects.NamedObject.META_TYPES;

class IndexesFolderNode extends TableFolderNode {

    public IndexesFolderNode(DatabaseTable databaseTable) {
        super(databaseTable);
    }

    @Override
    protected void buildObjectNodes() {
        DefaultDatabaseMetaTag metaTag = new DefaultDatabaseMetaTag(databaseTable.getHost(), META_TYPES[INDEX]);

        List<DefaultDatabaseIndex> indices = databaseTable.getIndexes();
        indices.forEach(index -> index.setParent(metaTag));
        buildObjectNodes(indices);
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

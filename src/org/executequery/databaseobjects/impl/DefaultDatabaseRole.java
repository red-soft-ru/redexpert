package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.NamedObject;

/**
 * Created by vasiliy on 02.02.17.
 */
public class DefaultDatabaseRole extends DefaultDatabaseExecutable {
    public String name;

    public DefaultDatabaseRole(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
    }

    @Override
    public int getType() {
        if (isSystem())
            return NamedObject.SYSTEM_ROLE;
        return NamedObject.ROLE;
    }

    @Override
    public String getMetaDataKey() {
        if (isSystem())
            return META_TYPES[SYSTEM_ROLE];
        return META_TYPES[ROLE];
    }
}
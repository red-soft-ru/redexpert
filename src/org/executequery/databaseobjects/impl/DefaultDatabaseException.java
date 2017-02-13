package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;

/**
 * Created by vasiliy on 13.02.17.
 */
public class DefaultDatabaseException extends DefaultDatabaseExecutable
        implements DatabaseProcedure {
    /**
     * Creates a new instance.
     */
    public DefaultDatabaseException() {}

    /**
     * Creates a new instance.
     */
    public DefaultDatabaseException(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
    }

    /**
     * Creates a new instance with
     * the specified values.
     */
    public DefaultDatabaseException(String schema, String name) {
        setName(name);
        setSchemaName(schema);
    }

    public int getType() {
        return EXCEPTION;
    }
}

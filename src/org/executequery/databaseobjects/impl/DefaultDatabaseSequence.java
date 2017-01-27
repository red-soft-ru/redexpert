package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;

/**
 * Created by vasiliy on 27.01.17.
 */
public class DefaultDatabaseSequence extends DefaultDatabaseExecutable
        implements DatabaseProcedure {
    
    /**
     * Creates a new instance.
     */
    public DefaultDatabaseSequence() {}

    /**
     * Creates a new instance.
     */
    public DefaultDatabaseSequence(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
    }

    /**
     * Creates a new instance with
     * the specified values.
     */
    public DefaultDatabaseSequence(String schema, String name) {
        setName(name);
        setSchemaName(schema);
    }

    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    public int getType() {
        return SEQUENCE;
    }

    /**
     * Returns the meta data key name of this object.
     *
     * @return the meta data key name.
     */
    public String getMetaDataKey() {
        return META_TYPES[SEQUENCE];
    }

}

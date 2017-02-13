package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;

/**
 * Created by vasiliy on 13.02.17.
 */
public class DefaultDatabaseUDF extends DefaultDatabaseExecutable
        implements DatabaseProcedure {

    private String udfName;

    /**
     * Creates a new instance.
     */
    public DefaultDatabaseUDF() {}

    /**
     * Creates a new instance.
     */
    public DefaultDatabaseUDF(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
    }

    /**
     * Creates a new instance with
     * the specified values.
     */
    public DefaultDatabaseUDF(String schema, String name) {
        setName(name);
        setSchemaName(schema);
    }

    public int getType() {
        return UDF;
    }

    public String getID() {
        return udfName;
    }

    public void setUdfName(String udfName) {
        this.udfName = udfName;
    }

    public String getCreateSQLText() {
        return "";
    }
}
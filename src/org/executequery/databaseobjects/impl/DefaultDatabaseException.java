package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;

/**
 * Created by vasiliy on 13.02.17.
 */
public class DefaultDatabaseException extends DefaultDatabaseExecutable
        implements DatabaseProcedure {

    private String exceptionID;
    private String exceptionText;

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

    public String getID() {
        return exceptionID;
    }

    public void setExceptionID(String exceptionID) {
        this.exceptionID = exceptionID;
    }

    public String getExceptionText() {
        return exceptionText;
    }

    public void setExceptionText(String exceptionText) {
        this.exceptionText = exceptionText;
    }

    public String getCreateSQLText() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE EXCEPTION \n");
        sb.append(getName());
        sb.append("\n");
        sb.append("'");
        sb.append(getExceptionText());
        sb.append("';");

        return sb.toString();
    }
}

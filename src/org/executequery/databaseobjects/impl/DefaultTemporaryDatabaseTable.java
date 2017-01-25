package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseObject;
import org.underworldlabs.jdbc.DataSourceException;


/**
 * Created by vasiliy on 25.01.17.
 */
public class DefaultTemporaryDatabaseTable extends DefaultDatabaseObject {

    public DefaultTemporaryDatabaseTable(DatabaseObject object) {

        this(object.getHost());

        setCatalogName(object.getCatalogName());
        setSchemaName(object.getSchemaName());
        setName(object.getName());
        setRemarks(object.getRemarks());
    }

    public DefaultTemporaryDatabaseTable(DatabaseHost host) {

        super(host, "GLOBAL TEMPORARY");
    }

    public String getCreateSQLText() throws DataSourceException {

        return "";
    }

    @Override
    public boolean hasSQLDefinition() {

        return true;
    }
}
package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.NamedObject;
import org.underworldlabs.jdbc.DataSourceException;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by vasiliy on 02.02.17.
 */
public class DefaultDatabaseRole extends AbstractDatabaseObject {
    public String name;

    public DefaultDatabaseRole(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
    }

    @Override
    public String getCreateFullSQLText() throws DataSourceException {
        return null;
    }

    @Override
    public String getCreateSQL() throws DataSourceException {
        return null;
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return null;
    }

    @Override
    public String getAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {
        return null;
    }

    @Override
    public String getFillSQL() throws DataSourceException {
        return null;
    }

    @Override
    protected String queryForInfo() {
        return null;
    }

    @Override
    protected void setInfoFromResultSet(ResultSet rs) throws SQLException {

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

    @Override
    public boolean allowsChildren() {
        return false;
    }
}
package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.NamedObject;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.SQLUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

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
        return SQLUtils.generateCreateRole(getName());
    }

    @Override
    public String getCreateSQL() throws DataSourceException {
        return getCreateFullSQLText();
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropRequest("ROLE", getName());
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
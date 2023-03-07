package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.NamedObject;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.SQLUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by vasiliy on 02.02.17.
 */
public class DefaultDatabaseRole extends DefaultDatabaseExecutable {
    public String name;

    public DefaultDatabaseRole(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
    }

    @Override
    public String getCreateSQLText() throws DataSourceException {
        return SQLUtils.generateCreateRole(getName());
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("ROLE", getName());
    }

    @Override
    protected String queryForInfo() {

        String query = "select r.rdb$description as DESCRIPTION\n" +
                "from rdb$roles r\n" +
                "where r.rdb$role_name = ?'";

        return query;
    }

    @Override
    protected void setInfoFromResultSet(ResultSet rs) throws SQLException {
        try {
            if (rs.next())
                setRemarks(getFromResultSet(rs, "DESCRIPTION"));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
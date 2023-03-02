package org.executequery.databaseobjects.impl;

import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.SQLUtils;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by vasiliy on 25.01.17.
 */
public class DefaultTemporaryDatabaseTable extends DefaultDatabaseTable {


    public DefaultTemporaryDatabaseTable(DatabaseObject object) {
        super(object, NamedObject.META_TYPES[NamedObject.GLOBAL_TEMPORARY]);
    }

    public DefaultTemporaryDatabaseTable(DatabaseHost host) {
        super(host, NamedObject.META_TYPES[NamedObject.GLOBAL_TEMPORARY]);
    }

    @Override
    public String getCreateSQLText() throws DataSourceException {

        updateListCD();
        updateListCC();

        return SQLUtils.generateCreateTable(
                getName(), listCD, listCC, true, true, true, true, true,
                getTypeTemporary(), getExternalFile(), getAdapter(), getSqlSecurity(), getTablespace(), getRemarks());
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("TABLE", getName());
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {
        return null;
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) {
        return null;
    }

    private String getTypeTemporary() {

        DefaultStatementExecutor querySender = new DefaultStatementExecutor();
        querySender.setDatabaseConnection(getHost().getDatabaseConnection());
        int type = -1;

        try {
            String statement = "SELECT RDB$RELATION_TYPE FROM RDB$RELATIONS R WHERE R.RDB$RELATION_NAME = '%s'";
            SqlStatementResult result = querySender.getResultSet(String.format(statement, getName()));

            if (result.isException())
                throw result.getSqlException();

            ResultSet resultSet = result.getResultSet();
            resultSet.next();
            type = resultSet.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            querySender.releaseResources();
        }

        String typeTemporary = "";
        if (type == 4)
            typeTemporary = " ON COMMIT PRESERVE ROWS";
        else if (type == 5)
            typeTemporary = " ON COMMIT DELETE ROWS";

        return typeTemporary;
    }

    @Override
    public int getType() {
        return GLOBAL_TEMPORARY;
    }

    /**
     * Returns the metadata key name of this object.
     *
     * @return the metadata key name.
     */
    @Override
    public String getMetaDataKey() {
        return NamedObject.META_TYPES[NamedObject.GLOBAL_TEMPORARY];
    }

    @Override
    public boolean hasSQLDefinition() {
        return true;
    }

}
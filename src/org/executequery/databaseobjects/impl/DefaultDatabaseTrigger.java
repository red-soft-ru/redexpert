package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;
import org.firebirdsql.jdbc.FBDatabaseMetaData;
import org.firebirdsql.jdbc.FirebirdDatabaseMetaData;
import org.underworldlabs.jdbc.DataSourceException;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by vasiliy on 26.01.17.
 */
public class DefaultDatabaseTrigger extends DefaultDatabaseExecutable
        implements DatabaseProcedure {

    private String triggerSourceCode;
    private boolean triggerActive;
    private String tableName;

    /**
     * Creates a new instance.
     */
    public DefaultDatabaseTrigger() {}

    /**
     * Creates a new instance.
     */
    public DefaultDatabaseTrigger(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
    }

    /**
     * Creates a new instance with
     * the specified values.
     */
    public DefaultDatabaseTrigger(String schema, String name) {
        setName(name);
        setSchemaName(schema);
    }

    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    public int getType() {
        return TRIGGER;
    }

    /**
     * Returns the meta data key name of this object.
     *
     * @return the meta data key name.
     */
    public String getMetaDataKey() {
        return META_TYPES[TRIGGER];
    }

    public String getTriggerSourceCode() {
        if (!isMarkedForReload() && triggerSourceCode != null) {

            return triggerSourceCode;
        }

        triggerSourceCode = "";

        try {

            DatabaseMetaData dmd = getMetaTagParent().getHost().getDatabaseMetaData();

            String _catalog = getCatalogName();
            String _schema = getSchemaName();


            if (dmd instanceof FBDatabaseMetaData) {
                FirebirdDatabaseMetaData fbMetaData = (FirebirdDatabaseMetaData)dmd;
                triggerSourceCode = fbMetaData.getTriggerSourceCode(getName());
            }

            return triggerSourceCode;

        } catch (SQLException e) {

            throw new DataSourceException(e);

        } finally {

            setMarkedForReload(false);
        }
    }

    public boolean isTriggerActive() {

        Statement statement = null;

        try {

            DatabaseMetaData dmd = getMetaTagParent().getHost().getDatabaseMetaData();

            String _catalog = getCatalogName();
            String _schema = getSchemaName();


            if (dmd instanceof FBDatabaseMetaData) {
                statement = dmd.getConnection().createStatement();
                ResultSet rs = statement.executeQuery("SELECT R.RDB$TRIGGER_INACTIVE \n" +
                        " FROM RDB$TRIGGERS R \n" +
                        " WHERE R.RDB$TRIGGER_NAME = '" + getName() + "'");

                if (rs.next())
                    triggerActive = rs.getInt(1) == 0 ? true : false;
            }

            return triggerActive;

        } catch (SQLException e) {

            throw new DataSourceException(e);

        } finally {

            if (statement != null)
                releaseResources(statement);

            setMarkedForReload(false);
        }
    }

    public String getTriggerTableName() {
        if (!isMarkedForReload() && tableName != null) {

            return tableName;
        }

        Statement statement = null;

        tableName = "";

        try {

            DatabaseMetaData dmd = getMetaTagParent().getHost().getDatabaseMetaData();

            String _catalog = getCatalogName();
            String _schema = getSchemaName();

            if (dmd instanceof FBDatabaseMetaData) {
                statement = dmd.getConnection().createStatement();
                ResultSet rs = statement.executeQuery("SELECT R.RDB$RELATION_NAME \n" +
                        " FROM RDB$TRIGGERS R \n" +
                        " WHERE R.RDB$TRIGGER_NAME = '" + getName() + "'");

                if (rs.next())
                    tableName = rs.getString(1);
            }
            return tableName;

        } catch (SQLException e) {

            throw new DataSourceException(e);

        } finally {

            setMarkedForReload(false);
        }
    }
}

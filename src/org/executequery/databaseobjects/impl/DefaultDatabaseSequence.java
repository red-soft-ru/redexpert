package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;
import org.executequery.datasource.ConnectionManager;
import org.underworldlabs.jdbc.DataSourceException;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by vasiliy on 27.01.17.
 */
public class DefaultDatabaseSequence extends DefaultDatabaseExecutable
        implements DatabaseProcedure {

    private long value = -1;
    private String description;

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

    public long getSequenceValue() {

        Statement statement = null;

        if (!isMarkedForReload() && value != -1) {

            return value;
        }

        try {

            DatabaseMetaData dmd = getMetaTagParent().getHost().getDatabaseMetaData();

            String _catalog = getCatalogName();
            String _schema = getSchemaName();

            if (ConnectionManager.realConnection(dmd).getClass().getName().contains("FBConnection")) {

                statement = dmd.getConnection().createStatement();
                ResultSet rs = statement.executeQuery("select gen_id(" + getName() + ", 0) from rdb$database");

                if (rs.next())
                    value = rs.getLong(1);
            }

            return value;

        } catch (SQLException e) {

            throw new DataSourceException(e);

        } finally {

            setMarkedForReload(false);
        }
    }

    @Override
    public String getDescription() {

        Statement statement = null;

        if (!isMarkedForReload() && description != null) {

            return description;
        }

        try {

            DatabaseMetaData dmd = getMetaTagParent().getHost().getDatabaseMetaData();

            String _catalog = getCatalogName();
            String _schema = getSchemaName();

            if (ConnectionManager.realConnection(dmd).getClass().getName().contains("FBConnection")) {

                statement = dmd.getConnection().createStatement();
                ResultSet rs = statement.executeQuery("select r.rdb$description\n" +
                        "from rdb$generators r\n" +
                        "where\n" +
                        "trim(r.rdb$generator_name)='" + getName() + "'");

                if (rs.next())
                    description = rs.getString(1);
            }

            return description;

        } catch (SQLException e) {

            throw new DataSourceException(e);

        } finally {

            setMarkedForReload(false);
        }
    }

    @Override
    public String getCreateSQLText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Create sequence ");
        sb.append(getName());
        sb.append(";");

        return sb.toString();
    }


    public String getAlterSQLText() {
        StringBuilder sb = new StringBuilder();

        sb.append("Alter sequence ");
        sb.append(getName());
        sb.append(" restart with ");
        sb.append(getSequenceValue());
        sb.append(";");

        return sb.toString();
    }
}

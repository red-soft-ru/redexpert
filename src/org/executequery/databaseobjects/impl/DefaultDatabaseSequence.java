package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;
import org.firebirdsql.jdbc.FBDatabaseMetaData;
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

            if (dmd instanceof FBDatabaseMetaData) {

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

}

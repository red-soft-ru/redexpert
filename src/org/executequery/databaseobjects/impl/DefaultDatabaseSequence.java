package org.executequery.databaseobjects.impl;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;
import org.executequery.datasource.ConnectionManager;
import org.executequery.log.Log;
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
    private Integer increment;

    /**
     * Creates a new instance.
     */
    public DefaultDatabaseSequence() {
    }

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
            if (statement != null)
                try {
                    if (!statement.isClosed())
                        statement.close();
                } catch (SQLException e) {
                    Log.error("Error close statement in method getSequenceValue in class DefaultDatabaseSequence", e);
                }

            setMarkedForReload(false);
        }
    }

    public int getIncrement() {

        Statement statement = null;

        if (!isMarkedForReload() && increment != null) {

            return increment;
        }

        try {

            DatabaseMetaData dmd = getMetaTagParent().getHost().getDatabaseMetaData();

            String _catalog = getCatalogName();
            String _schema = getSchemaName();

            if (ConnectionManager.realConnection(dmd).getClass().getName().contains("FBConnection")) {

                statement = dmd.getConnection().createStatement();
                ResultSet rs = statement.executeQuery("select r.rdb$generator_increment\n" +
                        "from rdb$generators r\n" +
                        "where\n" +
                        "trim(r.rdb$generator_name)='" + getName() + "'");

                if (rs.next())
                    increment = rs.getInt(1);
            }

            return increment;

        } catch (SQLException e) {

            throw new DataSourceException(e);

        } finally {
            if (statement != null)
                try {
                    if (!statement.isClosed())
                        statement.close();
                } catch (SQLException e) {
                    Log.error("Error close statement in method getIncrement in class DefaultDatabaseSequence", e);
                }
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

    @Override
    protected void getObjectInfo() {
        super.getObjectInfo();
        DefaultStatementExecutor querySender = new DefaultStatementExecutor(getHost().getDatabaseConnection());
        try {
            String query = queryForInfo();
            ResultSet rs = querySender.getResultSet(query).getResultSet();
            setInfoFromResultSet(rs);
        } catch (SQLException e) {
            GUIUtilities.displayExceptionErrorDialog("Error get info about" + getName(), e);
        } finally {
            querySender.releaseResources();
            setMarkedForReload(false);
        }
    }

    protected void setInfoFromResultSet(ResultSet rs) throws SQLException {
        if (rs.next())
            setRemarks(rs.getString(1));
    }

    protected String queryForInfo() {
        return "select rdb$description from rdb$generators where \n" +
                "     rdb$generator_name='" + getName().trim() + "'";
    }
}

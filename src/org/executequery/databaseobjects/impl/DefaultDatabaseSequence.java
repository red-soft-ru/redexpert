package org.executequery.databaseobjects.impl;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.browser.comparer.Comparer;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by vasiliy on 27.01.17.
 */
public class DefaultDatabaseSequence extends AbstractDatabaseObject {

    private long currentValue = -1;
    private long firstValue = -1;
    private Integer increment;


    /**
     * Creates a new instance.
     */
    public DefaultDatabaseSequence(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
    }


    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    public int getType() {
        if (isSystem())
            return SYSTEM_SEQUENCE;
        return SEQUENCE;
    }

    /**
     * Returns the metadata key name of this object.
     *
     * @return the metadata key name.
     */
    public String getMetaDataKey() {
        if (isSystem())
            return META_TYPES[SYSTEM_SEQUENCE];
        return META_TYPES[SEQUENCE];
    }

    @Override
    public boolean allowsChildren() {
        return false;
    }

    public long getSequenceCurrentValue() {

        Statement statement = null;

        if (!isMarkedForReload() && currentValue != -1)
            return currentValue;

        try {

            DatabaseMetaData dmd = getMetaTagParent().getHost().getDatabaseMetaData();

            if (ConnectionManager.realConnection(dmd).getClass().getName().contains("FBConnection")) {

                statement = dmd.getConnection().createStatement();
                ResultSet rs = statement.executeQuery("SELECT GEN_ID(" + MiscUtils.getFormattedObject(getName()) + ", 0) FROM RDB$DATABASE");

                if (rs.next())
                    currentValue = rs.getLong(1);
            }

            return currentValue;

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

    public long getSequenceFirstValue() {

        Statement statement = null;

        if (!isMarkedForReload() && firstValue != -1)
            return firstValue;

        try {

            DatabaseMetaData dmd = getMetaTagParent().getHost().getDatabaseMetaData();

            if (ConnectionManager.realConnection(dmd).getClass().getName().contains("FBConnection")) {

                statement = dmd.getConnection().createStatement();
                ResultSet rs = statement.executeQuery("select r.rdb$initial_value\n" +
                        "from rdb$generators r\n" +
                        "where\n" +
                        "trim(r.rdb$generator_name)='" + getName() + "'");

                if (rs.next())
                    firstValue = rs.getInt(1);
            }

            return firstValue;

        } catch (SQLException e) {

            throw new DataSourceException(e);

        } finally {

            if (statement != null)
                try {
                    if (!statement.isClosed())
                        statement.close();
                } catch (SQLException e) {
                    Log.error("Error close statement in method getSequenceFirstValue in class DefaultDatabaseSequence", e);
                }

            setMarkedForReload(false);
        }
    }

    public int getIncrement() {

        Statement statement = null;

        if (!isMarkedForReload() && increment != null)
            return increment;

        try {

            DatabaseMetaData dmd = getMetaTagParent().getHost().getDatabaseMetaData();

            if (getVersion() >= 3) {
                if (ConnectionManager.realConnection(dmd).getClass().getName().contains("FBConnection")) {

                    statement = dmd.getConnection().createStatement();
                    ResultSet rs = statement.executeQuery("select r.rdb$generator_increment\n" +
                            "from rdb$generators r\n" +
                            "where\n" +
                            "trim(r.rdb$generator_name)='" + getName() + "'");

                    if (rs.next())
                        increment = rs.getInt(1);
                }

            } else
                increment = 1;

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
        String query = "";
        try {
            long firstValue = (getVersion() >= 3) ? getSequenceFirstValue() : getSequenceCurrentValue();
            query = SQLUtils.generateCreateSequence(getName(), firstValue,
                    getIncrement(), getRemarks(), getVersion(), false);

        } catch (SQLException e) {
            GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e);
            e.printStackTrace();
        }

        return query;
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {
        String query = "";
        String comment = Comparer.isCommentsNeed() ? getRemarks() : null;
        try {
            query = SQLUtils.generateCreateSequence(getName(), getSequenceFirstValue(),
                    getIncrement(), comment, getVersion(), false);

        } catch (SQLException e) {
            GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e);
            e.printStackTrace();
        }

        return query;
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropRequest("SEQUENCE", getName());
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {
        DefaultDatabaseSequence comparingSequence = (DefaultDatabaseSequence) databaseObject;
        return SQLUtils.generateAlterSequence(this, comparingSequence);
    }

    @Override
    public String getFillSQL() throws DataSourceException {
        return null;
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

    int getVersion() throws SQLException {
        return getHost().getDatabaseMetaData().getDatabaseMajorVersion();
    }

}

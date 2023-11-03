package org.executequery.databaseobjects.impl;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.browser.comparer.Comparer;
import org.executequery.log.Log;
import org.executequery.sql.sqlbuilder.Field;
import org.executequery.sql.sqlbuilder.SelectBuilder;
import org.executequery.sql.sqlbuilder.Table;
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

    protected final static String CURRENT_VALUE = "CURRENT_VALUE";
    protected final static String INITIAL_VALUE = "INITIAL_VALUE";
    protected final static String GENERATOR_INCREMENT = "GENERATOR_INCREMENT";
    protected final static String DESCRIPTION = "DESCRIPTION";

    private Integer increment;
    private Long firstValue;
    private Long currentValue;

    /**
     * Creates a new instance
     */
    public DefaultDatabaseSequence(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
    }

    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    @Override
    public int getType() {
        return isSystem() ? SYSTEM_SEQUENCE : SEQUENCE;
    }

    /**
     * Returns the metadata key name of this object.
     *
     * @return the metadata key name.
     */
    @Override
    public String getMetaDataKey() {
        return isSystem() ? META_TYPES[SYSTEM_SEQUENCE] : META_TYPES[SEQUENCE];
    }

    @Override
    public boolean allowsChildren() {
        return false;
    }

    @Override
    public String getCreateSQLText() {

        String query = "";
        try {

            long firstValue = (getVersion() >= 3) ? getSequenceFirstValue() : getSequenceCurrentValue();
            query = SQLUtils.generateCreateSequence(getName(), firstValue,
                    getIncrement(), getRemarks(), getVersion(), false, getHost().getDatabaseConnection());

        } catch (SQLException e) {
            GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e);
            e.printStackTrace();
        }

        return query;
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("SEQUENCE", getName(), getHost().getDatabaseConnection());
    }

    public long getSequenceCurrentValue() {

        Statement statement = null;

        if (!isMarkedForReload() && currentValue != null)
            return currentValue;

        try {

            DatabaseMetaData dmd = getMetaTagParent().getHost().getDatabaseMetaData();
            if (ConnectionManager.realConnection(dmd).getClass().getName().contains("FBConnection")) {

                statement = dmd.getConnection().createStatement();
                ResultSet rs = statement.executeQuery("SELECT GEN_ID(" + MiscUtils.getFormattedObject(getName(), getHost().getDatabaseConnection()) + ", 0) FROM RDB$DATABASE");

                if (rs.next())
                    currentValue = rs.getLong(1);
            }

            if (currentValue == null)
                currentValue = 0L;
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
        checkOnReload(firstValue);
        return firstValue;
    }

    public int getIncrement() {
        checkOnReload(increment);
        return increment;
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {

        String query = "";
        String comment = Comparer.isCommentsNeed() ? getRemarks() : null;
        try {
            long firstValue = (getVersion() >= 3) ? getSequenceFirstValue() : getSequenceCurrentValue();
            query = SQLUtils.generateCreateSequence(getName(), firstValue,
                    getIncrement(), comment, getVersion(), false, getHost().getDatabaseConnection());

        } catch (SQLException e) {
            GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e);
            e.printStackTrace();
        }

        return query;
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {

        DefaultDatabaseSequence comparingSequence = (DefaultDatabaseSequence) databaseObject;
        int version = 0;
        try {
            version = getVersion();
        } catch (SQLException ignored) {
        }

        return SQLUtils.generateAlterSequence(this, comparingSequence, version);
    }

    @Override
    protected String getFieldName() {
        return "GENERATOR_NAME";
    }

    @Override
    protected Table getMainTable() {
        return Table.createTable("RDB$GENERATORS", "G");
    }

    @Override
    protected SelectBuilder builderCommonQuery() {
        SelectBuilder sb = SelectBuilder.createSelectBuilder(getHost().getDatabaseConnection());
        Table gens = getMainTable();
        sb.appendTable(gens);
        sb.appendField(getObjectField());
        sb.appendField(Field.createField(gens, INITIAL_VALUE).setNull(getDatabaseMajorVersion() < 3));
        sb.appendField(Field.createField(gens, GENERATOR_INCREMENT).setNull(getDatabaseMajorVersion() < 3));
        sb.appendField(Field.createField(gens, DESCRIPTION));
        sb.setOrdering(getObjectField().getFieldTable());
        return sb;
    }

    @Override
    public Object setInfoFromSingleRowResultSet(ResultSet rs, boolean first) throws SQLException {
        setRemarks(getFromResultSet(rs, DESCRIPTION));
        firstValue = rs.getLong(INITIAL_VALUE);
        increment = rs.getInt(GENERATOR_INCREMENT);
        return null;
    }

    @Override
    public void prepareLoadingInfo() {

    }

    @Override
    public void finishLoadingInfo() {

    }

    @Override
    public boolean isAnyRowsResultSet() {
        return false;
    }


    public int getVersion() throws SQLException {
        return getDatabaseMajorVersion();
    }

}

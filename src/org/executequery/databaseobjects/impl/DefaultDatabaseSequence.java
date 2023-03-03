package org.executequery.databaseobjects.impl;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.browser.comparer.Comparer;
import org.executequery.log.Log;
import org.executequery.sql.sqlbuilder.Condition;
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
    protected final static String GENERATOR_INCREMENT = "GENERATOR_INCREMENT";
    protected final static String DESCRIPTION = "DESCRIPTION";
    private Long currentValue;

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
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("SEQUENCE", getName());
    }
    private Long firstValue;

    public long getSequenceCurrentValue() {

        Statement statement = null;

        if (!isMarkedForReload() && currentValue != null)
            return currentValue;

        try {

            DatabaseMetaData dmd = getMetaTagParent().getHost().getDatabaseMetaData();

            if (ConnectionManager.realConnection(dmd).getClass().getName().contains("FBConnection")) {

                statement = dmd.getConnection().createStatement();
                ResultSet rs = statement.executeQuery("SELECT GEN_ID(" + MiscUtils.getFormattedObject(getName()) + ", 0) FROM RDB$DATABASE");

                if (rs.next())
                    currentValue = rs.getLong(1);
            }
            if (currentValue == null)
                currentValue = Long.valueOf(0);
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
                    getIncrement(), comment, getVersion(), false);

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

    protected void setInfoFromResultSet(ResultSet rs) throws SQLException {
        if (rs.next()) {
            setRemarks(getFromResultSet(rs, DESCRIPTION));
            firstValue = rs.getLong(INITIAL_VALUE);
            increment = rs.getInt(GENERATOR_INCREMENT);
        }

    }

    @Override
    protected String queryForInfo() {
        SelectBuilder sb = SelectBuilder.createSelectBuilder();
        Table gens = Table.createTable("RDB$GENERATORS", "G");
        sb.appendTable(gens);

        //sb.appendField(Field.createField().setStatement("GEN_ID(" + MiscUtils.getFormattedObject(getName()) + ", 0)").setAlias(CURRENT_VALUE));
        sb.appendField(Field.createField(gens, INITIAL_VALUE));
        sb.appendField(Field.createField(gens, GENERATOR_INCREMENT).setNull(getDatabaseMajorVersion() < 3));
        sb.appendField(Field.createField(gens, DESCRIPTION));

        sb.appendCondition(Condition.createCondition(Field.createField(gens, "GENERATOR_NAME"), "=", "?"));
        return sb.getSQLQuery();
    }

    public int getVersion() throws SQLException {
        return getDatabaseMajorVersion();
    }

}

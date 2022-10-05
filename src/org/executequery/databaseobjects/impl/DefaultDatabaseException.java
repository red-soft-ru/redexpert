package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.SQLUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by vasiliy on 13.02.17.
 */
public class DefaultDatabaseException extends AbstractDatabaseObject {

    private String exceptionID;
    private String exceptionText;

    private static final String NAME = "FIELD_NAME";
    private static final String EXCEPTION_TEXT = "EXCEPTION_TEXT";
    private static final String ID = "ID";
    private static final String DESCRIPTION = "DESCRIPTION";

    /**
     * Creates a new instance.
     */
    public DefaultDatabaseException() {
        super((DatabaseMetaTag) null);
    }

    /**
     * Creates a new instance.
     */
    public DefaultDatabaseException(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);


    }

    /**
     * Creates a new instance with
     * the specified values.
     */
    public DefaultDatabaseException(String schema, String name) {
        super((DatabaseMetaTag) null);
        setName(name);
        setSchemaName(schema);
    }

    public int getType() {
        return EXCEPTION;
    }

    @Override
    public String getMetaDataKey() {
        return META_TYPES[EXCEPTION];
    }

    @Override
    public boolean allowsChildren() {
        return false;
    }

    public String getID() {
        checkOnReload(exceptionID);
        return exceptionID;
    }

    public void setExceptionID(String exceptionID) {
        this.exceptionID = exceptionID;
    }

    public String getExceptionText() {
        checkOnReload(exceptionText);
        return exceptionText;
    }

    public void setExceptionText(String exceptionText) {
        this.exceptionText = exceptionText;
    }

    public String getCreateFullSQLText() {
        return SQLUtils.generateCreateException(getName(), getExceptionText());
    }

    @Override
    public String getCreateSQL() throws DataSourceException {
        return getCreateFullSQLText();
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropRequest("EXCEPTION", getName());
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
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT\n")
                .append("RDB$MESSAGE AS ").append(EXCEPTION_TEXT).append(",\n")
                .append("RDB$EXCEPTION_NUMBER AS ").append(ID).append(",\n")
                .append("RDB$DESCRIPTION AS ").append(DESCRIPTION).append("\n")
                .append("FROM RDB$EXCEPTIONS\n")
                .append("WHERE\n")
                .append("TRIM(RDB$EXCEPTION_NAME) = '").append(getName()).append("'");
        return sb.toString();
    }

    @Override
    protected void setInfoFromResultSet(ResultSet rs) throws SQLException {
        if (rs.next()) {
            setExceptionID(rs.getString(ID));
            setExceptionText(rs.getString(EXCEPTION_TEXT));
            setRemarks(rs.getString(DESCRIPTION));
        }
    }
}

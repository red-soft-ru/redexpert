package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.sql.sqlbuilder.SelectBuilder;
import org.executequery.sql.sqlbuilder.Table;
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
    private static final String EXCEPTION_TEXT = "MESSAGE";
    private static final String ID = "EXCEPTION_NUMBER";
    private static final String DESCRIPTION = "DESCRIPTION";

    /**
     * Creates a new instance.
     */
    public DefaultDatabaseException(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
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

    @Override
    public String getCreateSQLText() {
        return SQLUtils.generateCreateException(getName(), getExceptionText());
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("EXCEPTION", getName());
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {
        return this.getCreateSQLText();
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {
        DefaultDatabaseException comparingException = (DefaultDatabaseException) databaseObject;
        return SQLUtils.generateAlterException(this, comparingException);
    }


    @Override
    protected String getFieldName() {
        return "EXCEPTION_NAME";
    }

    @Override
    protected Table getMainTable() {
        return Table.createTable("RDB$EXCEPTIONS", "EXP");
    }

    @Override
    protected SelectBuilder builderCommonQuery() {
        SelectBuilder sb = new SelectBuilder();
        Table mainTable = getMainTable();
        sb.appendFields(mainTable, getFieldName(), ID, EXCEPTION_TEXT, DESCRIPTION);
        sb.appendTable(mainTable);
        sb.setOrdering(getObjectField().getFieldTable());
        return sb;
    }

    @Override
    public Object setInfoFromSingleRowResultSet(ResultSet rs, boolean first) throws SQLException {
        setExceptionID(getFromResultSet(rs, ID));
        setExceptionText(getFromResultSet(rs, EXCEPTION_TEXT));
        setRemarks(getFromResultSet(rs, DESCRIPTION));
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
}

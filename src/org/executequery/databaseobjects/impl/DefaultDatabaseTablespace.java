package org.executequery.databaseobjects.impl;

import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.sql.sqlbuilder.SelectBuilder;
import org.executequery.sql.sqlbuilder.Table;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.SQLUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DefaultDatabaseTablespace extends AbstractDatabaseObject {

    public static final String ID = "TABLESPACE_ID";
    public static final String SYSTEM = "SYSTEM_FLAG";
    public static final String OWNER = "OWNER_NAME";
    public static final String FILE_NAME = "FILE_NAME";
    public static final String OFFLINE = "OFFLINE";
    public static final String READ_ONLY = "READ_ONLY";

    private String id;
    private String owner;
    private String fileName;
    private boolean offline;
    private boolean readOnly;
    private List<String> indexes;
    private List<String> tables;
    private final DefaultStatementExecutor querySender;

    public DefaultDatabaseTablespace(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
        querySender = new DefaultStatementExecutor(getHost().getDatabaseConnection());
    }

    @Override
    public boolean allowsChildren() {
        return false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getFileName() {
        checkOnReload(fileName);
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    protected String getFieldName() {
        return "TABLESPACE_NAME";
    }

    @Override
    protected Table getMainTable() {
        return Table.createTable("RDB$TABLESPACES", "T");
    }

    @Override
    protected SelectBuilder builderCommonQuery() {
        SelectBuilder sb = new SelectBuilder(getHost().getDatabaseConnection());
        Table table = getMainTable();
        sb.appendFields(table, getFieldName(), ID, SYSTEM, DESCRIPTION, OWNER, FILE_NAME, READ_ONLY, OFFLINE);
        sb.appendTable(table);
        sb.setOrdering(getObjectField().getFieldTable());
        return sb;
    }

    @Override
    public Object setInfoFromSingleRowResultSet(ResultSet rs, boolean first) throws SQLException {
        setId(getFromResultSet(rs, ID));
        setFileName(getFromResultSet(rs, FILE_NAME));
        setOwner(getFromResultSet(rs, OWNER));
        setOffline(rs.getBoolean(OFFLINE));
        setReadOnly(rs.getBoolean(READ_ONLY));
        setSystemFlag(rs.getInt(SYSTEM) != 0);
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

    @Override
    public int getType() {
        return NamedObject.TABLESPACE;
    }

    @Override
    public String getMetaDataKey() {
        return NamedObject.META_TYPES[TABLESPACE];
    }


    public List<String> getIndexes() {
        if (isMarkedForReload() || indexes == null) {
            getObjectInfo();
        }
        return indexes;
    }

    public void setIndexes(List<String> indexes) {
        this.indexes = indexes;
    }


    public String getIndexesQuery() {
        return "SELECT RDB$INDEX_NAME, RDB$INDEX_INACTIVE FROM RDB$INDICES WHERE RDB$TABLESPACE_NAME='" + getName() + "' ORDER BY 1";
    }


    public String getTablesQuery() {
        return "SELECT RDB$RELATION_NAME FROM RDB$RELATIONS WHERE RDB$TABLESPACE_NAME='" + getName() + "' ORDER BY 1";
    }




    public List<String> getTables() {
        if (tables == null || isMarkedForReload())
            getObjectInfo();
        return tables;
    }

    public void setTables(List<String> tables) {
        this.tables = tables;
    }


    @Override
    public String getCreateSQLText() throws DataSourceException {
        return SQLUtils.generateCreateTablespace(getName(), getFileName(), getHost().getDatabaseConnection());
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("TABLESPACE", getName(), getHost().getDatabaseConnection());
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {
        return getCreateSQLText();
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {
        DefaultDatabaseTablespace comparingTablespace = (DefaultDatabaseTablespace) databaseObject;
        return SQLUtils.generateAlterTablespace(this, comparingTablespace);
    }

}


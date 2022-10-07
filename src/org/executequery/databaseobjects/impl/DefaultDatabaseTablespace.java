package org.executequery.databaseobjects.impl;

import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.NamedObject;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.SQLUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class DefaultDatabaseTablespace extends AbstractDatabaseObject {
    public static final String[] COLUMNS =
            {"ID", "SECURITY_CLASS", "SYSTEM", "DESCRIPTION", "OWNER", "FILE_NAME", "OFFLINE", "READ_ONLY"};
    public static final int ID = 0;
    public static final int SECURITY_CLASS = ID + 1;
    public static final int SYSTEM = SECURITY_CLASS + 1;
    public static final int DESCRIPTION = SYSTEM + 1;
    public static final int OWNER = DESCRIPTION + 1;
    public static final int FILE_NAME = OWNER + 1;
    public static final int OFFLINE = FILE_NAME + 1;
    public static final int READ_ONLY = OFFLINE + 1;

    private String[] attributes;
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

    @Override
    protected String queryForInfo() {
        String query = MessageFormat.format("select rdb$tablespace_id as {" + ID + "},rdb$security_class as {" + SECURITY_CLASS + "}," +
                "rdb$system_flag as {" + SYSTEM + "},rdb$description as {" + DESCRIPTION + "},rdb$owner_name as {" + OWNER + "}," +
                "rdb$file_name as {" + FILE_NAME + "}, rdb$offline as {" + OFFLINE + "},rdb$read_only as {" + READ_ONLY + "}" +
                " from rdb$tablespaces where rdb$tablespace_name = ''" + getName() + "''", COLUMNS);
        return query;
    }

    @Override
    protected void setInfoFromResultSet(ResultSet rs) throws SQLException {
        attributes = new String[COLUMNS.length];
        if (rs.next()) {
            for (int i = 0; i < COLUMNS.length; i++) {
                attributes[i] = rs.getString(COLUMNS[i]);
            }
        }
        setSystemFlag(Integer.parseInt(attributes[SYSTEM].trim()) != 0);
        setRemarks(attributes[DESCRIPTION]);

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

    private void loadIndexes() {
        indexes = new ArrayList<>();
        try {
            ResultSet rs = querySender.getResultSet(getIndexesQuery()).getResultSet();
            while (rs.next()) {
                tables.add(rs.getString(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            querySender.releaseResources();
        }
    }

    public String getTablesQuery() {
        return "SELECT RDB$RELATION_NAME FROM RDB$RELATIONS WHERE RDB$TABLESPACE_NAME='" + getName() + "' ORDER BY 1";
    }

    private void loadTables() {
        tables = new ArrayList<>();

        try {
            ResultSet rs = querySender.getResultSet(getTablesQuery()).getResultSet();
            while (rs.next()) {
                tables.add(rs.getString(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            querySender.releaseResources();
        }

    }


    public List<String> getTables() {
        if (tables == null || isMarkedForReload())
            getObjectInfo();
        return tables;
    }

    public void setTables(List<String> tables) {
        this.tables = tables;
    }

    public String[] getAttributes() {
        if (attributes == null || isMarkedForReload())
            getObjectInfo();
        return attributes;
    }

    public String getAttribute(int atrrIndex) {
        if (attributes == null || isMarkedForReload())
            getObjectInfo();
        return attributes[atrrIndex];
    }

    public String getFileName() {
        return getAttribute(FILE_NAME);
    }

    public String getCreateFullSQLText() throws DataSourceException {

        return SQLUtils.generateCreateTablespace(getName(), getFileName());
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {
        return getCreateFullSQLText();
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropRequest("TABLESPACE", getName());
    }

    @Override
    public String getAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {
        return null;
    }

    @Override
    public String getFillSQL() throws DataSourceException {
        return null;
    }
}


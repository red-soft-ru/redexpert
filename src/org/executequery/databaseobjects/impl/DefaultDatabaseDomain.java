package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;
import org.executequery.databaseobjects.DatabaseTypeConverter;
import org.executequery.datasource.ConnectionManager;
import org.underworldlabs.jdbc.DataSourceException;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vasiliy on 02.02.17.
 */
public class DefaultDatabaseDomain extends DefaultDatabaseExecutable
        implements DatabaseProcedure {

    int sqlType, sqlSubtype, sqlSize, sqlScale;

    private List<DatabaseColumn> columns;

    /**
     * Creates a new instance.
     */
    public DefaultDatabaseDomain() {
    }

    /**
     * Creates a new instance.
     */
    public DefaultDatabaseDomain(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
    }

    /**
     * Creates a new instance with
     * the specified values.
     */
    public DefaultDatabaseDomain(String schema, String name) {
        setName(name);
        setSchemaName(schema);
    }

    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    public int getType() {
        return DOMAIN;
    }

    /**
     * Returns the meta data key name of this object.
     *
     * @return the meta data key name.
     */
    public String getMetaDataKey() {
        return META_TYPES[DOMAIN];
    }

    public List<DatabaseColumn> getDomainData() {

        Statement statement = null;

        if (!isMarkedForReload() && columns != null) {

            return columns;
        }

        try {

            DatabaseMetaData dmd = getMetaTagParent().getHost().getDatabaseMetaData();

            String _catalog = getCatalogName();
            String _schema = getSchemaName();

            columns = new ArrayList<>();
            if (ConnectionManager.realConnection(dmd).getClass().getName().contains("FBConnection")) {
                statement = dmd.getConnection().createStatement();

                ResultSet rs = statement.executeQuery("SELECT first 1\n" +
                        "cast(F.RDB$FIELD_NAME as varchar(63)) AS FIELD_NAME,\n" +
                        "F.RDB$FIELD_TYPE AS FIELD_TYPE,\n" +
                        "F.RDB$FIELD_SUB_TYPE AS FIELD_SUB_TYPE,\n" +
                        "F.RDB$FIELD_PRECISION AS FIELD_PRECISION,\n" +
                        "F.RDB$FIELD_SCALE AS FIELD_SCALE,\n" +
                        "F.RDB$FIELD_LENGTH AS FIELD_LENGTH,\n" +
                        "F.RDB$CHARACTER_LENGTH AS CHAR_LEN,\n" +
                        "F.RDB$DESCRIPTION AS REMARKS,\n" +
                        "F.RDB$DEFAULT_SOURCE AS DEFAULT_SOURCE,\n" +
                        "F.RDB$DEFAULT_SOURCE AS DOMAIN_DEFAULT_SOURCE,\n" +
                        "F.RDB$NULL_FLAG AS NULL_FLAG,\n" +
                        "F.RDB$NULL_FLAG AS SOURCE_NULL_FLAG,\n" +
                        "F.RDB$COMPUTED_BLR AS COMPUTED_BLR,\n" +
                        "F.RDB$CHARACTER_SET_ID,\n" +
                        "'NO' AS IS_IDENTITY,\n" +
                        "CAST(NULL AS VARCHAR(10)) AS JB_IDENTITY_TYPE\n" +
                        "FROM RDB$RELATION_FIELDS RF,RDB$FIELDS F\n" +
                        "WHERE\n" +
                        "TRIM(F.RDB$FIELD_NAME) = '" + getName() + "'");
                while (rs.next()) {

                    DefaultDatabaseColumn column = new DefaultDatabaseColumn();
                    column.setCatalogName(_catalog);
                    column.setSchemaName(_schema);
                    column.setName(rs.getString(1));
                    column.setTypeInt(rs.getInt(2));
                    column.setTypeName(DatabaseTypeConverter.getDataTypeName(rs.getInt(2), rs.getInt(3), rs.getInt(5)));
                    column.setColumnSize(rs.getInt(6));
                    if (rs.getInt(4) != 0)
                        column.setColumnSize(rs.getInt(4));
                    column.setColumnScale(Math.abs(rs.getInt(5)));
                    column.setRequired(rs.getInt(12) == DatabaseMetaData.columnNoNulls);
                    column.setRemarks(rs.getString(8));
                    this.setRemarks(rs.getString(8));
                    column.setDefaultValue(rs.getString(13));

                    sqlType = rs.getInt(2);
                    sqlSubtype = rs.getInt(3);
                    sqlSize = column.getColumnSize();
                    sqlScale = rs.getInt(5);

                    columns.add(column);
                }
                releaseResources(rs);

            }

            return columns;

        } catch (SQLException e) {

            throw new DataSourceException(e);

        } finally {

            setMarkedForReload(false);
        }
    }

    @Override
    public String getCreateSQLText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Create domain \n\t");
        sb.append(getName());
        sb.append("\n");
        sb.append("\t as \n\t");
        sb.append(DatabaseTypeConverter.getTypeWithSize(sqlType, sqlSubtype, sqlSize, sqlScale));
        sb.append(";");
        return sb.toString();
    }
}
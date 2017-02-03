package org.executequery.databaseobjects.impl;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;
import org.firebirdsql.jdbc.FBDatabaseMetaData;
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

    private static final int SUBTYPE_NUMERIC = 1;
    private static final int SUBTYPE_DECIMAL = 2;

    private static final int smallint_type = 7;
    private static final int integer_type = 8;
    private static final int quad_type = 9;
    private static final int float_type = 10;
    private static final int d_float_type = 11;
    private static final int date_type = 12;
    private static final int time_type = 13;
    private static final int char_type = 14;
    private static final int int64_type = 16;
    private static final int double_type = 27;
    private static final int timestamp_type = 35;
    private static final int varchar_type = 37;
    //  private static final int cstring_type = 40;
    private static final int blob_type = 261;
    private static final short boolean_type = 23;

    private List<DatabaseColumn> columns;

    /**
     * Creates a new instance.
     */
    public DefaultDatabaseDomain() {}

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
            if (dmd instanceof FBDatabaseMetaData) {
                statement = dmd.getConnection().createStatement();

                ResultSet rs = statement.executeQuery("SELECT first 1 " +
                        "cast(RF.RDB$RELATION_NAME as varchar(63)) " +
                        "AS RELATION_NAME,\n" +
                        "cast(F.RDB$FIELD_NAME as varchar(63)) AS FIELD_NAME,\n" +
                        "F.RDB$FIELD_TYPE AS FIELD_TYPE," +
                        "F.RDB$FIELD_SUB_TYPE AS FIELD_SUB_TYPE,\n" +
                        "F.RDB$FIELD_PRECISION AS FIELD_PRECISION,\n" +
                        "F.RDB$FIELD_SCALE AS FIELD_SCALE,\n" +
                        "F.RDB$FIELD_LENGTH AS FIELD_LENGTH,\n" +
                        "F.RDB$CHARACTER_LENGTH AS CHAR_LEN,\n" +
                        "F.RDB$DESCRIPTION AS REMARKS,\n" +
                        "RF.RDB$DEFAULT_SOURCE AS DEFAULT_SOURCE,\n" +
                        "F.RDB$DEFAULT_SOURCE AS DOMAIN_DEFAULT_SOURCE,\n" +
                        "RF.RDB$FIELD_POSITION + 1 AS FIELD_POSITION,\n" +
                        "CAST(NULL AS SMALLINT) AS NULL_FLAG,\n" +
                        "F.RDB$NULL_FLAG AS SOURCE_NULL_FLAG,\n" +
                        "F.RDB$COMPUTED_BLR AS COMPUTED_BLR,\n" +
                        "F.RDB$CHARACTER_SET_ID,\n" +
                        "'NO' AS IS_IDENTITY,\n" +
                        "CAST(NULL AS VARCHAR(10)) AS JB_IDENTITY_TYPE\n" +
                        "FROM RDB$RELATION_FIELDS RF,RDB$FIELDS F\n" +
                        "WHERE\n" +
                        "TRIM(F.RDB$FIELD_NAME) = '" + getName() + "'\n" +
                        "and  RF.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME\n" +
                        "order by RF.RDB$RELATION_NAME, RF.RDB$FIELD_POSITION");
                while (rs.next()) {

                    DefaultDatabaseColumn column = new DefaultDatabaseColumn();
                    column.setCatalogName(_catalog);
                    column.setSchemaName(_schema);
                    column.setName(rs.getString(2));
                    column.setTypeInt(rs.getInt(3));
                    column.setTypeName(getDataTypeName(rs.getInt(3), rs.getInt(4), rs.getInt(6)));
                    column.setColumnSize(rs.getInt(7));
                    column.setColumnScale(rs.getInt(6));
                    column.setRequired(rs.getInt(13) == DatabaseMetaData.columnNoNulls);
                    column.setRemarks(rs.getString(9));
                    this.setRemarks(rs.getString(9));
//                    column.setDefaultValue(rs.getString(13));

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

    private static String getDataTypeName(int sqltype, int sqlsubtype, int sqlscale) {
        switch (sqltype) {
            case smallint_type:
                if (sqlsubtype == SUBTYPE_NUMERIC || (sqlsubtype == 0 && sqlscale < 0))
                    return "NUMERIC";
                else if (sqlsubtype == SUBTYPE_DECIMAL)
                    return "DECIMAL";
                else
                    return "SMALLINT";
            case integer_type:
                if (sqlsubtype == SUBTYPE_NUMERIC || (sqlsubtype == 0 && sqlscale < 0))
                    return "NUMERIC";
                else if (sqlsubtype == SUBTYPE_DECIMAL)
                    return "DECIMAL";
                else
                    return "INTEGER";
            case double_type:
            case d_float_type:
                if (sqlsubtype == SUBTYPE_NUMERIC || (sqlsubtype == 0 && sqlscale < 0))
                    return "NUMERIC";
                else if (sqlsubtype == SUBTYPE_DECIMAL)
                    return "DECIMAL";
                else
                    return "DOUBLE PRECISION";
            case float_type:
                return "FLOAT";
            case char_type:
                return "CHAR";
            case varchar_type:
                return "VARCHAR";
            case timestamp_type:
                return "TIMESTAMP";
            case time_type:
                return "TIME";
            case date_type:
                return "DATE";
            case int64_type:
                if (sqlsubtype == SUBTYPE_NUMERIC || (sqlsubtype == 0 && sqlscale < 0))
                    return "NUMERIC";
                else if (sqlsubtype == SUBTYPE_DECIMAL)
                    return "DECIMAL";
                else
                    return "BIGINT";
            case blob_type:
                if (sqlsubtype < 0)
                    // TODO Include actual subtype?
                    return "BLOB SUB_TYPE <0";
                else if (sqlsubtype == 0)
                    return "BLOB SUB_TYPE 0";
                else if (sqlsubtype == 1)
                    return "BLOB SUB_TYPE 1";
                else
                    return "BLOB SUB_TYPE " + sqlsubtype;
            case quad_type:
                return "ARRAY";
            case boolean_type:
                return "BOOLEAN";
            default:
                return "NULL";
        }
    }
}
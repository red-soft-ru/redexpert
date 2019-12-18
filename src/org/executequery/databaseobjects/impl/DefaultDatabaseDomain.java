package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseTypeConverter;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vasiliy on 02.02.17.
 */
public class DefaultDatabaseDomain extends AbstractDatabaseObject {

    int sqlType, sqlSubtype, sqlSize, sqlScale;

    private List<DatabaseColumn> columns;

    private static final String NAME = "FIELD_NAME";
    private static final String TYPE = "FIELD_TYPE";
    private static final String SUB_TYPE = "FIELD_SUB_TYPE";
    private static final String FIELD_PRECISION = "FIELD_PRECISION";
    private static final String SCALE = "FIELD_SCALE";
    private static final String FIELD_LENGTH = "FIELD_LENGTH";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String NULL_FLAG = "NULL_FLAG";
    private static final String COMPUTED_BY = "COMPUTED_BY";


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
        super((DatabaseMetaTag) null);
        setName(name);
        setSchemaName(schema);
    }

    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    public int getType() {
        if (isSystem()) {
            return SYSTEM_DOMAIN;
        } else return DOMAIN;
    }

    /**
     * Returns the meta data key name of this object.
     *
     * @return the meta data key name.
     */
    public String getMetaDataKey() {
        return META_TYPES[getType()];
    }

    @Override
    public boolean allowsChildren() {
        return false;
    }

    public List<DatabaseColumn> getDomainData() {
        checkOnReload(columns);
        return columns;
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

    @Override
    protected String queryForInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT \n")
                .append("F.RDB$FIELD_TYPE AS ").append(TYPE).append(",\n")
                .append("F.RDB$FIELD_SUB_TYPE AS ").append(SUB_TYPE).append(",\n")
                .append("F.RDB$FIELD_PRECISION AS ").append(FIELD_PRECISION).append(",\n")
                .append("F.RDB$FIELD_SCALE AS ").append(SCALE).append(",\n")
                .append("F.RDB$FIELD_LENGTH AS ").append(FIELD_LENGTH).append(",\n")
                .append("F.RDB$DESCRIPTION AS ").append(DESCRIPTION).append(",\n")
                .append("F.RDB$NULL_FLAG AS ").append(NULL_FLAG).append(",\n")
                .append("F.RDB$COMPUTED_BLR AS ").append(COMPUTED_BY).append("\n")
                .append("FROM RDB$FIELDS F\n")
                .append("WHERE\n")
                .append("TRIM(F.RDB$FIELD_NAME) = '").append(getName()).append("'");
        return sb.toString();
    }

    @Override
    protected void setInfoFromResultSet(ResultSet rs) throws SQLException {
        columns = new ArrayList<>();
        while (rs.next()) {

            DefaultDatabaseColumn column = new DefaultDatabaseColumn();
            column.setName(getName());
            column.setTypeInt(rs.getInt(TYPE));
            column.setTypeName(DatabaseTypeConverter.getDataTypeName(rs.getInt(TYPE), rs.getInt(SUB_TYPE), rs.getInt(SCALE)));
            column.setColumnSize(rs.getInt(FIELD_LENGTH));
            if (rs.getInt(FIELD_PRECISION) != 0)
                column.setColumnSize(rs.getInt(FIELD_PRECISION));
            column.setColumnScale(Math.abs(rs.getInt(SCALE)));
            column.setRequired(rs.getInt(NULL_FLAG) == DatabaseMetaData.columnNoNulls);
            column.setRemarks(rs.getString(DESCRIPTION));
            setRemarks(rs.getString(DESCRIPTION));
            column.setDefaultValue(COMPUTED_BY);

            sqlType = rs.getInt(TYPE);
            sqlSubtype = rs.getInt(SUB_TYPE);
            sqlSize = column.getColumnSize();
            sqlScale = rs.getInt(SCALE);

            columns.add(column);
        }
    }
}
package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseTypeConverter;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.table.CreateTableSQLSyntax;
import org.underworldlabs.util.MiscUtils;

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
    private static final String CHAR_LENGTH = "CH_LENGTH";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String NULL_FLAG = "NULL_FLAG";
    private static final String COMPUTED_BY = "COMPUTED_BY";
    private static final String CHARSET = "CHARSET";
    private static final String VALIDATION_SOURCE = "VALIDATION_SOURCE";
    private static final String DEFAULT_SOURCE = "DEFAULT_SOURCE";
    private static final String COMPUTED_SOURCE = "COMPUTED_SOURCE";
    private static final String SEGMENT_LENGTH = "SEGMENT_LENGTH";
    private static final String COLLATION_NAME = "COLLATION_NAME";

    /**
     * Creates a new instance.
     */
    public DefaultDatabaseDomain(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
    }

    ColumnData domainData;

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

    public List<DatabaseColumn> getDomainCols() {
        checkOnReload(columns);
        return columns;
    }

    public ColumnData getDomainData() {
        checkOnReload(domainData);
        return domainData;
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
                .append("F.RDB$CHARACTER_LENGTH AS ").append(CHAR_LENGTH).append(",\n")
                .append("F.RDB$DESCRIPTION AS ").append(DESCRIPTION).append(",\n")
                .append("F.RDB$NULL_FLAG AS ").append(NULL_FLAG).append(",\n")
                .append("F.RDB$COMPUTED_BLR AS ").append(COMPUTED_BY).append(",\n")
                .append("C.RDB$CHARACTER_SET_NAME AS ").append(CHARSET).append(",\n")
                .append("F.RDB$VALIDATION_SOURCE AS ").append(VALIDATION_SOURCE).append(",\n")
                .append("F.RDB$DEFAULT_SOURCE AS ").append(DEFAULT_SOURCE).append(",\n")
                .append("F.RDB$COMPUTED_SOURCE AS ").append(COMPUTED_SOURCE).append(",\n")
                .append("F.RDB$SEGMENT_LENGTH AS ").append(SEGMENT_LENGTH).append(",\n")
                .append("CO.RDB$COLLATION_NAME AS ").append(COLLATION_NAME).append("\n")
                .append("FROM RDB$FIELDS F\n")
                .append("LEFT JOIN RDB$CHARACTER_SETS AS C ON F.RDB$CHARACTER_SET_ID = C.RDB$CHARACTER_SET_ID\n")
                .append("LEFT JOIN RDB$COLLATIONS CO ON ((F.RDB$COLLATION_ID = CO.RDB$COLLATION_ID) AND")
                .append("(F.RDB$CHARACTER_SET_ID = CO.RDB$CHARACTER_SET_ID))\n")
                .append("WHERE\n")
                .append("TRIM(F.RDB$FIELD_NAME) = '").append(getName()).append("'");
        String query = sb.toString();
        return query;
    }

    @Override
    protected void setInfoFromResultSet(ResultSet rs) throws SQLException {
        columns = new ArrayList<>();
        domainData = new ColumnData(getHost().getDatabaseConnection());
        while (rs.next()) {
            domainData.setDomainType(rs.getInt(TYPE));
            domainData.setDomainSize(rs.getInt(FIELD_LENGTH));
            if (rs.getInt(FIELD_PRECISION) != 0)
                domainData.setDomainSize(rs.getInt(FIELD_PRECISION));
            if (rs.getInt(CHAR_LENGTH) != 0)
                domainData.setDomainSize(rs.getInt(CHAR_LENGTH));
            domainData.setDomainScale(Math.abs(rs.getInt(SCALE)));
            domainData.setDomainSubType(rs.getInt(SUB_TYPE));
            String domainCharset = rs.getString(CHARSET);
            String domainCheck = rs.getString(VALIDATION_SOURCE);
            domainData.setDomainDescription(rs.getString(DESCRIPTION));
            domainData.setDomainNotNull(rs.getInt(NULL_FLAG) == 1);
            domainData.setDomainDefault(rs.getString(DEFAULT_SOURCE));
            domainData.setDomainComputedBy(rs.getString(COMPUTED_SOURCE));
            String domainCollate = rs.getString(COLLATION_NAME);
            domainData.setDomainType(DatabaseTypeConverter.getSqlTypeFromRDBType(domainData.getDomainType(), domainData.getDomainSubType()));
            if (!MiscUtils.isNull(domainCheck)) {
                domainCheck = domainCheck.trim();
                if (domainCheck.toUpperCase().startsWith("CHECK"))
                    domainCheck = domainCheck.substring(5).trim();
                if (domainCheck.startsWith("(") && domainCheck.endsWith(")")) {
                    domainCheck = domainCheck.substring(1, domainCheck.length() - 1);
                }
            }
            domainData.setDomainCheck(domainCheck);
            domainData.setDomainDefault(domainData.processedDefaultValue(domainData.getDefaultValue()));
            if (MiscUtils.isNull(domainCharset)) {
                domainCharset = CreateTableSQLSyntax.NONE;
            } else domainCharset = domainCharset.trim();
            domainData.setDomainCharset(domainCharset);
            if (MiscUtils.isNull(domainCollate)) {
                domainCollate = CreateTableSQLSyntax.NONE;
            } else domainCollate = domainCollate.trim();
            domainData.setDomainCollate(domainCollate);

            DefaultDatabaseColumn column = new DefaultDatabaseColumn();
            column.setName(getName());
            column.setTypeInt(rs.getInt(TYPE));
            column.setTypeName(DatabaseTypeConverter.getDataTypeName(rs.getInt(TYPE), rs.getInt(SUB_TYPE), rs.getInt(SCALE)));
            column.setColumnSize(rs.getInt(FIELD_LENGTH));
            if (rs.getInt(FIELD_PRECISION) != 0)
                column.setColumnSize(rs.getInt(FIELD_PRECISION));
            if (rs.getInt(CHAR_LENGTH) != 0)
                column.setColumnSize(rs.getInt(CHAR_LENGTH));
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
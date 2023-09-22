package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseTypeConverter;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.comparer.Comparer;
import org.executequery.sql.sqlbuilder.*;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

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
    private static final String TYPE = "FIELD_TYPE";
    private static final String SUB_TYPE = "FIELD_SUB_TYPE";
    private static final String FIELD_PRECISION = "FIELD_PRECISION";
    private static final String SCALE = "FIELD_SCALE";
    private static final String FIELD_LENGTH = "FIELD_LENGTH";
    private static final String CHAR_LENGTH = "CH_LENGTH";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String NULL_FLAG = "NULL_FLAG";
    private static final String COMPUTED_BY = "COMPUTED_BLR";
    private static final String CHARSET = "CHARACTER_SET_NAME";
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
     * Returns the metadata key name of this object.
     *
     * @return the metadata key name.
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
        return SQLUtils.generateCreateDomain(getDomainData(), getName(), true, true);
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("DOMAIN", getName());
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {
        return SQLUtils.generateCreateDomain(getDomainData(), getName(), true, Comparer.isCommentsNeed());
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {
        DefaultDatabaseDomain comparingDomain = (DefaultDatabaseDomain) databaseObject;
        return SQLUtils.generateAlterDomain(getDomainData(), comparingDomain.getDomainData());
    }

    @Override
    protected String getFieldName() {
        return FIELD_NAME;
    }

    @Override
    protected Table getMainTable() {
        return Table.createTable("RDB$FIELDS", "F");
    }

    protected SelectBuilder builderCommonQuery() {
        SelectBuilder sb = new SelectBuilder();
        Table fields = getMainTable();
        Table charsets = Table.createTable("RDB$CHARACTER_SETS", "C");
        Table collations = Table.createTable("RDB$COLLATIONS", "CO");
        Table dimensions = Table.createTable("RDB$FIELD_DIMENSIONS", "FD");
        sb.appendFields(fields, FIELD_NAME, TYPE, SUB_TYPE, FIELD_PRECISION, SCALE, FIELD_LENGTH,
                NULL_FLAG, COMPUTED_BY, VALIDATION_SOURCE, DEFAULT_SOURCE, COMPUTED_SOURCE, SEGMENT_LENGTH, DESCRIPTION);
        sb.appendField(Field.createField(fields, "CHARACTER_LENGTH").setAlias(CHAR_LENGTH));
        sb.appendField(Field.createField(charsets, CHARSET));
        sb.appendField(Field.createField(collations, COLLATION_NAME));
        sb.appendFields(dimensions, DIMENSION, LOWER_BOUND, UPPER_BOUND);
        sb.appendJoin(Join.createLeftJoin().appendFields(Field.createField(fields, "CHARACTER_SET_ID"), Field.createField(charsets, "CHARACTER_SET_ID")));
        sb.appendJoin(Join.createLeftJoin().appendFields(Field.createField(fields, "COLLATION_ID"), Field.createField(collations, "COLLATION_ID"))
                .appendFields(Field.createField(fields, "CHARACTER_SET_ID"), Field.createField(collations, "CHARACTER_SET_ID")));
        sb.appendJoin(Join.createLeftJoin().appendFields(Field.createField(fields, FIELD_NAME), Field.createField(dimensions, FIELD_NAME)));
        sb.setOrdering("1");
        return sb;
    }

    @Override
    protected SelectBuilder builderForInfoAllObjects(SelectBuilder commonBuilder) {
        SelectBuilder sb = super.builderForInfoAllObjects(commonBuilder);
        if (!isSystem())
            sb.appendCondition(Condition.createCondition(getObjectField(), "STARTING  WITH ", "'RDB$'").setNot(true));
        return sb;
    }

    @Override
    public Object setInfoFromSingleRowResultSet(ResultSet rs, boolean first) throws SQLException {
        if (first) {
            domainData.setDomainType(rs.getInt(TYPE));
            domainData.setDomainSize(rs.getInt(FIELD_LENGTH));
            if (rs.getInt(FIELD_PRECISION) != 0)
                domainData.setDomainSize(rs.getInt(FIELD_PRECISION));
            if (rs.getInt(CHAR_LENGTH) != 0)
                domainData.setDomainSize(rs.getInt(CHAR_LENGTH));
            if (rs.getInt(SEGMENT_LENGTH) != 0)
                domainData.setDomainSize(rs.getInt(SEGMENT_LENGTH));
            domainData.setDomainScale(Math.abs(rs.getInt(SCALE)));
            domainData.setDomainSubType(rs.getInt(SUB_TYPE));
            String domainCharset = rs.getString(CHARSET);
            String domainCheck = rs.getString(VALIDATION_SOURCE);
            domainData.setDomainDescription(rs.getString(DESCRIPTION));
            domainData.setDomainNotNull(rs.getInt(NULL_FLAG) == 1);
            domainData.setNotNull(domainData.isDomainNotNull());
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
            domainData.setDomainDefault(domainData.processedDefaultValue(domainData.getDomainDefault(), false));
            if (MiscUtils.isNull(domainCharset)) {
                domainCharset = "";
            } else domainCharset = domainCharset.trim();
            domainData.setDomainCharset(domainCharset);
            if (MiscUtils.isNull(domainCollate)) {
                domainCollate = "";
            } else domainCollate = domainCollate.trim();
            domainData.setDomainCollate(domainCollate);
            domainData.setDomainTypeName(DatabaseTypeConverter.getDataTypeName(rs.getInt(TYPE), rs.getInt(SUB_TYPE), rs.getInt(SCALE)));
            if (rs.getObject(DIMENSION) != null) {
                domainData.appendDimension(rs.getInt(DIMENSION), rs.getInt(LOWER_BOUND), rs.getInt(UPPER_BOUND));
            }
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
            column.setRemarks(getFromResultSet(rs, DESCRIPTION));
            setRemarks(getFromResultSet(rs, DESCRIPTION));
            column.setDefaultValue(COMPUTED_BY);
            column.setDimensions(domainData.getDimensions());
            sqlType = rs.getInt(TYPE);
            sqlSubtype = rs.getInt(SUB_TYPE);
            sqlSize = column.getColumnSize();
            sqlScale = rs.getInt(SCALE);

            columns.add(column);
        } else if (rs.getObject(DIMENSION) != null) {
            domainData.appendDimension(rs.getInt(DIMENSION), rs.getInt(LOWER_BOUND), rs.getInt(UPPER_BOUND));
            columns.get(0).setDimensions(domainData.getDimensions());
        }
        return null;
    }

    @Override
    public void prepareLoadingInfo() {
        columns = new ArrayList<>();
        domainData = new ColumnData(getHost().getDatabaseConnection());
        domainData.setColumnName(getName());
    }

    @Override
    public void finishLoadingInfo() {

    }

    @Override
    public boolean isAnyRowsResultSet() {
        return true;
    }
}
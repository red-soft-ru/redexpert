package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseTypeConverter;
import org.executequery.gui.browser.ColumnData;
import org.executequery.sql.sqlbuilder.*;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by vasiliy on 02.02.17.
 */
public class DefaultDatabaseDomain extends AbstractDatabaseObject {

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

    private ColumnData domainData;

    public DefaultDatabaseDomain(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
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
    public String getCreateSQLTextWithoutComment() {
        return SQLUtils.generateCreateDomain(getDomainData(), getName(), true, false);
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("DOMAIN", getName(), getHost().getDatabaseConnection());
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {
        DefaultDatabaseDomain comparingDomain = (DefaultDatabaseDomain) databaseObject;
        return SQLUtils.generateAlterDomain(getDomainData(), comparingDomain.getDomainData());
    }

    @Override
    protected SelectBuilder builderCommonQuery() {
        SelectBuilder sb = new SelectBuilder(getHost().getDatabaseConnection());

        Table fields = getMainTable();
        Table charsets = Table.createTable("RDB$CHARACTER_SETS", "C");
        Table collations = Table.createTable("RDB$COLLATIONS", "CO");
        Table dimensions = Table.createTable("RDB$FIELD_DIMENSIONS", "FD");

        sb.appendFields(fields,
                FIELD_NAME, TYPE, SUB_TYPE, FIELD_PRECISION, SCALE, FIELD_LENGTH, NULL_FLAG,
                COMPUTED_BY, VALIDATION_SOURCE, DEFAULT_SOURCE, COMPUTED_SOURCE, SEGMENT_LENGTH, DESCRIPTION
        );
        sb.appendField(Field.createField(fields, "CHARACTER_LENGTH").setAlias(CHAR_LENGTH));
        sb.appendField(Field.createField(charsets, CHARSET));
        sb.appendField(Field.createField(collations, COLLATION_NAME));
        sb.appendFields(dimensions, DIMENSION, LOWER_BOUND, UPPER_BOUND);

        sb.appendJoin(Join.createLeftJoin().appendFields(
                Field.createField(fields, "CHARACTER_SET_ID"),
                Field.createField(charsets, "CHARACTER_SET_ID")
        ));
        sb.appendJoin(Join.createLeftJoin()
                .appendFields(
                        Field.createField(fields, "COLLATION_ID"),
                        Field.createField(collations, "COLLATION_ID")
                )
                .appendFields(
                        Field.createField(fields, "CHARACTER_SET_ID"),
                        Field.createField(collations, "CHARACTER_SET_ID")
                )
        );
        sb.appendJoin(Join.createLeftJoin().appendFields(
                Field.createField(fields, FIELD_NAME),
                Field.createField(dimensions, FIELD_NAME)
        ));
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

        if (rs.getObject(DIMENSION) != null)
            domainData.appendDimension(rs.getInt(DIMENSION), rs.getInt(LOWER_BOUND), rs.getInt(UPPER_BOUND));

        if (first) {

            domainData.setSubtype(rs.getInt(SUB_TYPE));
            domainData.setScale(Math.abs(rs.getInt(SCALE)));
            domainData.setNotNull(rs.getInt(NULL_FLAG) == 1);
            domainData.setRemarks(rs.getString(DESCRIPTION));
            domainData.setComputedBy(rs.getString(COMPUTED_SOURCE));
            domainData.setDefaultValue(domainData.processedDefaultValue(rs.getString(DEFAULT_SOURCE), false));
            domainData.setSQLType(DatabaseTypeConverter.getSqlTypeFromRDBType(rs.getInt(TYPE), rs.getInt(SUB_TYPE)));
            domainData.setTypeName(DatabaseTypeConverter.getDataTypeName(rs.getInt(TYPE), rs.getInt(SUB_TYPE), rs.getInt(SCALE)));

            domainData.setSize(rs.getInt(FIELD_LENGTH));
            if (rs.getInt(FIELD_PRECISION) != 0)
                domainData.setSize(rs.getInt(FIELD_PRECISION));
            if (rs.getInt(CHAR_LENGTH) != 0)
                domainData.setSize(rs.getInt(CHAR_LENGTH));
            if (rs.getInt(SEGMENT_LENGTH) != 0)
                domainData.setSize(rs.getInt(SEGMENT_LENGTH));

            String charset = rs.getString(CHARSET);
            domainData.setCharset(MiscUtils.isNull(charset) ? "" : charset.trim());

            String collate = rs.getString(COLLATION_NAME);
            domainData.setCollate(MiscUtils.isNull(collate) ? "" : collate.trim());

            String check = rs.getString(VALIDATION_SOURCE);
            if (!MiscUtils.isNull(check)) {
                check = check.trim();

                if (check.toUpperCase().startsWith("CHECK"))
                    check = check.substring(5).trim();

                if (check.startsWith("(") && check.endsWith(")"))
                    check = check.substring(1, check.length() - 1);
            }
            domainData.setCheck(check);
        }

        return null;
    }

    @Override
    public void prepareLoadingInfo() {
        domainData = new ColumnData(getHost().getDatabaseConnection());
        domainData.setColumnName(getName());
    }

    @Override
    public int getType() {
        return isSystem() ? SYSTEM_DOMAIN : DOMAIN;
    }

    @Override
    public String getMetaDataKey() {
        return META_TYPES[getType()];
    }

    @Override
    protected String getFieldName() {
        return FIELD_NAME;
    }

    @Override
    protected Table getMainTable() {
        return Table.createTable("RDB$FIELDS", "F");
    }

    @Override
    public boolean isAnyRowsResultSet() {
        return true;
    }

    @Override
    public boolean allowsChildren() {
        return false;
    }

    @Override
    public void finishLoadingInfo() {
    }

}

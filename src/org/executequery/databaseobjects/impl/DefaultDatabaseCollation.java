package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.sql.sqlbuilder.Field;
import org.executequery.sql.sqlbuilder.Join;
import org.executequery.sql.sqlbuilder.SelectBuilder;
import org.executequery.sql.sqlbuilder.Table;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.SQLUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class DefaultDatabaseCollation extends AbstractDatabaseObject {


    /**
     * The set of characters for which collation is created
     */
    private String characterSet;

    /**
     * The collation that this collation is based on
     */
    private String baseCollate;

    /**
     * Is the collation based on collation from external file
     */
    private boolean isExternal = false;

    /**
     * Whether trailing spaces are taken into account when comparing
     */
    private boolean isPadSpace;

    /**
     * Is the comparison case-sensitive
     */
    private boolean isCaseSensitive;

    /**
     * Is the comparison accent-sensitive
     */
    private boolean isAccentSensitive;

    /**
     * List of available COLLATION attributes
     */
    private String attributes;

    private static final String BASE_COLLATE = "BASE_COLLATION_NAME";
    private static final String COLLATION_ATTRIBUTES = "COLLATION_ATTRIBUTES";
    private static final String ATTRIBUTES = "SPECIFIC_ATTRIBUTES";
    private static final String DESCRIPTION = "DESCRIPTION";

    public DefaultDatabaseCollation(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
    }

    @Override
    public boolean allowsChildren() {
        return false;
    }

    @Override
    protected String getFieldName() {
        return "COLLATION_NAME";
    }

    @Override
    protected Table getMainTable() {
        return Table.createTable("RDB$COLLATIONS", "CO");
    }

    @Override
    protected SelectBuilder builderCommonQuery() {
        SelectBuilder sb = new SelectBuilder(getHost().getDatabaseConnection());
        Table collates = getMainTable();
        Table charsets = Table.createTable("RDB$CHARACTER_SETS", "CH");
        sb.appendFields(collates, getFieldName(), BASE_COLLATE, ATTRIBUTES, DESCRIPTION, COLLATION_ATTRIBUTES);
        sb.appendFields(charsets, CHARACTER_SET_NAME);
        sb.appendJoin(Join.createLeftJoin().appendFields(Field.createField(collates, CHARACTER_SET_ID), Field.createField(charsets, CHARACTER_SET_ID)));
        sb.setOrdering(getObjectField().getFieldTable());
        return sb;
    }

    @Override
    public Object setInfoFromSingleRowResultSet(ResultSet rs, boolean first) throws SQLException {
        setCharacterSet(getFromResultSet(rs, CHARACTER_SET_NAME));
        setBaseCollate(getFromResultSet(rs, BASE_COLLATE));
        setAttributes(getFromResultSet(rs, ATTRIBUTES));
        setRemarks(getFromResultSet(rs, DESCRIPTION));
        int collationAttributes = rs.getInt(COLLATION_ATTRIBUTES);
        setPadSpace((collationAttributes & 1) == 1);
        collationAttributes = collationAttributes >> 1;
        setCaseSensitive((collationAttributes & 1) == 0);
        collationAttributes = collationAttributes >> 1;
        setAccentSensitive((collationAttributes & 1) == 0);
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
        return NamedObject.COLLATION;
    }

    @Override
    public String getMetaDataKey() {
        return NamedObject.META_TYPES[COLLATION];
    }

    public String getCharacterSet() {
        if (characterSet == null || isMarkedForReload())
            getObjectInfo();
        return characterSet;
    }

    public String getBaseCollate() {
        if (baseCollate == null || isMarkedForReload())
            getObjectInfo();
        return baseCollate;
    }

    public boolean isExternal() {
        if (isMarkedForReload())
            getObjectInfo();
        return isExternal;
    }

    public boolean isPadSpace() {
        if (isMarkedForReload())
            getObjectInfo();
        return isPadSpace;
    }

    public boolean isCaseSensitive() {
        if (isMarkedForReload())
            getObjectInfo();
        return isCaseSensitive;
    }

    public boolean isAccentSensitive() {
        if (isMarkedForReload())
            getObjectInfo();
        return isAccentSensitive;
    }

    public String getAttributes() {
        if (attributes == null || isMarkedForReload())
            getObjectInfo();
        return attributes;
    }

    public void setCharacterSet(String characterSet) {
        this.characterSet = characterSet;
    }

    public void setBaseCollate(String baseCollate) {
        this.baseCollate = baseCollate;
    }

    public void setExternal(boolean external) {
        isExternal = external;
    }

    public void setPadSpace(boolean padSpace) {
        isPadSpace = padSpace;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        isCaseSensitive = caseSensitive;
    }

    public void setAccentSensitive(boolean accentSensitive) {
        isAccentSensitive = accentSensitive;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getCreateSQLText() {
        return SQLUtils.generateCreateCollation(getName(), getCharacterSet(), getBaseCollate(),
                getAttributes(), isPadSpace(), isCaseSensitive(), isAccentSensitive(), isExternal());
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("COLLATION", getName(), getHost().getDatabaseConnection());
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {
        return getCreateSQLText();
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {
        String comparingSqlQuery = databaseObject.getCompareCreateSQL();
        return !Objects.equals(this.getCompareCreateSQL(), comparingSqlQuery) ?
                getDropSQL() + comparingSqlQuery : "/* there are no changes */\n";
    }

}

package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.NamedObject;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.SQLUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

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


    private static final String CHARSET = "CHARSET";
    private static final String BASE_COLLATE = "BASE_COLLATE";
    private static final String COLLATION_ATTRIBUTES = "COLLATION_ATTRIBUTES";
    private static final String ATTRIBUTES = "ATTRIBUTES";
    private static final String DESCRIPTION = "DESCRIPTION";

    public DefaultDatabaseCollation(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
    }

    @Override
    public boolean allowsChildren() {
        return false;
    }

    @Override
    protected String queryForInfo() {

        String query = "select ch.RDB$CHARACTER_SET_NAME as " + CHARSET + ",\n" +
                "co.RDB$BASE_COLLATION_NAME as " + BASE_COLLATE + ",\n" +
                "co.RDB$COLLATION_ATTRIBUTES as " + COLLATION_ATTRIBUTES + ",\n" +
                "co.RDB$SPECIFIC_ATTRIBUTES as " + ATTRIBUTES + ",\n" +
                "co.RDB$DESCRIPTION as " + DESCRIPTION + "\n" +
                "from RDB$COLLATIONS co left join RDB$CHARACTER_SETS ch on co.RDB$CHARACTER_SET_ID=ch.RDB$CHARACTER_SET_ID \n" +
                "where co.RDB$COLLATION_NAME=?";

        return query;
    }

    @Override
    protected void setInfoFromResultSet(ResultSet rs) throws SQLException {
        if (rs.next()) {
            setCharacterSet(getFromResultSet(rs, CHARSET));
            setBaseCollate(getFromResultSet(rs, BASE_COLLATE));
            setAttributes(getFromResultSet(rs, ATTRIBUTES));
            setRemarks(getFromResultSet(rs, DESCRIPTION));
            int collationAttributes = rs.getInt(COLLATION_ATTRIBUTES);
            setPadSpace((collationAttributes & 1) == 1);
            collationAttributes = collationAttributes >> 1;
            setCaseSensitive((collationAttributes & 1) == 0);
            collationAttributes = collationAttributes >> 1;
            setAccentSensitive((collationAttributes & 1) == 0);
        }
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
        return SQLUtils.generateDefaultDropQuery("COLLATION", getName());
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {
        return getCreateSQLText();
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {
        DefaultDatabaseCollation comparingCollation = (DefaultDatabaseCollation) databaseObject;
        return getDropSQL() + "\n" +
                SQLUtils.generateCreateCollation(comparingCollation.getName(), comparingCollation.getCharacterSet(),
                comparingCollation.getBaseCollate(), comparingCollation.getAttributes(), comparingCollation.isPadSpace(),
                comparingCollation.isCaseSensitive(), comparingCollation.isAccentSensitive(), isExternal());
    }

}

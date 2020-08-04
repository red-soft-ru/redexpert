/*
 * DefaultDatabaseColumn.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.NamedObject;
import org.underworldlabs.jdbc.DataSourceException;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

/**
 * @author takisd
 */
public class DefaultDatabaseColumn extends AbstractDatabaseObjectElement
        implements DatabaseColumn {

    /**
     * the database column size
     */
    private int columnSize;

    /**
     * the database column scale
     */
    private int columnScale;

    /**
     * the database column subtype
     */
    private int columnSubtype;

    /**
     * the parent object's name
     */
    private String parentsName;

    /**
     * column required indicator
     */
    private boolean required;

    /**
     * the column data type name
     */
    private String typeName;

    /**
     * the java.sql.Type int value
     */
    private int typeInt;

    /**
     * primary key flag
     */
    private boolean primaryKey;

    /**
     * foreign key flag
     */
    private boolean foreignKey;

    /**
     * unique flag
     */
    private boolean unique;

    /**
     * the column default value
     */
    private String defaultValue;

    /**
     * generated column
     */
    private boolean isGenerated;

    /**
     * the column source
     */
    private String computedSource;

    /**
     * the column meta data map
     */
    private Map<String, String> metaData;

    private String domain;

    private boolean identity;

    public DefaultDatabaseColumn() {
    }

    public List<ColumnConstraint> getConstraints() {
        return null;
    }

    @Override
    public void setColumnDescription(String description) {
        setRemarks(description);
    }

    @Override
    public String getColumnDescription() {
        return getRemarks();
    }

    @Override
    public boolean isIdentity() {
        return identity;
    }

    @Override
    public void setIdentity(boolean flag) {
        identity = flag;
    }

    public int getColumnSize() {
        return columnSize;
    }

    public void setColumnSize(int columnSize) {
        this.columnSize = columnSize;
    }

    public int getColumnScale() {
        return columnScale;
    }

    public void setColumnScale(int columnScale) {
        this.columnScale = columnScale;
    }

    public int getColumnSubtype() {
        return columnSubtype;
    }

    public void setColumnSubtype(int columnSubtype) {
        this.columnSubtype = columnSubtype;
    }

    public String getParentsName() {
        return parentsName;
    }

    public void setParentsName(String parentsName) {
        this.parentsName = parentsName;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public boolean isGenerated() {
        return isGenerated;
    }

    public void setGenerated(boolean generated) {
        isGenerated = generated;
    }

    public String getComputedSource() {
        return computedSource;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setComputedSource(String computedSource) {
        this.computedSource = computedSource;
    }

    /**
     * Returns the table column's default value.
     *
     * @return the default value
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default column value to that specified.
     *
     * @param defaultValue the defauilt value for this column
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * The type identfier int from java.sql.Type.
     *
     * @return the type int
     */
    public int getTypeInt() {
        return typeInt;
    }

    /**
     * Sets the type int to that specified.
     *
     * @param typeInt java.sql.Type int value
     */
    public void setTypeInt(int typeInt) {
        this.typeInt = typeInt;
    }

    /**
     * Indicates whether this column is a primary key column.
     *
     * @return true | false
     */
    public boolean isPrimaryKey() {
        return primaryKey;
    }

    /**
     * Indicates whether this column is a foreign key column.
     *
     * @return true | false
     */
    public boolean isForeignKey() {
        return foreignKey;
    }

    /**
     * Indicates whether this column has any constraints.
     *
     * @return true | false
     */
    public boolean hasConstraints() {

        return isForeignKey() || isPrimaryKey() || isUnique();
    }

    /**
     * Indicates whether this column is unique.
     *
     * @return true | false
     */
    public boolean isUnique() {
        return unique;
    }

    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    public int getType() {
        return TABLE_COLUMN;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public void setForeignKey(boolean foreignKey) {
        this.foreignKey = foreignKey;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    /**
     * Returns the display name of this object.
     *
     * @return the display name
     */
    /*
    public String getDisplayName() {
        String _name = getName();
        if (_name != null) {
            return _name.toUpperCase();
        }
        return _name;
    }
    */

    /**
     * Returns whether this column is a date type or
     * extension of.
     * <p>
     * ie. Types.DATE, Types.TIME, Types.TIMESTAMP.
     *
     * @return true | false
     */
    public boolean isDateDataType() {
        int _type = getTypeInt();
        return _type == Types.DATE ||
                _type == Types.TIME ||
                _type == Types.TIMESTAMP;
    }

    /**
     * Returns whether this column's type does not have
     * a precision such as in a BIT data type.
     *
     * @return true | false
     */
    public boolean isNonPrecisionType() {
        return getTypeInt() == Types.BIT;
    }

    /**
     * Returns a formatted string representation of the
     * column's data type and size - eg. VARCHAR(10).
     *
     * @return the formatted type string
     */
    public String getFormattedDataType() {

        String typeString = getTypeName() == null ? "" : getTypeName();

        boolean generated = isGenerated();

        if (generated) {
            StringBuilder buffer = new StringBuilder();
            buffer.append("COMPUTED BY ");
            buffer.append(getComputedSource());

            return buffer.toString();
        }

        StringBuilder buffer = new StringBuilder();

        if (computedSource != null && !computedSource.isEmpty()) {
            buffer.append("<domain>");
            buffer.append(computedSource);
            buffer.append("/*");
        }

        buffer.append(typeString.replace("<0", String.valueOf(this.getColumnSubtype())));

        // if the type doesn't end with a digit or it
        // is a char type then add the size - attempt
        // here to avoid int4, int8 etc. type values

        int _type = getTypeInt();
        if (!typeString.matches("\\b\\D+\\d+\\b") ||
                (_type == Types.CHAR ||
                        _type == Types.VARCHAR ||
                        _type == Types.LONGVARCHAR ||
                        _type == Types.LONGVARBINARY ||
                        _type == Types.BLOB)) {

            if (getColumnSize() > 0 && !isDateDataType() && !isNonPrecisionType()) {

                buffer.append("(");
                buffer.append(getColumnSize());

                if (getColumnScale() > 0) {
                    buffer.append(",");
                    buffer.append(getColumnScale());
                }
                buffer.append(")");
            }
        }

        if (computedSource != null && !computedSource.isEmpty()) {
            buffer.append("*/");
        }

        return buffer.toString();
    }

    /**
     * Drops this named object in the database.
     *
     * @return drop statement result
     */
    public int drop() throws DataSourceException {
        return 0;
    }

    @Override
    public boolean allowsChildren() {
        return false;
    }

    /**
     * Override to clear the meta data.
     */
    public void reset() {
        super.reset();
        metaData = null;
    }

    /**
     * Returns the meta data as a map of this column.
     *
     * @return the meta data
     */
    public Map<String, String> getMetaData() throws DataSourceException {
        NamedObject _parent = getParent();
        if (!(_parent instanceof DatabaseObject)) {
            return null;
        }

        if (!isMarkedForReload() && metaData != null) {
            return metaData;
        }

        ResultSet rs = null;
        try {

            DatabaseHost databaseHost = ((DatabaseObject) _parent).getHost();
            String _catalog = databaseHost.getCatalogNameForQueries(getCatalogName());
            String _schema = databaseHost.getSchemaNameForQueries(getSchemaName());

            DatabaseMetaData dmd = databaseHost.getDatabaseMetaData();
            rs = dmd.getColumns(_catalog, _schema, getParentsName(), getName());

            if (rs.next()) {

                metaData = resultSetRowToMap(rs);
            }
            return metaData;

        } catch (SQLException e) {

            throw new DataSourceException(e);

        } finally {

            releaseResources(rs, null);
        }
    }

}



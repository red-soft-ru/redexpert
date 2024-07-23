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

import org.executequery.databaseobjects.*;
import org.executequery.gui.browser.ColumnData;
import org.underworldlabs.jdbc.DataSourceException;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

    private boolean domainNotNull;

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
    private String domainDefaultValue;

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

    String charset;

    String collate;
    private List<ColumnData.Dimension> dimensions;

    private int position;

    public DefaultDatabaseColumn() {
    }

    List<ColumnConstraint> constraints;

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

    public boolean isDomainNotNull() {
        return domainNotNull;
    }

    public void setDomainNotNull(boolean domainNotNull) {
        this.domainNotNull = domainNotNull;
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

    public String getDomainDefaultValue() {
        return domainDefaultValue;
    }

    public void setDomainDefaultValue(String domainDefaultValue) {
        this.domainDefaultValue = domainDefaultValue;
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
                _type == Types.TIME_WITH_TIMEZONE ||
                _type == Types.TIMESTAMP ||
                _type == Types.TIMESTAMP_WITH_TIMEZONE
                ;
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
            String buffer = "COMPUTED BY " +
                    getComputedSource();

            return buffer;
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

            if (isEditSize() && getColumnSize() > 0 && !typeName.contains(String.valueOf(getColumnSize()))
                    && !isDateDataType() && !isNonPrecisionType()) {

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

    public boolean isEditSize() {
        return getTypeName() != null && (getTypeInt() == Types.NUMERIC
                || getTypeInt() == Types.CHAR
                || getTypeInt() == Types.VARCHAR
                || getTypeInt() == Types.DECIMAL
                || getTypeInt() == Types.BLOB
                || getTypeInt() == Types.LONGVARCHAR
                || getTypeInt() == Types.LONGVARBINARY
                || getTypeName().equalsIgnoreCase("VARCHAR")
                || getTypeName().equalsIgnoreCase("CHAR"))
                || getTypeName().equalsIgnoreCase(T.DECFLOAT);
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

            DatabaseMetaData dmd = databaseHost.getDatabaseMetaData();
            rs = dmd.getColumns(null, null, getParentsName(), getName());

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


    @Override
    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    @Override
    public String getCollate() {
        return collate;
    }

    public void setCollate(String collate) {
        this.collate = collate;
    }

    public List<ColumnConstraint> getConstraints() {
        return constraints;
    }

    public void addConstraint(ColumnConstraint constraint) {
        if (constraints == null)
            constraints = new ArrayList<>();
        constraints.add(constraint);
    }

    public void appendDimension(int orderNumber, int lowerBound, int upperBound) {
        if (dimensions == null)
            dimensions = new ArrayList<>();
        ColumnData.Dimension dimension = new ColumnData.Dimension(lowerBound, upperBound);
        if (orderNumber >= dimensions.size())
            dimensions.add(dimension);
        else dimensions.add(orderNumber, dimension);
    }

    public List<ColumnData.Dimension> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<ColumnData.Dimension> dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public void setPosition(int position) {
        this.position = position;
    }
}



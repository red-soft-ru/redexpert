/*
 * ColumnData.java
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

package org.executequery.gui.browser;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.impl.DefaultDatabaseDomain;
import org.executequery.databaseobjects.impl.DefaultDatabaseMetaTag;
import org.executequery.log.Log;
import org.underworldlabs.util.MiscUtils;

/**
 * This class represents a single table
 * column definition. This includes data types
 * sizes, scales and key referencing meta data.
 *
 * @author Takis Diakoumis
 * @version $Revision: 1784 $
 * @date $Date: 2017-09-19 00:55:31 +1000 (Tue, 19 Sep 2017) $
 */
public class ColumnData implements Serializable {

    static final long serialVersionUID = -4937385038396757064L;

    public static final int VALUE_REQUIRED = 0;

    public static final int VALUE_NOT_REQUIRED = 1;

    /**
     * the catalog for this column
     */
    private String catalog;

    /**
     * the schema for this column
     */
    private String schema;

    /**
     * The table this column belongs to
     */
    private String tableName;

    /**
     * The name of this column
     */
    private String columnName;

    /**
     * The name of the SQL type of this column
     */
    private String columnType;

    /**
     * The key of this column - if any
     * (ie primary, foreign etc)
     */
    private String keyType;

    /**
     * Whether this column is a primary key
     */
    private boolean primaryKey;

    /**
     * Whether this column is a foreign key
     */
    private boolean foreignKey;

    /**
     * The data size of this column
     */
    private int columnSize;

    /**
     * The data scale of this column
     */
    private int columnScale;

    /**
     * Whether this column is required ie. NOT NULL
     */
    private int columnRequired;

    /**
     * The mapped SQL type
     */
    private int sqlType;

    /**
     * the column's default value
     */
    private String defaultValue;

    /**
     * This column's constraints as a <code>Vector</code>
     * of <code>ColumnConstraint</code> objects
     */
    private Vector<ColumnConstraint> columnConstraints;

    /**
     * Whether this column is a new column in the table
     */
    private boolean newColumn;

    /**
     * Whether this column is marked as to be deleted
     */
    private boolean markedDeleted;

    private String domain;

    private int domainType;

    private int domainSize = -1;

    private int domainScale = -1;

    private int domainSubType;

    private String check;

    private String description;

    private String computedBy;

    DatabaseConnection dc;

    public ColumnData(DatabaseConnection databaseConnection) {
        primaryKey = false;
        foreignKey = false;
        newColumn = false;
        keyType = null;
        dc = databaseConnection;
    }

    public ColumnData(String columnName, DatabaseConnection databaseConnection) {
        this(databaseConnection);
        this.columnName = columnName;
    }

    public ColumnData(boolean newColumn, DatabaseConnection databaseConnection) {
        this(databaseConnection);
        this.newColumn = newColumn;
    }

    public ColumnConstraint[] getColumnConstraintsArray() {
        int v_size = columnConstraints.size();
        ColumnConstraint[] cca = new ColumnConstraint[v_size];

        for (int i = 0; i < v_size; i++) {
            cca[i] = columnConstraints.get(i);
        }

        return cca;
    }

    public Vector<ColumnConstraint> getColumnConstraintsVector() {
        return columnConstraints;
    }

    public void addConstraint(ColumnConstraint cc) {
        if (columnConstraints == null) {
            columnConstraints = new Vector<ColumnConstraint>();
        }
        columnConstraints.add(cc);
    }

    public void resetConstraints() {
        if (columnConstraints != null) {
            columnConstraints.clear();
        }
    }

    public void removeConstraint(ColumnConstraint cc) {
        columnConstraints.remove(cc);
    }

    public boolean isNewColumn() {
        return newColumn;
    }

    public boolean isValid() {
        return (columnName != null && columnName.length() > 0) &&
                (tableName != null && tableName.length() > 0) &&
                (columnType != null && columnType.length() > 0);
    }

    public void setNewColumn(boolean newColumn) {
        this.newColumn = newColumn;
    }

    public int getColumnScale() {
        return columnScale;
    }

    public void setColumnScale(int columnScale) {
        this.columnScale = columnScale;
    }

    public void setNamesToUpper() {
        if (tableName != null) {
            tableName = tableName.toUpperCase();
        }
        if (columnName != null) {
            columnName = columnName.toUpperCase();
        }
    }

    @SuppressWarnings("unchecked")
    public void setValues(ColumnData cd) {
        tableName = cd.getTableName();
        columnName = cd.getColumnName();
        columnType = cd.getColumnType();
        keyType = cd.getKeyType();
        primaryKey = cd.isPrimaryKey();
        foreignKey = cd.isForeignKey();
        columnSize = cd.getColumnSize();
        columnRequired = cd.getColumnRequired();
        sqlType = cd.getSQLType();
        domain = cd.getDomain();
        dc = cd.getDatabaseConnection();
        description = cd.getDescription();
        check = cd.getCheck();
        computedBy = cd.getComputedBy();

        Vector<ColumnConstraint> constraints = cd.getColumnConstraintsVector();
        if (constraints != null) {
            columnConstraints = (Vector<ColumnConstraint>) constraints.clone();
        }
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public boolean isForeignKey() {
        return foreignKey;
    }

    public boolean isKey() {
        return primaryKey || foreignKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public void setForeignKey(boolean foreignKey) {
        this.foreignKey = foreignKey;
    }

    public void setColumnRequired(int columnRequired) {
        this.columnRequired = columnRequired;
    }

    public int getColumnRequired() {
        return columnRequired;
    }

    /**
     * Returns whether this is a required column determined by
     * whether the column allows null values.
     *
     * @return true | false
     */
    public boolean isRequired() {
        return columnRequired == VALUE_REQUIRED;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    /**
     * Returns whether this column is a date type or
     * extension of.
     * <p>
     * ie. Types.DATE, Types.TIME, Types.TIMESTAMP.
     *
     * @return true | false
     */
    public boolean isDateDataType() {
        return sqlType == Types.DATE ||
                sqlType == Types.TIME ||
                sqlType == Types.TIMESTAMP;
    }

    public boolean isCharacterType() {
        return sqlType == Types.CHAR ||
                sqlType == Types.VARCHAR ||
                sqlType == Types.LONGVARCHAR;
    }

    public boolean isNonPrecisionType() {
        return sqlType == Types.BIT;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setSQLType(int sqlType) {
        this.sqlType = sqlType;
    }

    public int getSQLType() {
        return sqlType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnSize(int columnSize) {
        this.columnSize = columnSize;
    }

    public int getColumnSize() {
        return columnSize;
    }

    public String toString() {
        return columnName == null ? ColumnConstraint.EMPTY : columnName;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public boolean isMarkedDeleted() {
        return markedDeleted;
    }

    public void setMarkedDeleted(boolean markedDeleted) {
        this.markedDeleted = markedDeleted;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String Domain) {
        domain = Domain;
        if (!MiscUtils.isNull(domain))
            getDomainInfo();
    }

    private void getDomainInfo() {
        String query = "SELECT RDB$FIELD_TYPE,RDB$FIELD_LENGTH,RDB$FIELD_SCALE,RDB$FIELD_SUB_TYPE FROM RDB$FIELDS WHERE RDB$FIELD_NAME='" +
                domain.trim() + "'";
        DefaultStatementExecutor executor = new DefaultStatementExecutor(dc, true);
        try {
            ResultSet rs = executor.execute(QueryTypes.SELECT, query).getResultSet();
            if (rs.next()) {
                domainType = rs.getInt(1);
                domainSize = rs.getInt(2);
                domainScale = rs.getInt(3);
                domainSubType = rs.getInt(4);
            }
            executor.releaseResources();
            domainType = getSqlTypeFromRDBtype(domainType, domainSubType);
            sqlType = domainType;
            columnSize = domainSize;
            columnScale = domainScale;

        } catch (Exception e) {
            Log.error(e.getMessage());
        }

    }

    public int getDomainType() {
        return domainType;
    }

    public void setDatabaseConnection(DatabaseConnection databaseConnection) {
        dc = databaseConnection;
    }

    public DatabaseConnection getDatabaseConnection() {
        return dc;
    }


    public int getSqlTypeFromRDBtype(int type, int subtype) {
        switch (type) {
            case 7:
                switch (subtype) {
                    case 1:
                        return Types.NUMERIC;
                    case 2:
                        return Types.DECIMAL;
                    default:
                        return Types.SMALLINT;
                }
            case 8:
                switch (subtype) {
                    case 1:
                        return Types.NUMERIC;
                    case 2:
                        return Types.DECIMAL;
                    default:
                        return Types.INTEGER;
                }
            case 10:
                return Types.FLOAT;
            case 12:
                return Types.DATE;
            case 13:
                return Types.TIME;
            case 14:
                switch (subtype) {
                    case 0:
                        return Types.BINARY;
                    case 1:
                        return Types.CHAR;
                }
            case 16:
                switch (subtype) {
                    case 1:
                        return Types.NUMERIC;
                    case 2:
                        return Types.DECIMAL;
                    default:
                        return Types.BIGINT;
                }
            case 27:
                return Types.DOUBLE;
            case 35:
                return Types.TIMESTAMP;
            case 37:
                switch (subtype) {
                    case 0:
                        return Types.VARBINARY;
                    case 1:
                        return Types.VARCHAR;
                }
            case 261:
                switch (subtype) {
                    case 1:
                        return Types.LONGVARCHAR;
                    case 2:
                        return Types.LONGVARBINARY;
                    default:
                        return Types.BLOB;
                }
            default:
                return 0;
        }
    }

    /**
     * Returns a formatted string representation of the
     * column's data type and size - eg. VARCHAR(10).
     *
     * @return the formatted type string
     */
    public String getFormattedDataType() {

        String typeString = getColumnType();
        if (StringUtils.isBlank(typeString)) {

            return "";
        }

        StringBuilder sb = new StringBuilder(typeString);

        // if the type doesn't end with a digit or it
        // is a char type then add the size - attempt
        // here to avoid int4, int8 etc. type values

        int type = getSQLType();
        if (!typeString.matches("\\b\\D+\\d+\\b") ||
                (type == Types.CHAR ||
                        type == Types.VARCHAR ||
                        type == Types.LONGVARCHAR)) {

            if (getColumnSize() > 0 && !isDateDataType()
                    && !isNonPrecisionType()) {
                sb.append("(");
                sb.append(getColumnSize());

                if (getColumnScale() > 0) {
                    sb.append(",");
                    sb.append(getColumnScale());
                }
                sb.append(")");
            }
        }
        return sb.toString();
    }

    public void setCheck(String Check) {
        check = Check;
    }

    public String getCheck() {
        return check;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setComputedBy(String computedBy) {
        this.computedBy = computedBy;
    }

    public String getComputedBy() {
        return computedBy;
    }
}







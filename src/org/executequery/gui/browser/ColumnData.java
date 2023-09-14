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

import org.apache.commons.lang.StringUtils;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.T;
import org.executequery.databaseobjects.impl.DefaultDatabaseDomain;
import org.executequery.databaseobjects.impl.DefaultDatabaseObject;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.gui.table.Autoincrement;
import org.executequery.log.Log;
import org.underworldlabs.util.MiscUtils;

import java.io.Serializable;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * This class represents a single table
 * column definition. This includes data types
 * sizes, scales and key referencing metadata.
 *
 * @author Takis Diakoumis
 */
public class ColumnData implements Serializable {

    static final long serialVersionUID = -4937385038396757064L;

    public static final int INPUT_PARAMETER = 0;

    public static final int OUTPUT_PARAMETER = 1;

    public static final int VARIABLE = 2;

    public static final int TYPE_OF_FROM_DOMAIN = 0;

    public static final int TYPE_OF_FROM_COLUMN = 1;

    private boolean descriptionAsSingleComment = false;

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
     * (ie primary, foreign etc.)
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
     * The subtype of this column
     */
    private int columnSubtype;

    /**
     * Whether this column is required i.e. NOT NULL
     */
    private boolean notNull;

    /**
     * The mapped SQL type
     */
    private int sqlType;

    /**
     * the column's default value
     */
    private DefaultValue defaultValue;

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

    /**
     * Using domain for column
     */
    private String domain;

    /**
     * Domain type
     */
    private int domainType;

    /**
     * Domain type name
     */
    private String domainTypeName;

    /**
     * Domain size
     */
    private int domainSize = -1;

    /**
     * Domain scale
     */
    private int domainScale = -1;

    /**
     * Domain subtype
     */
    private int domainSubType;

    /**
     * Domain character set
     */
    private String domainCharset;

    /**
     * Domain checking string
     */
    private String domainCheck;

    /**
     * Domain description
     */
    private String domainDescription;

    /**
     * Whether this domain is null or not
     */
    private boolean domainNotNull;

    /**
     * Default value for domain
     */
    private String domainDefault;

    /**
     * Check string
     */
    private String check;

    /**
     * Description for column
     */
    private String description;

    /**
     * Whether this column is computed
     */
    private String computedBy;

    /**
     * Whether this domain is computed
     */
    private String domainComputedBy;

    /**
     * Auto increment panel
     */
    Autoincrement ai;

    /**
     * Current database connection
     */
    DatabaseConnection dc;

    /**
     * Current character set
     */
    private String charset;

    /**
     * Copy of current column
     */
    ColumnData copy;

    /**
     * Whether column parameter is in or out
     */
    int typeParameter;

    /**
     * Whether column is instance of
     */
    boolean typeOf;


    /**
     * Whether column is type of domain or column
     */
    int typeOfFrom;

    private String collate;

    private String domainCollate;

    String mechanism;

    boolean cstring;

    DefaultDatabaseObject table;
    List<NamedObject> tables;
    String columnTable;
    List<String> columns;

    private boolean cursor;

    private boolean scroll;

    private String selectOperator;

    private List<ColumnData.Dimension> dimensions;
    private List<ColumnData.Dimension> domainDimensions;

    DefaultStatementExecutor executor;

    public ColumnData(DatabaseConnection databaseConnection) {
        primaryKey = false;
        foreignKey = false;
        newColumn = false;
        keyType = null;
        dc = databaseConnection;
        ai = new Autoincrement();
        setCharset("");
        executor = new DefaultStatementExecutor(dc, true);
        tables = new ArrayList<>();
        columns = new ArrayList<>();
        defaultValue = new DefaultValue();
    }

    public ColumnData(DatabaseConnection databaseConnection, DatabaseColumn databaseColumn) {
        this(databaseConnection);
        setValues(databaseColumn, true);
    }

    public ColumnData(DatabaseConnection databaseConnection, DatabaseColumn databaseColumn, boolean loadDomainInfo) {
        this(databaseConnection);
        setValues(databaseColumn, loadDomainInfo);
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
        return columnConstraints.toArray(new ColumnConstraint[columnConstraints.size()]);
    }

    public Vector<ColumnConstraint> getColumnConstraintsVector() {
        return columnConstraints;
    }

    public void addConstraint(ColumnConstraint cc) {
        if (columnConstraints == null)
            columnConstraints = new Vector<>();
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

    public void setColumnSubtype(int columnSubtype) {
        this.columnSubtype = columnSubtype;
    }

    public int getColumnSubtype() {
        return columnSubtype;
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
        columnScale = cd.getColumnScale();
        notNull = cd.isNotNull();
        sqlType = cd.getSQLType();
        domain = cd.getDomain();
        dc = cd.getDatabaseConnection();
        description = cd.getDescription();
        check = cd.getCheck();
        computedBy = cd.getComputedBy();
        defaultValue = cd.getDefaultValue();
        table = cd.getTable();
        columnTable = cd.getColumnTable();
        typeOf = cd.isTypeOf();
        dimensions = cd.getDimensions();

        Vector<ColumnConstraint> constraints = cd.getColumnConstraintsVector();
        if (constraints != null) {
            columnConstraints = (Vector<ColumnConstraint>) constraints.clone();
        }
    }

    public void setValues(DatabaseColumn cd, boolean loadDomainInfo) {
        setTableName(cd.getParentsName());
        setColumnName(cd.getName());
        setColumnType(cd.getTypeName());
        setPrimaryKey(cd.isPrimaryKey());
        setForeignKey(cd.isForeignKey());
        setColumnSize(cd.getColumnSize());
        setColumnScale(cd.getColumnScale());
        setNotNull(cd.isRequired());
        setDomainNotNull(cd.isDomainNotNull());
        setSQLType(cd.getTypeInt());
        setDomain(cd.getDomain(), loadDomainInfo);
        setDescription(cd.getColumnDescription());
        setComputedBy(cd.getComputedSource());
        setDefaultValue(cd.getDefaultValue());
        setDomainDefault(cd.getDomainDefaultValue());
        setDimensions(cd.getDimensions());
        if (cd.isIdentity())
            ai.setIdentity(true);
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

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public boolean isNotNull() {
        return notNull;
    }

    /**
     * Returns whether this is a required column determined by
     * whether the column allows null values.
     *
     * @return true | false
     */
    public boolean isRequired() {
        return notNull;
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
                sqlType == Types.TIMESTAMP ||
                sqlType == Types.TIME_WITH_TIMEZONE ||
                sqlType == Types.TIMESTAMP_WITH_TIMEZONE;
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
        ai.setFieldName(columnName);
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

    public DefaultValue getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(DefaultValue defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue.value = defaultValue;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        setDomain(domain, true);
    }

    public void setDomain(String domain, boolean loadDomainInfo) {
        this.domain = domain;
        if (!MiscUtils.isNull(domain) && loadDomainInfo) {
            getDomainInfo();
        }

    }

    public void setDomainType(int domainType) {
        this.domainType = domainType;
    }

    public void setDomainCharset(String domainCharset) {
        this.domainCharset = domainCharset;
    }

    public void setDomainCheck(String domainCheck) {
        this.domainCheck = domainCheck;
    }

    public void setDomainDescription(String domainDescription) {
        this.domainDescription = domainDescription;
    }

    public void setDomainNotNull(boolean domainNotNull) {
        this.domainNotNull = domainNotNull;
    }

    public void setDomainDefault(String domainDefault) {
        this.domainDefault = domainDefault;
    }

    public void setDomainComputedBy(String domainComputedBy) {
        if (!MiscUtils.isNull(domainComputedBy) && domainComputedBy.startsWith("(")
                && domainComputedBy.endsWith(")"))
            domainComputedBy = domainComputedBy.substring(1, domainComputedBy.length() - 1);
        this.domainComputedBy = domainComputedBy;
    }

    public int getDomainSize() {
        return domainSize;
    }

    public void setDomainSize(int domainSize) {
        this.domainSize = domainSize;
    }

    public int getDomainScale() {
        return domainScale;
    }

    public void setDomainScale(int domainScale) {
        this.domainScale = domainScale;
    }

    public int getDomainSubType() {
        return domainSubType;
    }

    public void setDomainSubType(int domainSubType) {
        this.domainSubType = domainSubType;
    }

    public String getDomainCollate() {
        return domainCollate;
    }

    public void setDomainCollate(String domainCollate) {
        this.domainCollate = domainCollate;
    }

    private void getDomainInfo() {
        domain = MiscUtils.trimEnd(domain);

        DefaultDatabaseDomain defaultDatabaseDomain = (DefaultDatabaseDomain) ConnectionsTreePanel.getNamedObjectFromHost(dc, NamedObject.DOMAIN, domain);
        if (defaultDatabaseDomain == null)
            defaultDatabaseDomain = (DefaultDatabaseDomain) ConnectionsTreePanel.getNamedObjectFromHost(dc, NamedObject.SYSTEM_DOMAIN, domain);
        if (defaultDatabaseDomain == null) {
            DatabaseObjectNode hostNode = ConnectionsTreePanel.getPanelFromBrowser().getHostNode(dc);
            for (DatabaseObjectNode metaTagNode : hostNode.getChildObjects()) {
                if (metaTagNode.getMetaDataKey().equals(NamedObject.META_TYPES[NamedObject.DOMAIN]) ||
                        metaTagNode.getMetaDataKey().equals(NamedObject.META_TYPES[NamedObject.SYSTEM_DOMAIN])) {
                    ConnectionsTreePanel.getPanelFromBrowser().reloadPath(metaTagNode.getTreePath());
                }
            }
            defaultDatabaseDomain = (DefaultDatabaseDomain) ConnectionsTreePanel.getNamedObjectFromHost(dc, NamedObject.DOMAIN, domain);
            if (defaultDatabaseDomain == null)
                defaultDatabaseDomain = (DefaultDatabaseDomain) ConnectionsTreePanel.getNamedObjectFromHost(dc, NamedObject.SYSTEM_DOMAIN, domain);
        }

        boolean find = defaultDatabaseDomain != null;
        if (find) {
            domainType = defaultDatabaseDomain.getDomainData().domainType;
            domainSize = defaultDatabaseDomain.getDomainData().domainSize;
            domainScale = defaultDatabaseDomain.getDomainData().domainScale;
            domainSubType = defaultDatabaseDomain.getDomainData().domainSubType;
            domainCharset = defaultDatabaseDomain.getDomainData().domainCharset;
            domainCheck = defaultDatabaseDomain.getDomainData().domainCheck;
            domainDescription = defaultDatabaseDomain.getDomainData().domainDescription;
            domainNotNull = defaultDatabaseDomain.getDomainData().domainNotNull;
            domainDefault = defaultDatabaseDomain.getDomainData().domainDefault;
            domainComputedBy = defaultDatabaseDomain.getDomainData().domainComputedBy;
            domainCollate = defaultDatabaseDomain.getDomainData().domainCollate;
            domainDimensions = defaultDatabaseDomain.getDomainData().getDimensions();

        }
        sqlType = domainType;
        columnSize = domainSize;
        columnScale = domainScale;
        columnSubtype = domainSubType;
        setCharset(domainCharset);
        setCollate(domainCollate);
        setDimensions(domainDimensions);
        if (!find)
            Log.error("Error get Domain '" + domain + "'");


    }

    public int getDomainType() {
        return domainType;
    }

    public void setDatabaseConnection(DatabaseConnection databaseConnection) {
        dc = databaseConnection;
        executor.setDatabaseConnection(dc);
    }

    public DatabaseConnection getDatabaseConnection() {
        return dc;
    }

    public boolean isLOB() {
        return sqlType == Types.BINARY || sqlType == Types.BLOB || sqlType == Types.CLOB ||
                sqlType == Types.LONGNVARCHAR || sqlType == Types.LONGVARBINARY || sqlType == Types.LONGVARCHAR ||
                sqlType == Types.NCLOB;
    }

    /**
     * Returns a formatted string representation of the
     * column's data type and size - eg. VARCHAR(10).
     *
     * @return the formatted type string
     */
    public String getFormattedDataType() {

        if (typeOf) {
            if (getTypeOfFrom() == TYPE_OF_FROM_DOMAIN) {
                return "TYPE OF " + getFormattedDomain();
            } else return "TYPE OF COLUMN " + getFormattedTable() + "." + getFormattedColumnTable();
        }
        String typeString = getColumnType();
        if (StringUtils.isBlank(typeString)) {

            return "";
        }
        if (typeString.contains("<0")) {
            if (columnSubtype < 0)
                typeString = typeString.replace("<0", String.valueOf(columnSubtype));
            else typeString = typeString.replace("<0", "0");
        }
        StringBuilder sb = new StringBuilder(typeString);

        // if the type doesn't end with a digit, or it
        // is a char type then add the size - attempt
        // here to avoid int4, int8 etc. type values

        int type = getSQLType();
        if (!typeString.matches("\\b\\D+\\d+\\b") ||
                (type == Types.CHAR ||
                        type == Types.VARCHAR ||
                        type == Types.BLOB || type == Types.LONGVARCHAR
                        || type == Types.LONGVARBINARY)) {
            if (type == Types.BLOB || type == Types.LONGVARCHAR
                    || type == Types.LONGVARBINARY) {
                if (getColumnSize() != 80)
                    sb.append(" SEGMENT SIZE ").append(getColumnSize());
            } else if (isEditSize() && getColumnSize() > 0 && !isDateDataType()
                    && !isNonPrecisionType()
                    && (!getColumnType().equalsIgnoreCase(T.DECFLOAT) || getColumnType().equalsIgnoreCase(T.DECFLOAT)
                    && (getColumnSize() == 16 || getColumnSize() == 34))
            ) {
                sb.append("(");
                sb.append(getColumnSize());

                if (getColumnScale() > 0) {
                    sb.append(",");
                    sb.append(getColumnScale());
                }
                sb.append(")");
            }
            if (!MiscUtils.isNull(getCharset()) && dc != null && !getCharset().equalsIgnoreCase(dc.getDBCharset())) {
                sb.append(" CHARACTER SET ").append(getCharset());
            }
            if (dimensions != null) {
                sb.append("[");
                boolean first = true;
                for (Dimension dimension : dimensions) {
                    if (!first)
                        sb.append(",");
                    first = false;
                    if (dimension.lowerBound != 1) {
                        sb.append(dimension.lowerBound).append(":");
                    }
                    sb.append(dimension.upperBound);
                }
                sb.append("]");
            }
        }
        return sb.toString();
    }

    public String getFormattedDomainDataType() {
        sqlType = domainType;
        columnSize = domainSize;
        columnScale = domainScale;
        columnSubtype = domainSubType;
        setCharset(domainCharset);
        setCollate(domainCollate);
        setCheck(domainCheck);
        setDefaultValue(domainDefault);
        setColumnType(domainTypeName);
        return getFormattedDataType();
    }

    public boolean isEditSize() {
        return getColumnType() != null && (getSQLType() == Types.NUMERIC || getSQLType() == Types.CHAR || getSQLType() == Types.VARCHAR
                || getSQLType() == Types.DECIMAL || getSQLType() == Types.BLOB || getSQLType() == Types.LONGVARCHAR
                || getSQLType() == Types.LONGVARBINARY
                || getColumnType().equalsIgnoreCase("VARCHAR")
                || getColumnType().equalsIgnoreCase("CHAR"))
                || getColumnType().equalsIgnoreCase(T.DECFLOAT);
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

    public boolean isAutoincrement() {
        return ai.isAutoincrement();
    }

    public Autoincrement getAutoincrement() {
        ai.setFieldName(getColumnName());
        return ai;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getCharset() {
        return charset;
    }

    public String getDomainCharset() {
        return domainCharset;
    }

    public String getDomainCheck() {
        return domainCheck;
    }

    public String getDomainDescription() {
        return domainDescription;
    }

    public boolean isDomainNotNull() {
        return domainNotNull;
    }

    public String getDomainDefault() {
        return domainDefault;
    }

    public void makeCopy() {
        if (copy == null) {
            copy = new ColumnData(dc);
            copy.setValues(this);
        }
    }

    public boolean hasCopy() {
        return copy != null;
    }

    public boolean isNameChanged() {
        return hasCopy() && !getColumnName().equals(copy.getColumnName());
    }

    public boolean isDefaultChanged() {
        if (!hasCopy()) {
            return false;
        }
        if (MiscUtils.isNull(copy.getDefaultValue().getValue())) {
            return !MiscUtils.isNull(getDefaultValue().getValue());
        } else {
            if (MiscUtils.isNull(getDefaultValue().getValue()))
                return true;
        }
        return !copy.getDefaultValue().getValue().equalsIgnoreCase(getDefaultValue().getValue());
    }

    public boolean isCheckChanged() {
        if (!hasCopy()) {
            return false;
        }
        if (MiscUtils.isNull(copy.getCheck())) {
            return !MiscUtils.isNull(getCheck());
        } else {
            if (MiscUtils.isNull(getCheck()))
                return true;
        }
        return !copy.getCheck().equalsIgnoreCase(getCheck());
    }

    public boolean isTypeChanged() {
        return hasCopy() && (!getColumnType().equals(copy.getColumnType()) || getColumnSize() != copy.getColumnSize() || getColumnScale() != copy.getColumnScale() || getCharset() != copy.getCharset());
    }

    public boolean isDescriptionChanged() {
        if (!hasCopy()) {
            return false;
        }
        if (MiscUtils.isNull(copy.getDescription())) {
            return !MiscUtils.isNull(getDescription());
        } else {
            if (MiscUtils.isNull(getDescription()))
                return true;
        }
        return !copy.getDescription().equalsIgnoreCase(getDescription());
    }

    public boolean isRequiredChanged() {
        return hasCopy() && (isRequired() != copy.isRequired());
    }

    public boolean isChanged() {
        return hasCopy() && (isCheckChanged() || isDefaultChanged() || isNameChanged() || isDescriptionChanged() || isTypeChanged());
    }

    public void setTypeParameter(int typeParameter) {
        this.typeParameter = typeParameter;
    }

    public int getTypeParameter() {
        return typeParameter;
    }

    public String getDomainComputedBy() {
        return domainComputedBy;
    }

    public boolean isTypeOf() {
        return typeOf;
    }

    public void setTypeOf(boolean typeOf) {
        this.typeOf = typeOf;
    }

    public DefaultDatabaseObject getTable() {
        return table;
    }


    public void setDefaultValue(String defaultValue, boolean needProcessing) {
        if (needProcessing) {
            defaultValue = processedDefaultValue(defaultValue);
        }
        setDefaultValue(defaultValue);
    }

    public String processedDefaultValue(String defaultValue) {
        if (!MiscUtils.isNull(defaultValue)) {
            defaultValue = defaultValue.trim();
            if (defaultValue.toUpperCase().startsWith("DEFAULT")) {
                defaultValue = defaultValue.substring(7).trim();
                this.defaultValue.setOriginOperator("DEFAULT");
            }
            if (defaultValue.toUpperCase().startsWith("=")) {
                defaultValue = defaultValue.substring(1).trim();
                this.defaultValue.setOriginOperator("=");
            }
            if (defaultValue.startsWith("'") && defaultValue.endsWith("'") && defaultValue.length() > 2) {
                defaultValue = defaultValue.substring(1, defaultValue.length() - 1);
                this.defaultValue.setUseQuotes(true);
            }
        }
        return defaultValue;
    }

    public void setTable(String table) {
        if (table == null)
            return;
        for (NamedObject namedObject : getTables()) {
            if (namedObject.getName().contentEquals(table)) {
                this.table = (DefaultDatabaseObject) namedObject;
                break;
            }
        }
        columns.clear();
        for (DatabaseColumn column : this.table.getColumns()) {
            columns.add(column.getName());
        }
        if (!columns.isEmpty())
            setColumnTable(columns.get(0));

    }

    public String getFormattedTable() {
        return getFormattedObject(table.getName());
    }

    public void setTable(int tableIndex) {
        setTable(getTables().get(tableIndex).getName());
    }

    public String getColumnTable() {
        return columnTable;
    }

    public String getFormattedColumnTable() {
        return getFormattedObject(columnTable);
    }

    public void setColumnTable(String columnTable) {
        this.columnTable = columnTable;
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<NamedObject> getTables() {
        if (tables.isEmpty()) {
            tables.addAll(ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(dc).getTables());
        }
        return tables;
    }

    public List<String> getTableNames() {
        ArrayList<String> list = new ArrayList<>();
        for (NamedObject table : getTables())
            list.add(table.getName());
        return list;
    }

    public int getTypeOfFrom() {
        return typeOfFrom;
    }

    public void setTypeOfFrom(int typeOfFrom) {
        this.typeOfFrom = typeOfFrom;
    }

    public String getMechanism() {
        return mechanism;
    }

    public void setMechanism(String mechanism) {
        this.mechanism = mechanism;
    }

    public boolean isCstring() {
        return cstring;
    }

    public void setCstring(boolean cstring) {
        this.cstring = cstring;
    }

    public String getFormattedDomain() {
        return getFormattedObject(domain);
    }

    public String getFormattedColumnName() {
        return getFormattedObject(columnName);
    }

    String getFormattedObject(String obj) {
        return MiscUtils.getFormattedObject(obj);
    }

    public boolean isDescriptionAsSingleComment() {
        return descriptionAsSingleComment;
    }

    public void setDescriptionAsSingleComment(boolean descriptionAsSingleComment) {
        this.descriptionAsSingleComment = descriptionAsSingleComment;
    }

    public String getCollate() {
        return collate;
    }

    public void setCollate(String collate) {
        this.collate = collate;
    }

    public String getDomainTypeName() {
        return domainTypeName;
    }

    public void setDomainTypeName(String domainTypeName) {
        this.domainTypeName = domainTypeName;
    }

    public boolean isCursor() {
        return cursor;
    }

    public void setCursor(boolean cursor) {
        this.cursor = cursor;
    }

    public boolean isScroll() {
        return scroll;
    }

    public void setScroll(boolean scroll) {
        this.scroll = scroll;
    }

    public String getSelectOperator() {
        return selectOperator;
    }

    public void setSelectOperator(String selectOperator) {
        this.selectOperator = selectOperator;
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

    public List<Dimension> getDomainDimensions() {
        return domainDimensions;
    }

    public void setDomainDimensions(List<Dimension> domainDimensions) {
        this.domainDimensions = domainDimensions;
    }

    public class DefaultValue implements Serializable {
        String originOperator;
        String value;
        boolean useQuotes = false;

        public String getOriginOperator() {
            return originOperator;
        }

        public void setOriginOperator(String originOperator) {
            this.originOperator = originOperator;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isUseQuotes() {
            return useQuotes;
        }

        public void setUseQuotes(boolean useQuotes) {
            this.useQuotes = useQuotes;
        }
    }

    public static class Dimension {
        int lowerBound;
        int upperBound;

        public Dimension(int lowerBound, int upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        public int getLowerBound() {
            return lowerBound;
        }

        public void setLowerBound(int lowerBound) {
            this.lowerBound = lowerBound;
        }

        public int getUpperBound() {
            return upperBound;
        }

        public void setUpperBound(int upperBound) {
            this.upperBound = upperBound;
        }
    }
}







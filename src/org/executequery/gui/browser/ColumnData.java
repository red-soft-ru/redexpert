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
import org.executequery.databaseobjects.Types;
import org.executequery.databaseobjects.impl.DefaultDatabaseDomain;
import org.executequery.databaseobjects.impl.DefaultDatabaseObject;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.gui.table.Autoincrement;
import org.executequery.log.Log;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

/**
 * This class represents a single table
 * column definition. This includes data types
 * sizes, scales and key referencing metadata.
 *
 * @author Takis Diakoumis
 */
public class ColumnData implements Serializable {
    private static final long serialVersionUID = -4937385038396757064L;

    public static final int TYPE_OF_FROM_DOMAIN = 0;
    public static final int TYPE_OF_FROM_COLUMN = 1;
    public static final int INPUT_PARAMETER = 0;
    public static final int OUTPUT_PARAMETER = 1;
    public static final int VARIABLE = 2;

    private int size;
    private int scale;
    private int subtype;
    private int sqlType;
    private int typeOfFrom;
    private int typeParameter;
    private int columnPosition;

    private boolean typeOf;
    private boolean cursor;
    private boolean scroll;
    private boolean cString;
    private boolean notNull;
    private boolean newColumn;
    private boolean primaryKey;
    private boolean foreignKey;
    private boolean markedDeleted;
    private boolean remarkAsSingleComment;

    private String check;
    private String schema;
    private String domain;
    private String catalog;
    private String charset;
    private String remarks;
    private String collate;
    private String keyType;
    private String typeName;
    private String tableName;
    private String mechanism;
    private String computedBy;
    private String columnName;
    private String columnTable;
    private String selectOperator;

    private ColumnData copy;
    private DefaultValue defaultValue;
    private DefaultDatabaseObject table;
    private DatabaseConnection connection;

    private List<Dimension> dimensions;
    private Vector<ColumnConstraint> columnConstraints;

    private final Autoincrement autoincrement;
    private final List<String> columns;
    private final List<NamedObject> tables;
    private final DefaultStatementExecutor executor;

    public ColumnData(DatabaseConnection connection, DatabaseColumn databaseColumn) {
        this(connection);
        setValues(databaseColumn, true);
    }

    public ColumnData(DatabaseConnection connection, DatabaseColumn databaseColumn, boolean loadDomainInfo) {
        this(connection);
        setValues(databaseColumn, loadDomainInfo);
    }

    public ColumnData(String columnName, DatabaseConnection connection) {
        this(connection);
        setColumnName(columnName);
    }

    public ColumnData(boolean newColumn, DatabaseConnection connection) {
        this(connection);
        setNewColumn(newColumn);
    }

    public ColumnData(DatabaseConnection connection) {
        this.connection = connection;

        tables = new ArrayList<>();
        columns = new ArrayList<>();
        defaultValue = new DefaultValue();
        autoincrement = new Autoincrement();
        executor = new DefaultStatementExecutor(this.connection, true);

        setCharset("");
        setKeyType(null);
        setNewColumn(false);
        setPrimaryKey(false);
        setForeignKey(false);
        setRemarkAsSingleComment(false);
    }

    @SuppressWarnings("unchecked")
    public void setValues(ColumnData cd) {

        size = cd.getSize();
        check = cd.getCheck();
        scale = cd.getScale();
        table = cd.getTable();
        typeOf = cd.isTypeOf();
        domain = cd.getDomain();
        notNull = cd.isNotNull();
        keyType = cd.getKeyType();
        sqlType = cd.getSQLType();
        remarks = cd.getRemarks();
        typeName = cd.getTypeName();
        tableName = cd.getTableName();
        primaryKey = cd.isPrimaryKey();
        foreignKey = cd.isForeignKey();
        dimensions = cd.getDimensions();
        columnName = cd.getColumnName();
        computedBy = cd.getComputedBy();
        connection = cd.getConnection();
        columnTable = cd.getColumnTable();
        defaultValue = new DefaultValue(cd.getDefaultValue());

        Vector<ColumnConstraint> constraints = cd.getColumnConstraintsVector();
        if (constraints != null)
            columnConstraints = (Vector<ColumnConstraint>) constraints.clone();
    }

    public void setValues(DatabaseColumn cd, boolean loadDomainInfo) {

        setColumnName(cd.getName());
        setSize(cd.getColumnSize());
        setNotNull(cd.isRequired());
        setSQLType(cd.getTypeInt());
        setScale(cd.getColumnScale());
        setTypeName(cd.getTypeName());
        setPrimaryKey(cd.isPrimaryKey());
        setForeignKey(cd.isForeignKey());
        setDimensions(cd.getDimensions());
        setTableName(cd.getParentsName());
        setColumnPosition(cd.getPosition());
        setRemarks(cd.getColumnDescription());
        setComputedBy(cd.getComputedSource());
        setDefaultValue(cd.getDefaultValue());
        setDomain(cd.getDomain(), loadDomainInfo);
        autoincrement.setIdentity(cd.isIdentity());
    }

    private void getDomainInfo() {
        setDomain(MiscUtils.trimEnd(domain), false);

        DefaultDatabaseDomain databaseDomain = (DefaultDatabaseDomain) ConnectionsTreePanel.getNamedObjectFromHost(connection, NamedObject.DOMAIN, domain);
        if (databaseDomain == null)
            databaseDomain = (DefaultDatabaseDomain) ConnectionsTreePanel.getNamedObjectFromHost(connection, NamedObject.SYSTEM_DOMAIN, domain);

        if (databaseDomain == null) {

            DatabaseObjectNode hostNode = ConnectionsTreePanel.getPanelFromBrowser().getHostNode(connection);
            for (DatabaseObjectNode metaTagNode : hostNode.getChildObjects()) {
                boolean isDomain = Objects.equals(metaTagNode.getMetaDataKey(), NamedObject.META_TYPES[NamedObject.DOMAIN]);
                boolean isSystemDomain = Objects.equals(metaTagNode.getMetaDataKey(), NamedObject.META_TYPES[NamedObject.SYSTEM_DOMAIN]);

                if (isDomain || isSystemDomain)
                    ConnectionsTreePanel.getPanelFromBrowser().reloadPath(metaTagNode.getTreePath());
            }

            if (!SystemProperties.getBooleanProperty("user", "browser.show.system.objects"))
                ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(connection).reloadMetaTag(NamedObject.SYSTEM_DOMAIN);

            databaseDomain = (DefaultDatabaseDomain) ConnectionsTreePanel.getNamedObjectFromHost(connection, NamedObject.DOMAIN, domain);
            if (databaseDomain == null)
                databaseDomain = (DefaultDatabaseDomain) ConnectionsTreePanel.getNamedObjectFromHost(connection, NamedObject.SYSTEM_DOMAIN, domain);
        }

        if (databaseDomain != null) {
            ColumnData domainData = databaseDomain.getDomainData();

            setSize(domainData.size);
            setScale(domainData.scale);
            setCheck(domainData.check);
            setSQLType(domainData.sqlType);
            setRemarks(domainData.remarks);
            setSubtype(domainData.subtype);
            setCharset(domainData.charset);
            setNotNull(domainData.notNull);
            setCollate(domainData.collate);
            setComputedBy(domainData.computedBy);
            setDimensions(domainData.dimensions);
            setDefaultValue(domainData.defaultValue.value);

        } else
            Log.error("Error get Domain '" + domain + "'");
    }

    public String getFormattedDataType() {
        return getFormattedDataType(false);
    }

    public String getFormattedDataType(boolean isUdfParameter) {

        if (typeOf) {
            return (getTypeOfFrom() == TYPE_OF_FROM_DOMAIN) ?
                    "TYPE OF " + getFormattedDomain() :
                    "TYPE OF COLUMN " + getFormattedTable() + "." + getFormattedColumnTable();
        }

        String typeString = getTypeName();
        if (StringUtils.isBlank(typeString))
            return "";

        if (typeString.contains("<0")) {
            typeString = (subtype < 0) ?
                    typeString.replace("<0", String.valueOf(subtype)) :
                    typeString.replace("<0", "0");
        }

        int type = getSQLType();
        StringBuilder sb = new StringBuilder(typeString);
        if (!typeString.matches("\\b\\D+\\d+\\b") || (type == Types.CHAR
                || type == Types.VARCHAR
                || type == Types.BLOB
                || type == Types.LONGVARCHAR
                || type == Types.LONGVARBINARY
                || isCString())
        ) {

            if (type == Types.BLOB || type == Types.LONGVARCHAR || type == Types.LONGVARBINARY) {
                if (getSize() != 80 && !isUdfParameter)
                    sb.append(" SEGMENT SIZE ").append(getSize());

            } else if (isEditSize()
                    && getSize() > 0
                    && !isDate()
                    && !isBit()
                    && (!getTypeName().equalsIgnoreCase(T.DECFLOAT) || getTypeName().equalsIgnoreCase(T.DECFLOAT)
                    && (getSize() == 16 || getSize() == 34))
            ) {
                sb.append("(").append(getSize());
                if (getScale() > 0)
                    sb.append(",").append(getScale());
                sb.append(")");
            }

            if (!MiscUtils.isNull(getCharset()) && connection != null && !getCharset().equalsIgnoreCase(connection.getDBCharset()))
                sb.append(" CHARACTER SET ").append(getCharset());

            if (dimensions != null) {
                sb.append("[");

                boolean first = true;
                for (Dimension dimension : dimensions) {

                    if (!first)
                        sb.append(",");
                    first = false;

                    if (dimension.lowerBound != 1)
                        sb.append(dimension.lowerBound).append(":");
                    sb.append(dimension.upperBound);
                }
                sb.append("]");
            }
        }

        return sb.toString().trim();
    }

    public String getFormattedTable() {
        return getFormattedObject(table.getName());
    }

    public String getFormattedColumnTable() {
        return getFormattedObject(columnTable);
    }

    public String getFormattedDomain() {
        return getFormattedObject(domain);
    }

    public String getFormattedColumnName() {
        return getFormattedObject(columnName);
    }

    public Vector<ColumnConstraint> getColumnConstraintsVector() {
        return columnConstraints;
    }

    public ColumnConstraint[] getColumnConstraintsArray() {
        return getColumnConstraintsVector().toArray(new ColumnConstraint[0]);
    }

    public void addConstraint(ColumnConstraint cc) {
        if (columnConstraints == null)
            columnConstraints = new Vector<>();

        columnConstraints.add(cc);
    }

    public void removeConstraint(ColumnConstraint cc) {
        columnConstraints.remove(cc);
    }

    public void resetConstraints() {
        if (columnConstraints != null)
            columnConstraints.clear();
    }

    public void makeCopy() {
        if (copy == null) {
            copy = new ColumnData(connection);
            copy.setValues(this);
        }
    }

    public void setNamesToUpper() {
        if (tableName != null)
            tableName = tableName.toUpperCase();

        if (columnName != null)
            columnName = columnName.toUpperCase();
    }

    public String processedDefaultValue(String defaultValue, boolean isDomain) {

        if (!MiscUtils.isNull(defaultValue)) {
            defaultValue = defaultValue.trim();

            if (defaultValue.toUpperCase().startsWith("DEFAULT")) {
                defaultValue = defaultValue.substring(7).trim();
                this.defaultValue.setOperator("DEFAULT");
            }

            if (defaultValue.toUpperCase().startsWith("=")) {
                defaultValue = defaultValue.substring(1).trim();
                this.defaultValue.setOperator("=");
            }

            if (defaultValue.startsWith("'") && defaultValue.endsWith("'") && defaultValue.length() > 2) {
                defaultValue = defaultValue.substring(1, defaultValue.length() - 1);
                this.defaultValue.setUseQuotes(true);
            }
        }
        this.defaultValue.isDomain = isDomain;

        return defaultValue;
    }

    private String getFormattedObject(String obj) {
        return MiscUtils.getFormattedObject(obj, connection);
    }

    @Override
    public String toString() {
        return columnName == null ? ColumnConstraint.EMPTY : columnName;
    }

    // --- change check methods ---

    public boolean isHasCopy() {
        return copy != null;
    }

    public boolean isEditSize() {
        return getTypeName() != null && (getSQLType() == Types.NUMERIC
                || getSQLType() == Types.INT128
                || getSQLType() == Types.CHAR
                || getSQLType() == Types.VARCHAR
                || getSQLType() == Types.DECIMAL
                || getSQLType() == Types.BLOB
                || getSQLType() == Types.LONGVARCHAR
                || getSQLType() == Types.LONGVARBINARY
                || getTypeName().equalsIgnoreCase("CSTRING")
                || getTypeName().equalsIgnoreCase("VARCHAR")
                || getTypeName().equalsIgnoreCase("CHAR")
        ) || getTypeName().equalsIgnoreCase(T.DECFLOAT);
    }

    public boolean isValid() {
        return !MiscUtils.isNull(columnName)
                && !MiscUtils.isNull(tableName)
                && !MiscUtils.isNull(typeName);
    }

    public boolean isChanged() {
        return isHasCopy() && (isTypeChanged()
                || isNameChanged()
                || isCheckChanged()
                || isDefaultChanged()
                || isDescriptionChanged()
                || isNotNullChanged()
        );
    }

    public boolean isTypeChanged() {
        return isHasCopy() && (!getTypeName().equals(copy.getTypeName())
                || getSize() != copy.getSize()
                || getScale() != copy.getScale()
                || !Objects.equals(getCharset(), copy.getCharset())
        );
    }

    public boolean isNameChanged() {
        return isHasCopy() && !getColumnName().equals(copy.getColumnName());
    }

    public boolean isCheckChanged() {

        if (!isHasCopy())
            return false;

        if (MiscUtils.isNull(copy.getCheck()))
            return !MiscUtils.isNull(getCheck());

        if (MiscUtils.isNull(getCheck()))
            return true;

        return !copy.getCheck().equalsIgnoreCase(getCheck());
    }

    public boolean isDefaultChanged() {

        if (!isHasCopy())
            return false;

        if (MiscUtils.isNull(copy.getDefaultValue().getValue()))
            return !MiscUtils.isNull(getDefaultValue().getValue());

        if (MiscUtils.isNull(getDefaultValue().getValue()))
            return true;

        return !copy.getDefaultValue().getValue().equalsIgnoreCase(getDefaultValue().getValue());
    }

    public boolean isDescriptionChanged() {

        if (!isHasCopy())
            return false;

        if (MiscUtils.isNull(copy.getRemarks()))
            return !MiscUtils.isNull(getRemarks());

        if (MiscUtils.isNull(getRemarks()))
            return true;

        return !copy.getRemarks().equalsIgnoreCase(getRemarks());
    }

    public boolean isNotNullChanged() {
        return isHasCopy() && (isNotNull() != copy.isNotNull());
    }

    // --- get/set methods ---

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public boolean isForeignKey() {
        return foreignKey;
    }

    public void setForeignKey(boolean foreignKey) {
        this.foreignKey = foreignKey;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public boolean isNewColumn() {
        return newColumn;
    }

    public void setNewColumn(boolean newColumn) {
        this.newColumn = newColumn;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public int getSubtype() {
        return subtype;
    }

    public void setSubtype(int subtype) {
        this.subtype = subtype;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getSQLType() {
        return sqlType;
    }

    public void setSQLType(int sqlType) {
        this.sqlType = sqlType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
        autoincrement.setFieldName(columnName);
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
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

    public DatabaseConnection getConnection() {
        return connection;
    }

    public void setConnection(DatabaseConnection connection) {
        this.connection = connection;
        executor.setDatabaseConnection(this.connection);
    }

    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getComputedBy() {
        return computedBy;
    }

    public void setComputedBy(String computedBy) {

        if (!MiscUtils.isNull(computedBy) && computedBy.startsWith("(") && computedBy.endsWith(")"))
            computedBy = computedBy.substring(1, computedBy.length() - 1);

        this.computedBy = computedBy;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public int getTypeParameter() {
        return typeParameter;
    }

    public void setTypeParameter(int typeParameter) {
        this.typeParameter = typeParameter;
    }

    public boolean isTypeOf() {
        return typeOf;
    }

    public void setTypeOf(boolean typeOf) {
        this.typeOf = typeOf;
    }

    public String getColumnTable() {
        return columnTable;
    }

    public void setColumnTable(String columnTable) {
        this.columnTable = columnTable;
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

    public boolean isCString() {
        return cString;
    }

    public void setCString(boolean cString) {
        this.cString = cString;
    }

    public boolean isRemarkAsSingleComment() {
        return remarkAsSingleComment;
    }

    public void setRemarkAsSingleComment(boolean remarkAsSingleComment) {
        this.remarkAsSingleComment = remarkAsSingleComment;
    }

    public String getCollate() {
        return collate;
    }

    public void setCollate(String collate) {
        this.collate = collate;
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

    public int getColumnPosition() {
        return columnPosition;
    }

    public void setColumnPosition(int columnPosition) {
        this.columnPosition = columnPosition;
    }

    public void setDefaultValue(DefaultValue defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue.value = defaultValue;
    }

    public void setDefaultValue(String defaultValue, boolean needProcessing, boolean isDomain) {
        setDefaultValue(needProcessing ? processedDefaultValue(defaultValue, isDomain) : defaultValue);
    }

    public String getDomain() {
        return domain != null ? MiscUtils.trimEnd(domain) : null;
    }

    public void setDomain(String domain) {
        setDomain(domain, true);
    }

    public void setDomain(String domain, boolean loadDomainInfo) {
        this.domain = domain;
        if (!MiscUtils.isNull(domain) && loadDomainInfo)
            getDomainInfo();
    }

    public DefaultDatabaseObject getTable() {
        return table;
    }

    public void setTable(int tableIndex) {
        setTable(getTables().get(tableIndex).getName());
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
        this.table.getColumns().forEach(column -> columns.add(column.getName()));

        if (!columns.isEmpty())
            setColumnTable(columns.get(0));
    }

    public List<Dimension> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<Dimension> dimensions) {
        this.dimensions = dimensions;
    }

    public void appendDimension(int orderNumber, int lowerBound, int upperBound) {

        if (dimensions == null)
            dimensions = new ArrayList<>();

        Dimension dimension = new Dimension(lowerBound, upperBound);
        if (orderNumber >= dimensions.size())
            dimensions.add(dimension);
        else
            dimensions.add(orderNumber, dimension);
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<NamedObject> getTables() {

        if (tables.isEmpty()) {
            tables.addAll(ConnectionsTreePanel
                    .getPanelFromBrowser()
                    .getDefaultDatabaseHostFromConnection(connection)
                    .getTables()
            );
        }

        return tables;
    }

    public List<String> getTableNames() {

        ArrayList<String> list = new ArrayList<>();
        getTables().forEach(table -> list.add(table.getName()));

        return list;
    }

    public boolean isAutoincrement() {
        return autoincrement.isAutoincrement();
    }

    public Autoincrement getAutoincrement() {
        autoincrement.setFieldName(getColumnName());
        return autoincrement;
    }

    // ---

    public boolean isKey() {
        return isPrimaryKey() || isForeignKey();
    }

    public boolean isBit() {
        return sqlType == Types.BIT;
    }

    public boolean isChar() {
        return sqlType == Types.CHAR
                || sqlType == Types.VARCHAR
                || sqlType == Types.LONGVARCHAR;
    }

    public boolean isLOB() {
        return sqlType == Types.BINARY
                || sqlType == Types.BLOB
                || sqlType == Types.CLOB
                || sqlType == Types.LONGNVARCHAR
                || sqlType == Types.LONGVARBINARY
                || sqlType == Types.LONGVARCHAR
                || sqlType == Types.NCLOB;
    }

    public boolean isDate() {
        return sqlType == Types.DATE
                || sqlType == Types.TIME
                || sqlType == Types.TIMESTAMP
                || sqlType == Types.TIME_WITH_TIMEZONE
                || sqlType == Types.TIMESTAMP_WITH_TIMEZONE;
    }

    // ---

    public static class DefaultValue implements Serializable {

        protected String value;
        protected String operator;
        protected boolean isDomain;
        protected boolean useQuotes;

        public DefaultValue() {
            this.isDomain = false;
            this.useQuotes = false;
        }

        public DefaultValue(DefaultValue defaultValue) {
            this.value = defaultValue.value;
            this.operator = defaultValue.operator;
            this.isDomain = defaultValue.isDomain;
            this.useQuotes = defaultValue.useQuotes;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public boolean isDomain() {
            return isDomain;
        }

        public void setDomain(boolean domain) {
            isDomain = domain;
        }

        public boolean isUseQuotes() {
            return useQuotes;
        }

        public void setUseQuotes(boolean useQuotes) {
            this.useQuotes = useQuotes;
        }

    } // DefaultValue class

    public static class Dimension {

        protected int lowerBound;
        protected int upperBound;

        public Dimension(int lowerBound, int upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

    } // Dimension class

}

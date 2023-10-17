/*
 * DefaultDatabaseTable.java
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

import org.apache.commons.lang.StringUtils;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.*;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.comparer.Comparer;
import org.executequery.gui.browser.tree.TreePanel;
import org.executequery.sql.TokenizingFormatter;
import org.executequery.sql.sqlbuilder.*;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.Named;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import java.sql.*;
import java.util.*;

/**
 * @author Takis Diakoumis
 */
public class DefaultDatabaseTable extends AbstractTableObject implements DatabaseTable {
    protected static final String EXTERNAL_FILE = "EXTERNAL_FILE";
    protected static final String ADAPTER = "ADAPTER";
    protected static final String TABLESPACE = "TABLESPACE";
    protected static final String CONSTRAINT_NAME = "CONSTRAINT_NAME";
    protected static final String CONSTRAINT_TYPE = "CONSTRAINT_TYPE";
    protected static final String TRIGGER_SOURCE = "TRIGGER_SOURCE";

    static final long serialVersionUID = -963831243178078154L;

    List<ColumnConstraint> constraints;
    TokenizingFormatter formatter;

    /** the table columns exported */
    private List<DatabaseColumn> exportedColumns;

    /** the table indexed columns */
    private List<DefaultDatabaseIndex> indexes;

    /** the user modified SQL text for changes */
    private String modifiedSQLText;
    private transient TableDataChangeWorker tableDataChangeExecutor;

    private String externalFile;
    private String tablespace;
    private List<DefaultDatabaseTrigger> triggers;
    private String adapter;

    protected List<ColumnData> listCD;
    protected List<org.executequery.gui.browser.ColumnConstraint> listCC;
    protected List<ColumnConstraint> checkConstraints;

    /**
     * Creates a new instance of DatabaseTable
     */
    public DefaultDatabaseTable(DatabaseHost host) {
        super(host, "TABLE");
    }

    /**
     * Creates a new instance of DatabaseTable
     */
    public DefaultDatabaseTable(DatabaseHost host, String metaDataKey) {
        super(host, metaDataKey);
    }

    /**
     * Creates a new instance of DatabaseTable
     */
    public DefaultDatabaseTable(DatabaseObject object, String metaDataKey) {

        this(object.getHost(), metaDataKey);
        setName(object.getName());


        if (object instanceof DefaultDatabaseObject) {
            DefaultDatabaseObject ddo = ((DefaultDatabaseObject) object);
            setTypeTree(ddo.getTypeTree());
            setDependObject(ddo.getDependObject());
            metaTagParent = ddo.getMetaTagParent();

        } else {
            typeTree = TreePanel.DEFAULT;
            setDependObject(null);
        }
    }

    /**
     * Creates a new instance of DatabaseTable
     */
    public DefaultDatabaseTable(DatabaseObject object) {

        this(object.getHost());
        setName(object.getName());


        if (object instanceof DefaultDatabaseObject) {
            DefaultDatabaseObject ddo = ((DefaultDatabaseObject) object);
            setTypeTree(ddo.getTypeTree());
            setDependObject(ddo.getDependObject());
            metaTagParent = ddo.getMetaTagParent();

        } else {
            typeTree = TreePanel.DEFAULT;
            setDependObject(null);
        }
    }

    @Override
    public boolean allowsChildren() {
        return true;
    }

    public List<String> getColumnNames() {

        List<String> names = new ArrayList<>();
        for (DatabaseColumn column : getColumns())
            names.add(column.getName());

        return names;
    }

    @Override
    public List<DatabaseColumn> getExportedKeys() throws DataSourceException {

        if (!isMarkedForReload() && exportedColumns != null)
            return exportedColumns;

        if (exportedColumns != null) {
            exportedColumns.clear();
            exportedColumns = null;
        }

        DatabaseHost host = getHost();
        if (host != null)
            exportedColumns = host.getExportedKeys(getCatalogName(), getSchemaName(), getName());

        return exportedColumns;
    }

    @Override
    public boolean hasReferenceTo(DatabaseTable anotherTable) {

        List<ColumnConstraint> constraints = getConstraints();
        String anotherTableName = anotherTable.getName();

        for (ColumnConstraint constraint : constraints)
            if (constraint.isForeignKey())
                if (constraint.getReferencedTable().equals(anotherTableName))
                    return true;

        return false;
    }

    /**
     * Returns the column count of this table.
     *
     * @return the column count
     */
    @Override
    public int getColumnCount() throws DataSourceException {
        return getColumns().size();
    }

    private List<ColumnConstraint> databaseConstraintsListWithSize(int size) {
        return Collections.synchronizedList(new ArrayList<>(size));
    }

    private List<DefaultDatabaseIndex> databaseIndexListWithSize(int size) {
        return Collections.synchronizedList(new ArrayList<>(size));
    }

    /**
     * Returns the constraints of this table.
     *
     * @return the column constraints
     */
    @Override
    public synchronized List<ColumnConstraint> getConstraints() throws DataSourceException {

        if (constraints == null) {
            if (getColumns() != null) {

                constraints = new ArrayList<>();

                for (DatabaseColumn i : columns) {

                    DatabaseTableColumn column = (DatabaseTableColumn) i;
                    if (column.hasConstraints()) {

                        List<ColumnConstraint> columnConstraints = column.getConstraints();
                        for (ColumnConstraint constraint : columnConstraints) {

                            String name = constraint.getName();
                            if (isContainsTheSameObjectByName(name)) {

                                getConstraintByName(name).addColumnToDisplayList(constraint.getColumn());
                                if (Objects.equals(constraint.getTypeName(), "FOREIGN"))
                                    getConstraintByName(name).addReferenceColumnToDisplayList(constraint.getColumn());

                            } else
                                constraints.add(constraint);
                        }
                    }
                }

                if (checkConstraints == null)
                    getObjectInfo();

                constraints.addAll(checkConstraints);
                constraints.removeAll(Collections.singleton(null));
                constraints.sort(Comparator.comparing(Named::getName));

                return constraints;

            } else
                return databaseConstraintsListWithSize(0);
        } else
            return constraints;
    }

    private boolean isContainsTheSameObjectByName(String name) {

        if (constraints != null)
            for (ColumnConstraint element : constraints)
                if (Objects.equals(element.getName(), name))
                    return true;

        return false;
    }

    private ColumnConstraint getConstraintByName(String name) {

        ColumnConstraint constraint = null;
        if (constraints != null)
            for (ColumnConstraint element : constraints)
                if (Objects.equals(element.getName(), name))
                    constraint = element;

        return constraint;
    }

    /**
     * Returns the indexes of this table.
     *
     * @return the indexes
     */
    @Override
    public List<DefaultDatabaseIndex> getIndexes() throws DataSourceException {

        if (!isMarkedForReload() && indexes != null)
            return indexes;

        ResultSet rs = null;
        try {

            DatabaseHost _host = getHost();
            rs = _host.getDatabaseMetaData().getIndexInfo(getCatalogName(), getSchemaName(), getName(), false, true);
            TableColumnIndex lastIndex = null;
            indexes = new ArrayList<>();
            List<TableColumnIndex> tindexes = new ArrayList<>();

            while (rs.next()) {
                String name = rs.getString(6);
                if (StringUtils.isBlank(name))
                    continue;

                if (lastIndex == null || !lastIndex.getName().equals(name)) {
                    TableColumnIndex index = new TableColumnIndex(name);
                    index.setNonUnique(rs.getBoolean(4));
                    index.addIndexedColumn(rs.getString(9));
                    index.setMetaData(resultSetRowToMap(rs));
                    lastIndex = index;
                    tindexes.add(index);

                } else
                    lastIndex.addIndexedColumn(rs.getString(9));
            }

            releaseResources(rs, null);
            DefaultDatabaseMetaTag metaTag =
                    new DefaultDatabaseMetaTag(getHost(), null, null, META_TYPES[INDEX]);

            for (TableColumnIndex index : tindexes) {
                DefaultDatabaseIndex index1 = metaTag.getIndexFromName(index.getName());
                index1.getObjectInfo();
                indexes.add(index1);
                if (index1.getExpression() != null) {
                    index.setIndexedColumns(null);
                    index.setExpression(index1.getExpression());
                }
                index.setConstraint_type(index1.getConstraint_type());
            }

            return indexes;

        } catch (DataSourceException e) {

            // catch and re-throw here to create
            // an empty index list, so we don't
            // keep hitting the same error
            indexes = databaseIndexListWithSize(0);
            throw e;

        } catch (SQLException e) {

            // catch and re-throw here to create
            // an empty index list, so we don't
            // keep hitting the same error
            indexes = databaseIndexListWithSize(0);
            throw new DataSourceException(e);

        } finally {
            releaseResources(rs, null);
            setMarkedForReload(false);
        }
    }

    @Override
    public List<DefaultDatabaseTrigger> getTriggers() throws DataSourceException {

        if (!isMarkedForReload() && triggers != null)
            return triggers;

        triggers = new ArrayList<>();
        ResultSet rs;
        DefaultStatementExecutor querySender = new DefaultStatementExecutor(getHost().getDatabaseConnection());
        String query = "select T.RDB$TRIGGER_NAME,\n" +
                "T.RDB$RELATION_NAME\n" +
                "from RDB$TRIGGERS T\n" +
                "left join RDB$CHECK_CONSTRAINTS C ON C.RDB$TRIGGER_NAME = T.RDB$TRIGGER_NAME\n" +
                "where ((T.RDB$SYSTEM_FLAG = 0) or (T.RDB$SYSTEM_FLAG is null))\n" +
                "and (C.RDB$TRIGGER_NAME is NULL)\n" +
                "and (T.RDB$RELATION_NAME = ?)\n" +
                "order by  T.RDB$TRIGGER_SEQUENCE, T.RDB$TRIGGER_NAME";

        try {

            PreparedStatement st = querySender.getPreparedStatement(query);
            st.setString(1, getName());
            rs = querySender.getResultSet(-1, st).getResultSet();

            while (rs.next()) {
                String trigName = rs.getString(1);
                if (trigName != null) {
                    trigName = trigName.trim();
                    triggers.add((DefaultDatabaseTrigger) ((DefaultDatabaseHost) getHost()).getDatabaseObjectFromTypeAndName(NamedObject.TRIGGER, trigName));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            querySender.releaseResources();
        }

        return triggers;
    }

    /**
     * Returns this table's column meta data result set.
     *
     * @return the column meta data result set
     */
    @Override
    public ResultSet getColumnMetaData() throws DataSourceException {
        return getMetaData();
    }

    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    @Override
    public int getType() {
        return isSystem() ? SYSTEM_TABLE : TABLE;
    }

    /**
     * Returns the metadata key name of this object.
     *
     * @return the metadata key name.
     */
    @Override
    public String getMetaDataKey() {
        return META_TYPES[getType()];
    }

    /**
     * Override to clear the columns.
     */
    @Override
    public void reset() {
        super.reset();
        modifiedSQLText = null;
        clearColumns();
        clearIndexes();
        clearDataChanges();
        clearConstraints();
        clearTriggers();
    }

    public void clearDefinitionChanges() {
        modifiedSQLText = null;
        clearColumns();
        clearIndexes();
    }

    public void clearColumns() {
        if (columns != null) {
            columns.clear();
        }
        columns = null;
    }

    public void clearConstraints() {
        if (constraints != null) {
            constraints.clear();
        }
        constraints = null;
        checkConstraints = null;
    }

    public void clearIndexes() {
        if (indexes != null) {
            indexes.clear();
        }
        indexes = null;
    }

    public void clearTriggers() {
        if (triggers != null) {
            triggers.clear();
        }
        triggers = null;
    }

    /**
     * Reverts any changes made to this table and associated elements.
     */
    @Override
    public void revert() {

        List<DatabaseColumn> newColumns = new ArrayList<>();
        for (DatabaseColumn i : columns) {

            DatabaseTableColumn column = (DatabaseTableColumn) i;
            if (!column.isNewColumn())
                column.revert();
            else
                newColumns.add(column);
        }

        for (DatabaseColumn column : newColumns)
            columns.remove(column);

        newColumns.clear();
        tableDataChanges().clear();
        modifiedSQLText = null;

    }

    /**
     * Applies any changes to the database.
     */
    @Override
    public int applyChanges() throws DataSourceException {

        int result = applyTableDefinitionChanges();
        result += applyTableDataChanges();

        return result;
    }

    @Override
    public void cancelChanges() {
        if (tableDataChangeExecutor != null)
            tableDataChangeExecutor.cancel();
        tableDataChangeExecutor = null;
    }

    public int applyTableDefinitionChanges() throws DataSourceException {

        Statement stmnt = null;

        try {

            String changes = getModifiedSQLText();
            if (StringUtils.isBlank(changes)) {

                // bail if we're empty here
                return 1;
            }

            int result = 0;
            String[] queries = changes.split(";");

            Connection connection = getHost().getConnection();
            stmnt = connection.createStatement();

            for (String s : queries) {
                String query = s.trim();
                if (StringUtils.isNotBlank(query))
                    result += stmnt.executeUpdate(query);
            }

            if (!connection.getAutoCommit())
                connection.commit();

            // set to reset for the next call
            reset();

            return result;

        } catch (SQLException e) {
            throw new DataSourceException(e);

        } finally {
            releaseResources(stmnt);
        }
    }

    @Override
    public boolean hasTableDefinitionChanges() {
        return StringUtils.isNotBlank(getModifiedSQLText());
    }

    /**
     * Indicates whether this table or any of its columns
     * or constraints have pending modifications to be applied.
     *
     * @return true | false
     */
    @Override
    public boolean isAltered() throws DataSourceException {

        if (hasTableDataChanges())
            return true;

        List<DatabaseColumn> _columns = getColumns();
        if (_columns != null) {
            for (DatabaseColumn i : _columns) {
                DatabaseTableColumn column = (DatabaseTableColumn) i;
                if (column.hasChanges())
                    return true;
            }
        }

        List<ColumnConstraint> constraints = getConstraints();
        if (constraints != null)
            for (ColumnConstraint i : constraints)
                if (i.isNewConstraint() || i.isAltered())
                    return true;

        return false;
    }

    /**
     * Returns the ALTER TABLE statement to modify this constraint.
     */
    @Override
    public String getAlteredSQLText() throws DataSourceException {

        StringBuilder sb = new StringBuilder();
        List<DatabaseColumn> _columns = getColumns();
        List<ColumnConstraint> _constraints = getConstraints();
        boolean first = true;

        sb.append("ALTER TABLE ").append(MiscUtils.getFormattedObject(getName(), getHost().getDatabaseConnection()));
        if (_constraints != null) {
            for (ColumnConstraint constraint : _constraints) {
                if (constraint instanceof TableColumnConstraint) {

                    TableColumnConstraint dtc = (TableColumnConstraint) constraint;
                    if (dtc.isMarkedDeleted()) {
                        if (!first)
                            sb.append(",");
                        first = false;
                        sb.append("\nDROP CONSTRAINT ").append(MiscUtils.getFormattedObject(dtc.getName(), getHost().getDatabaseConnection()));
                    }
                }
            }
        }

        if (_columns != null) {
            for (DatabaseColumn column : _columns) {
                if (column instanceof DatabaseTableColumn) {

                    DatabaseTableColumn dtc = (DatabaseTableColumn) column;
                    if (dtc.isMarkedDeleted()) {
                        if (!first)
                            sb.append(",");
                        first = false;
                        sb.append("\nDROP ").append(dtc.getNameEscaped());
                    } else if (dtc.isPositionChanged()) {
                        if (!first)
                            sb.append(",");
                        first = false;
                        sb.append("\nALTER COLUMN ").append(dtc.getNameEscaped()).append(" POSITION ").append(dtc.getPosition());
                    }
                }
            }
        }

        return first ? "" : sb.toString();
    }

    @Override
    public String getCreateSQLText() throws DataSourceException {

        updateListCD();
        updateListCC();

        return SQLUtils.generateCreateTable(getName(), listCD, listCC, true, false, true,
                true, true, null, getExternalFile(),
                getAdapter(), getSqlSecurity(), getTablespace(), getRemarks(), ";");
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("TABLE", getName(), getHost().getDatabaseConnection());
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {

        updateListCD();
        updateListCC();

        if (Comparer.isComputedFieldsNeed())
            listCD.stream().filter(cd -> !MiscUtils.isNull(cd.getComputedBy())).forEach(cd -> cd.setComputedBy(null));

        return SQLUtils.generateCreateTable(getName(), listCD, listCC, true, false, false,
                false, Comparer.isCommentsNeed(), null, getExternalFile(),
                getAdapter(), getSqlSecurity(), getTablespace(), getRemarks(), ";");
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) {
        DefaultDatabaseTable comparingTable = (DefaultDatabaseTable) databaseObject;
        return SQLUtils.generateAlterTable(this, comparingTable, false,
                new boolean[]{false, false, false, false}, Comparer.isComputedFieldsNeed(), Comparer.isFieldsPositionsNeed());
    }

    public String getDropSQLText(boolean cascadeConstraints) {

    /*StatementGenerator statementGenerator = null;
    String databaseProductName = databaseProductName();

    String dropStatement = null;
    if (cascadeConstraints) {

      dropStatement = statementGenerator.dropTableCascade(databaseProductName, this);

    } else {

      dropStatement = statementGenerator.dropTable(databaseProductName, this);
    }

    return dropStatement;*/

        return null;
    }

    @Override
    public boolean hasForeignKey() {
        List<ColumnConstraint> keys = getForeignKeys();
        return keys != null && !keys.isEmpty();
    }

    @Override
    public boolean hasPrimaryKey() {
        List<ColumnConstraint> keys = getPrimaryKeys();
        return keys != null && !keys.isEmpty();
    }

    @Override
    public List<ColumnConstraint> getPrimaryKeys() {

        List<ColumnConstraint> primaryKeys = new ArrayList<>();
        List<ColumnConstraint> _constraints = getConstraints();

        for (ColumnConstraint columnConstraint : _constraints)
            if (columnConstraint.isPrimaryKey())
                primaryKeys.add(columnConstraint);

        return primaryKeys;
    }

    @Override
    public List<DatabaseColumn> getPrimaryKeysColumns() {

        List<DatabaseColumn> primaryKeys = new ArrayList<>();
        List<DatabaseColumn> _cols = getColumns();

        for (DatabaseColumn column : _cols)
            if (column.isPrimaryKey())
                primaryKeys.add(column);

        return primaryKeys;
    }

    @Override
    public List<ColumnConstraint> getForeignKeys() {

        List<ColumnConstraint> foreignKeys = new ArrayList<>();
        List<ColumnConstraint> _constraints = getConstraints();

        for (ColumnConstraint columnConstraint : _constraints)
            if (columnConstraint.isForeignKey())
                foreignKeys.add(columnConstraint);

        return foreignKeys;
    }

    @Override
    public List<DatabaseColumn> getForeignKeysColumns() {

        List<DatabaseColumn> keys = new ArrayList<>();
        List<DatabaseColumn> _cols = getColumns();

        for (DatabaseColumn column : _cols)
            if (column.isForeignKey())
                keys.add(column);

        return keys;
    }

    @Override
    public List<ColumnConstraint> getUniqueKeys() {

        List<ColumnConstraint> uniqueKeys = new ArrayList<>();
        List<ColumnConstraint> _constraints = getConstraints();

        for (ColumnConstraint columnConstraint : _constraints)
            if (columnConstraint.isUniqueKey())
                uniqueKeys.add(columnConstraint);

        return uniqueKeys;
    }

    @Override
    public List<DatabaseColumn> getUniqueKeysColumns() {

        List<DatabaseColumn> keys = new ArrayList<>();
        List<DatabaseColumn> _cols = getColumns();

        for (DatabaseColumn column : _cols)
            if (column.isUnique())
                keys.add(column);

        return keys;
    }

    @Override
    public String getAlterSQLTextForUniqueKeys() {

    /*StatementGenerator statementGenerator = null;
    return statementGenerator.createUniqueKeyChange(databaseProductName(), this);*/

        return null;
    }

    @Override
    public String getAlterSQLTextForForeignKeys() {

    /*StatementGenerator statementGenerator = null;
    return statementGenerator.createForeignKeyChange(databaseProductName(), this);*/

        return null;
    }

    @Override
    public String getAlterSQLTextForPrimaryKeys() {

    /*StatementGenerator statementGenerator = null;
    return statementGenerator.createPrimaryKeyChange(databaseProductName(), this);
     */
        return null;
    }

    public String getCreateConstraintsSQLText() throws DataSourceException {

    /*StatementGenerator statementGenerator = null;
    String databaseProductName = databaseProductName();
    return statementGenerator.tableConstraintsAsAlter(databaseProductName, this);*/

        return null;
    }

    /**
     * Returns the CREATE TABLE statement for this database table.
     * This will be table column (plus data type) definitions only,
     * this does not include constraint metadata.
     */
    @Override
    public String getCreateSQLText(int style) throws DataSourceException {

        updateListCD();
        updateListCC();

        return SQLUtils.generateCreateTable(getName(), listCD, listCC,
                true, false, true, true, true, null,
                getExternalFile(), getAdapter(), getSqlSecurity(), getTablespace(), getRemarks(), ";");
    }

    protected void updateListCD() {
        listCD = new ArrayList<>();
        for (int i = 0; i < getColumnCount(); i++)
            listCD.add(new ColumnData(getHost().getDatabaseConnection(), getColumns().get(i), false));
    }

    protected void updateListCC() {
        listCC = new ArrayList<>();
        for (int i = 0; i < getConstraints().size(); i++)
            listCC.add(new org.executequery.gui.browser.ColumnConstraint(false, getConstraints().get(i)));
    }

    /**
     * Returns the user modified SQL text to apply
     * any pending changes. If this has not been set (no
     * changes were made) then a call to getAlteredSQLText()
     * is made.
     *
     * @return the modified SQL
     */
    @Override
    public String getModifiedSQLText() throws DataSourceException {
        if (modifiedSQLText == null)
            return getAlteredSQLText();
        return modifiedSQLText;
    }

    @Override
    public void setModifiedSQLText(String modifiedSQLText) {
        this.modifiedSQLText = modifiedSQLText;
    }

    @Override
    public String getInsertSQLText() {

        String fields = "";
        String values = "";

        try {

            List<DatabaseColumn> columns = getColumns();
            for (int i = 0, n = columns.size(); i < n; i++) {

                DatabaseTableColumn column = (DatabaseTableColumn) columns.get(i);
                fields += column.getNameForQuery();
                values += ":" + toCamelCase(column.getName());

                if (i < n - 1) {
                    fields += ", ";
                    values += ", ";
                }
            }

        } catch (DataSourceException e) {

            fields = "_fields_";
            values = "_values_";
            e.printStackTrace();
        }

        return getFormatter().format(SQLUtils.generateDefaultInsertStatement(getName(), fields, values, getHost().getDatabaseConnection()));
    }

    @Override
    public String getUpdateSQLText() {

        String settings = "";

        try {

            List<DatabaseColumn> columns = getColumns();
            for (int i = 0, n = columns.size(); i < n; i++) {

                DatabaseTableColumn column = (DatabaseTableColumn) columns.get(i);
                settings += column.getNameForQuery() + " = :" + toCamelCase(column.getName());

                if (i < n - 1)
                    settings += ", ";
            }

        } catch (DataSourceException e) {

            settings = "_oldValue_ = _newValue_";
            e.printStackTrace();
        }

        return getFormatter().format(SQLUtils.generateDefaultUpdateStatement(getName(), settings, getHost().getDatabaseConnection()));
    }

    @Override
    public String getSelectSQLText() {

        String fields = "";

        try {

            List<DatabaseColumn> columns = getColumns();
            for (int i = 0, n = columns.size(); i < n; i++) {

                DatabaseTableColumn column = (DatabaseTableColumn) columns.get(i);
                fields += column.getNameForQuery();

                if (i < n - 1)
                    fields += ", ";
            }

        } catch (DataSourceException e) {

            fields = "*";
            e.printStackTrace();
        }

        return getFormatter().format(SQLUtils.generateDefaultSelectStatement(getName(), fields, getHost().getDatabaseConnection()));
    }

    protected TokenizingFormatter getFormatter() {
        if (formatter == null)
            formatter = new TokenizingFormatter();
        return formatter;
    }

    private String getSpacesForLength(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            sb.append(' ');
        return sb.toString();
    }

    @Override
    public DatabaseSource getDatabaseSource() {
        if (getParent() != null)
            return (DatabaseSource) getParent().getParent();
        return null;
    }

    @Override
    public String getParentNameForStatement() {
        if (getParent() != null && getParent().getParent() != null)
            return getParent().getParent().getName();
        return null;
    }

    @Override
    public boolean hasSQLDefinition() {
        return true;
    }


    @Override
    public String prepareStatementWithPK(List<String> columns) {

        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ").append(getNameWithPrefixForQuery()).append(" SET ");
        for (String column : columns)
            sb.append(MiscUtils.getFormattedObject(column, getHost().getDatabaseConnection())).append(" = ?,");
        sb.deleteCharAt(sb.length() - 1);
        sb.append(" WHERE ");

        boolean applied = false;
        for (String primaryKey : getPrimaryKeyColumnNames()) {

            if (applied)
                sb.append(" AND ");
            sb.append(MiscUtils.getFormattedObject(primaryKey, getHost().getDatabaseConnection())).append(" = ? ");
            applied = true;
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    @Override
    public String prepareStatementDeletingWithPK() {

        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ").append(getNameWithPrefixForQuery());
        sb.append(" WHERE ");

        boolean applied = false;
        for (String primaryKey : getPrimaryKeyColumnNames()) {

            if (applied)
                sb.append(" AND ");
            sb.append(MiscUtils.getFormattedObject(primaryKey, getHost().getDatabaseConnection())).append(" = ? ");
            applied = true;
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    @Override
    public List<String> getPrimaryKeyColumnNames() {
        return namesFromColumns(getPrimaryKeysColumns());
    }

    @Override
    public List<String> getForeignKeyColumnNames() {
        return namesFromColumns(getForeignKeysColumns());
    }

    private List<String> namesFromConstraints(List<ColumnConstraint> constraints) {

        List<String> names = new ArrayList<>();
        for (ColumnConstraint constraint : constraints)
            names.add(constraint.getColumnName());

        return names;
    }

    private List<String> namesFromColumns(List<DatabaseColumn> columns) {

        List<String> names = new ArrayList<>();
        for (DatabaseColumn column : columns)
            names.add(column.getName());

        return names;
    }

    @Override
    public String getExternalFile() {
        checkOnReload(externalFile);
        return externalFile;
    }

    public void setExternalFile(String externalFile) {
        this.externalFile = externalFile;
    }

    @Override
    public String getAdapter() {
        checkOnReload(adapter);
        return adapter;
    }

    public void setAdapter(String adapter) {
        this.adapter = adapter;
    }

    protected TreeSet<String> conNames;

    @Override
    protected String getFieldName() {
        return "RELATION_NAME";
    }

    @Override
    protected Table getMainTable() {
        return Table.createTable("RDB$RELATIONS", "R");
    }

    @Override
    protected SelectBuilder builderForInfoAllObjects(SelectBuilder commonBuilder) {
        SelectBuilder sb = super.builderForInfoAllObjects(commonBuilder);
        sb.appendCondition(Condition.createCondition(Field.createField(getMainTable(), "VIEW_BLR"), "IS", "NULL"));
        if (getDatabaseMajorVersion() >= 2 && !(this instanceof DefaultTemporaryDatabaseTable)) {
            sb.appendCondition(Condition.createCondition()
                    .appendCondition(Condition.createCondition(Field.createField(getMainTable(), "RELATION_TYPE"), "IS", "NULL"))
                    .appendCondition(Condition.createCondition(Field.createField(getMainTable(), "RELATION_TYPE"), "=", "0"))
                    .appendCondition(Condition.createCondition(Field.createField(getMainTable(), "RELATION_TYPE"), "=", "2"))
                    .setLogicOperator("OR"));
        }
        return sb;
    }

    @Override
    protected SelectBuilder builderCommonQuery() {
        SelectBuilder sb = new SelectBuilder(getHost().getDatabaseConnection());
        sb.setDistinct(true);
        Table rels = Table.createTable("RDB$RELATIONS", "R");
        Table relCons = Table.createTable("RDB$RELATION_CONSTRAINTS", "RC");
        Table checkCons = Table.createTable("RDB$CHECK_CONSTRAINTS", "CC");
        Table triggers = Table.createTable("RDB$TRIGGERS", "T");

        Field conName = Field.createField(relCons, CONSTRAINT_NAME);
        Field conType = Field.createField(relCons, CONSTRAINT_TYPE);
        Function compareCheck = Function.createFunction("IIF")
                .appendArgument(conType.getFieldTable() + " <> 'CHECK'")
                .appendArgument("NULL")
                .appendArgument(conName.getFieldTable());
        sb.appendField(getObjectField());
        sb.appendField(Field.createField().setStatement(compareCheck.getStatement()).setAlias(conName.getAlias()));
        compareCheck.setArgument(2, conType.getFieldTable());
        sb.appendField(Field.createField().setStatement(compareCheck.getStatement()).setAlias(conType.getAlias()));
        sb.appendField(Field.createField(triggers, TRIGGER_SOURCE));
        sb.appendField(buildSqlSecurityField(rels));
        sb.appendField(Field.createField(rels, EXTERNAL_FILE));
        sb.appendField(Field.createField(rels, ADAPTER).setNull(!isRDB()));
        sb.appendField(Field.createField(rels, TABLESPACE + "_NAME").setAlias(TABLESPACE).
                setNull(!tablespaceCheck()));
        sb.appendField(Field.createField(rels, DESCRIPTION));
        sb.appendJoin(Join.createLeftJoin().appendFields(getObjectField(), Field.createField(relCons, getObjectField().getAlias())));
        sb.appendJoin(Join.createLeftJoin().appendFields(conName, Field.createField(checkCons, conName.getAlias())));
        sb.appendJoin(Join.createLeftJoin().appendFields(Field.createField(checkCons, "TRIGGER_NAME"),
                Field.createField(triggers, "TRIGGER_NAME")));

        sb.appendCondition(Condition.createCondition()
                .appendCondition(Condition.createCondition(Field.createField(triggers, "TRIGGER_TYPE"), "=", "1"))
                .appendCondition(Condition.createCondition(Field.createField(triggers, "TRIGGER_TYPE"), "IS", "NULL"))
                .setLogicOperator("OR"));
        sb.setOrdering("1");

        return sb;
    }

    protected void addingConstraint(ResultSet rs) throws SQLException {
        String conType = rs.getString(CONSTRAINT_TYPE);
        if (conType != null) {
            String name = rs.getString(CONSTRAINT_NAME).trim();
            if (!conNames.contains(name)) {
                ColumnConstraint constraint = new TableColumnConstraint(rs.getString(TRIGGER_SOURCE));
                constraint.setName(name);
                constraint.setTable(this);
                checkConstraints.add(constraint);
                conNames.add(name);
            }
        }
    }

    @Override
    public Object setInfoFromSingleRowResultSet(ResultSet rs, boolean first) throws SQLException {
        if (first) {
            setRemarks(getFromResultSet(rs, DESCRIPTION));
            setSqlSecurity(getFromResultSet(rs, SQL_SECURITY));
            setExternalFile(getFromResultSet(rs, EXTERNAL_FILE));
            setAdapter(getFromResultSet(rs, ADAPTER));
            setTablespace(getFromResultSet(rs, TABLESPACE));
        }
        addingConstraint(rs);
        return null;
    }

    @Override
    public void prepareLoadingInfo() {
        checkConstraints = new ArrayList<>();
        conNames = new TreeSet<>();
    }

    @Override
    public void finishLoadingInfo() {

    }

    @Override
    public boolean isAnyRowsResultSet() {
        return true;
    }


    public String getTablespace() {
        checkOnReload(tablespace);
        return tablespace;
    }

    public void setTablespace(String tablespace) {
        this.tablespace = tablespace;
    }

}


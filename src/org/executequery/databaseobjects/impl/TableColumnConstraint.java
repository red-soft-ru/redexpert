/*
 * TableColumnConstraint.java
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
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.DatabaseTableObject;
import org.executequery.databaseobjects.NamedObject;
import org.underworldlabs.jdbc.DataSourceException;

import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Takis Diakoumis
 */
public class TableColumnConstraint extends AbstractDatabaseObjectElement
        implements ColumnConstraint {

    public static final String EMPTY = "";

    /**
     * the table column this constraint belongs to
     */
    private DatabaseTableColumn column;

    private List<String> columnsDisplayList;
    private List<String> referenceColumnsDisplayList;

    /**
     * The referenced table of this constraint
     */
    private String referencedTable;

    /**
     * The referenced column of this constraint
     */
    private String referencedColumn;

    /**
     * What happens to a foreign key when the primary key is updated
     */
    private String updateRule;

    /**
     * What happens to the foreign key when primary is deleted
     */
    private String deleteRule;

    /**
     * The type of constraint
     */
    private int keyType;

    /**
     * Whether this constraint is new (for editing a table definition)
     */
    private boolean newConstraint;

    /**
     * Whether this constraint is marked to be dropped
     */
    private boolean markedDeleted;

    /**
     * an original copy of this object
     */
    private TableColumnConstraint copy;

    /**
     * the foreign key column
     */
    private DatabaseColumn foreignKeyColumn;

    /**
     * the column meta data map
     */
    private Map<String, String> metaData;

    private String check;

    private boolean indexDesc = false;
    private String indexName;

    /**
     * Creates a new instance of TableColumnConstraint
     */
    public TableColumnConstraint(int type) {
        this(null, type);
    }

    /**
     * Creates a new instance of TableColumnConstraint
     */
    public TableColumnConstraint(DatabaseTableColumn column, int keyType) {
        setColumn(column);
        setKeyType(keyType);
    }

    public TableColumnConstraint(String Check) {
        setCheck(Check);
        setKeyType(CHECK_KEY);
    }

    /**
     * Returns the column object referenced by this column or
     * null if its not a foreign key column.
     *
     * @return the referenced column
     */
    public DatabaseColumn getForeignKeyReference() {

        if (isForeignKey()) {

            return foreignKeyColumn;
        }

        return null;
    }

    /**
     * Returns whether this is a foreign key constraint.
     *
     * @return true | false
     */
    public boolean isForeignKey() {
        return getKeyType() == FOREIGN_KEY;
    }

    /**
     * Returns whether this is a primary key constraint.
     *
     * @return true | false
     */
    public boolean isPrimaryKey() {
        return getKeyType() == PRIMARY_KEY;
    }

    /**
     * Returns whether this is a unique key constraint.
     *
     * @return true | false
     */
    public boolean isUniqueKey() {
        return getKeyType() == UNIQUE_KEY;
    }

    @Override
    public boolean isCheck() {
        return getKeyType() == CHECK_KEY;
    }

    /**
     * Returns whether this is a new constraint.
     *
     * @return true | false
     */
    public boolean isNewConstraint() {
        return newConstraint;
    }

    /**
     * Sets this constraint as a new constraint as specified.
     *
     * @param newConstraint true | false
     */
    public void setNewConstraint(boolean newConstraint) {
        this.newConstraint = newConstraint;
    }

    /**
     * Returns the string representation of this constraints
     * type - ie. PRIMARY, FOREIGN, UNIQUE.
     *
     * @return the type name
     */
    public String getTypeName() {
        int _type = getKeyType();
        switch (_type) {
            case PRIMARY_KEY:
                return PRIMARY;
            case FOREIGN_KEY:
                return FOREIGN;
            case UNIQUE_KEY:
                return UNIQUE;
            case CHECK_KEY:
                return CHECK;
            default:
                return null;
        }
    }

    /**
     * Sets the constraint type as specified.
     *
     * @param keyType the constraint type
     */
    public void setKeyType(int keyType) {
        this.keyType = keyType;
    }

    /**
     * Returns the constraint type identifier.
     *
     * @return the type int
     */
    public int getKeyType() {
        return keyType;
    }

    /**
     * Returns the table associated with this constraint.
     *
     * @return the table
     */
    public DatabaseTableObject getTable() {
        if (column != null) {
            return column.getTable();
        }
        return null;
    }

    @Override
    public void setTable(DatabaseTable table) {
        this.column = new DatabaseTableColumn(table);
    }

    /**
     * Returns the table name associated with this constraint.
     *
     * @return the table name
     */
    public String getTableName() {
        if (getTable() != null) {
            return getTable().getName();
        }
        return null;
    }

    /**
     * Returns the column name associated with this constraint.
     *
     * @return the column name
     */
    public String getColumnName() {
        if (column != null) {
            return column.getName();
        }
        return null;
    }

    @Override
    public List<String> getColumnDisplayList() {
        return columnsDisplayList;
    }

    @Override
    public List<String> getReferenceColumnDisplayList() {
        return referenceColumnsDisplayList;
    }

    /**
     * Returns the table column parent to this object.
     *
     * @return the table column
     */
    public DatabaseTableColumn getColumn() {
        return column;
    }

    /**
     * Sets the table column parent to this object
     * to that specified.
     *
     * @param column parent column
     */
    @Override
    public void setColumn(DatabaseTableColumn column) {
        this.column = column;
        columnsDisplayList = new ArrayList<>();
        if (column != null)
            columnsDisplayList.add(column.getName().trim());
    }

    @Override
    public void addColumnToDisplayList(DatabaseTableColumn column) {
        if (!columnsDisplayList.contains(column.getName()))
            columnsDisplayList.add(column.getName());
    }

    @Override
    public void addReferenceColumnToDisplayList(String column) {
        if (!referenceColumnsDisplayList.contains(column))
            referenceColumnsDisplayList.add(column.trim());
    }

    public String getReferencedTable() {
        return referencedTable;
    }

    public void setReferencedTable(String referencedTable) {
        this.referencedTable = referencedTable;
    }

    public String getReferencedColumn() {
        return referencedColumn;
    }

    public void setReferencedColumn(String referencedColumn) {
        this.referencedColumn = referencedColumn;
        referenceColumnsDisplayList = new ArrayList<>();
        if (referencedColumn != null)
            referenceColumnsDisplayList.add(referencedColumn.trim());
    }

    @Override
    public String getCheck() {
        return check;
    }

    @Override
    public void setCheck(String check) {
        this.check = check;
    }

    public String getUpdateRule() {
        return updateRule;
    }

    public void setUpdateRule(String updateRule) {
        this.updateRule = updateRule;
    }

    public String getDeleteRule() {
        return deleteRule;
    }

    public void setDeleteRule(String deleteRule) {
        this.deleteRule = deleteRule;
    }

    public boolean isMarkedDeleted() {
        return markedDeleted;
    }

    public void setMarkedDeleted(boolean markedDeleted) {
        this.markedDeleted = markedDeleted;
    }

    /**
     * Returns whether this constraint has been modified.
     * A modification exists where this constraint is not new,
     * an internal value has changed or it has been marked
     * for deletion.
     *
     * @return true | false
     */
    public boolean isAltered() {
        // check if its new - not modified
        if (isNewConstraint()) {
            return false;
        }

        // check for a pending deletion
        if (isMarkedDeleted()) {
            return true;
        }

        // ensure we have something to compare to
        if (copy == null) {
            return false;
        }

        // allow for name changes only
        return !(copy.getName().equalsIgnoreCase(getName()));
    }



    /**
     * Makes a copy of itself. A copy of this object may
     * not always be required and may be made available only
     * when deemed necessary - ie. table meta changes.
     */
    public void makeCopy() {
        if (copy == null) {
            // ensure the table column has a copy
            getColumn().makeCopy();
            // make a local copy
            copy = new TableColumnConstraint(getKeyType());
            copyConstraint(this, copy);
        }
    }

    /**
     * Creates and returns a copy of this object.
     *
     * @return a clone of this object
     */
    public Object clone() throws CloneNotSupportedException {
        TableColumnConstraint clone = new TableColumnConstraint(getKeyType());
        copyConstraint(this, clone);
        return clone;
    }

    /**
     * Copies the specified source object to the specified
     * destination object of the same type.
     *
     * @param source      the source constraint object
     * @param destination the destination constraint
     */
    protected void copyConstraint(TableColumnConstraint source, TableColumnConstraint destination) {
        destination.setKeyType(source.getKeyType());
        destination.setColumn(source.getColumn());
        destination.setReferencedTable(source.getReferencedTable());
        destination.setReferencedColumn(source.getReferencedColumn());
        destination.setName(source.getName());
        destination.setCheck(source.getCheck());
        destination.setIndexDesc(source.isIndexDesc());
    }

    /**
     * Reverts any changes made to this constraint.
     */
    public void revert() {

        if (isMarkedDeleted()) {

            setMarkedDeleted(false);

        } else if (isAltered()) {

            copyConstraint(copy, this);
            copy = null;
        }

    }

    /**
     * Does nothing.
     */
    public int drop() throws DataSourceException {
        return 0;
    }

    @Override
    public boolean allowsChildren() {
        return false;
    }

    /**
     * Returns the parent named object of this object.
     *
     * @return the parent object
     */
    public NamedObject getParent() {
        return getColumn();
    }

    /**
     * Detaches this constraint from the owner column
     */
    public void detachFromColumn() {
        column.removeConstraint(this);
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
        for (String key : this.metaData.keySet()) {

            String value = this.metaData.get(key);
            if (StringUtils.isNotBlank(value)) {
                if (StringUtils.equalsIgnoreCase("DELETE_RULE", key)) {

                    this.metaData.put(key, translateDeletedRule(shortValue(value)));

                } else if (StringUtils.equalsIgnoreCase("UPDATE_RULE", key)) {

                    this.metaData.put(key, translateUpdateRule(shortValue(value)));

                } else if (StringUtils.equalsIgnoreCase("DEFERRABILITY", key)) {

                    this.metaData.put(key, translateDeferrabilityRule(shortValue(value)));
                }
            }
        }
    }

    private Short shortValue(String value) {

        return Short.valueOf(value);
    }

    private String translateDeferrabilityRule(Short value) {

        String translated = String.valueOf(value);
        if (isForeignKey()) {

            switch (value) {
                case DatabaseMetaData.importedKeyInitiallyDeferred:
                    return translated + " - importedKeyInitiallyDeferred";

                case DatabaseMetaData.importedKeyInitiallyImmediate:
                    return translated + " - importedKeyInitiallyImmediate";

                case DatabaseMetaData.importedKeyNotDeferrable:
                    return translated + " - importedKeyNotDeferrable";
            }

        }
        return translated;
    }

    private String translateDeletedRule(Short value) {

        String translated = String.valueOf(value);
        if (isForeignKey()) {

            switch (value) {
                case DatabaseMetaData.importedKeyNoAction:
                    return translated + " - importedKeyNoAction";

                case DatabaseMetaData.importedKeyCascade:
                    return translated + " - importedKeyCascade";

                case DatabaseMetaData.importedKeySetNull:
                    return translated + " - importedKeySetNull";

                case DatabaseMetaData.importedKeyRestrict:
                    return translated + " - importedKeyRestrict";

                case DatabaseMetaData.importedKeySetDefault:
                    return translated + " - importedKeySetDefault";
            }

        }
        return translated;
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }

    private String translateUpdateRule(Short value) {

        String translated = String.valueOf(value);
        if (isForeignKey()) {

            switch (value) {
                case DatabaseMetaData.importedKeyNoAction:
                    return translated + " - importedKeyNoAction";

                case DatabaseMetaData.importedKeyCascade:
                    return translated + " - importedKeyCascade";

                case DatabaseMetaData.importedKeySetNull:
                    return translated + " - importedKeySetNull";

                case DatabaseMetaData.importedKeyRestrict:
                    return translated + " - importedKeyRestrict";

                case DatabaseMetaData.importedKeySetDefault:
                    return translated + " - importedKeySetDefault";
            }

        }
        return translated;
    }

    @Override
    public int getType() {

        if (isForeignKey()) {

            return FOREIGN_KEY;

        } else if (isPrimaryKey()) {

            return PRIMARY_KEY;

        }
        if (isUniqueKey()) {

            return UNIQUE_KEY;
        } else return CHECK_KEY;

    }

    @Override
    public boolean isIndexDesc() {
        return indexDesc;
    }

    @Override
    public void setIndexDesc(boolean indexDesc) {
        this.indexDesc = indexDesc;
    }

    @Override
    public String getIndexName() {
        return indexName;
    }

    @Override
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
}



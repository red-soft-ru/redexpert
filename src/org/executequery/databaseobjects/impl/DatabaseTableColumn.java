/*
 * DatabaseTableColumn.java
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
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.underworldlabs.util.MiscUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author takisd
 */
public class DatabaseTableColumn extends DefaultDatabaseColumn {

    /**
     * Whether this column is a new column in the table
     */
    private boolean newColumn;

    /**
     * Whether this column is marked as to be deleted
     */
    private boolean markedDeleted;

    /**
     * The table this column belongs to
     */
    protected DatabaseTable table;

    /**
     * constraints of this column
     */
    private List<ColumnConstraint> constraints;

    /**
     * an original copy of this object
     */
    private DatabaseTableColumn copy;

    //private transient static final StatementGenerator STATEMENT_GENERATOR = new LiquibaseStatementGenerator();

    /**
     * Creates a new instance of DatabaseTableColumn belonging to the
     * specified table.
     */
    public DatabaseTableColumn(DatabaseTable table) {
        this(table, null);
    }

    /**
     * Creates a new instance of DatabaseTableColumn with values derived from
     * the specified column and belonging to the specified table.
     */
    public DatabaseTableColumn(DatabaseTable table, DatabaseColumn column) {
        setTable(table);
        if (column != null) {
            initValues(column);
        }
    }

    /**
     * Initialises this object with values derived from the specified column.
     *
     * @param column
     */
    protected void initValues(DatabaseColumn column) {
        setCatalogName(column.getCatalogName());
        setSchemaName(column.getSchemaName());
        setName(column.getName());
        setTypeInt(column.getTypeInt());
        setTypeName(column.getTypeName());
        setColumnSize(column.getColumnSize());
        setColumnScale(column.getColumnScale());
        setRequired(column.isRequired());
        setRemarks(column.getRemarks());
        setDefaultValue(column.getDefaultValue());
        setPrimaryKey(column.isPrimaryKey());
        setForeignKey(column.isForeignKey());
        setGenerated(column.isGenerated());
        setComputedSource(column.getComputedSource());
        setColumnDescription(column.getColumnDescription());
        setDomain(column.getDomain());
        setColumnSubtype(column.getColumnSubtype());
        setIdentity(column.isIdentity());
    }

    @Override
    public String getDescription() {

        StringBuilder sb = new StringBuilder();
        sb.append("TABLE COLUMN: ");
        sb.append(getName().trim()).append("[").append(getTypeName()).append("]");

        if (isPrimaryKey()) {

            sb.append(" PRIMARY KEY");

        } else if (isForeignKey()) {

            sb.append(" FOREIGN KEY");

        } else if (isUnique()) {

            sb.append(" UNIQUE");
        }

        return sb.toString();
    }

    public String getNameEscaped() {

        return MiscUtils.getFormattedObject(getName());
    }

    /**
     * Returns the constraints attached to this table column.
     *
     * @return the column constraints
     */
    public List<ColumnConstraint> getConstraints() {

        return constraints;
    }

    public boolean hasChanges() {

        //*************** 
        if (hasConstraintsChanged()) {

            return true;

        } else if (!isNewColumn() && !isMarkedDeleted() && !hasCopy()) {

            return false;

        } else {

            return (isMarkedDeleted()
                    || isNewColumn()
                    || isNameChanged()
                    || isDataTypeChanged()
                    || isRequiredChanged()
                    || isDefaultValueChanged()
                    || isComputedChanged()
                    || isDescriptionChanged()
                    || isDomainChanged()
            );
        }
    }

    private boolean hasConstraintsChanged() {

        List<ColumnConstraint> constraints = getConstraints();
        if (constraints != null) {

            for (ColumnConstraint i : constraints) {

                if (i.isNewConstraint() || i.isAltered()) {

                    return true;
                }

            }

        }

        return false;
    }

    public boolean isRequiredChanged() {

        if (!hasCopy()) {

            return false;
        }

        return (copy.isRequired() != isRequired());
    }

    /**
     * Returns the ALTER TABLE statement to modify this constraint.
     */
    public String getAlteredSQLText() {

        if (!hasChanges()) {

            return "";
        }

//        StatementGenerator statementGenerator = new LiquibaseStatementGenerator();
//        
//        return statementGenerator.alterTable(
//                getTable().getHost().getDatabaseProductName(), getTable());

        StringBuilder sb = new StringBuilder();

        sb.append("ALTER TABLE ");
        sb.append(getTable().getName());

        // new definition for a new column
        if (isNewColumn()) {
            sb.append(" ADD COLUMN ");
            sb.append(getName());
            sb.append(" ");
            sb.append(getFormattedDataType());

            if (!MiscUtils.isNull(getDefaultValue())) {
                sb.append(" DEFAULT ");
                sb.append(getDefaultValue());
            }

            if (isRequired()) {
                sb.append(" NOT NULL");
            }

            sb.append(";");
            return sb.toString();
        }

        // check for a pending deletion
        if (isMarkedDeleted()) {
            sb.append(" DROP COLUMN ");
            sb.append(!hasCopy() ? getName() : copy.getName());
            sb.append(";");
            return sb.toString();
        }

        // column name change
        boolean hasNameChange = false;
        if (isNameChanged()) {
            sb.append(" RENAME COLUMN ");
            sb.append(copy.getName());
            sb.append(" TO ");
            sb.append(getName());
            sb.append(";");
            hasNameChange = true;
        }

        if (!copy.getTypeName().equalsIgnoreCase(getTypeName()) ||
                copy.getColumnSize() != getColumnSize() ||
                copy.getColumnScale() != getColumnScale() ||
                copy.isRequired() != isRequired() ||
                isDefaultValueChanged()) {

            if (hasNameChange) {
                sb.append("\n");
            }

            sb.append(" MODIFY COLUMN ");
            sb.append(getName());
            sb.append(" ");
            sb.append(getFormattedDataType());

            if (isDefaultValueChanged()) {
                sb.append(" DEFAULT ");
                if (MiscUtils.isNull(getDefaultValue())) {
                    sb.append("NULL");
                } else {
                    sb.append(getDefaultValue());
                }
            }

            if (isRequired()) {
                sb.append(" NOT NULL");
            }
            sb.append(";");
        }

        return sb.toString();
    }

    public boolean isNameChanged() {

        if (!hasCopy()) {

            return false;
        }

        return !(copy.getName().equalsIgnoreCase(getName()));
    }

    public boolean isDataTypeChanged() {

        if (!hasCopy()) {

            return false;
        }

        return (!copy.getTypeName().equalsIgnoreCase(getTypeName()))
                || (copy.getColumnSize() != getColumnSize())
                || (copy.getColumnScale() != getColumnScale());
    }

    public boolean isComputedChanged() {

        if (!hasCopy()) {

            return false;
        }

        if (MiscUtils.isNull(copy.getComputedSource()) || MiscUtils.isNull(getComputedSource())) {
            return false;
        }
        return (!(copy.getComputedSource().equalsIgnoreCase(getComputedSource())) && !copy.getComputedSource().isEmpty());
    }

    public boolean isDescriptionChanged() {

        if (!hasCopy()) {

            return false;
        }

        if (MiscUtils.isNull(copy.getColumnDescription())) {
            return !MiscUtils.isNull(getColumnDescription());
        } else {
            if (MiscUtils.isNull(getColumnDescription()))
                return true;
        }
        return !copy.getColumnDescription().equalsIgnoreCase(getColumnDescription());
    }

    public boolean isDomainChanged() {

        if (!hasCopy()) {

            return false;
        }

        return !copy.getDomain().equalsIgnoreCase(getDomain());
    }

    private boolean hasCopy() {

        return (copy != null);
    }

    /**
     * Determines whether there has been a change in the default value.
     */
    public boolean isDefaultValueChanged() {

        if (!hasCopy()) {

            return false;
        }

        if (MiscUtils.isNull(copy.getDefaultValue())) {
            return !MiscUtils.isNull(getDefaultValue());
        } else {
            if (MiscUtils.isNull(getDefaultValue()))
                return true;
        }
        return !copy.getDefaultValue().equalsIgnoreCase(getDefaultValue());
    }

    /**
     * Indicates whether the specified constraint belongs
     * to this table column.
     *
     * @param constraint the constraint to search for
     * @return true | false
     */
    public boolean containsConstraint(ColumnConstraint constraint) {

        if (constraints == null) {

            return false;
        }

        return constraints.contains(constraint);
    }

    /**
     * Removes the specified constraint from this table column.
     *
     * @param constraint the constraint to remove
     */
    public void removeConstraint(ColumnConstraint constraint) {

        if (constraints != null) {

            if (constraint.isNewConstraint()) {

                constraints.remove(constraint);

            } else {

                ((TableColumnConstraint) constraint).setMarkedDeleted(true);
            }

            resetKeyType();
            for (ColumnConstraint _constraint : constraints) {

                setKeyType(_constraint);
            }

        }

    }

    /**
     * Adds the specified constraint to this column.
     *
     * @param constraint the constraint to add
     */
    public void addConstraint(ColumnConstraint constraint) {

        if (constraints == null) {

            constraints = new ArrayList<ColumnConstraint>();
        }

        // make sure the column has been added
        if (constraint.getColumn() == null) {

            constraint.setColumn(this);
        }

        constraints.add(constraint);
        setKeyType(constraint);
    }

    private void resetKeyType() {

        setForeignKey(false);
        setPrimaryKey(false);
        setUnique(false);
    }

    private void setKeyType(ColumnConstraint constraint) {

        if (constraint.isForeignKey()) {

            setForeignKey(true);
        }

        if (constraint.isPrimaryKey()) {

            setPrimaryKey(true);
        }

        if (constraint.isUniqueKey()) {

            setUnique(true);
        }
    }

    /**
     * Returns whether this column is a referenced key
     * primary or foreign.
     *
     * @return true | false
     */
    public boolean isKey() {

        return isPrimaryKey() || isForeignKey();
    }

    /**
     * Returns whether this is a new table column that does not physically
     * exist in the database yet.
     *
     * @return true | false
     */
    public boolean isNewColumn() {

        return newColumn;
    }

    /**
     * Sets the new column flag as specified.
     *
     * @param newColumn true | false
     */
    public void setNewColumn(boolean newColumn) {

        this.newColumn = newColumn;
    }

    /**
     * Returns whether this column has been marked
     * for deletion.
     *
     * @return true | false
     */
    public boolean isMarkedDeleted() {

        return markedDeleted;
    }

    /**
     * Sets the mark deleted flag as specified.
     *
     * @param markedDeleted true | false
     */
    public void setMarkedDeleted(boolean markedDeleted) {

        this.markedDeleted = markedDeleted;
    }

    /**
     * Returns the parent named object of this object.
     *
     * @return the parent object
     */
    public NamedObject getParent() {

        return getTable();
    }

    public String getParentsName() {

        return getParent() != null ? getParent().getName() : "";
    }

    /**
     * Returns the database table object this
     * column belongs to.
     *
     * @return this column's table
     */
    public DatabaseTable getTable() {

        return table;
    }

    /**
     * Sets this column's table to that specified.
     *
     * @param table the table
     */
    public void setTable(DatabaseTable table) {

        this.table = table;
    }

    /**
     * Returns the catalog name parent to this column.
     *
     * @return the catalog name
     */
    public String getCatalogName() {

        if (table != null) {

            return table.getCatalogName();
        }

        return null;
    }

    /**
     * Returns the schema name parent to this database column.
     *
     * @return the schema name
     */
    public String getSchemaName() {

        if (table != null) {

            return table.getSchemaName();
        }

        return null;
    }

    /**
     * Override to clear the copy.
     */
    public void reset() {

        super.reset();
        copy = null;
    }

    /**
     * Reverts any changes made to this column and
     * associated constraints.
     */
    public void revert() {

        if (hasChanges()) {

            // check the constraints first
            List<ColumnConstraint> _constraints = getConstraints();
            if (_constraints != null) {

                for (int i = 0; i < _constraints.size(); i++) {

                    ColumnConstraint constraint = _constraints.get(i);
                    if (constraint.isNewConstraint()) {

                        removeConstraint(constraint);
                        i--;

                    } else {

                        ((TableColumnConstraint) constraint).revert();
                    }

                }
            }

            // revert to the copy
            if (copy != null) {

                copyColumn(copy, this);
                copy = null;
            }

        }
    }

    /**
     * Makes a copy of itself. A copy of this object may
     * not always be required and may be made available only
     * when deemed necessary - ie. table meta changes.
     */
    public void makeCopy() {

        if (!hasCopy()) {

            copy = new DatabaseTableColumn(getTable());
            copyColumn(this, copy);
        }

    }

    protected final String getNameForQuery() {

        String name = getName();

        if (name.contains(" ")) { // eg. access db allows this

            return "\"" + name + "\"";
        }

        return getNameEscaped();
    }

    /**
     * Copies the specified source object to the specified
     * destination object of the same type.
     *
     * @param source      the source column object
     * @param destination the destination column
     */
    protected void copyColumn(DatabaseTableColumn source,
                              DatabaseTableColumn destination) {

        destination.setCatalogName(source.getCatalogName());
        destination.setSchemaName(source.getSchemaName());
        destination.setName(source.getName());
        destination.setTypeInt(source.getTypeInt());
        destination.setTypeName(source.getTypeName());
        destination.setColumnSize(source.getColumnSize());
        destination.setColumnScale(source.getColumnScale());
        destination.setRequired(source.isRequired());
        destination.setRemarks(source.getRemarks());
        destination.setDefaultValue(source.getDefaultValue());
        destination.setPrimaryKey(source.isPrimaryKey());
        destination.setForeignKey(source.isForeignKey());
        destination.setNewColumn(source.isNewColumn());
        destination.setMarkedDeleted(source.isMarkedDeleted());
        destination.setGenerated(source.isGenerated());
        destination.setComputedSource(source.getComputedSource());
        destination.setColumnDescription(source.getColumnDescription());
        destination.setDomain(source.getDomain());
    }

    /**
     * Returns the display name of this object.
     *
     * @return the display name
     */
    public String getShortName() {

        return getName();
    }

    public DatabaseColumn getOriginalColumn() {

        return copy;
    }

    public boolean hasConstraints() {

        return super.hasConstraints() &&
                (constraints != null && !constraints.isEmpty());
    }

    static final long serialVersionUID = -8548400438832796686L;

}



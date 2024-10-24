/*
 * ColumnConstraint.java
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
import org.underworldlabs.jdbc.DataSourceException;

import java.util.List;

/**
 * @author Takis Diakoumis
 */
public interface ColumnConstraint extends DatabaseObjectElement {

    String PRIMARY = "PRIMARY";
    String FOREIGN = "FOREIGN";
    String UNIQUE = "UNIQUE";
    String CHECK = "CHECK";

    /**
     * Returns the column object referenced by this column or
     * null if it's not a foreign key column.
     *
     * @return the referenced column
     */
    DatabaseColumn getForeignKeyReference();

    /**
     * Returns whether this is a foreign key constraint.
     *
     * @return true | false
     */
    boolean isForeignKey();

    /**
     * Returns whether this is a primary key constraint.
     *
     * @return true | false
     */
    boolean isPrimaryKey();

    /**
     * Returns whether this is a unique key constraint.
     *
     * @return true | false
     */
    boolean isUniqueKey();

    boolean isCheck();

    /**
     * Returns whether this is a new constraint.
     *
     * @return true | false
     */
    boolean isNewConstraint();

    /**
     * Returns the string representation of this constraints
     * type - i.e. PRIMARY, FOREIGN, UNIQUE.
     *
     * @return the type name
     */
    String getTypeName();

    /**
     * Returns the constraint type identifier.
     *
     * @return the type int
     */
    int getKeyType();

    /**
     * Returns the table associated with this constraint.
     *
     * @return the table
     */
    DatabaseTableObject getTable();

    void setTable(DatabaseTable table);

    /**
     * Returns the table name associated with this constraint.
     *
     * @return the table name
     */
    String getTableName();

    /**
     * Returns the column name associated with this constraint.
     *
     * @return the column name
     */
    String getColumnName();

    List<String> getColumnDisplayList();

    List<String> getReferenceColumnDisplayList();

    /**
     * Returns the table column parent to this object.
     *
     * @return the table column
     */
    DatabaseTableColumn getColumn();

    String getReferencedTable();

    String getReferencedColumn();

    String getCheck();

    void setCheck(String check);

    boolean isMarkedDeleted();

    /**
     * Returns whether this constraint has been modified.
     * A modification exists where this constraint is not new,
     * an internal value has changed, or it has been marked
     * for deletion.
     *
     * @return true | false
     */
    boolean isAltered();

    /**
     * Does nothing.
     */
    int drop() throws DataSourceException;

    /**
     * Returns the parent named object of this object.
     *
     * @return the parent object
     */
    NamedObject getParent();

    void setColumn(DatabaseTableColumn databaseTableColumn);

    void addColumnToDisplayList(DatabaseTableColumn column);

    void addReferenceColumnToDisplayList(String column);

    /**
     * Detaches this constraint from the owner column
     */
    void detachFromColumn();

    String getUpdateRule();

    String getDeleteRule();

    boolean isIndexDesc();

    void setIndexDesc(boolean indexDesc);

    String getIndexName();

    void setIndexName(String indexName);

}

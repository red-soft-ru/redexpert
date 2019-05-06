/*
 * DatabaseTable.java
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

package org.executequery.databaseobjects;

import org.executequery.databaseobjects.impl.ColumnConstraint;
import org.executequery.databaseobjects.impl.DefaultDatabaseIndex;
import org.underworldlabs.jdbc.DataSourceException;

import java.sql.ResultSet;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public interface DatabaseTable extends DatabaseTableObject {

    /**
     * Propagates the call to getColumns().
     */
    List<NamedObject> getObjects() throws DataSourceException;

    List<DatabaseColumn> getExportedKeys() throws DataSourceException;

    DatabaseColumn getColumn(String name) throws DataSourceException;

    /**
     * Returns the columns of this table.
     *
     * @return the columns
     */
    List<DatabaseColumn> getColumns() throws DataSourceException;

    /**
     * Returns the columns of this table.
     *
     * @return the columns
     */
    List<ColumnConstraint> getConstraints() throws DataSourceException;

    /**
     * Returns the indexes of this table.
     *
     * @return the indexes
     */
    List<DefaultDatabaseIndex> getIndexes() throws DataSourceException;

    /**
     * Returns this table's column meta data result set.
     *
     * @return the column meta data result set
     */
    ResultSet getColumnMetaData() throws DataSourceException;

    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    int getType();

    /**
     * Returns the meta data key name of this object.
     *
     * @return the meta data key name.
     */
    String getMetaDataKey();

    /**
     * Retrieves the table data row count.
     */
    int getDataRowCount() throws DataSourceException;

    /**
     * Override to clear the columns.
     */
    void reset();

    /**
     * Reverts any changes made to this table and associated elements.
     */
    void revert();

    /**
     * Applies any changes to the database.
     */
    int applyChanges() throws DataSourceException;

    int applyTableDefinitionChanges();

    /**
     * Indicates whether this table or any of its columns
     * or constraints have pending modifications to be applied.
     *
     * @return true | false
     */
    boolean isAltered() throws DataSourceException;

    /**
     * Returns the ALTER TABLE statement to modify this constraint.
     */
    String getAlteredSQLText() throws DataSourceException;

    /**
     * identifier for no constraints in CREATE statement
     */
    int STYLE_NO_CONSTRAINTS = 0;

    /**
     * identifier for embedded constraints in CREATE statement
     */
    int STYLE_CONSTRAINTS_DEFAULT = 1;

    /**
     * identifier for constraints as ALTER TABLE statements
     */
    int STYLE_CONSTRAINTS_ALTER = 2;

    String getCreateSQLText() throws DataSourceException;

    /**
     * Returns the CREATE TABLE statement for this database table.
     * This will be table column (plus data type) definitions only,
     * this does not include constraint meta data.
     */
    String getCreateSQLText(int style) throws DataSourceException;

    /**
     * Returns the user modified SQL text to apply
     * any pending changes. If this has not been set (no
     * changes were made) then a call to getAlteredSQLText()
     * is made.
     *
     * @return the modified SQL
     */
    String getModifiedSQLText() throws DataSourceException;

    void setModifiedSQLText(String modifiedSQLText);

    String getDropSQLText(boolean cascadeConstraints);

    String getInsertSQLText();

    String getUpdateSQLText();

    String getSelectSQLText();

    boolean hasReferenceTo(DatabaseTable anotherTable);

    String getParentNameForStatement();

    DatabaseSource getDatabaseSource();

    List<ColumnConstraint> getPrimaryKeys();

    List<ColumnConstraint> getForeignKeys();

    List<ColumnConstraint> getUniqueKeys();

    String getAlterSQLTextForUniqueKeys();

    String getAlterSQLTextForPrimaryKeys();

    String getAlterSQLTextForForeignKeys();

    int getColumnCount() throws DataSourceException;

    boolean hasPrimaryKey();

    String prepareStatementWithPK(List<String> columns);

    String prepareStatementDeletingWithPK();

    List<String> getPrimaryKeyColumnNames();

    boolean hasForeignKey();

    List<String> getForeignKeyColumnNames();

    void cancelChanges();

    boolean hasTableDefinitionChanges();

    void resetRowsCount();
}





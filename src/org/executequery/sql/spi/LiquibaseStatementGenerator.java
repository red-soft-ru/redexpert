/*
 * LiquibaseStatementGenerator.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

package org.executequery.sql.spi;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import liquibase.CatalogAndSchema;
import liquibase.change.AddColumnConfig;
import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.AddDefaultValueChange;
import liquibase.change.core.AddForeignKeyConstraintChange;
import liquibase.change.core.AddNotNullConstraintChange;
import liquibase.change.core.AddPrimaryKeyChange;
import liquibase.change.core.AddUniqueConstraintChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropColumnChange;
import liquibase.change.core.DropForeignKeyConstraintChange;
import liquibase.change.core.DropNotNullConstraintChange;
import liquibase.change.core.DropPrimaryKeyChange;
import liquibase.change.core.DropTableChange;
import liquibase.change.core.DropUniqueConstraintChange;
import liquibase.change.core.ModifyDataTypeChange;
import liquibase.change.core.RenameColumnChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogFactory;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.structure.core.ForeignKeyConstraintType;

import org.apache.commons.lang.StringUtils;
import org.executequery.ApplicationException;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.DatabaseView;
import org.executequery.databaseobjects.impl.ColumnConstraint;
import org.executequery.databaseobjects.impl.DatabaseTableColumn;
import org.executequery.log.Log;
import org.executequery.sql.StatementGenerator;

public class LiquibaseStatementGenerator implements StatementGenerator {

    private LiquibaseDatabaseFactory databaseFactory;

    public String columnNameValueEscaped(DatabaseTableColumn tableColumn) {

        Database database = databaseFromName(
                connectionFromObject(tableColumn.getTable()), tableColumn.getTable().getHost().getDatabaseProductName());

        return database.escapeColumnName(tableColumn.getCatalogName(), tableColumn.getSchemaName(),
                tableColumn.getTable().getName(), tableColumn.getName());
    }

    public String dropTable(String databaseName, DatabaseTable table) {

        return dropTable(databaseName, table, false);
    }

    public String dropTableCascade(String databaseName, DatabaseTable table) {

        return dropTable(databaseName, table, true);
    }

    public String alterTable(String databaseName, DatabaseTable table) {

        StringBuilder sb = new StringBuilder();
        Database database = databaseFromName(connectionFromObject(table), databaseName);
        for (DatabaseColumn column : table.getColumns()) {

            sb.append(alterColumn(column, database));
        }

        for (ColumnConstraint constraint : table.getConstraints()) {

            if (constraint.isMarkedDeleted()) {

                sb.append(dropConstraint(constraint, database));

            } else if (constraint.isNewConstraint()) {

                sb.append(addConstraint(constraint, database));
            }

        }

        return sb.toString();
    }

    public String tableConstraintsAsAlter(String databaseName, DatabaseTable table) {

        StringBuilder sb = new StringBuilder();

        Database database = databaseFromName(connectionFromObject(table), databaseName);
        sb.append(addPrimaryKeys(primaryKeysForTable(table), database));
        for (ColumnConstraint constraint : table.getConstraints()) {

            if (!constraint.isPrimaryKey()) {

                sb.append(addConstraint(constraint, database));
            }
        }

        return sb.toString();
    }

    public String createUniqueKeyChange(String databaseName, DatabaseTable table) {

        StringBuilder sb = new StringBuilder();
        List<ColumnConstraint> uniqueKeys = table.getUniqueKeys();

        Database database = databaseFromName(connectionFromObject(table), databaseName);
        for (ColumnConstraint constraint : uniqueKeys) {

            AddUniqueConstraintChange change = new AddUniqueConstraintChange();
            change.setTableName(constraint.getTableName());
            change.setColumnNames(constraint.getColumnName());
            change.setConstraintName(constraint.getName());

            sb.append(generateStatements(change, database));
        }

        return sb.toString();
    }

    public String createForeignKeyChange(String databaseName, DatabaseTable table) {

        StringBuilder sb = new StringBuilder();
        List<ColumnConstraint> foreignKeys = table.getForeignKeys();

        Database database = databaseFromName(connectionFromObject(table), databaseName);
        for (ColumnConstraint constraint : foreignKeys) {

            AddForeignKeyConstraintChange change = new AddForeignKeyConstraintChange();
            change.setBaseTableName(constraint.getTableName());
            change.setBaseColumnNames(constraint.getColumnName());
            change.setConstraintName(constraint.getName());
            change.setReferencedTableName(constraint.getReferencedTable());
            change.setReferencedColumnNames(constraint.getReferencedColumn());

            sb.append(generateStatements(change, database));
        }

        return sb.toString();
    }

    public String createPrimaryKeyChange(String databaseName, DatabaseTable table) {

        List<ColumnConstraint> primaryKeys = table.getPrimaryKeys();

        if (primaryKeys.isEmpty()) {

            return "";
        }

        AddPrimaryKeyChange primaryKeyChange = new AddPrimaryKeyChange();
        primaryKeyChange.setTableName(table.getName());

        StringBuilder sb = new StringBuilder();
        for (int i = 0, n = primaryKeys.size(); i < n; i++) {

            ColumnConstraint columnConstraint = primaryKeys.get(i);
            sb.append(columnConstraint.getColumnName());

            if (i < (n - 1)) {

                sb.append(",");
            }

            primaryKeyChange.setConstraintName(columnConstraint.getName());
        }

        primaryKeyChange.setColumnNames(sb.toString());
        Database database = databaseFromName(connectionFromObject(table), databaseName);

        return generateStatements(primaryKeyChange, database);
    }

    public String createTableWithConstraints(String databaseName, DatabaseTable table) {

        Database database = databaseFromName(connectionFromObject(table), databaseName);
        CreateTableChange tableChange = createTableChange(table, database);

        List<DatabaseColumn> columns = table.getColumns();
        for (DatabaseColumn column : columns) {

            if (column.hasConstraints()) {

                ColumnConfig columnConfig = columnConfigForColumn(tableChange, column);
                ConstraintsConfig constraintConfig = new ConstraintsConfig();
                for (ColumnConstraint constraint : column.getConstraints()) {

                    if (constraint.isPrimaryKey()) {

                        constraintConfig.setPrimaryKey(Boolean.TRUE);
                    }

                    if (constraint.isForeignKey()) {

                        constraintConfig.setForeignKeyName(constraint.getName());

                        constraintConfig.setReferences(
                                constraint.getReferencedTable()
                                + "(" +
                                constraint.getReferencedColumn()
                                + ")");
                        
                        constraintConfig.setDeleteCascade(constraint.getDeleteRule() == DatabaseMetaData.importedKeyCascade);
                        constraintConfig.setInitiallyDeferred(constraint.getDeferrability() == DatabaseMetaData.importedKeyInitiallyDeferred);
                    }

                    if (constraint.isUniqueKey()) {

                        constraintConfig.setUnique(Boolean.TRUE);
                        constraintConfig.setUniqueConstraintName(constraint.getName());
                    }

                }

                columnConfig.setConstraints(constraintConfig);
            }

        }

        return generateStatements(tableChange, database);
    }

    public String columnDescription(DatabaseTableColumn tableColumn) {
        
        DatabaseTable table = (DatabaseTable) tableColumn.getParent();
        Database database = databaseFromName(connectionFromObject(table), table.getHost().getDatabaseProductName());

        ColumnConfig columnConfig = createColumn(tableColumn, database);
        
        StringBuilder sb = new StringBuilder();
        sb.append(tableColumn.getName());
//        sb.append(" [ ").append(database.getColumnType(columnConfig.getType(), false)).append(" ]");
        sb.append(" [ ").append(columnConfig.getType()).append(" ]");

        return sb.toString();
    }
    
    public String viewDefinition(String databaseName, DatabaseView view) {
        
        try {

            Database database = databaseFromName(connectionFromObject(view), databaseName);
//            return database.getViewDefinition(view.getNamePrefix(), view.getName());

            return database.getViewDefinition(
                    new CatalogAndSchema(view.getCatalogName(), view.getSchemaName()), view.getName());

        } catch (DatabaseException e) {

            return "";
        }
    }
    
    public String createTable(String databaseName, DatabaseTable table) {

        Database database = databaseFromName(connectionFromObject(table), databaseName);        
        CreateTableChange tableChange = createTableChange(table, database);

        return generateStatements(tableChange, database);
    }

    private String dropTable(String databaseName,
            DatabaseTable table, boolean cascadeConstraints) {

        DropTableChange tableChange = dropTableChange(table);
        tableChange.setCascadeConstraints(Boolean.valueOf(cascadeConstraints));

        Database database = databaseFromName(connectionFromObject(table), databaseName);

        return generateStatements(tableChange, database).trim();
    }

    private ColumnConfig columnConfigForColumn(
            CreateTableChange tableChange, DatabaseColumn column) {

        String name = column.getName();
        List<ColumnConfig> columns = tableChange.getColumns();
        for (ColumnConfig columnConfig : columns) {

            if (columnConfig.getName().equalsIgnoreCase(name)) {

                return columnConfig;
            }

        }

        return null;
    }

    private Connection connectionFromObject(DatabaseObject databaseObject) {

        return databaseObject.getHost().getConnection();
    }

    private DropTableChange dropTableChange(DatabaseTable table) {

        DropTableChange tableChange = new DropTableChange();
        tableChange.setTableName(table.getName());

        return tableChange;
    }

    private CreateTableChange createTableChange(DatabaseTable table, Database database) {

        CreateTableChange tableChange = new CreateTableChange();
        //tableChange.setSchemaName(table.getSchemaName());
        tableChange.setTableName(table.getName());

        for (DatabaseColumn column : table.getColumns()) {

            tableChange.addColumn(createColumn(column, database));
        }

        return tableChange;
    }

    private String addConstraint(ColumnConstraint constraint, Database database) {

        StringBuilder sb = new StringBuilder();
        if (constraint.isPrimaryKey()) {

            sb.append(addPrimaryKey(constraint, database));
        }

        if (constraint.isForeignKey()) {

            sb.append(addForeignKey(constraint, database));
        }

        if (constraint.isUniqueKey()) {

            sb.append(addUniqueKey(constraint, database));
        }

        return sb.toString();
    }

    private String addUniqueKey(ColumnConstraint constraint, Database database) {

        AddUniqueConstraintChange change = new AddUniqueConstraintChange();
        change.setTableName(constraint.getTableName());
        change.setColumnNames(constraint.getColumnName());
        change.setConstraintName(constraint.getName());

        return generateStatements(change, database);
    }

    private String addForeignKey(ColumnConstraint constraint, Database database) {

        AddForeignKeyConstraintChange change = new AddForeignKeyConstraintChange();
        change.setBaseTableName(constraint.getTableName());
        change.setBaseColumnNames(constraint.getColumnName());
        change.setConstraintName(constraint.getName());
        change.setReferencedTableName(constraint.getReferencedTable());
        change.setReferencedColumnNames(constraint.getReferencedColumn());

        if (constraint.getDeleteRule() != DatabaseMetaData.importedKeyNoAction) {
        
            change.setOnDelete(getForeignKeyConstraintType(constraint.getDeleteRule()));
        }
        
        if (constraint.getUpdateRule() != DatabaseMetaData.importedKeyNoAction) {
         
            change.setOnUpdate(getForeignKeyConstraintType(constraint.getUpdateRule()));
        }
        
        return generateStatements(change, database);
    }

    private ForeignKeyConstraintType getForeignKeyConstraintType(short rule) {
        
        switch (rule) {
        
            case DatabaseMetaData.importedKeyCascade:
                return ForeignKeyConstraintType.importedKeyCascade;
                
            case DatabaseMetaData.importedKeySetNull:
                return ForeignKeyConstraintType.importedKeySetNull;
                
            case DatabaseMetaData.importedKeyRestrict:
                return ForeignKeyConstraintType.importedKeyRestrict;
                
            case DatabaseMetaData.importedKeySetDefault:
                return ForeignKeyConstraintType.importedKeySetDefault;
            
            case DatabaseMetaData.importedKeyNoAction:
            default:
                return ForeignKeyConstraintType.importedKeyNoAction;
        
        }
        
    }
    
    private String addPrimaryKey(ColumnConstraint constraint, Database database) {

        AddPrimaryKeyChange change = new AddPrimaryKeyChange();
        change.setTableName(constraint.getTableName());
        change.setColumnNames(constraint.getColumnName());
        change.setConstraintName(constraint.getName());

        return generateStatements(change, database);
    }

    private String addPrimaryKeys(List<ColumnConstraint> primaryKeys, Database database) {

        if (primaryKeys == null || primaryKeys.isEmpty()) {

            return "";
        }

        if (primaryKeys.size() == 1) {

            return addPrimaryKey(primaryKeys.get(0), database);
        }

        StringBuilder sb = new StringBuilder();

        String tableName = null;
        String constraintName = null;

        for (int i = 0, n = primaryKeys.size(); i < n; i++) {

            ColumnConstraint primaryKey = primaryKeys.get(i);

            if (i > 0) {

                sb.append(",");

            } else {

                tableName = primaryKey.getTableName();
                constraintName = primaryKey.getName();
            }

            sb.append(primaryKey.getColumnName());
        }

        AddPrimaryKeyChange change = new AddPrimaryKeyChange();
        change.setTableName(tableName);
        change.setColumnNames(sb.toString());
        change.setConstraintName(constraintName);

        return generateStatements(change, database);
    }

    private String dropConstraint(ColumnConstraint constraint, Database database) {

        StringBuilder sb = new StringBuilder();
        if (constraint.isPrimaryKey()) {

            sb.append(dropPrimaryKey(constraint, database));
        }

        if (constraint.isForeignKey()) {

            sb.append(dropForeignKey(constraint, database));
        }

        if (constraint.isUniqueKey()) {

            sb.append(dropUniqueKey(constraint, database));
        }

        return sb.toString();
    }

    private String dropUniqueKey(ColumnConstraint constraint, Database database) {

        DropUniqueConstraintChange change = new DropUniqueConstraintChange();
        change.setTableName(constraint.getTableName());
        change.setConstraintName(constraint.getName());

        return generateStatements(change, database);
    }

    private String dropForeignKey(ColumnConstraint constraint, Database database) {

        DropForeignKeyConstraintChange change = new DropForeignKeyConstraintChange();
        change.setBaseTableName(constraint.getTableName());
        change.setConstraintName(constraint.getName());

        return generateStatements(change, database);
    }

    private String dropPrimaryKey(ColumnConstraint constraint, Database database) {

        DropPrimaryKeyChange change = new DropPrimaryKeyChange();
        change.setTableName(constraint.getTableName());
        change.setConstraintName(constraint.getName());

        return generateStatements(change, database);
    }

    private String alterColumn(DatabaseColumn column, Database database) {

        boolean isNewOrDeleted = true;
        StringBuilder sb = new StringBuilder();
        DatabaseTableColumn tableColumn = (DatabaseTableColumn)column;

        if (tableColumn.isNewColumn()) {

            sb.append(addColumnChange(tableColumn, database));

        } else if (tableColumn.isMarkedDeleted()) {

            sb.append(dropColumnChange(tableColumn, database));

        } else {

            isNewOrDeleted = false;
        }

        if (isNewOrDeleted) {

            return sb.toString();
        }

        if (tableColumn.isNameChanged()) {

            sb.append(renameColumnChange(tableColumn, database));
        }

        if (tableColumn.isDataTypeChanged()) {

            sb.append(modifyColumnChange(tableColumn, database));
        }

        if (tableColumn.isRequiredChanged()) {

            if (tableColumn.isRequired()) {
                
                sb.append(addNotNullConstraintChange(tableColumn, database));                
            
            } else {
                
                sb.append(addNullConstraintChange(tableColumn, database));
            }
            
        }

        if (tableColumn.isDefaultValueChanged()) {

            sb.append(addDefaultValueChange(tableColumn, database));
        }

        return sb.toString();
    }

    private List<ColumnConstraint> primaryKeysForTable(DatabaseTable table) {

        List<ColumnConstraint> primaryKeys = new ArrayList<ColumnConstraint>();
        for (ColumnConstraint constraint : table.getConstraints()) {

            if (constraint.isPrimaryKey()) {

                primaryKeys.add(constraint);
            }

        }

        return primaryKeys;
    }

    private String addNotNullConstraintChange(DatabaseTableColumn tableColumn, Database database) {

        AddNotNullConstraintChange columnChange = new AddNotNullConstraintChange();

        //columnChange.setSchemaName(tableColumn.getSchemaName());
        columnChange.setTableName(tableColumn.getTable().getName());
        columnChange.setColumnName(tableColumn.getName());
        columnChange.setColumnDataType(tableColumn.getFormattedDataType());

        return generateStatements(columnChange, database);
    }

    private String addNullConstraintChange(DatabaseTableColumn tableColumn, Database database) {
        
        DropNotNullConstraintChange columnChange = new DropNotNullConstraintChange();
        
        //columnChange.setSchemaName(tableColumn.getSchemaName());
        columnChange.setTableName(tableColumn.getTable().getName());
        columnChange.setColumnName(tableColumn.getName());
        columnChange.setColumnDataType(tableColumn.getFormattedDataType());
        
        return generateStatements(columnChange, database);
    }
    
    private String addDefaultValueChange(DatabaseTableColumn tableColumn, Database database) {

        AddDefaultValueChange columnChange = new AddDefaultValueChange();

        //columnChange.setSchemaName(tableColumn.getSchemaName());
        columnChange.setTableName(tableColumn.getTable().getName());
        columnChange.setColumnName(tableColumn.getName());
        columnChange.setDefaultValue(tableColumn.getDefaultValue());

        return generateStatements(columnChange, database);
    }

    private String modifyColumnChange(DatabaseTableColumn tableColumn, Database database) {

        ModifyDataTypeChange columnChange = new ModifyDataTypeChange();

        //columnChange.setSchemaName(tableColumn.getSchemaName());
        columnChange.setTableName(tableColumn.getTable().getName());
        columnChange.setColumnName(tableColumn.getName());
        columnChange.setNewDataType(tableColumn.getFormattedDataType());

        return generateStatements(columnChange, database);
    }

    private String addColumnChange(DatabaseTableColumn tableColumn, Database database) {

        AddColumnChange columnChange = new AddColumnChange();

        //columnChange.setSchemaName(tableColumn.getSchemaName());
        columnChange.setTableName(tableColumn.getTable().getName());
        columnChange.addColumn((AddColumnConfig) createColumn(tableColumn, database));

        return generateStatements(columnChange, database);
    }

    private String dropColumnChange(DatabaseTableColumn tableColumn, Database database) {

        DropColumnChange columnChange = new DropColumnChange();

        //columnChange.setSchemaName(tableColumn.getSchemaName());
        columnChange.setTableName(tableColumn.getTable().getName());
        columnChange.setColumnName(tableColumn.getName());

        return generateStatements(columnChange, database);
    }

    private String renameColumnChange(DatabaseTableColumn tableColumn, Database database) {

        RenameColumnChange columnChange = new RenameColumnChange();

        columnChange.setTableName(tableColumn.getTable().getName());
        columnChange.setNewColumnName(tableColumn.getName());
        columnChange.setOldColumnName(tableColumn.getOriginalColumn().getName());
        columnChange.setColumnDataType(tableColumn.getFormattedDataType());

        return generateStatements(columnChange, database);
    }

    private String generateStatements(Change change, Database database) {

        StringBuilder sb = new StringBuilder();
        SqlStatement[] statements = change.generateStatements(database);
        for (SqlStatement statement : statements) {

            Sql[] generatedSql = SqlGeneratorFactory.getInstance().generateSql(statement, database);
            if (generatedSql != null) { // jt400
                for (Sql sql : generatedSql) {
                    
                    sb.append(sql.toSql());
                    sb.append(sql.getEndDelimiter());
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private ColumnConfig createColumn(DatabaseColumn column, Database database) {

        ColumnConfig columnConfig = new EqColumnConfig(column.getTypeName(), database);

        columnConfig.setName(column.getName() == null ? "" : column.getName());
        columnConfig.setType(column.getFormattedDataType());
        
        if (column.getTypeInt() == Types.BIT) {
            
            if (StringUtils.isNotBlank(column.getDefaultValue())) {
            
//                columnConfig.setDefaultValue(column.getDefaultValue().replaceAll("\\(|\\)", ""));
                columnConfig.setDefaultValueBoolean(column.getDefaultValue().replaceAll("\\(|\\)", ""));
            }

        } else {
            
            columnConfig.setDefaultValue(column.getDefaultValue());
        }
        
        if (column.isRequired()) {

            ConstraintsConfig constraintConfig = new ConstraintsConfig();
            constraintConfig.setNullable(Boolean.FALSE);
            columnConfig.setConstraints(constraintConfig);
        }

        return columnConfig;
    }

    private Database databaseFromName(Connection connection, String databaseName) {

        LogFactory.getInstance().setDefaultLoggingLevel("warning");

        Database database = databaseFactory().createDatabase(databaseName);
        database.setConnection(new JdbcConnection(connection));

        return database;
    }

    private LiquibaseDatabaseFactory databaseFactory() {

        if (databaseFactory == null) {

            databaseFactory = new LiquibaseDatabaseFactory();
        }

        return databaseFactory;
    }

    @SuppressWarnings("unused")
    private void handleAndRethrowException(Throwable e) {

        if (Log.isDebugEnabled()) {

            Log.error("Error generating SQL statement", e);
        }

        throw new ApplicationException(e);
    }

}











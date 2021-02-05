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
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.*;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.tree.TreePanel;
import org.executequery.gui.resultset.RecordDataItem;
import org.executequery.log.Log;
import org.executequery.sql.SQLFormatter;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class DefaultDatabaseTable extends AbstractTableObject implements DatabaseTable {

  /**
   * the table columns
   */
  private List<DatabaseColumn> columns;

  /**
   * the table columns exported
   */
  private List<DatabaseColumn> exportedColumns;

  /**
   * the table indexed columns
   */
  private List<DefaultDatabaseIndex> indexes;

  /**
   * the user modified SQL text for changes
   */
  private String modifiedSQLText;

  private transient TableDataChangeWorker tableDataChangeExecutor;

  /**
   * Creates a new instance of DatabaseTable
   */

  private int typeTree;

  private DatabaseObject dependObject;

  private String externalFile;

    public DefaultDatabaseTable(DatabaseObject object, String metaDataKey) {

      this(object.getHost(), metaDataKey);

      setCatalogName(object.getCatalogName());
      setSchemaName(object.getSchemaName());
      setName(object.getName());
      setRemarks(object.getRemarks());
      if (object instanceof DefaultDatabaseObject) {
        DefaultDatabaseObject ddo = ((DefaultDatabaseObject) object);
        setTypeTree(ddo.getTypeTree());
        setDependObject(ddo.getDependObject());
      } else {
        typeTree = TreePanel.DEFAULT;
        setDependObject(null);
      }
    }

  /**
   * Creates a new instance of DatabaseTable
   */
  public DefaultDatabaseTable(DatabaseHost host) {
    super(host, "TABLE");
  }

    public DefaultDatabaseTable(DatabaseHost host, String metaDataKey) {
        super(host, metaDataKey);
    }

  /**
   * Propagates the call to getColumns().
   */
  public List<NamedObject> getObjects() throws DataSourceException {

    List<DatabaseColumn> _columns = getColumns();
    if (_columns == null) {

      return null;
    }

    List<NamedObject> objects = new ArrayList<NamedObject>(_columns.size());
    for (DatabaseColumn i : _columns) {

      objects.add(i);
    }

    return objects;
  }

  @Override
  public boolean allowsChildren() {
    return true;
  }

  public List<String> getColumnNames() {
    List<String> names = new ArrayList<>();
    for (DatabaseColumn column : getColumns()) {
      names.add(column.getName());
    }
    return names;
  }

  public List<DatabaseColumn> getExportedKeys() throws DataSourceException {

    if (!isMarkedForReload() && exportedColumns != null) {

      return exportedColumns;
    }

    if (exportedColumns != null) {

      exportedColumns.clear();
      exportedColumns = null;
    }

    DatabaseHost host = getHost();
    if (host != null) {

      exportedColumns = host.getExportedKeys(getCatalogName(),
          getSchemaName(),
          getName());
    }

    return exportedColumns;
  }

  public boolean hasReferenceTo(DatabaseTable anotherTable) {



    List<ColumnConstraint> constraints = getConstraints();

    String anotherTableName = anotherTable.getName();

    for (ColumnConstraint constraint : constraints) {

      if (constraint.isForeignKey()) {

        if (constraint.getReferencedTable().equals(anotherTableName)) {

          return true;
        }

      }

    }

    return false;

  }

  /**
   * Returns the column count of this table.
   *
   * @return the column count
   */
  public int getColumnCount() throws DataSourceException {

    return getColumns().size();
  }

  /**
   * Returns the columns of this table.
   *
   * @return the columns
   */
  public synchronized List<DatabaseColumn> getColumns() throws DataSourceException {

    if (!isMarkedForReload() && columns != null) {

      return columns;
    }

    // otherwise cleanup existing references
    if (columns != null) {

      columns.clear();
      columns = null;
    }

    DatabaseHost host = getHost();
    if (host != null) {

      ResultSet rs = null;
      try {

        List<DatabaseColumn> _columns = null;
        if (typeTree == TreePanel.DEFAULT)
          _columns = host.getColumns(getCatalogName(),
                  getSchemaName(),
                  getName());
        if (typeTree == TreePanel.DEPENDED_ON)
          _columns = getDependedColumns();
        if (typeTree == TreePanel.DEPENDENT)
          _columns = getDependentColumns();
        if (_columns != null) {

          columns = databaseColumnListWithSize(_columns.size());
          for (DatabaseColumn i : _columns) {

            columns.add(new DatabaseTableColumn(this, i));
          }

          // reload and define the constraints
          String _catalog = host.getCatalogNameForQueries(getCatalogName());
          String _schema = host.getSchemaNameForQueries(getSchemaName());
          DatabaseMetaData dmd = host.getDatabaseMetaData();
          rs = dmd.getPrimaryKeys(_catalog, _schema, getName());
          while (rs.next()) {

            String pkColumn = rs.getString(4);
            for (DatabaseColumn i : columns) {

              if (i.getName().equalsIgnoreCase(pkColumn)) {

                DatabaseTableColumn column = (DatabaseTableColumn) i;
                TableColumnConstraint constraint = new TableColumnConstraint(column, ColumnConstraint.PRIMARY_KEY);

                constraint.setName(rs.getString(6));
                constraint.setMetaData(resultSetRowToMap(rs));
                column.addConstraint(constraint);
                break;

              }
            }
          }
          releaseResources(rs, null);

          try {

            // TODO: XXX

            // sapdb amd maxdb dump on imported/exported keys
            // surround with try/catch hack to get at least a columns list

            rs = dmd.getImportedKeys(_catalog, _schema, getName());

            while (rs.next()) {

              String fkColumn = rs.getString(8);

              for (DatabaseColumn i : columns) {

                if (i.getName().equalsIgnoreCase(fkColumn)) {
                  DefaultStatementExecutor querySender = new DefaultStatementExecutor(getHost().getDatabaseConnection());
                  DatabaseTableColumn column = (DatabaseTableColumn) i;
                  List<String> row = new ArrayList<>();
                  for (int g = 1; g <= rs.getMetaData().getColumnCount(); g++)
                    row.add(rs.getString(g));
                  TableColumnConstraint constraint = new TableColumnConstraint(column, ColumnConstraint.FOREIGN_KEY);
                  constraint.setReferencedCatalog(rs.getString(1));
                  constraint.setReferencedSchema(rs.getString(2));
                  constraint.setReferencedTable(rs.getString(3));
                  constraint.setReferencedColumn(rs.getString(4));
                  constraint.setName(rs.getString(12));
                  constraint.setDeferrability(rs.getShort(14));
                  constraint.setMetaData(resultSetRowToMap(rs));
                  ResultSet rulesRS = querySender.getResultSet("select RDB$REF_CONSTRAINTS.RDB$UPDATE_RULE, RDB$REF_CONSTRAINTS.RDB$DELETE_RULE" +
                          " from rdb$ref_constraints where RDB$REF_CONSTRAINTS.RDB$CONSTRAINT_NAME='" + constraint.getName() + "'").getResultSet();
                  try {
                    if (rulesRS.next()) {
                      for (int g = 1; g <= 2; g++) {
                        String rule = rulesRS.getString(g);
                        if (rule != null) {
                          if (g == 1)
                            constraint.setUpdateRule(rule.trim());
                          else
                            constraint.setDeleteRule(rule.trim());
                        }
                      }
                    }
                  } catch (Exception e) {
                    e.printStackTrace();
                  } finally {
                    querySender.releaseResources();
                  }
                  column.addConstraint(constraint);
                  break;

                }
              }
            }

          } catch (SQLException e) {
            Log.error("Error get imported keys for " + getName() + ": " + e.getMessage());
          }
        }

      } catch (DataSourceException e) {

        // catch and re-throw here to create
        // an empty column list so we don't
        // keep hitting the same error
        columns = databaseColumnListWithSize(0);
        throw e;

      } catch (SQLException e) {

        // catch and re-throw here to create
        // an empty column list so we don't
        // keep hitting the same error
        columns = databaseColumnListWithSize(0);
        throw new DataSourceException(e);

      } finally {

        releaseResources(rs, null);
        setMarkedForReload(false);
      }

    }
    return columns;
  }

  private List<DatabaseColumn> databaseColumnListWithSize(int size) {

    return Collections.synchronizedList(new ArrayList<DatabaseColumn>(size));
  }

  private List<ColumnConstraint> databaseConstraintsListWithSize(int size) {

    return Collections.synchronizedList(new ArrayList<ColumnConstraint>(size));
  }

  private List<DefaultDatabaseIndex> databaseIndexListWithSize(int size) {

    return Collections.synchronizedList(new ArrayList<DefaultDatabaseIndex>(size));
  }

  List<ColumnConstraint> constraints;

  /**
   * Returns the columns of this table.
   *
   * @return the columns
   */
  public List<ColumnConstraint> getConstraints() throws DataSourceException {

    if (constraints == null) {

      if (getColumns() != null) {

        constraints = new ArrayList<ColumnConstraint>();

        for (DatabaseColumn i : columns) {

          DatabaseTableColumn column = (DatabaseTableColumn) i;
          if (column.hasConstraints()) {

            List<ColumnConstraint> columnConstraints = column.getConstraints();
            for (ColumnConstraint j : columnConstraints) {

              constraints.add(j);
            }

          }

        }
        DefaultStatementExecutor executor = new DefaultStatementExecutor(getHost().getDatabaseConnection(), true);
        SqlStatementResult result = null;
        try {
          String query = "select A.RDB$CONSTRAINT_NAME,\n" +
                  "A.RDB$CONSTRAINT_TYPE,\n" +
                  "A.RDB$RELATION_NAME,\n" +
                  "C.RDB$TRIGGER_SOURCE\n" +
                  "from RDB$RELATION_CONSTRAINTS A, RDB$CHECK_CONSTRAINTS B, RDB$TRIGGERS C\n" +
                  "where (A.RDB$CONSTRAINT_TYPE = 'CHECK') and\n" +
                  "(A.RDB$CONSTRAINT_NAME = B.RDB$CONSTRAINT_NAME) and\n" +
                  "(B.RDB$TRIGGER_NAME = C.RDB$TRIGGER_NAME) and\n" +
                  "(C.RDB$TRIGGER_TYPE = 1)\n" +
                  "and (A.RDB$RELATION_NAME = ?)";
          PreparedStatement st = executor.getPreparedStatement(query);
          st.setString(1, getName());
          result = executor.execute(QueryTypes.SELECT, st);
          ResultSet rs = result.getResultSet();
          List<String> names = new ArrayList<>();
          if (rs != null) {
            while (rs.next()) {
              String name = rs.getString(1).trim();
              if (!names.contains(name)) {
                ColumnConstraint constraint = new TableColumnConstraint(rs.getString(4));
                constraint.setName(name);
                constraint.setTable(this);
                constraints.add(constraint);
                names.add(name);
              }
            }
          }
        } catch (Exception e) {
          Log.error("Error loading check-constraints:" + result.getErrorMessage(), e);
        } finally {
          executor.releaseResources();
        }
        result = null;
        try {
          String query = "SELECT C.RDB$CONSTRAINT_NAME,I.RDB$FIELD_NAME\n" +
                  "FROM RDB$RELATION_CONSTRAINTS AS C LEFT JOIN RDB$INDEX_SEGMENTS AS I\n" +
                  "ON C.RDB$INDEX_NAME=I.RDB$INDEX_NAME\n" +
                  "where C.RDB$RELATION_NAME=? AND C.RDB$CONSTRAINT_TYPE = 'UNIQUE'";
          PreparedStatement st = executor.getPreparedStatement(query);
          st.setString(1, getName());
          ResultSet rs = executor.getResultSet(-1, st).getResultSet();
          if (rs != null) {
            while (rs.next()) {
              String name = rs.getString(1).trim();
              ColumnConstraint constraint = new TableColumnConstraint(UNIQUE_KEY);
              constraint.setName(name);
              String columnName = rs.getString("RDB$FIELD_NAME").trim();
              for (DatabaseColumn i : columns) {
                if (i.getName().trim().contentEquals(columnName))
                  constraint.setColumn((DatabaseTableColumn) i);
              }
              constraints.add(constraint);
            }
          }
        } catch (Exception e) {
          Log.error("Error loading unique-constraints:" + result.getErrorMessage(), e);
        } finally {
          executor.releaseResources();
        }
        constraints.removeAll(Collections.singleton(null));
        constraints.sort(new Comparator<ColumnConstraint>() {
          @Override
          public int compare(ColumnConstraint o1, ColumnConstraint o2) {
            return o1.getName().compareTo(o2.getName());
          }
        });
        return constraints;

      } else {

        return databaseConstraintsListWithSize(0);
      }
    } else return constraints;
  }

  /**
   * Returns the indexes of this table.
   *
   * @return the indexes
   */
  public List<DefaultDatabaseIndex> getIndexes() throws DataSourceException {

    if (!isMarkedForReload() && indexes != null) {

      return indexes;
    }

    ResultSet rs = null;
    try {

      DatabaseHost _host = getHost();
      rs = _host.getDatabaseMetaData().getIndexInfo(getCatalogName(), getSchemaName(), getName(), false, true);
      TableColumnIndex lastIndex = null;
      indexes = new ArrayList<>();
      List<TableColumnIndex> tindexes = new ArrayList<>();
      while (rs.next()) {
        String name = rs.getString(6);
        if (StringUtils.isBlank(name)) {

          continue;
        }
        if (lastIndex == null || !lastIndex.getName().equals(name)) {
          TableColumnIndex index = new TableColumnIndex(name);
          index.setNonUnique(rs.getBoolean(4));
          index.addIndexedColumn(rs.getString(9));
          index.setMetaData(resultSetRowToMap(rs));
          lastIndex = index;
          tindexes.add(index);
        } else {
          lastIndex.addIndexedColumn(rs.getString(9));
        }
      }
      releaseResources(rs, null);
      DefaultDatabaseMetaTag metaTag = new DefaultDatabaseMetaTag(getHost(),null,null,META_TYPES[INDEX]);
      for (TableColumnIndex index : tindexes)
      {
        DefaultDatabaseIndex index1 = metaTag.getIndexFromName(index.getName());
        index1.loadColumns();
        indexes.add(index1);
        if(index1.getExpression()!=null)
        {
          index.setIndexedColumns(null);
          index.setExpression(index1.getExpression());
        }
        index.setConstraint_type(index1.getConstraint_type());
      }

      return indexes;

    } catch (DataSourceException e) {

      // catch and re-throw here to create
      // an empty index list so we don't
      // keep hitting the same error
      indexes = databaseIndexListWithSize(0);
      throw e;

    } catch (SQLException e) {

      // catch and re-throw here to create
      // an empty index list so we don't
      // keep hitting the same error
      indexes = databaseIndexListWithSize(0);
      throw new DataSourceException(e);

    } finally {

      releaseResources(rs, null);
      setMarkedForReload(false);
    }
  }

  /**
   * Returns this table's column meta data result set.
   *
   * @return the column meta data result set
   */
  public ResultSet getColumnMetaData() throws DataSourceException {

    return getMetaData();
  }

  /**
   * Returns the database object type.
   *
   * @return the object type
   */
  public int getType() {
    if (isSystem()) {
      return SYSTEM_TABLE;
    } else return TABLE;
  }

  /**
   * Returns the meta data key name of this object.
   *
   * @return the meta data key name.
   */
  public String getMetaDataKey() {
    return META_TYPES[getType()];
  }

  /**
   * Override to clear the columns.
   */
  public void reset() {
    super.reset();
    modifiedSQLText = null;
    clearColumns();
    clearIndexes();
    clearDataChanges();
    clearConstraints();
  }

  public void clearDefinitionChanges() {
    modifiedSQLText = null;
    clearColumns();
    clearIndexes();
  }


  private void clearColumns() {
    if (columns != null) {
      columns.clear();
    }
    columns = null;
  }

  private void clearConstraints() {
    if (constraints != null) {
      constraints.clear();
    }
    constraints = null;
  }

  private void clearIndexes() {
    if (indexes != null) {
      indexes.clear();
    }
    indexes = null;
  }

  /**
   * Reverts any changes made to this table and associated elements.
   */
  public void revert() {

    List<DatabaseColumn> newColumns = new ArrayList<DatabaseColumn>();
    for (DatabaseColumn i : columns) {

      DatabaseTableColumn column = (DatabaseTableColumn) i;

      if (!column.isNewColumn()) {

        column.revert();

      } else {

        newColumns.add(column);
      }

    }

    for (DatabaseColumn column : newColumns) {

      columns.remove(column);
    }

    newColumns.clear();
    tableDataChanges().clear();
    modifiedSQLText = null;
  }

  /**
   * Applies any changes to the database.
   */
  public int applyChanges() throws DataSourceException {

    int result = applyTableDefinitionChanges();
    result += applyTableDataChanges();

    return result;
  }

  public void cancelChanges() {

    if (tableDataChangeExecutor != null) {

      tableDataChangeExecutor.cancel();
    }
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

      for (int i = 0; i < queries.length; i++) {

        String query = queries[i].trim();
        if (StringUtils.isNotBlank(query)) {

          result += stmnt.executeUpdate(query);
        }

      }

      if (!connection.getAutoCommit()) {

        connection.commit();
      }

      // set to reset for the next call
      reset();

      return result;

    } catch (SQLException e) {

      throw new DataSourceException(e);

    } finally {

      releaseResources(stmnt);
    }
  }

  public boolean hasTableDefinitionChanges() {

    return StringUtils.isNotBlank(getModifiedSQLText());
  }

  private String adapter;

  public DefaultDatabaseTable(DatabaseObject object) {

    this(object.getHost());

    setCatalogName(object.getCatalogName());
    setSchemaName(object.getSchemaName());
    setName(object.getName());
    setRemarks(object.getRemarks());
    if (object instanceof DefaultDatabaseObject) {
      DefaultDatabaseObject ddo = ((DefaultDatabaseObject) object);
      setTypeTree(ddo.getTypeTree());
      setDependObject(ddo.getDependObject());
    } else {
      typeTree = TreePanel.DEFAULT;
      setDependObject(null);
    }
  }

  private boolean loadedInfoAboutExternalFile = false;

  private void loadInfoAboutExternalFile() {
    DefaultStatementExecutor querySender = new DefaultStatementExecutor(getHost().getDatabaseConnection());
    try {
      //querySender.setDatabaseConnection(getHost().getDatabaseConnection());
      String adapter = ", RDB$ADAPTER";
      if (!getHost().getDatabaseProductName().toLowerCase().contains("reddatabase"))
        adapter = "";
      PreparedStatement statement = querySender.getPreparedStatement("select rdb$external_file" + adapter + " from rdb$relations where rdb$relation_name = ?");
      statement.setString(1, getName());
      ResultSet rs = querySender.getResultSet(-1, statement).getResultSet();
      if (rs.next()) {
        setExternalFile(rs.getString(1));
        if (!adapter.isEmpty())
          setAdapter(rs.getString(2));
      }
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    } finally {
      querySender.releaseResources();
      loadedInfoAboutExternalFile = true;
    }
  }

  @Override
  public String getExternalFile() {
    if (!loadedInfoAboutExternalFile)
      loadInfoAboutExternalFile();
    return externalFile;
  }

  public void setExternalFile(String externalFile) {
    this.externalFile = externalFile;
  }

  @Override
  public String getAdapter() {
    if (!loadedInfoAboutExternalFile)
      loadInfoAboutExternalFile();
    return adapter;
  }

  public void setAdapter(String adapter) {
    this.adapter = adapter;
  }

  /**
   * Indicates whether this table or any of its columns
   * or constraints have pending modifications to be applied.
   *
   * @return true | false
   */
  public boolean isAltered() throws DataSourceException {

    if (hasTableDataChanges()) {

      return true;
    }

    List<DatabaseColumn> _columns = getColumns();
    if (_columns != null) {

      for (DatabaseColumn i : _columns) {

        DatabaseTableColumn column = (DatabaseTableColumn) i;

        if (column.hasChanges()) {

          return true;
        }

      }
    }

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

  /**
   * Returns the ALTER TABLE statement to modify this constraint.
   */
  public String getAlteredSQLText() throws DataSourceException {

    StringBuilder sb = new StringBuilder();
    List<DatabaseColumn> _columns = getColumns();
    List<ColumnConstraint> _constraints = getConstraints();
    boolean first=true;
    sb.append("ALTER TABLE ").append(MiscUtils.getFormattedObject(getName()));
    if (_constraints != null) {
      for(int i=0;i<_constraints.size();i++) {
        if(_constraints.get(i) instanceof TableColumnConstraint)
        {
          TableColumnConstraint dtc=(TableColumnConstraint) _constraints.get(i);
          if(dtc.isMarkedDeleted()) {
            if(!first)
              sb.append(",");
            first=false;
            sb.append("\nDROP CONSTRAINT ").append(MiscUtils.getFormattedObject(dtc.getName()));
          }
        }
      }
    }
    if (_columns != null) {
      for(int i=0;i<_columns.size();i++) {
        if(_columns.get(i) instanceof DatabaseTableColumn)
        {
          DatabaseTableColumn dtc=(DatabaseTableColumn)_columns.get(i);
          if(dtc.isMarkedDeleted()) {
            if(!first)
              sb.append(",");
            first=false;
            sb.append("\nDROP ").append(dtc.getNameEscaped());
          }
        }
      }
    }
    if(first)
      return "";
    return sb.toString();
  }

  public String getCreateSQLText() throws DataSourceException {

    return getCreateSQLText(STYLE_CONSTRAINTS_ALTER);
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
  return  null;

  }

  public boolean hasForeignKey() {

    List<ColumnConstraint> keys = getForeignKeys();
    return keys != null && !keys.isEmpty();
  }

  public boolean hasPrimaryKey() {

    List<ColumnConstraint> keys = getPrimaryKeys();
    return keys != null && !keys.isEmpty();
  }

  public List<ColumnConstraint> getPrimaryKeys() {

    List<ColumnConstraint> primaryKeys = new ArrayList<ColumnConstraint>();
    List<ColumnConstraint> _constraints = getConstraints();
    for (int i = 0, n = _constraints.size(); i < n; i++) {

      ColumnConstraint columnConstraint = _constraints.get(i);
      if (columnConstraint.isPrimaryKey()) {

        primaryKeys.add(columnConstraint);
      }

    }

    return primaryKeys;
  }

  public List<ColumnConstraint> getForeignKeys() {

    List<ColumnConstraint> foreignKeys = new ArrayList<ColumnConstraint>();
    List<ColumnConstraint> _constraints = getConstraints();
    for (int i = 0, n = _constraints.size(); i < n; i++) {

      ColumnConstraint columnConstraint = _constraints.get(i);
      if (columnConstraint.isForeignKey()) {

        foreignKeys.add(columnConstraint);
      }

    }

    return foreignKeys;
  }

  public List<ColumnConstraint> getUniqueKeys() {

    List<ColumnConstraint> uniqueKeys = new ArrayList<ColumnConstraint>();
    List<ColumnConstraint> _constraints = getConstraints();

    for (int i = 0, n = _constraints.size(); i < n; i++) {

      ColumnConstraint columnConstraint = _constraints.get(i);
      if (columnConstraint.isUniqueKey()) {

        uniqueKeys.add(columnConstraint);
      }

    }

    return uniqueKeys;
  }

  public String getAlterSQLTextForUniqueKeys() {

    /*StatementGenerator statementGenerator = null;

    return statementGenerator.createUniqueKeyChange(databaseProductName(), this);*/
    return null;
  }

  public String getAlterSQLTextForForeignKeys() {

    /*StatementGenerator statementGenerator = null;
    return statementGenerator.createForeignKeyChange(databaseProductName(), this);*/
    return null;
  }

  public String getAlterSQLTextForPrimaryKeys() {

    /*StatementGenerator statementGenerator = null;

    return statementGenerator.createPrimaryKeyChange(databaseProductName(), this);
     */
    return  null;
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
   * this does not include constraint meta data.
   */
  public String getCreateSQLText(int style) throws DataSourceException {

    return formatSqlText(generateCreateTableSQLText().replaceAll("\\^",";"));

  }

  private String generateCreateTableSQLText() {
    List<ColumnData> listCD=new ArrayList<>();
    for(int i=0;i<getColumnCount();i++)
    {
      listCD.add(new ColumnData(getHost().getDatabaseConnection(),getColumns().get(i)));
    }
    List<org.executequery.gui.browser.ColumnConstraint> listCC=new ArrayList<>();
    for(int i=0;i<getConstraints().size();i++)
    {
      listCC.add(new org.executequery.gui.browser.ColumnConstraint(false,getConstraints().get(i)));
    }

    return SQLUtils.generateCreateTable(getName(), listCD, listCC, true, false, null, getExternalFile(), getAdapter());

    }

  private String formatSqlText(String text) {

    return new SQLFormatter(text).format();
  }

  /**
   * Returns the CREATE TABLE statement for this database table.
   * This will be table column (plus data type) definitions only,
   * this does not include constraint meta data.
   */
  public String getCreateSQLTextX(int style) throws DataSourceException {

    StringBuilder sb = new StringBuilder();

    sb.append("CREATE TABLE ");
    sb.append(getName());
    sb.append(" (");

    // determine the spaces from the left side to each column name
    String firstIndent = getSpacesForLength(sb.length());

    // determine the spaces from the column name to the data type
    int maxLength = 0;
    for (DatabaseColumn i : columns) {
      DatabaseTableColumn column = (DatabaseTableColumn) i;
      maxLength = Math.max(maxLength, column.getName().length());
    }
    // add another 5 spaces from the max
    maxLength += 5;

    int secondIndentLength = 0;
    for (int i = 0, n = columns.size(); i < n; i++) {
      DatabaseTableColumn column = (DatabaseTableColumn) columns.get(i);

      if (i > 0) {
        sb.append(firstIndent);
      }

      String columnName = column.getName();
      sb.append(columnName.toUpperCase());

      secondIndentLength = maxLength - columnName.length();
      for (int j = 0; j < secondIndentLength; j++) {
        sb.append(" ");
      }

      sb.append(column.getFormattedDataType());

      if (StringUtils.isNotBlank(column.getDefaultValue())) {
        sb.append(" DEFAULT ");
        sb.append(column.getDefaultValue());
      }

      if (column.isRequired()) {
        sb.append(" NOT NULL");
      }

      if (i < (n - 1)) {
        sb.append(",\n");
      }

    }

    if (style == STYLE_CONSTRAINTS_DEFAULT) {
      sb.append(",\n");
      List<ColumnConstraint> constraints = getConstraints();
      for (int i = 0, n = constraints.size(); i < n; i++) {
        TableColumnConstraint constraint =
            (TableColumnConstraint) constraints.get(i);
        sb.append(firstIndent);
        sb.append(constraint.getConstraintSQLText());

        if (i < (n - 1)) {
          sb.append(",\n");
        }

      }
      sb.append(");\n");
    } else if (style == STYLE_CONSTRAINTS_ALTER) {

      sb.append(");\n\n");
      List<ColumnConstraint> constraints = getConstraints();

      for (ColumnConstraint i : constraints) {

        TableColumnConstraint constraint = (TableColumnConstraint) i;

        sb.append(constraint.getCreateSQLText());
        sb.append("\n");
      }

    } else {

      // finish off the statement as is
      sb.append(");\n");
    }

    return sb.toString();
  }

  /**
   * Returns the user modified SQL text to apply
   * any pending changes. If this has not been set (no
   * changes were made) then a call to getAlteredSQLText()
   * is made.
   *
   * @return the modified SQL
   */
  public String getModifiedSQLText() throws DataSourceException {
    if (modifiedSQLText == null) {
      return getAlteredSQLText();
    }
    return modifiedSQLText;
  }

  public void setModifiedSQLText(String modifiedSQLText) {
    this.modifiedSQLText = modifiedSQLText;
  }

  public String getInsertSQLText() {

    try {

      StringBuilder sb = new StringBuilder();
      sb.append("INSERT INTO ");
      sb.append(getNameForQuery());
      sb.append(" (");

      String indent = getSpacesForLength(sb.length());

      List<DatabaseColumn> _columns = getColumns();
      for (int i = 0, n = _columns.size(); i < n; i++) {

        DatabaseTableColumn column = (DatabaseTableColumn) _columns.get(i);
        sb.append(column.getNameForQuery());

        if (i < n - 1) {
          sb.append(",\n");
          sb.append(indent);
        }

      }

      sb.append(")\n");

      String valuesString = "VALUES (";
      sb.append(valuesString);

      indent = getSpacesForLength(valuesString.length());

      for (int i = 0, n = _columns.size(); i < n; i++) {
        DatabaseTableColumn column = (DatabaseTableColumn) _columns.get(i);

        sb.append(columnAsValueString(column.getName()));

        if (i < n - 1) {
          sb.append(",\n");
          sb.append(indent);
        }

      }

      sb.append(");\n");

      return sb.toString();

    } catch (DataSourceException e) {

      logThrowable(e);
      return "";
    }
  }

  public String getUpdateSQLText() {
    try {
      StringBuilder sb = new StringBuilder();
      sb.append("UPDATE ");
      sb.append(getNameForQuery());

      String setString = "SET ";
      sb.append("\n");
      sb.append(setString);

      String indent = getSpacesForLength(setString.length());

      List<DatabaseColumn> _columns = getColumns();

      for (int i = 0, n = _columns.size(); i < n; i++) {
        DatabaseTableColumn column = (DatabaseTableColumn) _columns.get(i);

        sb.append(column.getNameForQuery());
        sb.append(" = ");
        sb.append(columnAsValueString(column.getName()));

        if (i < n - 1) {
          sb.append(",\n");
          sb.append(indent);
        }

      }

      sb.append(";\n");

      return sb.toString();
    } catch (DataSourceException e) {

      logThrowable(e);
      return "";
    }
  }

  public String getSelectSQLText() {
    try {
      StringBuilder sb = new StringBuilder();
      sb.append("SELECT ");

      String indent = getSpacesForLength(sb.length());

      List<DatabaseColumn> _columns = getColumns();

      for (int i = 0, n = _columns.size(); i < n; i++) {
        DatabaseTableColumn column = (DatabaseTableColumn) _columns.get(i);

        sb.append(column.getNameForQuery());

        if (i < n - 1) {
          sb.append(",\n");
          sb.append(indent);
        }

      }

      sb.append("\nFROM ");
      sb.append(getNameForQuery());
      sb.append(";\n");

      return sb.toString();

    } catch (DataSourceException e) {

      logThrowable(e);
      return "";
    }

  }

  private String columnAsValueString(String column) {

    return toCamelCase(column);
  }

  private String getSpacesForLength(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(' ');
    }
    return sb.toString();
  }

  public DatabaseSource getDatabaseSource() {

    if (getParent() != null) {

      return (DatabaseSource) getParent().getParent();
    }

    return null;
  }

  public String getParentNameForStatement() {

    if (getParent() != null && getParent().getParent() != null) {

      return getParent().getParent().getName();
    }

    return null;
  }

  @Override
  public boolean hasSQLDefinition() {

    return true;
  }

  public String prepareStatement(List<String> columns, List<RecordDataItem> changes) {

    StringBuilder sb = new StringBuilder();
    sb.append("UPDATE ").append(getNameWithPrefixForQuery()).append(" SET ");
    for (String column : columns) {
      sb.append(MiscUtils.getFormattedObject(column)).append(" = ?,");
    }

    sb.deleteCharAt(sb.length() - 1);
    sb.append(" WHERE ");

    boolean applied = false;
    List<DatabaseColumn> cols = getColumns();
    for (int i = 0; i < cols.size(); i++) {
      DatabaseColumn column = cols.get(i);
      String col = MiscUtils.getFormattedObject(cols.get(i).getName());
      RecordDataItem rdi = changes.get(i);
      if (column.isGenerated())
        rdi.setGenerated(true);
      else {
        if (applied) {
          sb.append(" AND ");
        }
        if (rdi.isValueNull())
          sb.append(col).append(" is NULL ");
        else
          sb.append(col).append(" = ? ");
        applied = true;
      }
    }

    sb.deleteCharAt(sb.length() - 1);
    sb.append("\nORDER BY " + cols.get(0) + " \n");
    sb.append("ROWS 1");
    return sb.toString();
  }

  public String prepareStatementDeleting(List<RecordDataItem> changes) {

    StringBuilder sb = new StringBuilder();
    sb.append("DELETE FROM ").append(getNameWithPrefixForQuery());
    sb.append(" WHERE ");

    boolean applied = false;
    List<DatabaseColumn> cols = getColumns();
    for (int i = 0; i < cols.size(); i++) {
      DatabaseColumn column = cols.get(i);
      String col = MiscUtils.getFormattedObject(cols.get(i).getName());
      RecordDataItem rdi = changes.get(i);
      if (column.isGenerated())
        rdi.setGenerated(true);
      else {
        if (applied) {

          sb.append(" AND ");
        }
        if (rdi.isValueNull())
          sb.append(col).append(" is NULL ");
        else
          sb.append(col).append(" = ? ");
        applied = true;
      }
    }

    sb.deleteCharAt(sb.length() - 1);
    sb.append("\nORDER BY " + cols.get(0) + " \n");
    sb.append("ROWS 1");
    return sb.toString();
  }

  public String prepareStatementAdding(List<String> columns, List<RecordDataItem> changes) {

    StringBuilder sb = new StringBuilder();
    sb.append("INSERT INTO ").append(getNameWithPrefixForQuery());
    String columnsForQuery = " (";
    String values = " VALUES (";
    boolean applied = false;
    List<DatabaseColumn> cols = getColumns();
    for (int i = 0; i < cols.size(); i++) {
      DatabaseColumn column = cols.get(i);
      String col = MiscUtils.getFormattedObject(cols.get(i).getName());
      RecordDataItem rdi = changes.get(i);
      if (column.isGenerated())
        rdi.setGenerated(true);
      else {
        if (applied) {

          columnsForQuery += " , ";
          values += " , ";
        }
        columnsForQuery += col;
        values += "?";
        applied = true;
      }
    }
    columnsForQuery += ") ";
    values += ") ";
    sb.append(columnsForQuery).append(values);
    return sb.toString();
  }

  @Override
  public String prepareStatementWithPK(List<String> columns) {

    StringBuilder sb = new StringBuilder();
    sb.append("UPDATE ").append(getNameWithPrefixForQuery()).append(" SET ");
    for (String column : columns) {
      sb.append(MiscUtils.getFormattedObject(column)).append(" = ?,");
    }
    sb.deleteCharAt(sb.length() - 1);
    sb.append(" WHERE ");
    boolean applied = false;
    for (String primaryKey : getPrimaryKeyColumnNames()) {
      if (applied) {
        sb.append(" AND ");
      }
      sb.append(MiscUtils.getFormattedObject(primaryKey)).append(" = ? ");
      applied = true;
    }
    sb.deleteCharAt(sb.length() - 1);

    return sb.toString();
  }

  public String prepareStatementDeletingWithPK() {

    StringBuilder sb = new StringBuilder();
    sb.append("DELETE FROM ").append(getNameWithPrefixForQuery());
    sb.append(" WHERE ");

    boolean applied = false;
    for (String primaryKey : getPrimaryKeyColumnNames()) {

      if (applied) {

        sb.append(" AND ");
      }
      sb.append(MiscUtils.getFormattedObject(primaryKey)).append(" = ? ");
      applied = true;
    }

    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }

  public List<String> getPrimaryKeyColumnNames() {

    return namesFromConstraints(getPrimaryKeys());
  }

  public List<String> getForeignKeyColumnNames() {

    return namesFromConstraints(getForeignKeys());
  }

  private List<String> namesFromConstraints(List<ColumnConstraint> constraints) {

    List<String> names = new ArrayList<String>();
    for (ColumnConstraint constraint : constraints) {

      names.add(constraint.getColumnName());
    }

    return names;

  }

  @Override
  public int getTypeTree() {
    return typeTree;
  }

  @Override
  public void setTypeTree(int typeTree) {
    this.typeTree = typeTree;
  }

  @Override
  public DatabaseObject getDependObject() {
    return dependObject;
  }

  public void setDependObject(DatabaseObject dependObject) {
    this.dependObject = dependObject;
  }

  private List<DatabaseColumn> getDependedColumns() {
    ResultSet rs = null;

    List<DatabaseColumn> columns = new ArrayList<DatabaseColumn>();

    try {
      DatabaseMetaData dmd = getHost().getDatabaseMetaData();
      String packageField = "";
      if (dmd.getDatabaseMajorVersion() > 2)
        packageField = "and (T1.RDB$PACKAGE_NAME IS NULL)\n";
      Connection connection = dmd.getConnection();
      Statement statement = null;
      String firebirdSql = "select distinct \n" +
              "D.RDB$FIELD_NAME as FK_Field\n" +
              "from RDB$REF_CONSTRAINTS B, RDB$RELATION_CONSTRAINTS A, RDB$RELATION_CONSTRAINTS C,\n" +
              "RDB$INDEX_SEGMENTS D, RDB$INDEX_SEGMENTS E, RDB$INDICES I\n" +
              "where (A.RDB$CONSTRAINT_TYPE = 'FOREIGN KEY') and\n" +
              "(A.RDB$CONSTRAINT_NAME = B.RDB$CONSTRAINT_NAME) and\n" +
              "(B.RDB$CONST_NAME_UQ=C.RDB$CONSTRAINT_NAME) and (C.RDB$INDEX_NAME=D.RDB$INDEX_NAME) and\n" +
              "(A.RDB$INDEX_NAME=E.RDB$INDEX_NAME) and\n" +
              "(A.RDB$INDEX_NAME=I.RDB$INDEX_NAME)\n" +
              "and (A.RDB$RELATION_NAME = '" + dependObject.getName() + "')\n" +
              "and (C.RDB$RELATION_NAME = '" + getName() + "')\n" +
              "union all\n" +
              "select cast(t1.RDB$FIELD_NAME as varchar(64))\n" +
              "from RDB$DEPENDENCIES t1 where (t1.RDB$DEPENDENT_NAME = '" + dependObject.getName() + "')\n" +
              "and (t1.RDB$DEPENDENT_TYPE = 0)\n" +
              packageField +
              "and (T1.RDB$DEPENDED_ON_NAME = '" + getName() + "')\n" +
              "union all\n" +
              "select distinct cast(d.rdb$field_name as varchar(64))\n" +
              "from rdb$dependencies d, rdb$relation_fields f\n" +
              "where (d.rdb$dependent_type = 3) and\n" +
              "(d.rdb$dependent_name = f.rdb$field_source)\n" +
              "and (f.rdb$relation_name = '" + dependObject.getName() + "')\n" +
              "and (d.RDB$DEPENDED_ON_NAME = '" + getName() + "')\n" +
              "order by 1";

      statement = connection.createStatement();
      rs = statement.executeQuery(firebirdSql);


      while (rs.next()) {

        DefaultDatabaseColumn column = new DefaultDatabaseColumn();

        column.setName(rs.getString(1));

        columns.add(column);
      }
      releaseResources(rs, connection);

      int columnCount = columns.size();
      if (columnCount > 0) {

        // check for primary keys
        rs = dmd.getPrimaryKeys(null, null, getName());
        while (rs.next()) {

          String pkColumn = rs.getString(4);

          // find the pk column in the previous list
          for (int i = 0; i < columnCount; i++) {

            DatabaseColumn column = columns.get(i);
            String columnName = column.getName();

            if (columnName.equalsIgnoreCase(pkColumn)) {
              ((DefaultDatabaseColumn) column).setPrimaryKey(true);
              break;
            }

          }

        }
        releaseResources(rs, connection);

        // check for foreign keys
        rs = dmd.getImportedKeys(null, null, getName());
        while (rs.next()) {
          String fkColumn = rs.getString(8);

          // find the fk column in the previous list
          for (int i = 0; i < columnCount; i++) {
            DatabaseColumn column = columns.get(i);
            String columnName = column.getName();
            if (columnName.equalsIgnoreCase(fkColumn)) {
              ((DefaultDatabaseColumn) column).setForeignKey(true);
              break;
            }
          }

        }

      }

      return columns;

    } catch (SQLException e) {

      if (Log.isDebugEnabled()) {

        Log.error("Error retrieving column data for table " + getName()
                + " using connection " + getHost().getDatabaseConnection(), e);
      }

      return columns;

//            throw new DataSourceException(e);

    } finally {

      try {
        releaseResources(rs, getHost().getDatabaseMetaData().getConnection());
      } catch (SQLException throwables) {
        releaseResources(rs, null);
      }
    }
  }

  private List<DatabaseColumn> getDependentColumns() {
    ResultSet rs = null;

    List<DatabaseColumn> columns = new ArrayList<DatabaseColumn>();

    try {
      DatabaseMetaData dmd = getHost().getDatabaseMetaData();
      String packageField = "";
      if (dmd.getDatabaseMajorVersion() > 2) {
        packageField = "and (T1.RDB$PACKAGE_NAME IS NULL)\n";
      }
      Connection connection = dmd.getConnection();
      Statement statement = null;
      String firebirdSql = "select " +
              "E.RDB$FIELD_NAME as OnField\n" +
              "from RDB$REF_CONSTRAINTS B, RDB$RELATION_CONSTRAINTS A, RDB$RELATION_CONSTRAINTS C,\n" +
              "RDB$INDEX_SEGMENTS D, RDB$INDEX_SEGMENTS E\n" +
              "where (A.RDB$CONSTRAINT_TYPE = 'FOREIGN KEY') and\n" +
              "(A.RDB$CONSTRAINT_NAME = B.RDB$CONSTRAINT_NAME) and\n" +
              "(B.RDB$CONST_NAME_UQ=C.RDB$CONSTRAINT_NAME) and (C.RDB$INDEX_NAME=D.RDB$INDEX_NAME) and\n" +
              "(A.RDB$INDEX_NAME=E.RDB$INDEX_NAME)\n" +
              "and (C.RDB$RELATION_NAME = '" + dependObject.getName() + "')\n" +
              "and (A.RDB$RELATION_NAME = '" + getName() + "')\n" +
              "union all\n" +
              "select cast(t1.RDB$FIELD_NAME as varchar(64))\n" +
              "from RDB$DEPENDENCIES t1 where (t1.RDB$DEPENDENT_NAME = '" + dependObject.getName() + "')\n" +
              "and (t1.RDB$DEPENDENT_TYPE = 0)\n" +
              packageField +
              "and t1.RDB$DEPENDED_ON_NAME = '" + getName() + "'\n" +
              "union all\n" +
              "select distinct cast(d.rdb$field_name as varchar(64))\n" +
              "from rdb$dependencies d, rdb$relation_fields f\n" +
              "where (d.rdb$dependent_type = 3) and\n" +
              "(d.rdb$dependent_name = f.rdb$field_source)\n" +
              "and (f.rdb$relation_name = '" + dependObject.getName() + "')\n" +
              "and  d.rdb$depended_on_name = '" + getName() + "'\n" +
              "order by 1";

      statement = connection.createStatement();
      rs = statement.executeQuery(firebirdSql);


      while (rs.next()) {

        DefaultDatabaseColumn column = new DefaultDatabaseColumn();

        column.setName(rs.getString(1));

        columns.add(column);
      }
      releaseResources(rs, connection);

      int columnCount = columns.size();
      if (columnCount > 0) {

        // check for primary keys
        rs = dmd.getPrimaryKeys(null, null, getName());
        while (rs.next()) {

          String pkColumn = rs.getString(4);

          // find the pk column in the previous list
          for (int i = 0; i < columnCount; i++) {

            DatabaseColumn column = columns.get(i);
            String columnName = column.getName();

            if (columnName.equalsIgnoreCase(pkColumn)) {
              ((DefaultDatabaseColumn) column).setPrimaryKey(true);
              break;
            }

          }

        }
        releaseResources(rs, connection);

        // check for foreign keys
        rs = dmd.getImportedKeys(null, null, getName());
        while (rs.next()) {
          String fkColumn = rs.getString(8);

          // find the fk column in the previous list
          for (int i = 0; i < columnCount; i++) {
            DatabaseColumn column = columns.get(i);
            String columnName = column.getName();
            if (columnName.equalsIgnoreCase(fkColumn)) {
              ((DefaultDatabaseColumn) column).setForeignKey(true);
              break;
            }
          }

        }

      }

      return columns;

    } catch (SQLException e) {

      if (Log.isDebugEnabled()) {

        Log.error("Error retrieving column data for table " + getName()
                + " using connection " + getHost().getDatabaseConnection(), e);
      }

      return columns;

//            throw new DataSourceException(e);

    } finally {

      try {
        releaseResources(rs, getHost().getDatabaseMetaData().getConnection());
      } catch (SQLException throwables) {
        releaseResources(rs, null);
      }
    }
  }


  static final long serialVersionUID = -963831243178078154L;

}


/*
 * TableColumnIndex.java
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
import org.executequery.databaseobjects.NamedObject;
import org.underworldlabs.jdbc.DataSourceException;

import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author takisd
 */
public class TableColumnIndex extends AbstractDatabaseObjectElement {

  /**
   * Whether the index is non-unique
   */
  private boolean non_unique;

  /**
   * The indexed column
   */
  private List<String> columns;

  /**
   * Whether this a new index value
   */
  private boolean markedNew;

  /**
   * Whether this column is marked as to be deleted
   */
  private boolean markedDeleted;

  /**
   * the column meta data map
   */
  private Map<String, String> metaData;

  private String expression;

  private String constraint_type;

  /**
   * Creates a new instance of DatabaseTableColumnIndex
   */
  public TableColumnIndex(String name) {
    setName(name);
    columns = new ArrayList<>();
  }

  /**
   * Returns the meta data key name of this object.
   *
   * @return the meta data key name.
   */
  public String getMetaDataKey() {
    return "INDEX";
  }

  @Override
  public int getType() {
    return TABLE_INDEX;
  }

  /**
   * Returns the parent named object of this object.
   *
   * @return the parent object
   */
  public NamedObject getParent() {
    return null;
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

  public void addIndexedColumn(String column) {
    columns.add(column);
  }

  public void setIndexedColumns(List<String> columns) {
    this.columns = columns;
  }

  public List<String> getIndexedColumns() {
    return columns;
  }

  public String getIndexedColumn() {
    String column = "";
    if(columns!=null)
    for (int i = 0; i < columns.size(); i++) {
      if (i != 0)
        column += ",";
      column += columns.get(i);
    }
    return column;
  }

  public void setNonUnique(boolean non_unique) {
    this.non_unique = non_unique;
  }

  public boolean isNonUnique() {
    return non_unique;
  }

  public boolean isMarkedNew() {
    return markedNew;
  }

  public void setMarkedNew(boolean markedNew) {
    this.markedNew = markedNew;
  }

  public boolean isMarkedDeleted() {
    return markedDeleted;
  }

  public void setMarkedDeleted(boolean markedDeleted) {
    this.markedDeleted = markedDeleted;
  }

  public void setMetaData(Map<String, String> metaData) {
    this.metaData = metaData;
    for (String key : this.metaData.keySet()) {

      if (StringUtils.equalsIgnoreCase("TYPE", key)) {

        Short value = Short.valueOf(this.metaData.get(key));
        this.metaData.put(key, translateType(value));
      }

    }
  }

  private String translateType(Short value) {

    String translated = String.valueOf(value);
    switch (value) {
      case DatabaseMetaData.tableIndexStatistic:
        return translated + " - tableIndexStatistic";

      case DatabaseMetaData.tableIndexClustered:
        return translated + " - tableIndexClustered";

      case DatabaseMetaData.tableIndexHashed:
        return translated + " - tableIndexHashed";

      case DatabaseMetaData.tableIndexOther:
        return translated + " - tableIndexOther";
    }
    return translated;
  }

  public Map<String, String> getMetaData() {
    return metaData;
  }

  public void setConstraint_type(String constraint_type) {
    this.constraint_type = constraint_type;
  }

  public String getConstraint_type() {
    return constraint_type;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

  public String getExpression() {
    return expression;
  }
}



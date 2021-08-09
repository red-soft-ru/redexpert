/*
 * TableColumnIndexTableModel.java
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

package org.executequery.gui.databaseobjects;

import org.executequery.databaseobjects.impl.DefaultDatabaseIndex;
import org.executequery.gui.browser.AbstractDatabaseTableViewModel;
import org.executequery.localization.Bundles;

import java.util.List;

/**
 * @author takisd
 */
public class TableColumnIndexTableModel extends AbstractDatabaseTableViewModel {

  private static final String[] header = Bundles.get(TableColumnIndexTableModel.class, new String[]{
          "",
          "IndexName",
          "IndexedColumn",
          "Expression",
          "Unique"});
    /**
     * the table indexed columns
     */
    private List<DefaultDatabaseIndex> indexes;

  /**
   * Creates a new instance of DatabaseTableColumnIndexTableModel
   */
  public TableColumnIndexTableModel() {
  }

    public void setIndexData(List<DefaultDatabaseIndex> indexes) {
    if (this.indexes == indexes) {
      return;
    }
    this.indexes = indexes;
    fireTableDataChanged();
  }

  public int getRowCount() {
    if (indexes == null) {
      return 0;
    }
    return indexes.size();
  }

  public int getColumnCount() {
    return header.length;
  }

  public String getColumnName(int col) {
    return header[col];
  }

  public boolean isCellEditable(int row, int col) {
    return false;
  }

  public Object getValueAt(int row, int col) {
      DefaultDatabaseIndex index = indexes.get(row);
    switch (col) {
      case 0:
        return index.getConstraint_type();
      case 1:
        return index.getName();
      case 2:
          String cols = "";
          for (int i = 0; i < index.getIndexColumns().size(); i++) {
              if (i != 0)
                  cols += ",";
              cols += " " + index.getIndexColumns().get(i).getFieldName();
          }
          return cols;
      case 3:
        return index.getExpression();
      case 4:
          return Boolean.valueOf(index.isUnique());
      default:
        return null;
    }
  }

  public void setValueAt(Object value, int row, int col) {
    /*DefaultDatabaseIndex index = indexes.get(row);
    switch (col) {
      case 1:
        index.setName((String) value);
        break;
      case 2:
        index.addIndexedColumn((String) value);
        break;
      case 4:
        index.setNonUnique(((Boolean) value).booleanValue());
        break;
    }
    fireTableRowsUpdated(row, row);*/
  }

  public Class<?> getColumnClass(int col) {
    if (col == 4) {
      return Boolean.class;
    }
    return String.class;
  }

    public List<DefaultDatabaseIndex> getIndexes() {
        return indexes;
    }
}













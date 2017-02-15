package org.executequery.databaseobjects.impl;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by vasiliy on 15.02.17.
 */
public class DefaultDatabaseIndex extends DefaultDatabaseExecutable {

    public static class DatabaseIndexColumn {

        private double selectivity;
        private String fieldName;
        private int fieldPosition;

        DatabaseIndexColumn () {

        }

        public double getSelectivity() {
            return selectivity;
        }

        public void setSelectivity(double selectivity) {
            this.selectivity = selectivity;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public int getFieldPosition() {
            return fieldPosition;
        }

        public void setFieldPosition(int fieldPosition) {
            this.fieldPosition = fieldPosition;
        }
    }

    public static class IndexColumnsModel implements TableModel {

        private Set<TableModelListener> listeners = new HashSet<TableModelListener>();

        private List<DatabaseIndexColumn> columns;

        public IndexColumnsModel(List<DatabaseIndexColumn> columns) {
            this.columns = columns;
        }

        public void addTableModelListener(TableModelListener listener) {
            listeners.add(listener);
        }

        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        public int getColumnCount() {
            return 3;
        }

        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return "Field Name";
                case 1:
                    return "Statistic (selectivity)";
                case 2:
                    return "Field Position";
            }
            return "";
        }

        public int getRowCount() {
            return columns.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            DatabaseIndexColumn column = columns.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return column.getFieldName();
                case 1:
                    return column.getSelectivity();
                case 2:
                    return column.getFieldPosition();
            }
            return "";
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        public void removeTableModelListener(TableModelListener listener) {
            listeners.remove(listener);
        }

        public void setValueAt(Object value, int rowIndex, int columnIndex) {

        }

    }

    private int indexType;
    private List<DatabaseIndexColumn> columns = new ArrayList<>();

    private String tableName;
    private boolean isActive;
    private boolean isUnique;

    public DefaultDatabaseIndex (String name) {
        setName(name);
    }

    public List<DatabaseIndexColumn> getIndexColumns() {
        return columns;
    }

    public void setIndexColumns(List<DatabaseIndexColumn> columns) {
        this.columns = columns;
    }

    public int getIndexType() {
        return indexType;
    }

    public void setIndexType(int indexType) {
        this.indexType = indexType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isUnique() {
        return isUnique;
    }

    public void setUnique(boolean unique) {
        isUnique = unique;
    }

    public void loadColumns() {
        Statement statement = null;
        try {
            statement = this.getHost().getConnection().createStatement();
            ResultSet rs2 = statement.executeQuery("select i.rdb$index_name,\n" +
                    "i.rdb$relation_name,\n" +
                    "i.rdb$unique_flag,\n" +
                    "i.rdb$index_inactive,\n" +
                    "i.rdb$index_type,\n" +
                    "isg.rdb$field_name,\n" +
                    "isg.rdb$field_position,\n" +
                    "i.rdb$statistics,\n" +
                    "i.rdb$expression_source,\n" +
                    "c.rdb$constraint_type,\n" +
                    "i.rdb$description\n" +
                    "from rdb$indices i\n" +
                    "left join rdb$relation_constraints c on i.rdb$index_name = c.rdb$index_name\n" +
                    "left join rdb$index_segments isg on isg.rdb$index_name = i.rdb$index_name\n" +
                    "where (i.rdb$index_name = '" + getName() + "')\n" +
                    "order by isg.rdb$field_position");

            List<DefaultDatabaseIndex.DatabaseIndexColumn> columns = new ArrayList<>();
            while (rs2.next()) {
                DefaultDatabaseIndex.DatabaseIndexColumn column = new DefaultDatabaseIndex.DatabaseIndexColumn();
                column.setFieldName(rs2.getString(6));
                column.setSelectivity(rs2.getDouble(8));
                column.setFieldPosition(rs2.getInt(7));

                columns.add(column);
            }

            this.setIndexColumns(columns);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getCreateSQLText() {
        return "";
    }

    public int getType() {
        return INDEX;
    }

    public String getMetaDataKey() {
        return META_TYPES[INDEX];
    }
}

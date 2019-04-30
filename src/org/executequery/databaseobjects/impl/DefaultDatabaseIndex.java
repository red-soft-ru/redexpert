package org.executequery.databaseobjects.impl;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.log.Log;

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
public class DefaultDatabaseIndex extends AbstractDatabaseObject {

    public static class DatabaseIndexColumn {

        private double selectivity;
        private String fieldName;
        private int fieldPosition;

        DatabaseIndexColumn() {

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
    private String expression;
    private String constraint_type;

    public DefaultDatabaseIndex(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
    }

    public List<DatabaseIndexColumn> getIndexColumns() {
        return columns;
    }

    public void setIndexColumns(List<DatabaseIndexColumn> columns) {
        this.columns = columns;
    }

    public int getIndexType() {
        if (isMarkedForReload())
            getObjectInfo();
        return indexType;
    }

    public void setIndexType(int indexType) {
        this.indexType = indexType;
    }

    public String getTableName() {
        if (isMarkedForReload())
            getObjectInfo();
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public boolean isActive() {
        if (isMarkedForReload())
            getObjectInfo();
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isUnique() {
        if (isMarkedForReload())
            getObjectInfo();
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
                String string = rs2.getString(6);
                if (string != null)
                    column.setFieldName(string.trim());
                column.setSelectivity(rs2.getDouble(8));
                column.setFieldPosition(rs2.getInt(7));
                setExpression(rs2.getString(9));
                columns.add(column);
            }

            this.setIndexColumns(columns);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null)
                try {
                    if (!statement.isClosed())
                        statement.close();
                } catch (SQLException e) {
                    Log.error("Error closing statement in method loadColumns of DefaultDatabaseIndex class", e);
                }
        }
    }

    public void setExpression(String expression) {
        if (expression == null)
            return;
        expression = expression.trim().substring(1, expression.trim().length() - 1);
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

    public void setConstraint_type(String constraint_type) {
        this.constraint_type = constraint_type;
    }

    public String getConstraint_type() {
        if (isMarkedForReload())
            getObjectInfo();
        return constraint_type;
    }

    @Override
    public String getCreateSQLText() {
        return "";
    }

    @Override
    protected String queryForInfo() {
        return "select " +
                "0, " +
                "I.RDB$RELATION_NAME, " +
                "0," +
                "I.RDB$INDEX_TYPE," +
                "I.RDB$UNIQUE_FLAG," +
                "I.RDB$INDEX_INACTIVE," +
                "I.RDB$DESCRIPTION," +
                "C.RDB$CONSTRAINT_TYPE\n" +
                "FROM RDB$INDICES AS I LEFT JOIN rdb$relation_constraints as c on i.rdb$index_name=c.rdb$index_name\n" +
                "where I.RDB$INDEX_NAME='" + getName().trim() + "'";
    }

    @Override
    protected void setInfoFromResultSet(ResultSet rs) {
        try {
            if (rs != null && rs.next()) {
                setTableName(rs.getString(2));
                setIndexType(rs.getInt(4));
                setActive(rs.getInt(6) != 1);
                setUnique(rs.getInt(5) == 1);
                setRemarks(rs.getString(7));
                setConstraint_type(rs.getString(8));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int getType() {
        if (isSystem()) {
            return SYSTEM_INDEX;
        } else return INDEX;
    }

    public String getMetaDataKey() {
        return META_TYPES[getType()];
    }

    @Override
    public boolean allowsChildren() {
        return false;
    }

    protected void getObjectInfo() {
        try {
            super.getObjectInfo();
            loadColumns();
        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog("Error loading info about Index", e);
        } finally {
            setMarkedForReload(false);
        }
    }
}

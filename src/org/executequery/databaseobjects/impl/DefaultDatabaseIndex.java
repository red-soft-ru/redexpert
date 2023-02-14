package org.executequery.databaseobjects.impl;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.util.MiscUtils;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.sql.ResultSet;
import java.sql.SQLException;
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

        private final Set<TableModelListener> listeners = new HashSet<TableModelListener>();

        private final List<DatabaseIndexColumn> columns;

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

        private String bundleString(String key){

            return Bundles.get(IndexColumnsModel.class,key);
        }

        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return bundleString("FieldName");
                case 1:
                    return bundleString("StatisticSelectivity");
                case 2:
                    return bundleString("FieldPosition");
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
    private boolean markedReloadActive;
    private String tablespace;

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
        if (isMarkedReloadActive())
            getObjectInfo();
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
        setMarkedReloadActive(false);
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
        DefaultStatementExecutor querySender = new DefaultStatementExecutor();
        querySender.setDatabaseConnection(getHost().getDatabaseConnection());
        try {
            ResultSet rs2 = querySender.getResultSet("select i.rdb$index_name,\n" +
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
                    "order by isg.rdb$field_position").getResultSet();

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
            querySender.releaseResources();
        }
    }

    public boolean isMarkedReloadActive() {
        return markedReloadActive;
    }

    public void setMarkedReloadActive(boolean markedReloadActive) {
        this.markedReloadActive = markedReloadActive;
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

        String query = "CREATE ";
        if (isUnique())
            query += "UNIQUE ";
        if (getIndexType() == 1)
            query += "DESCENDING ";
        query += "INDEX " + MiscUtils.getFormattedObject(getName()) +
                " ON " + MiscUtils.getFormattedObject(getTableName().trim()) + " ";
        if (getExpression() != null) {
            query += "COMPUTED BY (" + getExpression() + ")";
        } else {
            query += "(";
            StringBuilder fieldss = new StringBuilder();
            boolean first = true;
            for (int i = 0; i < getIndexColumns().size(); i++) {
                if (!first)
                    fieldss.append(",");
                first = false;
                fieldss.append(MiscUtils.getFormattedObject(getIndexColumns().get(i).getFieldName()));
            }
            query += fieldss + ")";
        }
        if (getTablespace() != null)
            query += "\nTABLESPACE " + MiscUtils.getFormattedObject(getTablespace());
        query += ";";
        if (!isActive())
            query += "ALTER INDEX " + MiscUtils.getFormattedObject(getName()) + " INACTIVE;";
        if (!MiscUtils.isNull(getRemarks()))
            query += "COMMENT ON INDEX " + MiscUtils.getFormattedObject(getName()) + " IS '" + getRemarks() + "'";
        return query;
    }

    @Override
    protected String queryForInfo() {
        String tablespace_query = "";
        try {
            if (getHost().getDatabaseProductName().toLowerCase().contains("reddatabase") && getHost().getDatabaseMajorVersion() >= 4)
                tablespace_query = ", I.RDB$TABLESPACE_NAME";
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String query = "select " +
                "0, " +
                "I.RDB$RELATION_NAME, " +
                "0," +
                "I.RDB$INDEX_TYPE," +
                "I.RDB$UNIQUE_FLAG," +
                "I.RDB$INDEX_INACTIVE," +
                "I.RDB$DESCRIPTION," +
                "C.RDB$CONSTRAINT_TYPE" +
                tablespace_query +
                "\nFROM RDB$INDICES AS I LEFT JOIN rdb$relation_constraints as c on i.rdb$index_name=c.rdb$index_name\n" +
                "where I.RDB$INDEX_NAME='" + getName().trim() + "'";
        return query;

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
                if (getHost().getDatabaseProductName().toLowerCase().contains("reddatabase") && getHost().getDatabaseMajorVersion() >= 4)
                    setTablespace(rs.getString(9));
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

    public String getTablespace() {
        return tablespace;
    }

    public void setTablespace(String tablespace) {
        this.tablespace = tablespace;
    }

    public boolean setStatistics() {
        boolean res = true;
        DefaultStatementExecutor querySender = new DefaultStatementExecutor(getHost().getDatabaseConnection());
        String query = "SET STATISTICS INDEX " + MiscUtils.getFormattedObject(getName());
        try {
            SqlStatementResult result = querySender.execute(QueryTypes.SET_STATISTICS, query);
            if (result.isException()) {
                res = false;
                result.getSqlException().printStackTrace();
            } else Log.info("Executing:\"" + query + "\"");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            querySender.releaseResources();
        }
        return res;
    }

    public void reset() {
        super.reset();
        setMarkedReloadActive(true);
    }
}

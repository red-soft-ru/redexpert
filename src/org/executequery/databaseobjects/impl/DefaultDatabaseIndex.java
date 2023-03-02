package org.executequery.databaseobjects.impl;

import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.sql.SqlStatementResult;
import org.executequery.sql.sqlbuilder.*;
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
    private static final String RELATION_NAME = "RELATION_NAME";

    private String tableName;
    private boolean isActive;
    private boolean isUnique;
    private String expression;
    private String constraint_type;
    private boolean markedReloadActive;
    private String tablespace;
    private static final String INDEX_TYPE = "INDEX_TYPE";

    public DefaultDatabaseIndex(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
    }
    private static final String UNIQUE_FLAG = "UNIQUE_FLAG";

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
    private static final String INDEX_INACTIVE = "INDEX_INACTIVE";

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


    public boolean isMarkedReloadActive() {
        return markedReloadActive;
    }

    public void setMarkedReloadActive(boolean markedReloadActive) {
        this.markedReloadActive = markedReloadActive;
    }
    private static final String STATISTICS = "STATISTICS";
    private static final String EXPRESSION_SOURCE = "EXPRESSION_SOURCE";

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
        if (!MiscUtils.isNull(getExpression())) {
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
        if (!MiscUtils.isNull(getCondition()))
            query += "\nWHERE " + getCondition();
        if (!MiscUtils.isNull(getTablespace()))
            query += "\nTABLESPACE " + MiscUtils.getFormattedObject(getTablespace());
        query += ";";
        if (!isActive())
            query += "ALTER INDEX " + MiscUtils.getFormattedObject(getName()) + " INACTIVE;";
        if (!MiscUtils.isNull(getRemarks()))
            query += "COMMENT ON INDEX " + MiscUtils.getFormattedObject(getName()) + " IS '" + getRemarks() + "'";
        return query;
    }
    private static final String CONDITION_SOURCE = "CONDITION_SOURCE";
    private static final String TABLESPACE_NAME = "TABLESPACE_NAME";
    private static final String CONSTRAINT_TYPE = "CONSTRAINT_TYPE";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String FIELD_NAME = "FIELD_NAME";
    private static final String FIELD_POSITION = "FIELD_POSITION";
    private List<DatabaseIndexColumn> columns;
    private String condition;

    public List<DatabaseIndexColumn> getIndexColumns() {
        checkOnReload(columns);
        return columns;
    }

    public String getTableName() {
        checkOnReload(tableName);
        return tableName;
    }

    public String getExpression() {
        checkOnReload(expression);
        return expression;
    }

    public void setExpression(String expression) {
        if (MiscUtils.isNull(expression))
            return;
        expression = expression.trim().substring(1, expression.trim().length() - 1);
        this.expression = expression;
    }

    @Override
    protected String queryForInfo() {
        SelectBuilder sb = new SelectBuilder();
        Table indicies = Table.createTable("RDB$INDICES", "I");
        Table constraints = Table.createTable("RDB$RELATION_CONSTRAINTS", "RC");
        Table indexSegments = Table.createTable("RDB$INDEX_SEGMENTS", "ISGMT");

        sb.appendField(Field.createField(indicies, RELATION_NAME));
        sb.appendField(Field.createField(indicies, INDEX_TYPE));
        sb.appendField(Field.createField(indicies, UNIQUE_FLAG));
        sb.appendField(Field.createField(indicies, INDEX_INACTIVE));
        sb.appendField(Field.createField(indicies, STATISTICS));
        sb.appendField(Field.createField(indicies, EXPRESSION_SOURCE));
        sb.appendField(Field.createField(indicies, DESCRIPTION));
        sb.appendField(Field.createField(indicies, CONDITION_SOURCE).setNull(getDatabaseMajorVersion() < 5));
        sb.appendField(Field.createField(indicies, TABLESPACE_NAME).setNull(!getHost().getDatabaseProductName().toLowerCase().contains("reddatabase")
                || getDatabaseMajorVersion() < 4));
        sb.appendField(Field.createField(constraints, CONSTRAINT_TYPE));
        sb.appendField(Field.createField(indexSegments, FIELD_NAME));
        sb.appendField(Field.createField(indexSegments, FIELD_POSITION));
        Field indexName = Field.createField(indicies, "INDEX_NAME");
        sb.appendJoin(LeftJoin.createLeftJoin().appendFields(indexName, Field.createField(constraints, indexName.getAlias())));
        sb.appendJoin(LeftJoin.createLeftJoin().appendFields(indexName, Field.createField(indexSegments, indexName.getAlias())));

        sb.appendCondition(Condition.createCondition(indexName, "=", "?"));
        sb.setOrdering(Field.createField(indexSegments, FIELD_POSITION).getFieldTable());
        String query = sb.getSQLQuery();
        return query;

    }

    @Override
    protected void setInfoFromResultSet(ResultSet rs) {
        try {
            boolean first = true;
            List<DefaultDatabaseIndex.DatabaseIndexColumn> columns = new ArrayList<>();
            while (rs.next()) {
                if (first) {
                    setTableName(getFromResultSet(rs, RELATION_NAME));
                    setIndexType(rs.getInt(INDEX_TYPE));
                    setActive(rs.getInt(INDEX_INACTIVE) != 1);
                    setUnique(rs.getInt(UNIQUE_FLAG) == 1);
                    setRemarks(getFromResultSet(rs, DESCRIPTION));
                    setConstraint_type(getFromResultSet(rs, CONSTRAINT_TYPE));
                    setExpression(getFromResultSet(rs, EXPRESSION_SOURCE));
                    setTablespace(getFromResultSet(rs, TABLESPACE_NAME));
                    setCondition(getFromResultSet(rs, CONDITION_SOURCE));
                }
                first = false;
                DefaultDatabaseIndex.DatabaseIndexColumn column = new DefaultDatabaseIndex.DatabaseIndexColumn();
                String name = rs.getString(FIELD_NAME);
                if (name != null)
                    column.setFieldName(name.trim());
                column.setSelectivity(rs.getDouble(STATISTICS));
                column.setFieldPosition(rs.getInt(FIELD_POSITION));
                columns.add(column);
            }
            setIndexColumns(columns);
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

    public String getTablespace() {
        checkOnReload(tablespace);
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

    public String getCondition() {
        checkOnReload(condition);
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
